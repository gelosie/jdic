package Desktop;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;

/**
This test case is used to test the following methods in the org.jdesktop.jdic.deskopt package:
static void browse(java.net.URL url)
          Opens the specified url in the system default browser.
static void edit(java.io.File file)
          Opens the specified file in the system default editor.
static boolean isEditable(java.io.File file)
          Returns true is the specified file is editable.
static boolean isPrintable(java.io.File file)
          Returns true if the file has the print verb
static void mail()
          Launches the message compose window of the default mailer.
static void mail(Message msg)
          launches message compose window of the default mailer with information contained by the constructed message.
static void open(java.io.File file)
          Opens the given file by the default application
static void print(java.io.File file)
          Prints the given file.          
**/

public class Win_desktop
{

	public boolean win_browser()
	{
		try 
		{
			Desktop.browse(new URL("http://java.sun.com"));
	    	return true;	
		} catch(MalformedURLException e) 
		{	   
		    e.printStackTrace();
		    return false;
		} catch (DesktopException e) 
		{
			e.printStackTrace();
	    	return false;
	    }
	}
	
	public boolean win_edit()
	 { 
		//File testFile = new File("\\Desktop\\test-dir\\setup.bat");
		File testFile =new File("d:\\jdic\\jdictest\\JDIC\\Desktop\\test-dir\\test"); //for Windows
	    if(Desktop.isEditable(testFile))
	    {
		    try
			{
			    Desktop.edit(testFile);
			    return true;        	    
		    }catch(DesktopException e)
		    {
		    	e.printStackTrace();
		    	return false;
		    }
	    }
	    
	    System.out.println("No application is associated with the file.");
	    return false; 
	}
	
	public boolean win_print()
	{
	      //File testFile = new File("\\Desktop\\test-dir\\setup.bat");
	      File testFile =new File("d:\\jdic\\jdictest\\JDIC\\Desktop\\test-dir\\test");
	      if(Desktop.isPrintable(testFile))
	      {
		      try
			  {
				    Desktop.print(testFile);        	    
			        return true; 
			  }catch(DesktopException e)
			  {
			      	e.printStackTrace();
			       	return false;
			  }		        
		  }
	      System.out.println("The file is not printable.");
	      return true;	
	}
	
	public static void main(String args[])
	{ 
		 Win_desktop tm = new Win_desktop();
		
		 boolean b1 = tm.win_browser();
		 boolean b2 = tm.win_edit();
		 boolean b3 = tm.win_print();
		
		 if (b1)
		 {
		 		System.out.println("static void browse(java.net.URL url) pass");
		 } else 
		 {
		   		System.out.println("static void browse(java.net.URL url) fail");
		 }
		 if (b2)
		 {
		  		System.out.println("static void edit(java.io.File file)) and static boolean isEditable(java.io.File file) pass");
		 } else 
		 {
		   		System.out.println("static void edit(java.io.File file) and static boolean isEditable(java.io.File file) fail");
		 }
		 if (b3)
		 {
		   		System.out.println("static boolean isPrintable(java.io.File file) and static void print(java.io.File file) pass");
		 } else 
		 {
		  		System.out.println("static boolean isPrintable(java.io.File file) and static void print(java.io.File file) fail");
		 }
	 }
}