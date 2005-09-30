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

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import org.jdesktop.jdic.desktop.internal.LaunchFailedException;
import org.jdesktop.jdic.desktop.internal.LaunchService;

/**
 * Concrete implementation of the LaunchService interface for Gnome.
 */
public class GnomeLaunchService implements LaunchService {
    static {
        Toolkit.getDefaultToolkit();
        System.loadLibrary("jdic");
    }
    
    /** 
     * Converts the given filename path to a unique canonical form. Which removes redundent 
     * names, such as: `.' or `..' or symbolic links (on UNIX).
     */
    public File resolveLinkFile(File file) {
        File resolvedFile = file; 
        try {
            resolvedFile = file.getCanonicalFile(); 
        } catch (IOException e) {
        }
         
         return resolvedFile;
    } 

    /**
     * Launches the associated application to open the given file.
     * 
     * @param file the given file to be opened.
     * @throws LaunchFailedException if the given file has no associated application, 
     *         or the associated application fails to be launched.
     */
    public void open(File file) throws LaunchFailedException {
        boolean result = nativeOpenFile(file.toString());
        if (result == false) {
            throw new LaunchFailedException("Failed to launch the associated application with the specified file.");        
        }
    }
    
    /**
     * Checks if the given file is editable.
     */
    public boolean isEditable(File file) {
        return false;
    }

    /**
     * Launches the associated editor to edit the given file.
     * 
     * @param file the given file to be edited.
     * @throws LaunchFailedException if the given file has no associated editor, 
     *         or the associated editor fails to be launched.
     */
    public void edit(File file) throws LaunchFailedException {
        throw new LaunchFailedException("No application associated with the specified file and verb.");  
    }

    /**
     * Checks if the given file is printable.
     */
    public boolean isPrintable(File file) {
        return false;
    }

    /**
     * Prints the given file.
     * 
     * @param file the given file to be printed.
     * @throws LaunchFailedException if the given file has no associated application, 
     *         or the associated application fails to be launched.
     */
    public void print(File file) throws LaunchFailedException {
        throw new LaunchFailedException("No application associated with the specified file and verb.");        
    }
    
    private native boolean nativeOpenFile(String filePath);
}
