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


import org.jdesktop.jdic.tray.internal.ServiceManager;


/**
 * The <code>ServiceManagerStub</code> class implements the particular
 * lookup of services. The request in ServiceManager to lookup services is 
 * delegated to this object.
 * 
 * @see org.jdesktop.jdic.tray.internal.ServiceManager
 */
public class ServiceManagerStub {

    /**
     * Suppress default constructor for noninstantiability.
     */
    private ServiceManagerStub() {}

    /**
     * Gets the requested service object according to the given service name.
     * 
     * @param serviceName the given service name.
     * @throws IllegalArgumentException if there is no approprate service according
     *         to the given service name, or UnsupportedOperationException if we've 
     *         got unsupported system mailer.
     * @return the requested service object.
     */
    public static Object getService(String serviceName) 
            throws IllegalArgumentException {
        if (serviceName.equals(ServiceManager.SYSTEM_TRAY_SERVICE)) {
            return new WinSystemTrayService();
        } else if (serviceName.equals(ServiceManager.TRAY_ICON_SERVICE)) {
            return new WinTrayIconService();
        } else {
            // Should never arrive here. 
            throw new IllegalArgumentException("The requested service is not supported.");
        }

    }
}
