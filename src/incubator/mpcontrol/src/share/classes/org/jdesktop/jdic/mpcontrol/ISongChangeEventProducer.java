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


/**
 *
 * If a IMediaPlayer implements this interface, then it can notify the registered listeners to song change events.
 * There is no strict guarentee, that the notification will be prompt. If there isn't a built in native functionality, 
 * then IMediaPlayer can implement a <i>polling</i> behaviour, to simulate.
 *
 * @see org.jdesktop.jdic.mpcontrol.IMediaPlayer
 * @see org.jdesktop.jdic.mpcontrol.ISongChangeListener
 *
 * @author Zsombor Gegesy
 */
public interface ISongChangeEventProducer {


    /**
     * add a new listener which consumes song change events.
     */
    public void addListener(ISongChangeListener listener);
	
    /**
     * remove a listener which consumes song change events.
     */
    public void removeListener(ISongChangeListener listener);

    /**
     * initiate the listening for the song change events.
     */	
    public void startListening();
	
    /**
     * stop listening. This will release any native resource which created/acquired when the listening initiated.
     * 
     */
    public void stopListening();
}
