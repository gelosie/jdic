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

package org.jdesktop.jdic.icons.impl;

import org.jdesktop.jdic.icons.spi.IconProvider;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageProducer;

public class WinIconProvider extends IconProvider {

    /**
    * Initialize the Provider instance.  If this method returns false, this instance should not be used.
    * @return true, if initialization was successful; false, if initialization was not successful
    */
    public boolean initialize() {
        return WinIconWrapper.isLoaded();
    }

    /**
     * Return an icon of the requested size from the desktop.  
     * If an icon of the requested size does not exist, return a scaled image.
     * @param name The name of the icon
     * @param size The requested size of the returned image
     * @param toolkit The toolkit to use to create the image.
     * @return An icon of the requested size or null
     */
    public Image getIcon(String name, int size, Toolkit toolkit) {
        final ImageProducer[] producers= WinIconWrapper.getIconsFromSpecification(name);
        if(producers==null)
            return null;

        Image bestImage= null;
        int leastError= Integer.MAX_VALUE;

        for(int i= 0; i<producers.length; ++i) {
            final Image image= toolkit.createImage(producers[i]);
            final int wErr= size-image.getWidth(null); // we can get away with null because 
            final int hErr= size-image.getHeight(null); // image is actually a BufferedImage
            final int error= wErr*wErr + hErr*hErr;
            if(error>=leastError)
                continue;

            bestImage= image;

            if(error==0)
                return bestImage;

            leastError= error;
        }
        return bestImage.getScaledInstance(size, size, Image.SCALE_DEFAULT);
    }

    /**
     * Obtain the icon theme that is being used.
     * @return The icon theme name.  This may be null if themes are not used by the platform.
     */
    public String getTheme() {
        return null;
    }

    /**
     * Set the icon theme to be used.
     * @param The icon theme name.  This may be ignored if themes are not used by the platform.
     */
    public void setTheme(String iconTheme) {
    }

}                                                          

