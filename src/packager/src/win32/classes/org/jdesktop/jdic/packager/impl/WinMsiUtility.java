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

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.IOException;

/**
 * WinMsiUtility provide some high level Msi APIs.
 */
public class WinMsiUtility {

    /**
     * Flag for success operation.
     */
    private static final int ERROR_SUCCESS = WinMsiWrapper.ERROR_SUCCESS;
    /**
     * Flag for failed operation.
     */
    private static final int ERROR_FAIL = -1;
    /**
     * Field index for licensed welcome index in table welcomemsg.
     */
    private static final int LICENSED_WELCOME_MSG_FIELD_INDEX = 2;
    /**
     * Field index for non licensed welcome index in table welcomemsg.
     */
    private static final int NON_LICENSED_WELCOME_MSG_FIELD_INDEX = 3;

    /**
     * Sets the property field of summary information stream.
     *
     * @param msiFilePath The msi database file path.
     * @param uiProperty Specify the property field to be changed.
     * @param strValue  Specify the property field value.
     * @throws IOException If failed to sets the property field.
     */
    public static void setSummaryInfoProperty(String msiFilePath,
                                              int uiProperty,
                                              String strValue)
                                              throws IOException {
        //Handle to the database elements
        int hDatabase = 0, hSummaryInfo = 0;
        try {
            //Opens the MSI database
            hDatabase = openDatabase(msiFilePath);
            //Database has been sucessfully opened
            hSummaryInfo = openSummaryInfo(hDatabase);
            WinMsiWrapper.winMsiSummaryInfoSetProperty(hSummaryInfo,
                                                       uiProperty,
                                                       strValue);
            WinMsiWrapper.winMsiSummaryInfoPersist(hSummaryInfo);
            WinMsiWrapper.winMsiDatabaseCommit(hDatabase);
        } finally {
            WinMsiWrapper.winMsiCloseHandle(hSummaryInfo);
            WinMsiWrapper.winMsiCloseHandle(hDatabase);
            WinMsiWrapper.winMsiCloseAllHandles();
        }
    }

    /**
     * Set the MSI fields property in the given table.
     *
     * @param msiFilePath The MSI file path.
     * @param tableName The table to be checked
     * @param keyFieldName  Name of the key field
     * @param valueIndex index of the value column
     * @param isBinary Specify whether we set the binary stream or text field
     *        property
     * @param map TreeMap containing the key-value pair to be replaced with
     * @throws IOException If failed to set the MSI field property.
     */
    public static void winMsiSetProperty(String msiFilePath,
                                         String tableName,
                                         String keyFieldName,
                                         int valueIndex,
                                         boolean isBinary,
                                         TreeMap map) throws IOException {
        ///////////////////////////////////////////////////////////////
        //Parameter checking first
        //table name should not be null
        if (tableName == null) {
            throw new IllegalArgumentException("MSI table name can't be null");
        }
        //keyFieldName should not be null
        if (keyFieldName == null) {
            throw new IllegalArgumentException(
                    "MSI key field name can't be null");
        }
        //valueIndex should not be <= 0
        if (valueIndex <= 0) {
            throw new IllegalArgumentException("MSI value index can't be null");
        }
        //map should not be null
        if (map == null) {
            throw new IllegalArgumentException("map data can't be null!");
        }
        //If the map contains no data, just return.
        if (map.size() == 0) {
            return;
        }
        ///////////////////////////////////////////////////////////////
        //Begin processing
        //Handle to the database elements
        int hDatabase = 0;
        int hView = 0;
        int hRecord = 0;
        //flag to indicate if one record has been replaced, default to false
        boolean recordsReplaced = false;
        try {
            //Opens the MSI database
            hDatabase = openDatabase(msiFilePath);
            //Database has been sucessfully opened
            Iterator it = map.keySet().iterator();
            String keyName = null;
            String newKeyValue = null;
            while (it.hasNext()) {
                keyName = (String) it.next();
                newKeyValue = (String) map.get(keyName);
                if (isBinary) {
                    //To set the file stream, a file validation check is needed
                    WinUtility.checkFileValid(newKeyValue);
                }
                String fieldNames = "*";
                String criterial = " where "
                                   + keyFieldName + " = '" + keyName + "'";
                hView = openView(hDatabase, tableName, fieldNames, criterial);
                hRecord = getRecord(hView);
                if (hRecord != 0) {
                    //We've got non-null record here
                    setRecordProperty(hView, hRecord, valueIndex,
                                    isBinary, newKeyValue);
                    recordsReplaced = true;
                }
            }
            //Only commit the database if
            //1. All the fields has been successfuly replaced.
            //2. There is an actual record field replacement.
            if (recordsReplaced) {
                WinMsiWrapper.winMsiDatabaseCommit(hDatabase);
            }
        } finally {
            closeView(hView);
            WinMsiWrapper.winMsiCloseHandle(hDatabase);
            WinMsiWrapper.winMsiCloseAllHandles();
        }
    }

    /**
     * Generated the MSI transform MST file based on the given MSI files.
     *
     * @param database           The given MSI file.
     * @param databaseReference  The given MSI file as the transform reference.
     * @param transformFile      The target transform file.
     *
     * @throws IOException If failed to generate the transform file.
     */
    public static void generateTransform(String database,
                                         String databaseReference,
                                         String transformFile)
                                         throws IOException {
        int hDatabase = 0;
        int hDatabaseReference = 0;
        try {
            hDatabase = openDatabase(database);
            hDatabaseReference = openDatabase(databaseReference);
            WinMsiWrapper.winMsiDatabaseGenerateTransform(hDatabase,
                                                          hDatabaseReference,
                                                          transformFile);
            WinMsiWrapper.winMsiCreateTransformSummaryInfo(hDatabase,
                                                           hDatabaseReference,
                                                           transformFile);
        } finally {
            WinMsiWrapper.winMsiCloseHandle(hDatabase);
            WinMsiWrapper.winMsiCloseHandle(hDatabaseReference);
        }
    }

    /**
     * Running sql on the given MSI database.
     *
     * @param msiFilePath The given MSI database file path.
     * @param sqlStrings The given sql string.
     *
     * @throws IOException If failed to run the sql.
     */
    public static void runSql(String msiFilePath, ArrayList sqlStrings)
                            throws IOException {
        int hDatabase = 0;
        int hView = 0;
        try {
            hDatabase = openDatabase(msiFilePath);
            int[] result = new int[] {0, 0};
            String sqlString;
            for (Iterator i = sqlStrings.iterator(); i.hasNext();) {
                sqlString = (String) i.next();
                result = WinMsiWrapper.winMsiDatabaseOpenView(hDatabase,
                                                              sqlString);
                hView = result[1];
                WinMsiWrapper.winMsiViewExecute(hView, 0);
            }
            WinMsiWrapper.winMsiDatabaseCommit(hDatabase);
        } finally {
            closeView(hView);
            WinMsiWrapper.winMsiCloseHandle(hDatabase);
            WinMsiWrapper.winMsiCloseAllHandles();
        }
    }

    /**
     * Add a record with binary fields.
     *
     * @param msiFilePath The given msi file path.
     * @param tableName The given table name.
     * @param fieldNames    The field names.
     * @param fieldProperties   The fields properties
     *                          ('String' for general field,
     *                          'stream' for binary field).
     * @param fieldValues   The value of the field (For general field, it should
     *                       be the string value, for binary field, it should
     *                       be the file name).
     *
     * @throws IOException If failed to add the binary record.
     */
    public static void addBinaryRecord(String msiFilePath,
                                       String tableName,
                                       String[] fieldNames,
                                       String[] fieldProperties,
                                       String[] fieldValues)
                                       throws IOException {
       int hMsiDatabase = 0;
       int hView = 0;
       int hRecord = 0;
       try {
            hMsiDatabase = openDatabase(msiFilePath);
            String fieldsName = fieldNames[0];
            for (int i = 1; i < fieldNames.length; i++) {
                fieldsName += ", " + fieldNames[i];
            }
            hView = openView(hMsiDatabase, tableName, fieldsName, "");
            hRecord = WinMsiWrapper.winMsiCreateRecord(fieldNames.length);
            for (int i = 0; i < fieldNames.length; i++) {
                if ((fieldProperties[i].compareToIgnoreCase("String")) == 0) {
                    WinMsiWrapper.winMsiRecordSetString(hRecord, i + 1,
                                                        fieldValues[i]);
                    WinMsiWrapper.winMsiViewExecute(hView, hRecord);
                }
                if ((fieldProperties[i].compareToIgnoreCase("Stream")) == 0) {
                    WinMsiWrapper.winMsiRecordSetStream(hRecord, i + 1,
                                                        fieldValues[i]);
                    WinMsiWrapper.winMsiViewModify(
                                  hView,
                                  WinMsiWrapper.MSIMODIFY_ASSIGN,
                                  hRecord);
                }
            }
           WinMsiWrapper.winMsiDatabaseCommit(hMsiDatabase);
       } finally {
           WinMsiWrapper.winMsiCloseHandle(hRecord);
           closeView(hView);
           WinMsiWrapper.winMsiCloseHandle(hMsiDatabase);
       }
    }
    /**
     * Incorporate the mst file into the msi database "_Storages" table with
     *  the name as fieldsName.
     * @param msiFilePath Given msi file path.
     * @param mstFilePath Given mst file path.
     * @param fieldName Given field name.
     * @throws IOException If failed to incorporate the MST file.
     */
    public static void incorporateMST(String msiFilePath,
                                      String mstFilePath,
                                      String fieldName)
                                      throws IOException {
        String[] fieldNames = new String[] {"Name", "Data"};
        String tableName = "_Storages";
        String[] fieldProperties = new String[] {"String", "Stream"};
        String[] fieldValues = new String[] {fieldName, mstFilePath};
        addBinaryRecord(msiFilePath, tableName, fieldNames,
                        fieldProperties, fieldValues);
    }

    /**
     * Import a table from a table txt file representation.
     * @param msiFilePath   The msi file path.
     * @param folderPath    The directory where the txt file locates.
     * @param txtTableName  The txt file name.
     * @throws IOException If failed to import the table from the file.
     */
    public static void importTableFromFile(String msiFilePath,
                                           String folderPath,
                                           String txtTableName)
                                           throws IOException {
       int hDatabase = 0;
       try {
           hDatabase = openDatabase(msiFilePath);
           WinMsiWrapper.winMsiDatabaseImport(hDatabase,
                                              folderPath,
                                              txtTableName);
           WinMsiWrapper.winMsiDatabaseCommit(hDatabase);
       } finally {
           WinMsiWrapper.winMsiCloseHandle(hDatabase);
       }
    }

    /**
     * Appy the MST transform onto the MSI file.
     * @param msiFilePath The specified MSI path.
     * @param mstFilePath The specified MST path.
     * @throws IOException If failed to apply the transform.
     */
    public static void applyMstToMsi(String msiFilePath,
                                     String mstFilePath)
                                     throws IOException {
        int[] result = new int[] {0, 0};
        try {
            result = WinMsiWrapper.winMsiOpenDatabase(
                                        msiFilePath,
                                        WinMsiWrapper.MSIDBOPEN_TRANSACT);
            //Get the database handle if succeed
            int hDatabase = result[1];
            WinMsiWrapper.winMsiDatabaseApplyTransform(hDatabase, mstFilePath);
            WinMsiWrapper.winMsiDatabaseCommit(hDatabase);
            WinMsiWrapper.winMsiCloseHandle(hDatabase);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Sets the record value field with the new value.
     * @param hView The given view.
     * @param hRecord The given record.
     * @param valueIndex The value column index.
     * @param isBinary Specify whether the field is binary field.
     * @param newValue New value to be replaced with.
     * @throws IOException If fail to set the record property.
     */
    private static void setRecordProperty(int hView,
                                          int hRecord,
                                          int valueIndex,
                                          boolean isBinary,
                                          String newValue)
                                          throws IOException {
        try {
            if (isBinary) {
                WinMsiWrapper.winMsiRecordSetStream(hRecord,
                                                    valueIndex,
                                                    newValue);
            } else {
                WinMsiWrapper.winMsiRecordSetString(hRecord,
                                                    valueIndex,
                                                    newValue);
            }
            WinMsiWrapper.winMsiViewModify(hView,
                                           WinMsiWrapper.MSIMODIFY_REPLACE,
                                           hRecord);
        } finally {
            WinMsiWrapper.winMsiCloseHandle(hRecord);
        }
    }

    /**
     * Opens the given databse.
     * @param msiFilePath Database file path.
     * @return The handle to the opened MSI database.
     * @throws IOException If fail to open the MSI database.
     */
    private static int openDatabase(String msiFilePath) throws IOException {
        int[] result = new int[] {ERROR_FAIL, 0};
        result = WinMsiWrapper.winMsiOpenDatabase(
                               msiFilePath,
                               WinMsiWrapper.MSIDBOPEN_TRANSACT);
        //return the database handle if succeed
        return result[1];
    }

    /**
     * Opens the msi summary information stream.
     * @param hDatabase Handle of the msi database opened.
     * @return The summary information stream handle.
     * @throws IOException If failed to open the Summary Info stream.
     */
    private static int openSummaryInfo(int hDatabase) throws IOException {
        int[] result = new int[] {ERROR_FAIL, 0};
        result = WinMsiWrapper.winMsiGetSummaryInformation(hDatabase);
        //return the summary information stream handle
        return result[1];
    }

    /**
     * Opens a table in the given database.
     * @param hDatabase The database handle.
     * @param tableName The name of the table to be opened.
     * @param fieldNames The fields to be selected.
     * @param criterial The records selecting criterial.
     * @return Handle to the opened view.
     * @throws IOException If fail to open the view.
     */
    private static int openView(int hDatabase,
                                String tableName,
                                String fieldNames,
                                String criterial)
                                throws IOException {
        int[] result = new int[] {ERROR_FAIL, 0};
        //Constructs a sql string
        String sqlSelectStr = "select " + fieldNames + " from " + tableName
                              + " " + criterial;
        result = WinMsiWrapper.winMsiDatabaseOpenView(hDatabase, sqlSelectStr);
        int hView = result[1];
        WinMsiWrapper.winMsiViewExecute(hView, 0);
        return hView;
    }

    /**
     * Close the view and view handle.
     * @param hView Handle to view.
     * @throws IOException If fail to close the view.
     */
    private static void closeView(int hView) throws IOException {
        WinMsiWrapper.winMsiViewClose(hView);
        WinMsiWrapper.winMsiCloseHandle(hView);
    }

    /**
     * Returns the handle to the record.
     * @param hView The given view.
     * @return Handle to the record if succeed.
     * @throws IOException If fail to get the record.
     */
    private static int getRecord(int hView) throws IOException {
       int[] result;
       result = WinMsiWrapper.winMsiViewFetch(hView);
       if (result[0] == WinMsiWrapper.ERROR_NO_MORE_ITEMS) {
           //No records found, just return zero (indicating null record)
           return 0;
       } else {
           //return a valid record handle if succeed
           return result[1];
       }
    }

    /**
     * Gets the localized welcome msg according to license display status.
     * @param msiFilePath The given msi file path.
     * @param locale The name of the current locale.
     * @param isShowLicense Whether the license information gets displayed.
     * @return The localized welcome message (according to isShowLicense).
     * @throws IOException If failed to get the welcome message.
     */
    public static String getWelcomeMsg(String msiFilePath,
                                        String locale,
                                        boolean isShowLicense)
                                        throws IOException {
        int hDatabase = 0;
        int hView = 0;
        int hRecord = 0;
        try {
            hDatabase = openDatabase(msiFilePath);
            String tableName =
                    MsiPackageGenerator.LOCALIZED_WELCOME_MSG_TABLE_NAME;
            String fieldNames = "*";
            String criterial = " where "
                               + "Locale = '"
                               + locale
                               + "'";
            hView = openView(hDatabase, tableName, fieldNames, criterial);
            hRecord = getRecord(hView);
            String welcomeMsg = null;
            if (isShowLicense) {
                welcomeMsg = WinMsiWrapper.winMsiRecordGetString(
                                hRecord,
                                LICENSED_WELCOME_MSG_FIELD_INDEX);
            } else {
                welcomeMsg = WinMsiWrapper.winMsiRecordGetString(
                                hRecord,
                                NON_LICENSED_WELCOME_MSG_FIELD_INDEX);
            }
            return welcomeMsg;
        } finally {
            WinMsiWrapper.winMsiCloseHandle(hRecord);
            closeView(hView);
            WinMsiWrapper.winMsiCloseHandle(hDatabase);
        }
    }

    /**
     * Generate a UUID String.
     * @return String representing the UUID.
     * @throws IOException If fail to generate the UUID.
     */
    public static String genUUID() throws IOException {
        String oneUUID = WinMsiWrapper.generateUUID();
        if (oneUUID != null) {
            return oneUUID.toUpperCase();
        } else {
            throw new IOException("Could not generate the UUID.");
        }
    }
}
