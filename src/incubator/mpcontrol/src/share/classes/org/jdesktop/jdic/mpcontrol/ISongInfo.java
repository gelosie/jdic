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
 * This interface encapsulates basic informations about a music file: title,
 * the path to file, and the position in the track list. An IMediaPlayer is free to 
 * provide more information, in that case, the object also implement IExtendedSongInfo.
 *
 * @see org.jdesktop.jdic.mpcontrol.IExtendedSongInfo 
 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer 
 * @author zsombor.gegesy
 *
 */
public interface ISongInfo {

    /**
     * @return the title of the song. It is possible to the song title is an aggregate of the artist/album/title. 
     */
    public abstract String getSongTitle();

    /**
     * 
     * @return the path to the media file.
     */
    public abstract String getPath();
    
    /**
     * 
     * @return the position in the playlist if it applicable, negative if unknown.
     */
    public abstract int getTrackNumber();
}
