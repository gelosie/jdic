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

import java.util.Vector;
import java.net.URL;
import java.net.MalformedURLException;
import java.awt.*;
import java.awt.event.*;
import java.security.*;
import java.io.File;

import org.jdesktop.jdic.init.JdicInitException;
import org.jdesktop.jdic.init.JdicManager;
import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

/**
 * A <code>WebBrowser</code> component represents a blank rectangular area of 
 * the screen onto which the application can display webpages or from which the
 * application can trap events from the browser window. In order to show <code>
 * WebBrowser</code> component in GUI, users need to add <code>WebBrowser</code> 
 * to a top-level container, such as <code>Frame</code>.
 * <p>
 * The class that is interested in processing a <code>WebBrowser</code> event 
 * should implement interface <code>WebBrowserListener</code>, and the object 
 * created with that class should use WebBrowser's <code>addWebBrowserListener
 * </code> method to register as a listener.
 * <p>
 * As an AWT component, a <code>WebBrowser</code> component must be hosted by 
 * a native container somewhere higher up in the component tree (for example, 
 * by a JPanel object).
 * 
 * @see WebBrowserEvent
 * @see WebBrowserListener
 *
 * @author Kyle Yuan
 * @version 0.1, 03/07/17
 */
public class WebBrowser extends Canvas
{
    private static final String EMBED_BINARY_WINDOWS_IE = "IeEmbed.exe";
    private static final String EMBED_BINARY_WINDOWS_MOZILLA = "MozEmbed.exe";
    private static final String EMBED_BINARY_LINUX_GTK1 
        = "mozembed-linux-gtk1.2";
    private static final String EMBED_BINARY_LINUX_GTK2 
        = "mozembed-linux-gtk2";
    private static final String EMBED_BINARY_FREEBSD_GTK1 
        = "mozembed-freebsd-gtk1.2";
    private static final String EMBED_BINARY_FREEBSD_GTK2 
        = "mozembed-freebsd-gtk2";
    private static final String EMBED_BINARY_SOLARIS_GTK1 
        = "mozembed-solaris-gtk1.2";
    private static final String EMBED_BINARY_SOLARIS_GTK2 
        = "mozembed-solaris-gtk2";

    // Native browser embedding binary: IeEmbed.exe or MozEmbed.exe on Windows, 
    // mozembed-<os>-gtk<version> on Linux/Unix.
    private static String embedBinary;
    private Status status = new Status();
    private MyFocusListener focusListener = new MyFocusListener();
    // eventThread should be initialized after JdicManager.initShareNative() 
    // in static block.
    private static NativeEventThread eventThread; 
    private Vector webclientListeners = new Vector();
    private int instanceNum;
    private static int lastInstanceNum = 0;
    private static boolean isRunningOnWindows = false;
    private static boolean isDebugOn = false;

    static {
        // Add the initialization code from package org.jdesktop.jdic.init.
        // To set the environment variables or initialize the set up for 
        // native libraries and executable files.
        try {
            JdicManager jm = JdicManager.getManager();
            jm.initShareNative();
            jm.initBrowserNative();
        } catch (JdicInitException e){
            e.printStackTrace();
        }
        eventThread = new NativeEventThread();
        
        AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        System.loadLibrary("jdic");
                        return null; // nothing to return
                    }
                }
            );
        isRunningOnWindows 
            = System.getProperty("os.name").indexOf("Windows") >= 0;
    }

    /**
     * Constructs a new <code>WebBrowser</code> object with no URL specified.
     */
    public WebBrowser() {
        this(null);
    }

    /**
     * Constructs a new <code>WebBrowser</code> with an URL specified.
     */
    public WebBrowser(URL url) {
        synchronized(this) {
            instanceNum = lastInstanceNum;
            lastInstanceNum++;
        }
        eventThread.attachWebBrowser(this);

        if (0 == instanceNum) {
            embedBinary = getEmbedBinaryName();
            
            eventThread.start();
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_INIT);
        }

        if (null != url) {
            setURL(url);
        }

        setFocusable(true);
        addFocusListener(focusListener);
    }

    /**
     * Returns the name of the native browser embedding binary. If no default
     * browser is set, null is returned.  
     */
    public static String getEmbedBinaryName() {
        if (embedBinary != null && embedBinary.length() > 0)
            return embedBinary;

        String embedBin = null;
        String nativePath = WebBrowserUtil.getBrowserPath();
        if (null == nativePath) {
            WebBrowser.trace("No default browser is found. " +
                    "Or environment variable MOZILLA_FIVE_HOME is not set to " +
                    "a Mozilla binary path if you are on Linux/Unix platform.");
            return null; 
        }

        String osname = System.getProperty("os.name");
        if (osname.indexOf("Windows") >= 0) {
            String windowspath = nativePath;
            int index = windowspath.indexOf("mozilla.exe");
            if (index >= 0)
                embedBin = EMBED_BINARY_WINDOWS_MOZILLA;
            else
                embedBin = EMBED_BINARY_WINDOWS_IE;
        }
        else {
            String libwidgetpath = nativePath + File.separator +
                                   "components" + File.separator + 
                                   "libwidget_gtk2.so";
            File file = new File(libwidgetpath);
            if (!file.exists()) {
                if (osname.indexOf("Linux") >= 0) {
                    embedBin = EMBED_BINARY_LINUX_GTK1;
                }
                else if (osname.indexOf("SunOS") >= 0) {
                    embedBin = EMBED_BINARY_SOLARIS_GTK1;
                }
                else if (osname.indexOf("FreeBSD") >= 0) {
                    embedBin = EMBED_BINARY_FREEBSD_GTK1;
                }
            }
            else {
                if (osname.indexOf("Linux") >= 0) {
                    embedBin = EMBED_BINARY_LINUX_GTK2;
                }
                else if (osname.indexOf("SunOS") >= 0) {
                    embedBin = EMBED_BINARY_SOLARIS_GTK2;
                }
                else if (osname.indexOf("FreeBSD") >= 0) {
                    embedBin = EMBED_BINARY_FREEBSD_GTK2;
                }
            }
        }
        
        return embedBin;
    }
    
    /**
     * Creates the peer for this WebBrowser component. The peer allows us to 
     * modify the appearance of the WebBrowser component without changing its 
     * functionality.
     */
    public void addNotify() {
        super.addNotify();
        
        eventThread.fireNativeEvent(instanceNum, 
                NativeEventData.EVENT_CREATEWINDOW);
    }

    /**
     * Moves and resizes this component. The new location of the top-left 
     * corner is specified by <code>x</code> and <code>y</code>, and the new 
     * size is specified by <code>width</code> and <code>height</code>.
     *
     * @param x - the new x-coordinate of this component
     * @param y - the new y-coordinate of this component
     * @param width - the new width of this component
     * @param height - the new height of this component
     */
    public void setBounds(int x, int y, int width, int height) {    
        super.setBounds(x, y, width, height);
        eventThread.fireNativeEvent(instanceNum, 
                NativeEventData.EVENT_SET_BOUNDS, 
                new Rectangle(x, y, width, height));
    }

    // dispatch a WebBrowserEvent to the embeddor, called by 
    // NativeEventThread.processIncomingMessage.
    void dispatchWebBrowserEvent(WebBrowserEvent e) {
        int eid = e.getID();

        WebBrowser.trace("Got event from NativeEventThread " + eid);

        // native browser needs immediate return value for these two events.
        String msg = "@" + instanceNum + "," + eid + ",";
        if (WebBrowserEvent.WEBBROWSER_BEFORE_NAVIGATE == eid) {
            URL url = null;
            try {
                url = new URL(e.getData());
            } catch (MalformedURLException ex1) {
                try {
                	// IE omits the file:/ protocol for local files, append it.
                	url = new URL("file:/" + e.getData());  
                } catch (MalformedURLException ex2) {                    
                }
            }
            
            msg += willOpenURL(url) ? "0" : "1";
            eventThread.messenger.sendMessage(msg);
            return;
        }
        else if (WebBrowserEvent.WEBBROWSER_BEFORE_NEWWINDOW == eid) {
            msg += willOpenWindow() ? "0" : "1";
            eventThread.messenger.sendMessage(msg);
            return;
        }
        else if (WebBrowserEvent.WEBBROWSER_COMMAND_STATE_CHANGE == eid) {
            String data = e.getData();
            if (data.startsWith("forward")) {
                status.forwardEnabled = data.substring(8).equals("1");
                WebBrowser.trace("Forward State changed = " 
                        + status.forwardEnabled);
            }
            else if (data.startsWith("back")) {
                status.backEnabled = data.substring(5).equals("1");
                WebBrowser.trace("Back State changed = " + status.backEnabled);
            }
            return;
        }
        else if (WebBrowserEvent.WEBBROWSER_FOCUS_REQUEST == eid) {
            WebBrowser.trace("Got Event from brower: Focus Rquest!");
            requestFocus();
            return;
        }

        // for the normal case, call the corresponding method in listeners.
        int size;
        Vector tl;
        synchronized (this) {
            size = webclientListeners.size();
            if (size == 0) {
                return;
            }

            tl = (Vector) webclientListeners.clone();
        }

        for (int i = 0; i < size; ++i) {
            WebBrowserListener listener = (WebBrowserListener) tl.elementAt(i);
            switch (eid) {
                case WebBrowserEvent.WEBBROWSER_DOWNLOAD_STARTED:
                    listener.downloadStarted(e);
                    break;
                case WebBrowserEvent.WEBBROWSER_DOWNLOAD_COMPLETED:
                    listener.downloadCompleted(e);
                    break;
                case WebBrowserEvent.WEBBROWSER_DOWNLOAD_PROGRESS:
                    listener.downloadProgress(e);
                    break;
                case WebBrowserEvent.WEBBROWSER_DOWNLOAD_ERROR:
                    listener.downloadError(e);
                    break;
                case WebBrowserEvent.WEBBROWSER_DOCUMENT_COMPLETED:
                    listener.documentCompleted(e);
                    break;
                case WebBrowserEvent.WEBBROWSER_TITLE_CHANGE:
                    listener.titleChange(e);
                    break;
                case WebBrowserEvent.WEBBROWSER_STATUSTEXT_CHANGE:
                    listener.statusTextChange(e);
                    break;
            }
        }
    }

    /**
     * Adds a <code>WebBrowserEvent</code> listener to the listener list. 
     * If listener is null, no exception is thrown and no action is performed.
     *
     * @param listener the WebBrowser event listener.
     */
    public synchronized void addWebBrowserListener(
            WebBrowserListener listener) {
        if (! webclientListeners.contains(listener)) {
            webclientListeners.addElement(listener);
        }
    }

    /**
     * Removes a <code>WebBrowserEvent</code> listener from the listener list.
     * If listener is null, no exception is thrown and no action is performed.
     * If the listener is not in the listener list, no listener is removed.  
     *
     * @param listener the WebBrowser event listener.
     */
    public synchronized void removeWebBrowserListener(
            WebBrowserListener listener) {

        if (listener == null) 
            return;
        
        webclientListeners.removeElement(listener);
    }

    /**
     * Returns the URL of the resource that is currently being displayed.
     *
     * @return the current URL being display, or <code>null</code> if no URL is
     *         currently displayed or the WebBrowser is not yet initialized.
     */
    public URL getURL() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_GETURL);

        if (waitForResult() == true) {
            try {
                return new URL(eventThread.eventRetString);
            }
            catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Sets the loaded page to be a blank page.
     *
     */
    public void setURL() {
        eventThread.fireNativeEvent(instanceNum, 
                NativeEventData.EVENT_NAVIGATE, "about:blank");
    }

    /**
     * Navigates to a resource identified by an URL using HTTP GET method.
     *
     * @param url the URL to navigate.
     */
    public void setURL(URL url) {
        setURL(url, null);
    }

    /**
     * Navigates to a resource identified by an URL using HTTP POST method.
     *
     * @param url       the URL to navigate.
     * @param postData  data to send to the server during the HTTP POST 
     *                  transaction.
     */
    public void setURL(URL url, String postData) {
        if (postData == null) {
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_NAVIGATE, url.toString());
        }
        else {
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_NAVIGATE_POST, url.toString());
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_NAVIGATE_POSTDATA, postData);
        }
    }

    /**
     * Navigates backward one item in the history list.
     */
    public void back() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_GOBACK);
    }

    /**
     * Navigates forward one item in the history list.
     */
    public void forward() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_GOFORWARD);
    }

    /**
     * Reloads the URL that is currently displayed in the WebBrowser component.
     */
    public void refresh() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_REFRESH);
    }

    /**
     * Stops any page loading and rendering activities. 
     */
    public void stop() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_STOP);
    }

    /**
     * Sets new HTML content. 
     * 
     * @param htmlContent the HTML content to set.
     * @since 0.9
     */
    public void setContent(String htmlContent) {
        eventThread.fireNativeEvent(instanceNum, 
                NativeEventData.EVENT_SETCONTENT, htmlContent);
    }

    /**
     * Returns the HTML content of a document, opened in a browser.
     * 
     * @return the HTML content of a document, opened in a browser.
     * @since 0.9
     */
    public String getContent() {
        eventThread.fireNativeEvent(instanceNum, 
                                    NativeEventData.EVENT_GETCONTENT);

        if (waitForResult() == true) {
            try {
                return eventThread.eventRetString;
            }
            catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Executes specified JavaScript code in a currently opened document.
     * This should not be called until after a documentComplete event is 
     * fired in <code>WebBrowserListener</code>. 
     *
     * @return the result of JavaScript execution, if there is any.
     * @since 0.9 
     */
    public String executeScript(java.lang.String javaScript) {
        eventThread.fireNativeEvent(instanceNum, 
                                    NativeEventData.EVENT_EXECUTESCRIPT, 
                                    javaScript);

        if (waitForResult() == true) {
            try {
                return eventThread.eventRetString;
            }
            catch (Exception e) {
            }
        }
        return null;
    }
    
    /**
     * Enables or disables debug message output. Debug message out is disabled
     * initially by default. Calls it via reflection when necessary.
     *
     * @param b if <code>true</true>, debug message output is enabled; 
     *          otherwise debug message output is disabled.
     */
    public static void setDebug(boolean b) {
        isDebugOn = b;
    }

    /**
     * Gets the boolean which indicates is debug on or off
     */
    static boolean getDebug () {
        return isDebugOn;
    }

    /**
     * Returns a <code>Status</code> object, which indicates the status of this
     * <code>WebBrowser</code> object.
     *
     * @see Status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Called before every navigation operation occurs. A subclass could 
     * override this method to change or block URL loading.
     *
     * @return <code>false</code> will prevent the the navigation from starting;
     *         otherwise <code>true</code>.
     */
    protected boolean willOpenURL(URL url) {
        if (null == url)
            return true;

        WebBrowser.trace("URL = " + url.toString());
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            try {
                security.checkConnect(url.getHost(), url.getPort());
            }
            catch (AccessControlException e){
                return false;
            }
        }
        return true;
    }

    /**
     * Called before every new window is to be created. A subclass could 
     * override this method to prevent new window from popping up.
     *
     * @return <code>false</code> will prevent the new window from popping up; 
     *         otherwise <code>true</code>.
     */
    protected boolean willOpenWindow() {
        return true;
    }

    int getInstanceNum() {
        return instanceNum;
    }

    private boolean waitForResult() {
        if (! status.initialized) {
            WebBrowser.trace("You can't call this method before WebBrowser " +
                    "initialized!");
            return false;
        }

        boolean ret = false;
        synchronized(this) {
            try {
                wait();
                ret = true;
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return ret;
    }

    int getNativeWindow() {
        // The java.home property value is required to load jawt.dll on Windows.         
        return nativeGetWindow(System.getProperty("java.home"));
    }
    
    /* native functions */
    private native int nativeGetWindow(String javaHome);
    
    /* debug helper */
    static void trace(String msg) {
        if (isDebugOn)
            System.out.println("*** Jtrace: " + msg);
    }

    /**
     * An inner class which is used for retrieving the WebBrowser's properties,
     * such as the initialization status, back and forward status.
     */
    public static class Status {
        private boolean initialized;
        private boolean backEnabled;
        private boolean forwardEnabled;
        static private String failReason;

        Status () {
            initialized = false;
            backEnabled = false;
            forwardEnabled = false;
            failReason = "WebBrowser has not finish intializing";
        }

        /**
         * Tests whether the <code>WebBrowser</code> object is initialized 
         * successfully.
         *
         * @return <code>true</code> if the <code>WebBrowser</code> object is 
         *         initialized successfully; otherwise, <code>false</code>.
         *
         */
        public boolean isInitialized() {
            return initialized;
        }

        /**
         * Tests whether the back navigation operation is enabled.
         *
         * @return <code>true</code> if the back navigation operation is enabled;
         * otherwise, <code>false</code>.
         *
         */
        public boolean isBackEnabled() {
            return backEnabled;
        }

        /**
         * Tests whether the forward navigation operation is enabled.
         *
         * @return <code>true</code> if the forward navigation operation is 
         *         enabled; otherwise, <code>false</code>.
         */
        public boolean isForwardEnabled() {
            return forwardEnabled;
        }

        void setInitStatus(boolean b) {
            initialized = b;
        }

        void setInitFailReason(String msg) {
            failReason = msg;
        }
    }

    class MyFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            WebBrowser.trace("\nMyFocusListener: focusGained\n");
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_FOCUSGAINED);
        }

        public void focusLost(FocusEvent e) {
            WebBrowser.trace("\nMyFocusListener: focusLost\n");
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_FOCUSLOST);
        }
    }
}
