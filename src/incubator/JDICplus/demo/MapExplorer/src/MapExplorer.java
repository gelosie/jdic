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

import org.jdic.web.BrMap;
import org.jdic.web.BrMapSprite;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.OrientationRequested;
import javax.print.attribute.standard.JobName;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.awt.*;
import java.awt.print.Printable;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileWriter;


/**
 * Sample map explorer implementation.
 * @author  uta
 */
public class MapExplorer extends JFrame
{
    MapExplorer() {
        BrMap.DESIGN_MODE = false;

        setTitle("Maps Clipper");

        Panel rootPanel = new Panel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        rootPanel.setLayout(gridbag);


        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;


        final  MapClipper mapM = new MapClipper(BrMap.MAP_GOOGLE);
        mapM.setBounds(0, 0, 740, 600);
        mapM.setPreferredSize(new Dimension(740, 600));
                                                                                    
        c.gridwidth = GridBagConstraints.REMAINDER; //end row REMAINDER
        gridbag.setConstraints(mapM, c);
        rootPanel.add(mapM);

        {
            JTextField help = new JTextField("Use pushed right mouse button for zoomed area selection.");
            help.setPreferredSize(new Dimension(325, 10));
            help.setBounds(50,10,325,16);
            mapM.add(help, BorderLayout.LINE_END);

            /*
            final BrMapSprite[] bs = new BrMapSprite[]{new BrMapSprite()};
            bs[0].isPoligon = false;

            JButton btSave = new JButton();
            btSave.setPreferredSize(new Dimension(32, 32));
            btSave.setBounds(6,110,32,32);
            btSave.setIcon(new ImageIcon(getClass().getResource("images/save.png")));
            btSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser fc = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                          "Map Files", "plg");
                        fc.setFileFilter(filter);
                        if( JFileChooser.APPROVE_OPTION == fc.showDialog(MapExplorer.this, "Save map") ){
                            bs[0].save(fc.getSelectedFile().getAbsolutePath());                            
                            mapM.getSprites().add(bs[0]);
                            bs[0] = new BrMapSprite();
                            mapM.repaint();
                        }
                    }
                }
            );
            mapM.add(btSave);

            JButton btStart = new JButton();
            btStart.setPreferredSize(new Dimension(32, 32));
            btStart.setBounds(6,110+40,32,32);
            btStart.setIcon(new ImageIcon(getClass().getResource("images/pluse.png")));
            btStart.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        bs[0].LLs.add( new Point2D.Double(
                            Double.parseDouble(mapM.edLat.getText()),
                            Double.parseDouble(mapM.edLng.getText())    
                        ));
                        mapM.repaint();
                    }
                }
            );
            mapM.add(btStart);
            */

            final SPBMetro mt =  new SPBMetro();
            JButton btMetro = new JButton();
            btMetro.setPreferredSize(new Dimension(46, 46));
            btMetro.setBounds(12,310/*+40+40*/,46,46);
            btMetro.setIcon(new ImageIcon(getClass().getResource("images/metro.png")));
            btMetro.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        mt.add(mapM);
                        mapM.repaint();
                    }
                }
            );
            mapM.add(btMetro);

            JButton btPrint = new JButton();
            btPrint.setPreferredSize(new Dimension(46, 46));
            btPrint.setBounds(12,310/*+40+40*/ + 50, 46, 46);
            btPrint.setIcon(new ImageIcon(getClass().getResource("images/print.png")));
            btPrint.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
                        PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
                        aset.add(OrientationRequested.LANDSCAPE);
                        aset.add(new Copies(1));
                        aset.add(new JobName("Map", null));
                        // locate a print service that can handle the request
                        PrintService[] services =
                                PrintServiceLookup.lookupPrintServices(flavor, aset);
                        if (services.length > 0) {
                                System.out.println("selected printer " + services[0].getName());
                                /// create a print job for the chosen service
                                DocPrintJob pj = services[0].createPrintJob();
                                try {
                                        //Create a Doc object to hold the print data.
                                        Doc doc = new SimpleDoc(mapM, flavor, null);
                                        //print mapM
                                        pj.print(doc, aset);
                                } catch(PrintException e) {
                                        System.err.println(e);
                                }
                        }

                    }
                }
            );
            mapM.add(btPrint);
        }
        //c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last in row
        c.weightx = 0.0;//reset to the default
        c.weighty = 0.0;

        //
        Dimension sz = new Dimension(100, 20);

        {
            JPanel p1 = new JPanel();
            gridbag.setConstraints(p1, c);
            rootPanel.add(p1);


            mapM.cbSelectWorkingAria = new JCheckBox("Select Area", false);
            p1.add(mapM.cbSelectWorkingAria, BorderLayout.LINE_END);

            mapM.cbFrosenArea = new JCheckBox("Froze Area", false);
            p1.add(mapM.cbFrosenArea, BorderLayout.LINE_END);

            JLabel lbAX = new JLabel("X:");
            p1.add(lbAX, BorderLayout.LINE_START);
            mapM.edAX = new JTextField("X");
            mapM.edAX.setPreferredSize(sz);
            p1.add(mapM.edAX, BorderLayout.CENTER);

            JLabel lbAY = new JLabel("Y:");
            p1.add(lbAY, BorderLayout.CENTER);
            mapM.edAY = new JTextField("Y");
            mapM.edAY.setPreferredSize(sz);
            p1.add(mapM.edAY, BorderLayout.LINE_END);

            JLabel lbAWidth = new JLabel("W:");
            p1.add(lbAWidth, BorderLayout.LINE_START);
            mapM.edAWidth = new JTextField("W");
            mapM.edAWidth.setPreferredSize(sz);
            p1.add(mapM.edAWidth, BorderLayout.CENTER);

            JLabel lbAHeight = new JLabel("H:");
            p1.add(lbAHeight, BorderLayout.CENTER);
            mapM.edAHeight = new JTextField("H");
            mapM.edAHeight.setPreferredSize(sz);
            p1.add(mapM.edAHeight, BorderLayout.LINE_END);
        }
        {
            JPanel p2 = new JPanel();
            gridbag.setConstraints(p2, c);
            rootPanel.add(p2);

            mapM.cbGrag = new JCheckBox("gg\u00BAmm\'ss\"", true);
            p2.add(mapM.cbGrag, BorderLayout.LINE_END);

            JLabel lbLat = new JLabel("Lat:");
            p2.add(lbLat, BorderLayout.LINE_END);
            mapM.edLat = new JTextField("Lat");
            mapM.edLat.setPreferredSize(sz);
            p2.add(mapM.edLat, BorderLayout.LINE_END);

            JLabel lbLng = new JLabel("Lng:");
            p2.add(lbLng, BorderLayout.LINE_END);
            mapM.edLng = new JTextField("Lng");
            mapM.edLng.setPreferredSize(sz);
            p2.add(mapM.edLng, BorderLayout.LINE_END);

            JButton btSave = new JButton("Save OZI explorer map");
            btSave.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        JFileChooser fc = new JFileChooser();
                        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                          "Map Files", "bmp");
                        fc.setFileFilter(filter);
                        if( JFileChooser.APPROVE_OPTION == fc.showDialog(MapExplorer.this, "Save map") ){
                            mapM.saveMap(fc.getSelectedFile().getAbsolutePath());
                            mapM.addBoundAsSprite();
                            mapM.repaint();                            
                        }
                    }
                }
            );

            mapM.cbGrid = new JCheckBox("cbGrid", false);
            mapM.cbGrid.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        mapM.repaint();
                    }
                }
            );

            p2.add(mapM.cbGrid, BorderLayout.LINE_END);

            JLabel lbGridStep = new JLabel("Step(m):");
            p2.add(lbGridStep, BorderLayout.LINE_END);
            mapM.edAGridStep = new JTextField("100");
            mapM.edAGridStep.setPreferredSize(sz);
            p2.add(mapM.edAGridStep, BorderLayout.LINE_END);

            p2.add(btSave, BorderLayout.LINE_END);

            JButton btClear = new JButton("Clear coverage");
            btClear.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        mapM.getSprites().clear();
                        mapM.repaint();
                    }
                }
            );
            p2.add(btClear, BorderLayout.LINE_END);

        }

        add(rootPanel);
        pack();
        setVisible(true);        
        mapM.bCanPaint = true;

        addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            }
        );
    }

    public static void main(String [] args) {
        MapExplorer test = new MapExplorer();
    }

}

class MapClipper extends BrMap  implements Printable {
    public Rectangle rcArea;
    public boolean bShowArea = false;
    //public boolean bCanPaint = false;

    public JCheckBox  cbGrag;
    public JTextField edLat;
    public JTextField edLng;
    public JTextField edAX;
    public JTextField edAY;
    public JTextField edAWidth;
    public JTextField edAHeight;
    public JTextField edAGridStep;
    public JCheckBox  cbSelectWorkingAria;
    public JCheckBox  cbFrosenArea;
    public JCheckBox  cbGrid;


    public MapClipper(String stWhichMap)
    {
        super(stWhichMap);                
    }
    public void addBoundAsSprite()
    {
        if(null!=rcArea){
            BrMapSprite pg = new BrMapSprite();
            pg.createFromPoints(this, new Point[] {
                new Point(rcArea.x, rcArea.y),
                new Point(rcArea.x + rcArea.width, rcArea.y),
                new Point(rcArea.x + rcArea.width, rcArea.y + rcArea.height),
                new Point(rcArea.x, rcArea.y + rcArea.height)
            });
           getSprites().add(pg);
        }
    }
    public void paintGrid(Graphics g) {
        if(cbGrid.isValid() && cbGrid.isSelected()){
            Graphics2D g2 = (Graphics2D)g;
            try{
                double gridStep = Double.parseDouble(edAGridStep.getText());

                String result = execJS("_getArea("
                    + rcArea.x + "," + rcArea.y + "," + rcArea.width + "," + rcArea.height + ")"
                );
                String args[] = result.split(",");
                double m2p = Double.parseDouble(args[8]);

                double step = gridStep/m2p;
                if( (step >= 3.0) && ((rcArea.height/step)<1000.0) ){
                    //the number of point in grid is big enouth
                    double iX0 = rcArea.x;
                    double iY0 = rcArea.y;
                    if(true){
                        //grid lines
                        Color colorsR[] = new Color[] {
                            new Color(1.0F, 0.0F, 0.0F, 1.0F),
                            new Color(0.0F, 1.0F, 0.0F, 1.0F),
                            new Color(0.0F, 0.0F, 1.0F, 1.0F),
                            new Color(1.0F, 1.0F, 0.0F, 1.0F),
                            new Color(0.0F, 1.0F, 1.0F, 1.0F),

                            new Color(0.5F, 0.0F, 0.0F, 1.0F),
                            new Color(0.0F, 0.5F, 0.0F, 1.0F),
                            new Color(0.0F, 0.0F, 0.5F, 1.0F),
                            new Color(0.5F, 0.5F, 0.0F, 1.0F),
                            new Color(0.0F, 0.5F, 0.5F, 1.0F)
                        };

                        int iC = 0;
                        for(double iX = iX0; iX<(rcArea.x + rcArea.width); iX += step){
                            g2.setColor( colorsR[ iC % colorsR.length ] );
                            g2.drawLine((int)iX, rcArea.y, (int)iX, rcArea.y + rcArea.height - 1);
                            ++iC;
                        }
                        iC = 0;
                        for(double iY = iY0; iY<(rcArea.y + rcArea.height); iY += step){
                            g2.setColor( colorsR[ iC % colorsR.length ] );
                            g2.drawLine(rcArea.x, (int)iY, rcArea.x + rcArea.width - 1, (int)iY);
                            ++iC;
                        }
                    } else {
                        Color colorsR[] = new Color[] {
                            new Color(1.0F, 0.0F, 0.0F, 0.15F),
                            new Color(0.0F, 1.0F, 0.0F, 0.15F),
                            new Color(0.0F, 0.0F, 1.0F, 0.15F)
                        };

                        Color colorsC[] = new Color[] {
                            new Color(0.5F, 0.5F, 0.0F, 0.15F),
                            new Color(0.0F, 0.5F, 0.5F, 0.15F),
                            new Color(0.5F, 0.0F, 0.5F, 0.15F)
                        };

                        boolean bStartLine = false;
                        int iRow = 0;
                        for(double iX = iX0; iX<(rcArea.x + rcArea.width); iX += step){
                            ++iRow;
                            int iCol = 0;
                            bStartLine = !bStartLine;
                            boolean bDraw = bStartLine;
                            for(double iY = iY0; iY<(rcArea.y + rcArea.height); iY += step){
                               ++iCol;
                               g2.setColor( bDraw
                                 ? colorsR[ iRow % colorsR.length ]
                                 : colorsC[ iCol % colorsC.length ]);
                               Rectangle rc = new Rectangle(
                                       (int)iX, (int)iY,
                                       (int)Math.min( step - 0.5, rcArea.x + rcArea.width - iX),
                                       (int)Math.min( step - 0.5, rcArea.y + rcArea.height - iY) );
                               g2.fill(rc);
                               bDraw = !bDraw;
                            }
                        }
                    }
                }
            }catch(NumberFormatException e){}
        }
    }

    @Override
    public synchronized Image getImage(int x, int y, int w, int h)
    {
        Image updateImage = super.getImage(x, y, w, h);
        if( null!=updateImage ){
            Graphics g1 = updateImage.getGraphics();
            if(null!=g1){
                try{
                    g1.translate(-x, -y);
                    paintGrid(g1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                g1.dispose();                        
            }
        }
        return updateImage;
    }

    @Override
    public void paintContent(Graphics g) {
        super.paintContent(g);
        if(null!=rcArea){
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(Color.BLACK);
            g2.drawRect(
                rcArea.x, rcArea.y,
                rcArea.width-1, rcArea.height-1
            );
        }
    }
    public boolean bCanPaint = false;
    
    @Override
    public void reshape(int x, int y, int width, int height)
    {
        super.reshape(x, y, width, height);
        if( null!=cbFrosenArea && !cbFrosenArea.isSelected() ){
            rcArea = new Rectangle(x + 68, y + 28, width - 68 - 2, height - 28 - 28);
            if(bCanPaint){
                areaDisplay();
            }
        }
    }

    public static String getGrad(double d, char[] ch){
        char c = (d > 0) ? ch[0] : ch[1];
        d = Math.abs(d);
        double G = Math.floor(d);
        double M = (d - G)*60.0;
        double S = (M - Math.floor(M))*60.0;
        return String.format("%d\u00BA%d\'%2.2f\" %c", (int)G, (int)Math.floor(M), S, c);        
    }
    
    @Override
    public void OnFreeMove(int x, int y, double lat, double lng)
    {
        final char[] EW = new char[] {'E', 'W'};
        final char[] NS = new char[] {'N', 'S'};
        //saveNativeCursor();
        edLat.setText( cbGrag.isSelected() ? getGrad(lat, NS) : ("" + lat));
        edLng.setText( cbGrag.isSelected() ? getGrad(lng, EW) : ("" + lng));
        //restoreNativeCursor();
    }
    public void areaDisplay()
    {
        if(null!=edAX){
            edAX.setText( "" + rcArea.x );
            edAY.setText( "" + rcArea.y );
            edAWidth.setText( "" + rcArea.width );
            edAHeight.setText( "" + rcArea.height );
        }            
    }

    @Override
    public void OnZoomRect(Rectangle rc, boolean bShow)
    {
        if(cbSelectWorkingAria.isSelected()){
            rcArea = new Rectangle(rc);
            areaDisplay();
        }
    }
    public void saveMap(String stFN)
    {
        System.out.println("save to " + stFN);
        if(null!=rcArea){
            Image im = getImage(rcArea.x, rcArea.y, rcArea.width, rcArea.height);
            if(null!=im){
                try{
                    stFN = stFN.toLowerCase();
                    if(!stFN.endsWith(".bmp")){
                       stFN += ".bmp"; 
                    }
                    File fn = new File(stFN);
                    ImageIO.write((RenderedImage)im, "BMP", fn);

                    int read = 1024;
                    InputStreamReader rd = new InputStreamReader(
                            getClass().getResourceAsStream("temp_ozf.map"));
                    char[] buf = new char[read];
                    StringBuffer sb = new StringBuffer(read);
                    while( -1 != (read = rd.read(buf, 0, read)) ){
                       sb.append(buf, 0, read);
                    }
                    rd.close();
                    String stFormat = sb.toString();                                        


                    String result = execJS("_getArea("
                        + rcArea.x + "," + rcArea.y + "," + rcArea.width + "," + rcArea.height + ")"
                    );
                    String args[] = result.split(",");
                    for(int i=0; i<4; ++i){
                        double d = Double.parseDouble(args[i*2]);
                        String toRepl = String.format("$(4lt%d)", i+1);
                        stFormat = stFormat.replace(toRepl, "" + (int)Math.floor(d));

                        toRepl = String.format("$(mlat%d)", i+1);
                        String that = String.format("%2.6f", ((d - Math.floor(d))*60.0));
                        stFormat = stFormat.replace(toRepl, that );

                        d = Double.parseDouble(args[i*2+1]);
                        toRepl = String.format("$(4ln%d)", i+1);
                        stFormat = stFormat.replace(toRepl, "" + (int)Math.floor(d));

                        toRepl = String.format("$(mlng%d)", i+1);
                        that = String.format("%2.6f", ((d - Math.floor(d))*60.0));
                        stFormat = stFormat.replace(toRepl, that );
                    }

                    stFormat = stFormat.replace("$(8lat1)", args[0]);
                    stFormat = stFormat.replace("$(8lng1)", args[1]);
                    stFormat = stFormat.replace("$(8lat2)", args[2]);
                    stFormat = stFormat.replace("$(8lng2)", args[3]);
                    stFormat = stFormat.replace("$(8lat3)", args[4]);
                    stFormat = stFormat.replace("$(8lng3)", args[5]);
                    stFormat = stFormat.replace("$(8lat4)", args[6]);
                    stFormat = stFormat.replace("$(8lng4)", args[7]);

                    stFormat = stFormat.replace("$(scale)", args[8]);//scale in metters/pixel

                    String w5 = String.format("%5d", rcArea.width);
                    stFormat = stFormat.replace("$(5w)", w5);
                    String h5 = String.format("%5d", rcArea.height);
                    stFormat = stFormat.replace("$(5h)", h5);
                    stFormat = stFormat.replace("$(w)", ""+ rcArea.width);
                    stFormat = stFormat.replace("$(h)", ""+ rcArea.height);

                    stFormat = stFormat.replace("$(BMP)", fn.getName());

                    int iPos = stFN.lastIndexOf('.');
                    String stBMP = stFN.substring(0, iPos);
                    String stOZF2 = stBMP;
                    stBMP += "_ozf.map";
                    stOZF2 += ".ozf2";

                    stFormat = stFormat.replace("$(OZF2)", stOZF2);
                    
                    FileWriter wt = new FileWriter(stBMP);

                    wt.write(stFormat);
                    wt.close();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }    
        }
    }

    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex == 0) {
                Graphics2D g2d = (Graphics2D)g;
                g2d.translate(pf.getImageableX(), pf.getImageableY());
                g2d.drawImage(
                        getImage(
                                rcArea.x,
                                rcArea.y,
                                rcArea.width,
                                rcArea.height),
                        0,0,
                        null);
                return Printable.PAGE_EXISTS;
        }
        return Printable.NO_SUCH_PAGE;
    }
}

class SPBMetro {
    BrMapSprite line1 = new BrMapSprite(new Color(1.0F, 0.0F, 0.0F, 0.5F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };
    BrMapSprite line2 = new BrMapSprite(new Color(0.0F, 0.0F, 1.0F, 0.5F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };
    BrMapSprite line3 = new BrMapSprite(new Color(0.0F, 1.0F, 0.0F, 0.5F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };
    BrMapSprite line4 = new BrMapSprite(new Color(1.0F, 0.8F, 0.3F, 0.8F)){
        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            Graphics2D g2 = (Graphics2D)g.create();
            g2.setStroke(new BasicStroke(5));
            super.paint(g2, x, y);
            g2.dispose();
        }
    };

    BrMapSprite duke = new BrMapSprite(){
        ImageIcon img = new ImageIcon(getClass().getResource("images/java.png"));

        @Override
        public void paint(Graphics g, int[] x, int[] y) {
            img.paintIcon(null, g, x[0], y[0]);
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
        duke.LLs.add( new Point2D.Double(59.911884983705214,30.295625925064087));
    }

    public void add(BrMap m){
        m.getSprites().clear();
        m.getSprites().add(line1);
        m.getSprites().add(line2);
        m.getSprites().add(line3);
        m.getSprites().add(line4);
        m.getSprites().add(duke);
        m.execJS( "map.setCenter( new GLatLng(59.94, 30.33), 12);" );
    }
}