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
package org.jdesktop.jdic.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Utility class for JDIC initialization.
 * @author Paul Huang
 * @created August 20, 2004
 */
public class InitUtility {
	static {
		System.loadLibrary("jdic");
	}
	
	/**
	 * Gets the value of the environment variable.
	 *
	 * @param envVarName The name of the environment variable.
	 * @return The value of the environment variable.
	 */
	public static native String getEnv(String envVarName);
	
	/**
	 * Sets the environment variable. 
	 *
	 * @param ennVarName The name of the environment variable.
	 * @param envValue The value to be set.
	 */
	public static native void setEnv(String envVarName, String envValue);
    
    /**
     * Append the value to the environment variable.
     *
     * @param envVarName environment variable name.
     * @param appendValue new value to be appended.
     */
	public static void appendEnv(String envVarName, String appendValue) {
        String originalValue = getEnv(envVarName);
        String newValue = appendValue;
        if (originalValue != null) {
            newValue = appendValue.concat(File.pathSeparator).concat(originalValue);
        }
        setEnv(envVarName, newValue);
	}
    
    /**
     * Evaluate is the given file is readable.
     * @param filePath The given file path.
     * @return true If file is not a directory and is readable.
     */
    private static boolean isFileReadable(File filePath) {
        if (filePath.isFile()) {
            if (filePath.canRead()) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Copy the file from source file to the destination file.
     * @param sourceFileName The given source file name.
     * @param destFileName The given destination file name.
     * @throws IOException If the copy failed.
     */
    public static void copyFile(String sourceFileName, String destFileName)
        throws IOException {
        File sourceFile = new File(sourceFileName);
        File destFile = new File(destFileName);

        if (!sourceFile.isDirectory()) {

        if (!isFileReadable(sourceFile)) {
            throw new IOException(
                "Copy file failed. The source file is not readable: "
                + sourceFile);
        }

        // Create the parent dir with all non-existent ancestor directories
        // are automatically created.
        File destParentFile = destFile.getParentFile();
        if (!destParentFile.exists()) {
            boolean mkdirSucceed = destParentFile.mkdirs();
            if (!mkdirSucceed) {
                throw new IOException(
                    "Failed to create the parent directory for: "
                    + destFileName);
            }
        }

        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(sourceFile);
            outStream = new FileOutputStream(destFile);
            byte[] byteBuffer = new byte[1024];
            int bytesRread;

            while (true) {
                bytesRread = inStream.read(byteBuffer);
                if (bytesRread == -1) {
                    break;
                }
                outStream.write(byteBuffer, 0, bytesRread);
            }
        } catch (IOException e) {
            throw new IOException(
                        "Failed to copy file: "
                        + sourceFile
                        + " to file: "
                        + destFileName);
        } finally {
            // No matter what happens, always close streams already opened.
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    throw new IOException(
                        "Failed to close the input stream during file copy.");
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    throw new IOException(
                        "Failed to close the ouput stream during file copy.");
                }
            }
        }
        }
        if (sourceFile.isDirectory()) {
            File[] fileList = sourceFile.listFiles();
            String fileName;

            if (!destFile.exists()) {
                destFile.mkdirs();
            }
            for (int i = 0; i < fileList.length; i++) {
                fileName = fileList[i].getName();
                copyFile(fileList[i].getCanonicalPath(),
                         destFileName + File.separator + fileName);
            }
        }
    }

}
