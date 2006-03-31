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

package org.jdesktop.jdic.browser;

import java.io.File;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;
import org.jdesktop.jdic.init.InitUtility;
import org.jdesktop.jdic.init.JdicInitException;
import org.jdesktop.jdic.init.JdicManager;

/**
 * Handles the communication with Internet Explorer.
 * 
 * @author Alexander Hars, Inventivio GmbH 
 * @author Michael Shan
 */
public class InternetExplorerEngine implements IBrowserEngine {

	private static final String NSPR4_DLL = "nspr4.dll";

	private static final String IELIB = "ielib";

	private static final String IEXPLORE = "iexplore";

	private static final String BROWSER_NAME = BrowserEngineManager.IE;

	private static String browserBinary = "IeEmbed.exe";;

	private static boolean isEngineAvailable = true;

	private static boolean initialized = false;

	private static final String libPathEnv = WebBrowserUtil.LIB_PATH_ENV;

	private String nspr4dllPath = null;

	private String browserFullPath = "";

	/**
	 * @return The standardized name of the embedded browser engine. May not be
	 *         null or empty.
	 */
	public String getBrowserName() {
		return BROWSER_NAME;
	}

//	/**
//	 * Additional information about the specific type and version of embedded
//	 * browser component being used.
//	 * 
//	 * @return A string indicating the specific version of the browser or
//	 *         browser component which is being embedded, null or an empty
//	 *         string.
//	 */
//	public String getBrowserVersion() {
//		return "";
//	}

	/**
	 * Checks whether the associated Engine is available on the current system.
	 * This is a prerequisite for creating <code>Browser</code> instances on
	 * the current system.
	 * 
	 * @return true if an engine is available, false if no engine is available,
	 *         the engine is not found or can not be accessed.
	 */
	public boolean isEngineAvailable() {
		isEngineAvailable = WebBrowserUtil.IS_OS_WINDOWS;
		return isEngineAvailable;
	}

	/**
	 * Checks whether IE is the default Browser.
	 * 
	 * @param browserPath
	 *            default browser path taken from the OS or whereever
	 * @return true if this BrowserEngine represents the default browser, false
	 *         otherwise or if browserPath is null
	 */
	public boolean isDefaultBrowser(String browserPath) {
		if (browserPath == null) {
			return false;
		} else {
			return browserPath.toLowerCase().indexOf(IEXPLORE.toLowerCase()) >= 0;
		}
	}

	/**
	 * 
	 * @return
	 */
	protected boolean prepareVariables() {
		return true;
	}

	/**
	 * Used to set the path of nspr4.dll path,seems needn't it anymore.
	 * 
	 * @deprecated this should be removed
	 * @param nspr4Path
	 * @return
	 */
	// TODO:path should be lib1:lib2:
	private boolean setNspr4dllPath(String nspr4Path) {
		// check it first
		String filePath = nspr4Path + NSPR4_DLL;
		File file = new File(filePath);
		if (file != null && file.exists()) {
			nspr4dllPath = nspr4Path;
			WebBrowserUtil.trace(NSPR4_DLL + " is set under " + nspr4Path);
			return true;
		} else {
			WebBrowserUtil.error(NSPR4_DLL + " doesn't exist under "
					+ nspr4Path);
			return false;
		}
	}

	/**
	 * 
	 * Pre-append the "ielib" directory to PATH, which includes the
	 * bundled,IeEmbed.exe dependent library nspr4.dll.
	 * 
	 * If nspr4.dll doesn't under curr folder,user can: 1. set it through
	 * setLibPath method 2. set it directlly in the path
	 */
	protected void setEnv() {
		if (nspr4dllPath == null) {
			String nsprPath = JdicManager.getManager().getBinaryPath()
					+ File.separator + IELIB;
			InitUtility.preAppendEnv(libPathEnv, nsprPath);
			WebBrowserUtil.trace(NSPR4_DLL + " under " + nsprPath
					+ " is set to PATH");
		} else {
			InitUtility.preAppendEnv(libPathEnv, nspr4dllPath);
			WebBrowserUtil.trace(NSPR4_DLL + " under " + nspr4dllPath
					+ " is set to PATH");
		}
	}

	public String getBrowserBinary() {
		return browserBinary;
	}

	/**
	 * @deprecated
	 * @return Returns the nspr4dllPath.
	 */
	protected String getNspr4dllPath() {
		return nspr4dllPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IBrowserEngine#initialize()
	 */
	public void initialize() throws JdicInitException {
		if (!initialized) {
			this.prepareVariables();
			this.setEnv();
			initialized = true;
			WebBrowserUtil.trace("Engine initialize once!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowserEngine#getBrowserFullPath()
	 */
	public String getEmbeddedBinaryName() {
		return browserBinary;
	}

	/**
	 * Not suppored under win for IE
	 * 
	 */
	public void setEnginePath(String fullPath) {
		this.browserFullPath = fullPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowserEngine#getCharsetName()
	 */
	public String getCharsetName() {
		return System.getProperty("file.encoding");
	}

	/**
	 * IE omited the file protocol
	 */
	public String getFileProtocolURLPrefix() {
		return "file:/";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IBrowserEngine#isInitialized()
	 */
	public boolean isInitialized() {
		return initialized;
	}
}
