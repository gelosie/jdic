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

package org.jdesktop.jdic.init;


/**
 *  Abstract base class for describing a native platform to initialize for.
 *  Encapsulates the detection of the platform and the names of the jars and
 *  directories containing the native files.
 *
 * @author     Michael Samblanet
 * @created    August 2, 2004
 */
public abstract class Platform {
	/**  Static referece to the Linux platform */
	public final static Platform LINUX = LinuxPlatform.getLinuxPlatform();
	/**  Listing of all known platforms */
	public final static Platform[] PLATFORMS = {WindowsPlatform.getWindowsPlatform(), LinuxPlatform.getLinuxPlatform(), SolarisPlatform.getSolarisPlatform()};
	/**  Static referece to the Solaris platform */
	public final static Platform SOLARIS = SolarisPlatform.getSolarisPlatform();
	/**  Static referece to the Windows platform */
	public final static Platform WINDOWS = WindowsPlatform.getWindowsPlatform();
	
	/**  Name of the directory containing native files (for generic impl only) */
	private String mPlatformDir;
	/**  Name of the jar containing native files (for generic impl only) */
	private String mPlatformJar;


	/**
	 *  Allows overriding of the native files directory name for platforms using the generic implementation
	 *
	 * @param  dir  New directory name for the native files
	 */
	public void setDirName(String dir) {
		mPlatformDir = dir;
	}


	/**
	 *  Allows overriding of the native files jar name for platforms using the generic implementation
	 *
	 * @param  jar  New jar name for the native files
	 */
	public void setJarName(String jar) {
		mPlatformJar = jar;
	}


	/**
	 * @return    The Platform object representing the currently running platform
	 *      or null if the platform cannot be identified
	 */
	public static Platform getPlatform() {
		for (int i = 0; i < PLATFORMS.length; i++) {
			if (PLATFORMS[i].isCurrentPlatform()) {
				return PLATFORMS[i];
			}
		}
		return null;
	}


	/**
	 * @return    The directory (from the root of the classpath) where the native
	 *      files for this platform are located. This is used to locate the files
	 *      in the JAR to copy when not running as web-start.
	 */
	public String getDirName() {
		return mPlatformDir;
	}


	/**
	 * @return    Name of the JAR file that contains the native files for this
	 *      platform (for WebStart use)
	 */
	public String getJarName() {
		return mPlatformJar;
	}


	/**
	 * @return    A descriptive name of the platform for user/error display
	 */
	public abstract String getName();


	/**
	 * @return    TRUE if the current running platfor is the one described by the
	 *      class
	 */
	public abstract boolean isCurrentPlatform();
}

