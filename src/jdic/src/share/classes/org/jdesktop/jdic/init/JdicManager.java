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

package org.jdesktop.jdic.init;

import java.io.File;
import java.net.URL;
import java.lang.reflect.Field;

import org.jdesktop.jdic.browser.internal.WebBrowserUtil;

/**
 * Initialization manager for JDIC to set the environment variables or initialize 
 * the set up for native libraries and executable files.
 * <p>
 * There are 3 modes of operation: WebStart, file system, and .jar file.
 * <p>
 * When using WebStart, please specify a .jar file(jdic-native.jar) with the 
 * native libraries for your platform to be loaded by WebStart in your JNPL. 
 * This class will find the unjared native libraries and executables, and use 
 * them directly.
 * <p>
 * If not in WebStart, the system will expect the native libraries to be located 
 * in directory at the root of the classpath or .jar containing this class. 
 * 
 * @author     Michael Samblanet
 *             Paul Huang
 *             George Zhang
 * @created    July 29, 2004
 */
public class JdicManager {
    private boolean isShareNativeInitialized = false;
    private boolean isBrowserNativeInitialized = false;    

    /** If the current platform is Windows */
    boolean isWindows = 
        (System.getProperty("os.name").indexOf("Windows") >= 0) ? 
        true : false;
    
    /** The environment variable for library path setting */
    String libPathEnv = isWindows ? "PATH" : "LD_LIBRARY_PATH";
    
    /** The path for the JDIC native files (jdic.dll/libjdic.so, etc) */ 
    String binaryPath = null;

    /**  Singleton instance of this class */
    private static JdicManager sSingleton = null;

    /**
     * Private constructor to prevent public construction.
     */
    private JdicManager() {
    }

    /**
     * Returns a singleton instance of <code>JdicManager</code>.
     */
    public static synchronized JdicManager getManager() {
        if (sSingleton == null) {
            sSingleton = new JdicManager();
        }
        return sSingleton;
    }

    /**
     * Initializes the shared native file settings for all the JDIC components/
     * packages. Set necessary environment variables for the shared native 
     * library and executable files, including *.dll files on Windows, and *.so 
     * files on Unix.
     * 
     * @exception JdicInitException Generic initialization exception
     */
    public void initShareNative() throws JdicInitException {
        // If the shared native file setting was already initialized, 
        // just return.
        if (isShareNativeInitialized) {
            return;
        }

        try {
            // Find the root path of this class.            
            binaryPath = (new URL(JdicManager.class.getProtectionDomain()
           	    .getCodeSource().getLocation(), ".")).openConnection()
                .getPermission().getName();
           	binaryPath = (new File(binaryPath)).getCanonicalPath();
            if (System.getProperty("javawebstart.version") != null) {
                //  We are running under WebStart.
                //  NOTE: for a WebStart application, the jar file including 
                //        the native libraries/executables must use the name 
                //        "jdic-native.jar". 
                String cacheDirName = "RN" + "jdic-native.jar" + "/";
                binaryPath += File.separator + cacheDirName; 
            }                                                     
    
            // Add the binary path (including jdic.dll or libjdic.so) to 
            // "java.library.path", since we need to use the native methods in 
            // class InitUtility.
            String newLibPath = binaryPath + File.pathSeparator +
                                System.getProperty("java.library.path"); 
            System.setProperty("java.library.path", newLibPath);
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            if (fieldSysPath != null) {
                fieldSysPath.set(System.class.getClassLoader(), null);
            }
    
        } catch (Throwable e) {
            throw new JdicInitException(e);
        }
        
        isShareNativeInitialized = true;
    }

    /**
     * Initializes the native file settings for the JDIC Browser component 
     * (package <code>org.jdecktop.jdic.browser</code>). Set necessary 
     * environment variables for the Browser specific native library and 
     * executable files, including *.exe files on Windows, and mozembed-<os>-gtk* 
     * files on Unix.
     * 
     * @exception JdicInitException Generic initialization exception
     */
    public void initBrowserNative() throws JdicInitException {
        // The Browser component is used.
        // If the Browser specific native file setting was already initialized, 
        // just return.
        if (isBrowserNativeInitialized) {
            return;
        }

        try {
            // Pre-append the JDIC binary path to PATH(on Windows) or 
            // LD_LIBRARY_PATH(on Unix).                     
            InitUtility.preAppendEnv(libPathEnv, binaryPath); 

            String browserPath = WebBrowserUtil.getBrowserPath();
            if (browserPath == null) {
                throw new JdicInitException(
                    "Can't locate the native browser path!");
            }

            
            if (!WebBrowserUtil.isDefaultBrowserMozilla()) {
                 if (isWindows) {
                    // Internet Explorer is the default/embedded browser.
                    // Pre-append the "ielib" directory to PATH, which includes 
                    // the bundled, IeEmbed.exe dependent library nspr4.dll.
                    // NOTE: if nspr4.dll is put under any other path, it needs
                    //       to be added to PATH.
                    InitUtility.preAppendEnv(libPathEnv, binaryPath 
                                          + File.separator + "ielib"); 
                 }                
            } else {
            	// Mozilla is the default/embedded browser.
                // Use the user defined env variable or the mozilla binary
                // path as the default MOZILLA_FIVE_HOME value.
                String envMFH = InitUtility.getEnv("MOZILLA_FIVE_HOME");
                if (envMFH == null) {
                    File browserFile = new File(browserPath);
                    if (browserFile.isDirectory()) {
                        envMFH = browserFile.getCanonicalPath();
                    } else {
                        envMFH = browserFile.getCanonicalFile().getParent();
                    }
                }
                
                if (!isWindows) {
                    // On Unix, add the binary path to PATH.
                    InitUtility.preAppendEnv("PATH", binaryPath);
                    // When running on webstart, the browser binary will lose
                    // "x" permission after extracted from .jar file.
                    String embedBinary = WebBrowserUtil.getEmbedBinaryName();
                    Runtime.getRuntime().exec("chmod a+x "+ 
                            binaryPath + File.separator + embedBinary);                    
                } else {               
                    // Mozilla on Windows, reset MOZILLA_FIVE_HOME to the GRE 
                    // directory path:  
                    //   [Common Files]\mozilla.org\GRE\1.x_BUILDID, 
                    // if Mozilla installs from a .exe package.
                    //                
                    String xpcomPath = envMFH + File.separator + "xpcom.dll";                        
                    if (!(new File(xpcomPath).isFile())) {
                        // Mozilla installs from a .exe package. Check the 
                        // installed GRE directory.
                        String mozGreHome 
                            = WebBrowserUtil.getMozillaGreHome();
                        if (mozGreHome == null) {
                            throw new JdicInitException(
                                "Can't locate the GRE directory of the " +
                                "installed Mozilla binary: " + envMFH);
                        }                       
                        envMFH = mozGreHome;
                    }
                }
              
                InitUtility.preAppendEnv("MOZILLA_FIVE_HOME", envMFH);
                InitUtility.preAppendEnv(libPathEnv, envMFH);
            } // end - Mozilla is the default/embedded browser.
        } catch (Throwable e) {
            throw new JdicInitException(e);
        }
        
        isBrowserNativeInitialized = true;
    }
}
