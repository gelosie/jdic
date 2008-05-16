package org.jdic.web;

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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JEditorPane;
import javax.swing.border.Border;


/**
 * Balloon geo space holder
 * @author uta
 */
public class BrMapBalloonSprite extends BrMapSprite {
    public enum Alignment {
        LEFT_ALIGNED_ABOVE, 
        RIGHT_ALIGNED_ABOVE, 
        LEFT_ALIGNED_BELOW, 
        RIGHT_ALIGNED_BELOW};

    JEditorPaneNoDraw txtPane;
    RoundedBalloonBorder border; 
    
    public BrMapBalloonSprite(
        String htmlText, 
        Point2D Location,
        Alignment alignment,            
        Dimension size,
        int hOffset, 
        int vOffset, 
        int arcWidth, 
        int arcHeight, 
        Color borderColor, 
        Color fillColor)   
    {
        super(htmlText, fillColor);
        LLs.add(Location);
        txtPane = new JEditorPaneNoDraw();
        border = new RoundedBalloonBorder(
            alignment,
            hOffset, 
            vOffset, 
            arcWidth, 
            arcHeight, 
            borderColor, 
            fillColor);   
        txtPane.setBorder(border);
        txtPane.setContentType("text/html");
        txtPane.setText(htmlText);
        txtPane.setBounds(new Rectangle(size));        
        txtPane.setEditable(false);        
        txtPane.setOpaque(false);
    }
    
    public BrMapBalloonSprite(
        String htmlText, 
        Point2D Location)
    {
        this(
            htmlText,
            Location,
            Alignment.LEFT_ALIGNED_ABOVE,
            new Dimension(100, 100),
            16, 16,
            16, 16, 
            new Color(0F, 0F, 0F),
            new Color(1F, 1F, 1F, 0.8F));
    }    
            
    @Override
    public void paint(Graphics g, int[] x, int[] y) {
        Graphics2D g2 = (Graphics2D)g.create();
        int dx = 0, dy = 0;
        Dimension sz = txtPane.getSize();
        switch(border.alignment){
            case LEFT_ALIGNED_ABOVE:
                dx = border.vOffset;
                dy = (int)sz.getHeight();                
                break;        
            case RIGHT_ALIGNED_ABOVE:
                dx = (int)sz.getWidth() - border.vOffset;
                dy = (int)sz.getHeight();                                
                break;
            case LEFT_ALIGNED_BELOW:
                dx = border.vOffset;
                break;
            case RIGHT_ALIGNED_BELOW:
                dx = (int)sz.getWidth() - border.vOffset;
                break;
        }
        g2.translate(x[0] - dx, y[0] - dy);
        txtPane.paint(g2);
        g2.dispose();
    }
    
    public void add(BrMap m){
        m.getSprites().add(this);
    }
}

class RoundedBalloonBorder implements Border {
    public final BrMapBalloonSprite.Alignment alignment;
    public final int hOffset;
    public final int vOffset;
    private final int arcWidth;
    private final int arcHeight;
    private final Color fillColor;
    private final Color borderColor;

    Dimension lastSize;
    Insets insets = new Insets(0, 0, 0, 0);

    public RoundedBalloonBorder(
            BrMapBalloonSprite.Alignment alignment, 
            int hOffset, 
            int vOffset, 
            int arcWidth, 
            int arcHeight, 
            Color borderColor, 
            Color fillColor) 
    {
        this.alignment = alignment;
        this.hOffset = hOffset;
        this.vOffset = vOffset;
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.borderColor = borderColor;
        this.fillColor = fillColor;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        Dimension currentSize = c.getSize();

        if (currentSize.equals(lastSize)) {
            return insets;
        }
        
        final int divFactor = 3;
        switch (alignment) {
            case LEFT_ALIGNED_ABOVE:
            case RIGHT_ALIGNED_ABOVE:
                insets = new Insets(arcHeight/divFactor, arcWidth/divFactor, arcHeight/divFactor + vOffset, arcWidth/2);
                break;
            case LEFT_ALIGNED_BELOW:
            case RIGHT_ALIGNED_BELOW:
                insets = new Insets(vOffset+arcHeight/divFactor, arcWidth/divFactor, arcHeight/divFactor, arcWidth/divFactor);
                break;
        }
        
        lastSize = currentSize;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return true;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int bWidth, int bHeight) {
        int rectY = y;
        if (alignment == BrMapBalloonSprite.Alignment.LEFT_ALIGNED_BELOW ||
            alignment == BrMapBalloonSprite.Alignment.RIGHT_ALIGNED_BELOW) {
            rectY = y + vOffset;
        }
        int[] triangleX = {x+hOffset, x+hOffset+vOffset, x+hOffset};
        int[] triangleY = {y+bHeight-vOffset-1, y+bHeight-vOffset-1, y+bHeight-1};

        if (alignment == BrMapBalloonSprite.Alignment.LEFT_ALIGNED_BELOW ||
            alignment == BrMapBalloonSprite.Alignment.RIGHT_ALIGNED_BELOW) {
            for (int i = 0; i < triangleX.length; ++i) {
                triangleY[i] = bHeight - triangleY[i] - 1;
            }
        }

        if (alignment == BrMapBalloonSprite.Alignment.RIGHT_ALIGNED_ABOVE ||
            alignment == BrMapBalloonSprite.Alignment.RIGHT_ALIGNED_BELOW) {
            for (int i = 0; i < triangleX.length; ++i) {
                triangleX[i] = bWidth - triangleX[i] - 1;
            }
        }
        
        Area tooltip = new Area();
        RoundRectangle2D.Double viewZone = new RoundRectangle2D.Double(
            x, rectY, 
            bWidth, bHeight-vOffset, 
            arcWidth*2, arcHeight*2);
        tooltip.add(new Area(viewZone));
        tooltip.add(new Area(new Polygon(
                triangleX, triangleY, 3)));
        
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(fillColor);        
        g2.fill(tooltip);
        g2.setColor(borderColor);        
        g2.draw(tooltip);
        
        g2.setClip(viewZone);
        ((JEditorPaneNoDraw)c).paintComponentOverBounds(g);
    }
}

class JEditorPaneNoDraw extends JEditorPane
{
   @Override
    protected void paintComponent(Graphics g) {}
   
    public void paintComponentOverBounds(Graphics g) {
        super.paintComponent(g);
    }
};
