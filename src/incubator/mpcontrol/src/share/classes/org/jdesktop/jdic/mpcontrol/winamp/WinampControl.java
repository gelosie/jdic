/*
 * Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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

package org.jdesktop.jdic.mpcontrol.winamp;


import java.net.URL;
import java.util.logging.Logger;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;
import org.jdesktop.jdic.mpcontrol.MediaPlayerService;
import org.jdesktop.jdic.mpcontrol.impl.ProcessUtil;


/**
 * @author zsombor_gegesy
 * Created :  Apr 29, 2005
 * mpcontrol
 * 
 */
public class WinampControl implements IMediaPlayer {

    static boolean nativeLibAvailable = false;
	
    static Logger log = Logger.getLogger("org.jdesktop.jdic.mpcontrol.winamp");
	
    static {
        try {
            System.loadLibrary("jdic-winamp");
            nativeLibAvailable = true;
        } catch (UnsatisfiedLinkError ule) {

            log.info("unsatisfied link error:" + ule.getMessage());
        }
    }

    private int hwnd;
    
    private static native int findWindow();
    
    private static native long getVersion(int hwnd);
    
    public WinampControl() {}
    
    public void init() {
        hwnd = findWindow();
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#destroy()
     */
    public void destroy() {}

    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#isRunning()
     */
    public boolean isRunning() {
        hwnd = findWindow(); 
        return hwnd != 0;
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#getVolume()
     */
    public float getVolume() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#setVolume(float)
     */
    public void setVolume(float volume) {
        Util.sendMessage(hwnd, WM_WA_IPC, (int) (volume * 255), IPC_SETVOLUME);
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#play()
     */
    public void play() {
        Util.sendMessage(hwnd, WM_WA_IPC, 0, IPC_STARTPLAY);
    }
    
    public String getVersion() {
        long wers = getVersion(hwnd);

        return "Winamp " + (wers >> 16) + "." + (wers & 0xff);
    }
    
    
    private static int IPC_STARTPLAY = 102;
    private static int IPC_ISPLAYING = 104;
    private static int IPC_SETVOLUME = 122;
    
    
    private static int WM_WA_IPC = 1024;
    private static int WM_USER = 1024;
    private static int WM_COMMAND = 273 ;
    
		private static int WM_PREV  = 40044;
		private static int WM_PLAY  = 40045;
		private static int WM_PAUSE = 40046;
		private static int WM_STOP  = 40047;
		private static int WM_NEXT  = 40048;
    
    
    public boolean isPlaying() {
        long res = Util.sendMessage(hwnd, WM_WA_IPC, 0, IPC_ISPLAYING);

        // case 0 : stoped
        // case 1 : playing
        // case 3 : paused
        return res == 1;
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#next()
     */
    public void next() {
        hwnd = findWindow(); 
        long res = Util.sendMessage(hwnd, WM_COMMAND, WM_NEXT, 0);
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#pause()
     */
    public void pause() {
        hwnd = findWindow(); 
        long res = Util.sendMessage(hwnd, WM_COMMAND, WM_PAUSE, 0);
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#previous()
     */
    public void previous() {
        hwnd = findWindow(); 
        long res = Util.sendMessage(hwnd, WM_COMMAND, WM_PREV, 0);

    }
    
    public ISongInfo getCurrentSong() {
        String txt = Util.getWindowText(hwnd);
        int pos = txt.indexOf('.') + 1;
        int endPos = txt.indexOf(" - ");
        int titleEndPos = txt.indexOf(" - Winamp");
        int trackNumber = Integer.parseInt(txt.substring(0, pos - 1));

        if (endPos == titleEndPos) {
            return new SongInfo(trackNumber, "",
                    txt.substring(pos + 1, titleEndPos));
        } else {
            return new SongInfo(trackNumber, txt.substring(pos, endPos),
                    txt.substring(endPos + 3, titleEndPos));
        }
    }

    public boolean isAvailableMediaPlayer() {
        return nativeLibAvailable;
    }

    public String getDescription() {
        return "Winamp 2 and 5 control";
    }

    public void setMediaLocation(URL location) {
        log.warning(
                this.getClass().getName() + ".setMediaLocation(" + location
                + ") not implemented");
		
    }

    public boolean startPlayerProcess() {
        if (isRunning()) {
            return true;
        }
        
        return ProcessUtil.execute(System.getProperty("org.jdesktop.jdic.mpcontrol.winamp.path")) || 
        	ProcessUtil.execute((String) MediaPlayerService.getProperties().get("org.jdesktop.jdic.mpcontrol.winamp.path")) ||
        	ProcessUtil.execute("winamp.exe") || 
        	ProcessUtil.execute("C:\\Program Files\\Winamp\\winamp.exe");
    }

}
