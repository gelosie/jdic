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

import org.jdesktop.jdic.dock.FloatingDock;
import java.awt.*;

class DockDemo {

    public static void main(String[] args) 
    {
	Button button1, button2, button3;
	
	FloatingDock fd = new FloatingDock();
	fd.setVisible (false);
	GridLayout gl1 = new GridLayout(3, 1);
	GridLayout gl2 = new GridLayout(1, 3);

	button1 = new Button("Button 1");
	button1.setVisible(true);
	button2 = new Button("Button 2");
        button2.setVisible(true);
	button3 = new Button("Button 3");
        button3.setVisible(true);

	fd.setLocation (FloatingDock.LEFT);
	fd.setLayout(gl1);
	fd.add(button1);
	fd.add(button2);
	fd.add(button3);
	fd.setVisible(true);
	try {
	    Thread.sleep (5000);
	} catch (Exception e) {}
	
	fd.setVisible(false);
	fd.remove(button1);
	fd.remove(button2);
	fd.remove(button3);
	fd.setLocation (FloatingDock.TOP);
        fd.setLayout(gl2);
        fd.add(button1);
        fd.add(button2);
        fd.add(button3);
	fd.setVisible(true);
        try {
            Thread.sleep (5000);
        } catch (Exception e) {}

	fd.setVisible(false);
	fd.remove(button1);
        fd.remove(button2);
        fd.remove(button3);
	fd.setLocation (FloatingDock.RIGHT);
        fd.setLayout(gl1);
        fd.add(button1);
        fd.add(button2);
        fd.add(button3);
	fd.setVisible(true);
        try {
            Thread.sleep (5000);
        } catch (Exception e) {}

	fd.setVisible(false);
	fd.remove(button1);
        fd.remove(button2);
        fd.remove(button3);
	fd.setLocation (FloatingDock.BOTTOM);
        fd.setLayout(gl2);
        fd.add(button1);
        fd.add(button2);
        fd.add(button3);
	fd.setVisible(true);
        try {
            Thread.sleep (5000);
        } catch (Exception e) {}
    }
}
