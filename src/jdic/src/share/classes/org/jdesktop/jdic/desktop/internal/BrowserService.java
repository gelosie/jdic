/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
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
 
package org.jdesktop.jdic.desktop.internal;

import java.net.URL;


/**
 * The <code>BrowserService</code> interface provides interaction with 
 * the system default browser.
 */
public interface BrowserService {
    /**
     * Launches the system default browser to show the given URL.
     * 
     * @param url the given URL.
     * @throws LaunchFailedException if the system browser is found but fails to be launched.
     */
    public void show(URL url) throws LaunchFailedException;
  
    /**
     * Opens the given URL in the target window of the system default browser.
     * 
     * @param url the given URL.
     * @param target the given target browser window name. 
     * @throws LaunchFailedException if the system browser is found but fails to be launched.
     */
    public void show(URL url, String target) throws LaunchFailedException;
}
