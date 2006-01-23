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
 * @author Alexander Hars, Inventivio GmbH Michael Shan
 */
public class MozillaEngine implements IBrowserEngine {

	private static final String XPCOM_DLL = "xpcom.dll";

	private static final String BROWSER_NAME = BrowserEngineManager.MOZILLA;

	private static final String PATH = "PATH";

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
	private static String envMozillaFiveHome;

	private static String browserBinary;

	private String runningPath;

	private String browserFullPath;

	/**
	 * @return The standardized name of the embedded browser engine. May not be
	 *         null or empty.
	 */
	public String getBrowserName() {
		return BROWSER_NAME;
	}

	/**
	 * @return A string indicating the specific version of the browser or
	 *         browser component which is being embedded, null or an empty
	 *         string.
	 */
	public String getBrowserVersion() {
		return "";
	}

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
	 * @param defaultBrowserPath
	 *            taken from the OS or whereever
	 * @return
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
	 * Checks whether Mozilla is available and sets environment variables as
	 * needed.MOZILLA_FIVE_HOME and browser binaryName
	 * 
	 * @throws JdicInitException
	 */
	protected boolean prepareVariables() throws JdicInitException {
		String mozillaPath = browserFullPath;
		runningPath = JdicManager.getManager().getBinaryPath();

		if (mozillaPath == null) {
			// isn't set by user
			if (!isDefaultBrowser(WebBrowserUtil.getDefaultBrowserPath())) {
				// mozilla isn't default browser but is set as the active
				mozillaPath = browserFullPath;
				if (null == mozillaPath) {
					WebBrowserUtil
							.error("Need Mozilla's full path,you must specify it with setEnginePath().");
					throw new JdicInitException(
							"Need Mozilla's full path,you must specify it with setEnginePath().");
				}
			} else {
				mozillaPath = WebBrowserUtil.getDefaultBrowserPath();
			}
		}

		envMozillaFiveHome = InitUtility.getEnv(MOZILLA_FIVE_HOME);
		if (envMozillaFiveHome == null) {
			// not set, set it by mozilla path
			WebBrowserUtil.trace(MOZILLA_FIVE_HOME
					+ " isn't set, will set it by " + mozillaPath);
			setMoz5HomeByMozPath(mozillaPath);
		}

		if (WebBrowserUtil.IS_OS_WINDOWS) {
			browserBinary = BIN_WIN_MOZILLA;
			String xpcomPath = envMozillaFiveHome + File.separator + XPCOM_DLL;
			if (!(new File(xpcomPath).isFile())) {
				// try GRE path, maybe installed
				setMoz5HomeByGRE();
			}
		} else if (WebBrowserUtil.IS_OS_LINUX || WebBrowserUtil.IS_OS_SUNOS
				|| WebBrowserUtil.IS_OS_FREEBSD) {
			String xpcomPath = envMozillaFiveHome + File.separator
					+ "libxpcom.so";
			if (!new File(xpcomPath).isFile()) {
				WebBrowserUtil.error("libxpcom.so doesn't exist under "
						+ envMozillaFiveHome);
				throw new JdicInitException("libxpcom.so doesn't exist under "
						+ envMozillaFiveHome);
			}
			// change setUnixBinaryName(mozillaPath) to below
			setUnixBinaryName(envMozillaFiveHome);
		} else {
			// other operating system
			WebBrowserUtil.trace("Not suppored OS now!");
			isEngineAvailable = false;
		}
		return true;
	}

	/**
	 * @param mozillaPath
	 * @throws JdicInitException
	 */
	private void setMoz5HomeByMozPath(String mozillaPath)
			throws JdicInitException {
		File browserFile = new File(mozillaPath);
		try {
			if (browserFile.isDirectory()) {
				envMozillaFiveHome = browserFile.getCanonicalPath();
			} else {
				envMozillaFiveHome = browserFile.getCanonicalFile().getParent();
			}
		} catch (IOException ex) {
			throw new JdicInitException(ex.getMessage());
		}
	}

	/**
	 * Set mozilla home by gre path. Mozilla on Windows, reset MOZILLA_FIVE_HOME
	 * to the GRE directory path: [Common Files]\mozilla.org\GRE\1.x_BUILDID, if
	 * Mozilla installs from a .exe package.
	 * 
	 * @throws JdicInitException
	 */
	private void setMoz5HomeByGRE() throws JdicInitException {
		WebBrowserUtil.trace(envMozillaFiveHome + " doesn't contain "
				+ XPCOM_DLL + " will recaculate it.");
		// Mozilla installs from a .exe package. Check the
		// installed GRE directory.
		String mozGreHome = WebBrowserUtil.getMozillaGreHome();
		if (mozGreHome == null) {
			isEngineAvailable = false;
			throw new JdicInitException("Can't find " + XPCOM_DLL + "!");
		} else {
			envMozillaFiveHome = mozGreHome;
			WebBrowserUtil.trace(MOZILLA_FIVE_HOME + " is set to "
					+ envMozillaFiveHome);
		}
	}

	/**
	 * Caculate the browser binary name under unix
	 * 
	 * @param mozillaPath
	 */
	private void setUnixBinaryName(String mozillaPath) {
		String osname = WebBrowserUtil.OS_NAME;
		String libwidgetpath = mozillaPath + File.separator + "components"
				+ File.separator + "libwidget_gtk2.so";
		File file = new File(libwidgetpath);
		if (!file.exists()) {
			if (WebBrowserUtil.IS_OS_LINUX) {
				browserBinary = BIN_LINUX_GTK1;
			} else if (WebBrowserUtil.IS_OS_SUNOS) {
				browserBinary = BIN_SOLARIS_GTK1;
			} else if (WebBrowserUtil.IS_OS_FREEBSD) {
				browserBinary = BIN_FREEBSD_GTK1;
			}
		} else {
			if (WebBrowserUtil.IS_OS_LINUX) {
				browserBinary = BIN_linux_GTK2;
			} else if (WebBrowserUtil.IS_OS_SUNOS) {
				browserBinary = BIN_SOLARIS_GTK2;
			} else if (WebBrowserUtil.IS_OS_FREEBSD) {
				browserBinary = BIN_FREEBSD_GTK2;
			}
		}
	}

	/**
	 * prepares the environment. Returns false if the preparation was not
	 * successful.
	 */
	protected void setEnvs() throws JdicInitException {
		InitUtility.preAppendEnv(libPathEnv, runningPath);
		InitUtility.setEnv(MOZILLA_FIVE_HOME, envMozillaFiveHome);
		WebBrowserUtil.trace(MOZILLA_FIVE_HOME + " is set to"
				+ InitUtility.getEnv(MOZILLA_FIVE_HOME));
		InitUtility.preAppendEnv(libPathEnv, envMozillaFiveHome);

		if (WebBrowserUtil.IS_OS_LINUX || WebBrowserUtil.IS_OS_SUNOS
				|| WebBrowserUtil.IS_OS_FREEBSD) {
			if (!grantXToBin())
				return;
		}

	}

	/**
	 * For webstart, the browser binary will lose "x" permission after extracted
	 * from .jar file.
	 * 
	 * @return
	 */
	private boolean grantXToBin() throws JdicInitException {
		try {
			Runtime.getRuntime()
					.exec(
							"chmod a+x " + runningPath + File.separator
									+ browserBinary);
			return true;
		} catch (IOException ex) {
			throw new JdicInitException(ex.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowserEngine#initialize()
	 */
	public void initialize() throws JdicInitException {
		if (!initialized) {
			this.prepareVariables();
			this.setEnvs();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowserEngine#setBrowserFullPath()
	 */
	public void setEnginePath(String fullPath) {
		browserFullPath = fullPath;
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
		return null;
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
