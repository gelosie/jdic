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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.browser.WebBrowserUtil;

/**
 *  Singleton class describing the Mozilla browser integration libraries for JDIC
 *  initialization
 *
 *  @author Paul Huang
 *  @created August 20, 2004
 */
public class MozBrowserPackage extends Package {


	/**  Singleton instance of this class */
	private final static MozBrowserPackage sSingleton = new MozBrowserPackage();
    private static final String MozEnvVarName = "MOZILLA_FIVE_HOME";


	/**  Private constructor to prevent public construction */
	private MozBrowserPackage() {
		addPlatform(Platform.WINDOWS, new String[]{"MozEmbed.exe"});
		addPlatform(Platform.LINUX, new String[]{"mozembed-linux-gtk2"});
		addPlatform(Platform.SOLARIS, new String[]{"mozembed-solaris-gtk2"});
	}

	/**
	 * @return    The singleton instance of this class
	 */
	public static MozBrowserPackage getBrowserPackage() {
		return sSingleton;
	}


	/**
	 *  {@inheritDoc}
	 *
	 * @return    The name value
	 */
	public String getName() {
		return "MozillaBrowser";
	}


	/**
	 * The post copy of MozBrowserPackage will do the following actions: 
     * 1. Check and set MOZILLA_FIVE_HOME, LD_LIBRARY_PATH
     * 2. For Windows, copy the mozembed.exe into the MOZILLA_FIVE_HOME dir
     * 3. Set the PATH env variable accordingly
     * 4. Set the browserBinary variable of WebBrowser accordingly.
     * 
	 * @exception  JdicInitException  
     *          Should never be thrown - used to wrap introspection exceptions
	 */
	public void postCopy() throws JdicInitException {
        boolean isOnWindows = false;
        if(WindowsPlatform.getWindowsPlatform().isCurrentPlatform()) {
            isOnWindows = true;
        }
        // Check and set MOZILLA_FIVE_HOME
        String envMFH = InitUtility.getEnv(MozEnvVarName);
        if (null == envMFH) {
            //MOZILLA_FIVE_HOME not set, 
            String nativeBrowserPath = WebBrowserUtil.getBrowserPath();
            if (null == nativeBrowserPath) {
                throw new JdicInitException(
                        "Can't locate the native browser path!");
            }
            File browserFile = new File(nativeBrowserPath);
            try {
                if (browserFile.isDirectory()) {
                	envMFH = browserFile.getCanonicalPath();
                } else {
                	envMFH = browserFile.getCanonicalFile().getParent();
                }
                //set MOZILLA_FIVE_HOME                
                InitUtility.setEnv(MozEnvVarName, envMFH);
                //append the MOZILLA_FIVE_HOME to the LD_LIBRARY_PATH
                InitUtility.appendEnv("LD_LIBRARY_PATH", envMFH);
            } catch (IOException e) {
                throw new JdicInitException(e);
            }
        }
        String sourceFileName = JdicManager.getManager().getBinaryDir() 
                                + File.separator
								+ getFiles(JdicManager.getManager().getPlatform())[0].toString();
        // Copy the relevant native files into the MOZILLA_FIVE_HOME dir
        // This should be done only for Mozilla 1.4 & Windows Platform
        if (isOnWindows) {
            String destFileName = envMFH + File.separator + getFiles(JdicManager.getManager().getPlatform())[0].toString();
            try {
            	InitUtility.copyFile(sourceFileName, destFileName);    
            } catch (IOException e) {
            	throw new JdicInitException(e);
            }
        }
        //Sets the path to the browser executable file
        try {
            Field bbf = WebBrowser.class.getDeclaredField("browserBinary");
            bbf.setAccessible(true);
            if (bbf.get(null) == null) {
                String bbPath;
                if (isOnWindows) {
                    // On windows, we've already copied the mozembed.exe into the MOZILLA_FIVE_HOME DIR
                	bbPath = envMFH + File.separator 
                             + getFiles(JdicManager.getManager().getPlatform())[0].toString();
                    InitUtility.appendEnv("PATH", envMFH);
                } else {
                    // On *nix, we do not do the copy but we should change the file to be executable
                	bbPath = sourceFileName;
                    Process chmodProc = Runtime.getRuntime().exec("chmod +x " + sourceFileName);
                    chmodProc.waitFor();
                  	InitUtility.appendEnv("PATH", JdicManager.getManager().getBinaryDir().getCanonicalPath());
                }
                bbf.set(null, bbPath);
            }
        } catch (NoSuchFieldException e) {
            throw new JdicInitException(e);
        } catch (IllegalAccessException e) {
            throw new JdicInitException(e);
        } catch (IOException e) {
        	throw new JdicInitException(e);
        } catch (InterruptedException e) {
        	throw new JdicInitException(e);
        }
	}
}
