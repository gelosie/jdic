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

package org.jdesktop.jdic.dock;


import org.jdesktop.jdic.dock.internal.ServiceManager;
import org.jdesktop.jdic.dock.internal.DockService;

import java.awt.*;
import java.awt.event.*;


/**
 */


public class FloatingDock {

    DockService ds;

    /** 
     * Position the dock window at the top of the specified display
     */
    public static final int TOP = 0;
    /** 
     * Position the dock window at the bottom of the specified display
     */
    public static final int BOTTOM = 1;
    /** 
     * Position the dock window at the right side of the specified display
     */
    public static final int RIGHT = 2;
    /** 
     * Position the dock window at the left side of the specified display
     */
    public static final int LEFT = 3;

    /**
     * Floating Dock constructor - creates a FloatingDock.
     *
     */
    public FloatingDock() {
        ds = (DockService)
            ServiceManager.getService(ServiceManager.DOCK_SERVICE);
        if (ds == null) {
            throw new RuntimeException("Unable to initialize dock library");
        }
    }

    /**
     * Floating Dock constructor - creates a FloatingDock.
     *
     * @param location integer location argument
     */
    public FloatingDock(int location) {
        this();
        setLocation(location);
    }

    /**
     *  Set the visibilty of the Floating Dock window
     *
     * @param visible visibility
     *
     */
    public void setVisible(boolean visible) {
        ds.setVisible(visible); 
    }
 
    /**
     *  Get the visibility of the Floating Dock window
     *
     * @return boolean visibility
     *
     */
    public boolean getVisible() {
       return ds.getVisible();
    }

    /**
     *  Set the size of the Floating Dock window
     *
     * @param size size
     *
     */
    public void setSize(Dimension size) {
        ds.setSize(size); 
    }

    /**
     *  Get the size of the Floating Dock window
     *
     * @return Dimension size
     *
     */
    public Dimension getSize() {
       return ds.getSize();
    }

 
    /**
     *  Add a component to the Floating Dock window
     *
     * @param component component.
     *
     */
 
    public void add(Component component) {
        ds.add(component);
    }

    /**
     *  Remove a component from the Floating Dock window
     *
     * @param component component.
     *
     */
 
    public void remove(Component component) {
        ds.remove(component);
    }

 
    /**
     *  Set the layout of the Floating Dock window
     *
     * @param layout layout
     *
     */
    public void setLayout(LayoutManager layout) {
        ds.setLayout(layout);
    }
 
    /**
     *  Get the layout of the Floating Dock window
     *
     * @return layout
     *
     */
    public LayoutManager getLayout() {
        return ds.getLayout();
    }
    

    /**
     *  Set the location of the Floating Dock window
     *
     * @param location location
     *     
     *  Please refer to location constants.
     */
    public void setLocation(int location) {
        ds.setLocation(location); 
    }

    /**
     *  Get the location of the Floating Dock window
     *
     * @return location
     *     
     *  Please refer to location constants.
     */
    public int getLocation() {
       return ds.getLocation(); 
    }



    /**
     *  Set the auto hide property of the Floating Dock window
     *
     * @param hide boolean value of auto hide property
     */
    public void setAutoHide(boolean hide) {
        ds.setAutoHide(hide);  
    }

    /**
     *  Get the auto hide property of the Floating Dock window
     *
     * @return autohide
     */
    public boolean getAutoHide() {
       return ds.getAutoHide();  
    }

    /**
     *  Add a WindowListener for the Floating Dock window
     *
     * @param listener listener
     */
    public void addWindowListener(WindowListener listener) {
        ds.addWindowListener(listener);
    }

    /**
     *  Remove a WindowListener from the Floating Dock window
     *
     * @param listener listener
     */
    public void removeWindowListener(WindowListener listener) {
        ds.removeWindowListener(listener);
    }

    

}
