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

/**
 *  The <code>UnixDockService</code> interface is the contract for a
 *  native <code>FloatingDock</code> implementation.
 *
 */

package org.jdesktop.jdic.dock.internal.impl;

import org.jdesktop.jdic.dock.internal.DockService;
import java.awt.Dimension;
import java.awt.event.WindowListener;
import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Toolkit;
import sun.awt.EmbeddedFrame;
import java.lang.reflect.Constructor;
import org.jdesktop.jdic.dock.FloatingDock;

public class UnixDockService implements DockService {

    EmbeddedFrame frame;
    Dimension size;
    boolean visible = false;
    int location;
    static long window_id;
    static UnixDockService uds;

    native long createDockWindow();
    native long getWidget(long window, int widht, int height, int x, int y);
    native void adjustSize (long window, int width, int height);
    native void adjustSizeAndLocation(long window, int width, int height, int location);
    static native boolean  locateDock();
    static native void eventLoop();
    static native void mapWindow(long window, boolean b);

    static Thread display_thread;

    static {
        //
        // Very important, we need to force AWT to get loaded before the
        // native library libtray.so is loaded. Otherwise AWT will fail.
        Toolkit t = Toolkit.getDefaultToolkit();

        t.sync();

        System.loadLibrary("floatingdock");
        if (!locateDock()) {
            throw new Error("Dock not Found !");
        }

        display_thread = new Thread(new Runnable() {
            public void run() {
                eventLoop();
            }
        });

        display_thread.start();

    }

    public UnixDockService()
    {
	uds = this;
	init();
    }

    void init()
    {
	window_id = createDockWindow();
	frame = createEmbeddedFrame(window_id);
    }

    EmbeddedFrame createEmbeddedFrame(long window) {
        EmbeddedFrame ef = null;
        String version = System.getProperty("java.version");
        String os = System.getProperty("os.name");

        if ((version.indexOf("1.5") == -1) || (os.equals("SunOS"))) {
            // 1.4.2 or older JVM, use MAWT !
            long w = getWidget(window, 400, 400, 0, 0);
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


    public void setVisible(boolean b)
    {
	visible = b;
	mapWindow(window_id, b);	
	frame.setVisible(b);
    }

    public boolean getVisible()
    {
	return visible;
    }

    long getWindow() {
        return window_id;
    }

    public void setSize(Dimension s)
    {
	size = s;
	adjustSize(getWindow(), size.width, size.height);
        frame.setSize(size.width, size.height);
	frame.validate();
    }

    void configureWindow(int x, int y, int w, int h) 
    {
	size = new Dimension(w, h);
        frame.setSize(w, h);
        frame.validate();
     //   System.out.println("configureWindow: frame = " + frame + " configure width = " + width + " height = " + height);
    }

    static void configureNotify(long window, int x, int y, int w, int h) 
    {
        //  System.out.println("configureNotify: window =" + window );
	if (window == window_id)
	{
            uds.configureWindow(x, y, w, h);
        }
    }

    public Dimension getSize()
    {
	return size;
    }

    public void add(Component c)
    {
    }

    public void remove(Component c)
    {
    }

    public void setLayout(LayoutManager l)
    {
    }

    public LayoutManager getLayout()
    {
	return null;
    }


    public void setLocation(int l)
    {
	location = l;
	adjustSizeAndLocation(getWindow(), size.width, size.height, location);
    }

    public int getLocation()
    {
	return location;
    }


    public void setAutoHide(boolean b)
    {
    }

    public boolean getAutoHide()
    {
	return true;
    }

    public void addWindowListener(WindowListener l)
    {
    }

    public void removeWindowListener(WindowListener l)
    {
    }
}
