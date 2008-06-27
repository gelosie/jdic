import java.awt.Dimension;
import javax.swing.SwingUtilities;
import org.jdic.web.BrComponent;
public class EasyBrowser extends javax.swing.JFrame {
    EasyBrowser()
    {
        BrComponent.DESIGN_MODE = false;
        add(new BrComponent("http://www.google.com/intl/en_BE/mobile/mail/#utm_source=en_BE-cpp-g4mc-gmhp&utm_medium=cpp&utm_campaign=en_BE"));
        setSize(new Dimension(320,200));
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {public void run() {
                new EasyBrowser().setVisible(true);
        }});
    }
}


