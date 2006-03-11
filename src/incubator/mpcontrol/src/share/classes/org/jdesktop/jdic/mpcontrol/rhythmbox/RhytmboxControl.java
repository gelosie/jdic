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
package org.jdesktop.jdic.mpcontrol.rhythmbox;


import java.net.URL;
import java.util.logging.Logger;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;
import org.jdesktop.jdic.mpcontrol.impl.ProcessUtil;
import org.jdesktop.jdic.mpcontrol.impl.SongChangeAdapter;


/**
 * @author Zsombor Gegesy
 *
 * Created at 3:07:07
 * net.sf.mpcontrol.RhytmboxControl 
 * 
 */
public class RhytmboxControl extends SongChangeAdapter implements IMediaPlayer {
    
    static boolean nativeLibAvailable = false;
    static Logger log = Logger.getLogger("org.jdesktop.jdic.mpcontrol.rhythmbox");
	
    static {
        try {
            System.loadLibrary("jdic-rhythmbox");
            nativeLibAvailable = true;
        } catch (UnsatisfiedLinkError ule) {
            log.info("unsatisfied link error:" + ule.getMessage());
        }
    }
    
    private static int corbaEnvironment;
    private int gnomeRhythmbox;
    
    private native final static int createCorbaEnvironment();
    
    private native final static int acquireRhythmboxInstance(int corbaEnv);

    private native final static void releaseRhythmboxInstance(int corbaEnv, int rhythmbox);
    
    private native final static int freeCorbaEnvironment(int corbaEnv);
    
    private native final static void playPause0(int corbaEnv, int rhythmboxInstance);

    // private native final static void stop0(int corbaEnv,int rhythmboxInstance);

    private native final static void setVolume0(int corbaEnv, int rhythmboxInstance, float volume);
    
    private native final static void next0(int corbaEnv, int rhythmboxInstance);

    private native final static void previous0(int corbaEnv, int rhythmboxInstance);

    private native final static SongInfo getCurrentSong0(int corbaEnv, int rhythmboxInstance);
    
    private native void bonoboEventLoop(int corbaEnv, int rhythmboxInstance);
    
    private native final static boolean isPlaying(int corbaEnv, int rhythmboxInstance);
    
    private native final static void select(int corbaEnv, int rhythmboxInstance, String url);

    private native final static void play(int corbaEnv, int rhythmboxInstance, String url);
    
    public void init() {
        synchronized (RhytmboxControl.class) {
            if (corbaEnvironment == 0) {
                corbaEnvironment = createCorbaEnvironment();
                if (corbaEnvironment == 0) {
                    throw new RuntimeException("CORBA couldn't initialized!");
                }
            }
        }
        synchronized (this) {
            gnomeRhythmbox = acquireRhythmboxInstance(corbaEnvironment);
            if (gnomeRhythmbox == 0) {
                throw new RuntimeException("Rhythmbox not found!");
            }
        }
    }
    
    public synchronized void destroy() {
        if (gnomeRhythmbox != 0) {
            releaseRhythmboxInstance(corbaEnvironment, gnomeRhythmbox);
            gnomeRhythmbox = 0;
        }
        if (corbaEnvironment != 0) {
            freeCorbaEnvironment(corbaEnvironment);
            corbaEnvironment = 0;
        }
    }
    
    public void setVolume(float volume) {
        setVolume0(corbaEnvironment, gnomeRhythmbox, volume);
    }
    
    public void play() {
        if (!isPlaying(corbaEnvironment, gnomeRhythmbox)) {
            playPause0(corbaEnvironment, gnomeRhythmbox);
        }
    }

    public void pause() {
        if (isPlaying(corbaEnvironment, gnomeRhythmbox)) {
            playPause0(corbaEnvironment, gnomeRhythmbox);
        }
    }
    
    public boolean isPlaying() {
        return isPlaying(corbaEnvironment, gnomeRhythmbox);
    }
    
    public void next() {
        next0(corbaEnvironment, gnomeRhythmbox);
    }

    public void previous() {
        previous0(corbaEnvironment, gnomeRhythmbox);
    }
    
    public ISongInfo getCurrentSong() {
        return getCurrentSong0(corbaEnvironment, gnomeRhythmbox);
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#isRunning()
     */
    public boolean isRunning() {
        return gnomeRhythmbox != 0;
    }

    /* (non-Javadoc)
     * @see net.sf.mpcontrol.IMediaPlayer#getVolume()
     */
    public float getVolume() {
        return 0;
    }

    public boolean isAvailableMediaPlayer() {
        return nativeLibAvailable;
    }
    
    public String getName() {
    		return "rhythmbox";
    }

    public String getDescription() {
        return "Rhythmbox controller (http://www.rhythmbox.org)";
    }

    /**
     * start listening for song changes.
     */
    public void doListening() {
        bonoboEventLoop(corbaEnvironment, gnomeRhythmbox);
    }

    /**
     * this method gets called when we receive a bonobo event
     * @param newSong
     */
    public void onChange(SongInfo newSong) {
        fireEvents(newSong);
    }

    public void setMediaLocation(URL location) {
        select(corbaEnvironment, gnomeRhythmbox, location.toString());
        play(corbaEnvironment, gnomeRhythmbox, location.toString());
        log.warning(
                this.getClass().getName() + ".setMediaLocation(" + location
                + ") not working!");
		
    }

    public boolean startPlayerProcess() {
        return ProcessUtil.execute("rhythmbox");
    }

}
