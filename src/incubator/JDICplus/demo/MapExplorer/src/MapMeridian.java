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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import org.jdic.web.BrMap;
import org.jdic.web.BrMapSprite;

/**
 * Megidian geo space holder
 * @author uta
 */
public class MapMeridian extends BrMapSprite {
    int iStepCount = 100;
    MapMeridian(Color spriteColor){
        super("", spriteColor);
        isPoligon = false;
        for(int i=5; i<=(iStepCount-5); ++i){
            LLs.add( new Point2D.Double(i*180.0/iStepCount - 90.0, 0.0) );
        }    
    }
    
    @Override
    public void paint(Graphics g, int[] x, int[] y) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setStroke(new BasicStroke(5));
        super.paint(g2, x, y);
        g2.dispose();
    }
    
    public void add(BrMap m){
        m.getSprites().add(this);
        m.setZoomLevel(6);
    }
}
