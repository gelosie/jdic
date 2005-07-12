/*
 *  Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
 *  subject to license terms.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
 
package org.jdesktop.jdic.misc;

/** This class interfaces with the users default music player to control it
(launch, start, stop, switch tracks, get current artist, etc.) It does not embed
a music player into your java app, it just controls music player from another process.

 * @author     Joshua Marinacci <a href="mailto:joshua@marinacci.org">joshua@marinacci.org</a>
 * @created    July 12, 2005
 */

public class MusicPlayer {
	
	
	public String getArtist() {
		return null;
	}
	public String getAlbumName() {
		return null;
	}
	public String getTrackName() {
		return null;
	}
	public int getPlayerPosition() {
		return -1;
	}
	public void start() { }
	public void stop() { }
	public int getTrackNumber() {return -1; }
	public int getAlbumSize() { return -1; }
	public void nextTrack() { }
	public void previousTrack() { }
	public void playPause() { }
	
	
	protected MusicPlayer() {
	}
	
	private static MusicPlayer _player = null;
	public static MusicPlayer newInstance() throws IllegalAccessException,
		InstantiationException, ClassNotFoundException {
		if (_player == null) {
			String os_name  = System.getProperty("os.name");
			System.out.println("os name = " + os_name);
			if (os_name.toLowerCase().startsWith("mac os x")) {
				loadMac();
			} else if (os_name.toLowerCase().startsWith("windows")) {
				loadWin();
			} else {
				_player = new MusicPlayer();
			}
		}
		return _player;
	}
	
	private static void loadMac() throws IllegalAccessException, 
			InstantiationException, ClassNotFoundException{
		_player = (MusicPlayer) MusicPlayer.class.forName(
				"org.jdesktop.jdic.misc.impl.MacOSXMusicPlayer")
				.newInstance();
	}

	private static void loadWin() throws IllegalAccessException, 
			InstantiationException, ClassNotFoundException{
		System.out.println("about to start the win alerter imp");
		_player = (MusicPlayer) MusicPlayer.class.forName(
				"org.jdesktop.jdic.misc.impl.WinAlerter")
				.newInstance();
	}
	
}
	
