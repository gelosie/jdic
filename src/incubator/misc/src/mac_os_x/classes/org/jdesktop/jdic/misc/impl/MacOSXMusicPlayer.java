package org.jdesktop.jdic.misc.impl;

import java.io.*;
import java.util.Date;
import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.NSApplication;
import org.jdesktop.jdic.misc.MusicPlayer;

public class MacOSXMusicPlayer extends MusicPlayer implements Runnable {
	public MacOSXMusicPlayer() {
	}
	
	public void run() {
		while(go) {
			try {
				setArtist(getString("tell app \"iTunes\" to artist of current track as string"));
				setAlbumName(getString("tell app \"iTunes\" to album of current track as string"));
				setTrackName(getString("tell app \"iTunes\" to name of current track as string"));
				setTrackNumber(getString("tell app \"iTunes\" to track number of current track as integer"));
				setAlbumSize(getString("tell app \"iTunes\" to track count of current track as integer"));
				setPlayerPosition(getString("tell app \"iTunes\" to player position as integer"));
				
				// sleep for 2 sec
				Thread.currentThread().sleep(2000);
			} catch (Exception ex) {
			}
		}
	}
	
	private String getString(String script) {
		NSAppleScript myscript = new NSAppleScript(script);
		NSMutableDictionary errors = new NSMutableDictionary();
		NSAppleEventDescriptor results = myscript.execute(errors);
		String str = results.stringValue();
		return str;
	}
	
	private boolean go = true;
	
	private String artist = "";
	protected synchronized void setArtist(String str) {
		this.artist = str;
	}
	public synchronized String getArtist() {
		return this.artist;
	}
	
	private String album = "";
	protected synchronized void setAlbumName(String str) {
		this.album = str;
	}
	public synchronized String getAlbumName() {
		return this.album;
	}
	
	private String trackname = "";
	protected synchronized void setTrackName(String str) {
		this.trackname = str;
	}
	public String getTrackName() {
		return this.trackname;
	}
	
	
	
	private int position = 0;
	private long last_set_time;
	private int prev_pos = 0;
	public synchronized int getPlayerPosition() {
		if(position == prev_pos) {
			return position;
		}
		long current_time = new Date().getTime();
		long diff = current_time - this.last_set_time;
		//u.p("diff = " + diff);
		return position + (int)(diff/1000);
	}
	
	
	protected synchronized void setPlayerPosition(String pos) {
		this.prev_pos = this.position;
		this.last_set_time = new Date().getTime();
		this.position = Integer.parseInt(pos);
	}
	
	private int track_number;
	protected synchronized void setTrackNumber(String num) {
		this.track_number = Integer.parseInt(num);
	}
	public synchronized int getTrackNumber() {
		return this.track_number;
	}
	
	
	private int album_size;
	protected synchronized void setAlbumSize(String num) {
		this.album_size = Integer.parseInt(	num);
	}
	public synchronized int getAlbumSize() {
		return this.album_size;
	}
	
	public void start() {
		go = true;
		new Thread(this).start();
	}

	public void stop() {
		go = false;
	}
	
	public void previousTrack() { 
		try {
			Runtime rt = Runtime.getRuntime();
					
			String[] args = { "osascript", "-e",
				"tell app \"iTunes\" to artist of current track as string"};
			args[2] = "tell app \"iTunes\" to previous track";
			Process proc = rt.exec(args);
		} catch (Exception ex) {
			System.out.println("ex = " + ex);
		}
	}
	
	public void playPause() {
		try {
		Runtime rt = Runtime.getRuntime();
		String[] args = { "osascript", "-e",
			"tell app \"iTunes\" to artist of current track as string"};
		args[2] = "tell app \"iTunes\" to playpause";
		Process proc = rt.exec(args);
		} catch (Exception ex) {
			System.out.println("ex = " + ex);
		}
	}
	
	public void nextTrack() {
		try {
		Runtime rt = Runtime.getRuntime();
				
		String[] args = { "osascript", "-e",
			"tell app \"iTunes\" to artist of current track as string"};
		args[2] = "tell app \"iTunes\" to next track";
		Process proc = rt.exec(args);
		} catch (Exception ex) {
			System.out.println("ex = " + ex);
		}
	}
}
