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

package org.jdesktop.jdic.tray.internal.impl;


import org.jdesktop.jdic.tray.internal.TrayIconService;
import org.jdesktop.jdic.tray.TrayIcon;
import javax.swing.BorderFactory;

/**
 * The <code>GnomeTrayIconService</code> interface is the contract for a Gnome 
 * TrayIcon implementation.
 *
 */

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;


public class GnomeTrayIconService extends GnomeTrayAppletService 
    implements TrayIconService {

    private JPopupMenu menu;
    private IconPanel iconPanel;
    private Icon icon;        
    private HWToolTip tooltip;
    private boolean autoSize;

    private LinkedList actionList = new LinkedList();

    public GnomeTrayIconService() {
        super();
        iconPanel = new IconPanel();
        frame.add(iconPanel);
        frame.setFocusable(true);
        frame.requestFocus();
        initListeners();
    }

    void mousePressed(MouseEvent e) {
        if (menu != null) {
            if (e.isPopupTrigger() && !menu.isShowing()) {
                tooltip.hide();
                menu.show();
                Dimension d = menu.getSize();  

                if ((d.height == 0) || (d.width == 0)) {
                    // size is zero because it has not been shown yet, show it.
                    menu.show((Component) e.getSource(), e.getX(), e.getY());
                    d = menu.getSize();                  // reposition it.
                    menu.show((Component) e.getSource(), e.getX(),
                            e.getY() - d.height);
                } else {
                    menu.show((Component) e.getSource(), e.getX(),
                            e.getY() - d.height);
                }
            } else if (!menu.isShowing()) {
                menu.hide();
                tooltip.hide();
                ListIterator li = actionList.listIterator(0);
                ActionListener al;

                while (li.hasNext()) {
                    al = (ActionListener) li.next();
                    al.actionPerformed(new ActionEvent(e.getSource(),
                                ActionEvent.ACTION_PERFORMED, "PressAction", e.getWhen(),
                                0));
                }

            }
        }
    }

    void mouseEntered(MouseEvent e) {
        if ((tooltip != null) && ((menu == null) || ((menu != null) && !menu.isShowing()))) {
            Dimension d = tooltip.getSize();
            Point p = frame.getLocationOnScreen();
            Dimension size = tooltip.getPreferredSize();

            tooltip.show(p.x, p.y - size.height - 5);
        }
    }

    void mouseExited(MouseEvent e) {
        if (tooltip != null) {
            tooltip.setVisible(false);
        }
    }

    void initListeners() {
        MouseAdapter ma = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                GnomeTrayIconService.this.mousePressed(e);
            }

            public void mouseEntered(MouseEvent e) {
                GnomeTrayIconService.this.mouseEntered(e);
            }

            public void mouseExited(MouseEvent e) {
                GnomeTrayIconService.this.mouseExited(e);
            }
        };

        iconPanel.addMouseListener(ma);
        frame.addMouseListener(ma);    // for some reason 1.4.2 needs
        // to have mouse listener installed on frame instead of iconPanel  

    }

    public void addNotify() {}

    public void setPopupMenu(JPopupMenu m) {
        menu = m;
        if (m != null) {
            m.setLightWeightPopupEnabled(false);
        }
    }

    public void setIcon(Icon i) {
        icon = i;
        if (icon != null) {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            reshape(0,0,w,h);
            frame.setVisible(false);
            frame.remove(iconPanel);
            iconPanel = new IconPanel();
            frame.add(iconPanel);
            frame.setVisible(true);
        }
        iconPanel.repaint();
    }

    public void setCaption(String s) {
        // System.out.println("setCaption s =" + s);
        if (tooltip == null) {
            tooltip = new HWToolTip(s, frame);
        } else {
            tooltip.setCaption(s);
        }
    }

    public void setIconAutoSize(boolean b) {
        autoSize = b;
        if (autoSize && (icon != null)) {
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            reshape(0,0,w,h);
            frame.setVisible(false);
            frame.remove(iconPanel);
            iconPanel = new IconPanel();
            frame.add(iconPanel);
            frame.setVisible(true);
        }
    }

    public void addActionListener(ActionListener l) {
        actionList.add(l);
    }

    public void removeActionListener(ActionListener l) {
        actionList.remove(l);
    }

    public Point getLocationOnScreen() {
        Point p = null;

        if (iconPanel != null) {
            p = iconPanel.getLocationOnScreen();
        }
        return p;
    }

    /*
     * This iconPanel paints the icon 
     *
     */

    class IconPanel extends JComponent {
        Image img;
        IconPanel() {}

        public void paintComponent(Graphics g) {
            Dimension d = getAppletSize();
            g.clearRect(0, 0, d.width, d.height);

            if (icon != null) {
                int w = icon.getIconWidth();
                int h = icon.getIconHeight();
                if (!autoSize) {
                    icon.paintIcon(this, g, 0, 0);
                } else {
                    /* Scale to the right size */
                    if (img == null) {
                        img = createImage(w, h);
                    }
                    icon.paintIcon(this, img.getGraphics(), 0, 0);
                    g.drawImage(img, 0, 0, d.width, d.height, 0,0,w,h, this);
                }
            }
          super.paintComponent(g);
        }

        boolean doesIconReferenceImage(Icon icon, Image image) {
            Image iconImage = (icon != null && (icon instanceof ImageIcon)) ?
                ((ImageIcon)icon).getImage() : null;
            return (iconImage == image);
        }

        /**
         * This is overridden to return false if the current Icon's Image is
         * not equal to the passed in Image <code>img</code>.
         *
         * @see     java.awt.image.ImageObserver
         * @see     java.awt.Component#imageUpdate(java.awt.Image, int, int, int, int, int)
         */
        public boolean imageUpdate(Image img, int infoflags,
                int x, int y, int w, int h) {
            if (!isShowing() ||
                    !doesIconReferenceImage(icon, img)) {
                return false;
            }
            return super.imageUpdate(img, infoflags, x, y, w, h);
        }
    }


    class HWToolTip extends JWindow { 

        Font font = new Font("Serif", 10, Font.PLAIN);

        JToolTip label; 
        public HWToolTip(String caption, Window parent) {
            super(parent);
            setFocusableWindowState(false);
            setName("###overrideRedirect###");
            label = new JToolTip();
            label.setTipText(caption);
            getContentPane().add(label);
        }

        public void setCaption(String caption) {
            label.setTipText(caption); 
        }

        public void show(int x, int y) {
            setLocation(x, y);
            Dimension d = label.getPreferredSize();

            // pack does not seem to work ! 
            setSize(d.width, d.height);
            setVisible(true);
        }
    }

}
