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

package org.jdesktop.jdic.tray;


import org.jdesktop.jdic.tray.internal.ServiceManager;
import org.jdesktop.jdic.tray.internal.SystemTrayService;


/**
 *  The <code>SystemTray</code> class represents the System Tray for a desktop.
 *  For any desktop there may be more than one instance of the System
 *  Tray but this is most commonly not the case. Typically a desktop
 *  will have one SystemTray. On Gnome and Windows this tray is 
 *  referred to as the Notification Area. 
 * 
 */

public class SystemTray {

    static SystemTrayService sts = (SystemTrayService) 
        ServiceManager.getService(ServiceManager.SYSTEM_TRAY_SERVICE);

    int trayIndex;

    /**
     * SystemTray constructor
     *
     */
    private SystemTray() {}

    private SystemTray(int index) {
        trayIndex = index;
    }

    /**
     * Gets the default SystemTray for the desktop.
     * @return SystemTray 
     */
    public static SystemTray getDefaultSystemTray() {
        return new SystemTray(0);
    }

    /**
     * Adds a <code>TrayIcon</code> to the SystemTray
     * @param trayIcon
     */
    public void addTrayIcon(TrayIcon trayIcon) {
        if (sts != null) {
            sts.addTrayIcon(trayIcon, trayIcon.getTrayIconService(), trayIndex);
        }
    }

    /**
     * Removes the specified <code>TrayIcon</code> from the SystemTray
     * @param trayIcon
     */
    public void removeTrayIcon(TrayIcon trayIcon) {
        if (sts != null) {
            sts.removeTrayIcon(trayIcon, trayIcon.getTrayIconService(),
                    trayIndex);
        }
    }
}
