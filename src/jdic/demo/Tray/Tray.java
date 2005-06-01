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

import org.jdesktop.jdic.tray.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class Tray implements ActionListener, ItemListener {

    SystemTray tray = SystemTray.getDefaultSystemTray();
    TrayIcon ti;
    JFrame frame;
    public Tray() {

        JPopupMenu menu;
        JMenu  submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if( Integer.parseInt(System.getProperty("java.version").substring(2,3)) >=5 )
            System.setProperty("javax.swing.adjustPopupLocationToFit", "false");
        menu = new JPopupMenu("A Menu");
        
        // a group of JMenuItems
        menuItem = new JMenuItem("A text-only menu item", KeyEvent.VK_T);
        // menuItem.setMnemonic(KeyEvent.VK_T); //used constructor instead
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
                ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // ImageIcon icon = new ImageIcon("middle.gif");
        ImageIcon icon = new ImageIcon(Tray.class.getResource("images/middle.gif"));

        menuItem = new JMenuItem("Both text and icon", icon);
        menuItem.setMnemonic(KeyEvent.VK_B);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem(icon);
        menuItem.setMnemonic(KeyEvent.VK_D);
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // a group of radio button menu items
        menu.addSeparator();
        ButtonGroup group = new ButtonGroup();

        rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Another one");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        rbMenuItem.addActionListener(this);
        menu.add(rbMenuItem);

        // a group of check box menu items
        menu.addSeparator();
        cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
        cbMenuItem.setMnemonic(KeyEvent.VK_C);
        cbMenuItem.addItemListener(this);
        menu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Another one");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        cbMenuItem.addItemListener(this);
        menu.add(cbMenuItem);

        // a submenu
        menu.addSeparator();
        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2,
                ActionEvent.CTRL_MASK));
        menuItem.addActionListener(this);
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        menuItem.addActionListener(this);
        submenu.add(menuItem);
        menu.add(submenu);

        // "Quit" menu item
        menu.addSeparator();
        menuItem = new JMenuItem("Quit");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        
        // ImageIcon i = new ImageIcon("duke.gif");
        ImageIcon i = new ImageIcon(Tray.class.getResource("images/duke.gif"));

        ti = new TrayIcon(i, "JDIC Tray Icon API Demo - TrayIcon", menu);

        ti.setIconAutoSize(true);
        ti.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	frame.setVisible(!frame.isVisible());
            }
        });
        ti.addBalloonActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
              JOptionPane.showMessageDialog(null, 
              "Balloon Message been clicked - TrayIcon", "Message",
              JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        tray.addTrayIcon(ti);
        
        // Construct the GUI for balloon message.
        frame = new JFrame("Show Balloon Message");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEtchedBorder());
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JLabel("Caption: "), BorderLayout.WEST);
        final JTextField captionField = new JTextField("JDIC TrayIcon"); 
        topPanel.add(captionField, BorderLayout.CENTER);
        JPanel typePanel = new JPanel();
        final JComboBox typeBox = new JComboBox(new String[]{"INFO", "ERROR", "WARNING", "NONE" });
        typePanel.add(new JLabel(" Type:"), BorderLayout.WEST);
        typePanel.add(typeBox, BorderLayout.EAST);
        topPanel.add(typePanel, BorderLayout.EAST);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.add(new JLabel("Message:"), BorderLayout.NORTH);
        final JTextArea messageArea = new JTextArea(5, 20); 
        messageArea.setText("This is a balloon message.\nYou can set the caption, message, \nand message type");
        messageArea.setBorder(BorderFactory.createEtchedBorder());
        messagePanel.add(messageArea);
        frame.getContentPane().add(messagePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        final JButton okButton = new JButton("OK");
        final JButton cancelButton = new JButton("Cancel");
        ActionListener al = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == cancelButton)
					frame.setVisible(false);
				else if(e.getSource() == okButton){
					ti.displayMessage(captionField.getText(), messageArea.getText(), typeBox.getSelectedIndex());
				}
			}
        };
        okButton.addActionListener(al);
        cancelButton.addActionListener(al);
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Returns just the class name -- no package info.
    protected String getClassName(Object o) {
        String classString = o.getClass().getName();
        int dotIndex = classString.lastIndexOf(".");

        return classString.substring(dotIndex + 1);
    }

    public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String s = source.getText();
        if (s.equalsIgnoreCase("Quit")) {
            System.out.println("Quit menu item selected!");
            System.exit(0);
        } else {
            s = "Action event detected." + "\n" + "    Event source: "
                + source + " (an instance of " + getClassName(source) + ")";

            System.out.println(s);
        }
    }

    public void itemStateChanged(ItemEvent e) {
        JMenuItem source = (JMenuItem) (e.getSource());
        String s = "Item event detected." + "\n" + "    Event source: "
                + source.getText() + " (an instance of " + getClassName(source)
                + ")" + "\n" + "    New state: "
                + ((e.getStateChange() == ItemEvent.SELECTED)
                        ? "selected"
                        : "unselected");

        System.out.println(s);
    }

    public static void main(String[] args) {
        new Tray();
    }

}
