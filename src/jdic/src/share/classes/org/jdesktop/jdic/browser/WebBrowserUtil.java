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
package org.jdesktop.jdic.browser;

/**
 * Utility class for <code>WebBrowser</code> to check the default browser 
 * path and type. 
 * 
 * @see WebBrowser
 * 
 * @author  Paul Huang
 * @version 0.1, August 20, 2004
 */
public class WebBrowserUtil {
    private static String browserPath = null;
    private static String osName = System.getProperty("os.name").toLowerCase();

    static {
        System.loadLibrary("jdic");
    }
    
    /* native functions */
    private static native String nativeGetBrowserPath();
    private static native String nativeGetMozillaGreHome();    
    static native void nativeSetEnv();
    
    /**
     *  Gets the native browser path.
     *  @return the path of the default browser in the current system
     */
    public static String getBrowserPath() {
        if (browserPath == null) {
            browserPath = nativeGetBrowserPath();
        }
        return browserPath;
    }
    
    /**
     * Checks if the default browser for the current platform is Mozilla.
     * @return true on Solaris and Linux and true on Windows platform if Mozilla
     * is set as the default browser.
     */
    public static boolean isDefaultBrowserMozilla() {
        if ((osName.indexOf("solaris") >= 0) ||
            (osName.indexOf("linux") >= 0) ) {
            return true;
        } else {
            String nativeBrowserPath = getBrowserPath();
            // Only when Mozilla is set as the default browser, return true. 
            // Or else, fall back to Internet Explorer.
            // FireFox 1.0 is statically linked into Gecko and therefore can not 
            // be embedded. If FireFox is embeddable for some future version,
            // we would have to explicitly check for both Mozilla and FireFox. 
            if (nativeBrowserPath.indexOf("mozilla") >= 0) {
            	return true;
            } else {
                return false;                
            }
        }
    }

    /**
     *  Gets the native Mozilla GRE home directory installed with a .exe package.
     *  @return the GRE home directory of the currently installed Mozilla.
     */
    public static String getMozillaGreHome() {
        return nativeGetMozillaGreHome();
    }    
}
