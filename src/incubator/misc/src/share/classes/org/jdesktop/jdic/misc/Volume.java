package org.jdesktop.jdic.misc;

import java.util.List;
import java.util.*;
import java.beans.*;


public class Volume {

	/* public interface */
	public float getVolume() {
		return -1f;
	}

	public void setVolume(float vol) {
	}
	
	public void addPropertyListener(PropertyChangeListener listener) {
		lists.add(listener);
	}

	public static Volume newInstance() {
		if(_volume == null) {
			String os_name = System.getProperty("os.name");
			System.out.println("os name = " + os_name);
			if(os_name.toLowerCase().startsWith("mac os x")) {
				loadMac();
			} else {
				_volume = new Volume();
			}
		}
		return _volume;
	}
	
	private static void loadMac() {
		System.out.println("creating mac dock menu");
		try {
		_volume = (Volume) Volume.class.forName(
			"org.jdesktop.jdic.misc.impl.MacOSXVolume")
			.newInstance();
		} catch (Throwable ex) {
			System.out.println("couldn't load the mac version");
			System.out.println(""+ex.getMessage());
			ex.printStackTrace();
			_volume = new Volume();
		}
	}
	
	private static Volume _volume;


	/* private and protected details */
	private List lists;
	protected Volume() {
		lists = new ArrayList();
	}


	protected void fireChangeEvent(PropertyChangeEvent pce) {
		Iterator it = lists.iterator();
		while(it.hasNext()) {
			PropertyChangeListener pcl = (PropertyChangeListener) it.next();
			pcl.propertyChange(pce);
		}
	}

}
