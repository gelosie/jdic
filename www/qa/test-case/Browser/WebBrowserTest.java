
/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */ 

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


// This test case is to start two browser instances by using different constructors within one frame.

public class WebBrowserTest implements ActionListener 
{
	WebBrowser wb1;
	WebBrowser wb2;
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
			wb1 = new WebBrowser(new URL("http://java.net"));
			wb1.setVisible(true);
			wb1.setSize(600, 1000);
			
			System.out.println(WebBrowserUtil.getBrowserPath());
			
			wb2 = new WebBrowser();
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
}
