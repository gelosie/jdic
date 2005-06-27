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

/**
 * The <code>GnomeTrayIconService</code> interface is the contract for a Gnome 
 * TrayIcon implementation.
 *
 */

import java.awt.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.awt.event.*;
import java.util.*;


public class GnomeTrayIconService extends GnomeTrayAppletService 
    implements TrayIconService {

    private JPopupMenu menu;
    private JDialog popupMenuParent;
    private IconPanel iconPanel;
    private Icon icon;        
    private HWToolTip tooltip;
    private BalloonMessageWindow bmw;
    private boolean autoSize;

    private LinkedList actionList = new LinkedList();
    private LinkedList balloonListeners = new LinkedList();

    public GnomeTrayIconService() {
        super();
        initFrame();
    }
    void initFrame(){
        iconPanel = new IconPanel();
        frame.add(iconPanel);
        frame.setFocusable(true);
        frame.requestFocus();
        bmw = new BalloonMessageWindow(frame);
        
        initListeners();
        
        popupMenuParent = new JDialog(frame, "JDIC Tray Icon");
        popupMenuParent.setUndecorated(true);
    }

    void mousePressed(final MouseEvent e) {
        if(tooltip != null)
        	tooltip.setVisible(false);

        if (e.isPopupTrigger()) {
        	if (menu != null) {
                Dimension d = menu.getPreferredSize();
                Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
                final Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, (Component) e.getSource());
                p.x = p.x + d.width > s.width ? p.x - d.width : p.x;
                p.y = p.y + d.height > s.height ? p.y - d.height : p.y;
                SwingUtilities.convertPointFromScreen(p, popupMenuParent);
                popupMenuParent.setVisible(true);
                SwingUtilities.invokeLater(new Runnable(){
                    public void run(){
                        menu.show(popupMenuParent.getContentPane(), p.x, p.y);
                        popupMenuParent.toFront();
                    }
                });
            } 
        }else {
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                  Thread actionThread = new Thread(){
                    public void run() {
                        ListIterator li = actionList.listIterator(0);
                        while (li.hasNext()) {
                        ActionListener al;
                        al = (ActionListener) li.next();
                        al.actionPerformed(new ActionEvent(GnomeTrayIconService.this,
                                    ActionEvent.ACTION_PERFORMED, "PressAction", e.getWhen(),
                                    e.getModifiers()));
                        }
                    }                  
                  };
                  actionThread.start();
                }
            });
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

    public void addNotify() {
    	if(GnomeTrayAppletService.winMap.get(new Long(this.getWindow())) == null){
    		super.init();
    		initFrame();
            
    		if(this.icon != null)
    			this.setIcon(this.icon);
    		if(this.menu != null)
    			this.setPopupMenu(this.menu);
    		
    		if(this.tooltip != null)
    			tooltip = new HWToolTip(this.tooltip.label.getTipText(), frame);
    		
    	}
        GnomeSystemTrayService.dockWindow(this.getWindow());
        frame.setVisible(true);
    }

    public void setPopupMenu(JPopupMenu m) {
        menu = m;
        if (m != null) {
            m.setLightWeightPopupEnabled(false);
            m.addPopupMenuListener(new PopupMenuListener(){

				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    popupMenuParent.setVisible(false);
                }

				public void popupMenuCanceled(PopupMenuEvent e) {
				}
                
            });
            // in jdk1.4, the popup menu is still visible after the invoker window lost focus.
            popupMenuParent.addWindowFocusListener(new WindowFocusListener() {
                public void windowGainedFocus(WindowEvent e) {
                }

                public void windowLostFocus(WindowEvent e) {
                    menu.setVisible(false);
                }
            });

        }
    }

    public void setIcon(final Icon i) {
        SwingUtilities.invokeLater(new Runnable(){
           public void run(){
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
        });
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
    
    public void showBalloonMessage(String caption, String text, int type){
        if( caption == null && text == null)
            throw new NullPointerException();
        bmw.showBalloonMessage(caption, text, type);
    }
    
    class BalloonMessageWindow extends JWindow{
        Dimension sd;
        Dimension pd;
        Dimension d;
        Point pp;
        Point p;
        boolean downToup = true;
        
        JLabel captionLabel = new JLabel();
        JLabel textLabel = new JLabel();
        private Thread showThread;
        private Thread hideThread;
        private int timeout = 10000;
        private int delay = 15;
        private int pixel = 2;

        private ActionListener hideAction;
        private javax.swing.Timer hideTimer;

        public BalloonMessageWindow(Window parent){
            super(parent);
            JPanel outerPanel = new JPanel();
            JPanel innerPanel = new JPanel();
            this.getContentPane().add(outerPanel);
            outerPanel.setLayout(new BorderLayout());
            outerPanel.add(innerPanel);
            outerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,  
                    new Color(0x00CBDAF3), new Color(0x002F4A78)));
            innerPanel.setLayout(new BorderLayout());
            innerPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED, 
                    new Color(0x00CBDAF3), new Color(0x002F4A78)));
            
            captionLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            textLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            innerPanel.setBackground(new Color(0x00FFFFE1));
            innerPanel.setOpaque(true);
            
            textLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 3, 3));

            ImageIcon x = new ImageIcon(
                    GnomeTrayIconService.class.getResource("images/x.png"));
            ImageIcon xx = new ImageIcon(
                    GnomeTrayIconService.class.getResource("images/xx.png"));
            ImageIcon xxx = new ImageIcon(
                    GnomeTrayIconService.class.getResource("images/xxx.png"));
            JButton closeButton = new JButton(x);
            closeButton.setPreferredSize(new Dimension(x.getIconWidth(), x.getIconHeight()));
            closeButton.setRolloverEnabled(true);
            closeButton.setRolloverIcon(xx);
            closeButton.setPressedIcon(xxx);
            closeButton.setBorder(null);
            closeButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    BalloonMessageWindow.this.hideCurrentMessageWindowImmediately();
                }
            });
            closeButton.setToolTipText("Close");
            JPanel closePanel = new JPanel();
            closePanel.setOpaque(false);
            closePanel.setLayout(new BorderLayout());
            closePanel.add(closeButton, BorderLayout.NORTH);
            
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.add(closePanel, BorderLayout.EAST);
            topPanel.add(captionLabel, BorderLayout.CENTER);
            topPanel.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 3));
            innerPanel.add(topPanel, BorderLayout.NORTH);
            innerPanel.add(textLabel, BorderLayout.CENTER);
            
            outerPanel.addMouseListener(new MouseAdapter(){
                public void mouseClicked(final MouseEvent e){
                    hideCurrentMessageWindowImmediately();
                    ListIterator li = balloonListeners.listIterator(0);
                    while (li.hasNext()) {
                    ActionListener al;
                    al = (ActionListener) li.next();
                    al.actionPerformed(new ActionEvent(GnomeTrayIconService.this,
                                ActionEvent.ACTION_PERFORMED, "PressAction", e.getWhen(),
                                e.getModifiers()));
                    }
                }
            });
            hideAction = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
                    doHide();    
				}
            };
            hideTimer = new javax.swing.Timer(timeout, hideAction);
            hideTimer.setRepeats(false);
        }
        private void hideCurrentMessageWindowImmediately(){
            if(hideThread != null){
                hideThread.interrupt(); // already in hiding.
            } else if(showThread != null){
                hideTimer.stop();
                showThread.interrupt();
                this.setVisible(false);

                synchronized(GnomeTrayIconService.class){
                    GnomeTrayIconService.class.notify();
                    showThread = null;
                }
            }
        }
        
        public void showBalloonMessage(final String caption, final String text, final int type){
            if(showThread != null){
                Thread waitThread = new Thread(){
                    public void run(){
                        synchronized(GnomeTrayIconService.class){
                            try{
                                if(showThread != null)
                                    GnomeTrayIconService.class.wait();
                            }catch(InterruptedException ie){
                                ie.printStackTrace();
                            }
                        }
                        doShowMessageWindow(caption, text, type);
                    }
                };
                waitThread.start();
            }else {
                this.doShowMessageWindow(caption, text, type);
            }
        }
        
        private void doShowMessageWindow(final String caption, final String text, final int type){
            showThread = new Thread(){
                public void run(){
                    doShow(caption, text, type);
                }
            };
            showThread.start();
        }
     
        private synchronized void doShow(String caption, String text, int type){
            switch(type) {
                case 0:
                    captionLabel.setIcon(new ImageIcon(
                            GnomeTrayIconService.class.getResource("images/info.png")));
                    break;
                case 1:
                    captionLabel.setIcon(new ImageIcon(
                            GnomeTrayIconService.class.getResource("images/error.png")));
                    break;
                case 2:
                    captionLabel.setIcon(new ImageIcon(
                            GnomeTrayIconService.class.getResource("images/warning.png")));
                    break;
                default :
                    captionLabel.setIcon(null);
                    break;
            }
            captionLabel.setText(caption != null ? caption : "");
            if(text != null){
                text = "<html>"+text.replaceAll("\\n", "<br>")+"</html>";
                textLabel.setText(text);
            }else{
                textLabel.setText("");   
            }
            
            this.pack();

            pp = frame.getLocation();
            SwingUtilities.convertPointToScreen(pp, frame);
            p = new Point();

            sd = Toolkit.getDefaultToolkit().getScreenSize();
            pd = frame.getSize();
            d = this.getSize();

            // Adjust for the frame's size
            pp.x -= 2;
            pp.y -= 4;
            pd.width += 2;
            pd.height += 6;

            p.x = (pp.x + d.width) < sd.width ? pp.x : sd.width-d.width;
            downToup = (pp.y - d.height) > 0;
            p.y = (pp.y - d.height) > 0 ? pp.y - d.height : pp.y + pd.height;
            try{
                if(downToup)
                    this.setBounds(p.x, p.y+d.height, d.width, 1);
                else
                    this.setBounds(p.x, p.y, d.width, 1);

                this.setVisible(true);
                for(int i=1; i < d.height; i+=pixel){
                    Thread.sleep(delay);
                    if(downToup)
                        this.setBounds(p.x, p.y+d.height-i, d.width, i);
                    else
                        this.setBounds(p.x, p.y, d.width, i);
                    
                    this.validate();
                }
                if(p.y != this.getLocation().y || d.height != this.getSize().getHeight()){
                    this.setBounds(p.x, p.y, d.width, d.height);
                    this.validate();
                }
                hideTimer.start();
            }catch(InterruptedException ie){}
        }

        private void doHide(){
            hideThread = new Thread(){
             public void run(){
                p = getLocation();
                d = getSize();
                try{
                    for(int i=d.height; i>=1; i-=pixel){
                        Thread.sleep(delay);
                        if(downToup){
                            setBounds(p.x, p.y+d.height-i, d.width, i);
                        }else{
                            setSize(d.width, i);
                        }
                        validate();
                    }
                }catch(Exception e){}
                setVisible(false);
                synchronized(GnomeTrayIconService.class){
                    showThread = null;
                    GnomeTrayIconService.class.notify();
                }
                hideThread = null;
             }
            };
            hideThread.start();
        }
    }

    public void addBalloonActionListener(ActionListener al) {
        balloonListeners.add(al);
    }
    public void removeBalloonActionListener(ActionListener al) {
        balloonListeners.remove(al);
    }
}
