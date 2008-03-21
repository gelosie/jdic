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

import org.jdic.web.event.BrComponentEvent;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.datatransfer.DataFlavor;
import java.io.*;

/**
 * The map browser class. Supports map exploring on Google and Microsoft servers.
 * @author uta
 */    

public class BrMap
    extends BrComponent
{
    public final static String MAP_GOOGLE  = "googleMap.html"; // http://maps.google.com
    public final static String MAP_MS_LIVE = "msLiveMap.html"; // http://virtualearth.net or http://maps.microsoft.com

    final static int FREE_MOVE = 0; //free mouse move
    final static int SHOW_ZOOM_RECT = 1; //drow rect from ancor
    final static int HIDE_ZOOM_RECT = 2; //drow rect from ancor

    //d'n'd
    private DataFlavor m_df = DataFlavor.imageFlavor;
    private transient int m_dropAction;
    private Image m_data = null;

    double distanse = 0.0;
    Rectangle rcOldSelect = null;
    Rectangle rcNewSelect = null;
    private java.util.AbstractList<GImage> lsGImages;


    public static void copyStream(
        InputStream in,
        OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read = 0;
        while((read = in.read(buffer, 0, buffer.length)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    public BrMap(String stWhichMap){
        super("");
        try {
// no chanse with Google signature...
//            setURL(
//                getClass().getResourceAsStream(stWhichMap),
//                stWhichMap);
            File tm = File.createTempFile("aaa", "aaa");
            File map =  new File( tm.getParent() + File.separator + stWhichMap);
            map.delete();
            map.deleteOnExit();
            tm.delete();
            if(!map.exists()){
                copyStream(
                        BrMap.class.getResourceAsStream(stWhichMap),
                        new FileOutputStream(map));
            }
            setURL(map.getAbsolutePath());            
        } catch( Exception e ) {
            e.printStackTrace();
        }
        //paintAlgorithm = IEComponent.DRAW_NATIVE_BEFORE_CONTENT;
        //paintAlgorithm = IEComponent.DRAW_DOUBLE_BUFFERED;
        lsGImages = new java.util.ArrayList<GImage>();
    }

    public void OnFreeMove(int x, int y, double lat, double lng)
    {

    }
    
    public void OnZoomRect(Rectangle rc, boolean bShow)
    {
        
    }

    public void processBrComponentEvent(BrComponentEvent e) {
        if(BrComponentEvent.DISPID_STATUSTEXTCHANGE == e.getID()){
            String st = e.getValue();
            if( st.startsWith("java,") ){
                String args[] = st.split(",");
                int iCommand = Integer.parseInt(args[1]);
                switch(iCommand){
                case FREE_MOVE:
                    try{
                        int x1 = Integer.parseInt(args[2]);
                        int y1 = Integer.parseInt(args[3]);
                        double lat1  = Double.parseDouble(args[4]);
                        double lng1  = Double.parseDouble(args[5]);
                        OnFreeMove(x1, y1, lat1, lng1);
                    }catch(Exception ex){
                        ex.printStackTrace();
                    }
                    break;
                case SHOW_ZOOM_RECT:
                    try{
                        int x1 = Integer.parseInt(args[2]);
                        int y1 = Integer.parseInt(args[3]);
                        double lat1  = Double.parseDouble(args[4]);
                        double lng1  = Double.parseDouble(args[5]);
                        OnFreeMove(x1, y1, lat1, lng1);
                        int x2 = Integer.parseInt(args[6]);
                        int y2 = Integer.parseInt(args[7]);
                        distanse = Double.parseDouble(args[8]);
                        if(null!=rcOldSelect){
                            repaint(rcOldSelect);
                        }
                        rcNewSelect = new Rectangle(
                            Math.min(x1, x2),
                            Math.min(y1, y2),
                            Math.abs(x1 - x2),
                            Math.abs(y1 - y2)
                        );
                        repaint(rcNewSelect);
                        OnZoomRect(rcNewSelect, true);
                    }catch(Exception ex){
                        ex.printStackTrace();                    
                    }
                    break;
                case HIDE_ZOOM_RECT:
                    rcNewSelect = null;
                    if(null!=rcOldSelect){
                        repaint(rcOldSelect);
                    }
                    OnZoomRect(rcNewSelect, false);
                    rcOldSelect = null;
                    break;
                }
            }
        }
        super.processBrComponentEvent(e);
    }
    
    public void paintContent(Graphics g) {
        super.paintContent(g);
        if(null!=rcNewSelect){
            int iMetters = (int)distanse;
            String stDisplay = "";
            int iKm = iMetters/1000;
            if(0 < iKm){
                stDisplay = iKm + "km ";
            }
            if(iKm < 5) {
                stDisplay += (iMetters % 1000) + "m";
            }
            paintPlaceHolder(
                g,
                rcNewSelect.x, rcNewSelect.y, rcNewSelect.width, rcNewSelect.height,
                stDisplay
            );
            rcOldSelect = new Rectangle(rcNewSelect);
        }
    }
}

class GImage{
    private Rectangle2D rcLatLog;
    private Image       image;

    public GImage(Rectangle2D _rcLatLog, Image _image){
        rcLatLog = _rcLatLog;
        image = _image;
    }
}
