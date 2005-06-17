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
import java.net.URL;

import org.jdesktop.jdic.desktop.internal.BrowserService;
import org.jdesktop.jdic.desktop.internal.LaunchFailedException;
import org.jdesktop.jdic.desktop.internal.LaunchService;
import org.jdesktop.jdic.desktop.internal.MailerService;
import org.jdesktop.jdic.desktop.internal.ServiceManager;


/**
 * The <code>Desktop</code> class provides several methods to interact with 
 * the system applications and facilities. It cannot be instantiated.
 * <p>
 * The methods include invoking system programs associated with a particular 
 * file type, invoking the system default browser to show a given URL, invoking 
 * the system default mailer with or without a constructed <code>Message</code> 
 * object, and making use of the printer to print a given file.
 *
 * @see Message
 * @see DesktopException
 */
public class Desktop {
 
    /**
     * Suppresses default constructor for noninstantiability.
     */
    private Desktop() {}
  
    /**
     * Checks if the given file is a valid file.
     * 
     * @throws DesktopException if the given file is not a valid file, or
     *         is not readable.
     */
    private static void checkFileValid(File file) throws DesktopException {
        if (file == null || !file.exists()) {
            throw new DesktopException("The given file doesn't exist.");
        }

        if (!file.canRead()) {
            throw new DesktopException("The given file couldn't be read.");
        }
    }

    /**
     * Launches the associated application to open a file 
     * <p>
     * If the specified file is a directory, the file manager of the current 
     * platform is launched to open it.   
     * @param file the given file.
     * @throws DesktopException If the specified file has no associated 
     * application, or the associated application fails to be launched. 
     */  
    public static void open(File file) throws DesktopException {
        if (file == null) {
            throw new DesktopException("The given file is null.");
        }
        
        // Get the launch service.
        LaunchService launchService = (LaunchService) ServiceManager.
            getService(ServiceManager.LAUNCH_SERVICE);
        
        File resolvedFile = launchService.resolveLinkFile(file);

        // Check if the given file is valid.
        checkFileValid(resolvedFile);

        try {
            launchService.open(resolvedFile);
        } catch (LaunchFailedException e) {
            throw new DesktopException(e.getMessage());  
        }                
    
    }
  
    /**
     * Tests whether the given file could be printed.
     *
     * @param file the given file.
     * @return <code>true</code> if the given file is valid and is printable;
     *         <code>false</code> otherwise.
     */
    public static boolean isPrintable(File file) { 
        if (file == null) {
            return false;
        }

        // Get the launch service.
        LaunchService launchService = (LaunchService) ServiceManager.
            getService(ServiceManager.LAUNCH_SERVICE);
                
        File resolvedFile = launchService.resolveLinkFile(file);
        try {
            checkFileValid(resolvedFile);
        } catch (DesktopException e) {
            return false;
        }
   
        return launchService.isPrintable(resolvedFile);
    }
  
    /**
     * Prints the given file.
     *
     * @param file the given file.
     * @throws DesktopException if the given file is not valid, or the given 
     *         file is not printable, or the given file fails to be printed. 
     */
    public static void print(File file) throws DesktopException {
        if (file == null) {
            throw new DesktopException("The given file is null.");
        }

        // Get the launch service.
        LaunchService launchService = (LaunchService) ServiceManager.
            getService(ServiceManager.LAUNCH_SERVICE);
        
        File resolvedFile = launchService.resolveLinkFile(file);
                
        // Check if valid file.
        checkFileValid(resolvedFile);
    
        // Check if printable.
        if (!isPrintable(resolvedFile)) {
            throw new DesktopException("The given file is not printable."); 
        }

    
        // Print the file.
        try {
            launchService.print(resolvedFile);
        } catch (LaunchFailedException e) {
            throw new DesktopException(e.getMessage());  
        }                
    
    }
  
    /**
     * Tests whether the given file is editable.
     *
     * @param file the given file.
     * @return <code>true</code> if the given file is valid and has an 
     *         associated editor; <code>false</code> otherwise.
     */
    public static boolean isEditable(File file) { 
        if (file == null) {
            return false;
        }

        // Get the launch service.
        LaunchService launchService = (LaunchService) ServiceManager.
            getService(ServiceManager.LAUNCH_SERVICE);
                
        File resolvedFile = launchService.resolveLinkFile(file);
                
        try {
            checkFileValid(resolvedFile);
        } catch (DesktopException e) {
            return false;
        }
   
        return launchService.isEditable(resolvedFile);        
    }    
  
    /**
     * Launches the associated editor to edit the given file.
     *
     * @param file the given file.
     * @throws DesktopException if the given file is not valid, or the given 
     *         file is not editable, or the given editor fails to be launched. 
     */
    public static void edit(File file) throws DesktopException {
        if (file == null) {
            throw new DesktopException("The given file is null.");
        }

        // Get the launch service.
        LaunchService launchService = (LaunchService) ServiceManager.
            getService(ServiceManager.LAUNCH_SERVICE);

        File resolvedFile = launchService.resolveLinkFile(file);
        
        // Check if valid file.
        checkFileValid(resolvedFile);
    
        // Check if editable.
        if (!isEditable(resolvedFile)) {
            throw new DesktopException("The given file is not editable."); 
        }
    
        // Edit the file.
        try {
            launchService.edit(resolvedFile);
        } catch (LaunchFailedException e) {
            throw new DesktopException(e.getMessage());  
        }                
    
    }    
  
    /**
     * Launches the system default browser to show the given URL. 
     *
     * @param url the given URL.
     * @throws DesktopException if the given url is null, or the system default 
     *         browser is not found, or the system browser fails to be launched. 
     */
    public static void browse(URL url) throws DesktopException {
        if (url == null) {
            throw new DesktopException("The given URL is null.");
        }
        
        // Get the browser service.
        BrowserService browserService = (BrowserService) ServiceManager.
            getService(ServiceManager.BROWSER_SERVICE);

        try {
            browserService.show(url);
        } catch (LaunchFailedException  e) {
            throw new DesktopException(e.getMessage());  
        }                
    }

    /**
     * ======================================================================
     * This method is not public for this release, and may be public later.
     * ======================================================================
     * Opens the given URL in the target window of the system default browser. 
     * <p>
     * If the target windows doesn't exist, a new browser window will be 
     * created, and named as the given target name. Otherwise, the given URL 
     * will be displayed in the browser window with the given target name.
     * <p>
     * If the target name is "_blank", the URL will always be displayed in a 
     * new, unnamed browser window.
     *
     * @param url the given URL.
     * @param target the given name of a target browser window. 
     * @throws DesktopException if the given url is null, or the system default 
     *         browser is not found, or it's found but fails to be launched.
     */
    private static void browse(URL url, String target) throws DesktopException {
        if (url == null) {
            throw new DesktopException("The given URL is null.");
        }
        if (target == null) {
            throw new DesktopException("The given target is null.");
        }

        // Get the browser service.
        BrowserService browserService = (BrowserService) ServiceManager.
            getService(ServiceManager.BROWSER_SERVICE);

        // Show the url in the browser window named as target.
        try {
            browserService.show(url, target);
        } catch (LaunchFailedException  e) {
            throw new DesktopException(e.getMessage());  
        }                
    }

  
    /**
     * Launches the message compose window of the default mailer.
     *
     * @throws DesktopException if the system default mailer was not found, 
     *         or it fails to be launched.
     */
    public static void mail() throws DesktopException {
        // Get the mailer service.
        MailerService mailerService = (MailerService) ServiceManager.
            getService(ServiceManager.MAILER_SERVICE);
    
        // Launch the mailer.
        try {
            mailerService.open();
        } catch (LaunchFailedException  e) {
            throw new DesktopException(e.getMessage());  
        }       
    }

    /**
     * Launches the message compose window of the default mailer, and fills 
     * in the message fields with the field values of the given <code>Message
     * </code> object.
     *
     * @param msg the constructed <code>Message</code> object.
     * @throws DesktopException if the given message object is null, or the 
     *         system default mailer was not found, or it fails to be launched.
     */
    public static void mail(Message msg) throws DesktopException {
        if (msg == null) {
            throw new DesktopException("The given message is null.");
        }

        // Get the mailerserservice
        MailerService mailerService = (MailerService) ServiceManager.
            getService(ServiceManager.MAILER_SERVICE);
   
        // Launch the mailer.
        try {
            mailerService.open(msg);
        } catch (LaunchFailedException  e) {
            throw new DesktopException(e.getMessage());  
        }
    } 
}