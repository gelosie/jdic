/* File: org/gnome/Rhythmbox/Shell.java */
package org.gnome.Rhythmbox;
import java.util.Map;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.UInt32;
import org.freedesktop.dbus.Variant;
public interface Shell extends DBusInterface
{
   public static class visibilityChanged extends DBusSignal
   {
      public final boolean a;
      public visibilityChanged(String path, boolean a) throws DBusException
      {
         super(path, a);
         this.a = a;
      }
   }

  public void notify(boolean userRequested);
  public void clearQueue();
  public void removeFromQueue(String uri);
  public void quit();
  public void addToQueue(String uri);
  public void setSongProperty(String uri, String propname, Variant value);
  public Map<String,Variant> getSongProperties(String uri);
  public void present(UInt32 arg0);
  public DBusInterface getPlaylistManager();
  public DBusInterface getPlayer();
  public void loadURI(String arg0, boolean arg1);

}
