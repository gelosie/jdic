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
package org.jdesktop.jdic.misc;
import java.beans.*;
import java.util.*;

import java.util.List;

/**
 *  Volume allows the developer to get, set, and watch the system
 *  volume. Currently only Mac OS X is supported. To avoid polling you
 *  can watch the volume by adding a property change listener that looks for the 
 *  'volume' property.

 <p><b>Example:</b> how to get and set the current volume.</p>
 
 <pre><code>
	final Volume vol = Volume.newInstance();
	
	// get the volume
	float current_volume = vol.getVolume();
	
	// increase volume by 10%		
	if (vol.getVolume() < 1f) {
		vol.setVolume(vol.getVolume() + 0.1f);
	}
</code></pre>

<p><b>Example:</b> how to add a property listener to watch for volume changes</p>

<pre><code>
import java.beans.*;

....

	final Volume vol = Volume.newInstance();
	
	PropertyChangeListener pcl  =
		new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				Float old_vol = (Float)evt.getOldValue();
				Float new_vol = (Float)evt.getNewValue();
				System.out.println("new volume is: " + new_vol);
			}
		};

	vol.addPropertyListener(pcl);
</code></pre>


 *
 * @author     Joshua Marinacci <a href="mailto:joshua@marinacci.org">joshua@marinacci.org</a>
 * @created    April 8, 2005
 */
public class Volume {

	/*
	 *  public interface
	 */
	/**
	 *  Gets the current system volume as a float. The volume will range
	 *  from 0.0 to 1.0. A value of -1 indicates that the volume was not
	 *  available.
	 *
	 * @return    The volume value
	 */
	public float getVolume() {
		return -1f;
	}


	/**
	 *  Set the current system volume. The value must be in the range of
	 *  0.0 to 1.0.
	 *
	 * @param  vol  The new volume value
	 */
	public void setVolume(float vol) {
	}


	/**
	 *  Add a PropertyChangeListener. Volume will return property change
	 *  events for the 'volume' property.
	 *
	 * @param  listener  The feature to be added to the PropertyListener
	 *      attribute
	 */
	public void addPropertyListener(PropertyChangeListener listener) {
		lists.add(listener);
	}


	/**
	 *  Get the Volume implementation for the currently running platform.
	 *  If no implemenation is available a dummy Volume will be returned
	 *  which does nothing.
	 *
	 * @return    Description of the Return Value
	 */
	public static Volume newInstance() {
		if (_volume == null) {
			String os_name  = System.getProperty("os.name");
			System.out.println("os name = " + os_name);
			if (os_name.toLowerCase().startsWith("mac os x")) {
				loadMac();
			} else {
				_volume = new Volume();
			}
		}
		return _volume;
	}


	/**
	 *  Do not call. Implemenation detail. Do not call.
	 */
	private static void loadMac() {
		System.out.println("creating mac dock menu");
		try {
			_volume = (Volume) Volume.class.forName(
					"org.jdesktop.jdic.misc.impl.MacOSXVolume")
					.newInstance();
		} catch (Throwable ex) {
			System.out.println("couldn't load the mac version");
			System.out.println("" + ex.getMessage());
			ex.printStackTrace();
			_volume = new Volume();
		}
	}


	private static Volume _volume;

	/*
	 *  private and protected details
	 */
	private List lists;


	/**
	 *  Do not call. Implementation detail. Only for subclass use.
	 */
	protected Volume() {
		lists = new ArrayList();
	}


	/**
	 *  Do not call. Implementation detail. Only for subclass use.
	 *
	 * @param  pce  Description of the Parameter
	 */
	protected void fireChangeEvent(PropertyChangeEvent pce) {
		Iterator it  = lists.iterator();
		while (it.hasNext()) {
			PropertyChangeListener pcl  = (PropertyChangeListener) it.next();
			pcl.propertyChange(pce);
		}
	}

}

