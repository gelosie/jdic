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

package org.jdesktop.jdic.desktop.internal.impl;

import java.net.URL;

import org.jdesktop.jdic.desktop.internal.BrowserService;
import org.jdesktop.jdic.desktop.internal.LaunchFailedException;

/**
 * Concrete implementation of the BrowserService interface for Mac OS X.
 *
 * @author Elliott Hughes <enh@acm.org>
 */
public class MacBrowserService implements BrowserService {
    static {
        System.loadLibrary("jdic");
    }

    /**
     * Invokes the system default browser to display the given URL.
     *
     * @param url the given URL. 
     * @throws LaunchFailedException if the default browser is not found, or 
     *         the default browser fails to be launched.  
     */
    public void show(URL url) throws LaunchFailedException {
        if (!nativeBrowseURL(url.toString())) {
            throw new LaunchFailedException(
                "Failed to launch the default browser.");
        }
    }

    public void show(URL url, String target) throws LaunchFailedException {
        show(url);
    }

    private native boolean nativeBrowseURL(String urlStr);
}
