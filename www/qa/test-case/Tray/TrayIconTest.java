//package trayicon;

/*
 * Created on Dec 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
//package trayicon;

/**
 * @author Conny
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.awt.Point;
import java.awt.Dimension; 
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.ItemListener;
import javax.swing.*;
import java.net.*;

import org.jdesktop.jdic.tray.TrayIcon;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;

public class TrayIconTest implements ActionListener
{
	SystemTray tray = SystemTray.getDefaultSystemTray();
	
	ImageIcon icon = new ImageIcon("./images/man1.gif");
	TrayIcon ti = new TrayIcon(icon);
    	TrayIcon tmp;
	//TrayIcon ti;
	
	private static final String URL = "http://java.sun.com";
	
	public TrayIconTest()
	{
		JPopupMenu menu;
		JMenuItem menuItem;
		
		menu = new JPopupMenu("A Menu");
		
		menuItem = new JMenuItem("Set Icon");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Set Icon NULL");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Set Icon 2");
		menuItem.addActionListener(this);
		menu.add(menuItem);               

        	menuItem = new JMenuItem("Get Location");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Go to...");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Open Mail...");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Quit");
		menuItem.addActionListener(this);
	    	menu.add(menuItem);

        	menuItem = new JMenuItem("Remove (É¾³ý)");
		menuItem.addActionListener(this);
	        menu.add(menuItem);
                
        	menuItem = new JMenuItem("Add (Ìí¼Ó)");
		menuItem.addActionListener(this);
		menu.add(menuItem);
				
	        //ti = new TrayIcon(icon, "Hello", menu);
		ti.setIconAutoSize(true);
		ti.setCaption("Hello!");
		ti.setPopupMenu(menu);
		ti.setToolTip("This case is to test the several TrayIcon methods.");
		ti.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						JOptionPane.showMessageDialog(null, "This test case test several TrayIcon APIs.",
								                      "Info", JOptionPane.INFORMATION_MESSAGE);
					}
				});
		
		tray.addTrayIcon(ti);
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		JMenuItem source = (JMenuItem)(e.getSource());
		String s = source.getText();
		if ( s.equalsIgnoreCase("Quit"))
		{
			System.out.println("Quit item is selected!");
			System.exit(0);
		}
	        if ( s.equalsIgnoreCase("Remove (É¾³ý)"))
		{
			System.out.println("Remove item is selected!");
			tray.removeTrayIcon(tmp);
		}

                if ( s.equalsIgnoreCase("Add (Ìí¼Ó)"))
		{
			System.out.println("Add item is selected!");
                        tmp = new TrayIcon(new ImageIcon("./images/duke.gif"),"Test",null);
			tmp.setIconAutoSize(true);
			tray.addTrayIcon(tmp);
		}

		if (s.equalsIgnoreCase("Set Icon"))
		{
			System.out.println("Set Icon is selected!");
			ImageIcon icon = new ImageIcon("./images/duke.gif");
			ti.setIconAutoSize(true);
			ti.setIcon(icon);
		}
		if (s.equalsIgnoreCase("Set Icon NULL"))
		{
			System.out.println("Set Icon NULL is selected!");
			ti.setIconAutoSize(true);
			ti.setIcon(null);
		}
		if (s.equalsIgnoreCase("Set Icon 2"))
		{
			System.out.println("Set Icon 2 is selected!");
			ImageIcon icon = new ImageIcon("./images/man1.gif");
			ti.setIconAutoSize(true);
			ti.setIcon(icon);
		}

	
		if (s.equalsIgnoreCase("Get Location"))
		{
			System.out.println("Get Location is selected");
			Point p;
			
			if (ti.getLocationOnScreen() instanceof Point)
			{
				p = new Point(ti.getLocationOnScreen());
				System.out.println(p.toString());
			}
			else
			{				
			    System.out.println("Can not get the location of the image!");
			}
					
		}
		
		if (s.equalsIgnoreCase("Go to..."))
		{
			System.out.println("Now go to the website...");
			try
			{
				Desktop.browse(new java.net.URL(URL));			
			} catch (DesktopException de)
			{
				System.err.println(de.getMessage());
			} catch (MalformedURLException mue)
			{
				System.out.println("Error URL");
			}
		}
		
		if (s.equalsIgnoreCase( "Open Mail..."))
		{
			System.out.println("A mail instance is starting...");
			try
			{
				Desktop.mail();
			} catch (DesktopException de)
			{
				de.printStackTrace();
			}
		}
	}
	
	private void TopFrame()
	{
		Point fLocation = new Point(200, 200);
		
		JFrame.setDefaultLookAndFeelDecorated(true);
		JFrame f = new JFrame("This frame is always on top");
		f.setAlwaysOnTop(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocation(fLocation);
		
		JPanel pane = new JPanel();
		pane.add(new JLabel("Hello!"));
		
		f.add(pane);		
		
		//f.setSize(new Dimension(500, 400));   
		f.setSize(500,400);
		f.pack();
		f.setVisible(true);
		
	}
	public static void main(String[] args)
	{
		TrayIconTest tiTest = new TrayIconTest();
		
		tiTest.TopFrame();		
	}		

}
