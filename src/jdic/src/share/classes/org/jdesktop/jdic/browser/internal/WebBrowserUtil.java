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
package org.jdesktop.jdic.browser.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jdesktop.jdic.init.JdicInitException;
import org.jdesktop.jdic.init.JdicManager;

/**
 * Utility class for <code>WebBrowser</code> class.
 */
public class WebBrowserUtil {

	/** Returns the name of the Operating system we are currently on. */
	public static final String OS_NAME = System.getProperty("os.name");

	/** The java.home property value is required to load jawt.dll on Windows */
	public static final String JAVA_DOT_HOME = "java.home";

	/** True if the current operating system is Windows, false otherwise */
	public final static boolean IS_OS_WINDOWS = isCurrentOS("Windows");

	/** True if the current operating system is Linux, false otherwise */
	public static final boolean IS_OS_LINUX = isCurrentOS("Linux");

	/** True if the current operating system is SunOS, false otherwise */
	public static final boolean IS_OS_SUNOS = isCurrentOS("SunOS");

	/** True if the current operating system is FreeBSD, false otherwise */
	public static final boolean IS_OS_FREEBSD = isCurrentOS("FreeBSD");

	/** True if the current operating system is Mac, false otherwise */
	public static final boolean IS_OS_MAC = isCurrentOS("Mac");

	private static final String JDIC_LIB_NAME = "jdic";

	private static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";

	/** The environment variable for library path setting */
	private final static String PATH = "PATH";

	/** The environment variable for library path setting */
	public final static String LIB_PATH_ENV = WebBrowserUtil.IS_OS_WINDOWS ? PATH
			: LD_LIBRARY_PATH;

	private static String browserPath = null;

	private static boolean nativeLibLoaded = false;

	private static boolean isDebugOn = false;

	/* native functions */
	private static native String nativeGetBrowserPath();

	private static native String nativeGetMozillaGreHome();

	private static native void nativeSetEnv();

	/** Loads the jdic library (unless it has already been loaded) */
	public static void loadLibrary() {
		if (!nativeLibLoaded) {
			
			try {
				JdicManager.getManager().initShareNative();
			} catch (JdicInitException e) {
				e.printStackTrace();
				WebBrowserUtil.error(e.getMessage());
			}
			AccessController.doPrivileged(new PrivilegedAction() {
				public Object run() {
					System.loadLibrary(JDIC_LIB_NAME);
					return null;
				}
			});
			nativeLibLoaded = true;
		}
	}

	/**
	 * Gets the native browser path.
	 * 
	 * @return the path of the default browser in the current system
	 */
	public static String getDefaultBrowserPath() {
		if (browserPath == null) {
			loadLibrary();
			browserPath = nativeGetBrowserPath();
		}
		return browserPath;
	}

	/**
	 * Gets the native Mozilla GRE home directory installed with a .exe package.
	 * 
	 * @return the GRE home directory of the currently installed Mozilla.
	 */
	public static String getMozillaGreHome() {
		loadLibrary();
		return nativeGetMozillaGreHome();
	}

	/**
	 * Used to check whether the current operating system matches a operating
	 * system name (osname).
	 * 
	 * @param osname
	 *            Name of an operating system we are looking for as being part
	 *            of the Sytem property os.name
	 * @return true, if osname matches the current operating system,false if not
	 *         and if osname is null
	 */
	public static boolean isCurrentOS(String osname) {
		if (osname == null) {
			return false;
		} else {
			return (OS_NAME.indexOf(osname) >= 0);
		}
	}

	/* debug helper */
	public static void trace(String msg) {
		if (isDebugOn)
			System.out.println("*** Jtrace: " + msg);
	}

	public static void error(String msg) {
		System.err.println("*** Error: " + msg);
	}

	/**
	 * Sets trace messages on or off. If on, the trace messages will be printed
	 * out in the console.
	 * 
	 * @param b
	 *            <code>true</code> if enable the trace messages; otherwise,
	 *            <code>false</code>.
	 */
	public static void enableDebugMessages(boolean b) {
		isDebugOn = b;
	}

	/**
	 * Get the boolean which indicates is debug on or off
	 */
	public static boolean getDebug() {
		return isDebugOn;
	}

	public static void nativeSetEnvironment() {
		loadLibrary();
		nativeSetEnv();
	}
}