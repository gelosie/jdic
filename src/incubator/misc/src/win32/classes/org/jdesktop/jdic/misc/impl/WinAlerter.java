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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;
import org.jdesktop.jdic.misc.Alerter;

/**
 * Windows Alerter
 * 
 * @author Fábio Castilho Martins (fcmartins@bol.com.br)
 * @created April 24, 2005
 */
public class WinAlerter extends Alerter {

	private boolean isLoaded;
	
	private static Timer loop;
	
	private class AlerterListener implements ActionListener {
		
		private Frame internalFrame;
		
		private AlerterListener(Frame frame) {
			this.internalFrame = frame;
		}
		
		public void actionPerformed(ActionEvent e) {
			if(!internalFrame.isActive()) {
				alertWindows(internalFrame);
			}
			else {
				loop.removeActionListener(this);
			}
			
			//Stop the loop when there are no frames to listen, saving resources
			if(loop.getActionListeners().length == 0 && loop.isRunning()) {
				loop.stop();
			}
		}		
	}

	/**
	 * Do not use. Call the Alerter.newInstance() factory method instead.
	 * @throws SecurityException
	 * @throws UnsatisfiedLinkError
	 */
	public WinAlerter() throws SecurityException, UnsatisfiedLinkError {
		if (isLoaded == false) {
			System.loadLibrary("jdic_misc");
			isLoaded = true;
		}
	}
	
	/**
	 * @param It will blink the TaskBar button and the Program Window at a 
	 * regular rate (controlled by the user's preferences), until the Window 
	 * gets the focus.
	 */
	public void alert(Frame frame) {
		if(!frame.isActive()) {
			if(loop != null) {
				ActionListener[] listeners = loop.getActionListeners();
				AlerterListener listener;
				//Make sure we don't put the same frame more than 1 time in loop
				for(int i = 0; i < listeners.length; i++) {
					if (listeners[i] instanceof AlerterListener) {
						listener = (AlerterListener) listeners[i];
						if(listener.internalFrame.equals(frame)) {
							return;
						}
					}
				}
			
				loop.addActionListener(new AlerterListener(frame));
				if(!loop.isRunning()) {
					loop.start();
				}			
			}
			else {
				int delay = (int) getBlinkRate(); //milliseconds
				loop = new Timer(delay, new AlerterListener(frame));
				loop.start();
			}
		}
	}
	
	private native void alertWindows(Frame frame);
	
	/**
	 * Return the System's Blink Rate, which is a user preference, that controls the blink rate on Windows. 
	 * Microsoft's Design Guidelines states that an application should respect this user configuration.
	 * @return The Native Blink Rate.
	 */
	private native long getBlinkRate();
	
	
	public boolean isAlertSupported() {
		return true;
	}

}
