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

package org.jdesktop.jdic.fileutil.impl;


import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import org.jdesktop.jdic.fileutil.FileUtil;

import com.apple.cocoa.foundation.NSPathUtilities;


/**
 * @author padrao
 *
 */
public class MacOSXFileUtil extends FileUtil {
	
    /**
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can. It's a convenience method, works in the same way
     * than delete(File file, true).
     * 
     * @param file the file or directory to be recycled.
     * @return <code>true</code> if and only if the file or directory is
     *         successfully recycled; <code>false</code> otherwise.
     * @throws IOException If an I/O error occurs, which is possible because 
     *         the construction of the canonical pathname may require filesystem
     *         queries.
     * 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed, or if a security manager exists and its <code>{@link
     *         java.lang.SecurityManager#checkRead}</code> method denies read 
     *         access to the file.
     */
    public boolean recycle(File file) throws IOException, SecurityException {
        return recycle(file, true);
    }

    /**
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can. It will return true even if the user aborted the 
     * operation.
     * 
     * @param file  the file or directory to be recycled.
     * @param confirm <code>true</code> shows a confirmation dialog; 
     *        <code>false</code> recycles without notification.
     * @return <code>true</code> if and only if the file or directory is
     *         successfully recycled; <code>false</code> otherwise.
     * @throws IOException If an I/O error occurs, which is possible because the
     *         construction of the canonical pathname may require filesystem
     *         queries.
     * 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed, or if a security manager exists and its <code>{@link
     *         java.lang.SecurityManager#checkRead}</code> method denies read 
     *         access to the file.
     */
    public boolean recycle(File file, boolean confirm) throws IOException,
            SecurityException {
        String fullPath = file.getCanonicalPath();

        System.out.println(fullPath);
        return (recycle(fullPath, confirm) == 0 ? true : false);
    }

    /**
     * Return the amount of free bytes available in the directory or file
     * referenced by the file Object.
     * 
     * @param file
     * @return the amount of free space in the Disk. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     */
    public BigInteger getFreeSpace(File file) throws IOException {
        return new BigInteger(NSPathUtilities.FileSystemFreeSize);
    }

    private native int recycle(String fullPath, boolean confirm);
}
