package com.colorfulwolf.webcamapplet;

import static com.googlecode.javacv.jna.highgui.cvCreateCameraCapture;
import static com.googlecode.javacv.jna.highgui.cvGrabFrame;
import static com.googlecode.javacv.jna.highgui.cvReleaseCapture;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.colorfulwolf.webcamapplet.gui.LabelPanel;
import com.colorfulwolf.webcamapplet.gui.LoadingScreen;
import com.googlecode.javacv.jna.highgui.CvCapture;

/**
 * Main applet entry point.
 * 
 * @author Randy van der Heide
 *
 */
public class WebcamApplet extends JApplet implements QRCodeListener
{	
	private static final long serialVersionUID = 1L;

	private final static String DEFAULT_DOWNLOAD_PATH = "http://colorfulwolf.com/dev/cam/";
	
	private final static String VERSION_ID = "1.0.0";
	
	// note that this list is windows-specific, so this is not a generic solution that works on all OSes
	private final static String[] LIBS = {
		"cv210.dll",
		"cvaux210.dll",
		"cxcore210.dll",
		"cxts210.dll",
		"highgui210.dll",
		"ml210.dll"
		};
	
	private final static String LIB_ARCHIVE = "opencv21.zip";
	
	Object cam;
	String lastValue = "";
	
	private LoadingScreen loadingScreen;
		
	private List<Integer> devices = new ArrayList<Integer>();
	
	private JPanel buttonPanel;
	private JLabel statusLabel = new JLabel("");
	
	public void init()
	{
		setSize(800,600);
		setBackground(Color.white);
		getContentPane().setBackground(Color.white);
		
		loadingScreen = new LoadingScreen();
		getContentPane().add(loadingScreen, BorderLayout.CENTER);
		
		Runnable loader = new Runnable()
		{	
			@Override
			public void run()
			{
				loadWebcam(); // webcam initialization happens in a background thread
			}
		};
		new Thread(loader).start();
	}
	
	private void loadWebcam()
	{
		loadingScreen.setMaxProgress(7);
		loadingScreen.setProgress(1, "Loading libraries..");
		
		String tmpDir = System.getProperty("java.io.tmpdir");
		File faPath = new File(tmpDir + File.separator + "WebcamApplet_" + VERSION_ID.replaceAll("\\.", "-"));
		System.out.println(faPath);
		System.setProperty("jna.library.path", faPath.getAbsolutePath());
		
		//the actual download path can be set in a parameter in the applet properties
		String downloadPath = this.getParameter("dll_path");
		if (downloadPath == null)
			downloadPath = DEFAULT_DOWNLOAD_PATH;
		
		try
		{
			prepareLibraries(faPath, downloadPath);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			loadingScreen.setProgress(3, "Error: "+e.getMessage());
			return;
		}
		
		loadingScreen.setProgress(4, "Discovering devices..");
		discoverDevices();
		if (devices.size() == 0)
		{
			loadingScreen.setProgress(4, "Error: no webcam devices found!");
			return;
		}
		
		
		loadingScreen.setProgress(5, "Initializing webcam..");
		OpenCVWebCam webCam = new OpenCVWebCam(devices.get(0), 640, 480); 
		this.cam = webCam;
		
		//initialize the processor chain
		QRCodeProcessor proc = new QRCodeProcessor();
		proc.addSquareCodeListener(this);
		webCam.setImageProcessor(proc);
		
		loadingScreen.setProgress(6, "Preparing display..");
		try { webCam.start(); } //start capturing images
		catch (Exception e) 
		{
			e.printStackTrace();
			loadingScreen.setProgress(6, "Error: "+e.getMessage());
			return;
		}
		
		initButtonPanel();
		
		loadingScreen.setProgress(7, "Complete!");
		
		//remove the loading screen and load the main UI
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				getContentPane().remove(loadingScreen);
				getContentPane().add((JComponent)cam, BorderLayout.CENTER);
				getContentPane().add(buttonPanel,BorderLayout.NORTH);
				getContentPane().add(statusLabel, BorderLayout.SOUTH);
				statusLabel.setText("Webcam initialized");
				getContentPane().validate();
				getContentPane().repaint();
			}
		});
	}
	
	private void initWebcam(int device, int width, int height) throws Exception
	{	
		OpenCVWebCam webCam = (OpenCVWebCam)cam;
		if (webCam != null)
		{
			statusLabel.setText("Waiting for webcam to stop..");
			getContentPane().remove(webCam);
			webCam.stop();
			while (webCam.isRunnning())
			{
				try { Thread.sleep(100); } catch (Exception ee) {}
			}
		}
		
		statusLabel.setText("Initializing webcam..");
		webCam = new OpenCVWebCam(device, width, height);
		getContentPane().add(webCam, BorderLayout.CENTER);
		webCam.start();
	}
	
	private void initButtonPanel()
	{
		buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBackground(Color.white);
		statusLabel.setBackground(Color.white);
		
		final JComboBox deviceBox = new JComboBox();
		for (int dev : devices)
			deviceBox.addItem("Device "+dev);
		deviceBox.setPreferredSize(new Dimension(100,20));
		deviceBox.setBackground(Color.white);
		
		final JTextField widthField = new JTextField();
		final JTextField heightField = new JTextField();
		widthField.setPreferredSize(new Dimension(64,22));
		heightField.setPreferredSize(new Dimension(64,22));
		widthField.setText("640");
		heightField.setText("480");
		
		widthField.setBackground(Color.white);
		heightField.setBackground(Color.white);
		
		JButton startB = new JButton("Restart");
		startB.setPreferredSize(new Dimension(80,22));
		
		buttonPanel.add(deviceBox);
		buttonPanel.add(new LabelPanel("   Width: ",widthField));
		buttonPanel.add(new LabelPanel("   Height: ",heightField));
		buttonPanel.add(startB);
		
		startB.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try 
				{
					int device = devices.get(deviceBox.getSelectedIndex());
					int width = Integer.parseInt(widthField.getText());
					int height = Integer.parseInt(heightField.getText());
					initWebcam(device, width, height);
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
					JOptionPane.showMessageDialog(WebcamApplet.this, "An error occurred: "+ee.getMessage(),
							"Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

	/**
	 * Find out if a webcam device is available.
	 */
	private void discoverDevices()
	{
		for (int i=0;i<10;i++)
		{
			CvCapture cap = null;
			try 
			{
				cap = cvCreateCameraCapture(i);
				int res =cvGrabFrame(cap);
				if (res > 0)
					devices.add(i);
				System.out.println("Device "+i+": "+res);
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			finally
			{
				if (cap != null)
				{
					try { cvReleaseCapture(cap.pointerByReference()); }
					catch (Exception e) { e.printStackTrace(); }
				}
			}
		}
	}
	
	private void prepareLibraries(File localPath, String downloadPath) throws Exception
	{
		if (localPath.exists())
		{
			boolean libMissing = false;
			for (String lib : LIBS)
			{
				File libFile = new File(localPath.getAbsolutePath() + File.separator + lib);
				if (!libFile.exists())
				{
					libMissing = true;
					break;
				}
			}
			
			if (!libMissing) return; //we don't have to download
		}
		
		if (!localPath.exists() && !localPath.mkdirs()) //fatal error!	
			throw new Exception("Could not create library path: "+localPath);
		
		loadingScreen.setProgress(2, "Downloading libraries..");
		File file = new File(localPath.getAbsolutePath() + File.separator + LIB_ARCHIVE);
		String link = downloadPath + LIB_ARCHIVE;
		download(link, file);
		
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		
		loadingScreen.setProgress(3, "Installing libraries..");
		while (entries.hasMoreElements())
		{
			ZipEntry entry = entries.nextElement();
			if (entry.isDirectory()) continue;
			
			File tar = new File(localPath.getAbsolutePath() + File.separator + entry.getName());
			InputStream is = zipFile.getInputStream(entry);
			OutputStream os = new FileOutputStream(tar);
			copyStream(is, os);
			os.flush();
			os.close();
			is.close();
		}
		zipFile.close();
		
		file.delete();
		if (file.exists())
			file.deleteOnExit();
		
		
	}

	private void download(String link, File localFile) throws Exception
	{
		System.out.println("Downloading "+link);
		URL url = new URL(link);
		
		InputStream is = url.openStream();
		FileOutputStream fos = new FileOutputStream(localFile);
		
		copyStream(new BufferedInputStream(is), fos);
		
		fos.flush();
		fos.close();
		
		is.close();	
	}
	
	private void copyStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		int ix=0;
		while ( (ix = in.read(buffer)) > 0) 
		{
			out.write(buffer, 0, ix);
		}
	}
	

	@Override
	public void qrCodeDetected(String text)
	{
		if (lastValue.equals(text)) return;
		lastValue = text;
		
		statusLabel.setText("Square code detected: "+text);
		
		//when a code is detected, we call a javascript function called squareCode.
		String url = "javascript:squareCode(\""+text+"\")";
		try
		{
			getAppletContext().showDocument(new URL(url));
		}
		catch (MalformedURLException e) { e.printStackTrace(); }
	}
	
	@Override
	public void destroy()
	{
		System.out.println("WebcamApplet shutting down..");
		try
		{
			OpenCVWebCam cam = (OpenCVWebCam)this.cam;
			if (cam != null)
			{
				cam.stop();
				while (cam.isRunnning())
				{
					try { Thread.sleep(100); } catch (Exception ee) {}
				}
			}
			System.out.println("WebCam stopped!");
		}
		catch (Exception e) { e.printStackTrace(); }
		
		super.destroy();
	}
}
