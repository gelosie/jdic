/*
 * Created on Jan 13, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//package Browser;

import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.browser.WebBrowserUtil;
import java.net.URL;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.TextField;
import java.awt.BorderLayout;
import java.awt.GridLayout; 
import java.net.MalformedURLException; 
import java.awt.event.ActionListener; 
import java.awt.event.ActionEvent; 

/**
 * @author Conny
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

// This test case is to start two browser instances by using different constructors within one frame.

public class WebBrowserTest implements ActionListener 
{
	WebBrowser wb1;
	MyBrowser wb2;
	TextField tfURL2;
	JButton b2;
	
	public WebBrowserTest()
	{
		JFrame f = new JFrame("This frame contains two web browsers.");
		JPanel p1 = new JPanel();
		JPanel p2 = new JPanel();
		JPanel p3 = new JPanel();
		JPanel p23 = new JPanel();
		tfURL2 = new TextField("http://", 60);
		b2 = new JButton("Go");
		b2.addActionListener(this);
		tfURL2.addActionListener(this);
	
		try
		{
			wb1 = new WebBrowser(new URL("http://java.sun.com"));
			wb1.setVisible(true);
			wb1.setSize(600, 1000);
			
			System.out.println(WebBrowserUtil.getBrowserPath());
			System.out.println(WebBrowserUtil.isDefaultBrowserMozilla());
			
			wb2 = new MyBrowser();
			wb2.setVisible(true);
			wb2.setSize(590, 1000);
					
			p1.add(wb1);
			
			p2.setSize(600, 100);
			p2.add(tfURL2);
			p2.add(b2);
			
			p3.add(wb2);
			
			p23.setLayout(new GridLayout(16,1));
			p23.add(p2);
			p23.add(p3);
						
			f.setLayout(new BorderLayout());
			f.getContentPane().add(p1, BorderLayout.WEST);
			f.getContentPane().add(p23, BorderLayout.EAST);
			f.setSize(1200,1000);
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.setVisible(true);
			
		}catch (MalformedURLException mue)
		{
			mue.printStackTrace();
		}
	}
	
	public void actionPerformed(ActionEvent ae)
	{
		String sURL = tfURL2.getText();
		if (ae.getSource().equals(b2))
		{
			System.out.println("Go To...");
			try
			{
				wb2.willOpenURL(sURL);
				wb2.setURL(new URL(sURL));
			} catch (MalformedURLException mue)
			{
				mue.printStackTrace();
			}
		}		
		
		if (ae.getSource().equals(tfURL2))
		{
			System.out.println("Go To...");
			try
			{
				wb2.willOpenURL(sURL);
				wb2.setURL(new URL(sURL));
			} catch (MalformedURLException mue)
			{
				mue.printStackTrace();
			}
		}
	}
	
	public static void main(String args[])
	{
		WebBrowserTest wet = new WebBrowserTest();
	} 
	
	class MyBrowser extends WebBrowser
	{
		protected void willOpenURL(String s)
		{
			System.out.println(s);
		}
	}
}
