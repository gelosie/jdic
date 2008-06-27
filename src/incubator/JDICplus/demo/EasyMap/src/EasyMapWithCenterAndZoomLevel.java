import java.awt.Dimension;
import javax.swing.SwingUtilities;
import org.jdic.web.BrMap;
public class EasyMapWithCenterAndZoomLevel extends javax.swing.JFrame {
    EasyMapWithCenterAndZoomLevel()
    {
        BrMap.DESIGN_MODE = false;
        BrMap map;
        map = new BrMap(
            BrMap.MAP_GOOGLE, //Google maps provider, BrMap.MAP_MS_LIVE or BrMap.MAP_YAHOO
            50.08, 14.44, //Prague
            12, // from BrMap.ZOOM_MIN(1) to BrMap.ZOOM_MAX(19);
            BrMap.VIEW_ROAD | BrMap.VIEW_SATELLITE //hybrid view, BrMap.VIEW_ROAD or BrMap.VIEW_SATELLITE
        );
        add(map);
        setSize(new Dimension(480,600));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {public void run() {
                new EasyMapWithCenterAndZoomLevel().setVisible(true);
        }});
    }
}

