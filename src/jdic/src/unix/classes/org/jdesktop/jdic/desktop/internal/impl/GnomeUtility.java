/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 * 
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

package org.jdesktop.jdic.desktop.internal.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StreamTokenizer;

/**
 * Utility class shared by classes on Gnome.
 */
public class GnomeUtility {
    static {
        System.loadLibrary("jdic");
    }

    /**
     * Suppress default constructor.
     */
    private GnomeUtility() {
    }

    /**
     * Checks if there is already a running Mozilla instance.
     * 
     * @param mozillaPath the given absolute path for mozilla executable.
     * @throws IOException if running Mozilla commandline fails.
     * @return true if there is already a mozilla instance running.
     */
    public static boolean isMozillaRunning(String mozillaPath) throws IOException {
        // Check running mozilla instance using: mozilla -remote ping().
        // If there is no running Mozilla instance. The complete output is: 'No running 
        // window found.'
        String MOZILLA_OUTPUT_NO_RUNNING = "No running window";

        InputStream stderr = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            Process proc =
                Runtime.getRuntime().exec(
                    new String[] { mozillaPath, "-remote", "ping()" });

            stderr = proc.getErrorStream();
            isr = new InputStreamReader(stderr);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.indexOf(MOZILLA_OUTPUT_NO_RUNNING) != -1) {
                    br.close();
                    return false;
                }
            }
            br.close();
        } catch (IOException e) {
            throw e;
        }

        return true;
    }

    /**
     * Returns the Mozilla version number.
     * 
     * @param mozillaPath the given absolute path for mozilla executable.
     * @return the Mozilla version number.
     */
    public static String getMozillaVersionNumber(String mozillaPath) {
        String MOZILLA_VERSION_PREFIX = "Mozilla ";

        InputStream stdin = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        Runtime rt = Runtime.getRuntime();
        Process proc = null;
        String verNum = null;

        try {
            // Run "mozilla -version" to get the version info.
            proc = rt.exec(new String[] { mozillaPath, "-version" });

            stdin = proc.getInputStream();
            isr = new InputStreamReader(stdin);
            br = new BufferedReader(isr);

            String line = null;
            if ((line = br.readLine()) != null) {
                // Parse the version info to get the version number.
                // The returned version info would be like: 
                // "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.0.1) Gecko/20020830, build 2002083014"
                // or: "Mozilla 1.4, Copyright (c) 2003 mozilla.org, build 2003052912"
                // Since the patch for supporting this feature(targeting frame) is checked into Mozilla source tree
                // in revision after 1.4, so it's only necessary to parse the second version info style.
                if (line.indexOf(MOZILLA_VERSION_PREFIX) == 0) {
                    verNum =
                        line.substring(
                            MOZILLA_VERSION_PREFIX.length(),
                            line.indexOf(','));
                }
            }
            br.close();
        } catch (IOException e) {
            return null;
        }

        return verNum;
    }
    
    /**
     * Returns the Gnome default system mailer path.
     * 
     * @return path of the default system mailer.
     */
    public static String getDefaultMailerPath() 
        throws UnsupportedOperationException {
        String DEFAULT_MAILER_PROPERTY_FILE = "defmailer.properties"; 
        // Find the system mailer (default "mailto" protocol handler) path by GConf settings first.
        String defMailerPath = nativeGetDefaultMailerPath();
        
        // If no default mailer setting in GConf, check the property file defining the default mailer.
        if (defMailerPath != null) {
            return defMailerPath;
        } else {       
            Properties mailerProp = new Properties();
            String propFilePath = getPropFilePath(DEFAULT_MAILER_PROPERTY_FILE);

            if (propFilePath == null) {
                throw new UnsupportedOperationException("No default mailer is set in GConf, and the property file defining default mailer" +
                    " is not found: " + DEFAULT_MAILER_PROPERTY_FILE);
            } else {
                // Found the default mailer property file.
                try {
                    mailerProp.load(new FileInputStream(propFilePath));
                    defMailerPath = mailerProp.getProperty("MAILER");
            
                    if (defMailerPath == null) {
                        throw new UnsupportedOperationException("The default mailer path is not set in the property file: " +
                            propFilePath);
                    }

                    return defMailerPath;
                } catch (IOException e) {
                    throw new UnsupportedOperationException("Failed to get default mailer path from property file: " + propFilePath);
                }
            }
        }
    }

    /**
     * Returns the path of the property file defining the system default mailer. 
     * 
     * @param propFileName the name of the property file defining the system default mailer.
     * @return path of the property file.
     */
    private static String getPropFilePath(String propFileName) {
        String classpath = System.getProperty("java.class.path");
        StreamTokenizer classpath_st = new StreamTokenizer(new StringReader(classpath));

        classpath_st.whitespaceChars(File.pathSeparatorChar, File.pathSeparatorChar);
        classpath_st.wordChars(File.separatorChar, File.separatorChar);

        classpath_st.ordinaryChar('.');
        classpath_st.wordChars('.', '.');
        classpath_st.ordinaryChar(' ');
        classpath_st.wordChars(' ', ' ');
        classpath_st.wordChars('_', '_');

        try {
            while (classpath_st.nextToken() != StreamTokenizer.TT_EOF) {
                int jarIndex = -1;

                if ((classpath_st.ttype == StreamTokenizer.TT_WORD) &&
                    ((jarIndex = classpath_st.sval.indexOf("jdic.jar")) != -1)) {
                    String propPath = classpath_st.sval.substring(0, jarIndex);
                    if (propPath != null) {
                        propPath = propPath + File.separator + propFileName;
                    } else {
                        propPath = "." + File.separator + propFileName;
                    }

                    File tmpFile = new File(propPath);
                    if (tmpFile.exists()) {
                        return tmpFile.getAbsolutePath();
                    } 
                }
            }
        } catch (IOException ioe) {
        }

        return null;
    } 
       
    private native static String nativeGetDefaultMailerPath();
}
