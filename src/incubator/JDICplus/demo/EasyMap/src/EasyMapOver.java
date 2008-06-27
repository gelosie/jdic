import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;
import org.jdic.web.BrMap;
import org.jdic.web.BrMapOver;

public class EasyMapOver extends javax.swing.JFrame {
    EasyMapOver(){
        BrMap.DESIGN_MODE = false;
        BrMap masterMap = new BrMap(
            BrMap.MAP_YAHOO, //Yahoo maps provider, BrMap.MAP_MS_LIVE or BrMap.MAP_YAHOO
            37.79, -122.48, //San Francisco
            12, // from BrMap.ZOOM_MIN(1) to BrMap.ZOOM_MAX(19);
            BrMap.VIEW_ROAD | BrMap.VIEW_SATELLITE //hybrid view, BrMap.VIEW_ROAD or BrMap.VIEW_SATELLITE
        );        
        
        BrMapOver overMap = new BrMapOver();
        overMap.setMapProvider(BrMap.MAP_MS_LIVE);        
        overMap.setSyncMap(masterMap);
        overMap.setBounds(new Rectangle(80, 30, 320,200));
        overMap.bnZoomMinus.setVisible(false);
        overMap.bnZoomPlus.setVisible(false);
        overMap.sbZoomLevel.setVisible(false);
        
        masterMap.getCentralPanel().add(overMap);
        add(masterMap);
        setSize(new Dimension(600,480));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {public void run() {
                new EasyMapOver().setVisible(true);
        }});
    }
}


