/*
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

package org.jdesktop.jdic.icons;

import java.awt.Image;
import java.awt.Toolkit;

import org.jdesktop.jdic.icons.spi.IconProvider;

/**
 * The <code>IconService</code> class provides several methods to access
 * system icons. It includes methods to retrieve a particular icon and to set and get icon themes.
 * The icons are obtained from the {@link IconProvider IconProvider}.
 */
public class IconService {

    private static IconProvider provider= IconProvider.getProvider();

    /**
     * Suppress default constructor for noninstantiability.
     */
    private IconService() {
    }

    /**
     * Return an icon of the requested size from the desktop.
     * If an icon of the requested size does not exist, return a scaled image.
     * The returned image is created by the default Toolkit.
     * This may return null if no icon of the given name exists in the desktop database.
     * @param name The name of the icon
     * @param size The requested size of the returned image
     * @return An icon of the requested size or null
     */
    public static Image getIcon(String name, int size) {
        return getIcon(name, size, Toolkit.getDefaultToolkit());
    }

    /**
     * Return an icon of the requested size from the desktop.
     * If an icon of the requested size does not exist, return a scaled image.
     * The returned image is created by the supplied Toolkit.
     * @param name The name of the icon
     * @param size The requested size of the returned image
     * @param toolkit The toolkit to use to create the image.
     * @return An icon of the requested size or null
     */
    public static Image getIcon(String name, int size, Toolkit toolkit) {
        return provider.getIcon(name, size, toolkit);
    }

    /**
     * Obtain the icon theme that is being used.
     * @return The icon theme name.  This may be null if themes are not used by the platform.
     */
    public static String getTheme() {
        return provider.getTheme();
    }

    /**
     * Set the icon theme to be used.
     * @param iconTheme The icon theme name.  This may be ignored if themes are not used by the platform.
     */
    public static void setTheme(String iconTheme) {
        provider.setTheme(iconTheme);
    }
}
