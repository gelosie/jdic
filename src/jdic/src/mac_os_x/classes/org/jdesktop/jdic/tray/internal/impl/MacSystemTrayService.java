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
 *  The <code>MacSystemTrayService</code> is the Mac OS implementation of the <code>SystemTrayService
 *  </code> interface.
 *  @author Rob Ross <robross@earthlink.net>
 *
 */
package org.jdesktop.jdic.tray.internal.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jdesktop.jdic.tray.TrayIcon;
import org.jdesktop.jdic.tray.internal.SystemTrayService;
import org.jdesktop.jdic.tray.internal.TrayIconService;


/**
 * There is only one Status Bar on the Mac, and this Java class represents that native peer,
 * so this is a singleton. Use the factory method getInstance to get the single instance of this
 * class.
 */
public class MacSystemTrayService implements SystemTrayService {

    private static MacSystemTrayService instance;

    /**
     * Contains all the Status Items currently displayed in the Status Bar.
     * When a Status Item (tray icon) is removed, the native resources it holds are freed; but the
     * owner of the tray icon may add the tray icon again and those native resources will be
     * re-allocated.
     */
    private Set trayIcons = new HashSet();


    private MacSystemTrayService()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            public void run()
            {
                dispose();
            }
        });
    }

    public static MacSystemTrayService getInstance()
    {
        if (instance == null)
        {
            instance = new MacSystemTrayService();
        }
        return instance;
    }

    public void addNotify() {}

    private class AnimationThread extends Thread
    {
        private boolean stop = false;

        public AnimationThread()
        {
            super("TrayIconAnimationThread");
            setDaemon(true);
            setPriority(Thread.NORM_PRIORITY - 1);
        }

        public void run()
        {
            while (! stop)
            {
                //notify all the TrayIcons in the tray so they can redraw themselves if needed
                synchronized (MacSystemTrayService.this)
                {
                    Iterator itar = trayIcons.iterator();

                    while (itar.hasNext())
                    {
                        MacTrayIconService trayIcon = (MacTrayIconService) itar.next();
                        trayIcon.updateNotify();
                    }
                }

                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    //don't care
                }

            };
        }
    }

    private AnimationThread animationThread;

    public synchronized void addTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex)
    {
        if (! trayIcons.contains(tis))
        {
            trayIcons.add(tis);
            MacTrayIconService trayIcon = (MacTrayIconService) tis;
            trayIcon.addNotify();
        }

        //lazily create the animation thread
        if (animationThread == null)
        {
            animationThread = new AnimationThread();
            animationThread.start();
        }
    }

    public synchronized void removeTrayIcon(TrayIcon ti, TrayIconService tis, int trayIndex)
    {
        if (trayIcons.contains(tis))
        {
            trayIcons.remove(tis);
            MacTrayIconService trayIcon = (MacTrayIconService) tis;
            trayIcon.remove(); //removes from Status Bar and frees native resources
        }
    }

    /**
     * Removes all tray icons currently attached to the Status Bar (system tray), and frees
     * their native resources
     */
    private synchronized void dispose()
    {
        if (! trayIcons.isEmpty())
        {
            Iterator itar = trayIcons.iterator();
            while (itar.hasNext())
            {
                MacTrayIconService trayIcon = (MacTrayIconService) itar.next();
                trayIcon.dispose();
            }
            trayIcons.clear();
        }
        if (animationThread != null)
        {
            animationThread.stop = true;
        }
    }

    protected void finalize() throws Throwable
    {
        dispose();
        super.finalize();
    }



    static
    {
        System.loadLibrary("tray");   //filesystem name is libtray.jnilib
    }
}