/*
 * Created on Dec 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package Desktop;

/**
 * @author Conny
 *
 * This test case is used to test the following APIs in the org.jdesktop.jdic.deskopt package:
 * static void mail();
 * 		Launches the message compose window of the default mailer.
 * static void mail(Message msg);
 * 		Launches the message compose window of the default mailer, and fills in the message fields with the field values of the given Message object.
 * void setAttachments(java.util.List attachList)
 *      Sets the message "Attachments" field.
 * void setBccAddrs(java.util.List abccList)
 *      Sets the message "Bcc" address list.
 * void setBody(java.lang.String abody)
 *       Sets the message "Body" field.
 * void setCcAddrs(java.util.List accList)
 *       Sets the message "Cc" address list.
 * void setSubject(java.lang.String asubject)
 *       Sets the message "Subject" field.
 * void setToAddrs(java.util.List atoList)
 *        Sets the message "To" address list.
 * java.util.Iterator getAttachments()
 *        Gets an iterator of the message "Attachment" file list.
 * java.util.Iterator getBccAddrs()
 *        Gets an iterator of the message "Bcc" address list.
 * java.util.Iterator getCcAddrs()
 *        Gets an iterator of the message "Cc" address list. 
 * java.util.Iterator getToAddrs()
 *        Gets an iterator of the message "To" address list.
 * java.lang.String getBody()
 *        Gets the "Body" field of the message.
 * java.lang.String getSubject()
 *        Gets the "Subject" field of the message.	
 */

import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.DesktopException;
import org.jdesktop.jdic.desktop.Message;
import java.util.ArrayList;
import java.util.Iterator; 
import java.io.IOException;

public class DesktopTest 
{
	public static void main(String[] args)
	{		
		try
		{
			Message msg = new Message();
			
			ArrayList toList = new ArrayList();
			toList.add("conny.cheng@sun.com");
			ArrayList ccList = new ArrayList();
			ccList.add("conny.cheng@sun.com");
			ArrayList bccList = new ArrayList();
			bccList.add("conny.cheng@sun.com");
			ArrayList attList = new ArrayList();
			attList.add("d:\\jdic\\jdictest\\JDIC\\Desktop\\test-dir\\error.txt");
			attList.add("d:\\jdic\\jdictest\\JDIC\\Desktop\\test-dir\\test.txt");
			
			String sub = new String("This is the test mail by JDIC");			
			String body = new String("This is the message body set by the JDIC API.");
								
			msg.setToAddrs(toList);
			msg.setCcAddrs(ccList);
			msg.setBccAddrs(bccList);
			msg.setSubject(sub);
			msg.setBody(body);
			
			try
			{
				msg.setAttachments(attList);
			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
			
			Desktop.mail(msg);
			Desktop.mail();
			
			Iterator toIterator = msg.getToAddrs();
	
			while (toIterator.hasNext())
			{
				System.out.println((String)toIterator.next());
			}	
			
			Iterator ccIterator = msg.getCcAddrs();
			
			while (ccIterator.hasNext())
			{
				System.out.println((String)ccIterator.next());
			}
			
			Iterator bccIterator = msg.getBccAddrs();
			
			while (bccIterator.hasNext())
	        {
				System.out.println((String)bccIterator.next());
		    }	
			
			Iterator attIterator = msg.getAttachments();
			
			while (attIterator.hasNext())
			{
				System.out.println((String)attIterator.next());
			}	

			String getSubject = new String();
			getSubject = msg.getSubject();
			String getBody = new String();
			getBody = msg.getBody();

			System.out.println(getSubject);
			System.out.println(getBody);
			
			System.out.println("Expected result: Two mail instances should popup, on with information set by code, the other one is an empty mail instance.");
		} catch (DesktopException de)
		{
			de.printStackTrace();
		}
	}

}
