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
 
package org.jdesktop.jdic.desktop.internal;

import java.io.File;


/**
 * The <code>LaunchService</code> interface provides opening, editing or printing the given file 
 * by launching the associated application.
 */
public interface LaunchService {
    /**
     * Opens the given file by launching the associated application.
     * 
     * @param file the given file.
     * @throws LaunchFailedException if the given file has no associated application,
     *         or the associated application fails to be launched.
     */
    public void open(File file) throws LaunchFailedException;
  
    /**
     * Checks if the given file is editable.
     *
     * @param file the given file.
     * @return <code>true</code> if the given file has no associated editor; <code>
     *         false</code> otherwise.
     */
    public boolean isEditable(File file);
    
    /**
     * Launches the associated editor to edit the given file.
     * 
     * @param file the given file.
     * @throws LaunchFailedException if the given file has no associated editor,
     *         or the associated editor fails to be launched.
     */
    public void edit(File file) throws LaunchFailedException;

    /**
     * Checks if the given file is printable.
     *
     * @param file the given file.
     * @return <code>true</code> if the given file is printable; <code>false</code> otherwise.
     */
    public boolean isPrintable(File file);

    /**
     * Prints the given file.
     * 
     * @param file the given file.
     * @throws LaunchFailedException if the given file is not printable, or fails to be printed.
     */
    public void print(File file) throws LaunchFailedException;
    
    /** 
     * Resolves the link file if the given file is a link file or symbol file.
     * <p> 
     * This method will get the target/referenced file path if the given file is a link/shortcut
     * file; or get the absolute path if the given file is in a relative path.
     * 
     * @param file the given file.
     * @return the resolved file.
     */
    public File resolveLinkFile(File file);
}
