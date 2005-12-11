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

import org.jdesktop.jdic.fileutil.NativeFileUtil;

/**
 * @author Fábio Castilho Martins
 *  
 */
class UnixNativeFileUtil extends NativeFileUtil {

	static {
    	System.loadLibrary("jdic_fileutil");
    }
	
	private int handle;
	
	/**
     * This method isn't supported on Linux. It will throw an UnsupportedOperationException.
     * <p>
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can.
     * 
     * @param file the file or directory to be recycled.
     * @return <b>true</b> if and only if the file or directory is
     *         successfully recycled; <b>false</b> otherwise.
     * @throws IOException If an I/O error occurs. 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed.
     * @throws UnsupportedOperationException If the method isn't supported in the specific platform.
     */
    public boolean recycle(File file) throws IOException, SecurityException {
    	throw new UnsupportedOperationException("Your platform doesn't support this method");
    }

    /**
     * Return the amount of free bytes available in the directory or file
     * referenced by the file Object. The returned value is the same obtained by "df" command on Linux machines, multiplied by 1024.
     * 
     * @param file
     * @return the amount of free space in the Disk. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     */
    public BigInteger getFreeSpace(File file) throws IOException {
        BigInteger freeSpace;
        
        if (file.isFile()) {
        	freeSpace = new BigInteger(Long.toString(this.getFreeSpace(file.getCanonicalFile().getParent())));
            return freeSpace; 
        } else if (file.isDirectory()) {
        	freeSpace = new BigInteger(Long.toString(this.getFreeSpace(file.getCanonicalPath())));
            return freeSpace;
        } else {
            return BigInteger.ZERO;
        }
    }
    
    public BigInteger getTotalSpace(File file) throws IOException {
    	BigInteger totalSpace;
        
        if (file.isFile()) {
        	totalSpace = new BigInteger(Long.toString(this.getTotalSpace(file.getCanonicalFile().getParent())));
            return totalSpace; 
        } else if (file.isDirectory()) {
        	totalSpace = new BigInteger(Long.toString(this.getTotalSpace(file.getCanonicalPath())));
            return totalSpace;
        } else {
            return BigInteger.ZERO;
        }
	}
    
    public void close() {
    	this.findClose();
	}

	public String readFirst(String fullPath) {
		String path = this.findFirst(fullPath);
		if(path != null) {
			if(path.equals(".") || path.equals("..")) {
				path = this.readNext();
			}
			path = File.separator + path;
		}
		return path;
	}

	public String readNext() {
		String path = this.findNext();
		if(path != null) {
			if(path.equals(".") || path.equals("..")) {
				path = this.findNext();
			}
			path = File.separator + path;
		}
		return path;
	}

	private native long getFreeSpace(String fullPath);
	
	private native long getTotalSpace(String fullPath);
	
	private native String findFirst(String fullPath);
	
	private native String findNext();
	
	private native boolean findClose();

}
