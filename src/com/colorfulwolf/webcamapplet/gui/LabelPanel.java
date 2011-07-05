package com.colorfulwolf.webcamapplet.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class LabelPanel extends JPanel
{
	public LabelPanel(String labelText, JComponent mainComponent)
	{
		super(new BorderLayout());
		this.add(mainComponent,BorderLayout.CENTER);
		JLabel label = new JLabel(labelText);
		this.add(label,BorderLayout.WEST);
		label.setBackground(Color.white);
		this.setBackground(Color.white);
	}
	
	public LabelPanel(String labelText, int labelWidth, JComponent mainComponent)
	{
		super(new BorderLayout());
		this.add(mainComponent,BorderLayout.CENTER);
		JLabel label = new JLabel(labelText);
		this.add(label,BorderLayout.WEST);
		label.setPreferredSize(new Dimension(labelWidth,18));		
		label.setBackground(Color.white);
		this.setBackground(Color.white);
	}
}
