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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 *  Initialization manager for JDIC. The application can register which
 *  components it needs with this singleton management object, alter the
 *  defaults if needed, and then call init() to initialize JDIC. <br />
 *  <br />
 *  Platforms and Packages are described by subclasses of the Platform and
 *  Package class, allowing for dynamic extension to support new subsystems or
 *  even non-JDIC components that might require native libraries<br />
 *  <br />
 *  There are 3 modes of operation - WebStart, file system, and JARed.<br />
 *  <br />
 *  When using webstart, please specify a Jar with the native libraries for your
 *  platform to be loaded by webstart in your JNPL. This class will find the unjared native
 *  libraries and use them directly, saving any extra copies. The native Jars
 *  must match the JAR names specified in the Platform subclasses.<br />
 *  <br />
 *  If not in webstart, the system will expect the native libraries to be
 *  located in subdirectories at the root of the classpath or jar containing
 *  this class. The names of the subdirectories must match the names in the
 *  Platform subclasses. If the classpath is a file: URL, then they will be used
 *  directly off the file path. Otherwise they will be copied to a temp
 *  directory and used from there. Note that some of the files may not be
 *  deleted automatically because the files are still in use while Java is
 *  shutting down<br />
 *  <br />
 *  Settings can be altered via the access methods before calling init, allowing
 *  you to alter most of the behavior. The most likely useful scenario would be
 *  to alter the temp directory being used.<br />
 *
 * @author     Michael Samblanet
 * @created    July 29, 2004
 * @todo       Add a new mode to allow non-webstart apps to copy the files from
 *      individual jars instead of from the main jar
 */
public class JdicManager {
	/**  Singleton instance of this class */
	private static JdicManager sSingleton = null;
	/**  The local directory containing the native files */
	private File mBinaryDir;
	/**  If TRUE, files will be copied from mSourceUrl to mBinaryDir on init */
	private boolean mCopyFiles = false;
	/**  If TRUE, copied files will be set to deleteOnExit */
	private boolean mDeleteFiles = false;
	/**  TRUE once the system has initialized */
	private boolean mIsInitialized = false;
	/**  A list describing the packages desired. Packages are added in order. */
	private List mPackages = new ArrayList(5);
	/**  The current system platform */
	private Platform mPlatform = Platform.getPlatform();
	/**  The URL to copy native files from */
	private URL mSourceUrl;


	/**
     * Private constructor to prevent public construction
     * 
     * @exception JdicInitException Description of the Exception
     */
    private JdicManager() throws JdicInitException {
        determineDefaultDirs();

        for (int i = 0; i < Package.MANDATORY_PACKAGES.length; i++) {
            addPackage(Package.MANDATORY_PACKAGES[i]);
        }
    }

    /**
     * @return Singleton instance of the JDIC Manager
     * @exception JdicInitException Generic Initialization exception
     */
    public static synchronized JdicManager getManager()
            throws JdicInitException {
        if (sSingleton == null) {
            sSingleton = new JdicManager();
            sSingleton.init();
            //Initialize all the core packages.
            sSingleton.initPackages();
        }
        return sSingleton;
    }

    /**
     * Copies a file from a URL to a directory
     * 
     * @param src Source File URL
     * @param destDir Destination Directory (file name will be unchanged)
     * @return File reference for the destination file
     * @exception IOException Generic IOException from copy
     */
    private static File copyFileURL(URL src, File destDir) throws IOException {
        String filename = src.getFile();
        int lastSlash = filename.lastIndexOf('/');
        if (lastSlash != -1) {
            filename = filename.substring(lastSlash);
        }

        File newFile = new File(destDir, filename);

        InputStream in = null;
        OutputStream out = null;
        try {
            in = src.openStream();
            out = new FileOutputStream(newFile);
            byte buf[] = new byte[4096];
            int n = 0;
            while ((n = in.read(buf, 0, buf.length)) > 0) {
                out.write(buf, 0, n);
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
        return newFile;
    }

    /**
     * @return The local directory containing all the native files
     */
    public File getBinaryDir() {
        return mBinaryDir;
    }

    /**
     * @return true if the files are to be copied
     */
    public boolean getCopyFiles() {
        return mCopyFiles;
    }

    /**
     * @return The local directory containing all the native files
     */
    public boolean getDeleteFiles() {
        return mDeleteFiles;
    }

    /**
     * @return List of the current packages - CAREFUL - this list is not a copy
     *         so mutations would impact the manager
     */
    public List getPackages() {
        return mPackages;
    }

    /**
     * @return The current platform
     */
    public Platform getPlatform() {
        return mPlatform;
    }

    /**
     * @return The source URL used for copying files
     */
    public URL getSourceUrl() {
        return mSourceUrl;
    }

    /**
     * Overrides the directory to contain the native files
     * 
     * @param dir The new binaryDir value
     */
    public void setBinaryDir(File dir) {
        if (mIsInitialized) {
            throw new IllegalStateException(
                    "Cannot alter JdicManager after initializing");
        }
        mBinaryDir = dir;
    }

    /**
     * Override the copy files flag
     * 
     * @param copy
     *            If true, files will be copied from SourceUrl to BinaryDir
     */
    public void setCopyFiles(boolean copy) {
        if (mIsInitialized) {
            throw new IllegalStateException(
                    "Cannot alter JdicManager after initializing");
        }
        mCopyFiles = copy;
    }

    /**
     * Override the delete files flag
     * 
     * @param b If true, delete copied files on exit
     */
    public void setDeleteFiles(boolean b) {
        if (mIsInitialized) {
            throw new IllegalStateException(
                    "Cannot alter JdicManager after initializing");
        }
        mDeleteFiles = b;
    }

    /**
     * Override the detected platform
     * 
     * @param p  New platform value
     */
    public void setPlatform(Platform p) {
        if (mIsInitialized) {
            throw new IllegalStateException(
                    "Cannot alter JdicManager after initializing");
        }
        mPlatform = p;
    }

    /**
     * Override the source URL
     * 
     * @param s The source URL to use for copying native files
     */
    public void setSourceUrl(URL s) {
        if (mIsInitialized) {
            throw new IllegalStateException(
                    "Cannot alter JdicManager after initializing");
        }
        mSourceUrl = s;
    }

    /**
     * Adds a package to the initialization list
     * 
     * @param p The feature to be added to the Package attribute
     */
    public void addPackage(Package p) {
        if (!mPackages.contains(p)) {
            // Ensure only one instance of each
            mPackages.add(p);
        }
    }

    /**
     * Removes a package from the initialization list
     * 
     * @param p The feature to be removeded from the Package attribute
     */
    public void removePackage(Package p) {
        if (mPackages.contains(p)) {
            // Ensure only one instance of each
            mPackages.remove(p);
        }
    }

    /**
     * Determines the class path and then builds the source and dest paths if
     * they have not been calculated already
     * 
     * @exception JdicInitException
     *                Generic initialization exception
     */
    private void determineDefaultDirs() throws JdicInitException {
        try {
            // Need to know the platform to do anyting...
            if (mPlatform == null)
                return;

            if (mSourceUrl == null || mBinaryDir == null) {
                // OK - find where we are...
                String thisClassFileName = this.getClass().getName()
                        .replaceAll("\\.", "/")
                        + ".class";
                String relativePathToRootParent = thisClassFileName.replaceAll(
                        "[^/]+", "..").substring(3);
                URL thisClassUrl = this.getClass().getClassLoader()
                        .getResource(thisClassFileName);
                if (thisClassUrl == null) {
                    throw new JdicInitException("Unable to locate "
                            + thisClassFileName + " in classloader");
                }

                // Now find the root of the class directories
                URL classpathRootUrl = new URL(thisClassUrl,
                        relativePathToRootParent);
                if (mSourceUrl == null) {
                    mSourceUrl = new URL(classpathRootUrl, mPlatform
                            .getDirName()
                            + "/");
                }

                if (mBinaryDir == null) {
                    if (System.getProperty("javawebstart.version") != null) {
                        // We are webstart....find the cached native files...
                        mCopyFiles = false;
                        mDeleteFiles = false;

                        // OK - Find the dir containing our Jar
                        // Once we find it, the native files should be in a dir
                        // named "RN"+jarName

                        URL cpParent = new URL(classpathRootUrl, ".."); 
                        // Try .. in case we are not in a jar...
                        if ("jar".equals(cpParent.getProtocol())) {
                            // At this point the jar URL will look like this:
                            // jar:file:/C:/code/jury/jury.jar!
                            // Strip it down and get rid of the JAR...
                            cpParent = new URL(new URL(cpParent.toString()
                                    .substring(4)), "./");
                        }
                        String cacheDirName = "RN" + mPlatform.getJarName()
                                + "/";
                        mBinaryDir = new File(new URI(new URL(cpParent,
                                cacheDirName).toString()));
                    } else if ("file".equals(classpathRootUrl.getProtocol())) {
                        // We are running from the file system...just use the 
                        // file system
                        mCopyFiles = false;
                        mDeleteFiles = false;
                        mBinaryDir = new File(new URI(new URL(classpathRootUrl,
                                mPlatform.getDirName()).toString()));
                    } else {
                        // Not a file system - need to copy the files...
                        mCopyFiles = true;
                        mDeleteFiles = true;
                        /**
                         * @todo    Perhaps an option to copy from individual 
                         *          Jars instead of just the main jar?
                         */
                        // Place files in the temp dir...
                        File tmpFile = File.createTempFile("jdic", ".tmp");
                        mBinaryDir = new File(tmpFile.getParent()
                                + File.separator + tmpFile.getName());
                        tmpFile.delete();
                    }
                }
            }
        } catch (Throwable e) {
            throw new JdicInitException(e);
        }
    }

    /**
     *  Initialzes the binary dirs.
     *
     * @exception  JdicInitException  Generic JDIC exception
     */
    private void init() throws JdicInitException {
        if (mIsInitialized) {
            throw new IllegalStateException("Cannot re-init JdicManager");
        }
        mIsInitialized = true;
        // If this fails, do not allow a reattempt

        if (mPlatform == null) {
            throw new JdicInitException("Unable to determine platform - "
                    + System.getProperty("os.name"));
        }

        // Just recheck the defaults - if the platform was not detected then
        // they may not have initialized...
        determineDefaultDirs();

        if (mCopyFiles) {
            // Ensure the target directory exists
            mBinaryDir.mkdirs();
            if (mDeleteFiles) {
                mBinaryDir.deleteOnExit();
            }
        }
    }

    /**
     *  Iterates each packages and works on uninitialized one.
     *
     * @exception  JdicInitException  Generic JDIC exception
     */
    public void initPackages() throws JdicInitException {
        Iterator pkgItr = mPackages.iterator();
        while (pkgItr.hasNext()) {
            Package p = (Package) pkgItr.next();
            //Try to work on the uninitialized pacakges
            if (!p.getIsInitialized()) {
                String[] files = p.getFiles(mPlatform);

                if (files == null) {
                    throw new JdicInitException("Package " + p.getName()
                            + " not supported on platform "
                            + mPlatform.getName());
                }

                p.preCopy();

                if (mCopyFiles) {
                    // OK - now copy the files...
                    for (int i = 0; i < files.length; i++) {
                        try {
                            URL src = new URL(mSourceUrl, files[i]);
                            File dest = copyFileURL(src, mBinaryDir);
                            if (mDeleteFiles) {
                                dest.deleteOnExit();
                                // NOTE: The deleteOnExit will fail on core
                                // libraries because the lib will be locked by
                                // the java process when the deleteOnExit occurs
                                /**
                                 * @todo Based on some quick net research, this
                                 *       can be worked around by loading all the
                                 *       launcher code into a new classloader
                                 *       and forcing the CL to GC before
                                 *       exit...unloading the class loader for
                                 *       the class that did the load >SHOULD <
                                 *       unload the library based on a net
                                 *       search - however a lot of work for a
                                 *       single dangling file...
                                 */
                            }
                        } catch (Throwable e) {
                            throw new JdicInitException(e);
                        }
                    }
                }

                // Verify all the files exist as a sanity check...
                for (int i = 0; i < files.length; i++) {
                    File f = new File(mBinaryDir, files[i]);
                    if (!f.exists()) {
                        throw new JdicInitException("Unable to locate file "
                                + f);
                    }
                }

                p.postCopy();
                p.setIsInitialized(true);
            }
        }
    }
}
