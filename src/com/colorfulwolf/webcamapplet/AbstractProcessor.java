package com.colorfulwolf.webcamapplet;

import com.googlecode.javacv.jna.cxcore.IplImage;

/**
 * CVImageProcessor that automatically forwards the image to the next processor.
 * 
 * @author Randy van der Heide
 *
 */
public abstract class AbstractProcessor implements CVImageProcessor
{
	private CVImageProcessor nextCVProcessor = null;
	
	@Override
	public final IplImage process(IplImage in)
	{
		in = processImage(in);
		if (nextCVProcessor != null)
			return nextCVProcessor.process(in);
		else
			return in;
	}
	
	public abstract IplImage processImage(IplImage in);
		
	@Override
	public void setNext(CVImageProcessor proc)
	{
		this.nextCVProcessor = proc;
	}
}
