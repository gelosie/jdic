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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jdesktop.jdic.mpcontrol.IMediaPlayer;
import org.jdesktop.jdic.mpcontrol.MediaPlayerService;

public class SimpleControl {

	private static final List NOT_NAME = Arrays.asList(new String[] { "default","playing","running"});



	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		MediaPlayerService mps =MediaPlayerService.getInstance(); 
		if (args.length<2) {
			usage(mps);
		}

		IMediaPlayer imp = search(mps,args[0]);
		if (imp==null) {
			System.out.println("player '"+args[0]+"' not found!");
			System.exit(-2);
		}
		String cmd = args[1];
		
		imp.init();
		try {
			if ("play".equalsIgnoreCase(cmd)) {
				imp.play();
				System.out.println("play - OK.");
				return;
			}
			if ("pause".equalsIgnoreCase(cmd)) {
				imp.pause();
				System.out.println("pause - OK.");
				return;
			}
			if ("next".equalsIgnoreCase(cmd)) {
				imp.next();
				System.out.println("next - OK.");
				return;
			}
			if ("prev".equalsIgnoreCase(cmd)) {
				imp.previous();
				System.out.println("prev - OK.");
				return;
			}
			if ("next".equalsIgnoreCase(cmd)) {
				imp.next();
				System.out.println("next - OK.");
				return;
			}
			
			System.out.println("Unknown command:"+cmd);

		} finally {
			imp.destroy();
		}
		usage(mps);

	}



	/**
	 * @param mps
	 */
	private static void usage(MediaPlayerService mps) {
		System.out.println("media-player-control <player-name> <command> [<parameters>]");
		System.out.println("player-name:");
		System.out.println("  'playing' - execute the command on a currently playing media player");
		System.out.println("  'running' - execute the command on a currently running media player");
		System.out.println("  'default' -  execute the command on a supported media player");
		
		for (Iterator iter=mps.getMediaPlayers().iterator();iter.hasNext();) {
			IMediaPlayer imp = (IMediaPlayer) iter.next();
			System.out.println("  '"+imp.getName()+"'  - "+imp.getDescription());
		}
		System.out.println("command:");
		System.out.println("  play,pause,next,previous,stop,exit");
		
		System.exit(-1);
	}

	
	
	private static IMediaPlayer search(MediaPlayerService mps, String name) {
	
		boolean nameSearch = Collections.binarySearch(NOT_NAME,name)<0; 
		System.out.println("locating "+name+" (nameSearch="+nameSearch+")");
		if (nameSearch) {
			for (Iterator iter=mps.getMediaPlayers().iterator();iter.hasNext();) {
				IMediaPlayer imp = (IMediaPlayer) iter.next();
	
				if (name.equalsIgnoreCase(imp.getName())) {
					return imp;
				}
			}
		} else {
			for (Iterator iter=mps.getMediaPlayers().iterator();iter.hasNext();) {
				IMediaPlayer imp = (IMediaPlayer) iter.next();
			
				System.out.println("testing "+imp.getName());
				if (imp.isAvailableMediaPlayer()) {
					//System.o
					if ("default".equalsIgnoreCase(name)) {
						return imp;
					}
					try {
						imp.init();
						if (imp.isRunning()) {
							if ("running".equalsIgnoreCase(name)) 
								return imp;
							if (imp.isPlaying()) {
								if ("playing".equalsIgnoreCase(name)) {
									return imp;
								}
								
							}
						}
					} finally {
						imp.destroy();
					}
				}
			}
		}
		return null;
	}

}
