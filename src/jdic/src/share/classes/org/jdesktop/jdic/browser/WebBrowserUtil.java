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
 * Utility class for <code>WebBrowser</code> class.
 * <p>
 * Note: this class was unintentionally exposed as a public class in release 
 * 0.8.8, which should be made internal. As from release 0.9, all the public 
 * APIs are deprecated and are not recommended for use. This class including 
 * all the public APIs may be removed in future releases.
 * 
 */
public class WebBrowserUtil {    
    /**
     * Gets the native browser path.
     * 
     * @deprecated This method is not recommended for use and may be removed 
     *             in future releases.
     * @return the path of the default browser in the current system
     */
    public static String getBrowserPath() {
        return org.jdesktop.jdic.browser.internal.WebBrowserUtil.
            getBrowserPath();
    }
    
    /**
     * Checks if the default browser for the current platform is Mozilla.
     * 
     * @deprecated This method is not recommended for use and may be removed 
     *             in future releases.
     * @return true on Solaris and Linux and true on Windows platform if Mozilla
     * is set as the default browser.
     */
    public static boolean isDefaultBrowserMozilla() {
        return org.jdesktop.jdic.browser.internal.WebBrowserUtil.
            isDefaultBrowserMozilla();        
    }

    /**
     * Gets the native Mozilla GRE home directory installed with a .exe package.
     * 
     * @deprecated This method is not recommended for use and may be removed 
     *             in future releases.
     * @return the GRE home directory of the currently installed Mozilla.
     */
    public static String getMozillaGreHome() {
        return org.jdesktop.jdic.browser.internal.WebBrowserUtil.
            getMozillaGreHome();    
    }    
}
