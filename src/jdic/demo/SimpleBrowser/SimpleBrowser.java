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

import java.awt.*;
import javax.swing.*;
import java.net.URL;
import java.net.MalformedURLException;
import org.jdesktop.jdic.browser.*;

/**
 * JDIC API demo main class.
 * <p>
 * SimpleBrowser demonstrate the usage of JDIC API package org.jdesktop.jdic.browser
 * (Browser component).
 */

public class SimpleBrowser {
    public static void main(String[] args) {
        JFrame frame = new JFrame("JDIC API Demo - SimpleBrowser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        WebBrowser webBrowser = new WebBrowser();
        
        //Use below code to check the status of the navigation process,
        //or register a listener for the notification events.
        //WebBrowser.Status myStatus = webBrowser.getStatus();
        //webBrowser.addWebBrowserListener(
        //    new WebBrowserListener() {
        //    public void downloadStarted(WebBrowserEvent event) {;}
        //    public void downloadCompleted(WebBrowserEvent event) {;}
        //    public void downloadProgress(WebBrowserEvent event) {;}
        //    public void downloadError(WebBrowserEvent event) {;}
        //    public void documentCompleted(WebBrowserEvent event) {;}
        //    public void titleChange(WebBrowserEvent event) {;}  
        //    public void statusTextChange(WebBrowserEvent event) {;}        
        //});

        try {
            webBrowser.setURL(new URL("http://java.net"));
            // Print out debug messages in the command line.
            //webBrowser.setDebug(true);
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
            return;
        }               
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(700, 500));
        panel.add(webBrowser, BorderLayout.CENTER);
        
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);        
    }
}