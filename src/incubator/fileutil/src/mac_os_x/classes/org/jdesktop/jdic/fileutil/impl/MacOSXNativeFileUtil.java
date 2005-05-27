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
public class MacOSXNativeFileUtil {
	
	static {
    	System.loadLibrary("jdic_fileutil");
    }
	
	static native long getFreeSpace(String fullPath);

}
