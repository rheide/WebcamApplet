package com.colorfulwolf.webcamapplet;

import java.util.ArrayList;
import java.util.List;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.googlecode.javacv.jna.cxcore.IplImage;

/**
 * CVImageProcessor that detects QR codes.
 * <p>
 * When a QR code is found in an image, an event will be sent to all SquareCodeListeners.
 * </p>
 * 
 * @author Randy van der Heide
 *
 */
public class QRCodeProcessor extends AbstractProcessor
{
	private List<QRCodeListener> listeners = new ArrayList<QRCodeListener>();
	
	private long lastFoundTime = 0;
	private final static long CLEAR_RESULT_TIMEOUT = 5000;
	private String lastResult = "";
	
	public QRCodeProcessor()
	{

	}
	
	public void addSquareCodeListener(QRCodeListener sql)
	{
		listeners.add(sql);
	}
	
	public void removeSquareCodeListener(QRCodeListener sql)
	{
		listeners.remove(sql);
	}
	
	private void fire(String text)
	{
		List<QRCodeListener> tmp = new ArrayList<QRCodeListener>(listeners);
		for (QRCodeListener sql : tmp) 
		{
			sql.qrCodeDetected(text);
		}
	}
	
	
	@Override
	public IplImage processImage(IplImage in)
	{
		LuminanceSource source = new BufferedImageLuminanceSource(in.getBufferedImage());
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result result;
		try
		{
			result = new MultiFormatReader().decode(bitmap);
			lastResult = result.getText();
			lastFoundTime = System.currentTimeMillis();
			fire(result.getText());
		}
		catch (NotFoundException e)
		{
			//that's ok
			if (!lastResult.equals("")) //if result was not empty, clear old result
			{
				long mt1 = System.currentTimeMillis();
				if (mt1 - lastFoundTime > CLEAR_RESULT_TIMEOUT)
				{
					lastFoundTime = mt1;
					fire("");
					lastResult = "";
				}
			}
		}
		
		return in;
	}
}
