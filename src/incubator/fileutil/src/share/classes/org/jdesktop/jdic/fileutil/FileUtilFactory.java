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

package org.jdesktop.jdic.fileutil;

import org.jdesktop.jdic.fileutil.impl.MacOSXFileUtil;
import org.jdesktop.jdic.fileutil.impl.SolarisFileUtil;
import org.jdesktop.jdic.fileutil.impl.UnixFileUtil;
import org.jdesktop.jdic.fileutil.impl.Win32FileProperties;
import org.jdesktop.jdic.fileutil.impl.Win32FileUtil;

/**
 * @author Fábio Castilho Martins
 *  
 */
public class FileUtilFactory {
	
	/**
     * Return a new FileUtil object.
     * <p>
     * This will create the appropriate implementation for the currently 
     * running platform.
     * <br>
     * If an implementation is not available (the current platform is not 
     * supported) then it will throw a UnsupportedPlatformException.
     * 
     * @return FileUtil for the current platform.
     * @throws ExceptionInInitializerError if the initialization of the 
     *         Platform implementation provoked by this method fails.
     * @throws SecurityException if there is no permission to create a new
     *         instance.
     * @throws ClassNotFoundException if the ClassNotFoundException if the class cannot be located.
     */
	public static FileUtil getFileUtilInstance() throws IllegalAccessException,
			InstantiationException, ClassNotFoundException, UnsupportedOperationException {
		String os_name = System.getProperty("os.name");

		if (os_name.startsWith("Mac OS X")) {
			return (MacOSXFileUtil) Class.forName(
					"org.jdesktop.jdic.fileutil.impl.MacOSXFileUtil")
					.newInstance();
		} else if (os_name.startsWith("Windows")) {
			return (Win32FileUtil) Class.forName(
					"org.jdesktop.jdic.fileutil.impl.Win32FileUtil")
					.newInstance();
		} else if (os_name.startsWith("Linux") || os_name.startsWith("LINUX")) {
			return (UnixFileUtil) Class.forName(
					"org.jdesktop.jdic.fileutil.impl.UnixFileUtil")
					.newInstance();
		} else if (os_name.startsWith("Solaris") || os_name.startsWith("SunOS")) {
			return (SolarisFileUtil) Class.forName(
					"org.jdesktop.jdic.fileutil.impl.SolarisFileUtil")
					.newInstance();
		} else {
			throw new UnsupportedOperationException("Your platform is not supported yet");
		}
	}

	public static FileProperties getFilePropertiesInstance() throws IllegalAccessException,
	InstantiationException, ClassNotFoundException, UnsupportedOperationException {
		String os_name = System.getProperty("os.name");
		
		if (os_name.startsWith("Windows")) {
			return (Win32FileProperties) Class.forName(
					"org.jdesktop.jdic.fileutil.impl.Win32FileProperties")
					.newInstance();
		} 
		else {
			throw new UnsupportedOperationException("Your platform is not supported yet");
		}
	}

}
