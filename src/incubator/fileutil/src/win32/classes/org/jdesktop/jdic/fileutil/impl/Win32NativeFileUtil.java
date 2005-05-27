/*
 * Created on 26/05/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.jdesktop.jdic.fileutil.impl;

/**
 * @author padrao
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
class Win32NativeFileUtil {
	
	static {
    	System.loadLibrary("jdic_fileutil");
    }
	
	static native long[] getFreeSpace(String fullPath);

    static native int recycle(String fullPath, boolean confirm);
	
	static native String getFileSystem(String fullPath);
	
	static native boolean isArchive(String fullPath);
	
	static native boolean setArchive(String fullPath);
	
	static native boolean isNormal(String fullPath);
	
	static native boolean setNormal(String fullPath);
	
	static native boolean isReadOnly(String fullPath);
	
	static native boolean isSystem(String fullPath);
	
	static native boolean setSystem(String fullPath);
	
	static native boolean isTemporary(String fullPath);
	
	static native boolean setTemporary(String fullPath);
	
	static native boolean isCompressed(String fullPath);
	
	static native boolean isEncrypted(String fullPath);
	
	static native boolean setHidden(String fullPath);

}
