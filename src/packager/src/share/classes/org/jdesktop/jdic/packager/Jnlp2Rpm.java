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

package org.jdesktop.jdic.packager;

import java.io.IOException;
import org.jdesktop.jdic.packager.impl.Jnlp2Package;
import org.jdesktop.jdic.packager.impl.JnlpConstants;
import org.jdesktop.jdic.packager.impl.JnlpUtility;


/**
 * Command line entry point into JDIC Packager to package a JNLP application
 * into a RPM package. This class is entered via the canonical `public static 
 * void main` entry point and reads the command line arguments. 
 * 
 * <pre>
 * The command line to use this class is:
 * java [property options] org.jdesktop.jdic.packager.Jnlp2Rpm &lt;JNLP file path&gt;
 * 
 * &lt;JNLP file path&gt;: the path of the JNLP file.
 * [property options] include:
 *   -DResourceDir=&lt;value&gt;: set the directory of the JNLP resource files. 
 *     The default value is the parent path of the given JNLP file.
 *   -DLicenseDir=&lt;value&gt;: set the directory of the license files if available.
 *   -DPackageName=&lt;value&gt;: set the name of the generated rpm package. 
 *     The default value is the jnlp file name without extension.
 *   -DOutputDir=&lt;value&gt;: set the directory where the generated rpm package 
 *     is put. The default value is the current directory.
 *   -DVersion=&lt;value&gt;: set the version number of the generated package.
 *     The default value is 1.0.
 *   -DRelease=&lt;value&gt;: set the release number of the generated package,
 *     The default value is 1.
 *   -DEnableShortcut=&lt;true|false&gt;: if true, create shortcut on the desktop after 
 *     the generated package is installed; if false, no shortcut will be created. 
 *     The default value is false.
 *   -DEnableAssociation=&lt;true|false&gt;: if true, associate the file extension .jnlp with 
 *     Java Web Start executable after the generated package is installed; if false, no
 *     association will be created. The default value is false. 
 *   -DEnableSystemCache=&lt;true|false&gt;: if true, install the JNLP application into 
 *     the system cache of Java Web Start; if false, it's installed in the user cache.
 *     The default value is false.
 * </pre>
 * <p>
 * While the end-user installs the generated package, Java Web Start executable (javaws)
 * will run to install the JNLP application to the system or user cache of Java Web Start.
 * It will then be displayed in the "Application Manager" dialog of Java Web Start.
 * <p> 
 * While the end-user uninstalls the installed package, javaws will run to remove the JNLP 
 * application from the system or user cache, as well as the "Application Manager" 
 * dialog of Java Web Start.
 * 
 * @see Jnlp2Msi
 * @see Jnlp2Pkg
 */
public class Jnlp2Rpm {
    /**
     * Command line entry point. This method starts generating a RPM package 
     * using the given JNLP application and specified properties.
     *
     * @param args command line arguments. 
     */
    public static void main(final String[] args) {
        // Check if this class/tool could be used on the platform.
        JnlpUtility.checkPlatformCompatibility(JnlpConstants.OS_LINUX);

        // Generate an installable package.
        try {
            Jnlp2Package.generatePackage(args);
        } catch (IOException e1) {
            System.out.println(e1.getMessage());
        } catch (IllegalArgumentException e2) {
            System.out.println(e2.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Error err) {
            err.printStackTrace();
        } finally {
            System.exit(0);
        } 
    }
}
