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
 * An abstract adapter class for receiving <code>WebBrowser</code> events. 
 * The methods in this class are empty. This class exists as convenience for 
 * creating listener objects.
 * <p>
 * Extend this class to create a <code>WebBrowserEvent</code> listener and 
 * override the methods for the events of interest. (If you implement the 
 * <code>WebBrowserListener</code> interface, you have to define all of the 
 * methods in it. This abstract class defines null methods for them all, 
 * so you can only have to define methods for events you care about.)
 * <p>
 * Create a listener object using the extended class and then register it with 
 * a component using the component's addWebBrowserListener method. When a <code>
 * WebBrowserEvent</code> is fired, the relevant method in the listener object 
 * is invoked, and the <code>WebBrowserEvent</code> is passed to it.
 * 
 * @see WebBrowserEvent
 * @see WebBrowserListener
 */
public abstract class WebBrowserAdapter implements WebBrowserListener {
    /**
     * Invoked when a download operation is beginning.
     *
     * @param event the WebBrowserEvent fired.
     */
    public void downloadStarted(WebBrowserEvent event) {}

    /**
     * Invoked when a navigation operation is completed, aborted or failed.
     *
     * @param event the WebBrowserEvent fired.
     */
    public void downloadCompleted(WebBrowserEvent event) {}

    /**
     * Invoked when the progress of a download operation is updated.
     *
     * @param event the WebBrowserEvent fired.
     */
    public void downloadProgressChanged(WebBrowserEvent event) {}

    /**
     * Invoked when an error occurs during a download operation.
     * 
     * @param event the WebBrowserEvent fired.
     */
    public void downloadError(WebBrowserEvent event) {}

    /**
     * Invoked when the title of a document is changed.
     * 
     * @param event the WebBrowserEvent fired.
     */
    public void titleChanged(WebBrowserEvent event) {}

    /**
     * Invoked when the status bar text is changed.
     * 
     * @param event the WebBrowserEvent fired.
     */
    public void statusTextChanged(WebBrowserEvent event) {}
}
