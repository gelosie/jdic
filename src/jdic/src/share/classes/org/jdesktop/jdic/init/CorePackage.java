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
import java.lang.reflect.Field;


/**
 *  Singleton class describing the core libraries for JDIC initialization
 *
 * @author     Michael Samblanet
 * @created    August 2, 2004
 */
public class CorePackage extends Package {

	/**  Singleton instance of this class */
	private final static CorePackage sSingleton = new CorePackage();


	/**  Private constructor to prevent public construction */
	private CorePackage() {
		addPlatform(Platform.WINDOWS, new String[]{"jdic.dll", "nspr4.dll"});
		addPlatform(Platform.LINUX, new String[]{"libjdic.so"});
		addPlatform(Platform.SOLARIS, new String[]{"libjdic.so"});
	}


	/**
	 * @return    The singleton instance of this class
	 */
	public static CorePackage getCorePackage() {
		return sSingleton;
	}


	/**
	 *  {@inheritDoc}
	 */
	public String getName() {
		return "Core JDIC";
	}


	/**  Dynamically sets the library path of Java to inlcude the location of the JDIC core
	     files.  Basically force Java to think it has not initialized and update the
		 library path.  This is a mega-hack that could be removed in the future if 
		 JDIC would allow us to specify where the libraries are located
	*/
	public void postCopy() throws JdicInitException {
		String newLibPath = System.getProperty("java.library.path") + File.pathSeparator + JdicManager.getManager().getBinaryDir();
		System.setProperty("java.library.path", newLibPath);
		try {
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			if (fieldSysPath != null) {
				fieldSysPath.set(System.class.getClassLoader(), null);
			}
		} catch (NoSuchFieldException e) {
			throw new JdicInitException(e);
		} catch (IllegalAccessException e) {
			throw new JdicInitException(e);
		}
	}
}

