/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jdic.web;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The map object that lays over another map.
 * @author uta
 */
public class BrMapOver extends BrMap 
        implements PropertyChangeListener 
{
    public BrMapOver()
    {
        super();
        setOpaque(false);                
        sbAlpha.setVisible(true);
        sbAlpha.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                fAlpha = 1.0f - ((float)sbAlpha.getValue())/((float)sbAlpha.getMaximum());
                repaint();
            }
        });
        addPropertyChangeListener(this);
    }

    /**
     * Transparency coefficient
     */
    float fAlpha = 0.0f;
    
    /**
     * Master map proterty holder
     */
    private BrMap syncMap;

    /**
     * Getter for syncMap property
     * @return master map object
     */
    public BrMap getSyncMap() {
        return syncMap;
    }

    /**
     * Setter for syncMap property
     * @param syncMap sets master map object
     */
    public void setSyncMap(BrMap _syncMap) {
        if(null!=syncMap){
            syncMap.removePropertyChangeListener(this);
        }
        syncMap = _syncMap;
        if(null!=syncMap){            
            syncMap.addPropertyChangeListener(this);
            syncMaps();            
        }
    }
    
    @Override
    public void paintClientArea(Graphics g, boolean withPeer) {
        super.paintClientArea(g, withPeer);                
        if(syncMap.isMapReady()){
            Graphics2D g2 = (Graphics2D)g.create(); 
            g2.translate(-getX(), -getY());
            AlphaComposite ac = 
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fAlpha); 
            g2.setComposite(ac);
            syncMap.paintClientArea(g2, true);
            g2.dispose();
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String pn = evt.getPropertyName();
        if( pn.equals("viewCenter") 
           || pn.equals("viewZoomLevel") 
           || pn.equals("mapReady") )
        {
            syncMaps();
        }    
    }
    
    public void syncMaps()
    {
        if(!isMapReady()){
            setZoomLevel(syncMap.getZoomLevel());            
            setViewCenter(syncMap.getViewCenter());
        } else {
            syncMaps(true);
        }
    }
    
    public void syncMaps(boolean bAsMaster)
    {
        if( isMapReady() && syncMap.isMapReady() ){
            BrMap src = bAsMaster ? syncMap: this;
            BrMap dst = bAsMaster ? this: syncMap;
            
            dst.skipJSChanges = true;
            dst.setZoomLevel(src.getZoomLevel());            
            
            Point2D center = src.getPoint(
              bAsMaster
              ? syncMap.execJS( "_fromPointToLatLng("
                + (getX() + getWidth()/2) + ","
                + (getY() + getHeight()/2) + ")" )
              : execJS( "_fromPointToLatLng("
                + (-getX() + syncMap.getWidth()/2) + ","
                + (-getY() + syncMap.getHeight()/2) + ")" ));
            
            Point2D centerOld = dst.getPoint(dst.getViewCenter());
            if( 1.0 <= dst.getDistanceInPixel(
                    center.getX(), center.getY(), 
                    centerOld.getX(), centerOld.getY()) )
            {    
                try{
                    dst.setViewCenter(center.getX() + "," + center.getY());
                    dst.execJS( "_setViewDescriptor("
                            + center.getX() + "," + center.getY() + "," 
                            + src.getZoomLevel() + ")" );
                    dst.skipJSChanges = true;
                }catch(Exception e){}    
            }    
        }    
    }

    @Override
    public void setBounds(Rectangle r) {
        super.setBounds(r);
        if(null!=syncMap){
            syncMap.validate();
            syncMaps();
        }    
    }
    
    public void processJSEvents(String st)
    {
        String args[] = st.split(",");
        String type = args[1].toLowerCase();
        if(null!=syncMap && syncMap.isMapReady() && isMapReady() && type.contains("mouse")){
            args[2] = (getX() + Integer.parseInt(args[2])) + "";
            args[3] = (getY() + Integer.parseInt(args[3])) + "";
            st = args[0];
            for(int i=1; i<args.length; ++i){
                st += "," + args[i];
            }    
            //final String _st = st;
            //java.awt.EventQueue.invokeLater(new Runnable() {public void run() {            
                syncMap.processJSEvents(st);
            //}});    
            int button = Integer.parseInt(args[4]);
            if(2==button){
                execJS( ":event_returnValue=true;event_cancelBubble=true;");
            }
        } else {
            super.processJSEvents(st);
        }
        
    }    
}
