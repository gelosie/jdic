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

import java.lang.reflect.Method;


/**
 * This factory class can return a supported default Wallpaper implementations.
 * If this factory does not support this your platform you may build your implementation
 * and pass the fully qualified name to the createWallpaper(String ImplClass) method.
 *
 *
 * @author Carl Dea
 */
public class WallpaperFactory
{
    /** Windows Implementation */
    private static final String WIN32 = "org.jdesktop.jdic.misc.impl.WinWallpaper";
    private static Wallpaper _wallpaper;

    private WallpaperFactory()
    {
    }

    /**
     * Sets and Returns a supported platform implementation of a Wallpaper.
     *
     * @return Wallpaper A supported platform implementation of a Wallpaper instance.
     */
    public static Wallpaper createWallpaper()
    {
        String os_name = null;

        if (getWallpaper() == null)
        {
            os_name = System.getProperty("os.name");
            System.out.println("os name = " + os_name);

            if (os_name.toLowerCase().startsWith("windows"))
            {
                return createWallpaper(WIN32);
            }
            else
            {
                System.err.println("Unknown Desktop Environment " + os_name);

                StringBuffer errMsg = new StringBuffer();
                errMsg.append("This desktop environment is not supported. Platform: ");
                errMsg.append(os_name).append(". Please try using ");
                errMsg.append(WallpaperFactory.class.getName()).append(".createWallpaper(String ImplClass)");
                throw new RuntimeException(errMsg.toString());
            }
        }

        return getWallpaper();
    }


    /**
     * This method will create and set a Wallpaper implementation.
     *
     * @param ImplClass The implementation class of a Wallpaper.
     * @return Wallpaper The Wallpaper instance.
     */
    public static Wallpaper createWallpaper(String ImplClass)
    {
        Wallpaper wallpaper = null;

        try
        {
            Class implClass = Class.forName(ImplClass);
            Method method = null;

            try
            {
                method = implClass.getMethod("newInstance", null);
                // get the singleton
                Object obj = method.invoke(null, null);
                wallpaper = (Wallpaper) obj;
            }
            catch (NoSuchMethodException nsme)
            {
                // user did not develop this method.
                // likely expected maybe there is a 
                // public constructor.
                wallpaper = (Wallpaper) implClass.newInstance();
            }
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();

            StringBuffer errMsg = new StringBuffer();
            errMsg.append("Unable to create an instance of " + ImplClass + " on Platform: ");
            errMsg.append(System.getProperty("os.name"));
            throw new RuntimeException(errMsg.toString(), ex);
        }

        setWallpaper(wallpaper);

        return wallpaper;
    }


    /**
     * @return Returns the wallpaper.
     */
    private static Wallpaper getWallpaper()
    {
        return _wallpaper;
    }


    /**
     * @param wallpaper The wallpaper to set.
     */
    private static void setWallpaper(Wallpaper wallpaper)
    {
        _wallpaper = wallpaper;
    }
}
