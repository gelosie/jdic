/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.jdesktop.jdic.icons.impl;

import java.io.File;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
* Contains base directory functionality. See 
* <a href="http://freedesktop.org/Standards/basedir-spec">basedir-spec</a> for specification.
*/
public class XdgDirectory {

    /**
    * not instantiable
    */
    private XdgDirectory() {
    }

    private static final boolean isLoaded;
    static {
        boolean success= false;
        try {
            System.loadLibrary("jdic_icon");
            gConfInit();
            success= true;
        }
        catch(Throwable ex) {
        }
        isLoaded= success;
    }

    /**
    * Was the native portion successfully loaded?
    */
    public static boolean isLoaded() {
        return isLoaded;
    }

    private native static void gConfInit();

    /**
    * Obtain the value of an environment variable
    * @param variableName The name of the environment variable to obtain.
    * @return The value of the environment variable.  Return a null if variable is not set or is an empty string.
    */
    private static native String getEnvironmentVariable(String variableName);

    /**
    * Obtain the value of a gconf key
    * @param variableName The name of the gconf variable to obtain.
    * @return The value of the gconf variable.  Return a null if variable is not set or is an empty string.
    */
    native static String getGconfValue(String key);

    /**
    * Obtain File based upon value of environment variable.  A default 
    * location is used if the environment variable is not set or is empty.
    * @param envVar The environment variable to consult for the name 
    * of the directory.
    * @param defaultLocation The default location, relative to the 
    * user's home directory, of the directory
    */
    private static File getUserFile(String envVar, String defaultLocation) {
        final String envVal= getEnvironmentVariable(envVar);
        return new File(envVal!=null ?envVal :getEnvironmentVariable("HOME")+defaultLocation);
    }

    private static File userDataDir;
    /**
    * Obtain the user data directory.
    * This is set by the environment variable <code>XDG_DATA_HOME</code>.
    * If the environment variable <code>XDG_DATA_HOME</code> is not set or is empty, 
    * a default value of <code>$HOME/.local/share</code> is used.
    * @return A File which represents the user data directory.
    */
    public static File getUserDataDir() {
        if(userDataDir==null)
            userDataDir= getUserFile("XDG_DATA_HOME", "/.local/share");
        return userDataDir;
    }

    private static File userConfigDir;
    /**
    * Obtain the user configuration directory.
    * This is set by the environment variable <code>XDG_CONFIG_HOME</code>.
    * If the environment variable <code>XDG_CONFIG_HOME</code> is not set or is empty, 
    * a default value of <code>$HOME/.config</code> is used.
    * @return A File which represents the user configuration directory.
    */
    public static File getUserConfigDir() {
        if(userConfigDir==null)
            userConfigDir= getUserFile("XDG_CONFIG_HOME", "/.config");
        return userConfigDir;
    }

    private static File cacheDir;
    /**
    * Obtain the cache directory.
    * This is set by the environment variable <code>XDG_CACHE_HOME</code>.
    * If the environment variable <code>XDG_CACHE_HOME</code> is not set or is empty, 
    * a default value of <code>$HOME/.cache</code> is used.
    * @return A File which represents the cache directory.
    */
    public static File getCacheDir() {
        if(cacheDir==null)
            cacheDir= getUserFile("XDG_CACHE_HOME", "/.cache");
        return cacheDir;
    }

    private static Pattern colon= Pattern.compile(File.pathSeparator);
    /**
    * Transform a directory set string into an array of directory names.
    * Separate directories using ':'.  The array will be stripped of empty entries.
    * @param sourcePath The colon(:) separated list of directories.
    * @return The cannonical array of directories
    */
    private static File[] cannon(String sourcePath) {
        String[] dirnames= colon.split(sourcePath);
        File[] dirs= new File[dirnames.length];

        int dst= 0;
        for(int src= 0; src<dirs.length;) {
            String d= dirnames[src++];
            if(d.length()==0)
                continue;

            dirs[dst++]= new File(d);
        }
        if(dst==dirs.length)
            return dirs;

        File[] rc= new File[dst];
        System.arraycopy(dirs, 0, rc, 0, dst);
        return rc;
    }

    /**
    * Obtain array of directory names based upon value of environment 
    * variable.  A default set of directory locations are used if the 
    * environment variable is not set or is empty.
    * <p>
    * The resulting array will be stripped of empty values.  All 
    * directory names will have a trailing '/'
    * @param envVar The environment variable to consult for the names 
    * of the directories.
    * @param defaultLocation The default directory locations
    */
    private static File[] getSystemDirs(String envVar, String defaultLocation) {
        final String envVal= getEnvironmentVariable(envVar);
        if(envVal!=null) {
            File[] dirs= cannon(envVal);
            if(dirs.length!=0)
                return dirs;
        }
        return cannon(defaultLocation);
    }

    private static File[] systemDataDirs;
    /**
    * Obtain the array of system data directories based upon value 
    * of environment variable <code>XDG_DATA_DIRS</code>.  If 
    * <code>XDG_DATA_DIRS</code> is not set, or is empty, the default
    * set <code>/usr/local/share/:/usr/share/</code> is used.
    */
    public static File[] getSystemDataDirs() {
        if(systemDataDirs==null)
            systemDataDirs= getSystemDirs("XDG_DATA_DIRS", "/usr/local/share/:/usr/share/");
        return systemDataDirs;
    }

    private static File[] systemConfigDirs;
    /**
    * Obtain the array of system config directoriess based upon value 
    * of environment variable <code>XDG_CONFIG_DIRS</code>.  If 
    * <code>XDG_CONFIG_DIRS</code> is not set, or is empty, the default
    * set <code>/etc/Xdg/</code> is used.
    */
    public static File[] getSystemConfigDirs() {
        if(systemConfigDirs==null)
            systemConfigDirs= getSystemDirs("XDG_CONFIG_DIRS", "/etc/Xdg/");
        return systemConfigDirs;
    }

    /**
    * Obtain an iterator over user and system directories.
    * First the ${user directory}/pathSuffix is returned, then 
    * each of the ${system directories}/pathSuffix are returned.
    * Only directories which exist will be returned.
    * @param pathSuffix The subdirectory of each data directory.
    * @return An iterator of File, each File will exist and can be read.
    */
    private static Iterator getDirs(final File user, final File[] system, final String pathSuffix) {

        return new Iterator() {

            private int i= -1;

            private File getNextValid() {
                for(;;) {
                    if(i==system.length)
                        return null;

                    File f= i<0 ?user :system[i];
                    ++i;
    
                    File sd= new File(f, pathSuffix);
                    if(sd.canRead())
                        return sd;
                }
            }

            private File lookAhead;

            // Returns true if the iteration has more elements.
            public boolean hasNext() {
                if(lookAhead==null) {
                    lookAhead= getNextValid();
                }
                return lookAhead!=null;
            }

            // Returns the next element in the iteration.
            public Object next() {
                Object rc= lookAhead;
                if(rc!=null) {
                    lookAhead= null;
                }
                else {
                    rc= getNextValid();
                    if(rc==null) {
                        throw new java.util.NoSuchElementException();
                    }
                }
                return rc;
            }

            // Removes from the underlying collection the last element returned by the iterator (optional operation).
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
    * Obtain an iterator over all data directories which may contain data.
    * First the ${user data directory}/pathSuffix is returned, then 
    * each of the ${system data directories}/pathSuffix are returned.
    * Only directories which exist will be returned.
    * @param pathSuffix The subdirectory for each data directory.
    * @return An iterator of File, each File will exist and can be read.
    */
    public static Iterator getDataDirs(String pathSuffix) {
        return getDirs(getUserDataDir(), getSystemDataDirs(), pathSuffix);
    }

    /**
    * Obtain an iterator over all config directories which may be read.
    * First the ${user config directory}/pathSuffix is returned, then 
    * each of the ${system config directories}/pathSuffix are returned.
    * Only files which exist will be returned.
    * @param pathSuffix The subdirectory for each config directory.
    * @return An iterator of File, each File will exist and can be read.
    */
    public static Iterator getConfigDirs(String pathSuffix) {
        return getDirs(getUserConfigDir(), getSystemConfigDirs(), pathSuffix);
    }

}


