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
 * 
 * @deprecated As of release 0.9 of JDIC. It was meant as an inner class, but 
 *             was marked "public" due to a coding error. It will be removed 
 *             in a future release.  
 */
public class WebBrowserUtil {    
    /**
     * Gets the native browser path.
     * 
     * @deprecated
     * @return the path of the default browser in the current system
     */
    public static String getBrowserPath() {
        return org.jdesktop.jdic.browser.internal.WebBrowserUtil.
            getBrowserPath();
    }
    
    /**
     * Checks if the default browser for the current platform is Mozilla.
     * 
     * @deprecated
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
     * @deprecated
     * @return the GRE home directory of the currently installed Mozilla.
     */
    public static String getMozillaGreHome() {
        return org.jdesktop.jdic.browser.internal.WebBrowserUtil.
            getMozillaGreHome();    
    }    
}
