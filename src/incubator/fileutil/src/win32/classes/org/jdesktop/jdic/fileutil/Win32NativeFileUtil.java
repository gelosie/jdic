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
 *  
 */
class Win32NativeFileUtil extends NativeFileUtil {
	
	static {
    	System.loadLibrary("jdic_fileutil");
    }
	
	/**
	 * Stores the Windows file HANDLE
	 */
	private int handle;
	
	/**
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can.
     * 
     * @param file the file or directory to be recycled.
     * @return <b>true</b> if and only if the file or directory is successfully 
     *         recycled; <b>false</b> otherwise.
     * @throws IOException If an I/O error occurs, which is possible because the
     *         construction of the canonical pathname may require filesystem
     *         queries. 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed.
     */
    public boolean recycle(File file) throws IOException,
            SecurityException {
        String fullPath = file.getCanonicalPath();
        int status = this.recycle(fullPath, false);
        
        if(status == 0) {
        	return !file.exists();
        }
        else {
        	throw new IOException();
        }
    }

    /**
     * Return the amount of free bytes available in the directory or file
     * referenced by the file Object.
     * 
     * @param file
     * @return the amount of free space in the Disk. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     */
    public BigInteger getFreeSpace(File file) throws IOException {
        long[] freeSpace;
        BigInteger highPart;
        BigInteger lowPart;

        if (file.isFile()) {
            freeSpace = this.getFreeSpace(file.getCanonicalFile().getParent());
            highPart = new BigInteger(String.valueOf(freeSpace[1])).shiftLeft(32);
            lowPart = new BigInteger(String.valueOf(freeSpace[0]));
            return highPart.add(lowPart); 
        } else if (file.isDirectory()) {
            freeSpace = this.getFreeSpace(file.getCanonicalPath());
            highPart = new BigInteger(String.valueOf(freeSpace[1])).shiftLeft(32);
            lowPart = new BigInteger(String.valueOf(freeSpace[0]));
            return highPart.add(lowPart);
        } else {
            return BigInteger.ZERO;
        }
    }
    
    public BigInteger getTotalSpace(File file) throws IOException {
    	long[] totalSpace;
        BigInteger highPart;
        BigInteger lowPart;

        if (file.isFile()) {
        	totalSpace = this.getTotalSpace(file.getCanonicalFile().getParent());
            highPart = new BigInteger(String.valueOf(totalSpace[1])).shiftLeft(32);
            lowPart = new BigInteger(String.valueOf(totalSpace[0]));
            return highPart.add(lowPart); 
        } else if (file.isDirectory()) {
        	totalSpace = this.getTotalSpace(file.getCanonicalPath());
            highPart = new BigInteger(String.valueOf(totalSpace[1])).shiftLeft(32);
            lowPart = new BigInteger(String.valueOf(totalSpace[0]));
            return highPart.add(lowPart);
        } else {
            return BigInteger.ZERO;
        }
	}

	public void close() {
		this.findClose();
	}

	public String readFirst(String fullPath) {
		return this.findFirst(fullPath);
	}

	public String readNext() {
		return this.findNext();
	}
	
	private native long[] getFreeSpace(String fullPath);

	private native long[] getTotalSpace(String fullPath);
	
	private native int recycle(String fullPath, boolean confirm);
	
	private native String getFileSystem(String rootPath);
	
	private native boolean isArchive(String fullPath);
	
	private native boolean setArchive(String fullPath, boolean status);
	
	private native boolean isNormal(String fullPath);
	
	private native boolean setNormal(String fullPath);
	
	private native boolean isReadOnly(String fullPath);
	
	private native boolean isSystem(String fullPath);
	
	private native boolean setSystem(String fullPath, boolean status);
	
	private native boolean isTemporary(String fullPath);
	
	private native boolean setTemporary(String fullPath, boolean status);
	
	private native boolean isCompressed(String fullPath);
	
	private native boolean isEncrypted(String fullPath);
	
	private native boolean setHidden(String fullPath, boolean status);
	
	private native String findFirst(String fullPath);
	
	private native String findNext();
	
	private native boolean findClose();

}
