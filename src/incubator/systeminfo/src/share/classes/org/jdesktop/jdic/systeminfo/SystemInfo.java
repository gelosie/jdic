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
package org.jdesktop.jdic.systeminfo;


/**
 * Provide information concerning the current user's desktop session.
 * 
 *<p>
 * Windows Usage:<br>
 * For Windows, most information is queried from info variables available thru 
 * the Win32 API. Older Windows versions, however, don't support these methods
 * so callbacks into event handlers must be inserted.<br>
 * This can cause dll locking (which in turn can mess up some multiuser
 * instances), but offers more backwards-compatibility.<br>
 * If you want to force callbacks code to be used, 
 */
public class SystemInfo
{
    private static boolean _systemInfoLibraryLoaded;

    /**
     * Load the native library needed
     * @todo Find a way to reload the class and still retain the system library
     */
    static
    {
        String operatingSystem = System.getProperty("os.name");
        boolean oldWindows = operatingSystem.matches("Windows (95|98|NT).*");
        
        String callbacksFlag = System.getProperty("systeminfo.callbacks");
        boolean forceCallbacks = "true".equals(callbacksFlag);
        
        try
        {
            if(oldWindows || forceCallbacks)
            {
                System.loadLibrary("systemcallback");
            }
            else
            {
                System.loadLibrary("systeminfo");
            }
              
            _systemInfoLibraryLoaded = true;
        }
        catch (UnsatisfiedLinkError e)
        {
            System.err.println("Couldn't find system info library in " + System.getProperty("java.library.path"));
            e.printStackTrace(System.err);
            _systemInfoLibraryLoaded = false;
        }
    }

    private int seconds = 0;

    private static native long nativeGetSessionIdleTime();
    
    private static native boolean nativeIsSessionLocked();

    /**
     * Return how long a user's session has been idle
     * @return The amount of time in milliseconds the session has been idle
     */
    public static long getSessionIdleTime()
    {
        if (_systemInfoLibraryLoaded)
        {
            return nativeGetSessionIdleTime();
        }
        else
        {
            return -1L;
        }
    }

    /**
     * Return a boolean true if the user's session or workstation has been locked
     * @return true if the session or workstation is locked and false otherwise
     */
    public static boolean isSessionLocked()
    {
        if (! _systemInfoLibraryLoaded)
        {
            throw new RuntimeException("systeminfo.dll not loaded");
        }

        return nativeIsSessionLocked();
    }
}
