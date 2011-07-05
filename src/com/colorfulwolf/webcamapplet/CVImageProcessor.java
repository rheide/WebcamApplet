package com.colorfulwolf.webcamapplet;

import com.googlecode.javacv.jna.cxcore.IplImage;

/**
 * Can modify OpenCV image data. Multiple CVImageProcessors can be chained together.
 * 
 * @author Randy van der Heide
 *
 */
public interface CVImageProcessor
{
	public IplImage process(IplImage in);
	
	public void setNext(CVImageProcessor proc);
}
