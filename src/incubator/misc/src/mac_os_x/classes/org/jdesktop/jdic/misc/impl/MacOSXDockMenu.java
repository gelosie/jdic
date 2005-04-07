package org.jdesktop.jdic.misc.impl;

import org.jdesktop.jdic.misc.DockMenu;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import com.apple.eawt.*;
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class MacOSXDockMenu extends DockMenu {

	public MacOSXDockMenu() {
	}
	
	public void setMenu(JMenu menu) {
		Delegate delegate = new Delegate(menu);
		NSApplication app = NSApplication.sharedApplication();
		app.setDelegate(delegate);
	}
	protected void finalize() {
		NSApplication app = NSApplication.sharedApplication();
		//app.removeDelegate(delegate);
	}
	
	class Delegate extends ApplicationAdapter{
		JMenu menu;
		
		public Delegate(JMenu menu) {
			this.menu = menu;
		}
	
		private final NSSelector actionSel =
			new NSSelector("doClick", new Class[] {});
	
		public NSMenu applicationDockMenu(NSApplication sender) {
			System.out.println("creating dock menu");
			//NSMenuItem showCurrItem;
			
			NSMenu dockMenu = dockMenu = new NSMenu();
			
			for(int i=0; i<menu.getMenuComponentCount(); i++) {
				JMenuItem item = (JMenuItem)menu.getMenuComponent(i);
				NSMenuItem nsitem = new NSMenuItem(item.getText(),actionSel,"");
				nsitem.setTarget(item);
				dockMenu.addItem(nsitem);
			}
			return dockMenu;
		}
		
	}
}


