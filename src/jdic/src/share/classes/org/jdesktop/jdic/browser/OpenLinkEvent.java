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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventObject;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

/**
 * The event object that encapuslates the details of the event of a link being
 * clicked in an {@link IWebBrowser}.
 * 
 * @author Felix Ferver
 */
public class OpenLinkEvent extends EventObject {
	
	/**
	 * The id for a link being opened in the same window.
	 */
	public final static int SAME_WINDOW_EVENT = 1;
	/**
	 * The id for a link being openend in a new window.
	 */
	public final static int NEW_WINDOW_EVENT = 2;
	/**
	 * The value of the link, can be null.
	 */
	private final String link;
	/**
	 * The id of the event.
	 */
	private final int id;
	/**
	 * The url of the link being clicked.
	 */
	private final URL url;
	
	/**
	 * Constructs a new event object with the event source, the id and the link.
	 */
	public OpenLinkEvent(IWebBrowser source, int id, String link) {
		super(source);
		this.id = id;
		this.link = link;
		this.url = link != null ? createURL(link) : null;
	}

	/**
	 * @return the browser that issues the event
	 */
	public IWebBrowser getBrowser() {
		return (IWebBrowser)getSource();
	}
	
	/**
	 * The id of the event.
	 * @return
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * @return can return null
	 */
	public String getLink() { 
		return link;
	}
	
	/**
	 * @return can return null if a valid url could not be constructed.
	 */
	public URL getURL() {
		return url;
	}

	/**
	 * Creates a url if possible from <code>urlString</code>, otherwise 
	 * returns null.
	 * If the url is not valid prepends file protocol and tries again.
	 */
	private URL createURL(String urlString) {
		try {
			return new URL(urlString);
		} 
		catch (MalformedURLException ex1) {
			try {
				return new URL(BrowserEngineManager.instance()
						.getActiveEngine().getFileProtocolURLPrefix()
						+ urlString);
			}
			catch (MalformedURLException e) {
				WebBrowserUtil.trace(e.toString());
			}
		}
		return null;
	}
}
