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

package demo.mpcontrol;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdesktop.jdic.mpcontrol.IExtendedSongInfo;
import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;
import org.jdesktop.jdic.mpcontrol.MediaPlayerService;


public class CurrentSong {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        System.out.println("JDIC Media Player Control - Demo : CurrentSong");
        List players = MediaPlayerService.getInstance().getMediaPlayers();
		
        for (Iterator iter = players.iterator(); iter.hasNext();) {
            IMediaPlayer player = (IMediaPlayer) iter.next();
			
            player.init();
            System.out.println("Player:" + player.getDescription());
            if (player.isRunning()) {
                System.out.println(" - currently running:");
				
                if (player.isPlaying()) {
					
                    ISongInfo song = player.getCurrentSong();
						
                    if (song instanceof IExtendedSongInfo) {
                        System.out.println(
                                "  -> playing: " + song.getTrackNumber()
                                + ". song from the tracklist");
                        IExtendedSongInfo e = (IExtendedSongInfo) song;

                        System.out.println("      artist:" + e.getArtist());
                        System.out.println("       album:" + e.getAlbum());
                        System.out.println("        song:" + e.getTitle());
                    } else {
                        System.out.println(
                                "  -> playing:" + song.getTrackNumber() + "."
                                + song.getSongTitle());
                    }
					
                    System.out.println("     from:" + song.getPath());
                } else {
                    System.out.println("not playing");
                }
				
            } else {
                System.out.println(" - currently not running");
            }

            player.destroy();
			
        }

    }

}
