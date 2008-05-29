/*
 * Copyright (C) 2008 Sun Microsystems, Inc. All rights reserved. Use is
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
package org.jdic.web.peer;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

/**
 * The peer interface are intended only for use in porting
 * the JDIC to other platforms or browsers. They are not intended 
 * for use by application developers, and JDICplus users should not 
 * implement peers nor invoke any of the peer methods directly on the peer
 * instances.
 * 
 * <code>BrComponentPeer</code> declares the common browser functionality
 * for manipulation from component level.
 */

public  interface BrComponentPeer {

    JComponent getCentralPanel();
    boolean isSelfPaint();
    boolean isNativeReady();
    /**
     * Switches the browser mode.
     * @param editable if <code>true</code> the browser provides the 
     * editable context (as MS Outlook mail body editor), <code>false</code> 
     * returns the browser to ordinal viewer mode (default value)
     */
    void setEditable(boolean editable);
    
    /**
     * Returns browser editor mode.
     * @return <code>true</code> if the browser provides the 
     * editable context (as MS Outlook mail body editor), <code>false</code> 
     * for the browser in ordinal viewer mode
     */
    boolean getEditable();    
    
    /**
     * Activates navigation to URL.
     */
    void acceptTargetURL();
    
    /**
     * Returns the browser URL from address bar.
     * @return actual document URL
     */
    String getURL();
    
    String getNativeHTML();
    String getNativeXHTML();
    String getNativeXHTML(boolean bWithUniqueID);
    
    void paintClientArea(Graphics g, int iPaintHint);
    void blockNativeInputHandler(boolean dropNativeAction);
    
    String execJS(String code);
    void reshape(int x, int y, int width, int height);
    void destroy();
    void validate();
    boolean hasFocus();
    void focusGain(boolean  bKeyboardFocus);
    long sendMouseEvent(MouseEvent e);
    
    /**
     * Process the action stack on browser thread to avoid deadlock with EDT.
     * Have to be called just before EDT action waiting in browser callback
     * @param flag reserved
     * @param busyState 
     * @return reserved
     */
    int setActionFiler(int flag, boolean busyState);
    void setEnabled(boolean enabled);
    void setVisible(boolean aFlag);
    long getNativeHandle();
    
    void onAddNotify();
    void onRemoveNotify();
}
