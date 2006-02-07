/*
 * Created on 2006-1-20
 *
 */
package ut.bm;

import junit.framework.TestCase;

import org.jdesktop.jdic.browser.BrowserEngineManager;
import org.jdesktop.jdic.browser.IBrowserEngine;
import org.jdesktop.jdic.browser.InternetExplorerEngine;
import org.jdesktop.jdic.browser.MozillaEngine;
import org.jdesktop.jdic.browser.WebBrowser;
import org.jdesktop.jdic.browser.internal.WebBrowserUtil;
import org.jdesktop.jdic.init.JdicInitException;

/**
 * @author dongdong.yang
 *  
 */
public class BrowserEngineManagerTest extends TestCase {
	
	//Register an exiting engine
	public void testRegisterBrowserEngine() {
		BrowserEngineManager engineManager = BrowserEngineManager.instance();
		IBrowserEngine browserEngine = engineManager.getActiveEngine();
		if (browserEngine.getBrowserName().equals("Internet Explorer")) {
			String engineName = "Mozilla";
			IBrowserEngine engine = new MozillaEngine();
			WebBrowser.setDebug(true);
			engineManager.registerBrowserEngine(engineName, engine);
			WebBrowserUtil
					.trace("testRegisterBrowserEngine,Internet Explorer is the default BrowserEngine.");
		} else {
			String engineName = "Internet Explorer";
			IBrowserEngine engine = new InternetExplorerEngine();
			WebBrowser.setDebug(true);
			engineManager.registerBrowserEngine(engineName, engine);
			WebBrowserUtil
					.trace("testRegisterBrowserEngine,Mozilla is the default BrowserEngine.");
		}
	}

	//Remove an exiting engine
	public void testRemoveExitingBrowserEngine() {
		BrowserEngineManager engineManager = BrowserEngineManager.instance();
		IBrowserEngine browserEngine = engineManager.getActiveEngine();
		if (browserEngine.getBrowserName().equals("Internet Explorer")) {
			assertTrue(engineManager
					.removeBrowserEngine(BrowserEngineManager.MOZILLA));
			WebBrowserUtil
					.trace("testRemoveExitingBrowserEngine,Internet Explorer is the default BrowserEngine.");
		} else {
			assertTrue(engineManager
					.removeBrowserEngine(BrowserEngineManager.IE));
			WebBrowserUtil
					.trace("testRemoveExitingBrowserEngine,Mozilla is the default BrowserEngine.");
		}
	}

	//Remove an active engine
	public void testRemoveActiveBrowserEngine() {
		BrowserEngineManager engineManager = BrowserEngineManager.instance();
		IBrowserEngine browserEngine = engineManager.getActiveEngine();
		if (browserEngine.getBrowserName().equals("Internet Explorer")) {
			assertFalse(engineManager
					.removeBrowserEngine(BrowserEngineManager.IE));
			WebBrowserUtil
					.trace("testRemoveActiveBrowserEngine,Internet Explorer is the default BrowserEngine.");
		} else {
			assertFalse(engineManager
					.removeBrowserEngine(BrowserEngineManager.MOZILLA));
			WebBrowserUtil
					.trace("testRemoveExitingBrowserEngine,Mozilla is the default BrowserEngine.");
		}

	}

	//Set an active engine after engine is initialized
	public void testSetActiveEngine() {
		BrowserEngineManager engineManager = BrowserEngineManager.instance();
		IBrowserEngine browserEngine = engineManager.getActiveEngine();
		if (browserEngine.getBrowserName().equals("Internet Explorer")) {
			try {
				engineManager.getActiveEngine().initialize();
			} catch (JdicInitException e) {
				e.printStackTrace();
			}
			assertTrue(engineManager.setActiveEngine(BrowserEngineManager.IE));
		} else {
			try {
				engineManager.getActiveEngine().initialize();
			} catch (JdicInitException e) {
				e.printStackTrace();
			}
			assertTrue(engineManager
					.setActiveEngine(BrowserEngineManager.MOZILLA));
		}
	}

}