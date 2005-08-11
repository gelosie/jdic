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

package org.jdesktop.jdic.misc.test;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.jdesktop.jdic.misc.DockMenu;

/**
 *  Description of the Class
 *
 * @author     joshua@marinacci.org
 * @created    April 8, 2005
 */
public class DockMenuTest {

	/**
	 *  The main program for the DockMenuTest class
	 *
	 * @param  args  The command line arguments
	 */
	public static void main(String[] args) {

		// set up the main menubar
		JMenuBar menubar  = new JMenuBar();
		JMenu menu        = new JMenu("Test");
		JMenuItem item    = new JMenuItem("Go right click on the dock icon!");
		menu.add(item);
		menubar.add(menu);

		// show the frame
		JFrame frame      = new JFrame("asdf");
		frame.setJMenuBar(menubar);
		frame.pack();
		frame.setSize(100, 100);
		frame.show();

		// create another menu for the dock
		JMenu dock_menu   = new JMenu("Dock");
		dock_menu.add(new JMenuItem("item 1"));
		dock_menu.add(new JMenuItem("item 2"));
		JMenuItem ditem   = new JMenuItem("item 3 (with action)");
		ditem.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					System.out.println("in the action");
					SwingUtilities.invokeLater(
						new Runnable() {
							public void run() {
								JOptionPane.showMessageDialog(null, "The dock action worked!");
							}
						});
				}
			});
		dock_menu.add(ditem);
		
		dock_menu.add(new JMenuItem("Separator Next"));
		dock_menu.add(new JSeparator());
		JMenuItem sub_menu = new JMenu("Sub Menu");
		sub_menu.add(new JMenuItem("Item 1"));
		sub_menu.add(new JMenuItem("Item 2"));
		dock_menu.add(sub_menu);
		

		// set the dock menu. you must do this after
		// after the first frame is created so that we are in AWT mode
		DockMenu dm       = DockMenu.newInstance();
		dm.setMenu(dock_menu);
	}
}

