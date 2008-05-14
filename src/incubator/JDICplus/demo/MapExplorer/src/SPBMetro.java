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
import javax.swing.ImageIcon;
import org.jdic.web.BrMap;
import org.jdic.web.BrMapSprite;

/**
 * Class with St. Petersburg metro schema in geo coordinates.
 * @author uta
 */
class SPBMetro {
    BrMapSprite line1 = new BrMapSprite("SPb Metro Line 1", new Color(1.0F, 0.0F, 0.0F, 0.5F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };
    BrMapSprite line2 = new BrMapSprite("SPb Metro Line 2", new Color(0.0F, 0.0F, 1.0F, 0.5F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };
    BrMapSprite line3 = new BrMapSprite("SPb Metro Line 3", new Color(0.0F, 1.0F, 0.0F, 0.5F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };
    BrMapSprite line4 = new BrMapSprite("SPb Metro Line 4", new Color(1.0F, 0.8F, 0.3F, 0.8F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };

    BrMapSprite duke = new BrMapSprite("duke", null){
        ImageIcon img = new ImageIcon(getClass().getResource("images/java.png"));

        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            img.paintIcon(null, g, x[0] - 9, y[0] - 9);
        }
    };

    public SPBMetro(){
        line1.isPoligon = false;
        line1.LLs.add( new Point2D.Double(60.04993183627221,30.44260025024414));
        line1.LLs.add( new Point2D.Double(60.034502033818626,30.418052673339844));
        line1.LLs.add( new Point2D.Double(60.01228761005297,30.395565032958984));
        line1.LLs.add( new Point2D.Double(60.008834027824285,30.370845794677734));
        line1.LLs.add( new Point2D.Double(59.99997318904818,30.366125106811523));
        line1.LLs.add( new Point2D.Double(59.98467031810702,30.34376621246338));
        line1.LLs.add( new Point2D.Double(59.97094966918809,30.347285270690918));
        line1.LLs.add( new Point2D.Double(59.956965507618506,30.355310440063477));
        line1.LLs.add( new Point2D.Double(59.9443940587573,30.35994529724121));
        line1.LLs.add( new Point2D.Double(59.93138780071647,30.360546112060547));
        line1.LLs.add( new Point2D.Double(59.92743116676219,30.34797191619873));
        line1.LLs.add( new Point2D.Double(59.91644043083372,30.31848907470703));
        line1.LLs.add( new Point2D.Double(59.907102974165,30.29956340789795));
        line1.LLs.add( new Point2D.Double(59.90096980546479,30.274672508239746));
        line1.LLs.add( new Point2D.Double(59.87965631128972,30.262012481689453));
        line1.LLs.add( new Point2D.Double(59.86716326554173,30.261454582214355));
        line1.LLs.add( new Point2D.Double(59.85046264369983,30.269179344177246));
        line1.LLs.add( new Point2D.Double(59.841775006732156,30.252270698547363));

        line2.isPoligon = false;
        line2.LLs.add( new Point2D.Double(60.05134587413322,30.33252239227295));
        line2.LLs.add( new Point2D.Double(60.0370741682298,30.321407318115234));
        line2.LLs.add( new Point2D.Double(60.01659871888036,30.315699577331543));
        line2.LLs.add( new Point2D.Double(60.002224,30.296516));
        line2.LLs.add( new Point2D.Double(59.985357274664224,30.300850868225098));
        line2.LLs.add( new Point2D.Double(59.96642855457519,30.311365127563477));
        line2.LLs.add( new Point2D.Double(59.956116816299456,30.31881093978882));
        line2.LLs.add( new Point2D.Double(59.93517196554956,30.328617095947266));
        line2.LLs.add( new Point2D.Double(59.926990316913894,30.320441722869873));
        line2.LLs.add( new Point2D.Double(59.90622072842308,30.31750202178955));
        line2.LLs.add( new Point2D.Double(59.891531121270596,30.31773805618286));
        line2.LLs.add( new Point2D.Double(59.87921482676433,30.318467617034912));
        line2.LLs.add( new Point2D.Double(59.86630150313929,30.321943759918213));
        line2.LLs.add( new Point2D.Double(59.85203609220049,30.32172918319702));
        line2.LLs.add( new Point2D.Double(59.833149799540486,30.349559783935547));
        line2.LLs.add( new Point2D.Double(59.82967752298239,30.375566482543945));

        line3.isPoligon = false;
        line3.LLs.add( new Point2D.Double(59.94834866950482,30.234460830688477));
        line3.LLs.add( new Point2D.Double(59.94252405136852,30.278191566467285));
        line3.LLs.add( new Point2D.Double(59.93388195785884,30.33350944519043));
        line3.LLs.add( new Point2D.Double(59.93145230714296,30.35501003265381));
        line3.LLs.add( new Point2D.Double(59.929560066503775,30.360889434814453));
        line3.LLs.add( new Point2D.Double(59.9242268075892,30.38522243499756));
        line3.LLs.add( new Point2D.Double(59.89670820033645,30.423717498779297));
        line3.LLs.add( new Point2D.Double(59.877244226779155,30.4416561126709));
        line3.LLs.add( new Point2D.Double(59.865116543375315,30.470194816589355));
        line3.LLs.add( new Point2D.Double(59.848436,30.45770645));
        line3.LLs.add( new Point2D.Double(59.83101471582272,30.500707626342773));

        line4.isPoligon = false;
        line4.LLs.add( new Point2D.Double(60.00842643842314,30.25905132293701));
        line4.LLs.add( new Point2D.Double(59.98980068049731,30.25536060333252));
        line4.LLs.add( new Point2D.Double(59.97172281464221,30.259780883789062));
        line4.LLs.add( new Point2D.Double(59.96106899949782,30.291881561279297));
        line4.LLs.add( new Point2D.Double(59.95174410010812,30.29055118560791));
        line4.LLs.add( new Point2D.Double(59.92605483563711,30.317373275756836));
        line4.LLs.add( new Point2D.Double(59.92816231944967,30.34595489501953));
        line4.LLs.add( new Point2D.Double(59.920269,30.355396));
        line4.LLs.add( new Point2D.Double(59.92349556816952,30.383291244506836)); //pl AN
        line4.LLs.add( new Point2D.Double(59.929065485817034,30.411357879638672));
        line4.LLs.add( new Point2D.Double(59.9323553839424,30.439252853393555)); //Ladozhskaya
        line4.LLs.add( new Point2D.Double(59.91986063898404,30.466718673706055));
        line4.LLs.add( new Point2D.Double(59.907361187996464,30.48332691192627));

        duke.LLs.add( new Point2D.Double(59.91237,30.295046));
    }

    public void add(BrMap m){
        m.getSprites().add(line1);
        m.getSprites().add(line2);
        m.getSprites().add(line3);
        m.getSprites().add(line4);
        m.getSprites().add(duke);
        m.setViewCenter("59.94, 30.33");
        m.setZoomLevel(12);
    }
}