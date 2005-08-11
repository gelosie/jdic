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

import java.io.File;
import java.net.URL;


/**
 * WinUtility provides several util funtion for windows platform.
 * 
 * @version 0.9
 */
public class WinUtility {
    /**
     * Suppress default constructor for noninstantiability.
     */
    private WinUtility() {}	

    /**
     * Gets the file extension of the given file.
     * <pre>
     * For examples:
     *     if the given file is: C:\\test\test.txt, the returned file extension is ".txt".
     *     if the given file is: C:\\test\test, the returned file extension is null. 
     * </pre>
     * 
     * @param file the given File object, which contains the absolute path of a file.
     * @return the file extension of the given file.
     */
    private static String getFileExtension(File file) {
        String trimFileStr = file.toString().trim();

        if (trimFileStr == null || trimFileStr == "") {
            return null;
        }
         
        int strIndex = trimFileStr.lastIndexOf(File.separator);
        String filePart = trimFileStr.substring(strIndex + 1, trimFileStr.length());

        strIndex = filePart.lastIndexOf(".");
        if (strIndex == -1 || strIndex == filePart.length() - 1) {
            return null;
        } else {
            String fileExt = filePart.substring(strIndex, filePart.length());

            return fileExt;
        }
    }
    
	/**
	 * Gets the command string associated with the given file and verb.
	 * <p>
	 * For example: suppose the given file is C:\test\test.txt, and the given verb is "print".
	 * The associated command string is retrieved as "%SystemRoot%\system32\NOTEPAD.EXE /p %1"
	 * 
	 * @param file the given file.
	 * @param verb the given verb.
	 * @return the command string in the Registry associated with the given file type 
	 *         and verb.
	 */
	public static String getVerbCommand(File file, String verb) {
		String fileExt = getFileExtension(file);
		if (fileExt == null) {
			return null;
		} else {
			return(WinAPIWrapper.WinAssocQueryString(fileExt, verb)); 
		}
	}

    /**
     * Gets the command string associated with the given URL's protocol and verb in the 
     * Registry.
     * <p>
     * For example: suppose the given URL is http://www.foo.com, and the given verb is "open".
     * The associated command string is retrieved as ""C:\Program Files\Internet Explorer\iexplore.exe" -nohome"
     *
     * @param url the given URL.
     * @param verb the given verb.
     * @return the command string in the Registry associated with the given URL protocol 
     *         and verb.
     */
    public static String getVerbCommand(URL url, String verb) {
        String protocolType = url.getProtocol().trim();
        // For now, this api uses browser to open a file. So replace file protocol 
        // to http protocol.
        if (protocolType.compareToIgnoreCase("file") == 0) {
            protocolType = "http";
        }
        
		if (protocolType == null) {
			return null;
		} else {
			return(WinAPIWrapper.WinAssocQueryString(protocolType, verb)); 
		}
    }

    /**
     * Get the Windows default system mailer.
     * 
     * @return Name of the default system mailer
     */
    static String getDefaultMailer() {
        String defaultMailer =
            WinAPIWrapper.WinRegQueryValueEx(
                WinAPIWrapper.HKEY_LOCAL_MACHINE,
                "SOFTWARE\\Clients\\Mail",
                "");
        return defaultMailer;
    }
    
    /**
     * Get the mozilla mailer location.
     * 
     * @param defMailer Name of the system default mailer
     *        This mailer is either "Mozilla" or "Mozilla Thunderbird"
     * @return Location of the mozilla mailer
     */
    static String getMozMailerLocation(String defMailer) {
   		String mailerPath = WinAPIWrapper.WinRegQueryValueEx(
   				WinAPIWrapper.HKEY_LOCAL_MACHINE,
	            "SOFTWARE\\Clients\\Mail\\"+defMailer+"\\shell\\open\\command",
				"");
   		int index = mailerPath.toLowerCase().lastIndexOf(".exe");
   		return mailerPath.substring(mailerPath.charAt(0) == '"' ? 1 : 0, index+4);
    }
    
    /**
     * Check if the simple MAPI is supported in the current system.
     * 
     * @return true if simple MAPI is supported.
     */
    static boolean isMapiSupported() {
        String regMapi =
            WinAPIWrapper.WinRegQueryValueEx(
                WinAPIWrapper.HKEY_LOCAL_MACHINE,
                "SOFTWARE\\Microsoft\\Windows Messaging Subsystem",
                "MAPI");
        if(regMapi != null) {
            if(regMapi.equals("1")) {
                return true;				
            }
        }
        return false;
    }
}