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

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.net.URL;

import com.sun.deploy.xml.XMLEncoding;
import com.sun.deploy.xml.XMLNode;
import com.sun.deploy.xml.XMLParser;
import com.sun.javaws.exceptions.BadFieldException;
import com.sun.javaws.exceptions.MissingFieldException;
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
import com.sun.javaws.jnl.XMLUtils;
import com.sun.javaws.util.GeneralUtil;

/**
 * This parameter class encapsulates all the description information for the
 * generated packages.
 */
public final class JnlpPackageInfo {

    private static String osName = System.getProperty("os.name").toLowerCase();
 
    /**
     * JnlpPackageInfo contructor.
     *
     */
    public JnlpPackageInfo() {
        titles = new String[JnlpConstants.LOCALES.length];
        vendors = new String[JnlpConstants.LOCALES.length];
        licenses = new String[JnlpConstants.LOCALES.length];
        descriptions = new String[JnlpConstants.LOCALES.length];
        uniqueTmpDirPath = null;
        enableLocalization = false;
        bannerJpgFilePath = null;
        panelJpgFilePath = null;
        jnlpRefFilePaths = new ArrayList();
    }

    /**
     * Jnlp file title fields of different locales.
     */
    private String[] titles;
    /**
     * Jnlp file vendor fields of different locales.
     */
    private String[] vendors;
    /**
     * Jnlp file license fields of different locales.
     */
    private   String[] licenses;
    /**
     * Jnlp file description fields of different locales.
     */
    private   String[] descriptions;

    /**
     * License Dir path.
     */
    private String licenseDirPath;

    /**
     * Unique temporary dir path in system tmp dir.
     */
    private String uniqueTmpDirPath;

    /**
     * Package name. Such as: notepad.
     */
    private String packageName;

    /**
     *  The destination path where the package file would be placed.
     */
    private String outputDirPath;

    /**
     * Jnlp href, which is codebase + href.
     * Such as: http://java.sun.com/products/javawebstart/apps/notepad.jnlp
     */
    private String jnlpFileHref;

    /**
     * Path of the jnlp file. Such as: C:/temp/notepad.jnlp
     */
    private String jnlpFilePath;

    /**
     * Parent path of resource files referenced by the jnlp file,
     * Which should be given by users' input, it will be the parent
     * path of Jnlp file by default.
     * Such as: C:/temp
     */
    private String resourceDirPath;

    /**
     * Version Number of the application to be packaged.
     */
    private String version;

    /**
     * Release Number of the application to be packaged.
     */
    private String release;

    /**
     * Relative path list of resource files referenced by the jnlp file.
     * Such as: notepad/notepad.jar, notepad/images/notepad.jpg
     */
    private List jnlpRefFilePaths;

    /**
     * Absolutepath to Banner jpg file.
     */
    private String bannerJpgFilePath;

    /**
     * Absolutepath to Panel jpg file.
     */
    private String panelJpgFilePath;
    /**
     * Absolutepath to the MS SDK update.
     */
    private String msSDKDirPath;
    /**
     * Absolutepath to the raw msi file.
     */
    private String rawMsiFilePath;

    /**
     * If localization needed.
     */
    private boolean enableLocalization;

    /**
     * If show license or not.
     */
    private boolean enableLicense;

    /**
     * If the shortcut should be created
     */
    private boolean shortcutEnabled;

    /**
     * If the association should be created
     */
    private boolean enableAssociation;

    /**
     * If use system or user(default) cache.
     */
    private boolean enableSystemCache;

    /**
     * Retrieves the banner jpeg file path.
     * @return The banner jpeg file path.
     */
    public String getBannerJpgFilePath() {
        return bannerJpgFilePath;
    }

    /**
     * Sets the banner jpeg file path.
     * @param theBannerJpgFilePath The given banner jpeg file path.
     */
    public void setBannerJpgFilePath(String theBannerJpgFilePath) {
        bannerJpgFilePath = theBannerJpgFilePath;
    }

    /**
     * Gets the panel jpeg file path.
     * @return The panel jpeg file path.
     */
    public String getPanelJpgFilePath() {
        return panelJpgFilePath;
    }

    /**
     * Sets the panel jpeg file path.
     * @param thePanelJpegFilePath The given panel jpeg file path.
     */
    public void setPanelJpgFilePath(String thePanelJpegFilePath) {
        panelJpgFilePath = thePanelJpegFilePath;
    }
    /**
     * Gets the MS SDK Path.
     * @return The MS SDK Path.
     */
    public String getMSSDKDirPath() {
        return msSDKDirPath;
    }
    /**
     * Sets the MS SDK Path.
     * @param theMSSDKDirPath The given ms SDK path.
     */
    public void setMSSDKDirPath(String theMSSDKDirPath) {
        msSDKDirPath = theMSSDKDirPath;
    }
    /**
     * Gets the raw msi file path.
     * @return The raw msi file path.
     */
    public String getRawMsiFilePath() {
        return rawMsiFilePath;
    }

    /**
     * Sets the raw msi file path.
     * @param theRawMsiFilePath The given raw msi file path.
     */
    public void setRawMsiFilePath(String theRawMsiFilePath) {
        rawMsiFilePath = theRawMsiFilePath;
    }

    /**
     * Gets whether to install the application into system cache or not.
     * @return True if the application goes into the system cache.
     */
    public boolean getSystemCacheEnabled() {
        return enableSystemCache;
    }

    /**
     * Sets whether to install into the system cache.
     * @param systemcache True if the application will goes into the sys cache.
     */
    public void setSystemCacheEnabled(boolean systemcache) {
        enableSystemCache = systemcache;
    }

    /**
     * Gets the package name.
     * @return The name of the package.
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Sets the package name.
     * @param theName The given name of the package.
     */
    public void setPackageName(String theName) {
        packageName = theName;
    }

    /**
     * Gets the output directory path.
     * @return The path of the output directory.
     */
    public String getOutputDirPath() {
        return outputDirPath;
    }

    /**
     * Sets the output directory.
     * @param theOutputDirPath The path of the output directory.
     */
    public void setOutputDirPath(String theOutputDirPath) {
        outputDirPath = theOutputDirPath;
    }

    /**
     * Gets the jnlp file href.
     * @return The href of the jnlp file.
     */
    public String getJnlpFileHref() {
        return jnlpFileHref;
    }

    /**
     * Sets the jnlp file href.
     * @param theJnlpFileHref The given jnlp file href.
     */
    public void setJnlpFileHref(String theJnlpFileHref) {
        jnlpFileHref = theJnlpFileHref;
    }

    /**
     * Gest the jnlp file path.
     * @return The path of the jnlp file.
     */
    public String getJnlpFilePath() {
        return jnlpFilePath;
    }

    /**
     * Sets the jnlp file path.
     * @param theJnlpFilePath The given jnlp file path.
     */
    public void setJnlpFilePath(String theJnlpFilePath) {
        jnlpFilePath = theJnlpFilePath;
    }

    /**
     * Returns jnlp file name with extension of the jnlp file according to the
     * jnlpFilePath field.
     * <p>
     * For examples: if jnlpFilePath is C:/temp/notepad.jnlp, this field is
     * notepad.jnlp.
     * <p>
     * Since this field is generated from the jnlpFilePath field,
     * there is no associated variable member, and there is only getter method
     * for this field, no setter method.
     * @return The single jnlp file name without any parent dir info.
     */
    public String getJnlpFileName() {
        return new File(jnlpFilePath).getName();
    }

    /**
     * Gets the resource directory information.
     * @return The resource directory path.
     */
    public String getResourceDirPath() {
        return resourceDirPath;
    }

    /**
     * Sets the resource directory information.
     * @param theResourcePath The given resource directory path.
     */
    public void setResourcePath(String theResourcePath) {
        resourceDirPath = theResourcePath;
    }

    /**
     * Gets version info.
     * @return The version number string.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version number info.
     * @param theVersion The given version number.
     */
    public void setVersion(String theVersion) {
        if (osName.startsWith(JnlpConstants.OS_WINDOWS)) {
            try {
                Float.parseFloat(theVersion);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: " +
                    "Illegal Version Number (Valid input: digits & '.')");
            }
        }
        version = theVersion;
    }

    /**
     * Gets the release number info.
     * @return The release number info.
     */
    public String getRelease() {
        return release;
    }

    /**
     * Sets the release number.
     * @param theRelease The given release number.
     */
    public void setRelease(String theRelease) {
        if (osName.startsWith(JnlpConstants.OS_WINDOWS)) {
            try {
                Float.parseFloat(theRelease);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: " +
                    "Illegal Release Number (Valid input: digits & '.')");
            }
        }
        release = theRelease;
    }

    /**
     * Gets the jnlp file refereced paths info.
     * @return The list iterator of jnlp file refereced paths.
     */
    public Iterator getJnlpRefFilePaths() {
        return jnlpRefFilePaths.iterator();
    }

    /**
     * Gets whether to create a shortcut after the installation.
     * @return True if the installation will create a shortcut.
     */
    public boolean getShortcutEnabled() {
        return shortcutEnabled;
    }

    /**
     * Sets whether to create a shortcut during the installation process.
     * @param shortcut True if the installation will create a shortcut.
     */
    public void setShortcutEnabled(boolean shortcut) {
        shortcutEnabled = shortcut;
    }

    /**
     * Gets whether to create an association during the installation process.
     * @return True if the installation will create an association.
     */
    public boolean getAssociationEnabled() {
        return enableAssociation;
    }

    /**
     * Sets whether to create an association during the installation process.
     * @param association True if the installation will create an association.
     */
    public void setAssociationEnabled(boolean association) {
        enableAssociation = association;
    }

    /**
     * Gets whether we need to localize the installation package.
     * @return True if localization supported.
     */
    public boolean getLocalizationEnabled() {
        return enableLocalization;
    }

    /**
     * Sets whether we need to provide localization support.
     * @param globalization True if localization supported.
     */
    public void setGlocalizationEnabled(boolean globalization) {
        enableLocalization = globalization;
    }

    /**
     * Gets the license directory.
     * @return The directory containing all the license files.
     */
    public String getLicenseDirPath() {
        return licenseDirPath;
    }

    /**
     * Sets the license directory.
     * @param theLicenseDirPath The directory containing all the license files.
     */
    public void setLicenseDirPath(String theLicenseDirPath) {
        licenseDirPath = theLicenseDirPath;
    }

    /**
     * Sets whether to show license info during installation process.
     * @param bShow True if license info gets displayed durinig installation.
     */
    public void setShowLicense(boolean bShow) {
        enableLicense = bShow;
    }

    /**
     * Gets whether to show license info during installation process.
     * @return True if license info gets displayed during installation process.
     */
    public boolean getShowLicense() {
        return enableLicense;
    }

    /**
     * Gets a unique temp directory.
     * @return The path of the uniqe temp directory.
     * @throws IOException If failed to get such a directory.
     */
    public String getUniqueTmpDirPath() throws IOException {
        if (uniqueTmpDirPath == null) {
            uniqueTmpDirPath = FileOperUtility.createUniqueTmpDir();
        }
        return uniqueTmpDirPath;
    }

    /**
     * Gets localized jnlp file field.
     * @param locale The given locale name.
     * @param info  The name of the jnlp file field.
     * @return The lolized jnlp field content string.
     */
    public String getLocalizedJnlpInfo(String locale, String info) {
        int localeIndex = getLocaleIndex(locale);
        String ret = null;
        if (info.compareToIgnoreCase(JnlpConstants.JNLP_FIELD_TITLE) == 0) {
            ret = titles[localeIndex];
        } else if (
            info.compareToIgnoreCase(JnlpConstants.JNLP_FIELD_VENDOR) == 0) {
            ret = vendors[localeIndex];
        } else if (
            info.compareToIgnoreCase(JnlpConstants.JNLP_FIELD_LICENSE) == 0) {
            ret = licenses[localeIndex];
        } else if (
            info.compareToIgnoreCase(JnlpConstants.JNLP_FIELD_DESCRIPTION)
                == 0) {
            ret = descriptions[localeIndex];
        }
        return ret;
    }

    /**
     * Gets the corresponding locale index.
     * @param locale The given locale string.
     * @return The index of the locale string.
     */
    protected int getLocaleIndex(String locale) {
        int index = -1;
        for (int i = 0; i < JnlpConstants.LOCALES.length; i++) {
            if (locale.compareToIgnoreCase(JnlpConstants.LOCALES[i]) == 0) {
                index = i;
                break;
            }
        }
        return index;
    }
    
    /**
     * get remote resources to local directory, and set the related fields of
     * the JnlpPackageInfo
     * 
     * @param jnlp points to the remote jnlp file
     * @throws IOException
     */
    public void parseRemoteJnlpInfo(URL jnlp) 
            throws IOException {
        if (jnlp == null) 
            throw new IOException("url is null when trying to parse JnlpInfo");
        String localBase = FileOperUtility.createUniqueTmpDir();
        URL localJnlp = FileOperUtility.getRemoteResource(jnlp, localBase);
        setResourcePath(localBase);
        parseLocalJnlpInfo(localJnlp);
    }
    
    /**
     * add file path to JnlpRefFilePath
     * 
     * @param url points to the file
     * @param codebase 
     * @throws IOException
     */
    public void addJnlpRefFilePath(URL url, URL codebase) throws IOException {
        String relPath = FileOperUtility.getRelativePath(
                url.toString(), codebase.toString());

        jnlpRefFilePaths.add(relPath);
    }
    
    /**
     * set related fields of the JnlpPackageInfo according to the local jnlp
     * file
     * 
     * @param jnlp points to the local jnlp file
     * @throws IOException
     */
    public void parseLocalJnlpInfo(URL jnlp) throws IOException {
        URL codebase = null;
        
        if (jnlp == null) 
            throw new IOException("url is null when trying to parse JnlpInfo");
        
        if (resourceDirPath == null || resourceDirPath.length() <= 0)
            throw new IOException("resourcePath have not been set");
        
        try {
            File jnlpFile = new File(jnlp.toURI());
            setJnlpFilePath(jnlpFile.getPath());
            LaunchDesc laDesc = LaunchDescFactory.buildDescriptor(jnlp);
            codebase = laDesc.getCodebase();
            setJnlpFileHref(laDesc.getCanonicalHome().toString());
            InformationDesc infoDesc = laDesc.getInformation();
            setTitle(JnlpConstants.LOCALE_EN, infoDesc.getTitle());
            setVendor(JnlpConstants.LOCALE_EN, infoDesc.getVendor());
            setDescription(JnlpConstants.LOCALE_EN, 
                    infoDesc.getDescription(InformationDesc.DESC_DEFAULT));

            addJnlpRefFilePaths(jnlp);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    public void addJnlpRefFilePaths(URL jnlp) throws IOException{
        
        if (jnlp == null) 
            throw new IOException("url is null when trying to parse JnlpInfo");
        
        if (resourceDirPath == null || resourceDirPath.length() <= 0)
            throw new IOException("resourcePath have not been set");
        
        try {
        	File jnlpFile = new File(jnlp.toURI());
            jnlpRefFilePaths.add(jnlpFile.getAbsolutePath());
            LaunchDesc laDesc = LaunchDescFactory.buildDescriptor(jnlp);
            URL codebase = laDesc.getCodebase();
            InformationDesc infoDesc = laDesc.getInformation();
            IconDesc[] iconArray = infoDesc.getIcons();
            for (int i = 0; i < iconArray.length; i++) {
                URL iconURL = iconArray[i].getLocation();
                this.addJnlpRefFilePath(iconURL, codebase);
            }
            ResourcesDesc reDesc = laDesc.getResources();
            reDesc.visit(
                    new JDICPackagerFileRefVisitor(codebase,
                            resourceDirPath, this));
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    /**
     * set title in the given locale
     * 
     * @param index
     * @param title
     */
    public void setTitle(int index, String title) {
        titles[index] = title;   
    }
    
    /**
     * set vector in the given locale
     * 
     * @param index
     * @param vendor
     */
    public void setVendor(int index, String vendor) {
        vendors[index] = vendor;   
    }
    
    /**
     * set description in the give locale
     * 
     * @param index
     * @param description
     */
    public void setDescription(int index, String description) {
        descriptions[index] = description;   
    }
    
    /**
     * set license in the give locale
     * 
     * @param index
     * @param license
     */
    public void setLicense(int index, String license) {
        licenses[index] = license;   
    }
    
    /**
     * set localized information to the jnlpPackagerInfo
     * 
     * @throws IOException
     */
    public void setLocalizedInformation()
            throws IOException {
        File jnlpFile = null;
        jnlpFile = new File(getJnlpFilePath());
        
        if (jnlpFile == null) {
            throw new IOException("Cannot find local jnlp file: " +
                    jnlpFile.getPath());   
        }
        byte[] bits;
        try { 
            bits = LaunchDescFactory.readBytes(new FileInputStream(jnlpFile),
                jnlpFile.length());
        } catch (Exception e) {
            throw new IOException("Exception when build localized InfoDesc " +
                    e.getMessage());   
        }

        setLocalizedInformation(bits);
        
        for (int i = 0; i < JnlpConstants.LOCALES.length; i++) {
            if (titles[i] == null || titles[i].length() == 0) {
                titles[i] = titles[JnlpConstants.LOCALE_EN];   
            }
            
            if (vendors[i] == null || vendors[i].length() == 0) {
                vendors[i] = vendors[JnlpConstants.LOCALE_EN];   
            }
            
            if (descriptions[i] == null || descriptions[i].length() == 0) {
                descriptions[i] = descriptions[JnlpConstants.LOCALE_EN];   
            }
        }
    }
    
    private void setLocalizedInformation(byte[] bits)
            throws IOException {
        String source;
        String encoding;
        XMLNode root;

        try {
            source = XMLEncoding.decodeXML(bits);
        } catch (Exception e) {
            throw new IOException("exception determining encoding of jnlp " +
                    "file: " + e.getMessage());
        }

        try {
            root = (new XMLParser(source)).parse();
        } catch (Exception e) {
            throw new IOException("exception parsing jnlp file " +
                    e.getMessage());
        }
        
        try {
            XMLUtils.visitElements(root, "<information>",
                    new JDICPackagerInfoElementVisitor(this));
        } catch (Exception e) {
            throw new IOException("exception creating InformationDesc " +
                    e.getMessage());   
        }
    }
}

/**
 * ResourceVisitor to add all the local resource file paths to
 * JnlpPackageInfo.jnlpRefFilePaths
 */
class JDICPackagerFileRefVisitor implements ResourceVisitor {
    private URL codebase = null; 
    private String localBase = null;
    private JnlpPackageInfo pkgInfo = null;

    public JDICPackagerFileRefVisitor(URL incodebase, String inlocalbase,
            JnlpPackageInfo inpkgInfo) {
        codebase = incodebase;
        localBase = inlocalbase;
        pkgInfo = inpkgInfo;
    }
    public void visitJARDesc(JARDesc jad) {
        try {
            pkgInfo.addJnlpRefFilePath(jad.getLocation(), codebase);
        } catch (IOException ioE) {
            ioE.printStackTrace();   
        }
    }
    
    public void visitExtensionDesc(ExtensionDesc ed) {
        String relPath = FileOperUtility.getRelativePath(
                ed.getLocation().toString(), codebase.toString());
        File tmpFile = new File(pkgInfo.getResourceDirPath() + File.separator +
                relPath);
        try {
            pkgInfo.addJnlpRefFilePaths(tmpFile.toURL());
        } catch (IOException ioE) {
            ioE.printStackTrace();   
        }
    }
    
    public void visitPropertyDesc(PropertyDesc prd) {}
    public void visitJREDesc(JREDesc jrd) {}
    public void visitPackageDesc(PackageDesc pad) {}
}

/**
 * Element Visitor to set informations in different locales.
 */
class JDICPackagerInfoElementVisitor extends XMLUtils.ElementVisitor {
    private JnlpPackageInfo pkgInfo;
    
    public JDICPackagerInfoElementVisitor(JnlpPackageInfo inpkgInfo) {
        pkgInfo = inpkgInfo;
    }
    
    public void visitElement(XMLNode e) throws BadFieldException,
        MissingFieldException {
        String[] locales = GeneralUtil.getStringList(
                XMLUtils.getAttribute(e, "", "locale", null));
        for (int i = 0; i < JnlpConstants.LOCALES.length; i++) {
             if (matchLocale(JnlpConstants.LOCALES[i], locales)) {
                 String title = XMLUtils.getElementContents(e, "<title>");
                 pkgInfo.setTitle(i, title);
                 String vendor = XMLUtils.getElementContents(e, "<vendor>");
                 pkgInfo.setVendor(i, vendor);
                 String description = XMLUtils.getElementContentsWithAttribute(
                        e, "<description>", "kind", "", null);
                 pkgInfo.setDescription(i, description);
             }
        }
    }
    
    private boolean matchLocale(String locale, String[] locales) {
        boolean match = false;
        if (locales == null)
            return match;
        
        for (int i = 0; i < locales.length; i++) {
            if (locales[i] != null && locales[i].equals(locale)) {
                match = true;
                break;
            }
        }
        
        return match;
    }
}
