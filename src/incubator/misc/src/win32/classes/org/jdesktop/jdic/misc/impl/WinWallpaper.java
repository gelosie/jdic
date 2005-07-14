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
package org.jdesktop.jdic.misc.impl;

import java.io.File;

import org.jdesktop.jdic.misc.Wallpaper;


/**
 * This class sets a Windows desktop background image or wallpaper.
 * If you would like to support another
 * platform (say Gnome or KDE on Linux) then please join the JDIC
 * mailing list at http://jdic.dev.java.net/
 *
 *
 * @author Carl Dea
 *
 */
public class WinWallpaper
implements Wallpaper
{
    private static boolean _miscLibraryLoaded;

    static
    {
        try
        {
            System.loadLibrary("jdic_misc");
            _miscLibraryLoaded = true;
        }
        catch (UnsatisfiedLinkError e)
        {
            System.err.println("Couldn't find jdic Misc library in " + System.getProperty("java.library.path"));
            e.printStackTrace(System.err);
            _miscLibraryLoaded = false;
        }
    }

    private static Wallpaper _wallpaper;

    protected WinWallpaper()
    {
    }

    private static native long nativeSetWallpaper(String fileName, int mode);


    /**
     * @todo support other popular image formats.
     * @todo support common modes stretch, tile, and center.
     *
     */
    public void setBackground(String fileName, int mode)
    {
        System.out.println("calling native set wallpaper");
        long result = nativeSetWallpaper(fileName, mode);

    }


    public void setBackground(File fileName, int mode)
    {
        if (! _miscLibraryLoaded)
        {
            throw new RuntimeException("jdic_misc.dll not loaded");
        }

        setBackground(fileName.getAbsoluteFile().toString(), mode);
     }


    public static Wallpaper newInstance()
    {
        if (_wallpaper == null)
        {
            return new WinWallpaper();
        }

        return _wallpaper;
    }
}
