import org.jdesktop.jdic.tray.TrayIcon;
import org.jdesktop.jdic.tray.SystemTray;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class IconFrame
{
	public JFrame testFrame;
	public TrayIcon ti;
	
	public IconFrame()
	{
		try 
		{
			initTrayIcon();
		} catch (UnsatisfiedLinkError ule)
		{
			ule.printStackTrace();
		}
		
		testFrame=new JFrame("TEST WINDOW!!!!!");
        	testFrame.setSize(300,300);
        	testFrame.setDefaultCloseOperation(WindowConstants.
		DO_NOTHING_ON_CLOSE);
        	testFrame.addWindowListener(new java.awt.event.WindowAdapter() 
        	{
        		public void windowClosing(java.awt.event.WindowEvent evt) {
                	exitForm(evt);
            		}
        	});
        
            testFrame.setVisible(true);		
	}
	
	private void initTrayIcon() throws UnsatisfiedLinkError
    	{
    		ImageIcon i = new ImageIcon("./images/duke.gif");
   	 	    
    		SystemTray tray = SystemTray.getDefaultSystemTray(); 
    		    
     		JPopupMenu menu=new JPopupMenu ();
       		JMenuItem menuItem = new JMenuItem("Show Main Window");
       		menuItem.addActionListener(new ActionListener()
       		{
       			public void actionPerformed(ActionEvent e) 
           		{
           			openCloseWindow();
            		}
       		});
       
        	menu.add(menuItem);
        	/*menuItem = new JMenuItem("About test...");
        	menuItem.addActionListener(new ActionListener()
        	{
        	
        	public void actionPerformed(ActionEvent e) 
        	{
                	//!!!!!
            	}
       		});
       
	        menu.add(menuItem);*/
	
	        menu.addSeparator();
	        
	        menuItem = new JMenuItem("Exit");
	        menuItem.addActionListener(new ActionListener()
	        {
	        	public void actionPerformed(ActionEvent e) 
	        	{
	        		removeTrayIcon();
	                	System.exit(0);
	       
	            }
	       });
	       
	        menu.add(menuItem);	        
	        
	        ti = new TrayIcon(i, "Hello", menu);
		
	        ti.setIconAutoSize(false);
	        
	        ti.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent e) 
	            {
	            	    openCloseWindow();
	            }
	        });    
	    
	}   
	
	private void openCloseWindow()
	{
		if (ti!=null)
		{			
			SystemTray tray = SystemTray.getDefaultSystemTray(); 
			if (testFrame.isVisible())
			{
				testFrame.setVisible(false);
			        tray.addTrayIcon(ti);
			} else
			{
				testFrame.setState(JFrame.NORMAL);
			        testFrame.setVisible(true);
			        testFrame.requestFocus();
			        testFrame.toFront();
			        tray.removeTrayIcon(ti);
			}
	    	}
	} 
	
	private void removeTrayIcon()
	{
		if (ti!=null)
		{
			SystemTray tray = SystemTray.getDefaultSystemTray(); 
	     		tray.removeTrayIcon(ti);
	    	}
	}
	
	private void exitForm(java.awt.event.WindowEvent evt) 
	{
		if (testFrame.getState()==JFrame.ICONIFIED)
		{
			removeTrayIcon();
         		System.exit(0);
     		} else
     		{
		         testFrame.setState(JFrame.ICONIFIED);
		         openCloseWindow();
     		}
	}
	
	public static void main(String[] args) 
    	{
        // TODO code application logic here
        	new IconFrame();
   	}
		
}