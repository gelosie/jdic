// Copyright © 2004 Sun Microsystems, Inc. All rights reserved. Use is
// subject to license terms.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the Lesser GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

package org.jdesktop.jdic.screensaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the currently active settings for a screensaver.
 * Assists in persisting the settings if the underlying platform specific
 * screensaver APIs do not do this for you (e.g. on Windows).  The settings
 * are saved to the user's home directory under 
 * .saverbeans/screensaver.properties.  In order for this class to work
 * properly, the configuration file must use '-' before each option.
 * Boolean options must be of the form '-option' and value options must
 * be of the form '-option value'.
 *
 * @author Mark Roth
 */
public class ScreensaverSettings {

    /** Logging support */
    private static Logger logger = 
        Logger.getLogger("org.jdesktop.jdic.screensaver");
    
    /** The directory we store settings in, under home */
    private static final String APPLICATION_DIRECTORY = ".saverbeans";
    
    /** The properties file to store settings to */
    private static final String PROPERTIES_FILE = "settings.properties";
    
    /** The currently active settings */
    private Properties settings = new Properties();
    
    /**
     * Sets a property for this screensaver.
     *
     * @param key The key of the property
     * @param value The value of the property
     */
    public void setProperty(String key, String value) {
        this.settings.setProperty(key, value);
    }
    
    /**
     * Gets a property for this screensaver.
     *
     * @return The value associated with the given key
     * @param key The key to retrieve the value for
     */
    public String getProperty(String key) {
        return this.settings.getProperty(key);
    }
    
    /**
     * Returns these settings as a normalized commandline.  Boolean
     * options are represented as -option if set or nothing if unset.
     * Options with values are represented as '-option value'.
     *
     * @return The normalized commandline
     */
    public String getNormalizedCommandline() {
        StringBuffer result = new StringBuffer();
        Iterator keys = this.settings.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            String value = this.settings.getProperty(key);
            result.append('-');
            result.append(key);
            if(!value.equals("")) {
                // if value is not "", it is not a boolean option
                result.append(' ');
                if(value.startsWith("-") || (value.indexOf(' ') != -1)) {
                    result.append('\"');
                    result.append(value);
                    result.append('\"');
                } 
                else {
                    result.append(value);
                }
            }
            if(keys.hasNext()) {
                result.append(' ');
            }
        }
        return result.toString();
    }
    
    /**
     * Saves the currently selected settings in the user's home directory.
     * This is provided if the underlying platform specific
     * screensaver APIs do not do this for you (e.g. on Windows).
     * If an error occurs, it is ignored.
     *
     * @param screensaverName The key to store the settings under in the
     *     properties file.
     */
    public void saveSettings(String screensaverName) {
        try {
            File propertiesFile = getPropertiesFile();
            Properties settings = new Properties();
            if(propertiesFile.exists()) {
                FileInputStream in = new FileInputStream(propertiesFile);
                settings.load(in);
                in.close();
            }
            settings.setProperty(screensaverName, getNormalizedCommandline());
            FileOutputStream out = new FileOutputStream(propertiesFile);
            settings.store(out, "SaverBeans Screensaver Settings");
            out.close();
        }
        catch(IOException e) {
            logger.log(Level.SEVERE, "Could not save settings for " + 
                screensaverName, e);
        }
    }
    
    /**
     * Loads the currently selected settings from the user's home directory.
     *
     * @param screensaverName The key to load the settings from in the
     *     properties file.
     */
    public void loadSettings(String screensaverName) {
        try {
            File propertiesFile = getPropertiesFile();
            Properties lsettings = new Properties();
            FileInputStream in = new FileInputStream(propertiesFile);
            lsettings.load(in);
            in.close();
            String loadedSettings = lsettings.getProperty(screensaverName);
            loadFromCommandline(loadedSettings);
        }
        catch(IOException e) {
            logger.log(Level.WARNING, "Could not load settings for " + 
                screensaverName, e);
        }
    }

    /**
     * Load the current settings from the given normalized commandline
     *
     * @param loadedSettingsParam The settings to be loaded, in normalized
     *     commandline form.
     * @see #getNormalizedCommandline
     */
    public void loadFromCommandline(String loadedSettingsParam) {
        settings.clear();
        String loadedSettings = loadedSettingsParam + " ";
        ArrayList options = new ArrayList();
        String option = "";
        boolean quote = false;
        // break into components, separated by spaces
        // handle spaces inside quotes as a single component
        for(int i = 0; i < loadedSettings.length(); i++) {
            char c = loadedSettings.charAt(i);
            if(c == '\"') {
                quote = !quote;
            }
            else if(!quote && c == ' ') {
                option = option.trim();
                if(!option.equals("")) {
                    options.add(option);
                }
                option = "";
            }
            else {
                option += c;
            }
        }
        // determine which are keys and which are values.
        // if 2 keys start with -, the first is a boolean
        // if last key starts with -, it is a boolean
        // otherwise, first is key, second is value.
        for(int i = 0; i < options.size(); i++) {
            String opt = (String)options.get(i);
            if(opt.startsWith("-")) {
                // ignore non-options.  This is an option since it 
                // starts with "-".
                if(i == (options.size()-1)) {
                    // last option, must be a boolean
                    settings.setProperty(opt.substring(1), "");
                }
                else {
                    String nextOpt = (String)options.get(i+1);
                    if(nextOpt.startsWith("-")) {
                        // this option is a boolean
                        settings.setProperty(opt.substring(1), "");
                    }
                    else {
                        // next option is the value for this option.
                        settings.setProperty(opt.substring(1), nextOpt);
                    }
                }
            }
        }
    }
    /**
     * Dump the current set of options to the display
     */
    private void dumpOptions() {
        Iterator keys = settings.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            String value = settings.getProperty(key);
            System.out.println("[OPTION:key=" + key + ",value=" + value + "]");
        }
        System.out.println("As commandline: " + getNormalizedCommandline());
    }
    
    /**
     * Returns the full path to the properties file we're storing settings in.
     */
    private File getPropertiesFile() {
        File homeDir = new File(System.getProperty("user.home"));
        File appDir = new File(homeDir, APPLICATION_DIRECTORY);
        appDir.mkdirs();
        return new File(appDir, PROPERTIES_FILE);
    }

}
