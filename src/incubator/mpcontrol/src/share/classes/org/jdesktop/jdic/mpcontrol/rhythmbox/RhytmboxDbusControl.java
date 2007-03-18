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

/**
 * created on 2007.03.17
 */
package org.jdesktop.jdic.mpcontrol.rhythmbox;

import java.net.URL;
import java.util.Map;

import org.freedesktop.DBus;
import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.Variant;
import org.freedesktop.dbus.exceptions.DBusException;
import org.gnome.Rhythmbox.Player;
import org.gnome.Rhythmbox.Shell;
import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;
import org.jdesktop.jdic.mpcontrol.impl.ProcessUtil;

/**
 * @author Zsombor Gegesy
 * 
 * This class implements controls the Rhytmbox Music Player over DBUS. DBUS is a new interprocess communication mode on Unix like operating systems. 
 *
 */
public class RhytmboxDbusControl implements IMediaPlayer {

	Player player;
	Shell shell;
	DBusConnection connection;
	/**
	 * 
	 */
	public RhytmboxDbusControl() {
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#destroy()
	 */
	public synchronized void destroy() {
		if (connection!=null) {
			connection.disconnect();
			connection=null;
		}

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getCurrentSong()
	 */
	public ISongInfo getCurrentSong() {
		init();
		String uri = player.getPlayingUri();
		Map<String, Variant> songProperties = shell.getSongProperties(uri);
		SongInfo s = new SongInfo(
				toString(songProperties.get("title")),
				toString(songProperties.get("artist")),
				toString(songProperties.get("genre")),
				toString(songProperties.get("album")),
				toString(songProperties.get("location")),

				toInt(songProperties.get("track-number")),
				toInt(songProperties.get("duration")),
				toInt(songProperties.get("bitrate")),
				toInt(songProperties.get("file-size")),
				toInt(songProperties.get("rating")),
				toInt(songProperties.get("play-count")),
				toInt(songProperties.get("last-played"))
				);
		return s;
	}
	
	private int toInt(Variant variant) {
		Object obj = variant.getValue();

		if (obj instanceof Number) {
			return ((Number)obj).intValue();
		}
		throw new RuntimeException("Variant is not a number:"+obj+", class:"+obj.getClass());
	}
	
	private String toString(Variant variant) {
		Object obj = variant.getValue();
		if (obj instanceof String) {
			return (String) obj;
		}
		if (obj==null) {
			return null;
		}
		return obj.toString();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getDescription()
	 */
	public String getDescription() {
        return "Rhythmbox controller (http://www.rhythmbox.org)";
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getName()
	 */
	public String getName() {
		return "rhythmbox(over dbus)";
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getVolume()
	 */
	public float getVolume() {
		return (float) player.getVolume();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#init()
	 */
	public void init() {
		try {
			getConnection();
			getPlayer();
			getShell();
			
		} catch (DBusException e) {
			e.printStackTrace();
			throw new RuntimeException("Error during acquiring DBUS connection",e);
			
		}
	}

	private Shell getShell() throws DBusException {
		if (shell==null) {
			shell =  (Shell) getConnection().getRemoteObject("org.gnome.Rhythmbox", "/org/gnome/Rhythmbox/Shell", Shell.class);
		}
		return shell;
	}

	private Player getPlayer() throws DBusException {
		if (player==null) {
			player = (Player) getConnection().getRemoteObject("org.gnome.Rhythmbox", "/org/gnome/Rhythmbox/Player", Player.class);
		}
		return player;
	}

	private final synchronized DBusConnection getConnection() throws DBusException {
		if (connection==null) {
			connection = DBusConnection.getConnection(DBusConnection.SESSION);
		}
		return connection;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#isAvailableMediaPlayer()
	 */
	public boolean isAvailableMediaPlayer() {
		try {
			getConnection();
			return true;
		} catch (DBusException e) {
			e.printStackTrace();
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#isPlaying()
	 */
	public boolean isPlaying() {
		init();
		return player.getPlaying();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#isRunning()
	 */
	public boolean isRunning() {
		try {
			DBusConnection con = getConnection();
			DBus dbus = (DBus) con.getRemoteObject("org.freedesktop.DBus","/org/freedesktop/DBus", DBus.class);
			for (String name : dbus.ListNames()) {
				if ("org.gnome.Rhythmbox".equals(name)) {
					return true;
				}
				
			}
		} catch (DBusException e) {
			e.printStackTrace();
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#next()
	 */
	public void next() {
		init();
		player.next();

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#pause()
	 */
	public void pause() {
		init();
		player.playPause(false);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#play()
	 */
	public void play() {
		init();
		player.playPause(true);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#previous()
	 */
	public void previous() {
		init();
		player.previous();

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#setMediaLocation(java.net.URL)
	 */
	public void setMediaLocation(URL location) {

		init();
		shell.loadURI(location.toString(), true);

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#setVolume(float)
	 */
	public void setVolume(float volume) {
		init();
		player.setVolume(volume);
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#startPlayerProcess()
	 */
	public boolean startPlayerProcess() {
		try {
			init();
			player.playPause(true);
	
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
