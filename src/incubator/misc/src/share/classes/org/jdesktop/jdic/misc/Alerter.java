package org.jdesktop.jdic.misc;

public class Alerter {
	protected Alerter() {
	}
	
	private static Alerter _alerter;
	public static Alerter newInstance() {
		if(_alerter == null) {
			String os_name = System.getProperty("os.name");
			System.out.println("os name = " + os_name);
			if(os_name.toLowerCase().startsWith("mac os x")) {
				loadMac();
			} else {
				_alerter = new Alerter();
			}
		}
		return _alerter;
	}
	
	private static void loadMac() {
		System.out.println("creating mac alerter");
		try {
		_alerter = (Alerter) Alerter.class.forName(
			"org.jdesktop.jdic.misc.impl.MacOSXAlerter")
			.newInstance();
		} catch (Throwable ex) {
			System.out.println("couldn't load the mac version");
			System.out.println(""+ex.getMessage());
			ex.printStackTrace();
			_alerter = new Alerter();
		}	
	}

	public void alert() {
		System.out.println("alerts are not supported on this platform");
	}
}
