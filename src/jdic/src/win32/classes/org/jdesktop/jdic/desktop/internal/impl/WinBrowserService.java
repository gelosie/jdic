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

import java.net.URL;

import org.jdesktop.jdic.desktop.internal.BrowserService;
import org.jdesktop.jdic.desktop.internal.LaunchFailedException;

/**
 * Concrete implementation of the interface <code>BrowserService</code> for Windows.
 *
 * @see     BrowserService
 * @see     GnomeBrowserService
 */
public class WinBrowserService implements BrowserService {
    /**
     * Browser name pattern for IE, which are used the check the currently used browser.
     */
    private static final String IE_NAME_PATTERN = "iexplore";

    /**
     * Reserved target names by native function IWebBrowser2::Navigate2().
     */
    private static final String[] RESERVED_TARGET_NAMES = {"_top", "_self", "_parent"};

    /**
     * Converts the given target name if it happens to be one of the reserved target names.
     * <p>  
     * First reverse the name, and then triple it with character '?' as the seperator. 
     * <p>
     * Notice: Though this conversion rule tries to make the converted string couldn't 
     * be used by the use in later calls, it couldn't be completely avoided.
     * 
     * @param target the given target name to be converted.
     * @return the converted target name according the rules. 
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

    /**
     * Invokes the system default browser to display the given URL.
     * 
     * @param url the given URL.
     * @throws LaunchFailedException if the default browser is not found, or the default browser 
     *         fails to be launched.  
     */
    public void show(URL url) throws LaunchFailedException {
		boolean findOpenNew = false;
		//First check if we could find command for verb opennew
		String verbCmd = WinUtility.getVerbCommand(url, DesktopConstants.VERB_OPENNEW);
		if (verbCmd != null) {
			findOpenNew = true;
		} else {
			//If no opennew verb command find, then check open verb command
			verbCmd = WinUtility.getVerbCommand(url, DesktopConstants.VERB_OPEN);
		}
		if (verbCmd != null) {
			boolean result;
			if (findOpenNew) {
				//If there is opennew verb command, use this one
				result = WinAPIWrapper.WinShellExecute(url.toString(), DesktopConstants.VERB_OPENNEW);
			} else {
				//else use open verb command
				result = WinAPIWrapper.WinShellExecute(url.toString(), DesktopConstants.VERB_OPEN);
			}
			if (!result) {
				throw new LaunchFailedException("Failed to launch the default browser");
			}
		} else {
			throw new LaunchFailedException("No default browser associated with this URL");
		}
		
    }

    /**
     * Invokes the system default browser to display the given URL in the target window.
     * 
     * @param url the given URL.
     * @param target the given target browser window name. 
     * @throws LaunchFailedException if the default browser is not found, or the default browser 
     *         fails to be launched.  
     */
    public void show(URL url, String target) throws LaunchFailedException {
        // Check whether the default browser is IE.
        String verbCommand = WinUtility.getVerbCommand(url, DesktopConstants.VERB_OPEN);
        if (verbCommand.toLowerCase().indexOf(IE_NAME_PATTERN) == -1) {
            throw new LaunchFailedException("The default browser doesn't support targeting URL feature.");
        } else {
            // Invoke IE to display the given URL in the given target window.
            boolean result = WinAPIWrapper.WinBrowseURLInIE(url.toString(), convertTargetName(target));
            
            if (!result) {
                throw new LaunchFailedException("Failed to invoke the default browser.");
            }
        }
    }
}
