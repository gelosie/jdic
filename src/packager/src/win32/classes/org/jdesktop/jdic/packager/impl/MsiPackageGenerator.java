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

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * Concrete implementation of interface PackageGenerator for msi packages
 * generating on Windows.
 */
public class MsiPackageGenerator implements PackageGenerator {
    /**
     * The jar file containg these classes.
     */
    private static final String SYS_JAR_FILE_NAME = "packager.jar";
    /**
     * Jar file name for packaging all the jnlp relevant files.
     */
    private static final String JNLP_FILES_JAR_NAME = "JnlpFiles.jar";
    /**
     * Miscellanious MSI generatation needed files location in packager.jar.
     */
    private static final String MSI_SUPPORT_FILES_HIERARCHY_PATH =
        "org/jdesktop/jdic/packager/impl/files/";
    /**
     * The relevant path of makecab.exe in MSSDKDir.
     */
    private static final String MAKECAB_EXE_PATH =
        "Samples\\sysmgmt\\msi\\Patching\\makecab.exe";
    /**
     * The relevant path of msidb.exe in MSSDKDir.
     */
    private static final String MSIDB_EXE_PATH =  
        "bin\\msidb.exe";
    /**
     * Bootstrapper file path.
     */
    private static final String SYS_BOOTSTRAPPER_FILE_PATH =
        MSI_SUPPORT_FILES_HIERARCHY_PATH + "bootstrapper.exe";
    /**
     * Custome dll file name.
     */
    private static final String CUSTOM_DLL_FILE_NAME = "custom.dll";
    /**
     * The text table containing the localized welcome message.
     */
    private static final String LOCALIZED_WELCOME_MSG_FILE_NAME =
        "WelcomeMsg.idt";
    /**
     * The name of the table containing the localized welcome message.
     */
    public static final String LOCALIZED_WELCOME_MSG_TABLE_NAME =
        "WelcomeMsg";
    /**
     * Default panel jpeg file name.
     */
    private static final String PANEL_JPG_FILE_NAME  = "panel.jpg";
    /**
     * Banner jpeg file name.
     */
    private static final String BANNER_JPG_FILE_NAME = "banner.jpg";
    /**
     * Property for Java home directory.
     */
    private static final String JAVA_HOME_DIR_PROPERTY = "java.home";
    /**
     * Property for java class path.
     */
    private static final String SYS_CLASS_PATH         = "java.class.path";
    /**
     * Property for author info.
     */
    private static final String AUTHOR_INFO       = "http://jdic.dev.java.net";
    /**
     * Value index of field Text, table Control.
     */
    private static final int VI_CONTROL_TEXT = 10;
    /**
     * Value index of field Argument, table ControlEvent.
     */
    private static final int VI_CONTROLEVENT_ARGUMENT = 4;
    /**
     * Value index of field Event, table ControlEvent.
     */
    private static final int VI_CONTROLEVENT_EVENT = 3;
    /**
     * Value index of field Value, table Property.
     */
    private static final int VI_PROPERTY_VALUE = 2;
    /**
     * Property ID of property Revision Number, table Summary Info.
     */
    private static final int PID_REVISION_NUMBER = 9;
    /**
     * Property ID of property Author, table Summary Info.
     */
    private static final int PID_AUTHOR = 4;
    /**
     * Full path of file packager.jar.
     */
    private static String packagerJarFilePath = null;

    /**
     * MsiPackageGenerator creator.
     *
     */
    public MsiPackageGenerator() {
    }

    /**
     * Gets the location of packager.jar.
     *
     * @throws IOException If failed to get the location of packager.jar.
     */
    private static void getPackagerJarFilePath() throws IOException {
        String sysClassPath = System.getProperty(SYS_CLASS_PATH);
        int fileNameIndex = sysClassPath.indexOf(SYS_JAR_FILE_NAME);
        int filePathIndex = -1;
        int i = fileNameIndex;
        char indexChar;
        while (i > 0) {
            i--;
            indexChar = sysClassPath.charAt(i);
            if ((indexChar == ';') || (i == 0)) {
                filePathIndex = i;
                break;
            }
        }
        if (filePathIndex > 0) {
            packagerJarFilePath =
                sysClassPath.substring(
                    filePathIndex + 1,
                    fileNameIndex + SYS_JAR_FILE_NAME.length());
        } else if (filePathIndex == 0) {
            packagerJarFilePath =
                sysClassPath.substring(
                    filePathIndex,
                    fileNameIndex + SYS_JAR_FILE_NAME.length());
        } else {
            throw new IOException(
                    "Could not locate "
                    + SYS_JAR_FILE_NAME
                    + " in system CLASSPATH setting!");
        }
    }
    
    /**
     *  Modify template msi according to ShowLicense or not.
     *  @param msiFilePath The given template msi file.
     *  @param pkgInfo JnlpPackageInfo object.
     *  @param localeIndex The index of the given locale.
     *  @throws IOException If failed to modify the template msi file.
     */
    private static void adjustMsiLicenseFields(
                        String msiFilePath,
                        JnlpPackageInfo pkgInfo,
                        int localeIndex) throws IOException {
        TreeMap treeMap = new TreeMap();
        File licenseFile;
        FileInputStream fis;
        //Set the license info field property
        if (pkgInfo.getShowLicense()) {
            try {
                String realLicenseInfo = new String();
                realLicenseInfo +=
                "{\\rtf1\\ansi\\ansicpg1252\\deff0\\deftab720{\\fonttbl{\\f0\\"
                + "froman\\fprq2 Times New Roman;}}{\\colortbl\\red0\\green0\\"
                + "blue0;} \\deflang1033\\horzdoc{\\*\\fchars }{\\*\\lchars }\\"
                + "pard\\plain\\f0\\fs20";

                // Get info from file
                licenseFile = new File(
                        pkgInfo.getLicenseDirPath()
                        + File.separator
                        + JnlpConstants.LOCALES[localeIndex]
                        + ".txt");

                if (!licenseFile.exists()) {
                    licenseFile =
                        new File(
                                pkgInfo.getLicenseDirPath()
                                + File.separator
                                + JnlpConstants.LOCALES[JnlpConstants.LOCALE_EN]
                                + ".txt");
                }

                fis = new FileInputStream(licenseFile);
                int fLen = (int) licenseFile.length();
                byte[] bits = new byte[fLen];
                fis.read(bits, 0, fLen);
                fis.close();

                realLicenseInfo += new String(bits);
                realLicenseInfo += "\\par }";
                treeMap.clear();
                treeMap.put("AgreementText", realLicenseInfo);
                WinMsiUtility.winMsiSetProperty(
                        msiFilePath,
                        "Control",
                        "Control",
                        VI_CONTROL_TEXT,
                        false,
                        treeMap);
            } catch (IOException e) {
                throw new IOException(
                        "License Agreement: Modify Control Table failed: "
                        + "Set MSI LicenseAgreement field property failed!");
            }

            try {
                treeMap.clear();
                treeMap.put("AfterWelcomeDlg", "LicenseAgreementDlg");
                WinMsiUtility.winMsiSetProperty(
                        msiFilePath,
                        "ControlEvent",
                        "Argument",
                        VI_CONTROLEVENT_ARGUMENT,
                        false,
                        treeMap);
            } catch (IOException e) {
                throw new IOException(
                        "License Agreement: Modify ControlEvent Table failed: "
                        + "Insert License Agreement dialog failed!");
            }
        } else {
            try {
                treeMap.clear();
                treeMap.put("AfterWelcomeDlg", "EndDialog");
                WinMsiUtility.winMsiSetProperty(
                        msiFilePath,
                        "ControlEvent",
                        "Argument",
                        VI_CONTROLEVENT_EVENT,
                        false,
                        treeMap);
            } catch (IOException e) {
                throw new IOException(
                    "No License Agreement: Modify ControlEvent Table failed: "
                    + "AfterWelcomeDlg/EndDialog");
            }

            try {
                treeMap.clear();
                treeMap.put("AfterWelcomeDlg", "Return");
                WinMsiUtility.winMsiSetProperty(
                        msiFilePath,
                        "ControlEvent",
                        "Argument",
                        VI_CONTROLEVENT_ARGUMENT,
                        false,
                        treeMap);
            } catch (IOException e) {
                throw new IOException(
                    "No License Agreement: Modify ControlEvent Table failed: "
                    + "AfterWelcomeDlg/Return");
            }
        }
        //Setting proper welcome message for the control table.
        //Try to get the welcomemsg for current locole.
        String welcomeMsg = WinMsiUtility.getWelcomeMsg(
                                msiFilePath,
                                JnlpConstants.LOCALES[localeIndex],
                                pkgInfo.getShowLicense());
        try {
            treeMap.clear();
            treeMap.put("SunButton", "[ButtonText_Next]");
            // get relavant info from file
            treeMap.put(
                "WelcomeDescription",
                welcomeMsg);
            WinMsiUtility.winMsiSetProperty(
                    msiFilePath,
                    "Control",
                    "Text",
                    VI_CONTROL_TEXT,
                    false,
                    treeMap);
        } catch (IOException e) {
            throw new IOException(
                "Handling Agreement: Modify Control Table failed: Next "
                + "Button/WelcomeDescription");
        }
    }

    /**
     *  Customize the non-localized MSI fields based on the JnlpPackageInfo.
     *  @param msiFilePath The given template msi file.
     *  @param pkgInfo JnlpPackageInfo object.
     *  @throws IOException If failed to modify the MSI file.
     */
    private static void customizeNonLocalizedMsiFields(String msiFilePath,
                    JnlpPackageInfo pkgInfo) throws IOException {
        TreeMap treeMap = new TreeMap();
        // get uuid of msi
        String uuidProduct = "{" + WinMsiUtility.genUUID() + "}";
        String uuidUpgrade = "{" + WinMsiUtility.genUUID() + "}";

        //Set property field of property table in the destination msi file
        try {
            treeMap.clear();
            treeMap.put("ARPCONTACT", AUTHOR_INFO);
            treeMap.put("ARPHELPLINK", AUTHOR_INFO);
            treeMap.put("ARPURLINFOABOUT", AUTHOR_INFO);
            treeMap.put("ARPURLUPDATEINFO", AUTHOR_INFO);
            treeMap.put(
                "ARPCOMMENTS",
                "Generated by JDIC Project");

            treeMap.put("ProductCode", uuidProduct);
            treeMap.put("UpgradeCode", uuidUpgrade);
            //Will put release # together with version #
            treeMap.put(
                "ProductVersion",
                pkgInfo.getVersion() +
                "." +
                pkgInfo.getRelease());
            treeMap.put("JnlpFileName", pkgInfo.getJnlpFileName());
            treeMap.put("UninstallInfo", pkgInfo.getJnlpFileHref());

            treeMap.put(
                "Shortcut",
                pkgInfo.getShortcutEnabled() ? "1" : "0");
            treeMap.put(
                "Association",
                pkgInfo.getAssociationEnabled() ? "1" : "0");

            treeMap.put(
                "CacheType",
                pkgInfo.getSystemCacheEnabled() ? "system" : "user");

            WinMsiUtility.winMsiSetProperty(
                msiFilePath,
                "Property",
                "Property",
                VI_PROPERTY_VALUE,
                false,
                treeMap);
        } catch (IOException e) {
            throw new IOException("Set MSI field property failed!");
        }

        //Set the destination msi file summary information stream
        try {
            WinMsiUtility.setSummaryInfoProperty(msiFilePath,
                                                 PID_REVISION_NUMBER,
                                                 uuidProduct);
            WinMsiUtility.setSummaryInfoProperty(msiFilePath,
                                                 PID_AUTHOR,
                                                 AUTHOR_INFO);
        } catch (IOException e) {
            throw new IOException("Set MSI summary information stream failed!");
        }

        String[] names = new String[] {"Name", "Data"};
        String[] properties = new String[] {"String", "Stream"};
        String[] values = new String[2];
        try {
            // Insert into "Binary" table, "CustomDll" record
            String customDLLFilePath =
                pkgInfo.getUniqueTmpDirPath() + CUSTOM_DLL_FILE_NAME;
            WinUtility.extractFileFromJarFile(
                packagerJarFilePath,
                MSI_SUPPORT_FILES_HIERARCHY_PATH + CUSTOM_DLL_FILE_NAME,
                customDLLFilePath);
            values[0] = "CustomDLL";
            values[1] = customDLLFilePath;
            WinMsiUtility.addBinaryRecord(
                    msiFilePath,
                    "Binary",
                    names,
                    properties,
                    values);
        } catch (IOException e) {
            throw new IOException("Binary: insert CustomDLL field failed!");
        }

        try {
            // Insert into "Binary" table, "JarPack" record
            String tempJarFilePath = pkgInfo.getUniqueTmpDirPath()
                                     + JNLP_FILES_JAR_NAME;
            WinUtility.jarJnlpFiles(tempJarFilePath, pkgInfo);
            values[0] = "JarPack";
            values[1] = tempJarFilePath;
            WinMsiUtility.addBinaryRecord(
                    msiFilePath,
                    "Binary",
                    names,
                    properties,
                    values);
        } catch (IOException e) {
            throw new IOException("Binary: insert JarPack field failed!");
        }

        treeMap.clear();
        try {
            // Modify "Binary" table, update "bannerBmp"
            String bannerJpgFilePath =
                pkgInfo.getUniqueTmpDirPath() + BANNER_JPG_FILE_NAME;
            if (pkgInfo.getBannerJpgFilePath() != null) {
                bannerJpgFilePath = pkgInfo.getBannerJpgFilePath();
            } else {
                WinUtility.extractFileFromJarFile(
                    packagerJarFilePath,
                    MSI_SUPPORT_FILES_HIERARCHY_PATH + BANNER_JPG_FILE_NAME,
                    bannerJpgFilePath);
            }
            treeMap.put("bannrbmp", bannerJpgFilePath);
        } catch (IOException e) {
            throw new IOException("Binary: pre insert bannrbmp field failed!");
        }

        try {
            // Modify "Binary" table, update "panelBmp"
            String panelJpgFilePath = pkgInfo.getUniqueTmpDirPath()
                                    + PANEL_JPG_FILE_NAME;
            if (pkgInfo.getPanelJpgFilePath() != null) {
                panelJpgFilePath = pkgInfo.getPanelJpgFilePath();
            } else {
                WinUtility.extractFileFromJarFile(
                        packagerJarFilePath,
                        MSI_SUPPORT_FILES_HIERARCHY_PATH + PANEL_JPG_FILE_NAME,
                        panelJpgFilePath);
            }
            treeMap.put("dlgbmp", panelJpgFilePath);
        }  catch (IOException e) {
            throw new IOException("Binary: pre insert dlgbmp field failed!");
        }

        try {
            WinMsiUtility.winMsiSetProperty(
                    msiFilePath,
                    "Binary",
                    "Name",
                    2,
                    true,
                    treeMap);
        }  catch (IOException e) {
            throw new IOException(
                        "Binary: insert bannrbmp and dlgbmp field failed!");
        }
}

    /**
     *  Generate the template MSI file based on the rawMSI file.
     *  @param msiFilePath The given raw msi file.
     *  @param pkgInfo JnlpPackageInfo object.
     *  @throws IOException If failed to generate the template msi file.
     */
    private static void createTemplateMsiFile(String msiFilePath,
                            JnlpPackageInfo pkgInfo) throws IOException {
        // Modify the template msi file
        ArrayList sqlStrings = new ArrayList();
        //Add table Directory
        sqlStrings.add("CREATE TABLE Directory (Directory CHAR(72) NOT NULL, "
                       + "Directory_Parent CHAR(72), DefaultDir CHAR(255) NOT "
                       + "NULL LOCALIZABLE PRIMARY KEY Directory)");
        sqlStrings.add("INSERT INTO Directory (Directory, Directory_Parent, "
                       + "DefaultDir) VALUES ('TARGETDIR', '', 'SourceDir')");
        sqlStrings.add("INSERT INTO Directory (Directory, Directory_Parent, "
                       + "DefaultDir) VALUES ('ProgramFilesFolder', "
                       + "'TARGETDIR', '.:PROGRA~1|program files')");
        sqlStrings.add("INSERT INTO Directory (Directory, Directory_Parent, "
                       + "DefaultDir) VALUES ('INSTALLDIR', 'TempFolder', "
                       + "'.')");
        sqlStrings.add("INSERT INTO Directory (Directory, Directory_Parent, "
                       + "DefaultDir) VALUES ('TempFolder', 'TARGETDIR', "
                       + "'.:Temp')");
        //Add table Component
        sqlStrings.add("CREATE TABLE Component (Component CHAR(72) NOT NULL, "
                       + "ComponentId CHAR(38), Directory_ CHAR(72) NOT NULL, "
                       + "Attributes INT NOT NULL, Condition CHAR(255), "
                       + "KeyPath CHAR(72) PRIMARY KEY Component)");
        sqlStrings.add("INSERT INTO Component (Component, ComponentId, "
                       + "Directory_, Attributes, Condition, KeyPath) VALUES "
                       + "('AllOtherFiles', "
                       + "'{31CCF52F-FCB3-4239-9D82-05CDA204867A}', "
                       + "'INSTALLDIR', 8, '', '')");
        //Add table File
        sqlStrings.add("CREATE TABLE File (File CHAR(72) NOT NULL, Component_ "
                       + "CHAR(72) NOT NULL, FileName CHAR(255) NOT NULL "
                       + "LOCALIZABLE, FileSize LONG NOT NULL, "
                       + "Version CHAR(72), Language CHAR(20), Attributes INT, "
                       + "Sequence INT NOT NULL PRIMARY KEY File)");
        sqlStrings.add("INSERT INTO File (File, Component_, FileName, "
                       + "FileSize, Version, Language, Attributes, Sequence) "
                       + "VALUES ('place.txt', 'AllOtherFiles', 'place.txt', "
                       + "18, '', '', 16384, 1)");
        //Add table Media
        sqlStrings.add("CREATE TABLE Media (DiskId INT NOT NULL, LastSequence "
                       + "INT NOT NULL, DiskPrompt CHAR(64) LOCALIZABLE, "
                       + "Cabinet CHAR(255), VolumeLabel CHAR(32), Source "
                       + "CHAR(72) PRIMARY KEY DiskId)");
        sqlStrings.add("INSERT INTO Media (DiskId, LastSequence, DiskPrompt, "
                       + "Cabinet, VolumeLabel, Source) VALUES (1, 1, '', "
                       + "'#Data1.cab', '', '')");
        //Add table Feature
        sqlStrings.add("CREATE TABLE Feature (Feature CHAR(38) NOT NULL, "
                       + "Feature_Parent CHAR(38), Title CHAR(64) LOCALIZABLE, "
                       + "Description CHAR(255) LOCALIZABLE, Display INT, "
                       + "Level INT NOT NULL, Directory_ CHAR(72), Attributes "
                       + "INT NOT NULL PRIMARY KEY Feature)");
        sqlStrings.add("INSERT INTO Feature (Feature, Feature_Parent, Title, "
                       + "Description, Display, Level, Directory_ , "
                       + "Attributes) VALUES ('NewFeature1', '', "
                       + "'NewFeature1', '', 2, 1, 'INSTALLDIR', 0)");
        //Add table FeatureComponents
        sqlStrings.add("CREATE TABLE FeatureComponents (Feature_ CHAR(38) NOT "
                       + "NULL, Component_ CHAR(72) NOT NULL PRIMARY KEY "
                       + "Feature_, Component_)");
        sqlStrings.add("INSERT INTO FeatureComponents (Feature_, Component_) "
                       + "VALUES ('NewFeature1', 'AllOtherFiles')");
        //Add table CustomActioni
        sqlStrings.add("CREATE TABLE CustomAction(Action CHAR(72) NOT NULL, "
                       + "Type INT NOT NULL, Source CHAR(72), Target CHAR(255) "
                       + "PRIMARY KEY Action)");
        sqlStrings.add("INSERT INTO CustomAction (Action,Type,Source,Target) "
                       + "VALUES ('CustomInstallAction', 1, 'CustomDLL', "
                       + "'InstallAction')");
        sqlStrings.add("INSERT INTO CustomAction (Action,Type,Source,Target) "
                       + "VALUES ('CustomUninstallAction', 1, 'CustomDLL', "
                       + "'UninstallAction')");
        //Modify table property
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('Manufacturer','" + AUTHOR_INFO + "')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('ProductCode', "
                       + "'{3D11E9FC-142F-4945-8010-861EAA24850F}')");
        sqlStrings.add("INSERT INTO Property (Property,Value) "
                       + "VALUES ('ProductName','Default')");
        sqlStrings.add("INSERT INTO Property (Property,Value) "
                       + "VALUES ('ProductVersion','1.00.0000')");
        sqlStrings.add("INSERT INTO Property (Property,Value) "
                       + "VALUES ('UpgradeCode', "
                       + "'{20AC1669-B481-4CAD-82AF-2CD00005F304}')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('JnlpFileName','ok')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('UninstallInfo','ok')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('Locale','en')");
        sqlStrings.add("UPDATE Property SET Value='" + AUTHOR_INFO
                       + "' where Property='ARPHELPLINK'");
        sqlStrings.add("UPDATE Property SET Value='" + AUTHOR_INFO
                       + "' where Property='ComponentDownload'");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('ARPCONTACT','" + AUTHOR_INFO + "')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('ARPURLINFOABOUT','" + AUTHOR_INFO + "')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('ARPURLUPDATEINFO','" + AUTHOR_INFO + "')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('ARPCOMMENTS','Generated by JDIC Project')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('Shortcut','yes')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('Association','yes')");
        sqlStrings.add("INSERT INTO Property (Property,Value) VALUES "
                       + "('CacheType','user')");

        //Modify table InstallExecuteSequence
        sqlStrings.add("INSERT INTO InstallExecuteSequence (Action, Condition, "
                       + "Sequence) VALUES ('CustomInstallAction', "
                       + "'NOT Installed', 6650)");
        sqlStrings.add("INSERT INTO InstallExecuteSequence (Action, Condition, "
                       + "Sequence) VALUES ('CustomUninstallAction', "
                       + "'Installed', 6651)");
        //Modify table Dialog
        sqlStrings.add("UPDATE Dialog SET Control_Cancel='RemoveButton' where "
                       + "Dialog='MaintenanceTypeDlg'");
        //Modify table Control
        sqlStrings.add("UPDATE Control SET Attributes=0 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='ChangeLabel'");
        sqlStrings.add("UPDATE Control SET Attributes=5767168 where "
                       + "Dialog_='MaintenannceTypeDlg' and "
                       + "Control='ChangeButton'");
        sqlStrings.add("UPDATE Control SET Attributes=0 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='ChangeText'");
        sqlStrings.add("UPDATE Control SET Attributes=0 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='RepairLabel'");
        sqlStrings.add("UPDATE Control SET Attributes=5767168 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='RepairButton'");
        sqlStrings.add("UPDATE Control SET Attributes=0 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='RepairText'");
        sqlStrings.add("UPDATE Control SET Y=65 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='RemoveButton'");
        sqlStrings.add("UPDATE Control SET Y=65 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='RemoveLabel'");
        sqlStrings.add("UPDATE Control SET Y=78 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='RemoveText'");
        sqlStrings.add("UPDATE Control SET Y=78 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='RemoveText'");
        sqlStrings.add("UPDATE Control SET "
                       + "Text='[DlgTitleFont]Remove installation' where "
                       + "Dialog_='MaintenanceTypeDlg' and Control='Title'");
        sqlStrings.add("UPDATE Control SET Attributes=196608 where "
                       + "Dialog_='MaintenanceTypeDlg' and "
                       + "Control='Description'");
        sqlStrings.add("UPDATE Control SET Text='The [Wizard] is ready to "
                       + "begin the installation' where "
                       + "Dialog_='VerifyReadyDlg' and Control='Description'");
        sqlStrings.add("UPDATE Control SET Text='Click Install to begin the "
                       + "installation. Click Cancel to exit the wizard.' "
                       + "where Dialog_='VerifyReadyDlg' and Control='Text'");
        sqlStrings.add("UPDATE Control SET Text='The [Wizard] will allow you to"
                       + "remove [ProductName] from your computer. Click Next "
                       + "to continue or Cancel to exit the [Wizard].' where "
                       + "Dialog_='MaintenanceWelcomeDlg' and "
                       + "Control='Description'");
        sqlStrings.add("UPDATE Control SET Text='Click Remove to remove "
                       + "[ProductName] from your computer. Click Cancel to "
                       + "exit the wizard.' where Dialog_='VerifyRemoveDlg' "
                       + "and Control='Text'");
        sqlStrings.add("UPDATE Control SET Text='{\\DlgFontBold8}Remove "
                       + "[ProductName]' where Dialog_='VerifyRemoveDlg' "
                       + "and Control='Title'");
        sqlStrings.add("UPDATE Control SET Text='SunButton' where "
                       + "Dialog_='WelcomeDlg' and Control='Next'");
        sqlStrings.add("UPDATE Control SET Text='WelcomeDescription' where "
                       + "Dialog_='WelcomeDlg' and Control='Description'");
        sqlStrings.add("UPDATE Control SET Text='[ButtonText_Remove]' where "
                       + "Dialog_='MaintenanceWelcomeDlg' and Control='Next'");
        sqlStrings.add("UPDATE Control SET Text='The [Wizard] will allow you "
                       + "to remove [ProductName] from your computer. Click "
                       + "Remove to continue or Cancel to exit the [Wizard].' "
                       + "where Dialog_='MaintenanceWelcomeDlg' and "
                       + "Control='Description'");
        // Modify the ControlEvent table
        sqlStrings.add("DELETE FROM ControlEvent where Dialog_='WelcomeDlg' "
                       + "and Control_='Next'");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_, Event, "
                       + "Argument, Condition) VALUES ('WelcomeDlg', 'Next', "
                       + "'NewDialog', 'AfterWelcomeDlg', 1)");
        sqlStrings.add("DELETE from ControlEvent where "
                       + "Dialog_='LicenseAgreementDlg' and Control_='Next' "
                       + "and Event='NewDialog'");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_,Event, "
                       + "Argument, Condition, Ordering) VALUES "
                       + "('LicenseAgreementDlg', 'Next', 'EndDialog', "
                       + "'Return', 'IAgree = \"Yes\"', 1)");
        sqlStrings.add("DELETE FROM ControlEvent where "
                       + "Dialog_='MaintenanceWelcomeDlg' and Control_='Next'");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_, Event, "
                       + "Argument, Condition, Ordering) VALUES "
                       + "('MaintenanceWelcomeDlg', 'Next', '[InstallMode]', "
                       + "'Remove', '1', 1)");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_, Event, "
                       + "Argument, Condition, Ordering) VALUES "
                       + "('MaintenanceWelcomeDlg', 'Next', '[Progress1]', "
                       + "'Removing', '1', 2)");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_, Event, "
                       + "Argument, Condition, Ordering) VALUES "
                       + "('MaintenanceWelcomeDlg', 'Next', '[Progress2]', "
                       + "'removes', '1', 3)");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_, Event, "
                       + "Argument, Condition, Ordering) VALUES "
                       + "('MaintenanceWelcomeDlg', 'Next', 'SpawnWaitDialog', "
                       + "'WaitForCostingDlg', 'CostingComplete = 1', 4)");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_, Event, "
                       + "Argument, Condition, Ordering) VALUES "
                       + "('MaintenanceWelcomeDlg', 'Next', 'Remove', "
                       + "'All', '1', 5)");
        sqlStrings.add("INSERT INTO ControlEvent (Dialog_, Control_, Event, "
                       + "Argument, Condition, Ordering) VALUES "
                       + "('MaintenanceWelcomeDlg', 'Next', 'EndDialog', "
                       + "'Return', '1', 6)");
        WinMsiUtility.runSql(msiFilePath, sqlStrings);

        // Create a temp place.txt file with certain content in it.
        File tempTxtFile = new File(pkgInfo.getUniqueTmpDirPath()
                                    + "place.txt");
        FileOutputStream fis = new FileOutputStream(tempTxtFile);
        String contentString = new String(AUTHOR_INFO);
        byte[] bits = contentString.getBytes();
        fis.write(bits, 0, contentString.length());
        fis.close();

        // Insert the place.txt into msi, in form of cab
        // Evaluate if makecab.exe and msidb.exe exist in MSSDKPath.
        String makecabFullPath = pkgInfo.getMSSDKDirPath() + MAKECAB_EXE_PATH;
        String msidbFullPath = pkgInfo.getMSSDKDirPath() + MSIDB_EXE_PATH; 
        File temFile = new File(makecabFullPath);
        if (!temFile.exists()) {
            throw new IOException("Can not locate makecab.exe at" +
                                   makecabFullPath);
        }
        temFile = new File(msidbFullPath);
        if (!temFile.exists()) {
            throw new IOException(
                "Can not locate msidb.exe at" +
                msidbFullPath);
        }
        String tempCabFilePath = pkgInfo.getUniqueTmpDirPath() + "Data1.cab";
        Process proc = Runtime.getRuntime().exec(
                "\"" + makecabFullPath + "\" " + tempTxtFile.toString()
                + " " + tempCabFilePath);
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        proc = Runtime.getRuntime().exec(
                "\"" + msidbFullPath + "\" -d " + msiFilePath +
                " -a " + tempCabFilePath);
        try {
            proc.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        
        //Import the localized WelcomeMsg table into the template MSI file.
        String tempWelcomeMsgFilePath = pkgInfo.getUniqueTmpDirPath()
                                        + LOCALIZED_WELCOME_MSG_FILE_NAME;
        String sysWelcomeMsgFilePath = MSI_SUPPORT_FILES_HIERARCHY_PATH
                                        + LOCALIZED_WELCOME_MSG_FILE_NAME;
        WinUtility.extractFileFromJarFile(
                    packagerJarFilePath,
                    sysWelcomeMsgFilePath,
                    tempWelcomeMsgFilePath);
        WinMsiUtility.importTableFromFile(
                    msiFilePath,
                    pkgInfo.getUniqueTmpDirPath(),
                    LOCALIZED_WELCOME_MSG_FILE_NAME);
    }

    /**
     * Generates the msi installation package.
     *
     * @param pkgInfo The given jnlp package info.
     * @throws IOException If failed to generate the MSi package.
     */
    public final void generatePackage(JnlpPackageInfo pkgInfo)
                throws IOException {
        //get the location of packager.jar
        getPackagerJarFilePath();
        // Get common Info for all locale packages

        // Copy user defined raw Msi file to the temp dir as en.msi
        String enMsiFilePath = pkgInfo.getUniqueTmpDirPath() + "en.msi";
        String rawMsiFilePath = pkgInfo.getRawMsiFilePath();
        FileOperUtility.copyLocalFile(rawMsiFilePath, enMsiFilePath);

        //Generate the en.msi
        createTemplateMsiFile(enMsiFilePath, pkgInfo);
        customizeNonLocalizedMsiFields(enMsiFilePath, pkgInfo);
        //Put JavaWS product name into MSI file
        putProductNameIntoMsi(enMsiFilePath, pkgInfo, JnlpConstants.LOCALE_EN);
        // Adjust the relevant fields to reflect current show license setting
        adjustMsiLicenseFields(enMsiFilePath, pkgInfo, JnlpConstants.LOCALE_EN);

        //Generate the localized MSI file if required
        if (pkgInfo.getLocalizationEnabled()) {
            // Genreate a locale-specific msi file in every loop
            for (int i = 1; i < JnlpConstants.LOCALES.length; i++) {
                // Copy en.msi to localized msi file
                String localizedMsiFilePath =
                    pkgInfo.getUniqueTmpDirPath()
                        + JnlpConstants.LOCALES[i]
                        + ".msi";
                FileOperUtility.copyLocalFile(enMsiFilePath,
                        localizedMsiFilePath);

                //Import the localized tables into target MSI file
                importLocalizedTables(localizedMsiFilePath, pkgInfo, i);

                //Put localized JavaWS product name into MSI file
                putProductNameIntoMsi(localizedMsiFilePath, pkgInfo, i);

                // Adjust the relevant fields to reflect current show
                // license setting
                adjustMsiLicenseFields(localizedMsiFilePath, pkgInfo, i);
            }
        }
        generateBootStrapper(pkgInfo);
    }

    /**
     * Import the localized MSI tables into the target MSI file, includes
     * ActionText, Error, Control & ControlEvent.
     * @param localizedMsiFilePath The localized MSI file path.
     * @param pkgInfo   The given JNLP package info.
     * @param localeIndex   The index of the current locale
     * @throws IOException If failed to import these tables.
     */
    public final void importLocalizedTables(String localizedMsiFilePath,
                                      JnlpPackageInfo pkgInfo,
                                      int localeIndex) throws IOException {
        ArrayList sqlStrings = new ArrayList();
        // If Localization support enabled, we will import the
        // "ActionText", "Error", "Control", "ControlEvent"
        sqlStrings.add("DROP TABLE ActionText");
        sqlStrings.add("DROP TABLE Error");
        sqlStrings.add("DROP TABLE Control");
        sqlStrings.add("DROP TABLE ControlEvent");

        WinMsiUtility.runSql(localizedMsiFilePath, sqlStrings);

        //Extract the tables from jar files
        String localizedControlTableFileName =
                "Control." + JnlpConstants.LOCALES_SUFFIX[localeIndex];
        String localizedControlTableFilePath =
                pkgInfo.getUniqueTmpDirPath() + localizedControlTableFileName;
        String localizedControlEventTableFileName =
                "ControlEvent." + JnlpConstants.LOCALES_SUFFIX[localeIndex];
        String localizedControlEventTableFilePath =
                pkgInfo.getUniqueTmpDirPath()
                + localizedControlEventTableFileName;

        WinUtility.extractFileFromJarFile(
            packagerJarFilePath,
            MSI_SUPPORT_FILES_HIERARCHY_PATH + localizedControlTableFileName,
            localizedControlTableFilePath);

        WinUtility.extractFileFromJarFile(
            packagerJarFilePath,
            MSI_SUPPORT_FILES_HIERARCHY_PATH
            + localizedControlEventTableFileName,
            localizedControlEventTableFilePath);

        // Import the ActionText table
        WinMsiUtility.importTableFromFile(
            localizedMsiFilePath,
            pkgInfo.getUniqueTmpDirPath(),
            "ActionTe." + JnlpConstants.LOCALES_SUFFIX[localeIndex]);

        // Import the Error table
        WinMsiUtility.importTableFromFile(
                localizedMsiFilePath,
                pkgInfo.getUniqueTmpDirPath(),
                "Error" + JnlpConstants.LOCALES_SUFFIX[localeIndex]);

        //Import the Control table
        WinMsiUtility.importTableFromFile(
                localizedMsiFilePath,
                pkgInfo.getUniqueTmpDirPath(),
                localizedControlTableFileName);

        //Import the Control event table
        WinMsiUtility.importTableFromFile(
                localizedMsiFilePath,
                pkgInfo.getUniqueTmpDirPath(),
                localizedControlEventTableFileName);

    }

    /**
     * Put localized product name into the MSI file.
     *
     * @param localizedMsiFilePath The localized MSI file path.
     * @param pkgInfo   The given jnlp package info.
     * @param localeIndex    The index of current locale.
     * @throws IOException If failed to put the product name into MSI.
     */
    public final void putProductNameIntoMsi(String localizedMsiFilePath,
                                            JnlpPackageInfo pkgInfo,
                                            int localeIndex)
                                            throws IOException {
        TreeMap treeMap = new TreeMap();
        try {
            treeMap.clear();
            treeMap.put(
                "ProductName",
                pkgInfo.getLocalizedJnlpInfo(
                        JnlpConstants.LOCALES[localeIndex],
                        JnlpConstants.JNLP_FIELD_TITLE));
            treeMap.put("Locale", JnlpConstants.LOCALES[localeIndex]);
            WinMsiUtility.winMsiSetProperty(
                    localizedMsiFilePath,
                    "Property",
                    "Property",
                    2,
                    false,
                    treeMap);
        } catch (IOException e) {
            throw new IOException(
                "Property Table: Modify ProductName field failed!");
        }
    }

    /**
     * Generate the target boot strapper file.
     *
     * @param pkgInfo The given package information.
     * @todo Get the target bootstrapper file name
     *
     * @throws IOException If failed to generate the bootstrapper.
     */
    public final void generateBootStrapper(JnlpPackageInfo pkgInfo)
        throws IOException {
        //Get the MSI file name for locale en, The localized msi file naming
        // schema would be locale.msi
        //e.g. zh_cn.msi
        String enMsiFilePath =
            pkgInfo.getUniqueTmpDirPath()
                + JnlpConstants.LOCALES[JnlpConstants.LOCALE_EN]
                + ".msi";

        //If there is localization requirement, we'll then generate the 9
        //mst files and incorporate them into the en.msi
        if (pkgInfo.getLocalizationEnabled()) {
            // Genreate a locale-specific mst file in every loop, the msi file
            // naming schema would be locale.mst, e.g. zh_cn.mst
            String localizedMsiFilePath;
            String targetMstFilePath;
            for (int i = 1; i < JnlpConstants.LOCALES.length; i++) {
                localizedMsiFilePath =
                    pkgInfo.getUniqueTmpDirPath()
                        + JnlpConstants.LOCALES[i]
                        + ".msi";
                targetMstFilePath =
                    pkgInfo.getUniqueTmpDirPath()
                        + JnlpConstants.LOCALES[i]
                        + ".mst";
                WinMsiUtility.generateTransform(
                    enMsiFilePath,
                    localizedMsiFilePath,
                    targetMstFilePath);
            }

            // Incorporate the 9 mst files into file en.msi, these mst files
            // woule be insert into _Storage table with each name as
            // cust_locale, e.g. cust_zh_cn, cust_fr.
            String newFieldName;
            for (int i = 1; i < JnlpConstants.LOCALES.length; i++) {
                targetMstFilePath =
                    pkgInfo.getUniqueTmpDirPath()
                        + JnlpConstants.LOCALES[i]
                        + ".mst";
                newFieldName = "cust_" + JnlpConstants.LOCALES[i];
                WinMsiUtility.incorporateMST(
                    enMsiFilePath,
                    targetMstFilePath,
                    newFieldName);
            }
        }

        //Extract the bootstrapper from the jar file
        String tempBootStrapperFilePath =
            pkgInfo.getUniqueTmpDirPath() + pkgInfo.getPackageName() + ".exe";
        WinUtility.extractFileFromJarFile(
            packagerJarFilePath,
            SYS_BOOTSTRAPPER_FILE_PATH,
            tempBootStrapperFilePath);

        //Store an UUID into the bootstrapper resource, which can be used to
        // identify whether there
        //are more than one instance running
        String exeUUID = WinMsiUtility.genUUID();
        WinUtility.updateResourceString(
            tempBootStrapperFilePath,
            exeUUID,
            WinUtility.STRING_RES_UUID_ID);

        //Incorporate the en.msi into the bootstrpper resources
        WinUtility.updateResourceData(
            tempBootStrapperFilePath,
            enMsiFilePath,
            WinUtility.BIN_RES_ID);

        //Store a flag string indicating whether localization is supported
        String localizationFlag;
        if (pkgInfo.getLocalizationEnabled()) {
            localizationFlag = "localization supported";
        } else {
            localizationFlag = "no localization support";
        }
        WinUtility.updateResourceString(
            tempBootStrapperFilePath,
            localizationFlag,
            WinUtility.STRING_RES_LOCALIZATION_FLAG_ID);

        //Copy the generated bootstrapper from temp dir to target dir
        String targetBootStrapperFilePath =
            pkgInfo.getOutputDirPath() + pkgInfo.getPackageName() + ".exe";
        FileOperUtility.copyLocalFile(tempBootStrapperFilePath,
                                 targetBootStrapperFilePath);
        //Indicate user where the file has been generated
        File targetFile = new File(targetBootStrapperFilePath);
        if (targetFile.exists()) {
            System.out.println("\nSuccess: The bootstrapper MSI file has been" +
                " generated at: \n" + targetBootStrapperFilePath + "\n");   
        }

        //Remove the Unique Tmp Dir
        File tempDir = new File(pkgInfo.getUniqueTmpDirPath());
        FileOperUtility.deleteDirTree(tempDir);
    }
}
