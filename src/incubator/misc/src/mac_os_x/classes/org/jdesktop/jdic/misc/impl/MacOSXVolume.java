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
import com.apple.audio.*;
import com.apple.audio.hardware.*;
import com.apple.audio.units.*;
import com.apple.audio.util.*;

import com.apple.component.*;

import java.beans.*;

import org.jdesktop.jdic.misc.*;

/**
 *  Description of the Class
 *
 * @author     joshua@marinacci.org
 * @created    April 8, 2005
 */
public class MacOSXVolume extends Volume {

	/*
	 *  public interface
	 */
	/**
	 *  Gets the volume attribute of the MacOSXVolume object
	 *
	 * @return    The volume value
	 */
	public float getVolume() {
		try {
			CAMemoryObject propVal  = new CAMemoryObject(4, true);
			dev.getOutputProperty(1, AHConstants.kAudioDevicePropertyVolumeScalar, propVal);
			return propVal.getFloatAt(0);
		} catch (Exception ex) {
			System.out.println("unable to get the volume");
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return 0f;
		}
	}


	/**
	 *  Sets the volume attribute of the MacOSXVolume object
	 *
	 * @param  newVolume  The new volume value
	 */
	public void setVolume(float newVolume) {
		try {
			CAMemoryObject propVal  = new CAMemoryObject(4, true);
			propVal.setFloatAt(0, newVolume);
			dev.setOutputProperty(new AudioTimeStamp(), 1,
					AHConstants.kAudioDevicePropertyVolumeScalar, propVal);
		} catch (Exception ex) {
			System.out.println("unable to set the volume");
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}


	private AudioDevice dev;


	/**
	 *  Constructor for the MacOSXVolume object
	 */
	public MacOSXVolume() {
		try {
			dev = AudioHardware.getDefaultOutputDevice();
			dev.addOutputPropertyListener(1,
					AHConstants.kAudioDevicePropertyVolumeScalar,
				new ADevicePropertyListener() {
					public int execute(AudioDevice dev, int chan,
							boolean input, int prop) {
						if (prop == AHConstants.kAudioDevicePropertyVolumeScalar) {
							updateVolume();
						}
						return 0;
					}
				}
					);

		} catch (Exception ex) {
			System.out.println("unable to access the volume");
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
		prev_val = getVolume();
	}



	/**
	 *  Description of the Method
	 */
	protected void updateVolume() {
		float val                = getVolume();
		PropertyChangeEvent pce  = new PropertyChangeEvent(this, "volume", new Float(prev_val), new Float(val));
		fireChangeEvent(pce);
		prev_val = getVolume();
	}


	/**
	 *  Description of the Field
	 */
	protected float prev_val = -1;

}


