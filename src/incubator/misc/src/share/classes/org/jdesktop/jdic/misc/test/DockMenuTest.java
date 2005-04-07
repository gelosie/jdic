package org.jdesktop.jdic.misc.test;

import org.jdesktop.jdic.misc.DockMenu;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class DockMenuTest {
	
	public static void main(String[] args) {
		
		// set up the main menubar
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("Test");
		JMenuItem item = new JMenuItem("Go right click on the dock icon!");		
		menu.add(item);
		menubar.add(menu);
		
		// show the frame
		JFrame frame = new JFrame("asdf");
		frame.setJMenuBar(menubar);
		frame.pack();
		frame.setSize(100,100);
		frame.show();

		
		// create another menu for the dock
		JMenu dock_menu = new JMenu("Dock");
		dock_menu.add(new JMenuItem("item 1"));
		dock_menu.add(new JMenuItem("item 2"));
		JMenuItem ditem = new JMenuItem("item 3 (with action)");
		ditem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				System.out.println("in the action");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JOptionPane.showMessageDialog(null,"The dock action worked!");
					}
				});
			}
		});
		dock_menu.add(ditem);
		
		// set the dock menu. you must do this after
		// after the first frame is created so that we are in AWT mode
		DockMenu dm = DockMenu.newInstance();
		dm.setMenu(dock_menu);
	}
}

