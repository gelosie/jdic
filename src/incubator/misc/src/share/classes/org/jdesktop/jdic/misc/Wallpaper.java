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
package org.jdesktop.jdic.misc;

import java.io.File;


/**
 * Sets the desktop back wallpaper.
 *
 * @author Carl Dea
 *
 */
public interface Wallpaper
{
    // @TODO convert this to enum pattern.
    /** This mode will stretch the image to fit the entire desktop.*/
    public static final int STRETCH = 1;

    /** This mode will center the image on the desktop.*/
    public static final int CENTER = 2;

    /** This mode will tile the image to fit the entire desktop.*/
    public static final int TILE = 8;

    /** These are the supported modes.*/
    public static final int[] MODES = { STRETCH, CENTER, TILE };
    
    /** These are the supported modes.*/
    public static final String[] MODE_NAMES = { "Stretch", "Center", "Tile"};
    
    /**
     * This method will take a file name and mode to render the 
     * desktop wallpaper.
     * 
     * @param fileName - The absolute file path.
     * @param mode - Most desktops support Center, Tile, and Stretch
     *
     */
    public void setBackground(String fileName, int mode);

    /**
     * This method will take a file name and mode to render the 
     * desktop wallpaper.
     * 
     * @param fileName - The absolute file path.
     * @param mode - Most desktops support Center, Tile, and Stretch
     *
     */
    public void setBackground(File fileName, int mode);
    
}
