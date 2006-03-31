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
import java.io.IOException;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;
import org.jdesktop.jdic.init.InitUtility;
import org.jdesktop.jdic.init.JdicInitException;
import org.jdesktop.jdic.init.JdicManager;

/**
 * Handles the communication with Mozilla's Gecko Runtime Engine (GRE).
 * 
 * @author Alexander Hars, Inventivio GmbH
 * @author Michael Shan
 */
public class MozillaEngine implements IBrowserEngine {

	private static final String BROWSER_NAME = BrowserEngineManager.MOZILLA;

	private static final String MOZILLA_FIVE_HOME = "MOZILLA_FIVE_HOME";

	private static final String BIN_WIN_MOZILLA = "MozEmbed.exe";

	private static final String BIN_LINUX_GTK1 = "mozembed-linux-gtk1.2";

	private static final String BIN_linux_GTK2 = "mozembed-linux-gtk2";

	private static final String BIN_FREEBSD_GTK1 = "mozembed-freebsd-gtk1.2";

	private static final String BIN_FREEBSD_GTK2 = "mozembed-freebsd-gtk2";

	private static final String BIN_SOLARIS_GTK1 = "mozembed-solaris-gtk1.2";

	private static final String BIN_SOLARIS_GTK2 = "mozembed-solaris-gtk2";

	/** Whether the Mozilla/GRE is available */
	private static boolean isEngineAvailable = true;

	/** if it has been initialized */
	private static boolean initialized = false;

	/** path to load libs */
	private static final String libPathEnv = WebBrowserUtil.LIB_PATH_ENV;

	/**
	 * Path to the mozilla executable,which must contain the xpcom.dll file.
	 */
	private String envXPComPath;

	/** the executable bin name of according browser */
	private String browserBinName;

	/** current parent path which runs JDIC */
	private String runningPath;

	/** The full path containing xpcom.dll which is set by user */
	private String xpcomPathSetByUser;

	/**
	 * Set according path items and grant executable permission to bin file.
	 *  
	 */
	public void initialize() throws JdicInitException {
		if (!initialized) {
			preapareEnvVariables();
			setToEnv();
			grantXToBinFile(this.envXPComPath);
			initialized = true;
		}
	}

	/**
	 * @return The standardized name of the embedded browser engine. May not be
	 *         null or empty.
	 */
	public String getBrowserName() {
		return BROWSER_NAME;
	}

//	/**
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
		return isEngineAvailable;
	}

	/**
	 * Checks whether mozilla is default Browser on the current system.
	 * 
	 * @param browserPath
	 *            taken from the OS or whereever
	 * @return if default browser return ture else return false
	 *  
	 */
	public boolean isDefaultBrowser(String browserPath) {
		if (browserPath == null) {
			return false;
		} else {
			return browserPath.toLowerCase()
					.indexOf(BROWSER_NAME.toLowerCase()) >= 0;
		}
	}

	/**
	 * get and set env variables For xpcom under win,it maybe under mozilla path
	 * or under gre path. It can be set by: 1. setEnginePath()
	 * 2.MOZILLA_FIVE_PATH 3.
	 * 
	 * @throws JdicInitException
	 */

	//TODO:Bug,for installed gre,can't show <input>element.
	protected void preapareEnvVariables() throws JdicInitException {
		//current running path
		runningPath = JdicManager.getManager().getBinaryPath();

		//if set by user
		if (getXPComPath(xpcomPathSetByUser)) {
			WebBrowserUtil.trace("Got xpcom from user set");
			return;
		}

		//check env
		if (getXPComPath(InitUtility.getEnv(MOZILLA_FIVE_HOME))) {
			WebBrowserUtil.trace("Got xpcom from MOZILLA_FIVE_HOME");
			return;
		}

		String defaultBrowserPath = WebBrowserUtil.getDefaultBrowserPath();
		//check error
		if (!isDefaultBrowser(defaultBrowserPath)) {
			WebBrowserUtil
					.error("Mozilla isn't default browser but set as active one, you must set its path(folder contains xpcom lib) through setEnginePath() or env MOZILLA_FIVE_HOME.");
			throw new JdicInitException(
					"Mozilla isn't default browser but set as active one, you must set its path(folder contains xpcom lib) through setEnginePath() or env MOZILLA_FIVE_HOME.");

		}

		//query from win registry or path under unix
		if (getXPComPath(defaultBrowserPath)) {
			WebBrowserUtil.trace("Got xpcom from registry(win)/path(unix)");
			return;
		}

		if (WebBrowserUtil.IS_OS_WINDOWS) {
			//for win only
			//query registry's GRE path for Mozilla installed from a .exe
			if (getXPComPath(WebBrowserUtil.getMozillaGreHome())) {
				WebBrowserUtil.trace("Got xpcom from GREHome"
						+ WebBrowserUtil.getMozillaGreHome());
				return;
			}
		}

		//error
		isEngineAvailable = false;
		WebBrowserUtil
				.error("Can't find xpcom.dll/libxpcom.so!You must set its path(folder contains xpcom lib) through setEnginePath() or env MOZILLA_FIVE_HOME.");
		throw new JdicInitException(
				"Can't find xpcom.dll/libxpcom.so!You must set its path(folder contains xpcom lib) through setEnginePath() or env MOZILLA_FIVE_HOME.");
	}

	/**
	 * Set the path of xpcom.dll/libxpcom.so.
	 * 
	 * @param containingPath
	 * @return
	 * @throws JdicInitException
	 */
	private boolean getXPComPath(String inputPath) throws JdicInitException {
		if (inputPath == null) {
			return false;
		}
		//erase "" in the path
		String containingPath = inputPath.replaceAll("\"", "");
		String xpcomFolder = null;

		//get parent path
		File browserFile = new File(containingPath);
		try {
			if (browserFile.isDirectory()) {
				xpcomFolder = browserFile.getCanonicalPath();
			} else {
				xpcomFolder = browserFile.getCanonicalFile().getParent();
			}
		} catch (IOException ex) {
			WebBrowserUtil.trace(ex.toString());
			WebBrowserUtil
					.error("Path \"" + containingPath + "\" is invalide.");
			throw new JdicInitException(ex.getMessage());
		}

		if (isXPComPathValid(xpcomFolder)) {
			envXPComPath = xpcomFolder;//set global
			return true;
		}
		return false;
	}

	/**
	 * @throws JdicInitException
	 */
	private boolean isXPComPathValid(String xpcomFileContainPath)
			throws JdicInitException {
		String xpcomFileExtention = null;

		if (WebBrowserUtil.IS_OS_WINDOWS) {
			xpcomFileExtention = "xpcom.dll";
		} else {
			xpcomFileExtention = "libxpcom.so";
		}
		String xpcomFile = xpcomFileContainPath + File.separator
				+ xpcomFileExtention;
		if ((new File(xpcomFile).exists())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @throws JdicInitException
	 */
	private void grantXToBinFile(String mozillaPath) throws JdicInitException {

		if (WebBrowserUtil.IS_OS_WINDOWS) {
			browserBinName = BIN_WIN_MOZILLA;
			return;
		}
		if (WebBrowserUtil.IS_OS_LINUX || WebBrowserUtil.IS_OS_SUNOS
				|| WebBrowserUtil.IS_OS_FREEBSD) {
			caculateUnixBinaryName(mozillaPath);
			return;
		}
		// other os
		WebBrowserUtil.trace("Not suppored OS now!");
		isEngineAvailable = false;
		throw new JdicInitException("Un supported OS!");
	}

	/**
	 * Caculate the browser binary name under unix
	 * 
	 * @param mozillaPath
	 * @throws JdicInitException
	 */
	private void caculateUnixBinaryName(String mozillaPath)
			throws JdicInitException {
		String osname = WebBrowserUtil.OS_NAME;
		String unixBinary = null;
		String libwidgetpath = mozillaPath + File.separator + "components"
				+ File.separator + "libwidget_gtk2.so";
		File file = new File(libwidgetpath);
		if (!file.exists()) {
			if (WebBrowserUtil.IS_OS_LINUX) {
				unixBinary = BIN_LINUX_GTK1;
			} else if (WebBrowserUtil.IS_OS_SUNOS) {
				unixBinary = BIN_SOLARIS_GTK1;
			} else if (WebBrowserUtil.IS_OS_FREEBSD) {
				unixBinary = BIN_FREEBSD_GTK1;
			}
		} else {
			if (WebBrowserUtil.IS_OS_LINUX) {
				unixBinary = BIN_linux_GTK2;
			} else if (WebBrowserUtil.IS_OS_SUNOS) {
				unixBinary = BIN_SOLARIS_GTK2;
			} else if (WebBrowserUtil.IS_OS_FREEBSD) {
				unixBinary = BIN_FREEBSD_GTK2;
			}
		}
		if (unixBinary != null) {
			browserBinName = unixBinary;//set the variable
			grantXToBin(unixBinary);//grant executable to the lib
		} else {
			WebBrowserUtil
					.trace("Failed to grant executable privilege to bin file for your OS. You'd better grant all JDIC bin files with executable privilege manually. ");
		}
	}

	/**
	 * Set variable to env.
	 */
	private void setToEnv() throws JdicInitException {
		InitUtility.preAppendEnv(libPathEnv, runningPath);
		InitUtility.preAppendEnv(libPathEnv, envXPComPath);
		InitUtility.setEnv(MOZILLA_FIVE_HOME, envXPComPath);
	}

	/**
	 * Grant browser binary's "x" permission after extracted from .zip
	 * file,which should be "r" at least. This's more reasonable to webstart bin
	 * file,since user perhaps don't know where this bin is located in cache.
	 * 
	 * @return
	 */
	private boolean grantXToBin(String unixBinary) throws JdicInitException {
		try {
			WebBrowserUtil.trace("will grant a+x to " + runningPath
					+ File.separator + unixBinary);
			Runtime.getRuntime().exec(
					"chmod a+x " + runningPath + File.separator + unixBinary);
			WebBrowserUtil.trace("grant ok");
			return true;
		} catch (IOException ex) {
			WebBrowserUtil.error(ex.getMessage());
			ex.printStackTrace();
			throw new JdicInitException(ex.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowserEngine#getBrowserFullPath()
	 */
	public String getEmbeddedBinaryName() {
		return browserBinName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowserEngine#setBrowserFullPath()
	 */
	public void setEnginePath(String fullPath) {
		xpcomPathSetByUser = fullPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowserEngine#getCharsetName()
	 */
	public String getCharsetName() {
		return "UTF-8";
	}

	/**
	 * Mozilla will not omit this prefix
	 *  
	 */
	public String getFileProtocolURLPrefix() {
		return "";
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