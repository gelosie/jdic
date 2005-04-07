package org.jdesktop.jdic.misc;

import javax.swing.JMenu;

public class DockMenu {
	
	private static DockMenu _dock_menu;
	
	protected DockMenu() {
	}
	
	public void setMenu(JMenu menu) {
		System.out.println("the dock menu is only supported on Mac OS X");
	}

	public static DockMenu newInstance() {
		if(_dock_menu == null) {
			String os_name = System.getProperty("os.name");
			System.out.println("os name = " + os_name);
			if(os_name.toLowerCase().startsWith("mac os x")) {
				loadMac();
			} else {
				_dock_menu = new DockMenu();
			}
		}
		return _dock_menu;
	}
	
	private static void loadMac() {
		System.out.println("creating mac dock menu");
		try {
		_dock_menu = (DockMenu) DockMenu.class.forName(
			"org.jdesktop.jdic.misc.impl.MacOSXDockMenu")
			.newInstance();
		} catch (Throwable ex) {
			System.out.println("couldn't load the mac version");
			System.out.println(""+ex.getMessage());
			ex.printStackTrace();
			_dock_menu = new DockMenu();
		}
	}
	
}

