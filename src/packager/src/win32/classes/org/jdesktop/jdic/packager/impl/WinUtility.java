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

package org.jdesktop.jdic.packager.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

/**
 * WinUtility will provide some general utility functions for Windows platform.
 */
public class WinUtility {
    /**
     * Block size for file operation.
     */
    private static final int BUFFER = 2048;
    /**
     * Resource ID for the binary resource.
     */
    public static final int BIN_RES_ID = 104;
    /**
     * Resource ID for the UUID string resource.
     */
    public static final int STRING_RES_UUID_ID = 1600;
    /**
     * Resource ID for the localization flag string resource.
     */
    public static final int STRING_RES_LOCALIZATION_FLAG_ID = 1760;

    /**
     * Checks whether the give file is a valid file.
     *
     * @param filePath The given file path.
     * @throws IOException If the file is not valid.
     */
    public static void checkFileValid(String filePath) throws IOException {
        File file = new File(filePath);
        if (!FileOperUtility.isFileReadable(file)) {
            throw new IOException("The file "
                                  + filePath
                                  + " does not exist or can not be accessed!");
        }
    }

    /**
     * Checks whether the given directory is a valid directory.
     * @param dirPath The given directory path.
     * @throws IOException If the given directory is not a valid directory.
     */
    private static void checkDirValid(String dirPath) throws IOException {
        File dir = new File(dirPath);
        if (!FileOperUtility.isDirectoryReadable(dir)) {
            throw new IOException("The dir "
                                  + dirPath
                                  + " does not exist or can not be accessed!");
        }
    }

    /**
     * Puts one file into the zip file.
     *
     * @param oneFileName The given file name
     * @param oneFilePath The file absolute path
     * @param out The zip file handle
     * @throws IOException If failed to package the file into the zip file.
     */
    private static void putIntoZip(String oneFileName,
                                   String oneFilePath,
                                   ZipOutputStream out) throws IOException  {
        try {
            byte[] data = new byte[BUFFER];
            BufferedInputStream origin = null;
            FileInputStream fi = new FileInputStream(oneFilePath);
            origin = new BufferedInputStream(fi, BUFFER);

            //Loop 1: iterate the file to get the size of the file
            int bytes = 0;
            int count = 0;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                bytes += count; //file size are recoreded into bytes variable
            }
            origin.close();
            fi.close();

            //Loop 2: iterate the file to get the CRC checksum
            fi = new FileInputStream(oneFilePath);
            origin = new BufferedInputStream(fi, BUFFER);
            CheckedInputStream originCheck = new CheckedInputStream(
                                                        origin,
                                                        new CRC32());
            long crcChecksum = 0;   //crc check sum value is stored here
            while ((count = originCheck.read(data, 0, BUFFER)) != -1) {
                ;
            }
            crcChecksum = originCheck.getChecksum().getValue();
            originCheck.close();
            origin.close();
            fi.close();

            fi = new FileInputStream(oneFilePath);
            origin = new BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(oneFileName);
            entry.setMethod(ZipEntry.STORED);
            entry.setSize(bytes);
            entry.setCrc(crcChecksum);
            out.putNextEntry(entry);
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
            out.closeEntry();

        } catch (Exception e) {
            throw new IOException("Failed to package the zip file!");
        }
    }

    /**
     * Create a jar file includs all the file in the given file list.
     *
     * @param jarFilePath The jar file to be created.
     * @param pkgInfo The given jnlp package info.
     *
     * @throws IOException If failed to jar the relevant files.
     */
    public static void jarJnlpFiles(String jarFilePath,
                                    JnlpPackageInfo pkgInfo)
                                    throws IOException {
        try {
            String sourcePath = pkgInfo.getResourceDirPath();
            //Check if the fileList is empty
            Iterator it = pkgInfo.getJnlpRefFilePaths();
            if (it == null) {
                return;
            }
            //Initialize all the input/output stream
            FileOutputStream dest = new FileOutputStream(jarFilePath);
            CheckedOutputStream checksum = new CheckedOutputStream(
                                                    dest,
                                                    new CRC32());
            ZipOutputStream out = new ZipOutputStream(
                                            new BufferedOutputStream(checksum));
            //Set compression rate to be zero: Just package, no deflation.
            out.setLevel(0);
            out.setMethod(ZipOutputStream.STORED);
            //Non-empty file list, loop each file to add into the jar file
            checkDirValid(sourcePath);
            //The first entry is the jnlp file specified in the absolute path,
            //so there would be no need to locate it relevant to the sourcepath.
            boolean isJnlpFile = true;
            while (it.hasNext()) {
                String oneFileName = (String) it.next();
                String oneFilePath = null; 
                if (!isJnlpFile) {
                    oneFilePath = sourcePath
                                  + File.separator
                                  + oneFileName;
                } else {
                    oneFilePath = oneFileName;
                    isJnlpFile = false;
                }
                checkFileValid(oneFilePath);
                putIntoZip(oneFileName, oneFilePath, out);
            }
            //Flush and close the jar package
            out.flush();
            out.close();
            checksum.close();
            dest.close();
        } catch (Exception e) {
            throw new IOException("Failed to create the jar file "
                                  + jarFilePath);
        }
    }

    /**
     * Extract specified source file from the jar package and copy it as the
     * destination file.
     *
     * @param jarFilePath The given jar file.
     * @param sourceFileName The given source file name.
     * @param destFilePath The given destination file path.
     * @throws IOException If failed to extract the specified file from
     *                     the jar file.
     */
     public static void extractFileFromJarFile(String jarFilePath,
                                               String sourceFileName,
                                               String destFilePath)
                                               throws IOException {
        try {
            //Check if the specified jar file is a valid file
            checkFileValid(jarFilePath);

            boolean findSourceFile = false;
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(jarFilePath);
            JarInputStream jis = new JarInputStream(
                                                new BufferedInputStream(fis));
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                String entryName = entry.getName();
                if (entry.getName().equalsIgnoreCase(sourceFileName)) {
                    //Find the source file
                    findSourceFile = true;
                    int count;
                    byte[] data = new byte[BUFFER];
                    //wrtie the files to the disk
                    FileOutputStream fos = new FileOutputStream(destFilePath);
                    dest = new BufferedOutputStream(fos, BUFFER);
                    while ((count = jis.read(data, 0, BUFFER)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.flush();
                    dest.close();
                }
            }
            jis.close();
            fis.close();
            if (!findSourceFile) {
                throw new IOException("The jar file "
                                      + jarFilePath
                                      + " does not contain file "
                                      + sourceFileName);
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Error: Illegal File Name: " + destFilePath);
        } catch (Exception e) {
            throw new IOException("Error extracting data from the jar file!");
        }
    }

    /**
     * Update/Add a string into the executable file resource's string table.
     *
     * @param appFilePath   The given executable file path.
     * @param contentStr    The string to be added.
     * @param resID         The resource ID of the added string.
     *
     * @throws IOException If failed to update the resource string.
     */
    public static void updateResourceString(String appFilePath,
                                             String contentStr,
                                             int resID)
                                             throws IOException {
        checkFileValid(appFilePath);
        WinMsiWrapper.winUpdateResourceString(appFilePath, contentStr, resID);
    }

    /**
     * Update/Add a binary data into the the executable file resource.
     *
     * @param appFilePath   The given executable file path.
     * @param dataFilePath  The file containing the data to be added.
     * @param resID         The resource ID of the added resource.
     *
     * @throws IOException If failed to update the binary resource field.
     */
    public static void updateResourceData(String appFilePath,
                                           String dataFilePath,
                                           int resID)
                                           throws IOException {
        checkFileValid(appFilePath);
        checkFileValid(dataFilePath);
        WinMsiWrapper.winUpdateResourceData(appFilePath, dataFilePath, resID);
    }
}
