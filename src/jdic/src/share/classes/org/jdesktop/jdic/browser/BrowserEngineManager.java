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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;
import org.jdesktop.jdic.init.JdicInitException;

/**
 * Factory pattern used to maitain instances of <code>IBrowserEngine</code>.
 * 
 * @author Alexander Hars,Inventio GmbH Michael Shan
 * 
 */
public class BrowserEngineManager {

	/** configuable through this */
	private static final String ORG_JDESKTOP_JDIC_BROWSER_BROWSERMANAGER = "org.jdesktop.jdic.browser.BrowserManager";

	/**
	 * Singleton instance. Protected so that subclasses can update the instance
	 * field in their constructor.
	 */
	private static BrowserEngineManager managerInstance = null;

	public static String MOZILLA = "Mozilla";

	public static String IE = "Internet Explorer";

	/** The set of BrowserConnectors */
	private static Map engines = new HashMap();

	/** The currently active BrowserEngine */
	private static IBrowserEngine activeEngine = null;

	/**
	 * Creates a new instance of BrowserEngineManager.
	 * 
	 * @exception JdicInitException
	 *                Generic initialization exception
	 */

	private BrowserEngineManager() {
		initExitingEngines();
	}

	/**
	 * Registers the engines that are available by default.
	 */
	protected void initExitingEngines() {
		registerBrowserEngine(IE, new InternetExplorerEngine());
		registerBrowserEngine(MOZILLA, new MozillaEngine());
	}

	/**
	 * Returns the singleton instance of the currently active BrowserManager.
	 * Creates a new instance if necessary. The class to be used for the new
	 * instance is determined by the System property:
	 * org.jdesktop.jdic.BrowserManager. (That class must be a subclass of
	 * BrowserManager) If that property does not exist, or an error occurs when
	 * trying to create a new instance of the class specified in the property,
	 * then the BrowserManager returned will be of class BrowserManager.
	 * 
	 * @return Singleton BrowserEngineManager instance
	 */
	public static synchronized BrowserEngineManager instance() {
		if (managerInstance == null) {
			String cname = System
					.getProperty(ORG_JDESKTOP_JDIC_BROWSER_BROWSERMANAGER);
			WebBrowserUtil.trace("Using browserManager " + cname);
			if (cname != null) {
				try {
					managerInstance = (BrowserEngineManager) Class.forName(
							cname).newInstance();
				} catch (ClassCastException ex) {
					ex.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			if (managerInstance == null) {
				// use default
				WebBrowserUtil.trace("Default browserManager is used.");
				managerInstance = new BrowserEngineManager();
			}
		}
		return managerInstance;
	}

	/**
	 * Used to find out which BrowserEngines have been registered AND are
	 * available on the current system.
	 * 
	 * @return Map of <code>BrowserEngine</code> instances representing all
	 *         BrowserEngines that are available. Never returns null.
	 */
	public Map getEngines() {
		return engines;
	}

	/**
	 * Adds a BrowserEngine to the list of BrowserConnectors that
	 * <code>BrowserEngineManager</code> uses to create new Browser instances.
	 * 
	 * @param BrowserEngine
	 *            The BrowserEngine to be registered.
	 * @return true if this engine was registered or was already registered;
	 *         false if it is null or if the engine is not available
	 */
	public boolean registerBrowserEngine(String engineName,
			IBrowserEngine engine) {
		if (engines.containsKey(engineName)) {
			WebBrowserUtil.trace("Engine " + engineName
					+ " has been registered.");
			return true;
		}
		if (engine == null) {
			WebBrowserUtil.error("Engine " + engineName + " is null.");
			return false;
		}
		if (checkEnginesWhenAdded()) {
			// check it
			if (engine.isEngineAvailable()) {
				engines.put(engineName, engine);
				return true;
			}
		} else {
			// put it directlly
			engines.put(engineName, engine);
			return true;
		}
		return false;
	}

	/**
	 * Removes a BrowserEngine to the list of BrowserConnectors that
	 * <code>BrowserEngineManager</code> uses to create new Browser instances.
	 * 
	 * @param BrowserEngine
	 *            The BrowserEngine to be removed.
	 * @return True .False if this is the active engine
	 */
	public boolean removeBrowserEngine(String engineName) {
		if (!engines.containsKey(engineName))
			return true;
		IBrowserEngine engine = (IBrowserEngine) engines.get(engineName);

		if (engine == activeEngine) {
			WebBrowserUtil.error("Can't remove active engine!");
			return false;
		}
		WebBrowserUtil.trace("Engine " + engineName + " will be removed.");
		return (engines.remove(engineName) == null ? false : true);
	}

	/**
	 * This code iterates over the currently registered BrowserEngines and
	 * decides which Engine will be used for instantiating <code>Browser</code>
	 * This method is only called once.
	 * 
	 * @return
	 * @throws JdicInitException
	 */
	protected void selectEngine() throws JdicInitException {
		if (activeEngine != null) {
			WebBrowserUtil.trace("Engine " + activeEngine.getBrowserName()
					+ " is active,won't select.");
			return;
		}

		String defaultBrowserpath = WebBrowserUtil.getDefaultBrowserPath();
		if (null == defaultBrowserpath) {
			WebBrowserUtil
					.trace("No defaultBrowser is detected! You must specify a browser engine by setActiveEngine()!");
			return;
		}

		Iterator engineNames = engines.keySet().iterator();
		while (engineNames.hasNext()) {
			String engineName = (String) engineNames.next();
			IBrowserEngine engine = (IBrowserEngine) engines.get(engineName);
			if (engine.isDefaultBrowser(defaultBrowserpath)
					&& engine.isEngineAvailable()) {
				activeEngine = engine;
				break;
			}
		}

		if (activeEngine == null) {
			WebBrowserUtil
					.trace("No browser is selected as active,you must specify one by setActiveEngine()!");
		}
	}

	/**
	 * This method is called when the BrowserManager instance is created. If it
	 * returns true, BrowserManger automatically checks the availability of all
	 * known engines and removes those engines that would not be able to provide
	 * a browser component. If this method returns false, no check is provided
	 * at startup. The getEngines() method then returns the full set of known
	 * engines. In that case, only when the methods getActiveBrowserEngine or
	 * getBrowser() are called the first time, will the list of engines be
	 * pruned to eliminate all engines that are not available. This method must
	 * consistently return the same value.
	 */
	protected boolean checkEnginesWhenAdded() {
		return true;
	}

	/**
	 * Set an active engine throgh enginename.TODO:if mozilla isn't the system
	 * default under win, can't set it as the active engine.
	 * 
	 * @return true if the activeEngine is set successfully
	 */
	public boolean setActiveEngine(String engineName) {
		if (activeEngine != null) {
			if (activeEngine.isInitialized()) {
				WebBrowserUtil.error("Engine " + activeEngine.getBrowserName()
						+ "has been initialized,can't change it anymore!");
			}
		}

		if (!engines.containsKey(engineName)) {
			WebBrowserUtil.error("Can't find engine " + engineName);
			return false;
		}

		IBrowserEngine engine = (IBrowserEngine) engines.get(engineName);
		if (engine == activeEngine) {
			return true;
		}

		if (!engine.isEngineAvailable()) {
			WebBrowserUtil.trace("Engine " + engineName + " isn't available.");
			return false;
		}

		activeEngine = engine;
		WebBrowserUtil.trace("Engine " + engineName
				+ " is set as activeEngine.");
		return true;
	}

	/**
	 * Get the active engine.Use the engine set by <code>setActiveEngine</code>else
	 * use the system default engine.
	 * 
	 * @return Returns the activeEngine.
	 * @throws JdicInitException
	 */
	public IBrowserEngine getActiveEngine() {
		if (activeEngine == null) {
			try {
				selectEngine();
			} catch (JdicInitException e) {
				WebBrowserUtil.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return activeEngine;
	}
}