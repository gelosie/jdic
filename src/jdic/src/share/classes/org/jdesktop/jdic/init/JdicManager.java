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
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

import com.sun.jnlp.JNLPClassLoader;

/**
 * Initialization manager for JDIC to set the environment variables or
 * initialize the set up for native libraries and executable files.
 * <p>
 * There are 3 modes of operation: WebStart, file system, and .jar file.
 * <p>
 * When using WebStart, please specify a .jar file(jdic-native.jar) with the
 * native libraries for your platform to be loaded by WebStart in your JNPL.
 * This class will find the unjared native libraries and executables, and use
 * them directly.
 * <p>
 * If not in WebStart, the system will expect the native libraries to be located
 * in directory at the root of the classpath or .jar containing this class.
 * 
 * @author Michael Samblanet
 * @author Paul Huang
 * @author George Zhang
 * @author Michael Shan
 * @since July 29, 2004
 */
public class JdicManager {
	private boolean isShareNativeInitialized = false;

	/** The path for the JDIC native files (jdic.dll/libjdic.so, etc) */
	String nativeLibPath = null;

	/** Singleton instance of this class */
	private static JdicManager sSingleton = null;

	/**
	 * Private constructor to prevent public construction.
	 */
	private JdicManager() {
	}

	/**
	 * Returns a singleton instance of <code>JdicManager</code>.
	 */
	public static synchronized JdicManager getManager() {
		if (sSingleton == null) {
			sSingleton = new JdicManager();
		}
		return sSingleton;
	}

	/**
	 * Initializes the shared native file settings for all the JDIC components/
	 * packages. Set necessary environment variables for the shared native
	 * library and executable files, including *.dll files on Windows, and *.so
	 * files on Unix.
	 * 
	 * @exception JdicInitException
	 *                Generic initialization exception
	 */
	public void initShareNative() throws JdicInitException {
		// If the shared native file setting was already initialized,
		// just return.
		if (isShareNativeInitialized) {
			return;
		}

		try {
			// Find the root path of this class.
			String jwsVersion = System.getProperty("javawebstart.version");
			if (jwsVersion != null) {
				//loaded by JWS
				WebBrowserUtil.trace("Loaded by JavaWebStart,version is "
						+ jwsVersion);
				//native libs will be loaded by webstart automatically
				nativeLibPath = caculateNativeLibPathBySunJWS();
				return;
			} else {				
				String runningURL = (new URL(JdicManager.class
						.getProtectionDomain().getCodeSource().getLocation(),
						".")).openConnection().getPermission().getName();//running url of current class
				String runningPath = (new File(runningURL)).getCanonicalPath();//running path of current class
				nativeLibPath = caculateNativeLibPath(runningPath);
				// Add the binary path (including jdic.dll or libjdic.so) to
				// "java.library.path", since we need to use the native methods
				// in class InitUtility.
				String newLibPath = nativeLibPath + File.pathSeparator
						+ System.getProperty("java.library.path");
				System.setProperty("java.library.path", newLibPath);
				Field fieldSysPath = ClassLoader.class
						.getDeclaredField("sys_paths");
				fieldSysPath.setAccessible(true);
				if (fieldSysPath != null) {
					fieldSysPath.set(System.class.getClassLoader(), null);
				}
			}
		} catch (Throwable e) {
			throw new JdicInitException(e);
		}
		isShareNativeInitialized = true;
	}

	/**
	 * To keep the using of crossplatform version of JDIC
	 * 
	 * @throws MalformedURLException
	 * @throws JdicInitException
	 *  
	 */
	private String caculateNativeLibPath(String runningPath)
			throws MalformedURLException, JdicInitException {

		String platformPath = runningPath + File.separator + getPlatform();
		File jdicStubJarFile = new File(platformPath + File.separator
				+ "jdic_stub.jar");
		if (!jdicStubJarFile.exists()) {
			//not cross platform version
			return runningPath;
		} else {
			//cross platform version
			String architecturePath = platformPath + File.separator
					+ getArchitecture();
			ClassLoader cl = getClass().getClassLoader();
			if (!(cl instanceof URLClassLoader)) {
				//not URLClassLoader,omit it,in case the stub jar has been
				// set to claspath
				String exceptionInfo = "We detect that you are not using java.net.URLClassLoader for cross platform versoin,you have to set jdic_stub.jar manually!";
				WebBrowserUtil.error(exceptionInfo);
				return architecturePath;//return the native lib path
			}
			//set stub jars to classpath
			URLClassLoader urlCl = (URLClassLoader) cl;
			try {
				Method addURLMethod = URLClassLoader.class.getDeclaredMethod(
						"addURL", new Class[] { URL.class });
				addURLMethod.setAccessible(true);
				addURLMethod.invoke(urlCl, new Object[] { jdicStubJarFile
						.toURL() });
				return architecturePath;//return the native lib path
			} catch (Throwable t) {
				t.printStackTrace();
				throw new JdicInitException(
						"Error, could not add URL to system classloader");
			}
		}
	}

	public String getBinaryPath() {
		System.out.print("native lib path "+nativeLibPath);
		return nativeLibPath;
	}

	/**
	 * Initialize native libs' running path if loaded by webstart.This method
	 * only works for sun webstart implementaion,for other webstart
	 * implementations, you have to rewrite this method.
	 * 
	 * @throws IOException
	 * @throws JdicInitException
	 */
	private String caculateNativeLibPathBySunJWS() throws IOException,
			JdicInitException {
		String jdicLibFolder = null;
		ClassLoader cl = this.getClass().getClassLoader();
		if (cl instanceof JNLPClassLoader) {
			JNLPClassLoader jnlpCl = (JNLPClassLoader) cl;
			String jdicLibURL = jnlpCl.findLibrary("jdic");//get lib path by classloder
			jdicLibFolder = (new File(jdicLibURL)).getParentFile().getCanonicalPath();
			WebBrowserUtil.trace("running path " + nativeLibPath);
			isShareNativeInitialized = true;
		} else {
			// only run well for sun jre
			throw new JdicInitException(

					"Unexpected ClassLoader for webstart, only com.sun.jnlp.JNLPClassLoader is supported.");
		}
		return jdicLibFolder;
	}

	/**
	 * Return the canonical name of the platform. This value is derived from the
	 * System property os.name.
	 * 
	 * @return The platform string.
	 */
	private static String getPlatform() {
		// See list of os names at: http://lopica.sourceforge.net/os.html
		// or at: http://www.tolstoy.com/samizdat/sysprops.html
		String osname = System.getProperty("os.name");
		if (osname.startsWith("Windows")) {
			return "windows";
		}

		return canonical(osname);
	}

	/**
	 * Return the name of the architecture. This value is determined by the
	 * System property os.arch.
	 * 
	 * @return The architecture string.
	 */
	private static String getArchitecture() {
		String arch = System.getProperty("os.arch");
		if (arch.endsWith("86")) {
			return "x86";
		}
		return canonical(arch);
	}

	/**
	 * @param value
	 *            The value to be canonicalized.
	 * @return The value with all '/', '\' and ' ' replaced with '_', and all
	 *         uppercase characters replaced with lower case equivalents.
	 */
	private static String canonical(String value) {
		WebBrowserUtil.trace("value:" + value);
		WebBrowserUtil.trace("canonical:"
				+ value.toLowerCase().replaceAll("[\\\\/ ]", "_"));
		return value.toLowerCase().replaceAll("[\\\\/ ]", "_");
	}

}
