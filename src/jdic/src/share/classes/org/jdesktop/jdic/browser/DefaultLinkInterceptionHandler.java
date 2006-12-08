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

package org.jdesktop.jdic.browser;

import java.net.URL;
import java.security.AccessControlException;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

/**
 * A default implementation that checks if a url can be connected to if
 * the event has a valid url, otherwise it just returns true.
 * 
 * @author Felix Ferber
 */
public class DefaultLinkInterceptionHandler implements ILinkInterceptionHandler {

	public boolean shouldOpenLink(OpenLinkEvent event) {
		// mimics former default behaviour
		URL url = event.getURL();
		switch (event.getID()) {
		case OpenLinkEvent.SAME_WINDOW_EVENT:
			if (url == null) {
				return true;
			}
			WebBrowserUtil.trace("URL = " + url.toString());
			SecurityManager security = System.getSecurityManager();
			if (security != null) {
				try {
					security.checkConnect(url.getHost(), url.getPort());
				} catch (AccessControlException e) {
					return false;
				}
			}
			return true;
		case OpenLinkEvent.NEW_WINDOW_EVENT:
			if (url != null) {
				WebBrowserUtil.trace("willOpenWindow " + url.toString());
			}
			return true;
		default:
			throw new IllegalStateException("Unhandled window state");
		}
	}
}
