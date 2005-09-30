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

import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;

import org.jdesktop.jdic.desktop.internal.BrowserService;
import org.jdesktop.jdic.desktop.internal.LaunchFailedException;

/**
 * Concrete implementation of the BrowserService interface for Gnome.
 */
public class GnomeBrowserService implements BrowserService {
    static {
        Toolkit.getDefaultToolkit();
        System.loadLibrary("jdic");
    }

    // Browser name pattern for MOZILLA, which are used the check the currently used browser.
    private static final String MOZILLA_NAME_PATTERN = "mozilla";

    /**
     * Reserved target names, which are used by Mozilla itself. 
     */
    private static final String[] RESERVED_TARGET_NAMES = {"_blank", "new-window", "new-tab"};
    
    /**
     * Converts the given target name if it happens to be one of the reserved target names.
     * <p>  
     * First reverse the name, and then triple it with character '?' as the seperator. 
     * <p>
     * Notice: Though this conversion rule tries to make the converted string couldn't 
     * be used by the use in later calls, it couldn't be completely avoided. 
     */
    private String convertTargetName(String target) {
        boolean isTargetNameReserved = false;
        for (int index = 0; index < RESERVED_TARGET_NAMES.length; index++) {
            if (target.equals(RESERVED_TARGET_NAMES[index])) {
                isTargetNameReserved = true;
                break;
            }
        }

        if (!isTargetNameReserved) {
            return target;
        } else {
            if (target.equals("_blank")) {
                return "new-window";
            } else {
                // Target name is "new-window" or "new-tab".
                // First, reverse the target name.
                char reversedChars[] = new char[target.length()];
                for (int i = 0; i < target.length(); i++)
                    reversedChars[i] = target.charAt(target.length() - i - 1);

                String reversedTarget = new String(reversedChars);

                // Then, triple the reversed name with '?' as seprators.
                String convertedTarget = reversedTarget + "?"
                                         + reversedTarget + "?"
                                         + reversedTarget;
                return convertedTarget;
            }                                         
        }
    }

    /**
     * Invokes the system default browser to display the given URL.
     *
     * @param url the given URL. 
     * @throws LaunchFailedException if the default browser is not found, or the default browser 
     *         fails to be launched.  
     */
    public void show(URL url) throws LaunchFailedException {
        if (!nativeBrowseURL(url.toString())) {
            throw new LaunchFailedException("Failed to launch the default browser.");
        }
    }

    /**
     * Invokes the system default browser to display the given URL in the target window.
     * 
     * @param url the given URL.
     * @param target the given browser target window name. 
     * @throws LaunchFailedException if the default browser is not found, or the default browser 
     *         fails to be launched.  
     */
    public void show(URL url, String target) throws LaunchFailedException {
        // !!! Notice: for now, on Unix, only mozilla version 1.5 or later support the 
        // targeting URL feature. Here we use mozilla as the default browser. Actually,
        // this may need to be retrieved from gconf registry.
        
        String mozillaPath = MOZILLA_NAME_PATTERN;
        
        boolean result = browseURLInMozilla(mozillaPath, url, target);
        if (!result) {
            throw new LaunchFailedException("Failed to launch mozilla.");
        }
    }
    
    /**    
     * Invokes Mozilla to display the given URL in the given target window.
     * 
     * @param browserPath the absolute path of the browser exectuable.
     * @param url the given url to be browsed.
     * @param target the given target browser window to show the given url
     */
    public boolean browseURLInMozilla(String browserPath, URL url, String target) {
        String MOZILLA_REQUIRED_VERSION_NUMBER = "1.4";
        String urlStr = url.toString();
        try {
            String verNum = GnomeUtility.getMozillaVersionNumber(browserPath);           
            if (verNum == null || !(verNum.compareToIgnoreCase(MOZILLA_REQUIRED_VERSION_NUMBER) > 0)) {
                // The targeting frame feature is not supported by this version of Mozilla.
                return false;
            } else {
                // Check whether there is running Mozilla instance.
                if (!GnomeUtility.isMozillaRunning(browserPath)) {
                      return false;
                } 

                // There is already running Mozilla instance. 
                // Start mozilla using: mozilla -remote openurl(url, target).
                String convertedTargetName = convertTargetName(target);
                Runtime.getRuntime().exec(new String[] {
                    browserPath, "-remote", "openurl(" + urlStr + "," + convertedTargetName + ")" });
                
                return true;    
            }
        } catch (IOException e) {
            return false;
        }
    }
    
    private native boolean nativeBrowseURL(String urlStr);
}
