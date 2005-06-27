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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import sun.awt.image.IntegerComponentRaster;


public class WinTrayIconService implements TrayIconService{

    private JPopupMenu menu;
    private Icon icon;
    private boolean autoSize;
    private byte[] caption = "JDIC TrayIcon".getBytes();

    private long hicon;
    private Image oldIconImage;
    static private HashMap map = new HashMap();

    private LinkedList actionList = new LinkedList();
    private LinkedList balloonListeners = new LinkedList();

    static int noIcons;

    int iconID;

    AnimationObserver observer;

    private final int WINDOWS_TASKBAR_ICON_WIDTH = 16;
    private final int WINDOWS_TASKBAR_ICON_HEIGHT = 16;

    static class PopupParent extends JDialog{
		public PopupParent(){
			super((Frame)null, "JDIC Tray Icon");
	        try{
	        	Method setAlwaysOnTop = this.getClass().getMethod("setAlwaysOnTop", new Class[]{boolean.class});
	        	setAlwaysOnTop.invoke(this, new Object[]{Boolean.TRUE});
	        }catch(NoSuchMethodException e){
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	        this.setUndecorated(true);
	        this.setBounds(0, 0, 0, 0);
		}
    }
    static PopupParent popupParentFrame;
	
    boolean created;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
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
        if(popupParentFrame == null){
            popupParentFrame = new PopupParent();
        }
    }

    private native long createIconIndirect(int[] rData, byte[] andMask,
            int nScanStride, int width,
            int height, int xHotSpot,
            int yHotSpot);

    private native void createIcon(long hIcon, int id, byte[] tooltip);

    private native void updateNativeIcon(long hIcon, int id, byte[] tooltip);
    
    private native void showBalloonMessage(long hIcon, int id, byte[] caption, byte[] text, int type);
    
    private native void deleteHIcon(long hIcon);
    
    private native int[] getRectangleOnScreen(int id);

    private static native void removeIcon(int id);
    
    public void showBalloonMessage(String caption, String text, int type){
    	byte[] batitle = caption == null ? new byte[0]: caption.getBytes();
    	byte[] bacontent = text == null ? new byte[0] : text.getBytes();
    	showBalloonMessage(hicon, iconID, batitle, bacontent, type);
    }
	
    public void addNotify() {
		if(popupParentFrame == null){
            popupParentFrame = new PopupParent();
		}
        popupParentFrame.pack();
        map.put(new Integer(iconID), this);

		observer = new AnimationObserver();
        updateIcon(null);
        created = true;
	}

    private void updateBufferedImage() {
    }

    public void setPopupMenu(JPopupMenu m) {
        menu = m;
        if (menu != null) {
            menu.setLightWeightPopupEnabled(false);

            menu.addPopupMenuListener(new PopupMenuListener() {
				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
					popupParentFrame.setVisible(false);
					popupParentFrame.toBack();
				}

				public void popupMenuCanceled(PopupMenuEvent e) {
				}
			});

            // in jdk1.4, the popup menu is still visible after the invoker window lost focus.
			popupParentFrame.addWindowFocusListener(new WindowFocusListener() {
				public void windowGainedFocus(WindowEvent e) {
				}

				public void windowLostFocus(WindowEvent e) {
					menu.setVisible(false);
				}
			});

		}
	}

    protected long createNativeIcon(BufferedImage bimage, int w, int h,
            int xHotSpot, int yHotSpot) {

        int  pixels[] = ((DataBufferInt) bimage.getRaster().getDataBuffer()).getData();
        Raster  raster = bimage.getRaster();
        byte[] andMask = new byte[(w * h) / 8];
        int npixels = pixels.length;

        for (int i = 0; i < npixels; i++) {
            int ibyte = i / 8;
            int omask = 1 << (7 - (i % 8));

            if ((pixels[i] & 0xff000000) == 0) {
                // Transparent bit
                if (ibyte < andMask.length) {
                    andMask[ibyte] |= omask;
                }
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

    public void processEvent(int mouseState, int x, int y, int modifiers) {
        
        switch (mouseState) {
        case 0x200: // WM_MOUSEMOVE
            break;

        case 0x202: // WM_LBUTTONUP
            ListIterator li = actionList.listIterator(0);
            ActionListener al;

            while (li.hasNext()) {
                al = (ActionListener) li.next();
                al.actionPerformed(new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED, "PressAction",
                        System.currentTimeMillis(), modifiers));
            }
            break;

        case 0x205: // WM_RBUTTONUP
            if (menu != null) {
            	Point p = new Point(x, y);
            	Dimension d = menu.getPreferredSize();
                Dimension s = Toolkit.getDefaultToolkit().getScreenSize();
                p.x = p.x + d.width > s.width ? p.x - d.width : p.x;
                p.y = p.y + d.height > s.height ? p.y - d.height : p.y;
                popupParentFrame.setVisible(true);
                menu.show(popupParentFrame.getContentPane(), p.x, p.y);
                popupParentFrame.toFront();
            }
            break;
        case 0x0405: // NIN_BALLOONUSERCLICK
            ListIterator ml = balloonListeners.listIterator(0);
            ActionListener listener;

            while (ml.hasNext()) {
                listener = (ActionListener) ml.next();
                listener.actionPerformed(new ActionEvent(this,
                        ActionEvent.ACTION_PERFORMED, "PressAction",
                        System.currentTimeMillis(), modifiers));
            }
            break;
        }
    }

    public synchronized static void notifyEvent(int id, final int mouseState, final int x, final int y, final int modifiers) {
        final  WinTrayIconService instance = (WinTrayIconService) map.get(new Integer(id));
        if(instance == null)
        	return;
        try {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    instance.processEvent(mouseState, x, y, modifiers);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateIcon(Image iconImage) {
        Graphics2D g;
        if (icon != null) {
            if (iconImage == null) {
                iconImage = new BufferedImage(icon.getIconWidth(),  icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                g = (Graphics2D) ((BufferedImage)iconImage).getGraphics();
                g.setComposite(AlphaComposite.Src);
                icon.paintIcon(observer, g, 0, 0);
                oldIconImage = iconImage;
            }

            // Temp image is used for scaling.

            BufferedImage tmpImage = new BufferedImage(WINDOWS_TASKBAR_ICON_WIDTH, WINDOWS_TASKBAR_ICON_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB);
            g = (Graphics2D) tmpImage.getGraphics();

            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(iconImage, 0, 0, WINDOWS_TASKBAR_ICON_WIDTH,WINDOWS_TASKBAR_ICON_HEIGHT, null);
            } finally {
                g.dispose();
            }
            tmpImage.flush();

            // Free old icon.
            if (hicon != 0) {
                deleteHIcon(hicon); 
            }
            hicon = createNativeIcon(tmpImage, WINDOWS_TASKBAR_ICON_WIDTH, WINDOWS_TASKBAR_ICON_HEIGHT, 0, 0);
            if (created) {
                updateNativeIcon(hicon, iconID, caption);
            }
            else {
                createIcon(hicon, iconID, caption);
            }
        }
    }

    private class AnimationObserver extends Component {
        boolean update = true;

        public void setUpdate(boolean b) {
            update = b;
        }
        public boolean imageUpdate(Image img,
                int infoflags,
                int x,
                int y,
                int width,
                int height) {

            if (update && created) {
                updateIcon(img);
            }
            return update;
        }
    }

    public void setIcon(final Icon i) {
        icon = i;
        if (created) {
            observer.setUpdate(false);
            observer = new AnimationObserver();
            updateIcon(null);
        }
    }

    public void setCaption(String s) {
        caption = (s == null ? "" : s ).getBytes();
        if (created) {
            observer.setUpdate(false);
            observer = new AnimationObserver();
            updateIcon(null);
        }
    }

    public void setIconAutoSize(boolean b) {
        // Not necessary for Win32 impl.
    }

    public void addActionListener(ActionListener l) {
        actionList.add(l);
    }

    public void removeActionListener(ActionListener l) {
        actionList.remove(l);
    }

    public Point getLocationOnScreen() {
        int[] rect = this.getRectangleOnScreen(iconID);
        return new Point(rect[0], rect[1]);
    }

    void remove() {
        removeIcon(iconID);
        // Free old icon.
        if (hicon != 0) {
            deleteHIcon(hicon); 
        }
		map.remove(new Integer(iconID));
        created = false;
		if(map.size() == 0){
			popupParentFrame.dispose();
			popupParentFrame = null;
		}
    }
    private static void restartTaskbar() {
            Iterator keyiterator = map.keySet().iterator();
            while (keyiterator.hasNext()) {
                WinTrayIconService iconService = (WinTrayIconService)map.get(keyiterator.next());
                if (iconService.created) { 
                    iconService.remove();
                    iconService.addNotify();
                }
            }
        }

    public void addBalloonActionListener(ActionListener al) {
        balloonListeners.add(al);
    }
    
    public void removeBalloonActionListener(ActionListener al) {
        balloonListeners.remove(al);
    }
}
