/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.jdic.misc.Wallpaper;
import org.jdesktop.jdic.misc.WallpaperFactory;


/**
 * This will demonstrate the JDIC API of setting of a desktop background
 * wallpaper.
 *
 * @author George Zhang
 * @author Carl Dea
 *
 */
public class WallpaperDemo
extends JPanel
{
    JPanel jWallpaperPanel = new JPanel();
    JPanel fileChooserPanel = new JPanel();
    JFileChooser jFileChooser = new JFileChooser();
    JPanel jStylePanel = new JPanel();
    JLabel jStyleLabel = new JLabel("Style:");
    WallpaperMode[] wallpaperStyles = { new WallpaperMode("Stretch", "The background image will be stretched.", Wallpaper.STRETCH), 
                                 new WallpaperMode("Center", "The background image will be centered.", Wallpaper.CENTER), 
                                 new WallpaperMode("Tile", "The background image will be tiled.", Wallpaper.TILE)
                                 };
    JComboBox jStyleComboBox = new JComboBox(wallpaperStyles);
    JPanel jBottomPanel = new JPanel();
    JPanel jButtonPanel = new JPanel();
    JButton jSetWallpaperButton = new JButton();
    JButton jCancelButton = new JButton();
    File selectedFile;

    public WallpaperDemo()
    {
        try
        {
            jbInit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
        }

        JFrame frame = new JFrame("JDIC Wallpaper API Demo - Set desktop background wallpaper");

        Container contentPane = frame.getContentPane();

        contentPane.setLayout(new GridLayout(1, 1));
        contentPane.add(new WallpaperDemo());

        frame.addWindowListener(new WindowAdapter()
            {
                public void windowClosing(WindowEvent e)
                {
                    System.exit(0);
                }
            });

        frame.pack();
        frame.setVisible(true);
    }


    private void jbInit()
    throws Exception
    {
        jFileChooser.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 0));
        jFileChooser.setControlButtonsAreShown(false);
        jFileChooser.addChoosableFileFilter(new WallpaperFilter());
        jFileChooser.addPropertyChangeListener(new WallpaperDemo_jFileChooser_propertyChangeAdapter(this));

        jStylePanel.setLayout(new GridLayout(14, 1));

        // Here, it aligns the top of jStylePanel with the top of the file 
        // list box in the jFileChooser.
        jStylePanel.setBorder(BorderFactory.createEmptyBorder(44, 10, 0, 10));
        jStylePanel.add(jStyleLabel, null);
        jStylePanel.add(jStyleComboBox, null);
        jStyleComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String style = jStyleComboBox.getSelectedItem().toString();
                jSetWallpaperButton.setActionCommand(style);
                if (style.equalsIgnoreCase("stretch"))
                {
                   jSetWallpaperButton.setToolTipText("Click to set desktop wallpaper. " + wallpaperStyles[0].getDescription());
                }
                else if (style.equalsIgnoreCase("center"))
                {
                   jSetWallpaperButton.setToolTipText("Click to set desktop wallpaper. " + wallpaperStyles[1].getDescription());
                }
                else if (style.equalsIgnoreCase("tile"))
                {
                   jSetWallpaperButton.setToolTipText("Click to set desktop wallpaper. " + wallpaperStyles[2].getDescription());
                }
            }
        });

        fileChooserPanel.setLayout(new BorderLayout());
        fileChooserPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        fileChooserPanel.add(jFileChooser, BorderLayout.CENTER);

        jWallpaperPanel.setLayout(new BorderLayout());
        jWallpaperPanel.add(fileChooserPanel, BorderLayout.CENTER);
        jWallpaperPanel.add(jStylePanel, BorderLayout.EAST);

        jSetWallpaperButton.setEnabled(false);
        jSetWallpaperButton.setText("Set Wallpaper");
        jSetWallpaperButton.setActionCommand(wallpaperStyles[0].toString());
        jSetWallpaperButton.setToolTipText("Click to set desktop wallpaper. " + wallpaperStyles[0].getDescription());
        jSetWallpaperButton.addActionListener(new WallpaperDemo_jSetWallpaperButton_actionAdapter(this));

        jCancelButton.setText("Cancel");
        jCancelButton.addActionListener(new WallpaperDemo_jCancelButton_actionAdapter(this));

        jButtonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        jButtonPanel.setLayout(new GridLayout(1, 2));
        jButtonPanel.add(Box.createHorizontalGlue());
        jButtonPanel.add(jSetWallpaperButton);
        jButtonPanel.add(jCancelButton);

        jBottomPanel.setLayout(new BorderLayout());
        jBottomPanel.add(jButtonPanel, BorderLayout.EAST);

        // Center the application.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        this.setPreferredSize(new Dimension((screenSize.width * 7) / 10, (screenSize.height * 6) / 10));
        this.setLayout(new BorderLayout());
        this.add(jWallpaperPanel, BorderLayout.NORTH);
        this.add(new JSeparator(), BorderLayout.CENTER);
        this.add(jBottomPanel, BorderLayout.SOUTH);
    }


    void jSetWallpaperButton_actionPerformed(ActionEvent e)
    {
        // below code needs to be updated to handle the selected style.
        Wallpaper wallpaper = WallpaperFactory.createWallpaper();
        int mode = 0;
        if (e.getActionCommand().equalsIgnoreCase("stretch"))
        {
           mode = wallpaperStyles[0].getModeId();
           jSetWallpaperButton.setToolTipText("Click to set desktop wallpaper. " + wallpaperStyles[0].getDescription());
        }
        else if (e.getActionCommand().equalsIgnoreCase("center"))
        {
           mode = wallpaperStyles[1].getModeId();
           jSetWallpaperButton.setToolTipText("Click to set desktop wallpaper. " + wallpaperStyles[1].getDescription());
        }
        else if (e.getActionCommand().equalsIgnoreCase("tile"))
        {
           mode = wallpaperStyles[2].getModeId();
           jSetWallpaperButton.setToolTipText("Click to set desktop wallpaper. " + wallpaperStyles[2].getDescription());
        }
        
        wallpaper.setBackground(selectedFile.getAbsolutePath(), mode);
    }


    void jCancelButton_actionPerformed(ActionEvent e)
    {
        System.exit(0);
    }


    void jFileChooser_propertyChange(PropertyChangeEvent e)
    {
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(e.getPropertyName()))
        {
            jSetWallpaperButton.setEnabled(false);
        }
        else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(e.getPropertyName()))
        {
            selectedFile = jFileChooser.getSelectedFile();

            if (selectedFile != null)
            {
                jSetWallpaperButton.setEnabled(true);
            }
            else
            {
                jSetWallpaperButton.setEnabled(false);
            }
        }
    }
}


class WallpaperDemo_jSetWallpaperButton_actionAdapter
implements java.awt.event.ActionListener
{
    WallpaperDemo adaptee;

    WallpaperDemo_jSetWallpaperButton_actionAdapter(WallpaperDemo adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.jSetWallpaperButton_actionPerformed(e);
    }
}


class WallpaperDemo_jCancelButton_actionAdapter
implements java.awt.event.ActionListener
{
    WallpaperDemo adaptee;

    WallpaperDemo_jCancelButton_actionAdapter(WallpaperDemo adaptee)
    {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e)
    {
        adaptee.jCancelButton_actionPerformed(e);
    }
}


class WallpaperDemo_jFileChooser_propertyChangeAdapter
implements java.beans.PropertyChangeListener
{
    WallpaperDemo adaptee;

    WallpaperDemo_jFileChooser_propertyChangeAdapter(WallpaperDemo adaptee)
    {
        this.adaptee = adaptee;
    }

    public void propertyChange(PropertyChangeEvent e)
    {
        adaptee.jFileChooser_propertyChange(e);
    }
}


/**
 * Filter the valid background wallpaper files.
 */
class WallpaperFilter
extends FileFilter
{
    // Accept all directories and all bmp, gif, jpg, jpeg, dib, png files, 
    // which should be valid for a wallpaper file.
    public boolean accept(File f)
    {
        if (f.isDirectory())
        {
            return true;
        }

        String lowerFileName = f.getName().toLowerCase();

        // Check the file extension.
        if (lowerFileName.endsWith("bmp") || lowerFileName.endsWith("gif") || lowerFileName.endsWith("jpg") || lowerFileName.endsWith("jpeg") ||
            lowerFileName.endsWith("dib") || lowerFileName.endsWith("png"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }


    // The description of this filter.
    public String getDescription()
    {
        return "Background Files (*.bmp;*.gif;*.jpg;*.jpeg;*.dib;*.png)";
    }
}

class WallpaperMode 
{

    String _displayName;
    String _description;
    int _modeId;

    public WallpaperMode ()
    {
     
    }
    public WallpaperMode (String name, String desc, int mode)
    {
        setDisplayName(name);
        setDescription(desc);
        setModeId(mode);
    }
    public String getDescription() 
    {
        return _description;
    }
    public void setDescription(String description) 
    {
        _description = description;
    }
    
    public String getDisplayName() 
    {
        return _displayName;
    }
    public void setDisplayName(String displayName) 
    {
        _displayName = displayName;
    }
    public int getModeId() 
    {
        return _modeId;
    }
    public void setModeId(int modeId) 
    {
        _modeId = modeId;
    }
    
    public String toString() 
    {
        return getDisplayName();
    }
}
