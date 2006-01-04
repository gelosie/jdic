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
 * Created on Apr 29, 2005
 *
 * mpcontrol
 */
package org.jdesktop.jdic.mpcontrol.winamp;


import org.jdesktop.jdic.mpcontrol.IExtendedSongInfo;
import org.jdesktop.jdic.mpcontrol.ISongInfo;


/**
 * @author zsombor_gegesy
 * Created :  Apr 29, 2005
 * mpcontrol
 * 
 */
public class SongInfo implements ISongInfo, IExtendedSongInfo {

	String path;
    String artist;
    String title;
    int trackNumber;
    
    /**
     * @param path the path to the music file 
     * @param artist
     * @param title
     */
    SongInfo(String path,int trackNumber, String artist, String title) {
    	this.path = path;
        this.trackNumber = trackNumber;
        this.artist = artist;
        this.title = title;
    }

    /**
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.ISongInfo#getSongTitle()
     */
    public String getSongTitle() {
        return artist + " - " + title;
    }

    /* (non-Javadoc)
     * @see net.sf.mpcontrol.ISongInfo#getPath()
     */
    public String getPath() {
        return path;
    }

    /* (non-Javadoc)
     * @see net.sf.mpcontrol.ISongInfo#getTrackNumber()
     */
    public int getTrackNumber() {
        return trackNumber;
    }

    public String getAlbum() {
        return "";
    }

    public String getArtist() {
        return artist;
    }
}
