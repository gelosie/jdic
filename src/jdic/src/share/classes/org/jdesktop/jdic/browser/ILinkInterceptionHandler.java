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

/**
 * Defines the requirements for a link interception handler that is queried
 * by an {@link IWebBrowser} whether the clicked link should be opened.
 * 
 * @author Felix Ferber
 */
public interface ILinkInterceptionHandler {

	/**
	 * A request to open a link has been made, <code>even</code> contains
	 * the details about the event.
	 * @param event
	 * @return whether the link sould be opened or not
	 */
	boolean shouldOpenLink(OpenLinkEvent event);
}
