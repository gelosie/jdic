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


import org.jdesktop.jdic.mpcontrol.ISongInfo;


/**
 * @author zsombor
 * 
 * Created at 18:30:04 net.sf.mpcontrol.bmp.BeepSongInfo
 * 
 */
public class BeepSongInfo implements ISongInfo {

    String title;
    String path;
    int trackNumber;

    /**
     * 
     */
    public BeepSongInfo() {}

    /**
     * @param title
     * @param path
     * @param trackNumber
     */
    public BeepSongInfo(String title, String path, int trackNumber) {
        this.title = title;
        this.path = path;
        this.trackNumber = trackNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.mpcontrol.ISongInfo#getSongTitle()
     */
    public String getSongTitle() {
        return title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.mpcontrol.ISongInfo#getPath()
     */
    public String getPath() {
        return path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sf.mpcontrol.ISongInfo#getTrackNumber()
     */
    public int getTrackNumber() {
        return trackNumber;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Title:" + title + "\n" + "Path:" + path + "\n" + "Track number:"
                + trackNumber;
    }
}
