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

package org.jdesktop.jdic.icons.spi;

import java.awt.Image;
import java.awt.Toolkit;

import org.jdesktop.jdic.spi.Provider;
import org.jdesktop.jdic.spi.ProviderFactory;

/**
 * The <code>IconProvider</code> class provides several methods to access
 * system icons. It includes methods to retrieve a particular icon and to set and get icon themes.
 * This is the plug point to allow alternate icon providers to the {@link org.jdesktop.jdic.icons.IconService IconService}
 * Once IconService has been instantiated, changing the IconProvider has no effect.
 * An application could federate multiple IconProviders by "chaining" IconProviders.
 */
public abstract class IconProvider implements Provider {

    // need permission: PropertyPermission("org.jdesktop.jdic.icons.spi.IconProvider", "read")
    private static ProviderFactory providerFactory= new ProviderFactory("org.jdesktop.jdic.icons.spi.IconProvider", true) {
        protected String getPlatformProviderClassName() {
            // need permission: PropertyPermission("os.name", "read")
            String osName= System.getProperty("os.name");

            // See list of os names at: http://www.tolstoy.com/samizdat/sysprops.html
            // or at: http://lopica.sourceforge.net/os.html
            if(osName.startsWith("Windows")) {
                return "org.jdesktop.jdic.icons.impl.WinIconProvider";
            }
            // if this is wrong, the class probably won't initialize
            return "org.jdesktop.jdic.icons.impl.UnixIconProvider";
        }
    };

    /**
     * Set the provider for the IconService.
     * Once IconService has been instantiated, changing the IconProvider has no effect.
     * @param iconProvider The provider which is to be used by the IconService.
     */
    public static void setProvider(IconProvider iconProvider) {
        providerFactory.setProvider(iconProvider);
    }

    /**
     * Obtain the provider for the IconService.  If no provider was previously set, create a provider by <ul>
     * <li>Instantiate an instance of the class specified by the System property "org.jdesktop.jdic.icons.spi.IconProvider"</li>
     * <li>or, instantiate the default platform specific class</li></ul>
     */
    public static IconProvider getProvider() {
        return (IconProvider)providerFactory.getProvider();
    }

    /**
     * Return an icon of the requested size from the desktop.
     * If an icon of the requested size does not exist, return a scaled image.
     * @param name The name of the icon
     * @param size The requested size of the returned image
     * @param toolkit The toolkit to use to create the image.
     * @return An icon of the requested size or null
     */
    public abstract Image getIcon(String name, int size, Toolkit toolkit);

    /**
     * Obtain the icon theme that is being used.
     * @return The icon theme name.  This may be null if themes are not used by the platform.
     */
    public abstract String getTheme();

    /**
     * Set the icon theme to be used.
     * @param iconTheme The icon theme name.  This may be ignored if themes are not used by the platform.
     */
    public abstract void setTheme(String iconTheme);
}

