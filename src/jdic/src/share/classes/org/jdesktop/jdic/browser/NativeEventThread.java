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
import java.awt.*;
import javax.swing.SwingUtilities;
import java.security.*;
import java.io.*;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

/**
 * An internal class for dealing with the communication between WebBrowser 
 * & native browser.
 *
 * @see WebBrowser
 * 
 * @author Kyle Yuan
 * @version 0.1, 03/07/17
 */ 
class NativeEventThread extends Thread
{
    private Vector webBrowsers = new Vector();
    private Vector nativeEvents = new Vector();
    private Process nativeBrowser;

    boolean eventRetBool;
    String eventRetString;

    MsgClient messenger = new MsgClient();

    NativeEventThread() {
        super("EventThread");
    }

    void attachWebBrowser(WebBrowser webBrowser) {
        int instanceNum = webBrowser.getInstanceNum();
        if (instanceNum >= webBrowsers.size()) {
            webBrowsers.setSize(instanceNum + 1);
        }
        webBrowsers.set(instanceNum, webBrowser);
    }

    public void run() {
        // create native browser process
        try {
            if (WebBrowserUtil.getEmbedBinaryName() == null) { 
                setBrowsersInitFailReason("Embedding browser binary is not set");
                WebBrowserUtil.trace("Embedding browser binary is not set, " +
                        "system exit");
                return;
            }     
            String jvmVendor = System.getProperty("java.vm.vendor");
            if (WebBrowserUtil.getEmbedBinaryName().endsWith("IeEmbed.exe") 
                    && jvmVendor.startsWith("Sun"))
                WebBrowserUtil.nativeSetEnv();
            final String cmd = WebBrowserUtil.getEmbedBinaryName() + " -port=" 
                + messenger.getPort();
            WebBrowserUtil.trace("Executing " + cmd);
            AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws IOException {
                        nativeBrowser = Runtime.getRuntime().exec(cmd);
                        new StreamGobbler(nativeBrowser.getErrorStream()).start();
                        new StreamGobbler(nativeBrowser.getInputStream()).start();
                        return null;
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            setBrowsersInitFailReason("Can't find native browser");
            System.out.println("Can't execute native browser. Error message is: " 
                    + e.getCause().getMessage());
            return;
        }
     
        // create socket client and connect to socket server
        try {
            AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws Exception {
                        messenger.connect();
                        return null;
                    }
                }
            );
        } catch (PrivilegedActionException e) {          
            System.out.println("Can't connect to native browser. Error " +
                    "message is: " + e.getCause().getMessage());
            setBrowsersInitFailReason("Can't connect to native browser");
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
                WebBrowserUtil.trace("Native browser died.");
                return;
            } catch (IllegalThreadStateException e) {
            }

            try {
                processEvents();
            } catch (Exception e) {
                WebBrowserUtil.trace("Exception occured when processEvent: " 
                        + e.getMessage());
                return;
            }

            try {
                messenger.portListening();
                processIncomingMessage(messenger.getMessage());
            } catch (Exception e) {
                WebBrowserUtil.trace("Exception occured when portListening: " 
                        + e.getMessage());
                return;
            }
        }
    }

    private WebBrowser getWebBrowserFromInstance(int instance) {
        try {
            return (WebBrowser) webBrowsers.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    private void notifyWebBrowser(int instance) {
        WebBrowser browser = getWebBrowserFromInstance(instance);
        if (null != browser) {
            synchronized(browser) {
                browser.notify();
            }
        }
    }

    private void processEvents() {
        int size = nativeEvents.size();
        for (int i = 0; i < size; ++i) {
            NativeEventData nativeEvent = (NativeEventData) nativeEvents.get(i);
            if (processEvent(nativeEvent)) {
                nativeEvents.removeElementAt(i);
                break;
            }
        }
    }

    private boolean processEvent(NativeEventData nativeEvent) {
        WebBrowser browser = getWebBrowserFromInstance(nativeEvent.instance);
        if (null == browser) {
            return true;
        }

        if (! browser.isInitialized() &&
            (nativeEvent.type != NativeEventData.EVENT_INIT &&
             nativeEvent.type != NativeEventData.EVENT_CREATEWINDOW)) {
            return false;
        }

        WebBrowserUtil.trace("Got event: type = " + nativeEvent.type + 
                " instance = " + nativeEvent.instance);

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
                break;
            case NativeEventData.EVENT_CREATEWINDOW:
                int nativeWindow = browser.getNativeWindow();
                if (0 == nativeWindow) {
                    WebBrowserUtil.trace("Can't get native window handle, " +
                            "please make sure the env variable JAVA_HOME has " +
                            "been set.");
                }
                else {
                    msg += nativeWindow;
                    messenger.sendMessage(msg);
                }
                break;
            case NativeEventData.EVENT_SET_BOUNDS:
                msg += nativeEvent.rectValue.x + "," +
                       nativeEvent.rectValue.y + "," +
                       nativeEvent.rectValue.width + "," +
                       nativeEvent.rectValue.height;
                messenger.sendMessage(msg);
                break;
            case NativeEventData.EVENT_NAVIGATE:
            case NativeEventData.EVENT_SETCONTENT:
            case NativeEventData.EVENT_EXECUTESCRIPT:                
                msg += nativeEvent.stringValue;
                messenger.sendMessage(msg);
                break;
        }

        return true;
    }

    static NativeEventData parseIncomingMessage(String msg) {
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
        }
        else {
            eventType = Integer.parseInt(msg.substring(pos1 + 1, pos2));
            if (pos2 + 1 < msg.length())
                stringValue = msg.substring(pos2 + 1);
        }

        return new NativeEventData(instance, eventType, stringValue);
    }

    private void processIncomingMessage(String msg) {
        NativeEventData eventData = parseIncomingMessage(msg);
        if (eventData == null) 
            return;

        WebBrowserUtil.trace("Got event from browser " + eventData.instance 
                + ", " + eventData.type + ", " + eventData.stringValue);

        if (WebBrowserEvent.WEBBROWSER_INIT_FAILED == eventData.type) {
            setBrowsersInitFailReason(eventData.stringValue);
            return;
        }

        if (eventData.instance < 0) {
            return;
        }

        if (WebBrowserEvent.WEBBROWSER_RETURN_URL == eventData.type ||
            WebBrowserEvent.WEBBROWSER_GETCONTENT == eventData.type ||
            WebBrowserEvent.WEBBROWSER_EXECUTESCRIPT == eventData.type) {
            eventRetString = eventData.stringValue;
            notifyWebBrowser(eventData.instance);
            return;
        }

        // anonymous inner class can only access final local variable
        final WebBrowser browser = getWebBrowserFromInstance(eventData.instance);
        if (null == browser) {
            return;
        }
        
        if (WebBrowserEvent.WEBBROWSER_INIT_WINDOW_SUCC == eventData.type) {
            browser.setInitialized(true);
            browser.setInitFailureMessage("");
            return;
        }

        final WebBrowserEvent event = new WebBrowserEvent(browser, 
                eventData.type, eventData.stringValue);

        Runnable dispatchEvent = new Runnable() {
            public void run() {
                browser.dispatchWebBrowserEvent(event);
            }
        };

        try {
            SwingUtilities.invokeLater(dispatchEvent);
        } catch (Exception e) {
            WebBrowserUtil.trace("Exception occured when invokeLater. " +
                    "ErrMsg is " + e.getMessage());
        }
    }

    synchronized void fireNativeEvent(int instance, int type) {
        nativeEvents.addElement(new NativeEventData(instance, type));
    }

    synchronized void fireNativeEvent(int instance, int type, 
            Rectangle rectValue) {
        nativeEvents.addElement(new NativeEventData(instance, type, rectValue));
    }

    synchronized void fireNativeEvent(int instance, int type, 
            String stringValue) {
        nativeEvents.addElement(
                new NativeEventData(instance, type, stringValue));
    }

    void setBrowsersInitFailReason(String msg) {
        ((WebBrowser)webBrowsers.elementAt(0)).setInitFailureMessage(msg);
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

} // end of class NativeEventThread
