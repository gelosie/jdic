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
import sun.awt.EmbeddedFrame;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.peer.ComponentPeer;
import java.awt.Toolkit;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.HashMap;


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

    public GnomeTrayAppletService() {
        init();
    }
    
    native long createAppletWindow();

    native long getWidget(long window, int widht, int height, int x, int y);
    native void adjustSizeHints (long window, int width, int height);

    long window_id;
   
    EmbeddedFrame createEmbeddedFrame(long window) {
        EmbeddedFrame ef = null;
        String version = System.getProperty("java.version");
        String os = System.getProperty("os.name");

        // System.out.println("version = " + version);
        // System.out.flush();

        if ((version.indexOf("1.5") == -1) || (os.equals("SunOS"))) {
            // 1.4.2 or older JVM, use MAWT !
            long w = getWidget(window, 400, 400, 0, 0);
            // System.out.println("Widget w = " + w);
            Class clazz = null;

            try {
                clazz = Class.forName("sun.awt.motif.MEmbeddedFrame");
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Constructor constructor = null;

            try {
                constructor = clazz.getConstructor(new Class[] {int.class});
            } catch (Throwable e1) {
                try {
                    constructor = clazz.getConstructor(new Class[] {long.class});
                } catch (Throwable e2) {
                    e1.printStackTrace();
                }
            }
            Object value = null;

            try {
                value = constructor.newInstance(new Object[] {new Long(w)});
            } catch (Throwable e) {
                e.printStackTrace();
            }
            ef = (EmbeddedFrame) value;
        } else {   
            // 1.5  JVM decide on which EmbeddedFrame to use 
            Toolkit toolkit = Toolkit.getDefaultToolkit();

            // System.out.println("toolkit = " + toolkit);
            // System.out.flush();
            if (toolkit instanceof sun.awt.motif.MToolkit) {
                Class clazz = null;

                try {
                    clazz = Class.forName("sun.awt.motif.MEmbeddedFrame");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                Constructor constructor = null;

                try {
                    constructor = clazz.getConstructor(new Class[] {int.class});
                } catch (Throwable e1) {
                    try {
                        constructor = clazz.getConstructor(new Class[] {long.class});
                    } catch (Throwable e2) {
                        e1.printStackTrace();
                    }
                }
                Object value = null;

                try {
                    value = constructor.newInstance(new Object[] {new Long(window)});
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                ef = (EmbeddedFrame) value;

            } else {
                Class clazz = null;

                try {
                    clazz = Class.forName("sun.awt.X11.XEmbeddedFrame");
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                Constructor constructor = null;

                try {
                    constructor = clazz.getConstructor(new Class[] {int.class});
                } catch (Throwable e1) {
                    try {
                        constructor = clazz.getConstructor(new Class[] {long.class});
                    } catch (Throwable e2) {
                        e1.printStackTrace();
                    }
                }
                Object value = null;

                try {
                    value = constructor.newInstance(new Object[] {new Long(window)});
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                ef = (EmbeddedFrame) value;
            }
        } 
        return ef; 
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
        dispose(getWindow());
    }
 
    native void dispose(long window_id); 

}
