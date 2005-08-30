/*
 * Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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

package org.jdesktop.jdic.fileutil;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @author Fábio Castilho Martins  
 */
abstract class NativeFileUtil {
	
	/**
     * Return a new NativeFileUtil object.
     * <p>
     * This will create the appropriate implementation for the currently 
     * running platform.
     * <p>
     * If an implementation is not available (the current platform is not 
     * supported) then it will throw a UnsupportedPlatformException.
     * 
     * @return NativeFileUtil for the current platform.
     * @throws UnsupportedOperationException If the method isn't supported in the specific platform.     
     */	
	public static NativeFileUtil getNativeFileUtil() throws UnsupportedOperationException {
			String os_name = System.getProperty("os.name");
			if (os_name.startsWith("Windows")) {
				return new Win32NativeFileUtil();
			} else if (os_name.startsWith("Linux") || os_name.startsWith("LINUX")) {
				return new UnixNativeFileUtil();
			} else if (os_name.startsWith("Solaris") || os_name.startsWith("SunOS")) {
				return new SolarisNativeFileUtil();
			} /*else if (os_name.startsWith("Mac OS X")) {
				return new MacOSXNativeFileUtil();
			}*/ else {
				throw new UnsupportedOperationException("Your platform is not supported yet");
			}
	}
    
    /**
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can.
     * 
     * @param file the file or directory to be recycled.
     * @return <b>true</b> if and only if the file or directory is
     *         successfully recycled; <b>false</b> otherwise.
     * @throws IOException If an I/O error occurs. 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed.
     * @throws UnsupportedOperationException if the method isn't supported in the specific platform.
     */
    public abstract boolean recycle(File file) throws IOException,
            SecurityException, UnsupportedOperationException;

    /**
     * Return the amount of free bytes available in the directory or file
     * referenced by the file Object.
     * 
     * @param file
     * @return the amount of free space in the Disk. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     * @throws UnsupportedOperationException if the method isn't supported in the specific platform.
     */
    public abstract BigInteger getFreeSpace(File file) throws IOException, UnsupportedOperationException;
    
    /**
     * Return the size of the partition denoted by the file Object.
     * 
     * @param file
     * @return the size of the partition. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     * @throws UnsupportedOperationException if the method isn't supported in the specific platform.
     */
    public abstract BigInteger getTotalSpace(File file) throws IOException, UnsupportedOperationException;
    
    public abstract String readFirst(String fullPath);
    
    public abstract String readNext();
    
    public abstract void close();    

}
