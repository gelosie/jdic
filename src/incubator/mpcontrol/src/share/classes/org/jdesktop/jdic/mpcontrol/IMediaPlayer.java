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

package org.jdesktop.jdic.mpcontrol;


import java.net.URL;


/**
 * This is the main interface for communicating with the native players.
 * @author zsombor.gegesy
 *
 */
public interface IMediaPlayer {

    /**
     * returns true if the media player is supported on this platform.
     * @return
     */
    public abstract boolean isAvailableMediaPlayer();
	
    /**
     * initialize the media player - create the nativ resources to access it, or start the external process, 
     * this method must be called before any other method.
     *
     */
    public abstract void init();

    /**
     * 
     * @return information about the supported program.
     */
    public String getDescription();
    
    /**
     * destroy/release any native resource. This must be called when there is no more need to access the player.  
     *
     */
    public abstract void destroy();

    /**
     * returns true if the external process is currently running.
     * @return 
     */
    public boolean isRunning(); 

    /**
     * set the volume on the player.
     * @param volume
     */
    public abstract void setVolume(float volume);
    
    /**
     * gets the volume. This value is between 0.0 and 1.0.
     * @return
     */
    public abstract float getVolume();

    /**
     * start playing.
     *
     */
    public abstract void play();

    /**
     * pause the play.
     *
     */
    public abstract void pause();
    
    /**
     * 
     * @return true if the media player is currently playing
     */
    public boolean isPlaying();

    /**
     * advance to the next track/song. 
     *
     */
    public abstract void next();

    /**
     * step back in the tracklist. 
     *
     */
    public abstract void previous();

    /**
     * returns the currently played song.
     * @return
     */
    public abstract ISongInfo getCurrentSong();	
    
    /**
     * Add the specified media to the playlist and/or start playing the song.
     * @param location
     */
    public void setMediaLocation(URL location);
    
    /**
     * Try to start the native media player.
     * @return true if succeeds.
     */
    public boolean startPlayerProcess();
    
}
