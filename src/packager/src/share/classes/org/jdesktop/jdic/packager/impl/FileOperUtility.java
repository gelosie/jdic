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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;
import java.net.URL;

import com.sun.deploy.net.proxy.DeployProxySelector;
import com.sun.deploy.net.proxy.StaticProxyManager;
import com.sun.deploy.services.ServiceManager;
import com.sun.deploy.services.PlatformType;
import com.sun.javaws.jnl.ExtensionDesc;
import com.sun.javaws.jnl.IconDesc;
import com.sun.javaws.jnl.InformationDesc;
import com.sun.javaws.jnl.JARDesc;
import com.sun.javaws.jnl.JREDesc;
import com.sun.javaws.jnl.LaunchDesc;
import com.sun.javaws.jnl.LaunchDescFactory;
import com.sun.javaws.jnl.PackageDesc;
import com.sun.javaws.jnl.PropertyDesc;
import com.sun.javaws.jnl.ResourceVisitor;
import com.sun.javaws.jnl.ResourcesDesc;

/**
 * This class contains some Utilities related to File Operation.
 */
public class FileOperUtility {
    static {
        if (System.getProperty("os.name").indexOf("Windows") != -1) {
            ServiceManager.setService(PlatformType.STANDALONE_TIGER_WIN32);
        } else {
            ServiceManager.setService(PlatformType.STANDALONE_TIGER_UNIX);
        }
        
        try {
            DeployProxySelector.reset();
        } catch (Throwable t) {
            StaticProxyManager.reset();
        }   
    }
    
    /**
     * copy remote file pointed by url to a local directory 
     * 
     * @param url points to a remote file
     * @param codebase remote codebase
     * @param localbase local directory where store the file
     * @throws IOException
     */
    public static void urlFile2LocalFile(URL url, URL codebase,
            String localbase) throws IOException {
        if (url == null || url.getFile() == null || url.getFile().length() <= 0)
            return;
        String relPath = getRelativePath(url.toString(), codebase.toString());
        File localFile = new File(localbase + File.separator + relPath);
        copyRemoteFile(url, localFile);
    }
    
    private static void createLocalFile(File localFile) throws IOException {
        if (localFile == null) 
            return;
        
        if (!localFile.getParentFile().exists()) {
            try {
                 if (!localFile.getParentFile().mkdirs()) {
                     throw new IOException("Cannot make parent directory " +
                           "when trying to create local file"); 
                 }
            } catch (Exception e) {
                 throw new IOException("Cannot make parent directory when " +
                       "trying to create local file: " + e.getMessage());  
            }
        }
       
        if (localFile.exists()) {
            try {
                if (!localFile.delete()) {
                     throw new IOException("Cannot delete original file when " +
                            "trying to create local file");   
                }
            } catch (Exception e) {
                throw new IOException("Cannot delete original file when " +
                        "trying to create local file: " + e.getMessage());    
            }
        }

        try {
            if (!localFile.createNewFile()) {
                throw new IOException("Cannot create new local file");   
            }
        } catch (Exception e) {
            throw new IOException("Cannot create new local file: " +
                    e.getMessage());   
        }
    }
    
    private static void copyRemoteFile(URL url, File localFile)
            throws IOException {
        if (url == null || localFile == null) 
            return;
        
        if (url.getFile() == "" || url.getFile() == null) 
            return;
        
        if (localFile.isDirectory()) {
            localFile = new File(localFile.getPath() + File.separator + 
                    url.getFile());
        }
        
        if (!localFile.exists()) {
            createLocalFile(localFile);   
        }
     
        DataInputStream inStream = new DataInputStream(url.openStream());
        DataOutputStream outStream = new DataOutputStream(
                new FileOutputStream(localFile));

        copyStream(inStream, outStream);
    }
    
    private static void copyStream(InputStream inStream, 
    		OutputStream outStream) throws IOException {
        int readbytes = 0;
                                
        try {
            do {
                byte[] buffer = new byte[512];
                readbytes = inStream.read(buffer, 0, 512);
                if (readbytes <= 0) {
                    break;
                }
                
                outStream.write(buffer, 0, readbytes);
                outStream.flush(); 
            } while (true);   
        } catch (IOException ioE) {
        	ioE.printStackTrace();
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }
    }
    
    /**
     * get relative path according the give path and base
     * 
     * @param path 
     * @param base
     * @return relative path
     */
    public static String getRelativePath(String path, String base) {
        if (path == null || base == null) 
            return null;
        // On Windows Plaform, change all the path string to lower case
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
            path = path.toLowerCase();
            base = base.toLowerCase();
        }
        if (path.lastIndexOf(base) < 0) {
            int index = path.lastIndexOf("/");
        	if (index < 0) {
        		return path;
        	} else {
        		return path.substring(index + 1);
        	}
        }
        String relPath = path.substring(path.lastIndexOf(base) + base.length());
        StringTokenizer st = new StringTokenizer(relPath, "/", false);
        String nativeRelPath = "";
        while (st.hasMoreTokens()) {
            if (nativeRelPath.length() == 0) {
                nativeRelPath = st.nextToken();
            } else {
                nativeRelPath += File.separator + st.nextToken();
            }
        }
        
        return nativeRelPath;
    }
    
    /**
     * copy source file to dest
     * 
     * @param sourceFileName name of the source, could be either directory or a file
     * @param destFileName name of the dest, could be either a file or a directory
     * @throws IOException
     */
    public static void copyLocalFile(String sourceFileName, String destFileName)
            throws IOException {
        File sourceFile = new File(sourceFileName);
        File destFile = new File(destFileName);
        
        if (!sourceFile.exists()) 
            return;
        
        /* return if source and dest are pointing at the same path */
        if (destFile.exists() && sourceFile.getPath().
        		equals(destFile.getPath())) {
        	return;
        }
        
        if (sourceFile.isFile()) { 
            if (destFile.isDirectory()) {
                destFile = new File(destFile.getPath() + File.separator +
                        sourceFile.getName());   
            }
            createLocalFile(destFile);

            DataInputStream inStream = new DataInputStream(
                    new FileInputStream(sourceFile));
            DataOutputStream outStream = new DataOutputStream(
                    new FileOutputStream(destFile));
            
            copyStream(inStream, outStream);
        } else if (sourceFile.isDirectory()) {
            if (destFile.exists() && !destFile.isDirectory()) {
                throw new IOException("cannot copy directory because there " +
                        "is a file with the same name in destination: " +
                        destFile.getPath());   
            }
            
            try {
                if (!destFile.exists() && !destFile.mkdirs()) {
                    throw new IOException("cannot create local directory: " + 
                            destFile.getPath());   
                }
                
                File[] filelist = sourceFile.listFiles();
                if (filelist == null)
                    return;
                for (int i = 0; i < filelist.length; i++) {
                	if (filelist[i].getPath().equals(destFile.getPath())) {
                		return;
                	}
                    String destName = destFileName + File.separator +
                            getRelativePath(filelist[i].getCanonicalPath(),
                                    sourceFile.getCanonicalPath());
                    copyLocalFile(filelist[i].getCanonicalPath(), destName);
                }
            } catch (Exception e) {
                throw new IOException("Cannot copy local directory: " +
                        e.getMessage());   
            }
        }
    }
    
    /**
     * delete the directory
     * 
     * @param dir directory to be deleted
     * @throws IOException
     */
    public static void deleteDirTree(File dir) throws IOException {
        if (dir == null || !dir.isDirectory())
            return;
        File[] filelist = dir.listFiles();
        try {
            if (filelist == null) {
                if (!dir.delete()) {
                    throw new IOException("Cannot delete directory: " +
                            dir.getPath());
                }
                return;
            }
            for (int i = 0; i < filelist.length; i++) {
                if (filelist[i].isFile()) {
                    if (!filelist[i].delete()) {
                        throw new IOException("Cannot delete file: " +
                                filelist[i].getPath());
                    }
                } else if (filelist[i].isDirectory()) {
                    deleteDirTree(filelist[i]);
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to delete directory: " + 
                    e.getMessage());   
        }
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

            tempDirPath = tempFile.getPath();
            tempFile.delete();
            tempFile = new File(tempDirPath);
            tempFile.mkdirs();
            tempFile.deleteOnExit();
        } catch (IOException e) {
            throw new IOException(
                        "Failed to create a local temp directory: "
                        + tempDirPath);
        }
        return tempDirPath + File.separator;
    }
    
    /**
     * get all the resource to local according to a remote jnlp file
     * 
     * @param jnlp remote jnlp file location
     * @param localBase local directory which stores the resources
     * @return URL points to the local jnlp file
     * @throws IOException
     */
    protected static URL getRemoteResource(URL jnlp, String localBase) 
            throws IOException {
        URL localJnlpUrl = null;
        URL codebase = null;
        try {     
            LaunchDesc laDesc = LaunchDescFactory.buildDescriptor(jnlp);
            codebase = laDesc.getCodebase();
            String jnlpName = jnlp.getFile().substring(
            		jnlp.getFile().lastIndexOf("/") + 1);
            File localJnlpFile = new File(localBase + 
            		File.separator + jnlpName);
            copyRemoteFile(jnlp, localJnlpFile);
 
            if (localJnlpFile == null || !localJnlpFile.exists()) {
                 throw new IOException("Cannot copy remote jnlp file to " +
                        "local");   
            }
            localJnlpUrl = localJnlpFile.toURL();
            
            InformationDesc infoDesc = laDesc.getInformation();
            IconDesc[] iconArray = infoDesc.getIcons();
            for (int i = 0; i < iconArray.length; i++) {
                URL iconURL = iconArray[i].getLocation();
                urlFile2LocalFile(iconURL, codebase, localBase);
            }
            ResourcesDesc reDesc = laDesc.getResources();
            reDesc.visit(
                    new JDICPackagerResourceCopyVisitor(codebase, localBase));
        } catch (Exception e) {
            throw new IOException("Exception when geting remote resource: " +
                    e.getMessage());   
        }
        
        return localJnlpUrl;
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
     * Evaluate if the given directory is readable.
     * 
     * @param filePath the given file to be evaluated
     * @return true is the filePath is readable, otherwise, false 
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
     * get the file name without extension
     * 
     * @param filePath input file
     * @return filename without extension
     */
    public static String getFileNameWithoutExt(File filePath) {
        if (filePath == null)
            return null;
        
        String fileName = filePath.getName();
        if (fileName == null)
            return null;
        
        return (fileName.substring(0, fileName.lastIndexOf(".")));
    }
}

/**
 * Resource Visitor to copy all the remote resource files to the local base
 */ 

class JDICPackagerResourceCopyVisitor implements ResourceVisitor {
    private URL codebase = null; 
    private String localBase = null;

    public JDICPackagerResourceCopyVisitor(URL incodebase, String inlocalbase) {
        codebase = incodebase;
        localBase = inlocalbase;
    }
    public void visitJARDesc(JARDesc jad) {
        try {
            FileOperUtility.urlFile2LocalFile(jad.getLocation(),
                    codebase, localBase);
        } catch (IOException ioE) {
            ioE.printStackTrace();   
        }
    }
    
    public void visitExtensionDesc(ExtensionDesc ed) {
        try {
/*
            FileOperUtility.urlFile2LocalFile(ed.getLocation(),
                    codebase, localBase);
*/
            FileOperUtility.getRemoteResource(ed.getLocation(),
                    localBase);
        } catch (IOException ioE) {
            ioE.printStackTrace();   
        }
    }
    
    public void visitPropertyDesc(PropertyDesc prd) {}
    
    public void visitJREDesc(JREDesc jrd) {}
    
    public void visitPackageDesc(PackageDesc pad) {}
}
