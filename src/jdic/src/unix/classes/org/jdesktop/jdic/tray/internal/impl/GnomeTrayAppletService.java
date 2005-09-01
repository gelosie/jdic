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

package org.jdesktop.jdic.tray.internal.impl;


import org.jdesktop.jdic.tray.internal.TrayAppletService;
import sun.awt.EmbeddedFrame;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.peer.ComponentPeer;
import java.awt.Toolkit;
import java.awt.Panel;
import java.awt.Dimension;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;


/**
 * The <code>GnomeTrayAppletService</code> interface is the contract for a 
 * Gnome TrayIcon implementation.
 *
 */


public class GnomeTrayAppletService implements TrayAppletService {

    static HashMap winMap = new HashMap(); 
    
    EmbeddedFrame frame;
    ComponentPeer peer;
    Panel panel;

    int x;
    int y;
    int width;
    int height;  
    static {
    	System.loadLibrary("tray");
    	GnomeSystemTrayService.initNative(System.getProperty("java.home"));
    }

    public GnomeTrayAppletService() {
        init();
    }
    
    native long createAppletWindow();

    native long getWidget(long window, int widht, int height, int x, int y);
    native void adjustSizeHints (long window, int width, int height);

    long window_id;
   
    EmbeddedFrame createEmbeddedFrame(long window) {
        String className = null;
        Class clazz = null;
        Constructor constructor = null;
        long w = window;
        EmbeddedFrame frame = null;
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        
        if(toolkit instanceof sun.awt.motif.MToolkit){
            w = getWidget(window, 400, 400, 0, 0);
            className = "sun.awt.motif.MEmbeddedFrame";
        }else{ // sun.awt.X11.XToolkit
            className = "sun.awt.X11.XEmbeddedFrame";
        }
        try {
            clazz = Class.forName(className);
            constructor = clazz.getConstructor(new Class[]{long.class});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            try {
                constructor = clazz.getConstructor(new Class[]{int.class});
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            }
        }
        if(constructor != null){
            try {
                frame = (EmbeddedFrame)constructor.newInstance(new Object[]{new Long(w)});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return frame;
    }

    void init() {
        window_id = createAppletWindow(); 
        // System.out.println("init: window " + window_id );

        // Add a mapping in the window map.
        synchronized (winMap) {
            winMap.put(new Long(window_id), this);
        }
        frame = createEmbeddedFrame(window_id);
        peer = frame.getPeer();
        width = 40;
        height = 46;
        frame.setSize(width, height);
        frame.setVisible(true);
            
    }

    long getWindow() {
        return window_id;
    }

    EmbeddedFrame getFrame() {
        return frame;
    }

    public void add(Component a) {
        frame.add(a);
    }

    public Graphics getGraphics() {
        return frame.getGraphics();
    }

    public void   	setVisible(boolean b) {
        frame.setVisible(b);
    }

    public void  reshape(int x, int y, int width, int height) {
        adjustSizeHints(getWindow(),width,height);
        frame.reshape(x, y, width, height);
    }

    public ComponentPeer getPeer() {
        return peer;
    }

    void configureWindow(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
        frame.setSize(width, height);
        frame.validate();
     //   System.out.println("configureWindow: frame = " + frame + " configure width = " + width + " height = " + height);
    }

    public Dimension getAppletSize() {
        return new Dimension(width, height);
    }

    static void configureNotify(long window, int x, int y, int w, int h) {
        GnomeTrayAppletService gas; 

      //  System.out.println("configureNotify: window =" + window );
        synchronized (winMap) {
            gas = (GnomeTrayAppletService) winMap.get(new Long(window));
        }
        if (gas != null) {
            gas.configureWindow(x, y, w, h);
        }
    }
   
    public void remove() {
        // remove mapping in the window map.
        synchronized (winMap) {
            winMap.remove(new Long(getWindow()));
        }
        frame.dispose();
        dispose(getWindow());
    }
    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	Iterator wins = winMap.keySet().iterator();
            	while(wins.hasNext())
            		dispose(((Long)wins.next()).longValue());
            }
        });
    }
 
     static native void dispose(long window_id);
}
