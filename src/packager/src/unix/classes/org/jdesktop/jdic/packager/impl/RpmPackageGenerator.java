/*
 * Copyright 2003 Contributors. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - The names of its contributors may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Contributor(s):
 *
 */

package org.jdesktop.jdic.packager.impl;

import java.io.IOException;
import java.io.File;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStreamReader;


/**
 * Concrete implementation of interface PackageGenerator for rpm packages 
 * generating on Linux.
 * <pre>
 * The steps to create installable packages are briefly as below:
 *   1. Set macros _topdir to the dir whitch general user has write permissions.
 *   2. Create spec file in _topdir/SPECS to define the process of building rpm package.
 *   3. launch rpmbuild -bb <spec> to actually build the rpm package, the target package
 * would be put at _topdir/RPMS/i386
 * </pre>
 */
public class RpmPackageGenerator implements PackageGenerator {
    private String homePath;
    private String topPath;
    private String buildPath;
    private String rpmsPath;
    private String specsPath;
 
    public RpmPackageGenerator() {
        homePath = System.getProperty("user.home");
        topPath = homePath + "/.rpm/";
        buildPath = homePath + "/.rpm/BUILD/";
        rpmsPath = homePath + "/.rpm/RPMS/";
        specsPath = homePath + "/.rpm/SPECS/";
    }
    
    /*
     * Generates the package according to the generated .spec file.
     */
    public void generatePackage(JnlpPackageInfo pkgInfo) throws IOException {
        File spec = null;
        BufferedReader in = null;
        String[] cmdArray = new String[3];

        try {
            setTopdir(pkgInfo);        
            spec = createSpec(pkgInfo);
            String rpmVersion = getRpmVersion();
            if (rpmVersion != null && rpmVersion.compareToIgnoreCase("4.0") >= 0) 
                cmdArray[0] = "rpmbuild";
            else
                cmdArray[0] = "rpm";
            cmdArray[1] = "-bb";
            cmdArray[2] = spec.getAbsolutePath();
            Process p = Runtime.getRuntime().exec(cmdArray); 
            in = new BufferedReader(
                                    new InputStreamReader(p.getErrorStream()));
            String line;
            while((line = in.readLine()) != null) {
                System.out.println(line);
            }

        } finally {
            if(in != null) 
                in.close();

            /* delete the .rpmmacros file we created in the home directory */            
            File macros = new File(homePath + "/.rpmmacros");
            if (macros.exists()) {
                if (!macros.delete())
                    System.out.println("Cannot delete ~/.rpmmacros");
            }
        }
               
    }

    private String getRpmVersion() throws IOException {
        String[] cmdArray = {"rpm", "--version"};
        BufferedReader in = null;
        try {
            Process p = Runtime.getRuntime().exec(cmdArray);
            in = new BufferedReader(
                                    new InputStreamReader(p.getInputStream()));
            String line = in.readLine();
            if (line != null) {
                line = line.trim();
                return line.substring(line.lastIndexOf(" ")).trim();
            }
            else
                return null;
        } finally {
            if(in != null) 
                in.close();
        }
    }
    
    private File createSpec(JnlpPackageInfo pkgInfo) throws IOException {
        int i = 0;
	
	PrintWriter pw = null;
        String appName = pkgInfo.getPackageName();
        String dest = pkgInfo.getOutputDirPath();
        
	if (dest == null) {
            dest = System.getProperty("user.dir");
            File destDir = new File(dest);
            if (!destDir.canWrite()) 
                throw new IOException("Cannot write to the current dir, please specify the PackagePath property");
        }
/*      String installationPath = pkgInfo.getUniqueTmpDirPath() + "/"; */
        String installationPath = "/tmp/.jnlp/" + appName + "/";
        String resourcePath = pkgInfo.getResourceDirPath() + "/";
        String description = pkgInfo.getLocalizedJnlpInfo(JnlpConstants.LOCALES[0], JnlpConstants.JNLP_FIELD_DESCRIPTION);
        String release = pkgInfo.getRelease();
        String version = pkgInfo.getVersion();
        String license = "NONE";
        String licensePath = pkgInfo.getLicenseDirPath();
        boolean hasLicense = false;
        if (licensePath != null) {
        	hasLicense = true;
        	license = "License files are located at /usr/share/doc/packages/" + appName + "/License";
        }
        String group = "System Environment/Base";
        boolean shortcut = pkgInfo.getShortcutEnabled();
        boolean association = pkgInfo.getAssociationEnabled();
        boolean result = false;
	boolean systemCacheEnabled = pkgInfo.getSystemCacheEnabled();
	
        try {
            /* create spec file in the SPECS directory of topdir */
            File spec = new File(specsPath + appName + ".spec");
            pw = new PrintWriter(new BufferedWriter(new FileWriter(spec)));

            /* create the header of the spec file */
            String input = new String();
            input  = "Summary: " + description + "\n";
	    for(i=1; i<10; i++) {
	        if((description = pkgInfo.getLocalizedJnlpInfo(JnlpConstants.LOCALES[i], JnlpConstants.JNLP_FIELD_DESCRIPTION)) != null) {
		    input += "Summary(" + JnlpConstants.LOCALES[i] + "): " + description + "\n";
		}
	    } 
            input += "name: " + appName + "\n";
            input += "Release: " + release + "\n";
            input += "Version: " + version + "\n";
            input += "License: " + license + "\n";
            input += "Group: " + group + "\n";
            input += "BuildRoot: " + buildPath + "\n";
            input += "\n";
            pw.println(input);

            /* create description */
	    description = pkgInfo.getLocalizedJnlpInfo(JnlpConstants.LOCALES[0], JnlpConstants.JNLP_FIELD_DESCRIPTION);
	    input = "%description\n";
            input += description + "\n";
            input += "\n";
	    for(i=1; i<10; i++) {
	        if((description = pkgInfo.getLocalizedJnlpInfo(JnlpConstants.LOCALES[i], JnlpConstants.JNLP_FIELD_DESCRIPTION)) != null) {
		    input += "%description -l " + JnlpConstants.LOCALES[i] + "\n";
		    input += description + "\n";
		}
	    } 
	    pw.println(input);

        /* copy files to install path in BuildRoot */
        try {
	    FileOperUtility.copyLocalFile(pkgInfo.getResourceDirPath(), buildPath + installationPath);
	    if (hasLicense) 
	    	FileOperUtility.copyLocalFile(licensePath, buildPath + File.separator + "License");
	} catch (IOException ioE) {
            throw new IOException("Cannot copy resource files to BuildRoot: " + ioE.getMessage());
	}	     
	

    /* create pre install script */
    input = "%pre\n";
	String[] checkScript;
	checkScript = JnlpUtility.javawsCheckScript();
	for(i=0; i<checkScript.length; i++) {
	    input += checkScript[i] + "\n";
	}
	input += "echo preinstall finished\n";
	pw.println(input);

			/* create post install script */
			input = "%post\n";
			input += "JNLP_ASSOCIATION=`grep '^[^#]*application/x-java-jnlp-file' /etc/mailcap`\n";
			input += "JAVAWS_PATH=`echo $JNLP_ASSOCIATION | awk -F\\; '{print $2}' | awk '{print $1}'`\n";
			input += "echo JAVAWS_PATH: $JAVAWS_PATH\n";
			input += "$JAVAWS_PATH ";
			if (systemCacheEnabled) 
			    input += "-system ";
			input += "-silent -import ";
			if (shortcut)
				input += "-shortcut ";
			if (association)
				input += "-association ";
			input += "-codebase "
				+ "file://"
				+ installationPath
				+ " "
				+ installationPath
				+ "/"
				+ pkgInfo.getJnlpFileName()
				+ "\n";
			input += "\n";
			pw.println(input);

			/* create pre uninstall script */
		    input = "%preun\n";

		    for(i=0; i<checkScript.length; i++) {
                input += checkScript[i] + "\n";
	        }

			pw.println(input);

			/* create postuninstall script */
			input = "%postun\n";
			input += "JNLP_ASSOCIATION=`grep '^[^#]*application/x-java-jnlp-file' /etc/mailcap`\n";
			input += "JAVAWS_PATH=`echo $JNLP_ASSOCIATION | awk -F\\; '{print $2}' | awk '{print $1}'`\n";
			input += "\n";
			input += "$JAVAWS_PATH ";
			if (systemCacheEnabled) 
			    input += "-system ";
			input += "-silent -uninstall "
			      + pkgInfo.getJnlpFileHref()
			      + "\n";
			input += "\n";
			pw.println(input);

			/* create clean script */
			input = "%clean\n";
			input += "mv " + "`find " + rpmsPath + " -name \"*.rpm\"` " + dest + "\n";
			input += "cd " + topPath + "\n";
			input += "cd ..\n";
			input += "rm -rf " + topPath + "\n";
			input += "\n";
			pw.println(input);

			/* create the files list */
			input = "%files\n";
			input += installationPath + "\n";
			if (hasLicense)
			    input += "%doc License\n";
			pw.println(input);

			pw.close();
			return spec;
		} finally {
            if (pw != null)
			    pw.close();
	    }
    }
    
    private void setTopdir(JnlpPackageInfo pkgInfo) throws IOException {
        PrintWriter pw = null;

        try {
            boolean result = false;

            /* create topdir in users home directory */ 
            File buildDir = new File(buildPath);
            result = buildDir.mkdirs();
            if(result == false) {
                result = buildDir.exists();
                if(result == false)
                    throw new IOException("Cannot create BUILD dir in $HOME directory");
            }
            File installDir = new File(buildPath + pkgInfo.getUniqueTmpDirPath() + "/");
            result = installDir.mkdirs();
            if(result == false) {
                result = installDir.exists();
                if(result == false)
                    throw new IOException("Cannot create install dir in BuildRoot");
            }
            File rpmsDir = new File(rpmsPath);
            result = rpmsDir.mkdirs();
            if(result == false) {
                result = rpmsDir.exists();
                if(result == false)
                    throw new IOException("Cannot create RPMS dir in $HOME directory");
            }
            File specsDir = new File(specsPath);
            result = specsDir.mkdirs();
            if(result == false) {
                result = specsDir.exists();
                if(result == false)
                    throw new IOException("Cannot create SPECS dir in $HOME directory");
            }

            /* modify the .rpmmacros to specify _topdir macros */
            pw = new PrintWriter(
                                 new BufferedWriter(new FileWriter(homePath + "/.rpmmacros")));
            pw.println("%_topdir " + topPath);
            pw.close();
        } finally {
            if(pw != null)
                pw.close();
        }
    }
}
