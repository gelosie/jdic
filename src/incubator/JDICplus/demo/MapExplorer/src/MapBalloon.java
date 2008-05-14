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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import org.jdic.web.BrMap;
import org.jdic.web.BrMapSprite;

/**
 * Balloon geo space holder
 * @author uta
 */
public class MapBalloon extends BrMapSprite {
    int iStepCount = 100;
    JEditorPane txtPane;
    
    MapBalloon(String title, Point2D Location){
        super(title, new Color(0.0F, 0.0F, 1.0F, 0.25F));
        LLs.add(Location);
        txtPane = new JEditorPane();
        txtPane.setBorder(new RoundedBalloonBorder(
                RoundedBalloonBorder.Alignment.LEFT_ALIGNED_BELOW,
                10, 10,
                5, 5, 
                new Color(0.0F, 0.0F, 0.0F),
                new Color(1F, 1F, 1F, 0.4F)
        ));
        txtPane.setContentType("text/html");
        txtPane.setEditable(false);
        txtPane.setText("<html><b style='color:black'>Location:</b><br><b style='color:red'>"+ title + "</b></html>");
        txtPane.setBounds(new Rectangle(0, 0, 100, 100));        
        txtPane.doLayout();
        txtPane.setOpaque(false);
    }
    
    @Override
    public void paint(Graphics g, int[] x, int[] y) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.translate(x[0], y[0]);
        txtPane.paint(g2);
        txtPane.paintComponents(g2);
        g2.dispose();
    }
    
    public void add(BrMap m){
        m.getSprites().add(this);
        if(name.contains("Greenwich")){
            MapMeridian mm = new MapMeridian(new Color(0xFF, 0x00, 0x00));
            mm.add(m);
        }
    }
}

class RoundedBalloonBorder implements Border {
    public enum Alignment {LEFT_ALIGNED_ABOVE, RIGHT_ALIGNED_ABOVE, LEFT_ALIGNED_BELOW, RIGHT_ALIGNED_BELOW};
    public enum TriangleTipLocation {AUTOMATIC, CENTER, NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST};
    
    private final Alignment alignment;
    private final int hOffset;
    private final int vOffset;
    private final int arcWidth;
    private final int arcHeight;
    private final Color fillColor;
    private final Color borderColor;

    Dimension lastSize;
    Insets insets = new Insets(0, 0, 0, 0);

    public RoundedBalloonBorder(Alignment alignment, int hOffset, int vOffset, int arcWidth, int arcHeight, Color borderColor, Color fillColor) {
        this.alignment = alignment;
        this.hOffset = hOffset;
        this.vOffset = vOffset;
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        this.borderColor = borderColor;
        this.fillColor = fillColor;
    }

    public Insets getBorderInsets(Component c) {
        Dimension currentSize = c.getSize();

        if (currentSize.equals(lastSize)) {
            return insets;
        }

        switch (alignment) {
            case LEFT_ALIGNED_ABOVE:
            case RIGHT_ALIGNED_ABOVE:
                insets = new Insets(arcHeight, arcWidth, arcHeight+vOffset, arcWidth);
                break;
            case LEFT_ALIGNED_BELOW:
            case RIGHT_ALIGNED_BELOW:
                insets = new Insets(vOffset+arcHeight, arcWidth, arcHeight, arcWidth);
                break;
        }

        lastSize = currentSize;

        return insets;
    }

    public boolean isBorderOpaque() {
        return false;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int bWidth, int bHeight) {
        int rectY = y;
        if (alignment == Alignment.LEFT_ALIGNED_BELOW ||
            alignment == Alignment.RIGHT_ALIGNED_BELOW) {
            rectY = y + vOffset;
        }
        g.setColor(fillColor);
        g.fillRoundRect(x, rectY, bWidth, bHeight-vOffset, arcWidth*2, arcHeight*2);
        g.setColor(borderColor);
        g.drawRoundRect(x, rectY, bWidth-1, bHeight-vOffset-1, arcWidth*2, arcHeight*2);

        int[] triangleX = {x+hOffset, x+hOffset+vOffset, x+hOffset};
        int[] triangleY = {y+bHeight-vOffset-1, y+bHeight-vOffset-1, y+bHeight-1};

        if (alignment == Alignment.LEFT_ALIGNED_BELOW ||
            alignment == Alignment.RIGHT_ALIGNED_BELOW) {
            int flipAxis = bHeight-1;
            for (int i = 0; i < triangleX.length; i++) {
                Point flippedPoint = FlipUtils.flipHorizontally(triangleX[i], triangleY[i], flipAxis);
                triangleX[i] = flippedPoint.x;
                triangleY[i] = flippedPoint.y;
            }
        }

        if (alignment == Alignment.RIGHT_ALIGNED_ABOVE ||
            alignment == Alignment.RIGHT_ALIGNED_BELOW) {
            int flipAxis = bWidth-1;
            for (int i = 0; i < triangleX.length; i++) {
                Point flippedPoint = FlipUtils.flipVertically(triangleX[i], triangleY[i], flipAxis);
                triangleX[i] = flippedPoint.x;
                triangleY[i] = flippedPoint.y;
            }
        }

        g.setColor(fillColor);
        g.fillPolygon(triangleX, triangleY, 3);
        g.setColor(borderColor);
        g.drawLine(triangleX[0], triangleY[0], triangleX[2], triangleY[2]);
        g.drawLine(triangleX[1], triangleY[1], triangleX[2], triangleY[2]);

        // bug workaround, Java Bug Database ID 6644471
        g.setColor(fillColor);
        g.drawLine(triangleX[0], triangleY[0], triangleX[1], triangleY[1]);
        g.setColor(borderColor);
        g.drawLine(triangleX[0], triangleY[0], triangleX[0], triangleY[0]);
        g.drawLine(triangleX[1], triangleY[1], triangleX[1], triangleY[1]);
    }
}

class FlipUtils {
	private FlipUtils() {};

	public static Point flipHorizontally(int x, int y, int flipAxis) {
	    Point p = new Point(x, y);
	    p.move(p.x, flipAxis-p.y);
	    return p;
	}

    public static Point flipVertically(int x, int y, int flipAxis) {
        Point p = new Point(x, y);
        p.move(flipAxis-p.x, p.y);
        return p;
    }

}
