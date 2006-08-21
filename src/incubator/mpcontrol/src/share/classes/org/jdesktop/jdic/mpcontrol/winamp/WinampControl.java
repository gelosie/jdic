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


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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

    private static int IPC_STARTPLAY = 102;
    private static int IPC_ISPLAYING = 104;
    private static int IPC_SETPLAYLISTPOS = 121;
    private static int IPC_SETVOLUME = 122;
    private static int IPC_GETLISTLENGTH = 124;
    private static int IPC_GETLISTPOS =125;

    
    private static int WM_WA_IPC = 1024;
    private static int WM_USER = 1024;
    private static int WM_COMMAND = 273 ;
    
	private static int WM_PREV  = 40044;
	private static int WM_PLAY  = 40045;
	private static int WM_PAUSE = 40046;
	private static int WM_STOP  = 40047;
	private static int WM_NEXT  = 40048;
    
    private int hwnd;
    
    private static native int findWindow();
    
    private static native long getVersion(int hwnd);

    private static native String getFileNameFromPlayList(int hwnd, int playListPosition);
    
    private static native void addToPlayList(int hwnd, String fileName);
    
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
        //int trackNumber = Integer.parseInt(txt.substring(0, pos - 1));

        int playListPosition = getPlayListPosition();
        String fileName = getFileName(playListPosition);
        
        if (endPos == titleEndPos) {
            return new SongInfo(fileName, playListPosition+1, "",
                    txt.substring(pos + 1, titleEndPos));
        } else {
            return new SongInfo(fileName, playListPosition+1, txt.substring(pos, endPos),
                    txt.substring(endPos + 3, titleEndPos));
        }
    }

    public boolean isAvailableMediaPlayer() {
        return nativeLibAvailable;
    }

    public String getName() {
		return "winamp";
    }

    public String getDescription() {
        return "Winamp 2 and 5 control";
    }

    public void setMediaLocation(URL location) {

        if ("file".equals(location.getProtocol())) {

        	String path = location.getPath();
        	if (path.startsWith("/")) 
        		path = path.substring(1);
        	
        	try {
				path = URLDecoder.decode(path, "utf-8");
	        	path = path.replace('/', '\\');
	        	log.info("path:"+path);
	
	        	addMedia(path);
        	} catch (UnsupportedEncodingException e) {
        		e.printStackTrace();
        		throw new RuntimeException("Something serious happened:"+e.getMessage(),e);
        	}
        	
        } else {
            log.warning(
                    this.getClass().getName() + ".setMediaLocation(" + location
                    + ") not implemented");
        }
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

    // misc. methods
    
    public void setPlayListPosition(int position) {
    	Util.sendMessage(hwnd,WM_WA_IPC,position,IPC_SETPLAYLISTPOS);
    }
    
    public int getPlayListPosition() {
    	return (int) Util.sendMessage(hwnd,WM_WA_IPC,0,IPC_GETLISTPOS);
    }
    
    public int getPlayListLength() {
    	return (int) Util.sendMessage(hwnd,WM_WA_IPC,0,IPC_GETLISTLENGTH);
    }
    
    public String getCurrentFileName() {
    	return getFileNameFromPlayList(hwnd, getPlayListPosition());
    }
    
    public String getFileName(int position) {
    	return getFileNameFromPlayList(hwnd, position);
    }
    
    public void addMedia(String fileName) {
        hwnd = findWindow(); 
    	addToPlayList(hwnd, fileName);
    }
    
    public static void main(String[] args) {
    	WinampControl wc = new WinampControl();
    	wc.init();
    	
    	if (wc.isRunning()) {
    		System.out.println("Winamp is running");
    	} else {
    		if (!wc.startPlayerProcess()) {
    			System.out.println("Winamp is not running, and i'm unable to start it!");
    			return;
    		}
    	}
    	
    	System.out.println("play list length:"+wc.getPlayListLength());
    	System.out.println("play list position:"+wc.getPlayListPosition());
    	System.out.println("current file :"+wc.getCurrentFileName());
    	
    	
    	if (args.length>0) {
    		//wc.addMedia(args[0]);
    		try {
				wc.setMediaLocation(new File(args[0]).toURL());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
        	System.out.println("play list length:"+wc.getPlayListLength());
        	System.out.println("play list position:"+wc.getPlayListPosition());
        	System.out.println("current file :"+wc.getCurrentFileName());
    	}
    }
    
    
}
