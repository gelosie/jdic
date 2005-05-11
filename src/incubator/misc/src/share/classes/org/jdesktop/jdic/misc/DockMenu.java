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

import javax.swing.JMenu;

/**
 *  <p>Displays a JMenu as a DockMenu, meaning the menu you get when you
 *  right click, control click, or press and hold on the application's
 *  Dock icon. The DockMenu is currently only supported on Mac OS X.
 *  <b>Note: the action listeners on the dock menu will not be called
 *  from the Swing event thread. If you want to manipulate Swing
 *  objects from your event handlers you must do so using
 *  <i>SwingUtilities.invokeLater().</i></b></p>
 *
 
 <p><b>Example:</b> To install a JMenu as the Dock menu:</p>
 
 <pre><code>
	JMenu dock_menu = new JMenu("Dock");
	dock_menu.add(new JMenuItem("item 1"));
	dock_menu.add(new JMenuItem("item 2"));
	DockMenu dm = DockMenu.newInstance();
	dm.setMenu(dock_menu);
 </code></pre>
 
 <p>Note that the {@link DockMenu#newInstance() newInstance()}method <i><b>must be called after at least one Swing/AWT window or frame has already been shown</b></i>, or else the AWT and Cocoa event threads may clash and cause your application to lock up.
 </p>
 
 
 * @author     Joshua Marinacci <a href="mailto:joshua@marinacci.org">joshua@marinacci.org</a>
 * @created    April 8, 2005
 */

public class DockMenu {

	private static DockMenu _dock_menu;


	/**
	 *  Constructor for the DockMenu object
	 */
	protected DockMenu() { }


	/**
	 *  Set the JMenu to be used as the Dock menu. Changes to the items
	 *  within this menu will be automatically reflected in the dock
	 *  menu. Sub menus are not currently supported.
	 *
	 * @param  menu  The new menu value
	 */
	public void setMenu(JMenu menu) {
		System.out.println("the dock menu is only supported on Mac OS X");
	}


	/**
	 *  Get a new instance of a DockMenu for the appropriate platform. If
	 *  an implementation is not available (only OSX is currently
	 *  supported) then it will return a dummy implemenation that does
	 *  nothing.
	 *
	 * @return    DockMenu for the current platform.
	 */
	public static DockMenu newInstance() {
		if (_dock_menu == null) {
			String os_name  = System.getProperty("os.name");
			System.out.println("os name = " + os_name);
			if (os_name.toLowerCase().startsWith("mac os x")) {
				loadMac();
			} else {
				_dock_menu = new DockMenu();
			}
		}
		return _dock_menu;
	}


	/**
	 *  implementation detail
	 */
	private static void loadMac() {
		System.out.println("creating mac dock menu");
		try {
			_dock_menu = (DockMenu) DockMenu.class.forName(
					"org.jdesktop.jdic.misc.impl.MacOSXDockMenu")
					.newInstance();
		} catch (Throwable ex) {
			System.out.println("couldn't load the mac version");
			System.out.println("" + ex.getMessage());
			ex.printStackTrace();
			_dock_menu = new DockMenu();
		}
	}

	/** Returns true if the current platform supports Dock Menus */
	public boolean isDockMenuSupported() {
		return false;
	}
}

