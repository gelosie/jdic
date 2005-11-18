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


/**
 * Some of the players provides a more detailed description about the song, in this case they will return this interface, 
 * instead of the basic ISongInfo   
 *
 * @see org.jdesktop.jdic.mpcontrol.ISongInfo
 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer
 *
 * @author zsombor.gegesy
 *
 */
public interface IExtendedSongInfo extends ISongInfo {

    /**
     * 
     * @return the name of the album if it known.
     */
    public String getAlbum();
    
    /**
     * 
     * @return the name of the performer/author. 
     */
    public String getArtist();
    
    /**
     * 
     * @return the title of the song.
     */
    public String getTitle();

}
