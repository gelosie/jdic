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

import org.jdesktop.jdic.fileutil.FileProperties;

/**
 * Windows specific File properties.
 * 
 * @author Fábio Castilho Martins
 *
 */
public class Win32FileProperties implements FileProperties {
	
	/**
	 * Returns true if the file/directory is marked for backup/removal.
	 * @throws IOException
	*/
	public boolean isArchive(File file) throws IOException {
		return Win32NativeFileUtil.isArchive(file.getCanonicalPath());		
	}
	
	/**
	 * Returns true if the file/directory is marked normal (no other properties set).
	 * @throws IOException
	*/
	public boolean isNormal(File file) throws IOException {
		return Win32NativeFileUtil.isNormal(file.getCanonicalPath());		
	}
	
	/**
	 * Returns true if the file/directory is marked read only. For a file, 
	 * it can't be wrote or deleted, for a directory, it can't be deleted.
	 * @throws IOException
	*/
	public boolean isReadOnly(File file) throws IOException {
		return Win32NativeFileUtil.isReadOnly(file.getCanonicalPath());
	}
	
	/**
	 * Returns true if the file/directory is used by the operating system.
	 * @throws IOException
	*/
	public boolean isSystem(File file) throws IOException {
		return Win32NativeFileUtil.isSystem(file.getCanonicalPath());
	}
	
	/**
	 * Returns true if the file/directory is temporary.
	 * @throws IOException
	*/
	public boolean isTemporary(File file) throws IOException {
		return Win32NativeFileUtil.isTemporary(file.getCanonicalPath());
	}
	
	/**
	 * Returns true if the file/directory data is compressed.
	 * @throws IOException
	*/
	public boolean isCompressed(File file) throws IOException {
		return Win32NativeFileUtil.isCompressed(file.getCanonicalPath());
	}
	
	/**
	 * Returns true if the file/directory data is encrypted.
	 * @throws IOException
	*/
	public boolean isEncrypted(File file) throws IOException {
		return Win32NativeFileUtil.isEncrypted(file.getCanonicalPath());
	}
	
	/**
	 * Marks the file/directory for backup/removal.
	 * @return true if successful, false otherwise.
	 * @throws IOException
	*/
	public boolean setArchive(File file, boolean status) throws IOException {
		return Win32NativeFileUtil.setArchive(file.getCanonicalPath(), status);		
	}
	
	/**
	 * Sets all other file/directory properties (except compressed and encrypted) false.
	 * @return true if successful, false otherwise.
	 * @throws IOException
	*/
	public boolean setNormal(File file) throws IOException {
		return Win32NativeFileUtil.setNormal(file.getCanonicalPath());		
	}
	
	/**
	 * Marks the file/directory for use by the operating system.
	 * @return true if successful, false otherwise.
	 * @throws IOException
	*/
	public boolean setSystem(File file, boolean status) throws IOException {
		return Win32NativeFileUtil.setSystem(file.getCanonicalPath(), status);
	}
	
	/**
	 * Marks the file/directory for temporary use.
	 * @return true if successful, false otherwise.
	 * @throws IOException
	*/
	public boolean setTemporary(File file, boolean status) throws IOException {
		return Win32NativeFileUtil.setTemporary(file.getCanonicalPath(), status);
	}
	
	/**
	 * Hides the file/directory. The file/directory will no be seen in an directory listing.
	 * @return true if successful, false otherwise.
	 * @throws IOException
	*/
	public boolean setHidden(File file, boolean status) throws IOException {
		return Win32NativeFileUtil.setHidden(file.getCanonicalPath(), status);
	}
		
}
