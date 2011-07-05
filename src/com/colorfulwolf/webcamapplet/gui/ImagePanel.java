package com.colorfulwolf.webcamapplet.gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;


@SuppressWarnings("serial")
public class ImagePanel extends JPanel
{
	private Image img;
	
	private boolean center = true;
	
	public ImagePanel() {}
	
	public ImagePanel(Image img)
	{
		this.img = img;
		this.setBackground(Color.white);
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{	
		super.paintComponent(g);
		
		if (img == null)
			return;
		
		if (center)
		{
			Dimension size = this.getSize();
			int width = img.getWidth(this);
			int height = img.getHeight(this);
			
			g.drawImage(img, size.width / 2 - width / 2, size.height / 2 - height / 2, this);
		}
		else
		{	
			g.drawImage(img,0,0,this);
		}
	}
	
	public void setImage(Image img)
	{
		this.img = img;
	}
}
