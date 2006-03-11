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

/*
 * Created on 2005.04.30.
 *
 * In mpcontrol project
 */
package org.jdesktop.jdic.mpcontrol.bmp;


import java.net.URL;
import java.util.logging.Logger;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;
import org.jdesktop.jdic.mpcontrol.impl.ProcessUtil;


/**
 * This class can control the 'beep media player' and the 'xmms' player. 
 * @author Zsombor Gegesy
 *
 * Created at 17:29:02
 * org.jdesktop.jdic.mpcontrol.bmp.BeepMediaPlayer 
 * 
 */
public class BeepMediaPlayer implements IMediaPlayer {

    static boolean nativeLibAvailable = false;
    static Logger log = Logger.getLogger("org.jdesktop.jdic.mpcontrol.bmp");

    static {
        try {
            System.loadLibrary("jdic-bmp");
            nativeLibAvailable = true;
        } catch (UnsatisfiedLinkError ule) {
            log.info("unsatisfied link error:" + ule.getMessage());
        }
        
    }
    int sessionID = -1;

    public boolean isAvailableMediaPlayer() {
        return nativeLibAvailable;
    }
    
    public String getName() {
		return "xmms";
    }

    
    public String getDescription() {
        return "xmms / beep-media player";
    }
    
    public void init() {
        sessionID = getRemoteSession0();
        // System.out.println("Beep session ID is "+sessionID);
    }

    /**
     * @return
     */
    private static native int getRemoteSession0();

    private static native void play0(int sesID);

    private static native void stop0(int sesID);

    private static native void next0(int sesID);

    private static native void prev0(int sesID);

    private static native void pause0(int sesID);

    private static native int getVolume0(int sesID);

    private static native void setVolume0(int sesID, int value);

    private static native BeepSongInfo getCurrentSong0(int sesID);

    private static native boolean isPlaying0(int sesID);
    
    private static native void addUrl(int sesID, String path);
    
    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#destroy()
     */
    public void destroy() {// TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#setVolume(float)
     */
    public void setVolume(float volume) {
        setVolume0(sessionID, (int) (volume * 100));
    }

    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#play()
     */
    public void play() {
        play0(sessionID);
        
    }

    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#pause()
     */
    public void pause() {
        pause0(sessionID);
        
    }

    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#next()
     */
    public void next() {

        next0(sessionID);
    }

    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#previous()
     */
    public void previous() {
        prev0(sessionID);
        
    }

    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getCurrentSong()
     */
    public ISongInfo getCurrentSong() {
        return getCurrentSong0(sessionID);
    }
    
    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#isRunning()
     */
    public boolean isRunning() {
    	
        sessionID = getRemoteSession0();
        return sessionID != -1;
    }

    /* (non-Javadoc)
     * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getVolume()
     */
    public float getVolume() {
        return getVolume0(sessionID) / 100f;
    }

    public void setMediaLocation(URL location) {
        addUrl(sessionID, location.toString());
		
    }

    public boolean startPlayerProcess() {
        if (ProcessUtil.execute("beep-media-player")) {
            return true;
        }
        if (ProcessUtil.execute("xmms")) {
            return true;
        }
		
        return false;
    }
	
    public boolean isPlaying() {
        return isPlaying0(sessionID);
    }

    /*
     public static void main(String[] args) {
     BeepMediaPlayer r = new BeepMediaPlayer();
     r.init();
     
     if (r.isRunning()) {
     System.out.println("play/pause");
     
     ISongInfo sg = r.getCurrentSong();
     if (sg!=null) {
     System.out.println("Currently playing:"+sg);
     } else {
     System.out.println("not playing anything!");
     }
     r.play();
     
     System.out.println("...wait...");
     System.out.println("Volume is "+r.getVolume());
     Util.sleep(1000);
     System.out.println("play/pause");
     
     Util.sleep(1000);
     System.out.println("volume:0.5");
     //r.setVolume(0.5f);
     r.next();
     
     Util.sleep(2000);
     System.out.println("volume:1.0");
     //r.setVolume(1.0f);
     r.pause();
     } else {
     System.out.println("NOT RUNNING!");
     }
     
     
     }
     */


    
}
