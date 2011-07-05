package com.colorfulwolf.webcamapplet;

/**
 * Interface for classes that want to receive events when a QR code has been detected. 
 * 
 * @author Randy van der Heide
 *
 */
public interface QRCodeListener
{
	public void qrCodeDetected(String text);
}
