import java.awt.Dimension;
import javax.swing.SwingUtilities;
import org.jdic.web.BrMap;
public class EasyMap extends javax.swing.JFrame {
    EasyMap()
    {
        BrMap.DESIGN_MODE = false;
        add(new BrMap());
        setSize(new Dimension(320,200));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {public void run() {
                new EasyMap().setVisible(true);
        }});
    }
}


