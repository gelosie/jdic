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

import java.awt.Component;
import java.net.URL;

/**
 * A <code>IWebBrowser</code> component represents a blank rectangular area of
 * the screen onto which the application can display webpages or from which the
 * application can trap events from the browser window. In order to show a
 * Browser component in GUI, user need to add Browser to a top-level container,
 * such as <code>Frame</code>.
 * <p>
 * The class that is interested in processing a Browser event should implement
 * interface <code>WebBrowserListener</code>, and the object created with
 * that class should use Browser's <code>addWebBrowserListener</code> method
 * to register as a listener.
 * <p>
 * 
 * @see IBrowserEngine
 * @see WebBrowserEvent
 * @see WebBrowserListener
 * 
 * @author Alexander Hars, Inventivo GmbH
 * @created 2005-01-20
 */
public interface IWebBrowser {

	/**
	 * Adds a <code>WebBrowserEvent</code> listener.
	 * 
	 * @param listener
	 *            object which implements WebBrowserListener interface.
	 */
	public void addWebBrowserListener(WebBrowserListener listener);

	/**
	 * Removes a <code>WebBrowserEvent</code> listener.
	 * 
	 * @param listener
	 *            object which implements WebBrowserListener interface. If the
	 *            listener was not in the listeners list, then no listener will
	 *            be removed.
	 */
	public void removeWebBrowserListener(WebBrowserListener listener);

	/**
	 * If the webbrowser works under synchronize model
	 * 
	 * @return
	 */
	public boolean isSynchronize();

	/**
	 * Returns the component to which the Browser paints. This method is needed
	 * to add a Browser to
	 * 
	 * @return The component on which the Browser paints.
	 */
	public Component asComponent();

	/**
	 * Retrieves the URL that is currently being displayed.
	 * 
	 * @return the current URL being display, or <code>null</code> if the
	 *         WebBrowser object is not ready with initialization of itself.
	 */
	public URL getURL();

	/**
	 * Sets the document to be a blank page.
	 *  
	 */
	public void setURL();

	/**
	 * Navigates to a resource identified by an URL using HTTP GET method.
	 * 
	 * @param url
	 *            the URL to navigate.
	 */
	public void setURL(URL url);

	/**
	 * Navigates to a resource identified by an URL using HTTP POST method.
	 * 
	 * @param url
	 *            the URL to navigate.
	 * @param postData
	 *            Data to send to the server during the HTTP POST transaction.
	 */
	public void setURL(URL url, String postData);

	/**
	 * Returns the HTML content of a document, opened in a browser.
	 * 
	 * @return the HTML content of a document, opened in a browser.
	 */
	public String getContent();

	/**
	 * Sets new HTML content.
	 * 
	 * @param htmlContent
	 *            the HTML content to set.
	 */
	public void setContent(String htmlContent);

	/**
	 * Executes specified JavaScript code in a currently opened document. This
	 * should not be called until after a documentComplete event is fired in
	 * <code>WebBrowserListener</code>.
	 * 
	 * @return the result of JavaScript execution, if there is any.
	 */
	public String executeScript(java.lang.String javaScript);

	/**
	 * Navigates to the previous session history item.
	 */
	public void back();

	/**
	 * Navigates to the next session history item.
	 */
	public void forward();

	/**
	 * Reloads the URL that is currently being displayed in the WebBrowser
	 * component.
	 */
	public void refresh();

	/**
	 * Stops loading of the current URL.
	 */
	public void stop();

	/**
	 * The BrowserEngine that was responsible for creating the browser instance.
	 * Use the BrowserEngine to get information about the Embedded Browser
	 * Component that is used for browsing.
	 * 
	 * @return The <code>BrowserEngine</code> that was responsible for
	 *         creating this browser instance.
	 */
	public IBrowserEngine getBrowserEngine();

	public boolean isBackEnabled();

	public boolean isForwardEnabled();

	public boolean isInitialized();

	/**
	 * A IWebBrowser must have a ticket to identify itself, here is the instance
	 * num.For a IWebBrowser instance, a native browser instance will be created
	 * to deal with its requests, this instance num will be used get according
	 * native browser instance.
	 * 
	 * @return
	 */
	public int getInstanceNum();

	/**
	 * Get the windows' handler of native window
	 * 
	 * @return
	 */
	public int getNativeWindow();

	/**
	 * 
	 * @param event
	 */
	public void dispatchWebBrowserEvent(WebBrowserEvent event);

	public void setInitFailureMessage(String msg);

	public String getInitFailureMessage();

	/**
	 * Set if the IWebBrowser has been initialized.
	 * 
	 * @param b
	 */
	public void setInitialized(boolean b);
}