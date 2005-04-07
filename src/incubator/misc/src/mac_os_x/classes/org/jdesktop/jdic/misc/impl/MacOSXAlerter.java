package org.jdesktop.jdic.misc.impl;

import com.apple.cocoa.application.*;
import com.apple.cocoa.foundation.*;
import com.apple.eawt.*;

import org.jdesktop.jdic.misc.Alerter;

public class MacOSXAlerter extends Alerter {
	public MacOSXAlerter() {
	}
	
	public void alert() {
		NSApplication app = NSApplication.sharedApplication();
		int id = app.requestUserAttention(
			NSApplication.UserAttentionRequestCritical);
		System.out.println("id = " + id);
	}
}
