/*
 *  Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 *  subject to license terms.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package org.jdesktop.jdic.misc;

/**
 *  <p>This class sends alerts to the user when the application is not in
 *  the foreground. On Mac OS X this will bounce the dock icon until
 *  the user selects the application. On Windows (not currently
 *  supported) this will flash the * taskbar icon until the user
 *  selects the application. There is no support for other platforms.
 *  If you would like to support another platform (say Gnome or KDE on
 *  Linux) then please join the JDIC mailing list at
 *  <a href="http://jdic.dev.java.net/">http://jdic.dev.java.net/</a></p>
 
 <p><b>Example:</b> To alert the user (only if the application is not in the 
 foreground) create an Alerter using the factory method and call alert:</p>
 
 <pre><code> 
	Alerter alerter = Alerter.newInstance();
	alerter.alert();
 </code></pre>
 *
 * @author     Joshua Marinacci <a href="mailto:joshua@marinacci.org">joshua@marinacci.org</a>
 * @created    April 8, 2005
 */
public class Alerter {
	/**
	 *  Implementation detail. Use factory method instead.
	 */
	protected Alerter() { }


	private static Alerter _alerter;


	/**
	 *  Factory Method: Return a new (probably shared) Alerter object.
	 *  This will create the appropriate implementation for the currently
	 *  running platform. If an implementation is not available (the
	 *  current platform is unsupported) then this method will return a
	 *  dummy object that does nothing. This lets the developer safely
	 *  call the methods without worrying about which platform the
	 *  application is on.
	 *
	 * @return    Alerter for the current platform, or dummy.
	 */
	public static Alerter newInstance() {
		if (_alerter == null) {
			String os_name  = System.getProperty("os.name");
			System.out.println("os name = " + os_name);
			if (os_name.toLowerCase().startsWith("mac os x")) {
				loadMac();
			} else if (os_name.toLowerCase().startsWith("windows")) {
				loadWin();
			} else {
				_alerter = new Alerter();
			}
		}
		return _alerter;
	}


	/**
	 *  loads the mac implementation via reflection
	 */
	private static void loadMac() {
		System.out.println("creating mac alerter");
		try {
			_alerter = (Alerter) Alerter.class.forName(
					"org.jdesktop.jdic.misc.impl.MacOSXAlerter")
					.newInstance();
		} catch (Throwable ex) {
			System.out.println("couldn't load the Mac OS X version");
			System.out.println("" + ex.getMessage());
			ex.printStackTrace();
			_alerter = new Alerter();
		}
	}
	
	/** laods the windows implementation 
	*/
	private static void loadWin() {
		System.out.println("about to start the win alerter imp");
		try {
			_alerter = (Alerter) Alerter.class.forName(
					"org.jdesktop.jdic.misc.impl.WinAlerter")
					.newInstance();
		} catch (Throwable ex) {
			System.out.println("couldn't load the Windows version of Alerter");
			System.out.println("" + ex.getMessage());
			ex.printStackTrace();
			_alerter = new Alerter();
		}
	}


	/**
	 *  Alert the user. On Mac OS X this will bounce the application's dock icon. On Windows this will flash the task bar icon (not supported yet). The alert will only show if the application is not in the foreground.  
	 */
	public void alert() {
		System.out.println("alerts are not supported on this platform");
	}
}

