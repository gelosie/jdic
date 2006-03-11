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
package org.jdesktop.jdic.mpcontrol.itunes;

import java.net.URL;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;

/**
 * @author Zsombor
 *
 */
public class ITunesMacOSX implements IMediaPlayer {


		public ITunesMacOSX () {
		}
		
		
	class Watchdog implements Runnable { 
		public void run() {
			while(go) {
				try {
					setArtist(getString("tell app \"iTunes\" to artist of current track as string"));
					setAlbumName(getString("tell app \"iTunes\" to album of current track as string"));
					setTrackName(getString("tell app \"iTunes\" to name of current track as string"));
					setTrackNumber(getString("tell app \"iTunes\" to track number of current track as integer"));
					setAlbumSize(getString("tell app \"iTunes\" to track count of current track as integer"));
					setPlayerPosition(getString("tell app \"iTunes\" to player position as integer"));
					
					running = true;
					// sleep for 2 sec
					Thread.sleep(2000);
				} catch (Exception ex) {
					running = false;
				}
			}
		}
	}
		
		String getString(String script) {
			/*NSAppleScript myscript = new NSAppleScript(script);
			NSMutableDictionary errors = new NSMutableDictionary();
			NSAppleEventDescriptor results = myscript.execute(errors);
			String str = results.stringValue();
			return str;*/
			return "";
		}
		
		boolean go = true;
		boolean running = false;
		
		private String artist = "";
		private String album = "";
		private String trackname = "";
		private int position = 0;
		private long last_set_time;
		private int prev_pos = 0;
		private int track_number;
		private int album_size;

		synchronized void setArtist(String str) {
			this.artist = str;
		}
		public synchronized String getArtist() {
			return this.artist;
		}
		
		synchronized void setAlbumName(String str) {
			this.album = str;
		}
		public synchronized String getAlbumName() {
			return this.album;
		}
		
		synchronized void setTrackName(String str) {
			this.trackname = str;
		}
		public String getTrackName() {
			return this.trackname;
		}
		
		
		
		public synchronized int getPlayerPosition() {
			if(position == prev_pos) {
				return position;
			}
			long current_time = System.currentTimeMillis();
			long diff = current_time - this.last_set_time;
			//u.p("diff = " + diff);
			return position + (int)(diff/1000);
		}
		
		
		synchronized void setPlayerPosition(String pos) {
			this.prev_pos = this.position;
			this.last_set_time = System.currentTimeMillis();
			this.position = Integer.parseInt(pos);
		}
		
		synchronized void setTrackNumber(String num) {
			this.track_number = Integer.parseInt(num);
		}
		public synchronized int getTrackNumber() {
			return this.track_number;
		}
		
		
		synchronized void setAlbumSize(String num) {
			this.album_size = Integer.parseInt(	num);
		}
		public synchronized int getAlbumSize() {
			return this.album_size;
		}

		
		int callOsaScript(String command) {
			try {
				Runtime rt = Runtime.getRuntime();
						
				String[] args = { "osascript", "-e",
					command};
				Process proc = rt.exec(args);
				return proc.waitFor();
			} catch (Exception ex) {
				System.out.println("ex = " + ex);
				return -1;
			}
			
		}
		
		public void playPause() {
			callOsaScript("tell app \"iTunes\" to playpause");
		}
		
		
		public boolean isAvailableMediaPlayer() {
			return callOsaScript("tell app \"iTunes\"")>=0;
		}
		public synchronized void init() {
			if (go)
				return;
			go = true;
			Thread t = new Thread(new Watchdog(),"iTunes-Watchdog");
			t.setDaemon(true);
			t.start();
			
		}
		public String getDescription() {
			return "iTunes for Mac OS X Control";
		}
		public String getName() {
			return "itunes";
		}
		
		public void destroy() {
			go = false;
		}
		public boolean isRunning() {
			return running;
		}
		public void setVolume(float volume) {
			
		}
		public float getVolume() {
			// TODO Auto-generated method stub
			return 0;
		}
		public void play() {
			//callOsaScript("tell app \"iTunes\" to play");
			playPause();
		}
		public void pause() {
			playPause();
			
		}
		public boolean isPlaying() {
			return false;
		}
		public void next() {
			callOsaScript("tell app \"iTunes\" to next track");
		}
		public void previous() {
			callOsaScript("tell app \"iTunes\" to previous track");
			
		}
		public ISongInfo getCurrentSong() {
			return new ITunesSongInfo(album,artist,trackname,track_number,null);
		}
		public void setMediaLocation(URL location) {
			
		}
		public boolean startPlayerProcess() {
			return false;
		}
}
