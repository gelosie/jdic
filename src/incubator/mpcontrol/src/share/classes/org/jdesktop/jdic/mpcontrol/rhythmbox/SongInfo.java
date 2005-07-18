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


import org.jdesktop.jdic.mpcontrol.IExtendedSongInfo;
import org.jdesktop.jdic.mpcontrol.ISongInfo;


/**
 * @author zsombor
 *
 * Created at 14:37:17
 * net.sf.mpcontrol.SongInfo 
 * 
 */
public class SongInfo implements ISongInfo, IExtendedSongInfo {
    
    String title;
    String artist;
    String genre;
    String album;           
    String path;
    int track_number;
    int duration;
    int bitrate;
    int filesize;
    int rating;
    int play_count;
    int last_played;           
    
    public SongInfo() {}
    
    /**
     * USED FROM NATIVE CODE!
     * @param title
     * @param artist
     * @param genre
     * @param album
     * @param path
     * @param track_number
     * @param duration
     * @param bitrate
     * @param filesize
     * @param rating
     * @param play_count
     * @param last_played
     */
    public SongInfo(String title, String artist, String genre, String album, String path, int track_number,
            int duration, int bitrate, int filesize, int rating, int play_count, int last_played) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
        this.album = album;
        this.path = path;
        this.track_number = track_number;
        this.duration = duration;
        this.bitrate = bitrate;
        this.filesize = filesize;
        this.rating = rating;
        this.play_count = play_count;
        this.last_played = last_played;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Title:" + title + "\n" + "Artist:" + artist + "\n" + "Genre:"
                + genre + "\n" + "Album:" + album + "\n" + "Path:" + path + "\n"
                + "Track number:" + track_number + "\n" + "Duration:" + duration
                + "\n" + "Bitrate:" + bitrate + "\n" + "Filesize:" + filesize
                + "\n" + "Rating:" + rating + "\n" + "Play count:" + play_count
                + '\n' + "Last played:" + last_played;
    }
    
    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public int getBitrate() {
        return bitrate;
    }

    public int getDuration() {
        return duration;
    }

    public int getFilesize() {
        return filesize;
    }

    public String getGenre() {
        return genre;
    }

    public int getLastPlayed() {
        return last_played;
    }

    public String getPath() {
        return path;
    }

    public int getPlayCount() {
        return play_count;
    }

    public int getRating() {
        return rating;
    }

    public String getTitle() {
        return title;
    }

    public int getTrackNumber() {
        return track_number;
    }
    
    /* (non-Javadoc)
     * @see net.sf.mpcontrol.ISongInfo#getSongTitle()
     */
    public String getSongTitle() {
        StringBuffer buf = new StringBuffer(256);
        boolean sep = false;

        if (artist != null) {
            buf.append(artist);
            sep = true;
        }
        if (album != null) {
            if (sep) {
                buf.append(" - ");
            }
            buf.append(album);
            sep = true;
        }
        if (title != null) {
            if (sep) {
                buf.append(" - ");
            }
            buf.append(title);
            sep = true;
        }
        
        return buf.toString();
    }
}
