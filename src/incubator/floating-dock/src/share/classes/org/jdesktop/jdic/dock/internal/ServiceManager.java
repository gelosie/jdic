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
 
package org.jdesktop.jdic.dock.internal;


import org.jdesktop.jdic.dock.internal.impl.ServiceManagerStub;


/**
 * The <code>ServiceManager</code> class provides static fields to refer to 
 * the available services, and static methods to get the approprate service 
 * objects with the given service name. This class is abstract and final and 
 * cannot be instantiated.
 * 
 * @see     ServiceManagerStub
 */
public class ServiceManager {
  
    /**
     * Constant name for looking up the SystemTray service object.
     */
    public static final String DOCK_SERVICE = "DockService";
    

    /**
     * Suppress default constructor for noninstantiability.
     */
    private ServiceManager() {}
  
    // Add the initialization code from package org.jdesktop.jdic.init.
    // To set the environment variables or initialize the set up for 
    // native libraries and executable files.
    // Note: below code is commented out at the moment. As the Tray Icon native code
    //           for Unix is c code (Tray.c). But the initialization native code is c++ code
    //           (InitUtility.cpp). These two code are not combined together into the generated
    //           libtray.so native library for now. So the user has to set LD_LIBRARY_PATH
    //           to use the Tray Icon API, the .jar file path will not be added to 
    //           LD_LIBRARY_PATH by default.
/*
    static {
        try {
            JdicManager jm = JdicManager.getManager();
            jm.initShareNative();
        } catch (JdicInitException e){
            e.printStackTrace();
        }
    }
*/
      
    /**
     * Gets a service object with the given name. The given service name should be one 
     * of the pre-defined service names.
     * 
     * @param  serviceName the given service name.
     * @return the appropriate service object.
     * @throws NullPointerException if the given service name is null.
     */
    public static Object getService(String serviceName) 
            throws NullPointerException {
        if (serviceName == null) { 
            throw new NullPointerException("Service name is null.");
        }
        
        return ServiceManagerStub.getService(serviceName); 
    }
}
