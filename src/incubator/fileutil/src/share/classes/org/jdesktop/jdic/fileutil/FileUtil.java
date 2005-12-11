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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.NoSuchElementException;

/**
 * Provides extensions to the <code>java.io.File</code> class.
 * 
 * @author Fábio Castilho Martins
 * @see File
 * @see File#listFiles()
 * @see File#listFiles(java.io.FileFilter)
 * @see File#listFiles(java.io.FilenameFilter)
 */
public class FileUtil {
	
	private NativeFileUtil fileUtil;
	
	private File internalFile;
	
	/**
	 * Creates a new FileUtil instance.
	 * 
	 * @throws UnsupportedOperationException If the method isn't supported in the specific platform.
	 */
	public FileUtil(File file) throws UnsupportedOperationException {
		this.fileUtil = NativeFileUtil.getNativeFileUtil();
		this.internalFile = file;
	}	
	
	private class InnerFileIterator implements FileIterator {
		
		private boolean closed;
		
		private File nextFile;
		
		private File directory;
		
		private boolean firstRead;
		
		private NativeFileUtil internalFileUtil;
		
		public InnerFileIterator() {
			this.internalFileUtil = NativeFileUtil.getNativeFileUtil();
			this.directory = FileUtil.this.internalFile;
			this.firstRead = true;
			this.closed = true;
		}

		public boolean hasNext() throws IOException {
			if(this.directory.isDirectory()) {
				String name;
				if(this.firstRead) {
					name = this.internalFileUtil.readFirst(this.directory.getCanonicalPath());
					this.firstRead = false;
					this.closed = false;
				}
				else {
					name = this.internalFileUtil.readNext();
				}
				
				if(name != null) {
					this.nextFile = new File(this.directory.getCanonicalPath() + name);
					return true;
				}
				else {
					this.nextFile = null;
					this.close(); // to save resources
					return false;
				}
			}
			else {
				return false;
			}
		}
		
		public File next() throws NoSuchElementException {
			if(this.closed) {
				throw new NoSuchElementException();
			}
			else {
				return this.nextFile;
			}
		}

		public void close() {
			if(!closed) {
				this.internalFileUtil.close();
				this.closed = true;
			}
		}

		protected void finalize() throws Throwable {
			this.close();
		}
	}
	
	private class InnerFileFilterIterator extends InnerFileIterator {
		
		private FileFilter filter;
		
		public InnerFileFilterIterator(FileFilter filter) {
			super();
			this.filter = filter;
		}

		public boolean hasNext() throws IOException {
			if(super.hasNext()) {
				if(this.filter.accept(this.next())) {
					return true;
				}
				else {
					return this.hasNext();
				}
			}
			else {
				return false;
			}
		}		
	}
	
	private class InnerFilenameFilterIterator extends InnerFileIterator {
		
		private FilenameFilter filter;
		
		private File directory;
		
		public InnerFilenameFilterIterator(FilenameFilter filter) {
			super();
			this.directory = FileUtil.this.internalFile;
			this.filter = filter;
		}

		public boolean hasNext() throws IOException {
			if(super.hasNext()) {
				if(this.filter.accept(this.directory, this.next().getName())) {
					return true;
				}
				else {
					return this.hasNext();
				}
			}
			else {
				return false;
			}
		}		
	}
    
    /**
     * Sends the file or directory denoted by this abstract pathname to the
     * Recycle Bin/Trash Can.
     * 
     * @return <b>true</b> if and only if the file or directory is
     *         successfully recycled; <b>false</b> otherwise.
     * @throws IOException If an I/O error occurs. 
     * @throws SecurityException If a required system property value cannot be 
     *         accessed.
     * @throws UnsupportedOperationException if the method isn't supported in the specific platform.
     */
    public boolean recycle() throws IOException,
            SecurityException, UnsupportedOperationException {
    	return this.fileUtil.recycle(internalFile);
    }

    /**
     * Return the amount of free bytes available in the directory or file
     * referenced by the File Object.
     * 
     * @return the amount of free space in the partition. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     * @throws UnsupportedOperationException if the method isn't supported in the specific platform.
     */
    public BigInteger getFreeSpace() throws IOException, UnsupportedOperationException {
    	return this.fileUtil.getFreeSpace(internalFile);
    }
    
    /**
     * Return the size in bytes of the partition denoted by the File Object.
     * 
     * @return the size of the partition. The size is wrapped in a
     *         BigInteger due to platform-specific issues.
     * @throws IOException
     * @throws UnsupportedOperationException if the method isn't supported in the specific platform.
     */
    public BigInteger getTotalSpace() throws IOException, UnsupportedOperationException {
    	return this.fileUtil.getTotalSpace(internalFile);
    }
       
    /**
     * Returns a FileIterator object, used to traverse the contents of the directory 
     * denoted by the File argument. The files in this FileIterator will not be in 
     * any particular order. 
     * 
     * @return a FileIterator object.
     */
    public FileIterator listFiles() {
    	return new InnerFileIterator();
    }
    
    /**
     * Returns a FileIterator object, used to traverse the contents of the directory  
     * denoted by the File argument. Only the files that satisfy the given filter will 
     * appear in this FileIterator and they'll not be in any particular order. 
     * 
     * @param filter the filter to aply to the contents of the directory.
     * @return a FileIterator object.
     */
    public FileIterator listFiles(FileFilter filter) {
    	return new InnerFileFilterIterator(filter);
    }
    
    /**
     * Returns a FileIterator object, used to traverse the contents of the directory  
     * denoted by the File argument. Only the files that satisfy the given filter will 
     * appear in this FileIterator and they'll not be in any particular order. 
     * 
     * @param filter the filter to aply to the contents of the directory.
     * @return a FileIterator object.
     */
    public FileIterator listFiles(FilenameFilter filter) {
    	return new InnerFilenameFilterIterator(filter);
    }

}
