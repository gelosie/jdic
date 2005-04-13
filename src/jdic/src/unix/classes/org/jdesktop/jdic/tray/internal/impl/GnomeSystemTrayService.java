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
 *  The <code>GnomeSystemTrayService</code> interface is the contract for a 
 *  native <code>SystemTray</code> implementation.
 *
 */
package org.jdesktop.jdic.tray.internal.impl;


import org.jdesktop.jdic.tray.internal.SystemTrayService;
import org.jdesktop.jdic.tray.internal.TrayIconService;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;
import java.awt.Toolkit;
import sun.awt.EmbeddedFrame;


public class GnomeSystemTrayService implements SystemTrayService {

    public GnomeSystemTrayService() {}
     
    public void addNotify() {}

    public void addTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex) {
        tis.addNotify();
    }

    // public void addTrayApplet(TrayApplet ta, int trayIndex) {}

    public void removeTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex) {
        ((GnomeTrayIconService)tis).remove();
    }

    // public void removeTrayApplet(TrayApplet ta, int trayIndex){}
  
    static native boolean  locateSystemTray(); 
    static Thread display_thread;
   
    static native void eventLoop();
    
    static {
        //
        // Very important, we need to force AWT to get loaded before the
        // native library libtray.so is loaded. Otherwise AWT will fail.
        Toolkit t = Toolkit.getDefaultToolkit();

        t.sync();

        System.loadLibrary("tray");
        GnomeSystemTrayService.initNative(System.getProperty("java.home"));
        if (!locateSystemTray()) {
            throw new Error("System Tray not Found !");
        }

        display_thread = new Thread(new Runnable() {
            public void run() {
                eventLoop();
            }
        });
        display_thread.setDaemon(true);
        
        display_thread.start();

    }

    static native void dockWindow(long window);
    static native void initNative(String javaHome);

}
