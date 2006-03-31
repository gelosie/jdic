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

import org.jdesktop.jdic.init.JdicInitException;

/**
 * This class represents a wrapper around an embedded browser engine. It returns
 * information about the browser type being used, the availability of the
 * embedded browser engine on the current system.
 * 
 * @author Alexander Hars, Inventivio GmbH 
 * @author Michael Shan
 */
public interface IBrowserEngine {

	/**
	 * Returns the standardadized name of the embedded browser engine. The name
	 * identifies the browser type but not the browser version. This name should
	 * be the same for all browser engines that embed the same browser,
	 * independent of platform. Names should be such that they can be presented
	 * in an English user dialog for choosing their favorite embedded engine.
	 * They may have spaces but they may not be internationalized.
	 * 
	 * Examples for the standardized names are: Internet Explorer, Mozilla.
	 * 
	 * @return The standardized name of the embedded browser engine. May not be
	 *         null or empty.
	 */
	public String getBrowserName();


//	/**
//	 * Additional information about the specific type and version of embedded
//	 * browser component being used.
//	 * 
//	 * @return A string indicating the specific version of the browser or
//	 *         browser component which is being embedded, null or an empty
//	 *         string.
//	 */
//	public String getBrowserVersion();

	/**
	 * Checks whether the associated Engine is available on the current system.
	 * This is a prerequisite for creating <code>Browser</code> instances on
	 * the current system. This method should not yet reserve resources required
	 * for actually launching the browser: The BrowserManager may decide to
	 * invoke the browser window from a different engine.
	 * 
	 * @return true if an engine is available, false if no engine is available,
	 *         the engine is not found or can not be accessed.
	 */
	public boolean isEngineAvailable();

	/**
	 * Checks whether the default BrowserPath indicates that this browser is set
	 * as the default browser on the current system.
	 * 
	 * @param browserPath
	 *            taken from the OS or whereever
	 * @return true if this BrowserEngine represents the default browser, true
	 *         otherwise
	 */
	public boolean isDefaultBrowser(String browserPath);

	/**
	 *
	 * @return embeded name of current browser.
	 */
	public String getEmbeddedBinaryName();

	/**
	 * Set browser's full path(folder and exe name). When active brower isn't
	 * set as the default browser, will have to use this method to specify the
	 * path of it. It's often used after <code>BrowserEngineManger</code>'s
	 * setActiveEngine() method.
	 * 
	 */
	public void setEnginePath(String fullPath);

	/**
	 * Initialize engine.
	 * 
	 * @throws JdicInitException
	 */
	public void initialize() throws JdicInitException;

	/**
	 * 
	 * @return browser's default charset used to de/encode messages.
	 */
	public String getCharsetName();

	/**
	 *
	 * @return browser's file protocol prefix (eg. "file:/")
	 */
	public String getFileProtocolURLPrefix();

	/**
	 * check if the browser engine has been intialized. It needs only be
	 * initialized once.
	 * 
	 * @return if initialized return true else return false 
	 *
	 */
	public boolean isInitialized();

}
