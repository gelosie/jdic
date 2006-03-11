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
package org.jdesktop.jdic.mpcontrol.gmb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.ISongInfo;
import org.jdesktop.jdic.mpcontrol.MediaPlayerService;
import org.jdesktop.jdic.mpcontrol.impl.ProcessUtil;

/**
 * @author zsombor
 *
 */
public class GMusicBrowser implements IMediaPlayer {

	String fifoPath;
	
	/**
	 * 
	 */
	public GMusicBrowser() {
		super();
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#isAvailableMediaPlayer()
	 */
	public boolean isAvailableMediaPlayer() {
		return new File(getFifoPath()).getParentFile().exists();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#init()
	 */
	public void init() {

	}

    public String getName() {
		return "gmusicbrowser";
    }

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getDescription()
	 */
	public String getDescription() {
		return "GMusicBrowser (http://squentin.free.fr/gmusicbrowser/gmusicbrowser.html)";
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#destroy()
	 */
	public void destroy() {

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#isRunning()
	 */
	public boolean isRunning() {
		return new File(getFifoPath()).canWrite();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#setVolume(float)
	 */
	public void setVolume(float volume) {
		throw new RuntimeException(this.getClass()+".setVolume not supported!");

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getVolume()
	 */
	public float getVolume() {
		throw new RuntimeException(this.getClass()+".getVolume not supported!");
//		return 0;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#play()
	 */
	public void play() {
		sendCommand("PlayPause\n");

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#pause()
	 */
	public void pause() {
		sendCommand("PlayPause\n");

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#isPlaying()
	 */
	public boolean isPlaying() {
		return isRunning();
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#next()
	 */
	public void next() {
		sendCommand("NextSong\n");

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#previous()
	 */
	public void previous() {

		sendCommand("PrevSong\n");
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#getCurrentSong()
	 */
	public ISongInfo getCurrentSong() {
		throw new RuntimeException(this.getClass()+".getCurrentSong not supported!");
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#setMediaLocation(java.net.URL)
	 */
	public void setMediaLocation(URL location) {
		throw new RuntimeException(this.getClass()+".setMediaLocation not supported!");

	}

	/* (non-Javadoc)
	 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer#startPlayerProcess()
	 */
	public boolean startPlayerProcess() {
		return ProcessUtil.execute("gmusicbrowser") || ProcessUtil.execute("/usr/bin/gmusicbrowser");
	}

	
	protected String getFifoPath() {
		String path = System.getProperty("org.jdesktop.jdic.mpcontrol.gmusicbrowser.path");
		if (path==null)
			path = (String) MediaPlayerService.getProperties().get("org.jdesktop.jdic.mpcontrol.gmusicbrowser.path");
		if (path==null) {
			path = System.getProperty("user.home",".")+"/.gmusicbrowser/gmusicbrowser.fifo";
		}
		return path;
	}
	
	protected void sendCommand(String line) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(getFifoPath());
			fos.write(line.getBytes("ASCII"));
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws InterruptedException {
		IMediaPlayer player = new GMusicBrowser();
		if (player.isAvailableMediaPlayer())  {
			player.init();
			if (player.isRunning()) {
				System.out.println("pause...");
				player.pause();
				Thread.sleep(2000);
				System.out.println("next...");
				player.next();
				System.out.println("play..");
				player.play();
				Thread.sleep(5000);
				System.out.println("prev...");
				player.previous();
				
			} else {
				System.out.println("GMusicBrowser not running!");
			}
			
			
			player.destroy();
		} else {
			System.out.println("GMusicBrowser not available!");
		}
		
		
		
		
	}
	
}
