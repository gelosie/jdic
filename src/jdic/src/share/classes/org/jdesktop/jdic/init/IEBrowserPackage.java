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
import org.jdesktop.jdic.browser.WebBrowser;



/**
 *  Singleton class describing the IE browser integration libraries for JDIC
 *  initialization
 *
 * @author     Michael Samblanet
 * @created    August 2, 2004
 */
public class IEBrowserPackage extends Package {

	/**  Singleton instance of this class */
	private final static IEBrowserPackage sSingleton = new IEBrowserPackage();


	/**  Private constructor to prevent public construction */
	private IEBrowserPackage() {
		addPlatform(Platform.WINDOWS, new String[]{"IeEmbed.exe"});
	}

	/**
	 * @return    The singleton instance of this class
	 */
	public static IEBrowserPackage getBrowserPackage() {
		return sSingleton;
	}


	/**
	 *  {@inheritDoc}
	 *
	 * @return    The name value
	 */
	public String getName() {
		return "IEBrowser";
	}


	/**
	 *  Dynamically sets the path to the browser executable file. This is needed
	 *  because the class does not provide a way to provide a path to the binarary.
	 *  This could be removed if the class is ever modified to allow path setting.
	 *
	 * @exception  JdicInitException  Should never be thrown - used to wrap introspection exceptions
	 */
	public void postCopy() throws JdicInitException {
		try {
			Field bbf = WebBrowser.class.getDeclaredField("browserBinary");
			bbf.setAccessible(true);
			if (bbf.get(null) == null) {
				String bbPath = new File(JdicManager.getManager().getBinaryDir(), getFiles(JdicManager.getManager().getPlatform())[0]).toString();
				bbf.set(null, bbPath);
			}
		} catch (NoSuchFieldException e) {
			throw new JdicInitException(e);
		} catch (IllegalAccessException e) {
			throw new JdicInitException(e);
		}
	}
}

