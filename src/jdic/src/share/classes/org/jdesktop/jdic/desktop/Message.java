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
 
package org.jdesktop.jdic.desktop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class represents a message structure. 
 * 
 * <p>
 * It consists of several necessary message fields.
 * 
 * @see Desktop#mail(Message)
 */
public class Message {
  
    /**
     * Name of the message "To" list field.
     */
    private List toAddrs; 
  
    /**
     * Name of the "Cc" list field.
     */
    private List ccAddrs;
  
    /**
     * Name of the "Bcc" list field.
     */
    private List bccAddrs;
  
    /**
     * Name of the message "Subject" field.
     */
    private String subject;
  
    /**
     * Name of the message "Body" field.
     */
    private String body;
  
    /**
     * Name of the message attachment list field.
     */
    private List attachments;

    /**
     * Constructor of a <code>Message</code> object.
     */
    public Message() {
        toAddrs= new ArrayList();
        ccAddrs = new ArrayList();
        bccAddrs = new ArrayList();
        attachments = new ArrayList();
    }

    /**
     * Gets an iterator of the message "To" address list.
     *
     * @return an <code>Iterator</code> object of the message "To" address list.
     */
    public Iterator getToAddrs() {
        return toAddrs.iterator();  	
    }

    /**
     * Sets the message "To" address list. 
     *
     * @param atoList an email address list for the "To" field.
     */
    public void setToAddrs(List atoList) {
        toAddrs.clear();

        if(atoList == null)
            return;
        Iterator iter = atoList.iterator();
        if(iter == null)
            return;
        while(iter.hasNext())
            toAddrs.add(iter.next());	
    }

    /**
     * Gets an iterator of the message "Cc" address list.
     *
     * @return an <code>Iterator</code> object of the message "Cc" address list.
     */
    public Iterator getCcAddrs() {
        return ccAddrs.iterator();
    }
  
    /**
     * Sets the message "Cc" address list.
     * 
     * @param accList an email address list for the "Cc" field.
     */
    public void setCcAddrs(List accList) {
        ccAddrs.clear();

        if(accList == null)
            return;
        Iterator iter = accList.iterator();
        if(iter == null)
            return;
        while(iter.hasNext())
            ccAddrs.add(iter.next());
    }

    /**
     * Gets an iterator of the message "Bcc" address list.
     *
     * @return an <code>Iterator</code> object of the message "Bcc" address list.
     */
    public Iterator getBccAddrs() {
        return bccAddrs.iterator();
    }
  
    /**
     * Sets the message "Bcc" address list.
     * 
     * @param abccList an email address list for the "Bcc" field.
     */
    public void setBccAddrs(List abccList) {
        bccAddrs.clear();

        if(abccList == null)
            return;
        Iterator iter = abccList.iterator();
        if(iter == null)
            return;
        while(iter.hasNext())
            bccAddrs.add(iter.next());
    }


    /**
     * Gets the "Subject" field of the message.
     *
     * @return the value of the "Subject" field.
     */
    public String getSubject() {
        return subject;
    }
  
    /**
     * Sets the message "Subject" field.
     * 
     * @param asubject a string for the "Subject" field.
     * 
     */
    public void setSubject(String asubject) {
        subject = asubject;
    }
  
    /**
     * Gets the "Body" field of the message.
     *
     * @return the value of the "Body" field.
     */
    public String getBody() {
        return body;
    }
  
    /**
     * Sets the message "Body" field.
     * 
     * @param abody a string for the "Body" field.
     */
    public void setBody(String abody) {
        body = abody;  	
    }
  
    /**
     * Gets an iterator of the message "Attachment" file list.
     *
     * @return an <code>Iterator</code> object of the message "Attachment" file 
     *         list.
     */
    public Iterator getAttachments() {
        return attachments.iterator();
    }
  
    /**
     * Sets the message "Attachments" field. 
     *
     * @param attachList the given attachment list, whose elements are the 
     *        abosolute paths of files to be attached.
     * @throws IOException if any of the attached files is not readable.
     */
    public void setAttachments(List attachList) throws IOException {
        boolean hasUnreadable = false;
        boolean hasDuplicated  = false;
        List unattachedlist = new ArrayList();
        String unattach_filenames = "";
        attachments.clear();

        if(attachList == null)
            return;
        Iterator iter = attachList.iterator();
        if(iter == null)
            return;
        while(iter.hasNext()) {
            String filename = (String)iter.next();
            Iterator inner_iter = attachments.iterator();
            if(inner_iter != null) {
                while(inner_iter.hasNext()) {
                    if(hasDuplicated 
                            = filename.equals((String)(inner_iter.next())))
                        break;
                }
            }
            if(hasDuplicated) 
                continue;
            File attfile = new File(filename);
            
            // add the file to attachments if it is a readable file.
            if (attfile.canRead() && !attfile.isHidden()) {
                if (attfile.isFile())
                    attachments.add(attfile.getAbsolutePath());
            } 
            
            // continue to add other files in the list if the current one 
            // unreadbale.
            else { 
                hasUnreadable = true;
                unattachedlist.add(attfile.getAbsolutePath());
                continue;
            }
        }

        // throw IOException if there are any unreadable files.
        if(hasUnreadable) {
            Iterator unattach_iter = unattachedlist.iterator();
            if (unattach_iter != null) {
                while(unattach_iter.hasNext()) {
                     unattach_filenames += (String)unattach_iter.next() + "\n";
                }
                throw new IOException("Following files cannot be added to " +
                        "the attachments:\n" + unattach_filenames);
            }
        }
    }
}
