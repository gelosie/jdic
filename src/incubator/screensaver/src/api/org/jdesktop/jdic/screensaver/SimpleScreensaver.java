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

import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for simple screensavers who will have their <code>paint()</code>
 * method invoked on a regular basis to render each frame.  
 *
 * @author Mark Roth
 */
public abstract class SimpleScreensaver 
    extends ScreensaverBase
{
    /** Logging support */
    private static Logger logger = 
        Logger.getLogger("org.jdesktop.jdic.screensaver");
    
    /**
     * Override this method in your subclasses to paint a single frame.
     * Any exceptions thrown during paint will be sent to the
     * org.jdesktop.jdic.screensaver J2SE logger.
     * <p>
     * Tip: Treat this as though it were part of a game loop.  In general, 
     * it's a good idea not to allocate ('new') any objects after init
     * unless they will not be deleted.  This will yield smoother animation
     * and no pauses from the garbage collector.
     *
     * @param g Graphics context used to draw to the screensaver window.
     */
    public abstract void paint( Graphics g );
    
    /**
     * Called by the native layer to render the next frame.
     */
    public final void renderFrame() {
        try {
            super.renderFrame();
            Graphics g = context.getComponent().getGraphics();
            java.awt.Toolkit.getDefaultToolkit().sync();
            paint( g );
        }
        catch(Throwable t) {
            logger.log(Level.WARNING, 
                "Screensaver threw exception while rendering frame: ", t);
        }
    }
    
}
