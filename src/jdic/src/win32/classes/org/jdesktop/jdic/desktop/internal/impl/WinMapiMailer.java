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

package org.jdesktop.jdic.desktop.internal.impl;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.jdesktop.jdic.desktop.Message;
import org.jdesktop.jdic.desktop.internal.LaunchFailedException;
import org.jdesktop.jdic.desktop.internal.MailerService;


/**
 * Represents the Mapi implementation of MailerService interface for Windows.
 * 
 * @see     MailerService
 * @see     WinMozMailer
 */
public class WinMapiMailer implements MailerService {
    /**
     * Opens the default system mailer
     * 
     * @throws LaunchFailedException if the native system mailer is not found, 
     *         or fails to be launched.
     */
    public void open() throws LaunchFailedException {
        WinAPIWrapper.WinOpenMapiMailer(null, null, null, null, null, null);
    }
  
    /**
     * Opens the default system mailer with the information in msg filled in.
     * 
     * @param msg the given Message object.
     * @throws LaunchFailedException if the system mailer is not found, or fails
     *         to be launched.
     */
    public void open(Message msg) throws LaunchFailedException {
        String[] toArray = ItrToStringArray(msg.getToAddrs());
        String[] ccArray = ItrToStringArray(msg.getCcAddrs());
        String[] bccArray = ItrToStringArray(msg.getBccAddrs());
        String[] attachArray = ItrToStringArray(msg.getAttachments());

        /* open message compose window for the system mailer by native method */
        WinAPIWrapper.WinOpenMapiMailer(toArray, ccArray, bccArray, msg.getSubject(), msg.getBody(), attachArray);
    }
  
    /**
     * Converts List object to String[] to faciliate handling in native code
     *
     * @param inList input List
     * @return String[] array contains inList's elements
     */
    private String[] ItrToStringArray(Iterator inItr) {
        String[] stringArray = null;
        int count = 0;	/* count for array conversion */
        List tmpList = null;
        Iterator tmpItr = null;

        if(inItr != null) {
        	tmpList = new ArrayList();
            while(inItr.hasNext())
                tmpList.add(inItr.next());
        }
        if(tmpList != null) {
            tmpItr = tmpList.iterator();
            if(tmpItr != null) {
                stringArray = new String[tmpList.size()];
                while(tmpItr.hasNext()) {
                    stringArray[count] = (String) tmpItr.next();
                    count++;
                }
            }
        }

        return stringArray;
    }
}
