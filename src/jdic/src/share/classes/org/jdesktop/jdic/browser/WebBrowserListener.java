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
 * The listener interface for receiving WebBrowser events. The class that is
 * interested in processing a WebBrowser event implements this interface or 
 * inherits the <code>WebBrowserAdapter</code> abstract class.
 * <p>
 * The object created with that class is registered with a <code>
 * WebBrowser</code> component, using the <code>addWebBrowserListener</code> 
 * method. When the WebBrowser event occurs, that object's corresponding method 
 * is invoked.
 *
 * @see WebBrowserEvent
 * @see WebBrowser 
 * 
 * @author Kyle Yuan
 * @version 0.1, 03/07/17
 */
public interface WebBrowserListener extends java.util.EventListener
{
	/**
     * Invoked when the initialization is completed.
     * 
     * @param event the WebBrowserEvent fired.
     */
    void initializationCompleted(WebBrowserEvent event);
	
    /**
     * Invoked when a download operation is beginning.
     *
     * @param event the WebBrowserEvent fired.
     */
    void downloadStarted(WebBrowserEvent event);

    /**
     * Invoked when a download operation finishes, is halted, or fails.
     *
     * @param event the WebBrowserEvent fired.
     */
    void downloadCompleted(WebBrowserEvent event);

    /**
     * Invoked when the progress of a download operation is updated.
     *
     * @param event the WebBrowserEvent fired.
     */
    void downloadProgress(WebBrowserEvent event);

    /**
     * Invoked when an error occurs during a download operation.
     * 
     * @param event the WebBrowserEvent fired.
     */
    void downloadError(WebBrowserEvent event);

    /**
     * Invoked when the document loading has been completed.
     *
     * @param event the WebBrowserEvent fired.
     * @since 0.9
     */
    void documentCompleted(WebBrowserEvent event);
    
    /**
     * Invoked when the title of a document is changed.
     * 
     * @param event the WebBrowserEvent fired.
     */
    void titleChange(WebBrowserEvent event);

    /**
     * Invoked when the status bar text is changed.
     * 
     * @param event the WebBrowserEvent fired.
     */
    void statusTextChange(WebBrowserEvent event);
}
