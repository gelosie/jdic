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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * This class provides support for utility functions of File operation.
 */
public class FileOperUtility {
    /**
     * Copy the remote file with relative path to a given base url
     * to a local specified path.
     *
     * @param baseUrl the base url for the specified relative file path.
     * @param relativeFilePath the relative file path.
     * @param baseLocalPath the local base file path for the specified file path
     * @throws IOException If the copy failed.
     */
    public static void urlFile2LocalFile(
        URL baseUrl,
        String relativeFilePath,
        String baseLocalPath)
        throws IOException {
        // Get the absolute url path to the specific file.
        URL urlFilePath = null;
        try {
            // Make sure the given baseUrl ends with a "/".
            URL newBaseUrl = null;
            String baseUrlStr = baseUrl.toString();
            if (!baseUrlStr.endsWith("/")) {
                newBaseUrl = new URL(baseUrlStr.concat("/"));
            } else {
                newBaseUrl = baseUrl;
            }

            urlFilePath = new URL(newBaseUrl, relativeFilePath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        // Get the absolute local path to the specific file.
        File localFilePath = new File(baseLocalPath, relativeFilePath);
        // Check if the parent dir of the specific file exists and is writable.
        File parentDir = localFilePath.getParentFile();
        if (!parentDir.exists()) {
            // Create the parent dir with all non-existent ancestor directories
            // are automatically created.
            boolean mkdirSucceed = parentDir.mkdirs();
            if (!mkdirSucceed) {
                throw new IOException(
                    "Failed to create local directory: " + parentDir);
            }
        } else {
            // Check it the parent dir is writable.
            if (!parentDir.canWrite()) {
                throw new IOException(
                    "The local directory is not writable: " + parentDir);
            }

            if (localFilePath.exists()) {
                // The local file already exists, delete it first.
                boolean deleteSucceed = localFilePath.delete();
                if (!deleteSucceed) {
                    throw new IOException(
                        "Failed to remove the already existed local file: "
                        + localFilePath);
                }
            }
        }

        // Create the new local file.
        try {
            boolean createSucceed = localFilePath.createNewFile();
            if (!createSucceed) {
                throw new IOException(
                    "Failed to create local file: " + localFilePath);
            }
        } catch (IOException e) {
            throw new IOException(
                "Failed to create local file: " + localFilePath);
        }

        // Until now, everything is ok; copy the file from url to local.
        DataInputStream inStream = null;
        DataOutputStream outStream = null;
        try {
            inStream = new DataInputStream(urlFilePath.openStream());
            outStream = new DataOutputStream(
                new FileOutputStream(localFilePath));
            byte[] byteBuffer = new byte[
                                JnlpConstants.FILE_COPY_BLOCK_SIZE];
            int bytesRead;

            while (true) {
                bytesRead = inStream.read(byteBuffer);
                if (bytesRead == -1) {
                    break;
                }
                outStream.write(byteBuffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException(
                "Failed to copy file: "
                + urlFilePath
                + " to file: "
                + localFilePath);
        } finally {
            // No matter what happens, always close streams we've already opened
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
                        "Failed to close the output stream during file copy.");
                }
            }
        }
    }

    /**
     * Copy the file given by a href into a local specified path.
     *
     * @param fileHref The give file href.
     * @param localPath The local path for the destination file.
     * @throws IOException If fileHref is a invalid href.
     */
    public static void urlFile2LocalFile(
            URL fileHref, String localPath) throws IOException {
        String fileHrefStr = fileHref.toString();
        int index = fileHrefStr.lastIndexOf('/');

        URL fileHrefBase = null;
        try {
            fileHrefBase = new URL(fileHrefStr.substring(0, index));
        } catch (MalformedURLException e) {
            throw new IOException(
                "The given url doesn't point to a file path: " + fileHref);
        }

        String fileName = fileHrefStr.substring(
                                index + 1, fileHrefStr.length());

        urlFile2LocalFile(fileHrefBase, fileName, localPath);
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

        if (!FileOperUtility.isFileReadable(sourceFile)) {
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
            byte[] byteBuffer = new byte[JnlpConstants.FILE_COPY_BLOCK_SIZE];
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

    /**
     * Copy the given file with a relative path from the source parent path to
     * the dest parent path.
     * @param baseSrcParentDirPath The given source parent directory.
     * @param relativeFilePath The given relevant file path.
     * @param baseDestParentDirPath The given desination parent directory.
     * @throws IOException If the file copy process failed.
     */
    public static void copyFile(String baseSrcParentDirPath,
                                String relativeFilePath,
                                String baseDestParentDirPath)
        throws IOException {
        File sourceFile = new File(baseSrcParentDirPath, relativeFilePath);
        File destFile = new File(baseDestParentDirPath, relativeFilePath);
        copyFile(sourceFile.toString(), destFile.toString());
    }

    /**
     * Copy the jnlp file and its referenced resource files into a temporarily
     * directory.
     * @param pkgInfo              The parsed jnlp file information.
     * @param tmpResourceDirPath   The temporary resource directory path.
     * @throws IOException         If the copy process failed.
     */
    public static void copyJnlpFiles(JnlpPackageInfo pkgInfo,
                                     String tmpResourceDirPath)
                                     throws IOException {
        File tmpResourcePathFile = new File(tmpResourceDirPath);
        if (tmpResourcePathFile.isDirectory()) {
            FileOperUtility.deleteDirTree(tmpResourcePathFile);
        }

        boolean createSucceed = tmpResourcePathFile.mkdirs();
        if (!createSucceed) {
            throw new IOException(
                    "Failed to create the temp directory to copy jnlp files: "
                    + tmpResourceDirPath);
        }

        String jnlpFilePath = pkgInfo.getJnlpFilePath();
        File jnlpFile = new File(jnlpFilePath);
        try {
            // Copy the jnlp file itself first.
            FileOperUtility.copyFile(jnlpFile.getParent(),
                                     pkgInfo.getJnlpFileName(),
                                     tmpResourceDirPath);

            // Copy the jnlp referenced resource files.
            Iterator jnlpRefFilePaths = pkgInfo.getJnlpRefFilePaths();
            String resourcePath = pkgInfo.getResourceDirPath();
            while (jnlpRefFilePaths.hasNext()) {
                String oneRelFilePath = (String) jnlpRefFilePaths.next();
                if (oneRelFilePath != null) {
                    FileOperUtility.copyFile(resourcePath,
                                             oneRelFilePath,
                                             tmpResourceDirPath);
                }
            }
        } catch (IOException e) {
            throw new IOException(
                        "Failed to copy jnlp files into the temp directory: "
                        + tmpResourceDirPath);
        }
   }

   /**
    * Copy the specified copyright file(s) into a temporarily directory.
    * @param pkgInfo    The parsed jnlp file information.
    * @param tmpCopyrightDirPath The given temporary directory.
    * @throws IOException If the copy process failed.
    */
   public static void copyCopyrightFiles(JnlpPackageInfo pkgInfo,
                                         String tmpCopyrightDirPath)
                                        throws IOException {
       File tmpCopyrightPathFile = new File(tmpCopyrightDirPath);
       if (tmpCopyrightPathFile.isDirectory()) {
           FileOperUtility.deleteDirTree(tmpCopyrightPathFile);
       }

       // The temp destination path for the copyright files are:
       //   <tmpCopyrightPath>/licenses/<vendor>/<name>/<version>.<release>
       String copyrightRelativePath =
                "licenses"
                + File.separator
                + pkgInfo.getLocalizedJnlpInfo("en",
                                    JnlpConstants.JNLP_FIELD_VENDOR)
                + File.separator
                + pkgInfo.getPackageName()
                + File.separator
                + pkgInfo.getVersion() + '.' + pkgInfo.getRelease();

       // Remove the " " characters from the path string, which is illegal in
       // the prototype file.
       copyrightRelativePath = copyrightRelativePath.replaceAll(" ", "");
       File tmpCompletePathFile = new File(tmpCopyrightPathFile,
                                           copyrightRelativePath);
       boolean createSucceed = tmpCompletePathFile.mkdirs();
       if (!createSucceed) {
           throw new IOException(
               "Failed to create the temp directory to copy copyright file(s): "
               + tmpCompletePathFile);
       }

       // Copy all the license files from the input license path to the
       // created temporarily directory.
       String licenseFilePath = pkgInfo.getLicenseDirPath();
       try {
           for (int locNum = 0; locNum < JnlpConstants.LOCALES.length; locNum++)
           {
               String curLicenseFileName = "LICENSE."
                                           + JnlpConstants.LOCALES[locNum];
               File curLicenseFile = new File(licenseFilePath
                                              + File.separator
                                              + curLicenseFileName);

               if (FileOperUtility.isFileReadable(curLicenseFile)) {
                   FileOperUtility.copyFile(licenseFilePath, curLicenseFileName,
                       tmpCompletePathFile.toString());
               }
           }
       } catch (IOException e) {
           throw new IOException(
                    "Failed to copy license files into the temp directory: "
                    + tmpCopyrightDirPath);
       }
   }

    /**
     * Evaluate is the given file is readable.
     * @param filePath The given file path.
     * @return true If file is not a directory and is readable.
     */
    public static boolean isFileReadable(File filePath) {
        if (filePath.isFile()) {
            if (filePath.canRead()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluate if the given directory is readable.
     * @param dirPath The given directory.
     * @return true If the given file is a valid directory and is readable.
     */
    public static boolean isDirectoryReadable(File dirPath) {
        if (dirPath.isDirectory()) {
            if (dirPath.canRead()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Evaluate if the given directory is writable.
     * @param dirPath The given direcotry to be evaluated.
     * @return true If the given file is a valid directory and is writableable.
     */
    public static boolean isDirectoryWritable(File dirPath) {
        if (dirPath.isDirectory()) {
            if (dirPath.canWrite()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Delete a file or non-empty directory.
     * @param filePath The should-be-delete path in FILE format.
     */
    public static void deleteDirTree(File filePath) {
        if (filePath.isDirectory()) {
            File[] files = filePath.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteDirTree(files[i]);
            }
            
            filePath.delete();
        } else if (filePath.exists()) {
            filePath.delete();
        }
    }

    /**
     * Gets the file name without the extension by the given file path.
     * @param filePath The given file path.
     * @return The file name without the extension
     */
    public static String getFileNameWithoutExt(File filePath) {
        String fileNameWithoutExt = null;
        String fileNameWithExt = filePath.getName();

        if (fileNameWithExt != null) {
            int index = fileNameWithExt.lastIndexOf('.');
            if (index != -1 && index != fileNameWithExt.length()) {
                fileNameWithoutExt = fileNameWithExt.substring(0, index);
            }
        }

        return fileNameWithoutExt;
    }

    /**
     * Creates an unique local temp directory.
     * @return The name of the created directory.
     * @throws IOException If failed to create such a directory.
     */
    public static String createUniqueTmpDir()
        throws IOException {
        // Create a unique temp directory.
        String tempDirPath = null;
        try {
            File sysTempDir = new File(System.getProperty("java.io.tmpdir"));

            File tempFile = File.createTempFile("jnlp", "jnlp", sysTempDir);
            tempDirPath = FileOperUtility.formatPath(tempFile.toString());

            tempFile.delete();
            tempFile = new File(tempDirPath);
            tempFile.mkdirs();
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new IOException(
                        "Failed to create a local temp directory: "
                        + tempDirPath);
        }
        return tempDirPath;
    }

    /**
     * Copy the http jnlp file into the given local directory.
     * @param httpJnlp The given http jnlp file.
     * @param baseLocalDir The given location to copy the remote jnlp file.
     * @return The locale jnlp file copy.
     * @throws IOException If failed to copy the remote jnlp file.
     */
    public static File httpJnlp2localJnlp(URL httpJnlp, String baseLocalDir)
        throws IOException {
        String httpJnlpStr = httpJnlp.toString();
        // Retrieves the URL jnlp file to the created temp dir.
        String jnlpFileName = null;
        int lastIndex = 0;
        if ((lastIndex = httpJnlpStr.lastIndexOf("/")) != 0) {
            jnlpFileName = httpJnlpStr.substring(lastIndex,
                                                 httpJnlpStr.length());
        }

        File localJnlpFile = new File(baseLocalDir + jnlpFileName);
        try {
            FileOperUtility.urlFile2LocalFile(httpJnlp, baseLocalDir);
        } catch (IOException e) {
            throw new IOException(
                    "Failed to retrieve the http jnlp file locally: "
                    + localJnlpFile);
        }
        return localJnlpFile;
    }

    /**
     * Copy the remote jnlp resource files into locale directory.
     * @param jnlpPkgInfo The parsed jnlp package information.
     * @param baseLocalDir  The given locale directory.
     * @throws IOException If failed to copy these resource files.
     */
    public static void httpJnlpRes2localRes(JnlpPackageInfo jnlpPkgInfo,
                                            String baseLocalDir)
        throws IOException {
        // Get codebase + href as the absolute jnlp file href.
        String jnlpFileHref = jnlpPkgInfo.getJnlpFileHref();
        String codebase =
            jnlpFileHref.substring(0,
                                   jnlpFileHref.lastIndexOf("/") + 1);
        Iterator fileIterator = jnlpPkgInfo.getJnlpRefFilePaths();
        Iterator bakFileIterator = fileIterator;

        fileIterator = bakFileIterator;
        while (fileIterator.hasNext()) {
            String baseLocalFilePath = null;
            URL baseUrl = null;
            String remoteFilePath = null;

            String curHref = (String) fileIterator.next();
            try {
                remoteFilePath = codebase + curHref;
                baseUrl = new URL(remoteFilePath);
                // Get baseLocalFilePath
                curHref = curHref.replace('/', '\\');
                String localFilePath = baseLocalDir + "\\" + curHref;
                baseLocalFilePath =
                localFilePath.substring(0,
                                        localFilePath.lastIndexOf("\\"));
            } catch (MalformedURLException e) {
                throw new IOException(
                          "Failed to construct an URL: " + remoteFilePath);
            }

            // Get the file locally.
            try {
                FileOperUtility.urlFile2LocalFile(baseUrl, baseLocalFilePath);
            } catch (IOException e) {
                throw new IOException(
                        "Failed to retrieve jnlp resource file locally: "
                        + baseUrl);
            }
        }
    }

    /**
     * Format the path String to be ended with "/".
     * @param path The give directory path.
     * @return The formatted path string.
     */
    public static String formatPath(String path) {
        String resultPath = null;
        resultPath = path
                     + (path.endsWith(File.separator) ? "" : File.separator);
        return resultPath;
    }
}