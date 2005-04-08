package org.jdesktop.jdic.misc.impl;

import org.jdesktop.jdic.misc.*;

import com.apple.component.*;
import com.apple.audio.units.*;
import com.apple.audio.*;
import com.apple.audio.hardware.*;
import com.apple.audio.util.*;

import java.beans.*;

public class MacOSXVolume extends Volume {

/* public interface */
	public float getVolume() {
		try {
			CAMemoryObject propVal = new CAMemoryObject(4,true);
			dev.getOutputProperty(1, AHConstants.kAudioDevicePropertyVolumeScalar, propVal);
			return propVal.getFloatAt(0);
		} catch (Exception ex) {
			System.out.println("unable to get the volume");
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			return 0f;
		}
	}
	
	public void setVolume(float newVolume) {
		try {
			CAMemoryObject propVal = new CAMemoryObject(4,true);
			propVal.setFloatAt(0,newVolume);
			dev.setOutputProperty(new AudioTimeStamp(), 1, 
				AHConstants.kAudioDevicePropertyVolumeScalar, propVal);
		} catch (Exception ex) {
			System.out.println("unable to set the volume");
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}
	}
	

	private AudioDevice dev;
	
	public MacOSXVolume() {
		try {
			dev = AudioHardware.getDefaultOutputDevice();
			dev.addOutputPropertyListener(1,
				AHConstants.kAudioDevicePropertyVolumeScalar,
				new ADevicePropertyListener() {
					public int execute(AudioDevice dev, int chan, 
						boolean input, int prop) {
						if(prop == AHConstants.kAudioDevicePropertyVolumeScalar) {
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
	
	
	
	protected void updateVolume() {
		float val = getVolume();
		PropertyChangeEvent pce = new PropertyChangeEvent(this,"volume", new Float(prev_val), new Float(val));
		fireChangeEvent(pce);
		prev_val = getVolume();
	}
	
	protected float prev_val = -1;
	
}
	

