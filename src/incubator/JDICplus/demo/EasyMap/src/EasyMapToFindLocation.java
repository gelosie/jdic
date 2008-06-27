import java.awt.Dimension;
import java.awt.geom.Point2D;
import javax.swing.SwingUtilities;
import org.jdic.web.BrMap;
import org.jdic.web.BrMapMousePos;
public class EasyMapToFindLocation extends javax.swing.JFrame {
    EasyMapToFindLocation()
    {
        BrMap.DESIGN_MODE = false;
        final BrMap map = new BrMap();
        map.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String stPN = evt.getPropertyName();
                if( stPN.equals("progressBar") ){
                    //you can show the progress information here
                    String stNV = (String)evt.getNewValue();
                    if(null==stNV)
                        stNV = "";
                    String st[] = stNV.split(",");
                    int iMax = Integer.parseInt(st[0]),
                        iPos = Integer.parseInt(st[1]);
                    if(0==iMax){
                        System.out.println("download complite");
                    } else {
                        System.out.printf("...download progress %3.3f%%\n", ((iPos*100.0/iMax)) );
                    }
                } else if( stPN.equals("mouseGeoPos") ){
                    //you can show the geo coordinates of mouse position here
                    Point2D gp = ((BrMapMousePos)evt.getNewValue()).getMouseGeoPos();
                    System.out.printf("mouse location (Latitude:%3.3f, Longitude:%3.3f)\n",
                           gp.getX(), gp.getY() );
                } else if( stPN.equals("mapReady") ){
                    //map API is ready here! We are trying to find "London, UK" location 
                    //    ':' at start means that we aren't interested in return value,
                    //    it's essentially accelerate the call. 
                    //    Attention: only double quotes are available in Javascript statements.
                    map.execJS( ":_findAddress(\"London, UK\")" );
                }
            }
        });
        add(map);
        setSize(new Dimension(480,600));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {public void run() {
                new EasyMapToFindLocation().setVisible(true);
        }});
    }
}

