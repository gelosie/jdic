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
import java.awt.*;
import java.awt.event.*;
import java.security.*;
import java.io.File;

/**
 * A <code>WebBrowser</code> component represents a blank rectangular area of the 
 * screen onto which the application can display webpages or from which the 
 * application can trap events from the browser window. In order to show WebBrowser 
 * component in GUI, user need to add WebBrowser to a top-level container, such as 
 * <code>Frame</code>. 
 * <p>
 * The class that is interested in processing a WebBrowser event should implement
 * interface <code>WebBrowserListener</code>, and the object created with that class 
 * should use WebBrowser's <code>addWebBrowserListener</code> method to register 
 * as a listener.
 * <p>
 * The <code>WebBrowser</code> class has an inner class <code>Status</code>. User can use 
 * <code>getStatus</code> method to retrieve the <code>Status</code> object of the 
 * WebBrowser object. Querying status of this WebBrowser can be done by calling methods 
 * provided by the <code>Status</code> class.
 *
 * @see WebBrowserEvent 
 * @see WebBrowserListener
 * 
 * @author Kyle Yuan
 * @version 0.1, 03/07/17
 */ 
public class WebBrowser extends Canvas
{
    private static final String binary_windows_ie = "IeEmbed.exe";
    private static final String binary_windows_mozilla = "MozEmbed.exe";
    private static final String binary_linux_gtk1 = "mozembed-linux-gtk1.2";
    private static final String binary_linux_gtk2 = "mozembed-linux-gtk2";
    private static final String binary_freebsd_gtk1 = "mozembed-freebsd-gtk1.2";
    private static final String binary_freebsd_gtk2 = "mozembed-freebsd-gtk2";
    private static final String binary_solaris_gtk1 = "mozembed-solaris-gtk1.2";
    private static final String binary_solaris_gtk2 = "mozembed-solaris-gtk2";

    private static String browserBinary;
    private Status status = new Status();    
    private MyFocusListener focusListener = new MyFocusListener();
    private static NativeEventThread eventThread = new NativeEventThread();
    private Vector webclientListeners = new Vector();
    private int instanceNum;
    private static int lastInstanceNum = 0;
    private static boolean isRunningOnWindows = false;
    private static boolean isDebugOn = false;

    static {
        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    System.loadLibrary("jdic");
                    return null; // nothing to return
                }
            }
        );
        isRunningOnWindows = System.getProperty("os.name").indexOf("Windows") >= 0;
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
            setBinaryName();
            eventThread.start();
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_INIT);
        }

        if (null != url) {
            setURL(url);
        }
        
        setFocusable(true);
        addFocusListener(focusListener);
    }
    
    private void setBinaryName() {
        if (browserBinary != null && browserBinary.length() > 0)
            return;
        
        String nativePath = nativeGetBrowserPath();
        if (null == nativePath) {
            WebBrowser.trace("Cant find default browser if you are on windows!");
            WebBrowser.trace("Or environment variable MOZILLA_FIVE_HOME not set if you are on linux/unix!");
            browserBinary = null;
            return;
        }	
        
        String osname = System.getProperty("os.name");
        if (osname.indexOf("Windows") >= 0) {
            String windowspath = nativePath;
            int index = windowspath.indexOf("mozilla.exe");
            if (index >= 0)
                browserBinary = binary_windows_mozilla;
            else 
                browserBinary = binary_windows_ie;
        }
        else {
            String libwidgetpath = nativePath + File.separator +
                                   "components" + File.separator + "libwidget_gtk2.so";
            File file = new File(libwidgetpath);
            if (!file.exists()) {
                if (osname.indexOf("Linux") >= 0) {
                    browserBinary = binary_linux_gtk1;
                }
                else if (osname.indexOf("SunOS") >= 0) {
                    browserBinary = binary_solaris_gtk1;
                }
                else if (osname.indexOf("FreeBSD") >= 0) {
                    browserBinary = binary_freebsd_gtk1;
                }
            }
            else {
                if (osname.indexOf("Linux") >= 0) {
                    browserBinary = binary_linux_gtk2;
                }
                else if (osname.indexOf("SunOS") >= 0) {
                    browserBinary = binary_solaris_gtk2;
                }
                else if (osname.indexOf("FreeBSD") >= 0) {
                    browserBinary = binary_freebsd_gtk2;
                }
            }
        }
    }

    /**
     * Overrides the same method of <code>java.awt.Canvas</code> in order to 
     * paint the browser window. On Windows system it is invoked by the system 
     * automatically, users are not recommended to call it.
     */
    public void addNotify() {
        super.addNotify();
        if (isRunningOnWindows) {
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_CREATEWINDOW);
        }
    }
    
    /**
     * Moves and resizes this component. The new location of the top-left corner is 
     * specified by x and y, and the new size is specified by width and height.
     *
     * @param x - the new x-coordinate of this component 
     * @param y - the new y-coordinate of this component 
     * @param w - the new width of this component 
     * @param h - the new height of this component
     */
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_SET_BOUNDS, new Rectangle(x, y, w, h));
    }

    // dispatch a WebBrowserEvent to the embeddor, called by NativeEventThread.processIncomingMessage.
    void dispatchWebBrowserEvent(WebBrowserEvent e) {
        int eid = e.getID();

        WebBrowser.trace("Got event from NativeEventThread " + eid);
        
        // native browser needs immediate return value for these two events.
        String msg = "@" + instanceNum + "," + eid + ",";
        if (WebBrowserEvent.WEBBROWSER_BEFORE_NAVIGATE == eid) {
            URL url = null;
            try {
                url = new URL(e.getData());
            }
            catch (Exception ex) {
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
                WebBrowser.trace("Forward State changed = " + status.forwardEnabled);
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
            }
        }
    }

    /**
     * Adds a <code>WebBrowserEvent</code> listener. 
     *
     * @param listener object which implements WebBrowserListener interface.
     */
    public synchronized void addWebBrowserListener(WebBrowserListener listener) {
        if (! webclientListeners.contains(listener)) {
            webclientListeners.addElement(listener);
        }
    }

    /**
     * Removes a <code>WebBrowserEvent</code> listener. 
     *
     * @param listener object which implements WebBrowserListener interface. 
     * If the listener was not in the listeners list, then no listener will be
     * removed.
     */
    public synchronized void removeWebBrowserListener(WebBrowserListener listener) {
        webclientListeners.removeElement(listener);
    }

    /**
     * Retrieves the URL that is currently being displayed. 
     *
     * @return the current URL being display, or <code>null</code> if the WebBrowser object 
     * is not ready with initialization of itself. 
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
     * Sets the document to be a blank page.
     *
     */   
    public void setURL() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_NAVIGATE, "about:blank");
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
     * @param postData  Data to send to the server during the HTTP POST transaction.
     */
    public void setURL(URL url, String postData) {
        if (postData == null) {
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_NAVIGATE, url.toString());
        }
        else {
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_NAVIGATE_POST, url.toString());
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_NAVIGATE_POSTDATA, postData);
        }
    }	

    /**
     * Navigates to the previous session history item.
     */
    public void back() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_GOBACK);
    }

    /**
     *  Navigates to the next session history item. 
     */
    public void forward() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_GOFORWARD);
    }
    
    /**
     * Reloads the URL that is currently being displayed in the WebBrowser component.
     */
    public void refresh() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_REFRESH);
    }
    
    /**
     * Stops loading of the current URL.
     */
    public void stop() {
        eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_STOP);
    }

    /**
     * Sets trace messages on or off. If on, the trace messages will be printed
     * out in the console. 
     * 
     * @param b <code>true</code> if enable the trace messages; otherwise, 
     * <code>false</code>.
     */
    public static void setDebug(boolean b) {
        isDebugOn = b;
    }

    /**
     * Get the boolean which indicates is debug on or off
     */
    static boolean getDebug () {
        return isDebugOn;
    }

    /**
     * Get the pathname which points to the embedded browser's binary
     */
    static String getBrowserBinary () {
        return browserBinary;
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
     * Called before every navigation operation occurs. A subclass could override 
     * this method to change or block URL loading. 
     *
     * @return <code>false</code> will prevent the the navigation from starting; 
     *         otherwise <code>true</code>.
     */
    protected boolean willOpenURL(URL url) {
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
     * Called before every new window is to be created. A subclass could override this 
     * method to prevent new window from popping up.
     *
     * @return <code>false</code> will prevent the new window from popping up; otherwise
     * <code>true</code>. 
     */
    protected boolean willOpenWindow() {
        return true;
    }
    
    int getInstanceNum() {
        return instanceNum;	
    }
    
    private boolean waitForResult() {
        if (! status.initialized) {
            WebBrowser.trace("You can't call this method before WebBrowser initialized!");
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
        return nativeGetWindow();
    }

    /* only used for GTK */
    private boolean firstTime = true;

     /**
     * Overrides the same method of <code>java.awt.Canvas</code> in order to 
     * paint the browser window. On Linux and Unix systems it is invoked by 
     * the system automatically, users are not recommended to call it.
     *
     * @param g the specified graphics context.
     */
    public void paint(Graphics g) {
        // don't call super.paint(), we will handle the drawing by ourselves.
        if (firstTime) {
            // On GTK, the native window id of Canvas is only available when the first call of paint
            firstTime = false;
            if (! isRunningOnWindows) {
                eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_CREATEWINDOW);
            }
        }
    }

    /* native functions */
    private native int nativeGetWindow();
    private native String nativeGetBrowserPath();
    static native void nativeSetEnv();

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
         * Tests whether the <code>WebBrowser</code> object is initialized successfully.
         * 
         * @return <code>true</code> if the <code>WebBrowser</code> object is initialized 
         * successfully; otherwise, <code>false</code>.
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
         * @return <code>true</code> if the forward navigation operation is enabled; 
         * otherwise, <code>false</code>.
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
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_FOCUSGAINED);
        }

        public void focusLost(FocusEvent e) {
            WebBrowser.trace("\nMyFocusListener: focusLost\n");
            eventThread.fireNativeEvent(instanceNum, NativeEventData.EVENT_FOCUSLOST);
        }
    }
}
