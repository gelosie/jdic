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
 *  The <code>WinSystemTrayService</code> interface is the <code>SystemTray
 *  </code> implementation.
 *
 */
package org.jdesktop.jdic.tray.internal.impl;


import org.jdesktop.jdic.tray.internal.SystemTrayService;
import org.jdesktop.jdic.tray.internal.TrayIconService;
import org.jdesktop.jdic.tray.TrayIcon;
import java.awt.Toolkit;


public class WinSystemTrayService implements SystemTrayService {

    public WinSystemTrayService() { init(); }
     
    public void addNotify() {}

    public void addTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex) {
        WinTrayIconService trayIcon = (WinTrayIconService) tis;

        trayIcon.addNotify();
    }

    public void removeTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex) {
        WinTrayIconService trayIcon = (WinTrayIconService) tis;

        trayIcon.remove();
    }
   
    static Thread display_thread;
    
    private static Object lock = new Object();
    private static boolean inited = false;
    private static native void eventLoop();
    private static native void initTray();
    private static synchronized void init(){
    	if(inited)
    		return;
        //
        // Very important, we need to force AWT to get loaded before the
        // native library libtray.so is loaded. Otherwise AWT will fail.
        Toolkit t = Toolkit.getDefaultToolkit();

        t.sync();

        System.loadLibrary("tray");
        
        display_thread = new Thread(new Runnable() {
            public void run() {
            	synchronized(lock){
            		initTray();
            		inited = true;
            		lock.notify();
            	}
            	eventLoop();
            }
        });
		display_thread.start();
        synchronized(lock){
        	while(!inited){
	        	try{
	        		lock.wait(100);
	        	}catch(InterruptedException e){
	        		// ignore interrupted exception
	        	}
        	}
        }
    }

}
