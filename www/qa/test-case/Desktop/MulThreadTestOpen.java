package Desktop;

import java.io.File;
import java.lang.Thread;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;

public class MulThreadTestOpen implements Runnable 
{
	public void run()
    {
		try
		{ 
            System.out.println("open a file with .bat postfix . ");
            Desktop.open(new File("d:\\jdic\\jdictest\\JDIC\\Desktop\\test-dir\\setup.bat"));
        }
            catch (DesktopException e) 
		{
            e.printStackTrace();
        }
            
        try
		{
        	System.out.println("open a file with .lnk postfix . ");
            Desktop.open(new File("d:\\jdic\\jdictest\\JDIC\\Desktop\\test-dir\\StarSuite 6.0.lnk"));
        }catch (DesktopException e) 
		{
        	e.printStackTrace();
        }
   	
        try
		{
        	System.out.println("open a file with .gif postfix. ");  
            Desktop.open(new File("d:\\jdic\\jdictest\\JDIC\\Desktop\\test-dir\\test.gif"));
        }catch (DesktopException e) 
		{
        	e.printStackTrace();
        }  	
   	}
 
 
    public static void main(String[] args) 
    {
        MulThreadTestOpen tm = new MulThreadTestOpen();
        Thread t1 = new Thread(tm);
        Thread t2 = new Thread(tm);
        t1.start();
        try 
		{
        	t2.sleep(1000);
		}catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
		
        t2.start();      
    }
}