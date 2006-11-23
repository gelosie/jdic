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

import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Vector;
import java.net.URL;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.EventListenerList;

import org.jdesktop.jdic.browser.internal.NativeEventThread;
import org.jdesktop.jdic.browser.internal.WebBrowserUtil;
import org.jdesktop.jdic.init.JdicInitException;
import org.jdesktop.jdic.init.JdicManager;

import com.apple.eawt.CocoaComponent;


/**
 * @author Dmitry Markman
 * @author Christopher Atlan
 */
public class WebKitWebBrowser extends CocoaComponent implements IWebBrowser {

	public static final int storeJavaObject         = 1;
	public static final int loadURL			= 2;
	public static final int resizeFrame		= 3;
        public static final int stopLoading             = 4;
        public static final int goBack                  = 5;
        public static final int goForward               = 6;
        public static final int updateCursor            = 7;
        public static final int runJS                   = 8;
        public static final int textLarger              = 9;
        public static final int textSmaller             = 10;
        public static final int search                  = 11;
        public static final int dispose                 = 12;
        public static final int ordercallback           = 13;
        public static final int loadURLFromString       = 14;
        public static final int reload                  = 15;
        
        public static final int INIT_WIDTH              = 500;
        public static final int INIT_HEIGHT             = 420;

        
        public static final int WEB_VIEW_POLICY_NONE                = 0;
        public static final int WEB_VIEW_POLICY_IN_OLD_WINDOW       = 1;
        public static final int WEB_VIEW_POLICY_IN_NEW_WINDOW       = 2;
        
        
        protected boolean needResize = false;
    java.net.URL  lastURL = null;
    private String HTMLSource = null;
	public native int createNSView1();
	int nsObject = 0;

	protected int newWebViewCreatingPolicy = WEB_VIEW_POLICY_IN_OLD_WINDOW;
	
    /** Listeners. */
    protected EventListenerList listenerList = new EventListenerList();

	private java.net.URL url;

	private boolean backEnabled;

	private boolean forwardEnabled;

	private static String failReason;

	private static IBrowserEngine engine = new WebKitEngine();

	/**default is async model*/
	private boolean synchronize = false;

	private String initFailureMessage = "WebBrowser is not initialized.";

	// if the WebBrowser successfully initialized
	private boolean isInitialized = false;

	static {
	   WebBrowserUtil.loadLibrary();
	}

	public WebKitWebBrowser() {
		super();
		BrowserEngineManager.instance().setActiveEngine(
				BrowserEngineManager.WEBKIT);//set it as the default engine
	}

	public int getNewWebViewCreatingPolicy() {
		return newWebViewCreatingPolicy;
	}
	
	public void setNewWebViewCreatingPolicy(int newWebViewCreatingPolicy) {
		this.newWebViewCreatingPolicy = newWebViewCreatingPolicy;
		if (this.newWebViewCreatingPolicy < WEB_VIEW_POLICY_NONE
				|| this.newWebViewCreatingPolicy > WEB_VIEW_POLICY_IN_NEW_WINDOW) {
			this.newWebViewCreatingPolicy = WEB_VIEW_POLICY_IN_OLD_WINDOW;
		}
	}

	public void addNotify() {
		super.addNotify();
		sendMsg(WebKitWebBrowser.storeJavaObject, this);
		//        synchronized(notifyObject){
		//            notifyObject.notifyAll();
		//        }
		//        System.out.println("notifyAll "+Thread.currentThread());
		//    	addMouseListener(new MouseAdapter(){
		//           public void mouseReleased(MouseEvent e) {
		//				requestFocusInWindow();
		//            }
		//    	});

		if (url != null)
			sendMsg(loadURL, url.toString());
	}

	public void refresh() {
		sendMsg(resizeFrame, this);

		Container c = getParent();
		if (c != null)
			c.repaint();
		if (needResize) {
			Dimension cd = getSize();
			//System.out.println("cd "+cd);
			cd.width++;
			cd.height++;
			setSize(cd);
			cd.width--;
			cd.height--;
			setSize(cd);
			needResize = false;
		}

	}

	public void sendMsg(int messageID) {
		// send Message only if webkit has init
		if (nsObject != 0)
			sendMessage(messageID, null);
	}

	public void sendMsg(int messageID, Object message) {
		// send Message only if webkit has init
		if (nsObject != 0)
			sendMessage(messageID, message);
	}

	/**
	 * Executes specified JavaScript code in a currently opened document. This
	 * should not be called until after a documentComplete event is fired in
	 * <code>WebBrowserListener</code>.
	 * 
	 * @return the result of JavaScript execution, if there is any.
	 */
	public String executeScript(final String javaScript) {
		StringBuffer sb = new StringBuffer();
		Object[] args = { javaScript, sb };
		sendMsg(runJS, args);
		return sb.toString();
	}

	public void reload() {
		sendMsg(reload);
	}

	public void makeTextLarger() {
		sendMsg(textLarger);
	}

	public void makeTextSmaller() {
		sendMsg(textSmaller);
	}

	protected void reRenderDocument() {//workaround!
		makeTextLarger();
		makeTextSmaller();
	}

	public void validate() {
		super.validate();
		//        reRenderDocument();
	}

	public void dispose() {
		sendMsg(dispose);
	}

	protected void orderCallback() {
		sendMsg(ordercallback);
	}

	protected void callback(Runnable runnable) {
		if (runnable != null)
			runnable.run();
	}

	/**
	 * @return true if you need to pop-up Alert dialog otherwise false
	 */
	public boolean runJavaScriptAlertPanelWithMessage(String message) {
		System.out
				.println("Java runJavaScriptAlertPanelWithMessage " + message);
		return true;
	}

	/**
	 * Returns the HTML content of a document, opened in a browser.
	 * 
	 * @return the HTML content of a document, opened in a browser.
	 */
	public String getContent() {
		return HTMLSource;
	}

	private void clearHTMLSource() {
		HTMLSource = null;
	}

	public void setHTMLSource(final String source) {
		clearHTMLSource();
		if (source != null)
			HTMLSource = new String(source);
	}

	/**
	 * Sets new HTML content.
	 * 
	 * @param htmlContent
	 *            the HTML content to set.
	 */
	public void setContent(final String htmlContent) {
		clearHTMLSource();
		if (htmlContent != null)
			HTMLSource = new String(htmlContent);
	}

	public void invokeFromWebKitThread(Runnable runnable) {
		sendMsg(ordercallback, runnable);
	}

	protected void finalize() throws Throwable {
		dispose();
		super.finalize();
	}

	/**
	 * Starts the search from the current selection. Will search across all
	 * frames. method highlights the string if it is found.
	 * 
	 * @param stringToFind
	 *            <code>String</code> The string to search for.
	 * @param forward
	 *            <code>boolean</code>.<code>true</code> to search
	 *            forward, <code>false</code> to seach backwards.
	 * @param caseFlag
	 *            <code>boolean</code>.<code>true</code> to for
	 *            case-sensitive search, <br>
	 *            <code>false</code> for case-insensitive search.
	 * @param wrap
	 *            <code>boolean</code>.
	 * @return <code>true</code> if found, <code>false</code> if not found.
	 */

	public boolean searchForString(String stringToFind, boolean forward,
			boolean caseFlag, boolean wrap) {
		Object[] args = new Object[2];
		args[0] = stringToFind;
		args[1] = new boolean[4];
		((boolean[]) args[1])[0] = forward;
		((boolean[]) args[1])[1] = caseFlag;
		((boolean[]) args[1])[2] = wrap;
		((boolean[]) args[1])[3] = false;
		sendMsg(search, args);
		return ((boolean[]) args[1])[3];
	}

	//callback
	public void finishURLLoading() {
		refresh();
		
		fireWebBrowserEvent(this,
				WebBrowserEvent.WEBBROWSER_DOWNLOAD_COMPLETED, null);
	}

	//callback
	public void startURLLoading(String urlStr) {
		if (urlStr == null)
			return;
		clearHTMLSource();
		url = null;
		try {
			url = new java.net.URL(urlStr);
		} catch (Throwable t) {
			url = null;
		}
		
		fireWebBrowserEvent(this,
				WebBrowserEvent.WEBBROWSER_DOWNLOAD_STARTED, null);
	}

	//callback
	public void mouseDidMoveOverElement(Object elementInfoObject) {
	}

	public int createNSView() {
		nsObject = createNSView1();
		return nsObject;
	}
	
	public int getNativeWindow() {
		return nsObject;
	}

    public int getInstanceNum() {
        return 0;
    }
	
	public void setInitialized(boolean b) {
		isInitialized = b;
	}
	
	public boolean isInitialized() {
		return isInitialized;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jdesktop.jdic.browser.IWebBrowser#getInitFailureMessage()
	 */
	public String getInitFailureMessage() {
		return initFailureMessage;
	}

	public void setInitFailureMessage(String msg) {
		initFailureMessage = msg;
	}
	
	/**
	 * @return Returns the synchronize.
	 */
	public boolean isSynchronize() {
		return synchronize;
	}
	

	/**
	 * @return Returns the failReason.
	 */
	public static String getFailReason() {
		return failReason;
	}

	/**
	 * @param failReason
	 *            The failReason to set.
	 */
	public static void setFailReason(String failReason) {
		WebKitWebBrowser.failReason = failReason;
	}

	/**
	 * @return Returns the backEnabled.
	 */
	public boolean isBackEnabled() {
		return backEnabled;
	}

	/**
	 * @param backEnabled
	 *            The backEnabled to set.
	 */
	public void setBackEnabled(boolean backEnabled) {
		this.backEnabled = backEnabled;
	}

	/**
	 * @return Returns the forwardEnabled.
	 */
	public boolean isForwardEnabled() {
		return forwardEnabled;
	}

	/**
	 * @param forwardEnabled
	 *            The forwardEnabled to set.
	 */
	public void setForwardEnabled(boolean forwardEnabled) {
		this.forwardEnabled = forwardEnabled;
	}

	/*
	 * Dispatches a WebBrowserEvent to the Java embeddor, called by
	 * NativeEventThread.processMessageFromNative.
	 */
	public void dispatchWebBrowserEvent(WebBrowserEvent e) {
	}

	public Dimension getMaximumSize() {
		return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
	}

	public Dimension getMinimumSize() {
		return new Dimension(160, 90);
	}

	public Dimension getPreferredSize() {
		return new Dimension(480, 270);
	}

	//callback
	public void setURLTitle(String title) {
        fireWebBrowserEvent(this,
				WebBrowserEvent.WEBBROWSER_TITLE_CHANGE, title);
	}

	//callback
	public void setURLIcon(java.nio.ByteBuffer buffer) {

	}

	//callback
	public void setBackButtonEnable(boolean v) {
		this.backEnabled = v;
	}

	//callback
	public void setForwardButtonEnable(boolean v) {
		this.forwardEnabled = v;
	}

	//callback
	public void errorOccurred(String errorDesc, String urlString) {
		this.failReason = errorDesc;
		
		fireWebBrowserEvent(this,
				WebBrowserEvent.WEBBROWSER_DOWNLOAD_ERROR, errorDesc);
	}

	public URL getURL() {
		return url;
	}

	public void setURL() {
		this.setURL(null, null);
	}

	public void setURL(final URL url) {
		this.setURL(url, null);
	}

	public void setURL(final URL url, final String postData) {
		this.url = url;
		if (url != null)
			sendMsg(loadURL, url.toString());
		else
			sendMsg(loadURL, "about:blank");
	}
	
	public void setURL(URL url, String postData, String headers){		
		throw new UnsupportedOperationException("Method is not supported");
	}

	public void back() {
		sendMsg(goBack);
	}

	public void forward() {

		sendMsg(goForward);

	}

	public void stop() {
		sendMsg(stopLoading);
	}

	/** returns the browser engine used for creating this browser. */
	public IBrowserEngine getBrowserEngine() {
		return engine;
	}

	/**
	 * WebKitBrowser works in one thread mode,needsn't care this method
	 */
	public void shutdown() {
	}

	
	public Component asComponent() {
		return (Component) this;
	}

	/**
	 * Adds a <code>WebBrowserEvent</code> listener.
	 * 
	 * @param listener
	 *            object which implements WebBrowserListener interface.
	 */
	public synchronized void addWebBrowserListener(WebBrowserListener listener) {
        listenerList.add(WebBrowserListener.class, listener);
	}

	/**
	 * Removes a <code>WebBrowserEvent</code> listener.
	 * 
	 * @param listener
	 *            object which implements WebBrowserListener interface. If the
	 *            listener was not in the listeners list, then no listener will
	 *            be removed.
	 */
	public synchronized void removeWebBrowserListener(
			WebBrowserListener listener) {
        listenerList.remove(WebBrowserListener.class, listener);
	}

    protected void fireWebBrowserEvent(final IWebBrowser source, final int id,
    final String data) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        WebBrowserEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==WebBrowserListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new WebBrowserEvent(source, id, data);
            
            switch (id) {
			case WebBrowserEvent.WEBBROWSER_INIT_WINDOW_SUCC:
				((WebBrowserListener)listeners[i+1]).initializationCompleted(e);
				break;
			case WebBrowserEvent.WEBBROWSER_DOWNLOAD_STARTED:
				((WebBrowserListener)listeners[i+1]).downloadStarted(e);
				break;
			case WebBrowserEvent.WEBBROWSER_DOWNLOAD_COMPLETED:
				((WebBrowserListener)listeners[i+1]).downloadCompleted(e);
				break;
			case WebBrowserEvent.WEBBROWSER_DOWNLOAD_PROGRESS:
				((WebBrowserListener)listeners[i+1]).downloadProgress(e);
				break;
			case WebBrowserEvent.WEBBROWSER_DOWNLOAD_ERROR:
				((WebBrowserListener)listeners[i+1]).downloadError(e);
				break;
			case WebBrowserEvent.WEBBROWSER_DOCUMENT_COMPLETED:
				((WebBrowserListener)listeners[i+1]).documentCompleted(e);
				break;
			case WebBrowserEvent.WEBBROWSER_TITLE_CHANGE:
				((WebBrowserListener)listeners[i+1]).titleChange(e);
				break;
			case WebBrowserEvent.WEBBROWSER_STATUSTEXT_CHANGE:
				((WebBrowserListener)listeners[i+1]).statusTextChange(e);
				break;
			}
            }          
        }
    }
    
	/**
	 * auto dispose property is empty for web kit
	 */
	public void setAutoDispose(boolean autoDispose) {
	}
}
