import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.swing.SwingUtilities;
import org.jdic.web.BrMap;
import org.jdic.web.BrMapBalloonSprite;
import org.jdic.web.BrMapSprite;
public class EasyMapplets extends javax.swing.JFrame {
    String stAddress;
    BrMap map;
    
    EasyMapplets() {
        BrMap.DESIGN_MODE = false;
        map = new BrMap() {
            @Override
            public void onFoundAddress(final String[] args) {
                //overwritten default action - 
                //  positioning of found location at the center of map
                //super.onFoundAddress(args);
                SwingUtilities.invokeLater(new Runnable(){ public void run() {        
                    setZoomLevel(2);
                    String stCenter = args[2] + "," + args[3];
                    setViewCenter(stCenter);
                    new BrMapBalloonSprite(
                        "<html><b>Location:</b><br><b style=\"color:green\">" + stAddress + "</b></html>",
                        getPoint(stCenter),
                        BrMapBalloonSprite.Alignment.LEFT_ALIGNED_ABOVE,
                        new Dimension(100, 70),
                        16, 16,
                        16, 16, 
                        new Color(0F, 0F, 0F),
                        new Color(1F, 1F, 1F, 0.8F)
                    ).add(map);
                }});
            }
        };
        map.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String stPN = evt.getPropertyName();
                if( stPN.equals("mapReady") ){
                    stAddress = "London, UK";
                    //map API is ready here! We are trying to find "London, UK" location 
                    //    ':' at start means that we aren't interested in return value,
                    //    it's essentially accelerate the call. 
                    //    Attention: only double quotes are available in Javascript statements.
                    map.execJS( ":_findAddress(\"" + stAddress + "\")" );
                }
            }
        });
        map.getSprites().add(new MapZeroMeridian(new Color(1f, 0f, 0f)));
        map.getSprites().add(new MapEquator(new Color(0f, 0f, 1f)));
        add(map);
        setSize(new Dimension(480,600));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {public void run() {
                new EasyMapplets().setVisible(true);
        }});
    }
}

class MapZeroMeridian extends BrMapSprite {
    int iStepCount = 100;
    MapZeroMeridian(Color spriteColor){
        super("ZeroMeridian", spriteColor);
        isPoligon = false;
        //block poles
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
}

class MapEquator extends BrMapSprite {
    int iStepCount = 100;
    MapEquator(Color spriteColor){
        super("Equator", spriteColor);
        isPoligon = false;
        for(int i=0; i<iStepCount; ++i){
            LLs.add( new Point2D.Double(0.0, i*360.0/iStepCount - 180.0));
        }    
    }
    @Override
    public void paint(Graphics g, int[] x, int[] y) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setStroke(new BasicStroke(5));
        super.paint(g2, x, y);
        g2.dispose();
    }
}
