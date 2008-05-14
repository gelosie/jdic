/*
 * Copyright (C) 2008 Sun Microsystems, Inc. All rights reserved. Use is
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

package  org.jdic.web.event;

/**
 * The listener interface for receiving browser events.
 * The class that is interested in processing a browser event
 * implements this interface.
 * 
 * The listener object created from that class is then registered with a
 * browser using the BrComponent's <code>addBrComponentListener</code>
 * method. 
 *
 * @author uta
 */
public interface BrComponentListener {
    /**
     * Invoked when the browser wants to notify.
     * @param e happened event
     * @return the processing result
     */
    public String sync(BrComponentEvent e);
}
