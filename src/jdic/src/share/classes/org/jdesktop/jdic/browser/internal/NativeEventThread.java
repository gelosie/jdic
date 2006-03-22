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

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Vector;

import javax.swing.SwingUtilities;

import org.jdesktop.jdic.browser.IWebBrowser;
import org.jdesktop.jdic.browser.IBrowserEngine;
import org.jdesktop.jdic.browser.WebBrowserEvent;
import org.jdesktop.jdic.browser.BrowserEngineManager;
import org.jdesktop.jdic.init.JdicInitException;
import org.jdesktop.jdic.init.JdicManager;

/**
 * An internal class for dealing with the communication between WebBrowser &
 * native browser.
 * 
 * @see IWebBrowser
 * 
 * @author Kyle Yuan
 * @version 0.1, 03/07/17
 */
public class NativeEventThread extends Thread {
	private Vector webBrowsers = new Vector();

	// Event queue for events sent from Java to the native browser.
	private Vector nativeEvents = new Vector();

	private Process nativeBrowser;

	private boolean eventRetBool;

	private String eventRetString;

	private MsgClient messenger = null;

	private IBrowserEngine engine = null;

	public NativeEventThread() {
		super("EventThread");
		WebBrowserUtil.trace("Envent Thread new once!");
	}

	public void attachWebBrowser(IWebBrowser webBrowser) {
		int instanceNum = webBrowser.getInstanceNum();
		if (instanceNum >= webBrowsers.size()) {
			webBrowsers.setSize(instanceNum + 1);
		}
		webBrowsers.set(instanceNum, webBrowser);
	}

	public void run() {
		WebBrowserUtil.trace("Envent Thread run once!");
		// We can only decide which engine to use here! Shouldn't access
		// engine's info before this method!
		try {
			messenger = new MsgClient();
			engine = BrowserEngineManager.instance().getActiveEngine();
			try {
				engine.initialize();
			} catch (JdicInitException e) {
				e.printStackTrace();
				return;
			}

			if (engine.getEmbeddedBinaryName() == null) {
				setBrowsersInitFailReason("The embedded browser binary is "
						+ "not set.");
				WebBrowserUtil.error("The embedded browser binary is not set, "
						+ "system exit.");
				return;
			}
			String jvmVendor = System.getProperty("java.vm.vendor");
			if (engine.getEmbeddedBinaryName().endsWith("IeEmbed.exe")
					&& jvmVendor.startsWith("Sun"))
				// ie and sun jvm
				WebBrowserUtil.nativeSetEnvironment();

			// start native browser
			String filepath = JdicManager.getManager().getBinaryPath()
					+ File.separator + engine.getEmbeddedBinaryName();
			final String cmd = (new File(filepath).exists()) ? filepath
					: engine.getEmbeddedBinaryName();
			WebBrowserUtil.trace("Executing " + cmd + " -port="
					+ messenger.getPort());

			AccessController.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws IOException {
					nativeBrowser = Runtime.getRuntime()
							.exec(
									new String[] { cmd,
											"-port=" + messenger.getPort() });
					new StreamGobbler(nativeBrowser.getErrorStream()).start();
					new StreamGobbler(nativeBrowser.getInputStream()).start();
					return null;
				}
			});
		} catch (PrivilegedActionException e) {
			setBrowsersInitFailReason("Can't find the native embedded browser.");
			System.out.println("Can't execute the native embedded browser. "
					+ "Error message: " + e.getCause().getMessage());
			return;
		}

		// create socket client and connect to socket server
		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws Exception {
					messenger.connect();
					return null;
				}
			});
		} catch (PrivilegedActionException e) {
			System.out.println("Can't connect to the native embedded "
					+ "browser. Error message: " + e.getCause().getMessage());
			setBrowsersInitFailReason("Can't connect to the native embedded "
					+ "browser.");
			return;
		}

		// main event loop
		while (true) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {
			}

			try {
				int exitValue = nativeBrowser.exitValue();
				WebBrowserUtil.trace("Native embedded browser died.");
				return;
			} catch (IllegalThreadStateException e) {
			}

			try {
				processEventsFromJava();
			} catch (Exception e) {
				WebBrowserUtil.trace("Exception occured when processEvent: "
						+ e.getMessage());
				return;
			}

			try {
				messenger.portListening();
				processMessageFromNative(messenger.getMessage());
			} catch (Exception e) {
				WebBrowserUtil.trace("Exception occured when portListening: "
						+ e.getMessage());
				return;
			}
		}
	}

	private IWebBrowser getWebBrowserFromInstance(int instance) {
		try {
			return (IWebBrowser) webBrowsers.get(instance);
		} catch (Exception e) {
			return null;
		}
	}

	private void notifyWebBrowser(int instance) {
		IWebBrowser browser = getWebBrowserFromInstance(instance);
		if (null != browser) {
			synchronized (browser) {
				browser.notify();
			}
		}
	}

	/*
	 * Processes events sent from Java to the native browser.
	 */
	private void processEventsFromJava() {
		int size = nativeEvents.size();
		for (int i = 0; i < size; ++i) {
			NativeEventData nativeEvent = (NativeEventData) nativeEvents.get(i);
			if (processEventFromJava(nativeEvent)) {
				nativeEvents.removeElementAt(i);
				break;
			}
		}
	}

	private boolean processEventFromJava(NativeEventData nativeEvent) {
		IWebBrowser browser = getWebBrowserFromInstance(nativeEvent.instance);
		if (null == browser) {
			return true;
		}

		if (!browser.isInitialized()
				&& (nativeEvent.type != NativeEventData.EVENT_INIT && nativeEvent.type != NativeEventData.EVENT_CREATEWINDOW)) {
			return false;
		}

		WebBrowserUtil.trace("Process event to native browser: "
				+ nativeEvent.instance + ", " + nativeEvent.type + ", ");

		String msg = nativeEvent.instance + "," + nativeEvent.type + ",";
		switch (nativeEvent.type) {
		case NativeEventData.EVENT_INIT:
		case NativeEventData.EVENT_DESTROYWINDOW:
		case NativeEventData.EVENT_GOBACK:
		case NativeEventData.EVENT_GOFORWARD:
		case NativeEventData.EVENT_REFRESH:
		case NativeEventData.EVENT_STOP:
		case NativeEventData.EVENT_GETURL:
		case NativeEventData.EVENT_FOCUSGAINED:
		case NativeEventData.EVENT_FOCUSLOST:
		case NativeEventData.EVENT_GETCONTENT:
			messenger.sendMessage(msg);
			break;
		case NativeEventData.EVENT_SHUTDOWN:
			messenger.sendMessage(msg);
			break;
		case NativeEventData.EVENT_CREATEWINDOW:
			int nativeWindow = browser.getNativeWindow();
			if (0 == nativeWindow) {
				WebBrowserUtil
						.trace("Can't get the JAWT native window handler.");
			} else {
				msg += nativeWindow;
				messenger.sendMessage(msg);
			}
			break;
		case NativeEventData.EVENT_SET_BOUNDS:
			msg += nativeEvent.rectValue.x + "," + nativeEvent.rectValue.y
					+ "," + nativeEvent.rectValue.width + ","
					+ nativeEvent.rectValue.height;
			messenger.sendMessage(msg);
			break;
		case NativeEventData.EVENT_NAVIGATE:
		case NativeEventData.EVENT_NAVIGATE_POST:
		case NativeEventData.EVENT_SETCONTENT:
		case NativeEventData.EVENT_EXECUTESCRIPT:
			msg += nativeEvent.stringValue;
			messenger.sendMessage(msg);
			break;
		}

		return true;
	}

	public static NativeEventData parseMessageString(String msg) {
		if (null == msg || 0 == msg.length()) {
			return null;
		}

		int eventType = -1;
		String stringValue = null;

		int pos1 = msg.indexOf(",", 0);
		int instance = Integer.parseInt(msg.substring(0, pos1));
		int pos2 = msg.indexOf(",", pos1 + 1);
		if (pos2 < 0) {
			eventType = Integer.parseInt(msg.substring(pos1 + 1));
		} else {
			eventType = Integer.parseInt(msg.substring(pos1 + 1, pos2));
			if (pos2 + 1 < msg.length())
				stringValue = msg.substring(pos2 + 1);
		}

		return new NativeEventData(instance, eventType, stringValue);
	}

	/*
	 * Process an event received from the native browser
	 */
	private void processMessageFromNative(String msg) {
		NativeEventData eventData = parseMessageString(msg);
		if (eventData == null)
			return;

		WebBrowserUtil.trace("Process event from native browser: "
				+ eventData.instance + ", " + eventData.type + ", "
				+ eventData.stringValue);

		if (WebBrowserEvent.WEBBROWSER_INIT_FAILED == eventData.type) {
			setBrowsersInitFailReason(eventData.stringValue);
			WebBrowserUtil.error(eventData.stringValue);
			return;
		}

		if (eventData.instance < 0) {
			return;
		}
		
		//anonymous inner class can only access final local variable
		final IWebBrowser browser = getWebBrowserFromInstance(eventData.instance);
		if (null == browser) {
			return;
		}

		if (WebBrowserEvent.WEBBROWSER_DOCUMENT_COMPLETED == eventData.type) {
			if (browser.isSynchronize()) {
				//if works under sync model, will not call the listener's downloadCompleted event
				notifyWebBrowser(eventData.instance);
				return;
			}
		}
		
		if (WebBrowserEvent.WEBBROWSER_RETURN_URL == eventData.type
				|| WebBrowserEvent.WEBBROWSER_GETCONTENT == eventData.type
				|| WebBrowserEvent.WEBBROWSER_EXECUTESCRIPT == eventData.type
				|| WebBrowserEvent.WEBBROWSER_DESTROYWINDOW_SUCC == eventData.type) {
			eventRetString = eventData.stringValue;
			notifyWebBrowser(eventData.instance);
			return;
		}

		if (WebBrowserEvent.WEBBROWSER_INIT_WINDOW_SUCC == eventData.type) {
			browser.setInitialized(true);
			browser.setInitFailureMessage("");
		}

		final WebBrowserEvent event = new WebBrowserEvent(browser,
				eventData.type, eventData.stringValue);

		// For thread-safety reason, invokes the dispatchWebBrowserEvent method
		// of IWebBrowser.
		Runnable dispatchEvent = new Runnable() {
			public void run() {
				browser.dispatchWebBrowserEvent(event);
			}
		};

		try {
			SwingUtilities.invokeLater(dispatchEvent);
		} catch (Exception e) {
			WebBrowserUtil.trace("Exception occured when invokeLater. "
					+ "Error message: " + e.getMessage());
		}
	}

	public synchronized void fireNativeEvent(int instance, int type) {
		nativeEvents.addElement(new NativeEventData(instance, type));
	}

	public synchronized void fireNativeEvent(int instance, int type,
			Rectangle rectValue) {
		nativeEvents.addElement(new NativeEventData(instance, type, rectValue));
	}

	public synchronized void fireNativeEvent(int instance, int type,
			String stringValue) {
		nativeEvents
				.addElement(new NativeEventData(instance, type, stringValue));
	}

	public void setBrowsersInitFailReason(String msg) {
		((IWebBrowser) webBrowsers.elementAt(0)).setInitFailureMessage(msg);
	}

	class StreamGobbler extends Thread {
		InputStream is;

		StreamGobbler(InputStream is) {
			this.is = is;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println("+++ Ctrace: " + line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return Returns the eventRetString.
	 */
	public String getEventRetString() {
		return eventRetString;
	}

	/**
	 * @return Returns the messenger.
	 */
	public MsgClient getMessenger() {
		return messenger;
	}
} // end of class NativeEventThread
