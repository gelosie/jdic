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

package org.jdesktop.jdic.screensaver.bouncingline;

import org.jdesktop.jdic.screensaver.SimpleScreensaver;
import org.jdesktop.jdic.screensaver.ScreensaverContext;
import org.jdesktop.jdic.screensaver.ScreensaverSettings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Properties;

/**
 * Example classic bouncing line screen saver.  Good starting point for
 * new screensavers.
 *
 * @author Mark Roth
 */
public class BouncingLine 
    extends SimpleScreensaver
{
    // Maximum speed for line points
    private final int MAX_SPEED = 4;
    
    // First point on the line
    private Point p1 = new Point();
    
    // Second point on the line
    private Point p2 = new Point();
    
    // Direction of first point (vector)
    private Point dir1;
    
    // Direction of second point (vector)
    private Point dir2;
    
    // Color of line
    private Color lineColor = Color.blue;

    /**
     * Initialize this screen saver - pick two random points and directions.
     */
    public void init() {
        ScreensaverSettings settings = getContext().getSettings();
        Component c = getContext().getComponent();
        int width = c.getWidth();
        int height = c.getHeight();
        randomizePoint( p1, width, height );
        randomizePoint( p2, width, height );
        dir1 = new Point( randomVector(), randomVector() );
        dir2 = new Point( randomVector(), randomVector() );
        String colorOption = settings.getProperty( "color" );
        if( colorOption != null ) {
            String COLOR_FORMAT = "Color must be in #rrggbb format";
            if( colorOption.length() != 7 ) {
                throw new RuntimeException( COLOR_FORMAT );
            }
            try {
                int r = Integer.parseInt( colorOption.substring( 1, 3 ), 16 );
                int g = Integer.parseInt( colorOption.substring( 3, 5 ), 16 );
                int b = Integer.parseInt( colorOption.substring( 5, 7 ), 16 );
                lineColor = new Color( r, g, b );
            }
            catch( NumberFormatException e ) {
                throw new RuntimeException( COLOR_FORMAT );
            }
        }
    }
    
    /**
     * Paint the next frame - erase the old line and paint the new line.
     */
    public void paint( Graphics g ) {
        Component c = getContext().getComponent();
        int width = c.getWidth();
        int height = c.getHeight();
        
        // Erase old line:
        g.setColor( c.getBackground() );
        g.drawLine( p1.x, p1.y, p2.x, p2.y );
        
        // Move points and bounce off walls:
        bounce( p1, dir1, width, height );
        bounce( p2, dir2, width, height );
        
        // Draw new line:
        g.setColor( lineColor );
        g.drawLine( p1.x, p1.y, p2.x, p2.y );
    }

    /**
     * Move the given point to a random location within (0, 0, width, height)
     */
    private void randomizePoint( Point p, int width, int height ) {
        p.x = (int)(Math.random() * width);
        p.y = (int)(Math.random() * height);
    }
    
    /**
     * Move the given point in the given direction vector.  If the point
     * falls outside of the bounds (0, 0, width, height), bounce off the
     * wall and pick a new random speed.
     */
    private void bounce( Point p, Point dir, int width, int height ) {
        p.x += dir.x;
        if( (p.x < 0) || (p.x > width) ) {
            p.x -= dir.x;
            if( dir.x < 0 ) {
                dir.x = Math.abs( randomVector() );
            }
            else {
                dir.x = -Math.abs( randomVector() );
            }
        }
        
        p.y += dir.y;
        if( (p.y < 0) || (p.y > height) ) {
            p.y -= dir.y;
            if( dir.y < 0 ) {
                dir.y = Math.abs( randomVector() );
            }
            else {
                dir.y = -Math.abs( randomVector() );
            }
        }
        
        // If we're still out of bounds, pick a brand new point:
        if( (p.x < 0) || (p.x > width) || (p.y < 0) || (p.y > height) ) {
            randomizePoint( p, width, height );
        }
    }
    
    /**
     * Returns a random number between -MAX_SPEED and MAX_SPEED, inclusive, 
     * but excluding 0.
     */
    private int randomVector() {
        int result = (int)(Math.random() * MAX_SPEED) + 1;
        if( Math.random() > 0.5 ) {
            result = -result;
        }
        return result;
    }
}
