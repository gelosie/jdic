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
package org.jdesktop.jdic.misc.impl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import com.apple.eawt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.AbstractAction;
import javax.swing.Action;

import org.jdesktop.jdic.misc.DockMenu;

/**
 *  The Mac OS X implementation of the Dock Menu. This is most likely
 *  the only implementation, as no other platform as the concept of a
 *  dock menu. In the future this may change.
 *
 * @author     joshua@marinacci.org
 * @created    April 8, 2005
 */
public class MacOSXDockMenu extends DockMenu {

	/**
	 *  Do not call. Use the DockMenu.newInstance() factory method
	 *  instead.
	 */
	public MacOSXDockMenu() { }


	/**
	 *  Set the JMenu you want exported to the dock menu. You can change
	 *  the menu items later and the changes will be reflected in this
	 *  dock menu.
	 *
	 * @param  menu  The JMenu to show on the dock.
	 */
	public void setMenu(JMenu menu) {
		Delegate delegate  = new Delegate(menu);
		NSApplication app  = NSApplication.sharedApplication();
		app.setDelegate(delegate);
	}

	/**
	 *  Implementation detail. Do not use!
	 *
	 * @author     joshua@marinacci.org
	 * @created    April 8, 2005
	 */
	class Delegate extends ApplicationAdapter {
		JMenu menu;


		/**
		 *  Constructor for the Delegate object
		 *
		 * @param  menu  Description of the Parameter
		 */
		public Delegate(JMenu menu) {
			this.menu = menu;
		}


		private final NSSelector actionSel =
				new NSSelector("doClick", new Class[]{});


		/**
		 *  Description of the Method
		 *
		 * @param  sender  Description of the Parameter
		 * @return         Description of the Return Value
		 */
		public NSMenu applicationDockMenu(NSApplication sender) {
			System.out.println("creating dock menu");
			//NSMenuItem showCurrItem;

			//NSMenu dockMenu  = dockMenu = new NSMenu();
            NSMenu dockMenu = new NSMenu();
            addSubmenu(menu, dockMenu);
			
			/*
			for (int i = 0; i < menu.getMenuComponentCount(); i++) {
				JMenuItem item     = (JMenuItem) menu.getMenuComponent(i);
				NSMenuItem nsitem  = new NSMenuItem(item.getText(), actionSel, "");
				nsitem.setTarget(item);
				dockMenu.addItem(nsitem);
			}
			*/
			return dockMenu;
		}
		
		private void addSubmenu(JMenu m, NSMenu submen) {
			for (int i = 0; i<m.getItemCount(); i++) {
				if (m.getMenuComponent(i) instanceof JSeparator) {
					NSMenuItem nsitem = new NSMenuItem();
					nsitem = nsitem.separatorItem();
					submen.addItem(nsitem);
				} else {
					JMenuItem mi = (JMenuItem) m.getMenuComponent(i);
					NSMenuItem nsitem  = new NSMenuItem(mi.getText(), actionSel, "");
					nsitem.setTarget(mi);
					submen.addItem(nsitem);
				
					if (mi instanceof JMenu) {
						NSMenu submenu = new NSMenu(mi.getText());
						submen.setSubmenuForItem(submenu, nsitem);
						addSubmenu((JMenu)mi, submenu);
					}
				}
			}
		}


	}
	
}


