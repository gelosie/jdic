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

package org.jdesktop.jdic.mpcontrol.impl;


import java.util.ArrayList;
import java.util.List;

import org.jdesktop.jdic.mpcontrol.ISongChangeEventProducer;
import org.jdesktop.jdic.mpcontrol.ISongChangeListener;
import org.jdesktop.jdic.mpcontrol.ISongInfo;


public abstract class SongChangeAdapter implements ISongChangeEventProducer {
	
    boolean listening = false;
	
    List listeners = new ArrayList();
    Thread listenerThread = null;

    public void addListener(ISongChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListener(ISongChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public void startListening() {

        synchronized (this) {
            if (listenerThread == null) {

                listenerThread = new Thread(new Runnable() { 
                    public void run() {
						
                        while (listening) {
                            doListening();
                        }
						
                    }
                }, "song-change-listener");
                listenerThread.setDaemon(true);
            }
        }
    }

    public void stopListening() {
        listening = false;

    }

    public abstract void doListening();
	
    protected void fireEvents(ISongInfo songInfo) {
        ISongChangeListener[] objs; 

        synchronized (listeners) {
            objs = new ISongChangeListener[listeners.size()];
            objs = (ISongChangeListener[]) listeners.toArray(objs);
        }
        for (int i = 0; i < objs.length; i++) {
            objs[i].onChange(songInfo);
        }
    }
	
}
