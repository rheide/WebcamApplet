package com.colorfulwolf.webcamapplet.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * A basic loading screen that displays a logo, a progressbar and a status.
 * 
 * @author Randy van der Heide
 *
 */
@SuppressWarnings("serial")
public class LoadingScreen extends JPanel
{
	private int progress = 20;
	private int maxProgress = 100;
	
	private String statusText = "Loading..";
	
	private Image logo = null;
	
	public LoadingScreen()
	{
		this.setBackground(Color.white);
		try
		{
			InputStream is = LoadingScreen.class.getResourceAsStream("/logo.png");
			logo = ImageIO.read(is);
		}
		catch (IOException e) { e.printStackTrace(); }
	}
	
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		Dimension size = this.getSize();
		int xInc = size.width / 16;
		int yInc = size.height / 16;
		
		if (logo != null)
		{
			g.drawImage(logo, xInc, yInc, this);
		}
		
		Font font = new Font("Arial", Font.PLAIN, 24);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics(font);
		
		String text = "Please wait. Loading...";
		int textWidth = fm.stringWidth(text);
		
		Color textColor = new Color(180,0,0);
		g.setColor(textColor);
		g.drawString(text, size.width / 2 - textWidth / 2, size.height / 2 - yInc); 
		
		
		//draw progressbar
		int pbWidth = xInc * 14;
		int pbHeight = yInc * 1;
		
		int sx = size.width / 2 - pbWidth / 2;
		int sy = size.height / 2 - pbHeight / 2;
		
		g.setColor(new Color(0,0,180));
		g.drawRect(sx, sy, pbWidth, pbHeight);
		
		int fillWidth = (int)(((double)progress / (double)maxProgress) * pbWidth);
		g.fillRect(sx, sy, fillWidth, pbHeight);
		
		//draw status text
		font = new Font("Arial", Font.PLAIN, 18);
		g.setFont(font);
		fm = g.getFontMetrics(font);
		textWidth = fm.stringWidth(statusText);
		g.setColor(textColor);
		
		g.drawString(statusText, size.width / 2 - textWidth / 2, sy + pbHeight + yInc);
	}
	
	public void setMaxProgress(int maxProgress)
	{
		this.maxProgress = maxProgress;
		this.repaint();
	}
	
	public void setProgress(int progress, String text)
	{
		this.progress = progress;
		this.statusText = text;
		this.repaint();
	}
}
