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


/**
 *  The <code>WinTrayIconService</code> interface is the contract for a Windows 
 *  <code>TrayIcon</code> implementation.
 *
 */

import org.jdesktop.jdic.tray.internal.TrayIconService;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.*;
import java.util.*;

import sun.awt.image.IntegerComponentRaster;


public class WinTrayIconService implements TrayIconService, PopupMenuListener {

    private JPopupMenu menu;
    private Icon icon;        
    private boolean autoSize;
    private boolean isShowing;
    private String caption = "Hello";

    private long hicon;

    static private HashMap map = new HashMap();

    private LinkedList actionList = new LinkedList();

    BufferedImage iconImage;

    static int noIcons;

    int iconID;

    Component observer;

    JWindow popupParentFrame;

    boolean created;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println("Removing all Icons");
                System.out.flush();
                removeAllIcons();
            }
        });
    }

    public static void removeAllIcons() {
        for (int i = 0; i < noIcons; i++) {
            removeIcon(i);
        }
    }

    public WinTrayIconService() {
        iconID = noIcons++;
        map.put(new Integer(iconID), this);
    }

    private native long createIconIndirect(int[] rData, byte[] andMask,
            int nScanStride, int width,
            int height, int xHotSpot,
            int yHotSpot);

    private native void createIcon(long hIcon, int id, String string);

    private native void updateIcon(long hIcon, int id, String string);

    private static native void removeIcon(int id);

    public void addNotify() {
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();

        iconImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) iconImage.getGraphics();

        observer = new DummyComponent();
        g.setComposite(AlphaComposite.Src);
        icon.paintIcon(observer, g, 0, 0);
        updateNativeIcon(iconImage);
        createIcon(hicon, iconID, caption);
        created = true;
    }

    public void setPopupMenu(JPopupMenu m) {
        menu = m;
        m.setLightWeightPopupEnabled(false);
    }

    protected long createNativeIcon(BufferedImage bimage, int w, int h,
            int xHotSpot, int yHotSpot) {

        int  pixels[] = ((DataBufferInt) bimage.getRaster().getDataBuffer()).getData();
        Raster  raster = bimage.getRaster();
        byte[] andMask = new byte[w * h / 8];
        int npixels = pixels.length;

        for (int i = 0; i < npixels; i++) {
            int ibyte = i / 8;
            int omask = 1 << (7 - (i % 8));

            if ((pixels[i] & 0xff000000) == 0) {
                // Transparent bit
                andMask[ibyte] |= omask;
            }
        } {
            int     ficW = raster.getWidth();

            if (raster instanceof IntegerComponentRaster) {
                ficW = ((IntegerComponentRaster) raster).getScanlineStride();
            }
            return createIconIndirect(((DataBufferInt) bimage.getRaster().getDataBuffer()).getData(),
                    andMask, ficW, raster.getWidth(), raster.getHeight(),
                    xHotSpot, yHotSpot);
        }
    }

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        isShowing = false;
        popupParentFrame.dispose();
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
        isShowing = false;
        popupParentFrame.dispose();
    }

    public void processEvent(int mouseState, int x, int y) {

        switch (mouseState) {
        case 0:
            break;

        case 1:
            if (!isShowing) {
                ListIterator li = actionList.listIterator(0);
                ActionListener al;

                while (li.hasNext()) {
                    al = (ActionListener) li.next();
                    al.actionPerformed(new ActionEvent(this,
                            ActionEvent.ACTION_PERFORMED, "PressAction",
                            System.currentTimeMillis(), 0));
                }
            }
            else {
             popupParentFrame.toFront();
            }
            break;

        case 2:
            if (!isShowing) {
                isShowing = true;
                popupParentFrame = new JWindow();
                popupParentFrame.setBounds(x, y, 1, 1);
                popupParentFrame.setVisible(true);
                menu.show(popupParentFrame, 0, 0);
                menu.addPopupMenuListener(this);
            }
            popupParentFrame.toFront();
            break;
        }
    }

    public synchronized static void notifyEvent(int id, final int mouseState, final int x, final int y) {
        final  WinTrayIconService instance = (WinTrayIconService) map.get(new Integer(id));

        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    instance.processEvent(mouseState, x, y);
                }
            });
        } catch (Exception e) {}
    }

    private void updateNativeIcon(Image img) {
        int w;
        int h;

        if (autoSize) {
            w = h = 16;
        } else {
            w = img.getWidth(observer);
            h = img.getHeight(observer);
        }
        BufferedImage tmpImage = new BufferedImage(w, h,
                BufferedImage.TYPE_INT_ARGB);
        Graphics g = tmpImage.getGraphics();

        try {
            ((Graphics2D) g).setComposite(AlphaComposite.Src);
            g.drawImage(img, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        tmpImage.flush();
        hicon = createNativeIcon(tmpImage, w, h, 0, 0);
    }

    private class DummyComponent extends Component {
        public boolean imageUpdate(Image img,
                int infoflags,
                int x,
                int y,
                int width,
                int height) {

            if (created) {
                updateNativeIcon(img);
                updateIcon(hicon, iconID, caption);
            }
            return true;
        }
    }

    public void setIcon(final Icon i) {
        icon = i;
        if (created) {
            updateNativeIcon(iconImage);
            updateIcon(hicon, iconID, caption);
        }
    }

    public void setCaption(String s) {
        caption = s;
        if (created) {
            updateNativeIcon(iconImage);
            updateIcon(hicon, iconID, caption);
        }
    }

    public void setIconAutoSize(boolean b) {
        autoSize = b;
        if (created) {
            updateNativeIcon(iconImage);
            updateIcon(hicon, iconID, caption);
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

        // PENDING implement me.
        return p;
    }

    void remove() {
        removeIcon(iconID);
    }

}
