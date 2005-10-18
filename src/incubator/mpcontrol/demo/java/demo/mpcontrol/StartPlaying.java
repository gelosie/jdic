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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;
import org.jdesktop.jdic.mpcontrol.MediaPlayerService;


public class StartPlaying {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        System.out.println("JDIC Media Player Control - Demo : StartPlaying");
        IMediaPlayer player = findRunningPlayer();

        try {
				
            System.out.println("Player selected:" + player.getDescription());
            for (int i = 0; i < args.length; i++) {
                try {
                    URL url = new URL(args[i]);

                    System.out.println("set media location to " + url);
                    player.setMediaLocation(url);
                    ISongInfo song = player.getCurrentSong();

                    System.out.println(
                            "  -> playing:" + song.getTrackNumber() + "."
                            + song.getSongTitle());

                } catch (MalformedURLException e) {
	
                    System.out.println("malformed url:" + args[i]);
                    player.setMediaLocation(new File(args[i]).toURI().toURL());
                    ISongInfo song = player.getCurrentSong();

                    System.out.println(
                            "  -> playing:" + song.getTrackNumber() + "."
                            + song.getSongTitle());
                }
            }
        } finally {

            if (player != null) {
                player.destroy();
            }
        }
    }
	
    public static IMediaPlayer findRunningPlayer() throws IOException {
        List players = MediaPlayerService.getInstance().getMediaPlayers();
		
        for (Iterator iter = players.iterator(); iter.hasNext();) {
            IMediaPlayer player = (IMediaPlayer) iter.next();
			
            player.init();
			
            // is already running? 
            if (player.isRunning() && !player.isPlaying()) {
                return player;
            }

            // no, release the native resources...
            player.destroy();
        }
		
        for (Iterator iter = players.iterator(); iter.hasNext();) {
            IMediaPlayer player = (IMediaPlayer) iter.next();
			
            player.init();
			
            // is already running? 
            if (player.isRunning()) {
                return player;
            }
            // can we start it? 
            if (player.startPlayerProcess()) {
                return player;
            }

            // no, release the native resources...
            player.destroy();
        }
		
        throw new RuntimeException("No suitable media player found!");
		
    }
}
