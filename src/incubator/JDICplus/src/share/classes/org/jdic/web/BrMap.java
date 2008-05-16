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
import java.awt.geom.Point2D;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;



/**
 * The map browser class. Supports map exploring on Google and Microsoft servers.
 * @author uta
 */
public class BrMap
    extends BrComponent
{
    public final static int MAP_GOOGLE  = 0;
    public final static int MAP_MS_LIVE = 1;
    public final static int MAP_YAHOO = 2;

    public final static int VIEW_ROAD = 0x00000001;//road view
    public final static int VIEW_SATELLITE = 0x00000002;//road view
    public final static int VIEW_TRAFFIC = 0x00000004;//traffic view
    public final static int VIEW_3D  = 0x00010000; //road view
    public final static int VIEW_25D = 0x00020000; //landscape view

    public final static int ZOOM_MIN  = 1;
    public final static int ZOOM_MAX  = 19;

    //Map resolution = 156543.04 meters/pixel * cos(latitude) / (2 ^ zoomlevel)
    //scale[1..19] m/pixel for Latitude=0 (Equator)
    public static double[] meter2pixel = new double[ZOOM_MAX-ZOOM_MIN+1];
    static {
        meter2pixel[0] = 156543.04/2;
        for(int i=1; i<meter2pixel.length; ++i)
            meter2pixel[i] = meter2pixel[i-1]/2;
    }

    /**
     * Calculates the distance between two geo-points in screen meters.
     * 
     * At the lowest level of detail (Level 1), the map is 512 x 512 pixels. 
     * At each successive level of detail, the map width and height grow by a 
     * factor of 2: Level 2 is 1024 x 1024 pixels, Level 3 is 2048 x 2048 pixels, 
     * Level 4 is 4096 x 4096 pixels, and so on. In general, the width and height 
     * of the map (in pixels) can be calculated as:
     *      map width = map height = 256 * 2^level <b>pixels</b>
     * The ground resolution indicates the distance on the ground that’s represented 
     * by a single pixel in the map. For example, at a ground resolution of 
     * 10 meters/pixel, each pixel represents a ground distance of 10 meters. 
     * The ground resolution varies depending on the level of detail and the 
     * latitude at which it’s measured. 
     * Using an earth radius of 6378137 meters, the ground resolution 
     * (in meters per pixel) can be calculated as:<br/>
     *      ground resolution = cos(latitude * pi/180) * earth circumference / map width<br/>
     *          = (cos(latitude * pi/180) * 2 * pi * 6378137 meters) / (256 * 2 level pixels)
     * The map scale indicates the ratio between map distance and ground distance, 
     * when measured in the same units. For instance, at a map scale of 1 : 100,000, 
     * each inch on the map represents a ground distance of 100,000 inches. 
     * Like the ground resolution, the map scale varies with the level of detail 
     * and the latitude of measurement. It can be calculated from the ground 
     * resolution as follows, given the screen resolution in dots per inch, 
     * typically 96 dpi:<br/>
     *      map scale = 1 : ground resolution * screen dpi / 0.0254 <b>meters/inch</b><br/>
     *          = 1 : (cos(latitude * pi/180) * 2 * pi * 6378137 * screen dpi) / (256 * 2 level * 0.0254)<br/> 
     * <a href="http://msdn2.microsoft.com/en-us/library/bb259689.aspx">read more...</a>
     * @param Latitude1
     * @param Longitude1
     * @param Latitude2
     * @param Longitude2
     * @return
     */
    public static double getDistance(
         double Latitude1, double Longitude1,
         double Latitude2, double Longitude2)
    {
        Latitude1 = Math.toRadians(Latitude1);
        Longitude1 = Math.toRadians(Longitude1);
        Latitude2 = Math.toRadians(Latitude2);
        Longitude2 = Math.toRadians(Longitude2);

        final double R = 6371.0; // earth's mean radius in km
        double  dSinLat05  = Math.sin( (Latitude2 - Latitude1)/2 );
        double  dSinLong05 = Math.sin( (Longitude2 - Longitude1)/2 );
        double  a = dSinLat05 * dSinLat05 +
                Math.cos(Latitude1) * Math.cos(Latitude2) * dSinLong05 * dSinLong05;
        double  c = (0==a || 1==a)
            ? 0
            : 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0-a));
        return R * c * 1000.0; //in meters
    }
    
    /**
     * Calculates the absolute pixel point without top-left offset two from 
     * geo-coordinates.
     * Given latitude and longitude in degrees, and the level of detail, 
     * the pixel XY coordinates can be calculated as follows:<br/>
     *   sinLatitude = sin(latitude * pi/180)<br/>
     *   pixelX = ((longitude + 180) / 360) * 256 * 2^level<br/>
     *   pixelY = (0.5 - log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * pi)) * 256 * 2^level<br/>
     * <a href="http://msdn2.microsoft.com/en-us/library/bb259689.aspx">read more...</a>
     * @param Latitude
     * @param Longitude
     * @return
     */
    public Point2D getPixelPos(double Latitude, double Longitude)
    {
        double sinLatitude = Math.sin(Math.toRadians(Latitude));
        return new Point2D.Double(
            ((Longitude + 180.0) / 360.0) * 256.0 * (1<<viewZoomLevel),
            ( 0.5 - Math.log((1.0+sinLatitude)/(1.0-sinLatitude))
                /(4.0*Math.PI) )*256.0*(1<<viewZoomLevel)
        );
    }
    /**
     * Calculates the distance between two geo-points in screen pixels.
     * @param Latitude1
     * @param Longitude1
     * @param Latitude2
     * @param Longitude2
     * @return the distance in screen pixels
     */

    public double getDistanceInPixel(
         double Latitude1, double Longitude1,
         double Latitude2, double Longitude2)
    {
        Point2D p1 = getPixelPos(Latitude1, Longitude1);
        Point2D p2 = getPixelPos(Latitude2, Longitude2);
        return Point2D.distance(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    public double getMeter2pixel(double latitude)
    {
        return meter2pixel[getZoomLevel() - ZOOM_MIN]*Math.cos(Math.toRadians(latitude));
    }
    public int getBestZoomLevel(
         double Latitude1, double Longitude1,
         double Latitude2, double Longitude2)
    {
        double  lat1 = Math.min(Latitude1, Latitude1);
        double  CosLat = Math.cos(Math.toRadians(lat1));
        double  Wmeter = getDistance(
            lat1, Longitude1,
            lat1, Longitude2)/CosLat;
        double  Hmeter = getDistance(
            Latitude1, Longitude1,
            Latitude2, Longitude1)/CosLat;

        int zoom = 0;
        for(;
           zoom < meter2pixel.length
           && (getWidth()*meter2pixel[zoom]) > Wmeter
           && (getHeight()*meter2pixel[zoom]) > Hmeter;
           ++zoom) ;

        return zoom + ZOOM_MIN;
//        viewCenter = ((Latitude1 + Latitude2)/2) + "," + ((Latitude1 + Latitude2)/2);
//        _viewZoomLevel = zoom;
//        setViewDescriptor(viewCenter + _viewZoomLevel);
    }

    public static char[] LNG = new char[] {'E', 'W'};
    public static char[] LAT = new char[] {'N', 'S'};
    public static String getGrade(double d, char[] ch){
        char c = (d > 0) ? ch[0] : ch[1];
        d = Math.abs(d);
        double G = Math.floor(d);
        double M = (d - G)*60.0;
        double S = (M - Math.floor(M))*60.0;
        return String.format("%d\u00BA%d\'%2.2f\" %c", (int)G, (int)Math.floor(M), S, c);
    }

    private BrMapInfo[] mapInfos = new BrMapInfo[]{
        // http://maps.google.com
        new BrMapInfo("Google Maps", "googleMap.html", 19, "map_google42x42.png"),
        // http://virtualearth.net or http://maps.microsoft.com
        new BrMapInfo("Microsoft Maps", "msLiveMap.html", 19, "map_msLive42x42.png"),
        // http://maps.yahoo.com/
        new BrMapInfo("Yahoo Maps", "yahooMap.html", 17, "map_yahoo42x42.png")
    };

    private void init()
    {
        initComponents();
        sbAlpha.setVisible(false);
        setZoomLevel(getZoomLevel());
        setViewType(getViewType());
        setMapProvider(getMapProvider());
    }

    public BrMap(){
        init();
    }

    public BrMap(
        int _iMapProvider,
        double _viewCenterLatitude,
        double _viewCenterLongitude,
        int _viewZoomLevel,
        int _viewType)
    {
        setInitView(
            _iMapProvider,
            _viewCenterLatitude,
            _viewCenterLongitude,
            _viewZoomLevel,
            _viewType);
        init();
    }

    public void setInitView(
        int _iMapProvider,
        double _viewCenterLatitude,
        double _viewCenterLongitude,
        int _viewZoomLevel,
        int _viewType)
    {
        mapProvider = _iMapProvider;
        viewCenter = _viewCenterLatitude + "," + _viewCenterLongitude;
        viewZoomLevel = Math.min(
                _viewZoomLevel,
                mapInfos[mapProvider].getMaxZoomLevel());
        viewType = _viewType;
        setURL( mapInfos[mapProvider].getHTMLFile()
            /*+ "?" + viewCenter
            + "," + viewZoomLevel
            + "," + viewType*/);
    }
    
    private void initComponents() {

        btRoadView = new javax.swing.JToggleButton();
        btSatelliteView = new javax.swing.JToggleButton();
        btHybridView = new javax.swing.JToggleButton();
        bnZoomPlus = new javax.swing.JButton();
        sbZoomLevel = new javax.swing.JSlider();
        bnZoomMinus = new javax.swing.JButton();
        btGoogleMap = new javax.swing.JToggleButton();
        btMicrosoftMap = new javax.swing.JToggleButton();
        btYahooMap = new javax.swing.JToggleButton();
        sbAlpha = new JBottomSlider();

        setDoubleBuffered(true);

        btRoadView.setText("Road");
        btRoadView.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btRoadView.setMaximumSize(new java.awt.Dimension(52, 20));
        btRoadView.setMinimumSize(new java.awt.Dimension(52, 20));
        btRoadView.setPreferredSize(new java.awt.Dimension(52, 20));
        btRoadView.setRolloverEnabled(true);
        btRoadView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRoadViewActionPerformed(evt);
            }
        });

        btSatelliteView.setText("Satellite");
        btSatelliteView.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btSatelliteView.setMaximumSize(new java.awt.Dimension(52, 20));
        btSatelliteView.setMinimumSize(new java.awt.Dimension(52, 20));
        btSatelliteView.setPreferredSize(new java.awt.Dimension(52, 20));
        btSatelliteView.setRolloverEnabled(true);
        btSatelliteView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSatelliteViewActionPerformed(evt);
            }
        });

        btHybridView.setText("Hybrid");
        btHybridView.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btHybridView.setMaximumSize(new java.awt.Dimension(52, 20));
        btHybridView.setMinimumSize(new java.awt.Dimension(52, 20));
        btHybridView.setPreferredSize(new java.awt.Dimension(52, 20));
        btHybridView.setRolloverEnabled(true);
        btHybridView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btHybridViewActionPerformed(evt);
            }
        });

        bnZoomPlus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_plus.png"))); // NOI18N
        bnZoomPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bnZoomPlusActionPerformed(evt);
            }
        });

        sbZoomLevel.setMajorTickSpacing(1);
        sbZoomLevel.setMaximum(19);
        sbZoomLevel.setMinimum(1);
        sbZoomLevel.setMinorTickSpacing(1);
        sbZoomLevel.setOrientation(javax.swing.JSlider.VERTICAL);
        sbZoomLevel.setPaintTicks(true);
        sbZoomLevel.setMaximumSize(new java.awt.Dimension(32, 107));
        sbZoomLevel.setOpaque(false);
        sbZoomLevel.setPreferredSize(new java.awt.Dimension(32, 107));
        sbZoomLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sbZoomLevelStateChanged(evt);
            }
        });

        bnZoomMinus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_minus.png"))); // NOI18N
        bnZoomMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bnZoomMinusActionPerformed(evt);
            }
        });

        btGoogleMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_google42x42.png"))); // NOI18N
        btGoogleMap.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btGoogleMap.setMaximumSize(new java.awt.Dimension(50, 50));
        btGoogleMap.setMinimumSize(new java.awt.Dimension(50, 50));
        btGoogleMap.setOpaque(false);
        btGoogleMap.setPreferredSize(new java.awt.Dimension(50, 50));
        btGoogleMap.setRolloverEnabled(true);
        btGoogleMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGoogleMapActionPerformed(evt);
            }
        });

        btMicrosoftMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_msLive42x42.png"))); // NOI18N
        btMicrosoftMap.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btMicrosoftMap.setMaximumSize(new java.awt.Dimension(50, 50));
        btMicrosoftMap.setMinimumSize(new java.awt.Dimension(50, 50));
        btMicrosoftMap.setOpaque(false);
        btMicrosoftMap.setPreferredSize(new java.awt.Dimension(50, 50));
        btMicrosoftMap.setRolloverEnabled(true);
        btMicrosoftMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMicrosoftMapActionPerformed(evt);
            }
        });

        btYahooMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_yahoo42x42.png"))); // NOI18N
        btYahooMap.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btYahooMap.setMaximumSize(new java.awt.Dimension(50, 50));
        btYahooMap.setMinimumSize(new java.awt.Dimension(50, 50));
        btYahooMap.setOpaque(false);
        btYahooMap.setPreferredSize(new java.awt.Dimension(50, 50));
        btYahooMap.setRolloverEnabled(true);
        btYahooMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btYahooMapActionPerformed(evt);
            }
        });

        sbAlpha.setMinimum(50);
        sbAlpha.setValue(sbAlpha.getMaximum());
        sbAlpha.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getCentralPanel());
        getCentralPanel().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bnZoomPlus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sbZoomLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bnZoomMinus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btGoogleMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btMicrosoftMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btYahooMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 179, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sbAlpha, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btRoadView, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btSatelliteView, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btHybridView, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btRoadView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btSatelliteView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btHybridView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(bnZoomPlus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sbZoomLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bnZoomMinus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btGoogleMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btMicrosoftMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btYahooMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                .addComponent(sbAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btGoogleMap.getAccessibleContext().setAccessibleName("Google");
        btMicrosoftMap.getAccessibleContext().setAccessibleName("Microsoft");
        btYahooMap.getAccessibleContext().setAccessibleName("Yahoo");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    /*
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btRoadView = new javax.swing.JToggleButton();
        btSatelliteView = new javax.swing.JToggleButton();
        btHybridView = new javax.swing.JToggleButton();
        bnZoomPlus = new javax.swing.JButton();
        sbZoomLevel = new javax.swing.JSlider();
        bnZoomMinus = new javax.swing.JButton();
        btGoogleMap = new javax.swing.JToggleButton();
        btMicrosoftMap = new javax.swing.JToggleButton();
        btYahooMap = new javax.swing.JToggleButton();
        sbAlpha = new JBottomSlider();

        setDoubleBuffered(true);

        btRoadView.setText("Road");
        btRoadView.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btRoadView.setMaximumSize(new java.awt.Dimension(52, 20));
        btRoadView.setMinimumSize(new java.awt.Dimension(52, 20));
        btRoadView.setPreferredSize(new java.awt.Dimension(52, 20));
        btRoadView.setRolloverEnabled(true);
        btRoadView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btRoadViewActionPerformed(evt);
            }
        });

        btSatelliteView.setText("Satellite");
        btSatelliteView.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btSatelliteView.setMaximumSize(new java.awt.Dimension(52, 20));
        btSatelliteView.setMinimumSize(new java.awt.Dimension(52, 20));
        btSatelliteView.setPreferredSize(new java.awt.Dimension(52, 20));
        btSatelliteView.setRolloverEnabled(true);
        btSatelliteView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSatelliteViewActionPerformed(evt);
            }
        });

        btHybridView.setText("Hybrid");
        btHybridView.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btHybridView.setMaximumSize(new java.awt.Dimension(52, 20));
        btHybridView.setMinimumSize(new java.awt.Dimension(52, 20));
        btHybridView.setPreferredSize(new java.awt.Dimension(52, 20));
        btHybridView.setRolloverEnabled(true);
        btHybridView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btHybridViewActionPerformed(evt);
            }
        });

        bnZoomPlus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_plus.png"))); // NOI18N
        bnZoomPlus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bnZoomPlusActionPerformed(evt);
            }
        });

        sbZoomLevel.setMajorTickSpacing(1);
        sbZoomLevel.setMaximum(19);
        sbZoomLevel.setMinimum(1);
        sbZoomLevel.setMinorTickSpacing(1);
        sbZoomLevel.setOrientation(javax.swing.JSlider.VERTICAL);
        sbZoomLevel.setPaintTicks(true);
        sbZoomLevel.setMaximumSize(new java.awt.Dimension(32, 107));
        sbZoomLevel.setOpaque(false);
        sbZoomLevel.setPreferredSize(new java.awt.Dimension(32, 107));
        sbZoomLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sbZoomLevelStateChanged(evt);
            }
        });

        bnZoomMinus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_minus.png"))); // NOI18N
        bnZoomMinus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bnZoomMinusActionPerformed(evt);
            }
        });

        btGoogleMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_google42x42.png"))); // NOI18N
        btGoogleMap.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btGoogleMap.setMaximumSize(new java.awt.Dimension(50, 50));
        btGoogleMap.setMinimumSize(new java.awt.Dimension(50, 50));
        btGoogleMap.setOpaque(false);
        btGoogleMap.setPreferredSize(new java.awt.Dimension(50, 50));
        btGoogleMap.setRolloverEnabled(true);
        btGoogleMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btGoogleMapActionPerformed(evt);
            }
        });

        btMicrosoftMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_msLive42x42.png"))); // NOI18N
        btMicrosoftMap.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btMicrosoftMap.setMaximumSize(new java.awt.Dimension(50, 50));
        btMicrosoftMap.setMinimumSize(new java.awt.Dimension(50, 50));
        btMicrosoftMap.setOpaque(false);
        btMicrosoftMap.setPreferredSize(new java.awt.Dimension(50, 50));
        btMicrosoftMap.setRolloverEnabled(true);
        btMicrosoftMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btMicrosoftMapActionPerformed(evt);
            }
        });

        btYahooMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jdic/web/images/map_yahoo42x42.png"))); // NOI18N
        btYahooMap.setMargin(new java.awt.Insets(2, 2, 2, 2));
        btYahooMap.setMaximumSize(new java.awt.Dimension(50, 50));
        btYahooMap.setMinimumSize(new java.awt.Dimension(50, 50));
        btYahooMap.setOpaque(false);
        btYahooMap.setPreferredSize(new java.awt.Dimension(50, 50));
        btYahooMap.setRolloverEnabled(true);
        btYahooMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btYahooMapActionPerformed(evt);
            }
        });

        sbAlpha.setMinimum(25);
        sbAlpha.setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bnZoomPlus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sbZoomLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bnZoomMinus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btGoogleMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btMicrosoftMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btYahooMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 129, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(sbAlpha, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btRoadView, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btSatelliteView, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btHybridView, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btRoadView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btSatelliteView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btHybridView, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(bnZoomPlus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sbZoomLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bnZoomMinus, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btGoogleMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btMicrosoftMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btYahooMap, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(sbAlpha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btGoogleMap.getAccessibleContext().setAccessibleName("Google");
        btMicrosoftMap.getAccessibleContext().setAccessibleName("Microsoft");
        btYahooMap.getAccessibleContext().setAccessibleName("Yahoo");
    }// </editor-fold>//GEN-END:initComponents
    */
private void btRoadViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btRoadViewActionPerformed
    setViewType(VIEW_ROAD); 
}//GEN-LAST:event_btRoadViewActionPerformed

private void btSatelliteViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSatelliteViewActionPerformed
    setViewType(VIEW_SATELLITE);
}//GEN-LAST:event_btSatelliteViewActionPerformed

private void btHybridViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btHybridViewActionPerformed
    setViewType(VIEW_ROAD|VIEW_SATELLITE);
}//GEN-LAST:event_btHybridViewActionPerformed

private void sbZoomLevelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sbZoomLevelStateChanged
    //System.out.println("zoom from sb:" + sbZoomLevel.getValue());
    setZoomLevel(sbZoomLevel.getValue());
}//GEN-LAST:event_sbZoomLevelStateChanged

private void bnZoomPlusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bnZoomPlusActionPerformed
    setZoomLevel(getZoomLevel()+1);
}//GEN-LAST:event_bnZoomPlusActionPerformed

private void bnZoomMinusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bnZoomMinusActionPerformed
    setZoomLevel(getZoomLevel()-1);
}//GEN-LAST:event_bnZoomMinusActionPerformed

private void btGoogleMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btGoogleMapActionPerformed
    setMapProvider(MAP_GOOGLE);
}//GEN-LAST:event_btGoogleMapActionPerformed

private void btMicrosoftMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btMicrosoftMapActionPerformed
    setMapProvider(MAP_MS_LIVE);
}//GEN-LAST:event_btMicrosoftMapActionPerformed

private void btYahooMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btYahooMapActionPerformed
    setMapProvider(MAP_YAHOO);
}//GEN-LAST:event_btYahooMapActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton bnZoomMinus;
    public javax.swing.JButton bnZoomPlus;
    public javax.swing.JToggleButton btGoogleMap;
    public javax.swing.JToggleButton btHybridView;
    public javax.swing.JToggleButton btMicrosoftMap;
    public javax.swing.JToggleButton btRoadView;
    public javax.swing.JToggleButton btSatelliteView;
    public javax.swing.JToggleButton btYahooMap;
    public javax.swing.JSlider sbAlpha;
    public javax.swing.JSlider sbZoomLevel;
    // End of variables declaration//GEN-END:variables
    
    /*
     var event_cancelBubble = false;
     var event_return = false;

     window.status =  "javaevent"
        + "," + event.type //1
        + "," + event.x //2
        + "," + event.y //3
        + "," + event.button;//4
     */
    public Point2D point2LL(Point pt) {
       String boundsLL = execJS( "_fromPointToLatLng(" +
               + pt.x + "," + pt.y + ");" );
       String args[] = boundsLL.split(",");
       return new Point2D.Double(Double.parseDouble(args[0]), Double.parseDouble(args[1]));
    }

    public void processJSEvents(String st)
    {
        final String args[] = st.split(",");
        String type = args[1].toLowerCase();
        if( type.equals("mapready") ){
            SwingUtilities.invokeLater(new Runnable(){ public void run() {
                setMapReady(true);
            }});
        } else if( type.equals("foundaddress") ){
            onFoundAddress(args);
        } else if( type.equals("viewrequest") ){
            onViewRequest();
        } else if( type.equals("zoomchanged") ){
            SwingUtilities.invokeLater(new Runnable(){ public void run() {
                skipJSChanges = true;
                setZoomLevel(Integer.parseInt(args[2]));
                //System.out.println("zoom from JS:" + args[2]);
                skipJSChanges = false;
            }});
        } else if( type.equals("centerchanged") ){
            SwingUtilities.invokeLater(new Runnable(){ public void run() {
                skipJSChanges = true;
                setViewCenter(args[2] + "," + args[3]);
                skipJSChanges = false;
            }});
        } else if(isMapReady() && type.contains("mouse")){
            onMouseEvent(type, args);
        }
    }


    @Override
    public String processBrComponentEvent(BrComponentEvent e) {
        if(BrComponentEvent.DISPID_STATUSTEXTCHANGE == e.getID()){
            final String st = e.getValue();
            if( st.startsWith("javaevent,") ){
                processJSEvents(st);
                //block message echo
                execJS(":window.status=\"\"");
            }
        }
        return super.processBrComponentEvent(e);
    }

    @Override
    public void paintClientArea(Graphics g, boolean withPeer) {
        super.paintClientArea(g, withPeer);
        Rectangle _rcNewSelect = rcNewSelect;
        if(null!=_rcNewSelect){
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
                _rcNewSelect.x, _rcNewSelect.y, _rcNewSelect.width, _rcNewSelect.height,
                stDisplay
            );
            rcOldSelect = new Rectangle(_rcNewSelect);
        }
    }

    public boolean skipJSChanges = false;
    public void setMapProperty(
            String stName,
            Object oValue)
    {
        if( isMapReady()
            && !stURL.equals(stAboutBlank)
            && !skipJSChanges)
        {
            execJS(":_set" + stName + "(" + oValue + ");");
        }
    }


    /**
     * Holds value of property viewCenter.
     */
    private String viewCenter = "37.70,-122.44";

    /**
     * Getter for property viewCenter - the center of map "Latitude, Longitude".
     * @return Value of property viewCenter.
     */
    public String getViewCenter() {
        return viewCenter;
    }

    /**
     * Converter for property viewCenter to Point2D
     * @param center String in "latitude, longitude" format.
     * @return Parsed Point2D object.
     */
    public Point2D getPoint(String center) {
        String ll[] = center.split(",");
        return new Point2D.Double(
                Double.parseDouble(ll[0]),
                Double.parseDouble(ll[1])
        );
    }
    
    /**
     * Setter for property viewCenter.
     * @param viewCenter New value of property viewCenter.
     */
    public void setViewCenter(String viewCenter) {
        if( !viewCenter.equals(this.viewCenter) ){
            setMapProperty("Center", viewCenter);
        }
        String old = this.viewCenter;
        this.viewCenter = viewCenter;
        propertyChangeSupport.firePropertyChange("viewCenter", old, viewCenter);
    }

    /**
     * Holds value of property _viewZoomLevel.
     */
    private int viewZoomLevel = 6;

    /**
     * Getter for property _viewZoomLevel - [1..19]
     * @return Value of property _viewZoomLevel.
     */
    public int getZoomLevel() {
        return viewZoomLevel;
    }

    /**
     * Setter for property _viewZoomLevel.
     * @param _viewZoomLevel New value of property _viewZoomLevel.
     */
    public void setZoomLevel(int _viewZoomLevel) {
        int old = viewZoomLevel;
        viewZoomLevel = _viewZoomLevel;
        
        viewZoomLevel = Math.max(viewZoomLevel, ZOOM_MIN);
        viewZoomLevel = Math.min(viewZoomLevel, mapInfos[mapProvider].getMaxZoomLevel());
        bnZoomPlus.setEnabled(
                bnZoomPlus.isEnabled() 
                && viewZoomLevel<mapInfos[mapProvider].getMaxZoomLevel());
        bnZoomMinus.setEnabled(
                bnZoomMinus.isEnabled()
                && viewZoomLevel>1);
        //System.out.println("Zoom:" + toString() + "we set:" + viewZoomLevel);        
        sbZoomLevel.setValue(viewZoomLevel);        
        if( old!=viewZoomLevel ){
            setMapProperty("ZoomLevel", viewZoomLevel);
        }
        propertyChangeSupport.firePropertyChange("viewZoomLevel", old, viewZoomLevel);
    }

    /**
     * Holds value of property _viewType.
     */
    private int viewType = VIEW_ROAD;

    /**
     * Getter for property viewType - combination of VIEW_XXXX consts
     * @return Value of property viewType.
     */
    public int getViewType() {
        return viewType;
    }

    /**
     * Setter for property viewType.
     * @param _viewType New value of property viewType.
     */
    protected void setViewType(int _viewType) {
        int old = viewType;
        viewType = _viewType;
        
        if( old!=viewType ){
            setMapProperty("View", _viewType);
        }

        int iVM = viewType & 3;
        btRoadView.setSelected(VIEW_ROAD==iVM);
        btSatelliteView.setSelected(VIEW_SATELLITE==iVM);
        btHybridView.setSelected((VIEW_ROAD|VIEW_SATELLITE)==iVM);

        propertyChangeSupport.firePropertyChange("viewType", old, viewType);
    }

    /**
     * Holds value of property mapProvider.
     */
    private int mapProvider = MAP_MS_LIVE;

    /**
     * Getter for property mapProvider - MAP_XXXX const
     * @return Value of property mapProvider.
     */
    public int getMapProvider() {
        return mapProvider;
    }

    /*
        final String _stViewState = (null==stViewState && stURL.equals(stAboutBlank))
                ? null
                : ((null==stViewState)
                    ? execJS("_getViewDescriptor();")
                    : stViewState);
        if(null!=_stViewState){
            String args[] = _stViewState.split(",");
            if(2<=args.length){
                setViewCenter(args[0] + "," + args[1]);
            }
            if(3<=args.length){
                setZoomLevel( Math.min(
                    Integer.parseInt(args[2]),
                    mapInfos[getMapProvider()].getMaxZoomLevel()));
            }
            if(4<=args.length){
                setViewType(Integer.parseInt(args[3]));
            }
        }
    */
    /**
     * Setter for property _mapProvider.
     * @param _mapProvider New value of property _mapProvider.
     */
    public void setMapProvider(int _mapProvider) {
        if(isMapReady()){
            //viewCenter = execJS("_getCenter()");
            setMapReady(false);
        }
        int old = mapProvider;
        mapProvider = _mapProvider;

        skipJSChanges = true;
        setZoomLevel(viewZoomLevel);
        skipJSChanges = false;
        setURL( mapInfos[mapProvider].getHTMLFile()
            /*+ "?" + viewCenter
            + "," + viewZoomLevel
            + "," + viewType*/);
        //set the limits
        btGoogleMap.setSelected(MAP_GOOGLE==mapProvider);
        btMicrosoftMap.setSelected(MAP_MS_LIVE==mapProvider);
        btYahooMap.setSelected(MAP_YAHOO==mapProvider);

        propertyChangeSupport.firePropertyChange("mapProvider", old, mapProvider);
    }
    
    public void onViewRequest()
    {
        execJS("_setViewDescriptor(" 
            + viewCenter
            + "," + viewZoomLevel
            + "," + viewType 
            + ");" );
    }
    
    public void onFoundAddress(final String args[])
    {
        SwingUtilities.invokeLater(new Runnable(){ public void run() {        
            if( 4 != args.length){
                JOptionPane.showConfirmDialog(
                    BrMap.this,
                    "The address was not found.",
                    "Warning",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.ERROR_MESSAGE);
            } else {
                setViewCenter(args[2] + "," + args[3]);
            }        
        }});//end posponed operation
    }

    public void onSelectionRectChanged(Rectangle rc, boolean bShow)
    {}

    final static int FREE_MOVE = 0; //free mouse move
    final static int SHOW_ZOOM_RECT = 1; //drow rect from ancor
    final static int HIDE_ZOOM_RECT = 2; //drow rect from ancor

    double distanse = 0.0;
    Rectangle rcOldSelect = null;
    Rectangle rcNewSelect = null;

    int map_state = FREE_MOVE;
    Point   ancor;
    Point2D ancorLL;
    
    public void onMouseEvent(final String type, final String args[])
    {
        Point pos = new Point(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
        final Point2D posLL = point2LL(pos);
        //System.out.println("posLL:" + posLL);
        int button = Integer.parseInt(args[4]);

        if(FREE_MOVE==map_state && 2==button){
            map_state = SHOW_ZOOM_RECT;
            ancor = pos;
            ancorLL = posLL;
        } else  if(
             ( SHOW_ZOOM_RECT==map_state && 0==(2 & button) )
          || ( SHOW_ZOOM_RECT==map_state && 2==button && "mouseup".equals(type))
        ){
            if(null!=ancor && !isSelectionMode()){
                SwingUtilities.invokeLater(new Runnable(){ public void run() {
                    skipJSChanges = true;
                    setZoomLevel(getBestZoomLevel(
                           ancorLL.getX(), ancorLL.getY(),
                           posLL.getX(), posLL.getY()));
                    setViewCenter( ((ancorLL.getX() + posLL.getX())/2)
                           + "," + ((ancorLL.getY() + posLL.getY())/2) );
                    skipJSChanges = false;
                    execJS( ":_setViewDescriptor("
                           + getViewCenter()
                           + "," + getZoomLevel() + ")" );
                    /*
                    execJS( ":setTimeout(\"_setViewDescriptor("
                           + getViewCenter()
                           + "," + getZoomLevel()
                           + ");\", 0);" );
                    */
                }});
            }
            //hide ZoomRect
            ancor = null;
            rcNewSelect = null;
            if(null!=rcOldSelect){
                onSelectionRectChanged(rcOldSelect, false);
                repaint(rcOldSelect);
            }
            rcOldSelect = null;
        } else if(SHOW_ZOOM_RECT==map_state && null!=ancor ){
            if(2==button){
                execJS( ":event_returnValue=true;event_cancelBubble=true;");
            }
            distanse = getDistance(
            //distanse = getDistanceInPixel(
                ancorLL.getX(), ancorLL.getY(),
                posLL.getX(), posLL.getY());
            //System.out.print(ancorLL.getX() + "," + ancorLL.getY() + "\n");
            if(null!=rcOldSelect){
                repaint(rcOldSelect);
            }
            rcNewSelect = new Rectangle(
                Math.min(ancor.x, pos.x),
                Math.min(ancor.y, pos.y),
                Math.abs(ancor.x - pos.x),
                Math.abs(ancor.y - pos.y)
            );
            onSelectionRectChanged(rcNewSelect, true);
            repaint(rcNewSelect);
        } else {
            map_state = FREE_MOVE;
        }
        setMouseGeoPos( new BrMapMousePos(pos, posLL) );
    }
    
    /**
     * Mouse geo and ordinal position holder.
     */
    public BrMapMousePos mouseGeoPos;

    /**
     * Get the value of mouseGeoPos.
     * @return the value of mouseGeoPos
     */
    public BrMapMousePos getMouseGeoPos() {
        return mouseGeoPos;
    }

    /**
     * Set the value of mouseGeoPos.
     * @param mouseGeoPos new value of mouseGeoPos
     */
    public void setMouseGeoPos(BrMapMousePos mouseGeoPos) {
        BrMapMousePos old = this.mouseGeoPos;
        this.mouseGeoPos = mouseGeoPos;
        propertyChangeSupport.firePropertyChange("mouseGeoPos", old, mouseGeoPos);
    }

    /**
     * Hold map script object state
     */
    public boolean mapReady = false;

    /**
     * Get the value of mapReady
     * @return the value of mapReady
     */
    public boolean isMapReady() {
        return mapReady;
    }

    /**
     * Set the value of mapReady
     * @param mapReady new value of mapReady
     */
    public void setMapReady(boolean _mapReady) {
        boolean old = mapReady;
        mapReady = _mapReady;
        if(null!=bnZoomMinus){
            btGoogleMap.setEnabled(mapReady);
            btHybridView.setEnabled(mapReady);
            btMicrosoftMap.setEnabled(mapReady);
            btRoadView.setEnabled(mapReady);
            btSatelliteView.setEnabled(mapReady);
            btYahooMap.setEnabled(mapReady);
            sbZoomLevel.setEnabled(mapReady);
            bnZoomMinus.setEnabled(mapReady);
            bnZoomPlus.setEnabled(mapReady);
            setZoomLevel(getZoomLevel());
        }    
        propertyChangeSupport.firePropertyChange("mapReady", old, mapReady);
    }

    /**
     * Holds right-mouse click rectangle selection mode
     */
    public boolean selectionMode = false;

    /**
     * Get the value of selectionMode
     * @return the value of selectionMode
     */
    public boolean isSelectionMode() {
        return selectionMode;
    }

    /**
     * Set the value of selectionMode
     * @param selectionMode new value of selectionMode
     */
    public void setSelectionMode(boolean selectionMode) {
        boolean old = this.selectionMode;
        this.selectionMode = selectionMode;
        propertyChangeSupport.firePropertyChange("selectionMode", old, selectionMode);
    }

    @Override
    public void setURL(String _stURL) {
        setMapReady(false);
        super.setURL(_stURL);
    }
}

class JBottomSlider extends JSlider
{
    @Override
    public void reshape(int x, int y, int width, int height) {
        Rectangle rc = getParent().getBounds();
        y = rc.height - height - 3;
        super.reshape(x, y, width, height);
    }
}