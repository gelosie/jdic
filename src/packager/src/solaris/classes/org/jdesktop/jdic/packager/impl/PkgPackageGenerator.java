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
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

  
/**
 * Concrete implementation of interface PackageGenerator for pkg packages 
 * generating on Solaris.
 * <pre>
 * The steps to create installable packages are briefly as below:
 *   1. Create "pkginfo" file at the location for the package information files.
 *   2. Create "copyright" file at the location for the package information files. 
 *   3. Create "checkinstall" file at the location for the package information files. 
 *   4. Create "postinstall" file at the location for the package information files.
 *   5. Create "postremove" file at the location for the package information files.
 *   6. Generate 'prototype' file using command 'pkgproto' with the jnlp file, 
 *      jnlp resource files, and the created information files including "pkginfo",
 *      "postinstall" and "postremove".
 *   7. Generate an installable package using command 'pkgmk', with the created prototype file, 
 *      created information files, the given jnlp file, and JNLP referenced resource files.
 * </pre>
 */
public class PkgPackageGenerator implements PackageGenerator {
    // Information files used to generate installable packages in pkg format.
    // These files are generated during the packaging process, and will be
    // deleted automatically after the process finishes.
    private static final String FILE_NAME_PKGINFO = "pkginfo";
    private static final String FILE_NAME_COPYRIGHT = "copyright";
    private static final String FILE_NAME_CHECKINSTALL = "checkinstall";    
    private static final String FILE_NAME_POSTINSTALL = "postinstall";
    private static final String FILE_NAME_POSTREMOVE = "postremove";
    private static final String FILE_NAME_PROTOTYPE = "prototype";
    
    // Required information files to generate a package.
    File pkginfoFile = null;
    File copyrightFile = null;
    File checkinstallFile = null;
    File postinstallFile = null;
    File postremoveFile = null;
    File prototypeFile = null;    

    // Location of the generated JNLP package information files.
    private static String pkgInfoFileLocation = null;

    // Temporarily created directory for JNLP resource files.
    private static String tmpResourcePath = null;

    // Temporarily created directory for copyright/license files.
    private static String tmpCopyrightPath = null;
    
    /**
     * Runs a shell command and return the output string.
     */
    private String runShellCommand(String[] commandStrs) {
        BufferedReader br = null;
        try {      
            Process proc = Runtime.getRuntime().exec(commandStrs);

            // Output messages. 
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String oneLine = null;
            if ((oneLine = br.readLine()) != null) { 
                // ONLY return the first line of the output.
                return oneLine;
            } else {
                return null;
            }
        } catch (IOException e) {
            // If there are exceptions, just return null.
            return null;
        } finally {
            // No matter what happens, always close streams already opened. 
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }        
    }

    /**
     * Creates a new file with the given absolute file path. If it already exists, 
     * remove it first.
     */
    private void createFileIgnoreExistance(File absFilePath) throws IOException {
        // Check if the given file already exists. If it is, delete it first.
        if (absFilePath.exists()) {
            boolean deleteSucceed = absFilePath.delete();
            if (!deleteSucceed) {
                throw new IOException("Failed to remove already existed file: " + absFilePath);
            }
        }
        
        boolean createSucceed = absFilePath.createNewFile();
        if (!createSucceed) {
            throw new IOException("Failed to create new file: " + absFilePath);
        }
    }   

    /**
     * Deletes the temporarily created files and directories.
     */
    private void deleteTempFiles() throws IOException {
        if (pkginfoFile != null) pkginfoFile.delete();
        if (checkinstallFile != null) checkinstallFile.delete();
        if (postinstallFile != null) postinstallFile.delete();
        if (postremoveFile != null) postremoveFile.delete();
        if (prototypeFile != null) prototypeFile.delete();
        if (tmpResourcePath != null) {
            FileOperUtility.deleteDirTree(new File(tmpResourcePath));
        }

        if (tmpCopyrightPath != null) {
            FileOperUtility.deleteDirTree(new File(tmpCopyrightPath));            
        }
    }

    /*
     * Creates pkginfo file by extract the info fields in English locale.
     */
    private void createFilePkginfo(JnlpPackageInfo pkgInfo) throws IOException {
        String packageName = pkgInfo.getPackageName();
        String vendor = pkgInfo.getLocalizedJnlpInfo("en", JnlpConstants.JNLP_FIELD_VENDOR);
        String productInfo = pkgInfo.getLocalizedJnlpInfo("en", JnlpConstants.JNLP_FIELD_TITLE);
        String installationPath = pkgInfo.getUniqueTmpDirPath();
        String versionNum = pkgInfo.getVersion();
        String releaseNum = pkgInfo.getRelease();
        // Version string is like: 1.0,RELEASE 1
        String versionStr = versionNum + ",RELEASE " + releaseNum;

        // Create file pkginfo. If it already exists, delete it first.
        pkginfoFile = new File(pkgInfoFileLocation, FILE_NAME_PKGINFO);
        createFileIgnoreExistance(pkginfoFile);
        
        // Write the package information into the pkginfo file:
        BufferedWriter mBufferWriter = null;
        try {
            mBufferWriter = new BufferedWriter(new FileWriter(pkginfoFile));

            mBufferWriter.write("PKG=" + packageName + "\n");
            mBufferWriter.write("NAME=" + productInfo + "\n");
            mBufferWriter.write("ARCH=sparc" + "\n");
            mBufferWriter.write("CATEGORY=system" + "\n");
            mBufferWriter.write("VERSION=" + versionStr + "\n");
            mBufferWriter.write("BASEDIR=" + installationPath + "\n");
            mBufferWriter.write("PSTAMP=" + vendor + "\n");
            mBufferWriter.write("CLASSES=none" + "\n");
        } catch (IOException e) {
            throw new IOException ("Failed to write into file: " + pkginfoFile);
        } finally {
            // No matter what happens, always close streams already opened. 
            if (mBufferWriter != null) {
                try {
                    mBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /*
     * Creates checkinstall file.
     */
    private void createFileCheckinstall() throws IOException {
        // If it already exists, delete it first.
        checkinstallFile = new File(pkgInfoFileLocation, FILE_NAME_CHECKINSTALL);
        createFileIgnoreExistance(checkinstallFile);

        // Write the Javaws version check script into the postremove file:
        BufferedWriter mBufferWriter = null;
        try {
            mBufferWriter = new BufferedWriter(new FileWriter(checkinstallFile));

            String[] javawsCheckScript = JnlpUtility.javawsCheckScript(); 
            for (int lineNum = 0; lineNum < javawsCheckScript.length; lineNum++ ) {
                mBufferWriter.write(javawsCheckScript[lineNum] + "\n");
            }

            /* Append below lines, which are only needed on Solaris, not on Linux:
                  cat >$1 <<EOB
                  JAVAWS_PATH=${JAVAWS_PATH}
                  EOB
                  exit 0
             */            
            mBufferWriter.write("cat >$1 <<EOB" + "\n");            
            mBufferWriter.write("JAVAWS_PATH=${JAVAWS_PATH}" + "\n");
            mBufferWriter.write("EOB" + "\n");
            mBufferWriter.write("exit 0" + "\n");           
        } catch (IOException e) {
            throw new IOException ("Failed to write info into file: " + checkinstallFile);
        } finally {
            // No matter what happens, always close streams already opened. 
            if (mBufferWriter != null) {
                try {
                    mBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }    
    
    /*
     * Creates postinstall file.
     */
    private void createFilePostinstall(JnlpPackageInfo pkgInfo) throws IOException {
        String installationPath = pkgInfo.getUniqueTmpDirPath();
        String jnlpFileName = pkgInfo.getJnlpFileName();
        boolean shortcutEnabled = pkgInfo.getShortcutEnabled();
        boolean associationEnabled = pkgInfo.getAssociationEnabled();
        boolean sysCacheEnabled = pkgInfo.getSystemCacheEnabled() ;
        
        // Create file postinstall. If it already exists, delete it first.
        postinstallFile = new File(pkgInfoFileLocation, FILE_NAME_POSTINSTALL);
        createFileIgnoreExistance(postinstallFile);
        
        // Write the below info into the postinstall file:
        //   # Launch javaws using below command:
        //   $JAVAWS_PATH -silent -system -import -codebase <codebase> <local location of jnlp file>
        //   exit 0
        BufferedWriter mBufferWriter = null;
        try {
            mBufferWriter = new BufferedWriter(new FileWriter(postinstallFile));

            mBufferWriter.write("# Launch javaws using below command:" + "\n");
            mBufferWriter.write("\n");
            
            // Convert the installation path to url style, which will be used by javaws as codebase.
            File instFile = new File(installationPath);
            URL codebase = instFile.toURL();

            String jnlpFileLocation = null;
            if (installationPath.endsWith(File.separator)) {            
                jnlpFileLocation = installationPath + jnlpFileName;
            } else {
                jnlpFileLocation = installationPath + File.separator + jnlpFileName;
            }

            String javawsCommand = "$JAVAWS_PATH ";
            if (sysCacheEnabled) {
                javawsCommand += "-system ";                
            } 
            javawsCommand += "-silent ";
            if (shortcutEnabled) {
                javawsCommand += "-shortcut ";
            }
            if (associationEnabled) {
                javawsCommand += "-association ";
            }
            javawsCommand += "-import -codebase " + codebase + " " + jnlpFileLocation + "\n";
           
            mBufferWriter.write(javawsCommand);
            
            mBufferWriter.write("\n");
            mBufferWriter.write("exit 0" + "\n");
        } catch (MalformedURLException e) {
            throw new IOException ("Failed to convert the installation path to URL style: " + installationPath);
        } catch (IOException e) {
            throw new IOException ("Failed to write info into file: " + postinstallFile);
        } finally {
            // No matter what happens, always close streams already opened. 
            if (mBufferWriter != null) {
                try {
                    mBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }    

    /*
     * Creates postremove file.
     */
    private void createFilePostremove(JnlpPackageInfo pkgInfo) throws IOException {
        String installationPath = pkgInfo.getUniqueTmpDirPath();
        String jnlpFileHref = pkgInfo.getJnlpFileHref();
        boolean sysCacheEnabled = pkgInfo.getSystemCacheEnabled();
        
        // Create file postremove. If it already exists, delete it first.
        postremoveFile = new File(pkgInfoFileLocation, FILE_NAME_POSTREMOVE);
        createFileIgnoreExistance(postremoveFile);
        
        // Write the below info into the postremove file:
        //   # Launch javaws using below command:
        //   $JAVAWS_PATH -silent -system -uninstall <jnlp_href>
        //   exit 0
        BufferedWriter mBufferWriter = null;
        try {
            mBufferWriter = new BufferedWriter(new FileWriter(postremoveFile));

            mBufferWriter.write("# Launch javaws using below command:" + "\n");
            mBufferWriter.write("\n");

            String javawsCommand = "$JAVAWS_PATH ";
            if (sysCacheEnabled) {
                javawsCommand += "-system ";          
            } 
            javawsCommand += "-silent -uninstall " + jnlpFileHref + "\n";
            mBufferWriter.write(javawsCommand);

            // Delete the created installation path only if it's empty.
            mBufferWriter.write("rmdir $BASEDIR > /dev/null 2>&1" + "\n");

            mBufferWriter.write("\n");
            mBufferWriter.write("exit 0" + "\n");
        } catch (IOException e) {
            throw new IOException ("Failed to write info into file: " + postremoveFile);
        } finally {
            // No matter what happens, always close streams already opened. 
            if (mBufferWriter != null) {
                try {
                    mBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }    
    
    /* 
     * Generates file prototype according to the extracted local files,
     * generated information files.
     */
    private void genFilePrototype(JnlpPackageInfo pkgInfo) throws IOException {
        // Check if the default location for the package information files is writable.
        File locationFile = new File(pkgInfoFileLocation);
        if (!FileOperUtility.isDirectoryWritable(locationFile)) {
            throw new IOException("The default location for the package information files "
                + "is not writable: " + pkgInfoFileLocation);
        }
        
        // The given JNLP resource path may contain files other than the jnlp resource files, 
        // copy the required resource paths to a temp directory before packaging them.
        File tmpResourcePathFile = new File(pkgInfoFileLocation, "." + pkgInfo.getPackageName() + ".");
        tmpResourcePath = tmpResourcePathFile.toString();
        FileOperUtility.copyLocalFile(pkgInfo.getResourceDirPath(), tmpResourcePath);     

        String licenseFilePath = pkgInfo.getLicenseDirPath ();
        if (licenseFilePath != null) {
            // Copy the copyright files into a temp copyright path, since the license file path 
            // may contains files other than the license files.If no specified license path, return.
            File tmpCopyrightPathFile = new File(pkgInfoFileLocation, "." + FILE_NAME_COPYRIGHT + ".");
            tmpCopyrightPath = tmpCopyrightPathFile.toString();
            FileOperUtility.copyLocalFile(pkgInfo.getLicenseDirPath(), tmpCopyrightPath);
        }

        // Create information files.
        try {
            createFilePkginfo(pkgInfo);
            createFileCheckinstall();
            createFilePostinstall(pkgInfo);
            createFilePostremove(pkgInfo);
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        
        // Create prototype file using the JNLP files, and the generated information files.
        // Check if file prototype doesn't exist.
        prototypeFile = new File(pkgInfoFileLocation, FILE_NAME_PROTOTYPE);
        if (prototypeFile.exists()) {
            boolean deleteSucceed = prototypeFile.delete();
            if (!deleteSucceed) {
                throw new IOException("Failed to remove the already existed prototype file at: " + prototypeFile);
            }
        }
        
        // Include the jnlp resource files.
        String genCommand = "pkgproto " + tmpResourcePath + "= ";

        // If the user specified license file, store the file in the local directory:
        //   JnlpConstants.LICENSE_INSTALLATION_BASE_PATH/<vendor>/<name>/<version>.<release>
        if (pkgInfo.getLicenseDirPath() != null) {
            genCommand += tmpCopyrightPath + "=" + "/usr/share/lib/javaws";
        }
        genCommand += " > " + prototypeFile.toString(); 

        // Generate file prototype using pkgproto. 
        Process proc = null;
        try {
            // "/bin/sh -c" is required to interpret shell metacharacters like '>'.
            proc = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", genCommand});
            
            try {
                proc.waitFor();
            } catch (InterruptedException ie) {
            	return;
            }
            
        } catch (IOException e) {
            throw new IOException("Failed to generate prototype file using command: " +
                    genCommand);
        }

        // Up to now, the prototype file is created successfully, but the current user and group 
        // ownerships are used for each entry in the file. But the package might not work when 
        // installed if owned by another user.
        // Refer to http://www.sunfreeware.com/pkgadd.html for more info.
        // So, change the ownship of the entries to bin(user) and bin(group). To be exactly, 
        // change the "userid groupid" in the prototype file to "bin bin".
        try {
            String[] getUserCommandStrs = {
                "/bin/sh", "-c", "echo `id` | awk -F\\( '{print $2}' | awk -F\\) '{print $1}'"};     
            String user = runShellCommand(getUserCommandStrs);
            if (user != null) {
                String[] getGroupCommandStrs = {
                    "/bin/sh", "-c", "echo `id` | awk -F\\( '{print $3}' | awk -F\\) '{print $1}'"};
                String group = runShellCommand(getGroupCommandStrs);    
                
                if (group != null) {
                    // Create a temp prototype file. If it already exists, delete it. 
                    File tempPrototypeFile = new File(prototypeFile.toString() + ".tmp");
                    tempPrototypeFile.delete(); 
                    
                    String[] changePrototypeCommandStrs = {
                        "/bin/sh", "-c", "sed 's/" + user + " " + group + "/bin bin/' " + prototypeFile.toString() + " > " 
                        + tempPrototypeFile.toString()};

                    Runtime.getRuntime().exec(changePrototypeCommandStrs);
                    try {
                        proc.waitFor();
                    } catch (InterruptedException ie) {
                        return;
                    }
                    
                    if (tempPrototypeFile.exists()) {
                        // The temp file is created successfully. Delete the original prototype file 
                        // and rename the generated temp file to prototype.
                        prototypeFile.delete();
                        tempPrototypeFile.renameTo(prototypeFile);
                    }
                }
            }
        } catch (IOException e) {
            // If the above ownships changing operation fails, just ignore it.
        }   
        
        // Edit prototype file to add information file entries. 
        // First check if prototype is created successfully.
        if (!prototypeFile.exists()) {
            throw new IOException("No generated prototype file for generating the installable package.");
        }
        
        // *Append* information file items into prototype file:
        BufferedWriter mBufferWriter = null;
        try {
            mBufferWriter = new BufferedWriter(new FileWriter(prototypeFile, true));

            mBufferWriter.write("i pkginfo=" + pkginfoFile.toString() + "\n");
            String inputLicensePath = pkgInfo.getLicenseDirPath();
            if (inputLicensePath != null) {
                copyrightFile = new File(inputLicensePath, "LICENSE.en");
                mBufferWriter.write("i copyright=" + copyrightFile.toString() + "\n");
            }
  	        mBufferWriter.write("i checkinstall=" + checkinstallFile.toString() + "\n");
            mBufferWriter.write("i postinstall=" + postinstallFile.toString() + "\n");
            mBufferWriter.write("i postremove=" + postremoveFile.toString() + "\n");
        } catch (IOException e) {
            throw new IOException ("Failed to write installation file entries to : " + prototypeFile);
        } finally {
            // No matter what happens, always close streams already opened. 
            if (mBufferWriter != null) {
                try {
                    mBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }    

    /*
     * Generates the package according to the generated prototype, extracted local 
     * temp files, and generated information files.
     */
    public void generatePackage(JnlpPackageInfo pkgInfo) throws IOException {
        // If the package destination path is not specified, use the default spool 
        // directory (/var/spool/pkg).
        String packagePath = pkgInfo.getOutputDirPath();
        if (packagePath == null) {
            packagePath = "/var/spool/pkg";
        }
        File destinationPath = new File(packagePath);
        if (!FileOperUtility.isDirectoryWritable(destinationPath)) {
            throw new IOException("The destination directory for the generated package "
                + "is not writable: " + destinationPath);
        }

        // Use the resource path as the location of the generated package information files.
        String resourcePath = pkgInfo.getResourceDirPath();
        pkgInfoFileLocation = resourcePath;
        
        // Generate the prototype file. 
        try {
            genFilePrototype(pkgInfo);
        } catch (IOException e) {
            // On failure, try to delete all the created files and directories. 
            deleteTempFiles();

            throw new IOException(e.getMessage());
         }
        
        // Generate the package using command:
        //   pkgmk -o -f <prototype file path> -d packagePath
        String genCommand = "pkgmk -o -f " + prototypeFile.toString() + " -d " + packagePath;
        BufferedReader br = null;
        try {      
            Process proc = Runtime.getRuntime().exec(genCommand);

            // Output messages. 
            br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String oneLine = null;
            System.out.println("--- Output messages while generating the PKG package ---");
            while ((oneLine = br.readLine()) != null) { 
                System.out.println(oneLine);
            }

            // Error messages.
            br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while ((oneLine = br.readLine()) != null) { 
                System.out.println(oneLine);
            }
        } catch (IOException e) {
            throw new IOException("Failed to generate an installable package using command: " 
                + genCommand);
        } finally {
            // No matter what happens, always close streams already opened. 
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }

            // No matter what happens, always try to delete all the created temp files. 
            deleteTempFiles();            
        }
    }
}
