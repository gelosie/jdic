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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


/**
 * This class is the entry point to the Media Player Control API.
 * The basic usage, is to call MediaPlayerService.getInstance(), 
 * and get the list of the available media players from the singleton. 
 * Please look at the demos for basic usage sample. 
 * @author zsombor.gegesy 
 *
 */
public class MediaPlayerService {

    static Logger log = Logger.getLogger("org.jdesktop.jdic.mpcontrol");
	
    private static MediaPlayerService instance;

	private static Map properties;
	
    private List mediaPlayers;
	
    private MediaPlayerService() throws IOException {
        mediaPlayers = new ArrayList();
        BufferedReader bf = new BufferedReader(
                new InputStreamReader(
                        this.getClass().getClassLoader().getResourceAsStream(
                                "mediaplayer.properties"),
                                "UTF-8"));
        String line;

        do {
            line = bf.readLine();
            if (line != null && !line.trim().startsWith("#")
                    && line.trim().length() > 0) {
                try {
                    Class cls = Class.forName(line.trim());
                    IMediaPlayer obj = (IMediaPlayer) cls.newInstance();
					
                    if (obj.isAvailableMediaPlayer()) {
                        log.info(
                                "media player '" + obj.getDescription()
                                + "' added");
                        mediaPlayers.add(obj);
                    } else {
                        log.info(
                                "media player '" + obj.getDescription()
                                + "' not supported on this platform");
						
                    }
					
                } catch (ClassNotFoundException e) {
                    log.info("class not found:" + line.trim());
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        } while (line != null);
		
    }
	
    /**
     * Returns the singleton service class
     * @return the singleton.
     * @throws IOException
     */
    public static MediaPlayerService getInstance() throws IOException {
        synchronized (MediaPlayerService.class) {
            if (instance == null) {
                instance = new MediaPlayerService();
            }
            
            return instance;
        }
    }
	
    /**
     * @return the list of the available media players on this platform.
     */
    public List getMediaPlayers() {
        return mediaPlayers;
    }

    /**
     * various implementation related properties, this is where the implementation should store/retrieve their configuration.
     * @return
     */
    public static synchronized Map getProperties() {
    	if (properties==null)
    		properties = new HashMap();
    	return properties;
    }
}
