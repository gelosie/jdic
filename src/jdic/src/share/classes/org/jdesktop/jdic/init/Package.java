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

import java.util.HashMap;


/**
 *  Abstract base class describing a subsystem within JDIC. These classes must
 *  be aware of native libraries required.
 *
 * @author     Michael Samblanet
 * @created    August 2, 2004
 */
public abstract class Package {
	/**  Static instance of the Web Browser Integration package */
	public final static Package BROWSER = IEBrowserPackage.getBrowserPackage();
	/**  Static instance of the mandatory, core package */
	public final static Package CORE = CorePackage.getCorePackage();
	/**  Static array of the mandatory packages */
	public final static Package[] MANDATORY_PACKAGES = {CorePackage.getCorePackage()};
	/**  Static array of all optional packages */
	public final static Package[] OPTIONAL_PACKAGES = {IEBrowserPackage.getBrowserPackage(), SysTrayPackage.getSysTrayPackage()};
	/**  Static instance of the System Tray package */
	public final static Package SYSTRAY = SysTrayPackage.getSysTrayPackage();

	/**
	 *  Optional map for use by the generic Platform->File mapping implementation.
	 *  Maps Platform objects to String[] for native file names
	 */
	public HashMap mFilesMap = null;
    
    private boolean isInitialized = false;


	/**
	 * @param  platform  Platform for which you wish to locate files
	 * @return           String[] listing all the files for this platform. If no
	 *      files are needed, return an 0-length array. If the platform is not
	 *      supported, return null.
	 */
	public String[] getFiles(Platform platform) {
		if (mFilesMap == null) {
			return null;
		}
		return (String[]) mFilesMap.get(platform);
	}


	/**
	 * @return    A descriptive name for the package for use in user or error
	 *      display
	 */
	public abstract String getName();


	/**
	 *  Adds generic support for a platform by listing files in the mFilesMap
	 *
	 * @param  p      Platform to set the fioles for
	 * @param  files  Native files needed by this platform
	 */
	public void addPlatform(Platform p, String[] files) {
		if (mFilesMap == null) {
			mFilesMap = new HashMap();
		}
		mFilesMap.put(p, files);
	}


	/**
	 *  Method called after copying all the files for a platform
	 *
	 * @exception  JdicInitException  Generic initialization exception
	 */
	public void postCopy() throws JdicInitException {
	}


	/**
	 *  Method called before copying all the files for a platform
	 *
	 * @exception  JdicInitException  Generic initialization exception
	 */
	public void preCopy() throws JdicInitException {
	}
    
    /**
     * Checks if this package has been initialized. 
     * @return true if this package has been initialized.
     */
    public boolean getIsInitialized() {
        return isInitialized;
    }
    
    /**
     * Sets the initialized status of this package.
     *
     */
    public void setIsInitialized(boolean initializedStatus) {
        isInitialized = initializedStatus;
    }
}

