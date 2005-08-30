/*
 * Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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
import java.util.NoSuchElementException;

/**
 * @author Fábio Castilho Martins *  
 */
public interface FileIterator {
		
	/**
     * Returns <tt>true</tt> if next would return a File object in the directory.
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
	public boolean hasNext() throws IOException;
	
	/**
     * Returns the next File in the directory.
     *
     * @return the next File in the directory.
     * @exception NoSuchElementException if there are no more File objects in the directory.
     */
	public File next() throws NoSuchElementException;

	/**
     * Closes the FileIterator object, releasing resources immediately.
     * 
     * The FileIterator is automatically closed when hasNext returns false 
     * or when the object is garbage collected.
     */
	public void close();
	
}
