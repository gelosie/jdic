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

/**
 * This constant interface keeps the constants used or shared by other classes.
 */
public class JnlpConstants {
    /**
     * OS name for Linux.
     */
    public static final String OS_LINUX = "linux";
    /**
     * OS name for Solaris.
     */
    public static final String OS_SOLARIS = "sunos";
    /**
     * OS name for Windows.
     */
    public static final String OS_WINDOWS = "windows";

    /**
     * Default jnlp file extension.
     */
    public static final String JNLPFILE_EXTENSION = ".jnlp";

    /**
     * Default installation base location of license files on Unix.
     * On Unix, if the user specified license files in command line,
     * it will be installed under below parent path in the machine.
     */
    public static final String LICENSE_INSTALLATION_BASE_PATH =
        "/usr/share/lib/javaws/licenses";

    /**
     * The resource directory.
     */
    public static final String PROPERTY_NAME_RESOURCEDIR
        = "ResourceDir";
    /**
     * The package name.
     */
    public static final String PROPERTY_NAME_PACKAGENAME
        = "PackageName";
    /**
     * The output directory.
     */
    public static final String PROPERTY_NAME_OUTPUTDIR
        = "OutputDir";
    /**
     * The version number.
     */
    public static final String PROPERTY_NAME_VERSIONNO
        = "Version";
    /**
     * The release number.
     */
    public static final String PROPERTY_NAME_RELEASENO
        = "Release";
    /**
     * The license directory.
     */
    public static final String PROPERTY_NAME_LICENSEDIR
        = "LicenseDir";
    /**
     * The banner jpeg file.
     */
    public static final String PROPERTY_NAME_BANNERJPGFILE
        = "BannerJpgFile";
    /**
     * The panel jpeg file.
     */
    public static final String PROPERTY_NAME_PANELJPGFILE
        = "PanelJpgFile";
    /**
     * The Microsoft SDK update directory.
     */
    public static final String PROPERTY_NAME_MS_SDK_PATH
        = "MSSDKDir";
    /**
     * Specify whether a shortcut should be created after the installation.
     */
    public static final String PROPERTY_NAME_ENABLESHORTCUT
        = "EnableShortcut";
    /**
     * Specify whether to create an association after the installation.
     */
    public static final String PROPERTY_NAME_ENABLEASSOCIATION
        = "EnableAssociation";
    /**
     * Specify whether to install the JNLP application into system cache.
     */
    public static final String PROPERTY_NAME_ENABLESYSTEMCACHE
        = "EnableSystemCache";
    /**
     * The relevant path for UISample.msi within Microsoft SDK update dir.
     */
    public static final String HIERACHY_TO_RAW_MSI
        = "Samples\\SysMgmt\\msi\\database\\UISample.Msi";
    /**
     * The relevant path for the localization resource with Microsoft SDK
     * update directory.
     */
    public static final String HIERACHY_TO_INTL
        = "Samples\\SysMgmt\\msi\\database\\INTL";

    // Information fields to be parsed in given jnlp file.
    /**
     * The codebase field of the JNLP file.
     */
    public static final String JNLP_FIELD_CODEBASE    = "codebase";
    /**
     * The description field of the JNLP file.
     */
    public static final String JNLP_FIELD_DESCRIPTION = "description";
    /**
     * The title field of the JNLP file.
     */
    public static final String JNLP_FIELD_TITLE       = "title";
    /**
     * The vendor field of the JNLP file.
     */
    public static final String JNLP_FIELD_VENDOR      = "vendor";
    /**
     * The pesudo license field of the JNLP file.
     * Note: There are no license tag defination for Jnlp files, we just
     * create such pesudo field for easy operation.
     */
    public static final String JNLP_FIELD_LICENSE     = "license";

    //  Locale Index for English Locale
    /**
     * English locale index.
     */
    public static final int LOCALE_EN = 0;
    /**
     * Localization locale list.
     */
    public static final String[] LOCALES = {
        "en", "zh_CN", "ja", "zh_TW", "fr", "de", "it", "ko", "es", "sv"};
    /**
     * Corresponding localization file postfix for Microsoft lolizazation
     * resource files (Action Text and Error table).
     */
    public static final String[] LOCALES_SUFFIX = {
        "", "CHS", "JPN", "CHT", "FRA", "DEU", "ITA", "KOR", "ESN", "SVE"};
    /**
     * Block size for one time tranportation.
     */
    public static final int FILE_COPY_BLOCK_SIZE = 1024;
    /**
     * Default version number.
     */
    public static final String DEFAULT_VERSION_NUM = "1.0";
    /**
     * Default release number.
     */
    public static final String DEFAULT_RELEASE_NUM = "1";
}
