/*
 *  Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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

import java.awt.Frame;
import java.awt.event.WindowAdapter;

import org.jdesktop.jdic.misc.Alerter;

/**
 * Linux Alerter.
 * 
 * @author Fábio Castilho Martins (fcmartins@bol.com.br)
 * @created December 30, 2005
 */
public class LinuxAlerter extends Alerter {

	private boolean isLoaded;
	
	/**
	 * Do not use. Call the Alerter.newInstance() factory method instead.
	 * @throws SecurityException
	 * @throws UnsatisfiedLinkError
	 */
	public LinuxAlerter() throws SecurityException, UnsatisfiedLinkError {
		if (isLoaded == false) {
			System.loadLibrary("jdic_misc");
			isLoaded = true;
		}
	}

	/**
	 * @param It will blink the TaskBar button, until the Window 
	 * gets the focus.
	 */
	public void alert(Frame frame) {
		if(!frame.isActive()) {
			frame.addWindowListener(new WindowAdapter() {
				public void windowActivated(java.awt.event.WindowEvent e) {
					Frame frame = (Frame) e.getSource();
                    //stops sending alerts and removes the listener
					frame.removeWindowListener(this);
					setUrgencyHint(frame, false);					
				}
			});
			this.setUrgencyHint(frame, true);			
		}
	}
	
	private native void setUrgencyHint(Frame frame, boolean alert);

}
