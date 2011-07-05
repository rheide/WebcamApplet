package com.colorfulwolf.webcamapplet;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.colorfulwolf.webcamapplet.gui.ImagePanel;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.jna.cxcore.IplImage;

/**
 * A Swing UI component that displays images captured from a webcam via OpenCV. 
 * <p>
 * This class uses the CVImageProcessor interface to process the webcam image before it is output.
 * </p>
 * @author Randy van der Heide
 *
 */
@SuppressWarnings("serial")
public class OpenCVWebCam extends ImagePanel
{
	private boolean running = false;
	private Thread runner = null;
	
	private OpenCVFrameGrabber grabber;
	
	private CVImageProcessor imageProcessor = null;
	
	public OpenCVWebCam(int device, int width, int height)
	{
		this.grabber = new OpenCVFrameGrabber(device);
		grabber.setImageWidth(width);
		grabber.setImageHeight(height);
		this.setBackground(Color.white);
	}
	
	public void setImageProcessor(CVImageProcessor imageProcessor)
	{
		this.imageProcessor = imageProcessor;
	}
	
	public CVImageProcessor getImageProcessor()
	{
		return imageProcessor;
	}
	
	private void grabAndPaint()
	{
		try
		{
			BufferedImage out;
			
			//grab the raw image from the webcam
			IplImage frame = grabber.grab();
			
			//if an image processor has been defined, start processing the image
			if (imageProcessor != null)
				frame = imageProcessor.process(frame);
			
			//output the final result as a bufferedimage
			out = frame.getBufferedImage();
			
			this.setImage(out);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		this.repaint();
	}
	
	/**
	 * Start grabbing frames from the webcam. 
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception
	{
		if (running)
			return;
		
		grabber.start();
		
		running = true;
		runner = new Thread()
		{
			public void run()
			{
				while (running)
				{	
					grabAndPaint();
					Thread.yield();
				}
				try { grabber.stop(); } 
				catch (Exception e) { e.printStackTrace(); }
				runner = null;
			}
		};
		runner.start();
	}
	
	public boolean isRunnning()
	{
		return runner != null;
	}
	
	public void stop()
	{
		running = false;
	}
		
}
