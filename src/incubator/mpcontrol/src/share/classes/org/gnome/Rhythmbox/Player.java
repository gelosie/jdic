package org.gnome.Rhythmbox;

import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;

public interface Player extends DBusInterface {
	public static class playingSongPropertyChanged extends DBusSignal {
		public final String a;

		public final String b;

		public final Variant c;

		public final Variant d;

		public playingSongPropertyChanged(String path, String a, String b,
				Variant c, Variant d) throws DBusException {
			super(path, a, b, c, d);
			this.a = a;
			this.b = b;
			this.c = c;
			this.d = d;
		}
	}

	public static class elapsedChanged extends DBusSignal {
		public final UInt32 a;

		public elapsedChanged(String path, UInt32 a) throws DBusException {
			super(path, a);
			this.a = a;
		}
	}

	public static class playingUriChanged extends DBusSignal {
		public final String a;

		public playingUriChanged(String path, String a) throws DBusException {
			super(path, a);
			this.a = a;
		}
	}

	public static class playingChanged extends DBusSignal {
		public final boolean a;

		public playingChanged(String path, boolean a) throws DBusException {
			super(path, a);
			this.a = a;
		}
	}

	public boolean getMute();

	public void setMute(boolean mute);

	public void setVolumeRelative(double volume);

	public void setVolume(double volume);

	public double getVolume();

	public void setElapsed(UInt32 elapsed);

	public UInt32 getElapsed();

	public String getPlayingUri();

	public boolean getPlaying();

	public void next();

	public void previous();

	public void playPause(boolean arg0);

}
