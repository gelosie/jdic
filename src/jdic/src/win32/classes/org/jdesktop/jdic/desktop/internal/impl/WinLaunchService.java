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

import java.io.File;
import java.io.IOException;

import org.jdesktop.jdic.desktop.internal.LaunchFailedException;
import org.jdesktop.jdic.desktop.internal.LaunchService;


/**
 * Concrete implementation of the LaunchService interface for Win.
 *
 * @see     LaunchService
 * @see     GnomeLaunchService
 */
public class WinLaunchService implements LaunchService {
    // The shortcut file suffix on Windows. 
    private static final String LINK_FILE_SUFFIX = ".lnk";
    
    /** 
     * Checks if the given file is a shortcut.
     */
    private boolean isLinkFile(File file) {
        boolean result;

        return ((file.getPath()).toLowerCase() ).endsWith(LINK_FILE_SUFFIX);
    }
    
    /** 
     * Resolve the link file if the given file is a link file or symbol file.
     * 
     * @param inputFile the given file to be resolved.
     * @return the resolved file.
     */
    public File resolveLinkFile(File inputFile) {
        // Get the absolute file path if it's in relative path.
        File resolvedFile = inputFile;
        
        try {
            resolvedFile = inputFile.getCanonicalFile();
        } catch (IOException e) {
        }
        
        if (isLinkFile(resolvedFile)) {
            String fileStr = resolvedFile.toString();
            String targetFileStr = WinAPIWrapper.WinResolveLinkFile(fileStr);
            if (targetFileStr != null) {
                resolvedFile = new File(targetFileStr);
            }
        } 
            
        return resolvedFile;
    }
       
    /**
     * Invokes the system default file handler application to open the given file.
     * 
     * @param file the given file to be opened.
     * @throws LaunchFailedException if the given file has no associated application, or
     *         the associated application fails to be launched.
     */
    public void open(File file) throws LaunchFailedException {
        if(file.isDirectory()) {
            if( !WinAPIWrapper.WinShellExecute(file.toString(), DesktopConstants.VERB_OPEN))
                throw new LaunchFailedException("Failed to open the given directory.");
            return;
        }
    	boolean findOpenNew = false;
    	//First check if we could find command for verb opennew
    	String appCommand = WinUtility.getVerbCommand(file, DesktopConstants.VERB_OPENNEW);
    	if (appCommand != null) {
    		findOpenNew = true;
    	} else {
    		//If no opennew verb command find, then check open verb command
			appCommand = WinUtility.getVerbCommand(file, DesktopConstants.VERB_OPEN);
    	}
    	if (appCommand != null) {
    		boolean result;
    		if (findOpenNew) {
    			//If there is opennew verb command, use this one
				result = WinAPIWrapper.WinShellExecute(file.toString(), DesktopConstants.VERB_OPENNEW);
    		} else {
    			//else use open verb command
				result = WinAPIWrapper.WinShellExecute(file.toString(), DesktopConstants.VERB_OPEN);
    		}
			if (!result) {
				throw new LaunchFailedException("Failed to launch the associationed application");
			}
    	} else {
			throw new LaunchFailedException("No application associated with the specified file");
    	}
    }
    /**
     * Checks if the give file is printable.
     * 
     * @param file The given file
     * @return true if the file is printable
     */
    public boolean isEditable(File file) {
		String verbCommand = WinUtility.getVerbCommand(file, DesktopConstants.VERB_EDIT);
        return (verbCommand != null) ? true : false;
    }

	/**
     * Launches the relevant application to edit the given file.
     * 
     * @param file the given file to be edited.
     * @throws LaunchFailedException if the given file has no associated editor, or
     *         the associated editor fails to be launched.
     */
    public void edit(File file) throws LaunchFailedException {
    	if (isEditable(file)){
			boolean result = WinAPIWrapper.WinShellExecute(file.toString(), DesktopConstants.VERB_EDIT);
			if (!result) {
				throw new LaunchFailedException("Failed to edit the file.");
			}
    	} else {
			throw new LaunchFailedException("No application associated with the specified file");
    	}
    }
    
    /**
     * Checks if the given file is printable.
     * 
     * @param file The given file.
     * @return true if the given file is printable.
     */
    public boolean isPrintable(File file) {
		String verbCommand = WinUtility.getVerbCommand(file, DesktopConstants.VERB_PRINT);
        return (verbCommand != null) ? true : false;
    }

	/**
     * Launches the relevant application to print the given file.
     * 
     * @param file the given file to be printed.
     * @throws LaunchFailedException if the given file has no associated application, or
     *         the associated application fails to be launched.
     */
    public void print(File file) throws LaunchFailedException {
		if (isPrintable(file)){
			boolean result = WinAPIWrapper.WinShellExecute(file.toString(), DesktopConstants.VERB_PRINT);
			if (!result) {
				throw new LaunchFailedException("Failed to print the file.");
			}
		} else {
			throw new LaunchFailedException("No application associated with the specified file");
		}
    }
}