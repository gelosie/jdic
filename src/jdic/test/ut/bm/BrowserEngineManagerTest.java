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

	// Register an exiting engine
	public void testRegisterBrowserEngine() {
		try {
			BrowserEngineManager engineManager = BrowserEngineManager
					.instance();
			IBrowserEngine browserEngine = engineManager.getActiveEngine();
			if (browserEngine.getBrowserName().equals(BrowserEngineManager.IE)) {
				String engineName = BrowserEngineManager.MOZILLA;
				IBrowserEngine engine = new MozillaEngine();
				WebBrowser.setDebug(true);
				engineManager.registerBrowserEngine(engineName, engine);
				WebBrowserUtil
						.trace("testRegisterBrowserEngine,Internet Explorer is the default BrowserEngine.");
			} else {
				String engineName = BrowserEngineManager.IE;
				IBrowserEngine engine = new InternetExplorerEngine();
				WebBrowser.setDebug(true);
				engineManager.registerBrowserEngine(engineName, engine);
				WebBrowserUtil
						.trace("testRegisterBrowserEngine,Mozilla is the default BrowserEngine.");
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	// Remove an exiting engine
	public void testRemoveExitingBrowserEngine() {
		try {
			BrowserEngineManager engineManager = BrowserEngineManager
					.instance();
			IBrowserEngine browserEngine = engineManager.getActiveEngine();
			if (browserEngine.getBrowserName().equals(BrowserEngineManager.IE)) {
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
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	// Remove an active engine
	public void testRemoveActiveBrowserEngine() {
		try {
			BrowserEngineManager engineManager = BrowserEngineManager
					.instance();
			IBrowserEngine browserEngine = engineManager.getActiveEngine();
			if (browserEngine.getBrowserName().equals(BrowserEngineManager.IE)) {
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
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

	// Set an active engine after engine is initialized
	public void testSetActiveEngine() {
		try {
			BrowserEngineManager engineManager = BrowserEngineManager
					.instance();
			IBrowserEngine browserEngine = engineManager.getActiveEngine();
			if (browserEngine.getBrowserName().equals(BrowserEngineManager.IE)) {
				try {
					engineManager.getActiveEngine().initialize();
				} catch (JdicInitException e) {
					e.printStackTrace();
				}
				assertEquals(engineManager
						.setActiveEngine(BrowserEngineManager.IE),
						engineManager.getActiveEngine());
			} else {
				try {
					engineManager.getActiveEngine().initialize();
				} catch (JdicInitException e) {
					e.printStackTrace();
					fail(e.getMessage());
				}
				assertEquals(engineManager
						.setActiveEngine(BrowserEngineManager.IE),
						engineManager.getActiveEngine());
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.getMessage());
		}
	}

}