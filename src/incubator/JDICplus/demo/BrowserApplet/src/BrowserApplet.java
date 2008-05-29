/*
 * Copyright (C) 2008 Sun Microsystems, Inc. All rights reserved. Use is
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


import java.awt.Panel;
import javax.swing.SwingUtilities;
import  org.jdic.web.BrComponent;
import  org.jdic.web.BrTabbed;

/**
 * Applet example
 * @author uta
 */

public class BrowserApplet extends javax.swing.JApplet{
    public BrowserApplet() {}
    
    BrTabbed brMain;
            
    @Override
    public void init() {
        BrComponent.DESIGN_MODE = false;
        SwingUtilities.invokeLater(new Runnable() { public void run() {
            setLayout(new java.awt.BorderLayout());
            //Panel my = new Panel(new java.awt.BorderLayout());
            brMain = new BrTabbed();
            brMain.setURL("http://java.com");
            //my.add(brMain, java.awt.BorderLayout.CENTER);
            add(brMain, java.awt.BorderLayout.CENTER);
            //add(my, java.awt.BorderLayout.CENTER);
        }});
    }

    @Override
    public void destroy() {
        remove(brMain);
        super.destroy();
    }
}