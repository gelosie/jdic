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
 *  The <code>GnomeDockService</code> interface is the contract for a
 *  native <code>FloatingDock</code> implementation.
 *
 */

package org.jdesktop.jdic.dock.internal.impl;

import org.jdesktop.jdic.dock.internal.DockService;
import java.awt.Dimension;
import java.awt.event.WindowListener;
import java.awt.LayoutManager;
import java.awt.Component;

public class GnomeDockService implements DockService {

    public void setVisible(boolean b)
    {
    }

    public boolean getVisible()
    {
	return true;
    }

    public void setSize(Dimension s)
    {
    }

    public Dimension getSize()
    {
	return new Dimension();
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
    }

    public int getLocation()
    {
	return 0;
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
