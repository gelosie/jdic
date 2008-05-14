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

package org.jdic.web;

import com.sun.org.apache.xpath.internal.operations.Equals;
import java.awt.Point;
import java.awt.geom.Point2D;

/**
 * Mouse geo and ordinal position holder
 * @author uta
 */
public class BrMapMousePos {
    private Point pos;
    private Point2D posGeo;

    public BrMapMousePos(Point _pos, Point2D _posGeo){
        pos = _pos;
        posGeo = _posGeo;
    }
    public boolean equals(BrMapMousePos o){
        if(null==o)
            return false;
        return pos.equals(o.getMousePixelPos()) && posGeo.equals(o.getMouseGeoPos());
    }
    public boolean equals(Object o){
        return equals((BrMapMousePos)o);
    }
    public Point getMousePixelPos() { return pos; }
    public Point2D getMouseGeoPos() { return posGeo; }
}
