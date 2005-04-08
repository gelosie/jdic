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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import org.jdesktop.jdic.misc.Volume;

/**
 *  Description of the Class
 *
 * @author     joshua@marinacci.org
 * @created    April 8, 2005
 */
public class VolumeTest {

	/**
	 *  The main program for the VolumeTest class
	 *
	 * @param  args  The command line arguments
	 */
	public static void main(String[] args) {
		final Volume vol            = Volume.newInstance();

		JButton down                = new JButton("<");
		down.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (vol.getVolume() > 0f) {
						vol.setVolume(vol.getVolume() - 0.1f);
					}
				}
			});

		JButton up                  = new JButton(">");
		up.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (vol.getVolume() < 1f) {
						vol.setVolume(vol.getVolume() + 0.1f);
					}
				}
			});

		final JLabel cur_vol        = new JLabel("?");

		PropertyChangeListener pcl  =
			new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					System.out.println("evt = " + evt);
					final Float vol  = (Float) evt.getNewValue();
					SwingUtilities.invokeLater(
						new Runnable() {
							public void run() {
								cur_vol.setText("" + vol);
							}
						});
				}
			};

		vol.addPropertyListener(pcl);

		JFrame frame                = new JFrame("Volume Test");
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(down);
		frame.getContentPane().add(cur_vol);
		frame.getContentPane().add(up);
		frame.pack();
		frame.setSize(400, 200);
		frame.show();
	}
}

