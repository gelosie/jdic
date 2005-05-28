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

package org.jdesktop.jdic.fileutil.impl;

/**
 * @author Fábio Castilho Martins
 *  
 */
class Win32NativeFileUtil {
	
	static {
    	System.loadLibrary("jdic_fileutil");
    }
	
	static native long[] getFreeSpace(String fullPath);

    static native int recycle(String fullPath, boolean confirm);
	
	static native String getFileSystem(String fullPath);
	
	static native boolean isArchive(String fullPath);
	
	static native boolean setArchive(String fullPath, boolean status);
	
	static native boolean isNormal(String fullPath);
	
	static native boolean setNormal(String fullPath);
	
	static native boolean isReadOnly(String fullPath);
	
	static native boolean isSystem(String fullPath);
	
	static native boolean setSystem(String fullPath, boolean status);
	
	static native boolean isTemporary(String fullPath);
	
	static native boolean setTemporary(String fullPath, boolean status);
	
	static native boolean isCompressed(String fullPath);
	
	static native boolean isEncrypted(String fullPath);
	
	static native boolean setHidden(String fullPath, boolean status);

}
