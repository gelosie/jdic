// Copyright © 2004 Sun Microsystems, Inc. All rights reserved. Use is
// subject to license terms.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the Lesser GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

package org.jdesktop.jdic.screensaver;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.MemoryImageSource;
import javax.swing.SwingUtilities;

/**
 * Context object with information about the screensaver.
 *
 * @author Mark Roth
 */
public class ScreensaverContext {
    
    /** The component we're drawing to */
    private Component component;
    
    /** The settings for this screen saver */
    private ScreensaverSettings settings = new ScreensaverSettings();
    
    /** 
     * Returns the component to be rendered to.
     *
     * @return Value of property component.
     */
    public Component getComponent() {
        return component;
    }
    
    /** 
     * Sets the component to be rendered to.
     *
     * @param component New value of property component.
     */
    public void setComponent( Component component ) {
        this.component = component;
        component.setBackground( Color.black );
        
        if( isFullScreen() ) {
            setTransparentCursor( component );
        }
        
        // Add mouse motion event to exit when mouse moves.
        // The screensaver is hidden which causes the next move of
        // the mouse to be passed to Windows, which causes a clean
        // exit of the screensaver.
        component.addMouseMotionListener(
            new MouseMotionListener() {
                public void mouseDragged(MouseEvent e) {
                }
                public void mouseMoved(MouseEvent e) {
                    SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                if(isFullScreen()) {
                                    ScreensaverContext.this.component.
                                        setVisible(false);
                                }
                            }
                        }
                    );
                }
            });
    }
    
    /**
     * Sets an option for this screensaver.  This method is called 
     * from the native layer for each screen saver option set.
     *
     * @param key The key to set
     * @param value The value to set
     */
    public void option( String key, String value ) {
        this.settings.setProperty( key, value );
    }
    
    /**
     * Loads the screensaver options from the user's home directory.  This
     * method is called from the native layer if the underlying 
     * screensaver APIs do not support automatic setting persistence.
     *
     * @param screensaverName The name of the screensaver, to load 
     *     settings from
     */
    public void loadOptions(String screensaverName) {
        this.settings.loadSettings(screensaverName);
    }
    
    /**
     * Returns the screensaver settings.
     *
     * @return The current screensaver settings (not a copy).
     */
    public ScreensaverSettings getSettings() {
        return this.settings;
    }

    /**
     * Returns true if the screensaver is in full-screen mode
     * or false if it is in preview mode.
     */
    public boolean isFullScreen() {
        Dimension fullSize = Toolkit.getDefaultToolkit().getScreenSize();
        return component.getSize().equals(fullSize);
    }
    
    /**
     * Hide the mouse cursor on this component.
     *
     * @param component the component to hide the mouse cursor on
     */
    private void setTransparentCursor( Component component ) {
        // Set transparent cursor:
        int[] pixels = new int[16 * 16];
        Image image = Toolkit.getDefaultToolkit().createImage(
            new MemoryImageSource(16, 16, pixels, 0, 16) );
        Cursor transparentCursor =
            Toolkit.getDefaultToolkit().createCustomCursor( image, 
            new Point( 0, 0 ), "invisiblecursor" );
        component.setCursor( transparentCursor );
    }
    
}
