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

package org.jdesktop.jdic.fileutil;


import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.jdesktop.jdic.fileutil.impl.MacOSXFileUtil;
import org.jdesktop.jdic.fileutil.impl.SolarisFileUtil;
import org.jdesktop.jdic.fileutil.impl.UnixFileUtil;
import org.jdesktop.jdic.fileutil.impl.Win32FileUtil;


/**
 * @author Fábio Castilho Martins
 *  
 */
public abstract class FileUtil {

    private static FileUtil _fileUtil;
	
    /**
     * Return a new (probably shared) FileUtil object.
     * <p>
     * This will create the appropriate implementation for the currently 
     * running platform.
     * <br>
     * If an implementation is not available (the current platform is not 
     * supported) then it will throw a ClassNotFoundException.
     * 
     * @return FileUtil for the current platform.
     * @throws ExceptionInInitializerError if the initialization of the 
     *         Platform implementation provoked by this method fails.
     * @throws SecurityException if there is no permission to create a new
     *         instance.
     * @throws ClassNotFoundException if the platform is not supported.
     */
    public static FileUtil getInstance() throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        String os_name = System.getProperty("os.name");

        if (_fileUtil == null) {
            if (os_name.startsWith("Mac OS X")) {
                loadMac();
            } else if (os_name.startsWith("Windows")) {
                loadWin32();
            } else if (os_name.startsWith("Linux")
                    || os_name.startsWith("LINUX")) {
                loadUnix();
            } else if (os_name.startsWith("Solaris")
                    || os_name.startsWith("SunOS")) {
                loadSolaris();
            } else {
                throw new ClassNotFoundException("Platform unsupported");
            }
        }
        return _fileUtil;

    }

    /**
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can. It's a convenience method, works in the same way
     * than delete(File file, true).
     * 
     * @param file the file or directory to be recycled.
     * @return <b>true</b> if and only if the file or directory is
     *         successfully recycled; <b>false</b> otherwise.
     * @throws IOException If an I/O error occurs, which is possible because the
     *         construction of the canonical pathname may require filesystem
     *         queries.
     * 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed.
     */
    public abstract boolean recycle(File file) throws IOException,
            SecurityException;

    /**
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can. It will return true even if the user aborted the
     * operation.
     * 
     * @param file the file or directory to be recycled.
     * @param confirm <b>true</b> shows a confirmation dialog; <b>false</b> 
     *        recycles without notification.
     * @return <b>true</b> if and only if the file or directory is successfully 
     *         recycled; <b>false</b> otherwise.
     * @throws IOException If an I/O error occurs, which is possible because the
     *         construction of the canonical pathname may require filesystem
     *         queries.
     * 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed.
     */
    public abstract boolean recycle(File file, boolean confirm)
            throws IOException, SecurityException;

    /**
     * Return the amount of free bytes available in the directory or file
     * referenced by the file Object.
     * 
     * @param file
     * @return the amount of free space in the Disk. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     */
    public abstract BigInteger getFreeSpace(File file) throws IOException;

    private static void loadMac() throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        _fileUtil = (MacOSXFileUtil) Class.forName(
                "org.jdesktop.jdic.fileutil.impl.MacOSXFileUtil").newInstance();
    }

    private static void loadWin32() throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        _fileUtil = (Win32FileUtil) Class.forName(
                "org.jdesktop.jdic.fileutil.impl.Win32FileUtil").newInstance();
    }

    private static void loadUnix() throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        _fileUtil = (UnixFileUtil) Class.forName(
                "org.jdesktop.jdic.fileutil.impl.UnixFileUtil").newInstance();
    }

    private static void loadSolaris() throws IllegalAccessException,
            InstantiationException, ClassNotFoundException {
        _fileUtil = (SolarisFileUtil) Class.forName(
                "org.jdesktop.jdic.fileutil.impl.SolarisFileUtil").newInstance();
    }

}
