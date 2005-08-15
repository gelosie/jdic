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

import org.jdesktop.jdic.init.JdicInitException;
import org.jdesktop.jdic.init.JdicManager;
import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

/**
 * A <code>WebBrowser</code> component represents a blank rectangular area of 
 * the screen onto which the application can load webpages or from which the
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
    private MyFocusListener focusListener = new MyFocusListener();
    // eventThread should be initialized after JdicManager.initShareNative() 
    // in static block.
    private static NativeEventThread eventThread; 
    private Vector webBrowserListeners = new Vector();
    private int instanceNum;
    private static int lastInstanceNum = 0;
    
    // WebBrowser status.
    private boolean isInitialized = false;
    private boolean isBackEnabled = false;
    private boolean isForwardEnabled = false;
    private String initFailureMessage = "WebBrowser is not initialized.";       

    /**
     * boolean flag used to indicate how to dispose this instance.
     * @see #dispose()
     * @see #removeNotify()
     */
    private boolean autoDispose = true;
    
    /**
     * Used to cache the url before dispose this instance.
     * 
     * @see #dispose()
     * @see #addNotify()
     * @see #removeNotify()
     */
    private URL urlBeforeDispose = null;

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
    }

    void setInitialized(boolean b) {
        isInitialized = b;
    }

    void setInitFailureMessage(String msg) {
        initFailureMessage = msg;
    }    

    /**
     * Constructs a new <code>WebBrowser</code> object with no URL specified.
     * This instance will automatically dispose itself when <code>removeNotify()
     * </code> is called. 
     * <p>
     * This constructor is equivalent to <code>WebBrowser(true)</code>.
     * 
     * @see #WebBrowser(boolean)
     * @see #removeNotify()
     * @see #dispose()
     */
    public WebBrowser() {
        this(true);
    }
    
    /**
     * Constructs a new <code>WebBrowser</code> object with the specified 
     * boolean value <code>autoDispose</code> as the flag which indicates 
     * whether this instance will automatically dispose itself in <code>
     * removeNotify()</code> or should be disposed by the developer directly 
     * calling <code>dispose()</code>.
     * <p>
     * This constructor is equivalent to <code>WebBrowser(null, autoDispose)
     * </code>.
     * 
     * @param autoDispose ture to indicate this instance will automatically 
     *        dispose itself in <code>removeNotify()</code>; false to indicate
     *        the developer should call <code>dispose()</code> when this 
     *        instance is no longer needed.
     * 
     * @see #removeNotify()
     * @see #dispose()
     * @see #isAutoDispose()
     */
    public WebBrowser(boolean autoDispose){
        this(null, autoDispose);
    }


    /**
     * Constructs a new <code>WebBrowser</code> with an URL specified. This 
     * instance will automatically dispose itself when <code>removeNotify()
     * </code> is called. 
     * <p>This constructor is equivalent to <code>WebBrowser(url, true)</code>.

     * @param url the URL to be shown in this instance.
     * 
     * @see #WebBrowser(boolean)
     * @see #removeNotify()
     * @see #dispose()
     */
    public WebBrowser(URL url) {
        this(url, true);
    }
    
    /**
     * Constructs a new <code>WebBrowser</code> with an specified URL and 
     * boolean flag to indicate the dispose schema.
     * 
     * @param url the URL to be shown in this instance.
     * @param autoDispose ture to indicate this instance will automatically 
     *        dispose itself in <code>removeNotify()</code>; false to indicate
     *        the developer should call <code>dispose()</code> when this 
     *        instance is no longer needed.
     *        
     * @see #removeNotify()
     * @see #dispose()
     * @see #isAutoDispose()
     */
    public WebBrowser(URL url, boolean autoDispose){
        this.autoDispose = autoDispose;
        
        synchronized(WebBrowser.class) {
            instanceNum = lastInstanceNum;
            lastInstanceNum++;
        }
        eventThread.attachWebBrowser(this);

        if (0 == instanceNum) {
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
     * Creates the peer for this WebBrowser component. The peer allows us to 
     * modify the appearance of the WebBrowser component without changing its 
     * functionality.
     * 
     * @see #removeNotify()
     */
    public void addNotify() {
        super.addNotify();
        if(!isInitialized) {
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_CREATEWINDOW);
            
            /**
             * Reset the URL before this instance was disposed.
             * urlBeforeDispose is set in {@link #disposed()}.
             */
            if(urlBeforeDispose != null){
                this.setURL(urlBeforeDispose);
                urlBeforeDispose = null;
            }
        }
        if(!autoDispose){
            this.setVisible(true);
        }
    }
    
    /**
     * Makes this WebBrowser component undisplayable by destroying it native 
     * screen resource if the <code>isAutoDispose()</code> return true. Or just 
     * make this instance invisible if the <code>isAutoDispose()</code> return
     * false. <p>
     * This method is called by the toolkit internally and should not be called 
     * directly by programs. 
     * <p>
     * If <code>isAutoDispose()</code> return false, developer should call 
     * <code>dispose()</code> when this instance is no longer needed. 
     * Otherwise, it resource will never be released untill this JVM exit.
     * 
     * @see #addNotify()
     * @see #dispose()
     * @see #isAutoDispose()
     */     
    public void removeNotify() {
        if(autoDispose){
            dispose();
        }else{
            this.setVisible(false);
        }
    }
    
    /**
     * Release this instance's resource and make it undisplayable. If <code>
     * isAutoDispose()</code> return true, this method will be called by the 
     * toolkit internally. If <code>isAutoDispose()</code> return false, this 
     * method should be called by developer when this instance is no longer 
     * needed.
     * 
     * @see #removeNotify()
     * @see #isAutoDispose()
     */
    public void dispose() {
        urlBeforeDispose = this.getURL();
        synchronized (this) {
            eventThread.fireNativeEvent(instanceNum,
                    NativeEventData.EVENT_DESTROYWINDOW);

            try {
                // wait untill we get the ACK message
                // WebBrowserEvent.WEBBROWSER_DESTROYWINDOW_SUCC
                // from native process.
                this.wait();
            } catch (InterruptedException e) {
            }
        }
        WebBrowser.super.removeNotify();
        setInitialized(false);
    }
    
    /**
     * Return the boolean flag which indicates how to dispose this instance.
     * 
     * @return true if this instance should be disposed by itself when
     *         <code>removeNotify()</code> is called. false if this instance
     *         should be disposed by the developer directly calling
     *         <code>dispose()</code> when it is no longer needed.
     *         
     * @see #removeNotify()
     * @see #addNotify()
     * @see #dispose()
     */
    public boolean isAutoDispose(){
        return autoDispose;
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

    /*
     * Dispatches a WebBrowserEvent to the Java embeddor, called by 
     * NativeEventThread.processMessageFromNative. 
     */
    void dispatchWebBrowserEvent(WebBrowserEvent e) {
        int eid = e.getID();

        WebBrowserUtil.trace("Dispatch event from NativeEventThread: " + eid);

        // native browser needs immediate return value for these two events.       
        // Special trigger messages beginning with a '@' character, to give 
        // the native browser a yes or no to continue an operation(navigating
        // an URL or openning a new window).
        String msg = "@" + instanceNum + "," + eid + ",";
        if (WebBrowserEvent.WEBBROWSER_BEFORE_NAVIGATE == eid) {
            URL url = null;
            try {
                url = new URL(e.getData());
            } catch (MalformedURLException ex1) {
                try {
                	// IE omits the file:/ protocol for local files, append it.                    
                    if (!WebBrowserUtil.isDefaultBrowserMozilla()) {
                        url = new URL("file:/" + e.getData());
                    }
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
                isForwardEnabled = data.substring(8).equals("1");
                WebBrowserUtil.trace("Forward State changed = " 
                        + isForwardEnabled);
            }
            else if (data.startsWith("back")) {
                isBackEnabled = data.substring(5).equals("1");
                WebBrowserUtil.trace("Back State changed = " + isBackEnabled);
            }
            return;
        }
        else if (WebBrowserEvent.WEBBROWSER_FOCUS_REQUEST == eid) {
            WebBrowserUtil.trace("Got event from brower: Focus request.");
            requestFocus();
            return;
        }
        else if (WebBrowserEvent.WEBBROWSER_DESTROYWINDOW_SUCC == eid){
            WebBrowserUtil.trace("Got event from brower: Destory window " +
                    "succeeds.");
            synchronized(this){
                // notify the disposeThread in removeNotify().
                this.notify();
            }
       }

        // for the normal case, call the corresponding method in listeners.
        int size;
        Vector tl;
        synchronized (this) {
            size = webBrowserListeners.size();
            if (size == 0) {
                return;
            }

            tl = (Vector) webBrowserListeners.clone();
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
        if (! webBrowserListeners.contains(listener)) {
            webBrowserListeners.addElement(listener);
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
        
        webBrowserListeners.removeElement(listener);
    }

    /**
     * Returns an array of all the registered WebBrowser listeners.
     *
     * @return all of this component's <code>WebBrowserListener</code>s or an 
     *         empty array if no component listeners are currently registered.
     * @since 0.9 
     */
    public WebBrowserListener[] getWebBrowserListeners() {
        return (WebBrowserListener[])webBrowserListeners.toArray();
    }

    /**
     * Returns the URL of the resource that is currently being loaded.
     *
     * @return the current URL being loaded, or <code>null</code> if no URL is
     *         currentlloadayed or the WebBrowser is not yet initialized.
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
     * @param postData  the HTTP POST data. For example, 
     *                  <code>"username=myid&password=mypasswd"</code>
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
     * Reloads the URL that is currently loaded in the WebBrowser component.
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
     * Prints the currently loaded document.
     * <p>
     * This is a convenience method to use <code>executeScript</code> to print 
     * the currently loaded document:
     * <code>
     * <pre>
     *     // Print the currently loaded document.    
     *     WebBrowser webBrowser = new WebBrowser();
     *     ......
     *     webBrowser.executeScript("window.print();");
     * </pre>
     * </code>  
     * 
     * @see #executeScript(java.lang.String)
     * @since 0.9.2
     */
    public void print() {
        executeScript("window.print();");
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
     * Returns the HTML content of a document, loaded in a browser.
     * 
     * @return the HTML content of a document, loaded in a browser.
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
     * Executes the specified JavaScript code on the currently loaded document.
     * This should not be called until after a <code>documentCompleted</code> 
     * event fired in <code>WebBrowserListener</code>.
     * <p>    	   	
     * For example, execute JavaScript to show an alert dialog:
     * <code>
     * <pre>
     *     // Show a JavaScript alert dialog.    
     *     WebBrowser webBrowser = new WebBrowser();
     *     webBrowser.executeScript("alert('Using executeScript')");
     * </pre>
     * </code>
     * 
     * @return the result of JavaScript execution, if there is any.
     * @see WebBrowserListener#documentCompleted
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
        WebBrowserUtil.enableDebugMessages(b);        
    }

    /**
     * Returns a <code>Status</code> object, which indicates the status of this
     * <code>WebBrowser</code> object.
     * 
     * @deprecated The <code>WebBrowser.Status</code> inner class is deprecated 
     *             as of release 0.9 of JDIC. Its APIs have been moved to this 
     *             <code>Browser</code> class. This API is no longer used, and 
     *             will be removed in a future release.
     * @see Status
     */
    public Status getStatus() {
        return new Status(this);        
    }

    /**
     * Returns the name of the embedded browser's native binary, which runs as 
     * a standalone native process.
     * 
     * @deprecated As of release 0.9 of JDIC. This method was unnecessarily 
     *             exposed and will be removed in a future release.
     */
    public static String getBrowserBinary () {
        return WebBrowserUtil.getEmbedBinaryName();
    }
    
    /**
     * Checks whether this <code>WebBrowser</code> object is initialized 
     * successfully.
     *
     * @return <code>true</code> if the <code>WebBrowser</code> object is 
     *         initialized successfully; otherwise, <code>false</code>.
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Checks whether this <code>WebBrowser</code> object's back command 
     * is enabled. 
     *
     * @return <code>true</code> if the WebBrowser can navigate to the 
     *         previous session history item, and <code>false</code> otherwise.
     * @see #back
     */
    public boolean isBackEnabled() {
        return isBackEnabled;
    }

    /**
     * Checks whether this <code>WebBrowser</code> object's forward command 
     * is enabled. 
     *
     * @return <code>true</code> if the WebBrowser can navigate to the 
     *         next session history item, and <code>false</code> otherwise.
     * @see #forward
     */
    public boolean isForwardEnabled() {
        return isForwardEnabled;
    }

    /**
     * Called before a navigation occurs.
     * <p>
     * A subclass can override this method to block the navigation or allow it 
     * to proceed.
     * 
     * @param url the URL to navigate to.
     * @return <code>false</code> will block the navigation from starting;
     *         <code>true</code> otherwise. By default, it returns <code>
     *         true</code>.
     */
    protected boolean willOpenURL(URL url) {
        if (null == url)
            return true;

        WebBrowserUtil.trace("URL = " + url.toString());
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
     * Called when a new window is to be created for loading a resource.
     * <p>
     * A subclass can override this method to block the creation of a new 
     * window or allow it to proceed. 
     * 
     * @return <code>false</code> will block the creation of a new window;  
     *         <code>true</code> otherwise. By default, it returns <code>
     *         true</code>.
     */
    protected boolean willOpenWindow() {
        return true;
    }

    int getInstanceNum() {
        return instanceNum;
    }

    /**
     * Waits for a result returned from the native embedded browser.
     * <p>
     * This method is called by methods requiring a return value, such as
     * getURL, getContent, executeScript.
     */
    private boolean waitForResult() {
        if (! isInitialized) {
            WebBrowserUtil.trace("You can't call this method before " +
                    "WebBrowser is initialized!");
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
    
    /**
     * An inner class which is used for retrieving the WebBrowser's properties,
     * such as the initialization status, back and forward status.
     * 
     * @deprecated As of release 0.9 of JDIC. Its APIs have been moved to 
     *             <code>org.jdesktop.jdic.browser.WebBrowser</code> class. 
     *             Will be removed in a future release.
     */
    public static class Status {
        WebBrowser webBrowser;

        Status (WebBrowser curWebBrowser) {
            webBrowser = curWebBrowser;
        }

        /**
         * Checks whether this <code>WebBrowser</code> object is initialized 
         * successfully.
         * 
         * @deprecated As of release 0.9 of JDIC, replaced by 
         *             <code>WebBrowser.isInitialized()</code>. 
         * @return <code>true</code> if the <code>WebBrowser</code> object is 
         *         initialized successfully; otherwise, <code>false</code>.
         */
        public boolean isInitialized() {
            return webBrowser.isInitialized();
        }

        /**
         * Checks whether this <code>WebBrowser</code> object's back command 
         * is enabled. 
         *
         * @deprecated As of release 0.9 of JDIC, replaced by 
         *             <code>WebBrowser.isBackEnabled()</code>.
         * @return <code>true</code> if the WebBrowser can navigate to the 
         *         previous session history item, and <code>false</code> 
         *         otherwise.
         * @see #back
         */
        public boolean isBackEnabled() {
            return webBrowser.isBackEnabled();
        }

        /**
         * Checks whether this <code>WebBrowser</code> object's forward command 
         * is enabled. 
         *
         * @deprecated As of release 0.9 of JDIC, replaced by 
         *             <code>WebBrowser.isForwardEnabled()</code>. 
         * @return <code>true</code> if the WebBrowser can navigate to the 
         *         next session history item, and <code>false</code> otherwise.
         * @see #forward
         */
        public boolean isForwardEnabled() {
            return webBrowser.isForwardEnabled();
        }        
    }

    class MyFocusListener implements FocusListener {
        public void focusGained(FocusEvent e) {
            WebBrowserUtil.trace("\nMyFocusListener: focusGained\n");
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_FOCUSGAINED);
        }

        public void focusLost(FocusEvent e) {
            WebBrowserUtil.trace("\nMyFocusListener: focusLost\n");
            eventThread.fireNativeEvent(instanceNum, 
                    NativeEventData.EVENT_FOCUSLOST);
        }
    }
}
