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
 * The <code>TrayIconService</code> interface is the contract for a native 
 * TrayIcon implementation.
 *
 */

package org.jdesktop.jdic.tray.internal;


import javax.swing.JPopupMenu;
import javax.swing.Icon;
import java.awt.event.ActionListener;
import java.awt.Point;


public interface TrayIconService {
    public void addNotify();
    public void setPopupMenu(JPopupMenu menu);
    public void setIcon(Icon i);
    /**
     * Set caption of the icon.
     * This is only meaningful for Mac os. For windows, it equals with setToolTip.
     * @param caption caption to set
     */
    public void setCaption(String caption);
    /**
     * Sets the tool tip of the icon.
     * For windows,it can have a maximum of 64 characters including the terminating NULL.
     * @param toolTip toolTip to set
     */
    public void setToolTip(String toolTip);
    public void setIconAutoSize(boolean b);
    public void addActionListener(ActionListener al);
    public void removeActionListener(ActionListener al);
    public Point getLocationOnScreen();
    /**
     * This method isn't supported by Mac os,UnsupportedOperationException will be thrown for it.
     * @param caption caption of the msg
     * @param text content of the msg
     * @param type type of the msg
     */
    public void showBalloonMessage(String caption, String text, int type);
    public void addBalloonActionListener(ActionListener al);
    public void removeBalloonActionListener(ActionListener al);
}
