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
 * @author Fábio Castilho Martins
 *  
 */
public class Win32FileProperties implements FileProperties {
	
	public boolean isArchive(File file) throws IOException {
		return Win32NativeFileUtil.isArchive(file.getCanonicalPath());		
	}
	
	public boolean isNormal(File file) throws IOException {
		return Win32NativeFileUtil.isNormal(file.getCanonicalPath());		
	}
	
	public boolean isReadOnly(File file) throws IOException {
		return Win32NativeFileUtil.isReadOnly(file.getCanonicalPath());
	}
	
	public boolean isSystem(File file) throws IOException {
		return Win32NativeFileUtil.isSystem(file.getCanonicalPath());
	}
	
	public boolean isTemporary(File file) throws IOException {
		return Win32NativeFileUtil.isTemporary(file.getCanonicalPath());
	}
	
	public boolean isCompressed(File file) throws IOException {
		return Win32NativeFileUtil.isCompressed(file.getCanonicalPath());
	}
	
	public boolean isEncrypted(File file) throws IOException {
		return Win32NativeFileUtil.isEncrypted(file.getCanonicalPath());
	}
	
	public boolean setArchive(File file) throws IOException {
		return Win32NativeFileUtil.setArchive(file.getCanonicalPath());		
	}
	
	public boolean setNormal(File file) throws IOException {
		return Win32NativeFileUtil.setNormal(file.getCanonicalPath());		
	}
	
	public boolean setSystem(File file) throws IOException {
		return Win32NativeFileUtil.setSystem(file.getCanonicalPath());
	}
	
	public boolean setTemporary(File file) throws IOException {
		return Win32NativeFileUtil.setTemporary(file.getCanonicalPath());
	}
	
	public boolean setHidden(File file) throws IOException {
		return Win32NativeFileUtil.setHidden(file.getCanonicalPath());
	}
		
}
