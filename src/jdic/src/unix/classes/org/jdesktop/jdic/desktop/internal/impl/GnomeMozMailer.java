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

import java.io.IOException;
import java.util.Iterator;

import org.jdesktop.jdic.desktop.Message;
import org.jdesktop.jdic.desktop.internal.LaunchFailedException;
import org.jdesktop.jdic.desktop.internal.MailerService;

/**
 * Represents the Mozilla implementation of MailerService interface for Gnome.
 * 
 * @see     MailerInterface
 * @see     GnomeEvoMailer
 */
public class GnomeMozMailer implements MailerService {
    /* Location of mozilla */
    private String mozLocation;

    /**
     * Constructor, set default mozLocation as mozilla could be found in PATH
     */
    public GnomeMozMailer() {
        mozLocation = "mozilla";
    }

    /**
     * Constructor, initialize with a given path of mozilla location
     */
    public GnomeMozMailer(String location) {
        mozLocation = location;
    }

    /**
     * Launches Mozilla message compose window with information contained in msg prefilled.
     *
     * @throws LaunchFailedException if the process fails.
     */
    public void open(Message msg) throws LaunchFailedException {
        boolean HasInstance = false;
        String[] cmdArray = new String[3];
        String tmp; 

        /* construct the commandline to launch mozilla message compose window */
        cmdArray[0] = mozLocation;
        tmp = constructArgs(msg.getToAddrs(), msg.getCcAddrs(), msg.getBccAddrs(), msg.getSubject(), msg.getBody(), msg.getAttachments());

        /* detect if there is already a running instance of mozilla */
        try {
            HasInstance = GnomeUtility.isMozillaRunning(mozLocation);
        } catch (IOException e) {
            throw new LaunchFailedException("mozilla -remote commandline failed:" + e.getMessage());
        }

        if (!HasInstance) { /* case that there is no running instance of Mozilla */
            cmdArray[1] = "-compose";
            cmdArray[2] = tmp;

            try {
                Runtime.getRuntime().exec(cmdArray);
                Thread.sleep(500);
            } catch (IOException e) {
                throw new LaunchFailedException("Cannot launch Mozilla composer via -compose commandline:" + e.getMessage());
            } catch (InterruptedException iE) { }
        } else {               
            /* case that there is already a running instance of Mozilla, use -remote */
            cmdArray[1] = "-remote";
            cmdArray[2] = "xfeDoCommand(composeMessage," + tmp + ")";
  
            try {
                Runtime.getRuntime().exec(cmdArray);
            } catch (IOException e) {
                throw new LaunchFailedException("Cannot launch Mozilla composer via xremote commandline:" + e.getMessage());
            }
        }
    }        
                               
    /**
     * Launches a "blank" mozilla compose window.
     *
     * @throws LaunchFailedException if the process fails.
     */
    public void open() throws LaunchFailedException {
        boolean HasInstance = false;

        /* detect if there is already a running instance of mozilla */
        try {
            HasInstance = GnomeUtility.isMozillaRunning(mozLocation);
        } catch (IOException e) {
            throw new LaunchFailedException("mozilla -remote commandline failed:" + e.getMessage());
        }

        if (!HasInstance) { /* case that there is no running instance of Mozilla */
            String[] cmdArray = { mozLocation, "-compose" };	
            try {
                Runtime.getRuntime().exec(cmdArray);
                Thread.sleep(500);
            } catch (IOException e) {
                throw new LaunchFailedException("Cannot launch Mozilla composer via -compose commandline:" + e.getMessage());
            } catch (InterruptedException iE) { }
        }
        else {           /* case that there is already a running instance of Mozilla, use -remote */
            String[] cmdArray = { mozLocation, "-remote", "xfeDoCommand(composeMessage)" };
            try {
                Runtime.getRuntime().exec(cmdArray);
            } catch (IOException e) {
                throw new LaunchFailedException("Cannot launch Mozilla composer via -compose commandline:" + e.getMessage());
            }
        }
    }
    
    /**
     * Constructs commandline Arguments according to the various fields' values.
     *
     * @return argument string.
     */
    private String constructArgs(Iterator to, Iterator cc, Iterator bcc, String subject, String body, Iterator attach) {
        String argString = new String("");
        String tmp = new String();
      
        if(to != null) {
            while(to.hasNext()) {
                tmp = tmp + ((String)to.next()) + ",";
            }
            argString = "to='" + tmp + "',";
        }

        if(cc != null) {
            tmp = "";
            while(cc.hasNext()) {
                tmp = tmp + ((String)cc.next()) + ",";
            }
            argString = argString + "cc='" + tmp + "',";
        }

        if(bcc != null) {
            tmp = "";
            while(bcc.hasNext()) {
                tmp = tmp + ((String)bcc.next()) + ",";
            }
            argString = argString + "bcc='" + tmp + "',";
        }

        if(subject != null)
            argString = argString + "subject=" + parseSubject(URLUTF8Encoder.encode(subject)) + ",";
        if(body != null)
            argString = argString + "body=<body>" + parseBody(URLUTF8Encoder.encode(body)) + "</body>,";

        if(attach != null) {
            tmp = "";
            while (attach.hasNext()) {
                tmp = tmp + "file://" + (String)attach.next() + ",";
            }
            argString = argString + "attachment='" + tmp + "'";
        }
        
        return argString; 
    }

    /**
     * Handles special characters in subject string.
     *
     * @return subject string.
     */
    private String parseSubject(String inString) {
        String tmp = inString;

        //convert line-feed characters contained in a subject to white space
        tmp = tmp.replaceAll("%0a", "%20");
   
        return tmp;	    
    }

    /**
     * Handles special characters in body string.
     *
     * @return body string.
     */
    private String parseBody(String inString) {
        String tmp = inString;

        //encoding '<''>" pairs so that body wouldn't interpret them as HTML tag
        tmp = tmp.replaceAll("%3c", "&#60");
        tmp = tmp.replaceAll("%3e", "&#62");

        //convert line-feed characters to HTML tag <br>
        tmp = tmp.replaceAll("%0a", "<br>");
       
        return tmp;	    
    }
}

