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

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The map sprite class. Painted in latitude and longitude coordinats.
 * @author uta
 */
public class BrMapSprite implements BrISprite {
    public final java.util.LinkedList LLs = new java.util.LinkedList();
    public boolean isPoligon = true;
    public Color color;

    public BrMapSprite(Color cl)
    {
        color = cl;
    }
    public BrMapSprite()
    {
        this(new Color(0.0F, 0.0F, 1.0F, 0.25F));
    }
    public void createFromPoints(BrMap mc, Point[] pts) {
        String args = "";
        for(Point p : pts){
            if(0!=args.length()){
                args += ",";
            }
            args += p.x + "," + p.y;
        }
        String ret = mc.execJS("_fromPointToLatLng(" + args + ");");
        String[] aret = ret.split(",");
        if(0!=aret.length){
            for(int i=0; i<aret.length; i+=2){
                LLs.add( new Point2D.Double(
                         Double.parseDouble(aret[i]),
                         Double.parseDouble(aret[i+1])));
            }
        }
    }
    
    public void drawOn(BrComponent mc, Graphics g) {
       String args = "";
       for(Object LL : LLs) {
            Point2D o = (Point2D) LL;//GLangLng
            if(0!=args.length()) {
                args += ",";
            }
            args += o.getX() + "," + o.getY();
       }
       String ret = ((BrMap)mc).execJS("_fromLatLngToPoint(" + args + ");");
       String[] aret = ret.split(",");
       if(0==aret.length){
           return;
       }
       g.setColor(color);
       int pontsCount = aret.length/2;
       int[] x = new int[pontsCount];
       int[] y = new int[pontsCount];
       for(int k = 0; k < pontsCount; ++k){
            x[k] = Integer.parseInt(aret[k*2]);
            y[k] = Integer.parseInt(aret[k*2+1]);
       }
       paint(g, x, y);
    }

    public void paint(Graphics g, int[] x, int[] y) {
        int pontsCount = x.length;
        if(isPoligon){
            Polygon pg = new Polygon(x, y, pontsCount);
            g.fillPolygon(pg);
        } else {
            for(int k = 0; k < (pontsCount-1); ++k){
                 g.drawLine(x[k], y[k], x[k+1], y[k+1] );
            }
        }
    }
    
    public void save(String fn) {

        FileWriter fw = null;
        try {
            fw = new FileWriter(fn);
            fw.write("BrMapSprite bs = new BrMapSprite();\n");
            for(Object LL : LLs) {
                 Point2D o = (Point2D) LL;//GLangLng
                 fw.write("bs.LLs.add( new Point2D.Double(" + o.getX() + "," + o.getY() + "));\n");
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
