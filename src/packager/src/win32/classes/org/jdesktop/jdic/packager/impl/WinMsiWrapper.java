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
/**
 * Bottom layer java wrapper for Windows MSI APIs.
 */
public class WinMsiWrapper {

    static {
        System.loadLibrary("WinMsiWrapper");
    }

    //MsiOpenDatabase constants
    /**
     * Create a new database, transact mode read/write.
     */
    public static final int MSIDBOPEN_CREATE         = 3;
    /**
     * Open a database direct read/write without transaction.
     */
    public static final int MSIDBOPEN_DIRECT         = 2;
    /**
     * Open a database read-only, no persistent changes.
     */
    public static final int MSIDBOPEN_READONLY       = 0;
    /**
     * Open a database read/write in transaction mode.
     */
    public static final int MSIDBOPEN_TRANSACT       = 1;

    //MsiViewModify mode constants
    /**
     * Refreshes the information in the supplied record without changing the
     * position in the result set and without affecting subsequent
     * fetch operations.
     */
    public static final int MSIMODIFY_SEEK             = -1;
    /**
     * Refreshes the information in the record. Must first call MsiViewFetch
     * with the same record.
     */
    public static final int MSIMODIFY_REFRESH          = 0;
    /**
     * Inserts a record.
     */
    public static final int MSIMODIFY_INSERT           = 1;
    /**
     * Updates an existing record.
     */
    public static final int MSIMODIFY_UPDATE           = 2;
    /**
     * Writes current data in the cursor to a table row.
     */
    public static final int MSIMODIFY_ASSIGN           = 3;
    /**
     * Updates or deletes and inserts a record into a table.
     */
    public static final int MSIMODIFY_REPLACE          = 4;
    /**
     * Inserts or validates a record in a table.
     */
    public static final int MSIMODIFY_MERGE            = 5;
    /**
     * Remove a row from the table.
     */
    public static final int MSIMODIFY_DELETE           = 6;
    /**
     * Inserts a temporary record.
     */
    public static final int MSIMODIFY_INSERT_TEMPORARY = 7;
    /**
     * Validates a record.
     */
    public static final int MSIMODIFY_VALIDATE         = 8;
    /**
     * Validate a new record.
     */
    public static final int MSIMODIFY_VALIDATE_NEW     = 9;
    /**
     * Validates fields of a fetched or new record.
     */
    public static final int MSIMODIFY_VALIDATE_FIELD   = 10;
    /**
     * Validates a record that will be deleted later.
     */
    public static final int MSIMODIFY_VALIDATE_DELETE  = 11;

    //Error code
    /**
     * The function succeeded.
     */
    public static final int ERROR_SUCCESS               = 0;
    /**
     * Access was not permitted.
     */
    public static final int ERROR_ACCESS_DENIED         = 5;
    /**
     * An invalid or inactive handle was supplied.
     */
    public static final int ERROR_INVALID_HANDLE        = 6;
    /**
     * A validation was requested and the data did not pass.
     */
    public static final int ERROR_INVALID_DATA          = 13;
    /**
     * One of the parameters was invalid.
     */
    public static final int ERROR_INVALID_PARAMETER     = 87;
    /**
     * The database could not be opened as requested.
     */
    public static final int ERROR_OPEN_FAILED           = 110;
    /**
     * An invalid path was supplied.
     */
    public static final int ERROR_BAD_PATHNAME          = 161;
    /**
     * No records remain, and a NULL handle is returned.
     */
    public static final int ERROR_NO_MORE_ITEMS         = 259;
    /**
     * The handle is in an invalid state.
     */
    public static final int ERROR_INVALID_HANDLE_STATE  = 1609;
    /**
     * An invalid SQL query string was passed to the function.
     */
    public static final int ERROR_BAD_QUERY_SYNTAX      = 1615;
    /**
     * The database could not be created.
     */
    public static final int ERROR_CREATE_FAILED         = 1631;
    /**
     * A view could not be executed.
     */
    public static final int ERROR_FUNCTION_FAILED       = 1627;

    /**
     * Java wrapper for Windows MSI API MsiOpenDatabase.
     * @param databasePath Specifies the full path or relative path to the
     *        database file
     * @param persist Receives the full path to the file or the persistence
     *        mode. You can use the szPersist parameter to direct the
     *        persistent output to a new file or to specify one of the
     *        following predefined persistence modes.
     * @return   result[0]: Pointer to the location of the returned database
     *           handle if succeed.
     *           result[1]: error code.
     */
    private static native int[] msiOpenDatabase(byte[] databasePath,
                                                       int persist);

    /**
     * Java wrapper for Windows MSI API MsiDatabaseOpenView.
     * @param hDatabase Handle to the database to which you want to open
     *        a view object.
     * @param szQuery Specifies a SQL query string for querying the database.
     * @return result[0]: error code.
     *         result[1]: Pointer to a handle for the returned view.
     */
    private static native int[] msiDatabaseOpenView(int hDatabase,
                                                    byte[] szQuery);

    /**
     * Java wrapper for Windows MSI API MsiViewExecute.
     * @param hView Handle to the view upon which to execute the query.
     * @param hRecord Handle to a record that supplies the parameters.
     * @return error_code
     */
    private static native int msiViewExecute(int hView, int hRecord);

    /**
     * Java wrapper for Windows MSI API MsiViewFetch.
     * @param hView Handle to the view to fetch from.
     * @return result[0]: error_code.
     *         result[1]: handle for the fetched record.
     */
    private static native int[] msiViewFetch(int hView);

    /**
     * Java wrapper for Windows MSI API MsiRecordGetString.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field requested.
     * @return the returned string
     */
    private static native byte[] msiRecordGetString(int hRecord, int iField);

    /**
     * Java wrapper for Windows MSI API MsiRecordSetString.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field of the record to set.
     * @param szValue Specifies the string value of the field.
     * @return error_code
     */
    private static native int msiRecordSetString(int hRecord,
                                                 int iField,
                                                 byte[] szValue);

    /**
     * Java wrapper for Windows MSI API MsiViewModify.
     * @param hView Handle to a view.
     * @param eModifyMode Specifies the modify mode.
     * @param hRecord Handle to the record to modify.
     * @return error_code
     */
    private static native int msiViewModify(int hView,
                                            int eModifyMode,
                                            int hRecord);

    /**
     * Java wrapper for Windows MSI API MsiRecordGetFieldCount.
     * @param hRecord Handle to a record.
     * @return If the function succeeds, the return value is the number of
     *         fields in the record. If the function is given an invalid or
     *         inactive handle, it returns -1 or 0xFFFFFFFF.
     */
    private static native int msiRecordGetFieldCount(int hRecord);

    /**
     * Java Wrapper for Windows API MsiCreateRecord.
     * @param numRecords Number of fields to be created in this record
     * @return If the function succeeds, the return type is the handle to
     *         the record.
     */
    private static native int msiCreateRecord(int numRecords);

    /**
     * Java wrapper for Windows MSI API MsiViewClose.
     * @param hView Handle to a view that is set to release.
     * @return error_code
     */
    private static native int msiViewClose(int hView);

    /**
     * Java wrapper for Windows MSI API MsiDatabaseCommit.
     * @param hDatabase Handle to the database.
     * @return error_code
     */
    private static native int msiDatabaseCommit(int hDatabase);

    /**
     * Java wrapper for Windows MSI API MsiCloseHandle.
     * @param hAny Specifies any open installation handle.
     * @return error_code
     */
    private static native int msiCloseHandle(int hAny);

    /**
     * Java wrapper for Windows MSI API MsiRecordSetStream.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field of the record to set.
     * @param szFilePath Specifies the path to the file containing the stream.
     * @return error_code
     */
    private static native int msiRecordSetStream(int hRecord,
                                                 int iField,
                                                 byte[] szFilePath);

    /**
     * Java wrapper for Windows MSI API MsiRecordReadStream.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field of the record.
     * @return A buffer to receive the stream field.
     */
    private static native byte[] msiRecordReadStream(int hRecord, int iField);

    /**
     * Java wrapper for Windows MSI API MsiGetSummaryInformation.
     * @param hDatabase Handle of the database.
     * @return Handle to the summaryInformation, errorcode.
     */
    private static native int[] msiGetSummaryInformation(int hDatabase);

    /**
     * Java wrapper for Windows MSI API MsiSummaryInfoSetProperty.
     * @param hSummaryInfo Handle to the summary information.
     * @param uiProperty Specify the property set
     * @param szValue Specify the text value
     * @return errorcode
     */
    private static native int msiSummaryInfoSetProperty(int hSummaryInfo,
                                                        int uiProperty,
                                                        byte[] szValue);

    /**
     * Java wrapper for Windows MSI API MsiSummaryInfoPersist.
     * @param hSummaryInfo Handle to the summary information.
     * @return errorcode.
     */
    private static native int msiSummaryInfoPersist(int hSummaryInfo);

    /**
     * Java wrapper for Windows MSI API MsiCloseAllHandles.
     * @return This function returns 0 if all handles are closed.
     *         Otherwise, the function returns the number of handles
     *         open prior to its call.
     */
    private static native int msiCloseAllHandles();

    /**
     * Java wrapper for Windows MSI API MsiDatabaseGenerateTransform.
     * @param hDatabase Handle to the database includes the changes.
     * @param hDatabaseReference Handle to the database that does not include
     *        the changes.
     * @param szTransformFile Path of the tranform file.
     * @return errorcode
     */
    private static native int msiDatabaseGenerateTransform(
                int hDatabase,
                int hDatabaseReference,
                byte[] szTransformFile);

    /**
     * Java wrapper for Windows MSI API MsiCreateTransformSummaryInfo.
     * @param hDatabase Handle to the database includes the changes.
     * @param hDatabaseReference Handle to the database that does not
     *        include the changes.
     * @param szTransformFile Path of the tranform file.
     * @return errorcode.
     */
    private static native int msiCreateTransformSummaryInfo(
            int hDatabase,
            int hDatabaseReference,
            byte[] szTransformFile);

    /**
     * Java wrapper for MsiDatabaseImport.
     * @param hDatabase Handle to the database.
     * @param folderPath    Directory where the txt table file locates.
     * @param txtTableName  Given text table file.
     * @return errorcode.
     */
    private static native int msiDatabaseImport(int hDatabase,
                                                byte[] folderPath,
                                                byte[] txtTableName);

    /**
     * Java wrapper for MsiDatabaseApplyTransform.
     * @param hDatabase Handle to the database.
     * @param transformFile The given MST file path.
     * @param iErrorConditions  Error conditions that should be suppressed.
     * @return Error code.
     */
    private static native int msiDatabaseApplyTransform(
            int hDatabase,
            byte[] transformFile,
            int iErrorConditions);

    /**
     * Generate a UUID.
     * @return A buffer containing the UUID generated
     */
    private static native byte[] genUUID();

    /**
     * Windows api wrapper to edit/add a string in an executable
     * file's string table.
     * @param appFilePath   The given application's file path.
     * @param contentStr    The string to be added.
     * @param resID         The given resource ID.
     * @return error code.
     */
    private static native int updateResourceString(
        byte[] appFilePath,
        byte[] contentStr,
        int resID);

    /**
     * Windows api wrapper to edit/add a binary data into an executable
     * file's resource.
     * @param appFilePath   The given application's file path.
     * @param dataFilePath  The given file containing the binary data.
     * @param resID         The given resource ID.
     * @return              error code.
     */
    private static native int updateResourceData(byte[] appFilePath,
                                                 byte[] dataFilePath,
                                                 int resID);

    /**
     * Returns this java string as a null-terminated byte array.
     * @param str The given string to be converted.
     * @return The bytes translated.
     */
    private static byte[] stringToByteArray(String str) {
        if (str == null) {
            return null;
        }

        byte[] srcByte = str.getBytes();
        int srcLength = srcByte.length;
        byte[] result = new byte[srcLength + 1];

        System.arraycopy(srcByte, 0, result, 0, srcLength);
        result[srcLength] = 0;

        return result;
    }

    /**
     * Converts a null-terminated byte array to java string.
     * @param array The given array to be converted.
     * @return The generated string.
     */
    private static String byteArrayToString(byte[] array) {
        if (array != null) {
            String temString = new String(array);

            if (temString != null) {
                return temString;
            }
        }
        return null;
    }

    /**
     * Opens a database file for data access.
     * @param databasePath Specifies the full path or relative path to the
     *        database file.
     * @param persistMode Specifies the full path to the file or the persistence
     *        mode.
     * @return result[0]: error code.
     *         result[1]: Pointer to the location of the returned database
     *         handle if succeed
     * @throws IOException If fail to open the database.
     */
    public static int[] winMsiOpenDatabase(String databasePath,
                                           int persistMode) throws IOException {
        int[] result = msiOpenDatabase(stringToByteArray(databasePath),
                                       persistMode);
        if (result[0] == ERROR_SUCCESS) {
            return result;
        } else {
            throw new IOException("MSI Open database failed!");
        }
    }

    /**
     * Prepares a database query and creates a view object.
     * @param hDatabase Handle to the database to which you want to open
     *        a view object.
     * @param szQuery Specifies a SQL query string for querying the database.
     * @return result[0]: error code.
     *         result[1]: Pointer to a handle for the returned view.
     * @throws IOException If fail to open the view.
     */
    public static int[] winMsiDatabaseOpenView(int hDatabase,
                                               String szQuery)
                                               throws IOException {
        int[] result = msiDatabaseOpenView(hDatabase,
                                           stringToByteArray(szQuery));
        if (result[0] == ERROR_SUCCESS) {
            return result;
        } else {
            throw new IOException("MSI Database Open View Failed!");
        }
    }

    /**
     * Executes a SQL view query and supplies any required parameters.
     * @param hView Handle to the view upon which to execute the query.
     * @param hRecord Handle to a record that supplies the parameters.
     * @throws IOException If fail to execute the sql.
     */
    public static void winMsiViewExecute(int hView, int hRecord)
                                        throws IOException {
        if (msiViewExecute(hView, hRecord) != ERROR_SUCCESS) {
            throw new IOException("MSI View Execuation Failed!");
        }
    }

    /**
     * Fetches the next sequential record from the view.
     * @param hView Handle to the view to fetch from.
     * @return result[0]: error_code.
     *         result[1]: handle for the fetched record.
     * @throws IOException If fail to fetch the records.
     */
    public static int[] winMsiViewFetch(int hView) throws IOException {
        int[] result = msiViewFetch(hView);
        if (result[0] == ERROR_SUCCESS) {
            return result;
        } else {
            throw new IOException("MSI View Fetch failed!");
        }
    }

    /**
     * Returns the string value of a record field.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field requested.
     * @return The record string
     */
    public static String winMsiRecordGetString(int hRecord, int iField) {
        byte[] recordBytes = msiRecordGetString(hRecord, iField);
        if (recordBytes != null) {
            return byteArrayToString(recordBytes);
        } else {
            return null;
        }
    }

    /**
     * Copies a string into the designated field.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field of the record to set.
     * @param valueStr Specifies the string value of the field.
     * @throws IOException If fail to set the record string.
     */
    public static void winMsiRecordSetString(int hRecord,
                                             int iField,
                                             String valueStr)
                                             throws IOException {
        if (msiRecordSetString(hRecord, iField, stringToByteArray(valueStr))
                        != ERROR_SUCCESS) {
            throw new IOException("MSI Record Set String Failed!");
        }
    }

    /**
     * Updates a fetched record.
     * @param hView Handle to a view.
     * @param eModifyMode Specifies the modify mode.
     * @param hRecord Handle to the record to modify.
     * @throws IOException If fail to modify the view.
     */
    public static void winMsiViewModify(int hView, int eModifyMode, int hRecord)
                        throws IOException {
        if (msiViewModify(hView, eModifyMode, hRecord) != ERROR_SUCCESS) {
            throw new IOException("MSI View Modification Failed!");
        }
    }

    /**
     * Returns the number of fields in a record.
     * @param hRecord Handle to a record.
     * @return If the function succeeds, the return value is the number of
     *         fields in the record. If the function is given an invalid or
     *         inactive handle, it returns -1 or 0xFFFFFFFF.
     * @throws IOException If fail to get fields count.
     */
    public static int winMsiRecordGetFieldCount(int hRecord)
                throws IOException {
        int nFields = msiRecordGetFieldCount(hRecord);
        if (nFields != -1) {
            return nFields;
        } else {
            throw new IOException("MSI Record Get Field Count Failed!");
        }
    }

    /**
     * Releases the result set for an executed view.
     * @param hView Handle to a view that is set to release.
     * @throws IOException If fail to close the view.
     */
    public static void winMsiViewClose(int hView) throws IOException {
        if (msiViewClose(hView) != ERROR_SUCCESS) {
            throw new IOException("MSI View Close Failed!");
        }
    }

    /**
     * Commits changes to a database.
     * @param hDatabase Handle to the database.
     * @throws IOException If fail to commit the changes.
     */
    public static void winMsiDatabaseCommit(int hDatabase) throws IOException {
        if (msiDatabaseCommit(hDatabase) != ERROR_SUCCESS) {
            throw new IOException("MSI Database Commit Failed!");
        }
    }

    /**
     * Apply the transform to the MSI database.
     * @param hDatabase Handle of the MSI database.
     * @param transformFile The MST file containing the transform information.
     * @throws IOException If fail to apply the transform.
     */
    public static void winMsiDatabaseApplyTransform(int hDatabase,
                                                    String transformFile)
                                                    throws IOException {
        if (ERROR_SUCCESS
            != msiDatabaseApplyTransform(hDatabase,
               stringToByteArray(transformFile), 0)) {
            throw new IOException("MSI Database apply tranform failed!");
        }
    }

    /**
     * Closes an open installation handle.
     * @param hAny Specifies any open installation handle.
     * @throws IOException If fail to close the handle.
     */
    public static void winMsiCloseHandle(int hAny) throws IOException {
        if (msiCloseHandle(hAny) != ERROR_SUCCESS) {
            throw new IOException("MSI Close Handle Failed!");
        }
    }

    /**
     * Sets a record stream field from a file.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field of the record to set.
     * @param filePathStr Specifies the path to the file containing the stream.
     * @throws IOException If fail to set the stream into the record.
     */
    public static void winMsiRecordSetStream(int hRecord,
                                             int iField,
                                             String filePathStr)
                                             throws IOException {
        if (msiRecordSetStream(hRecord, iField, stringToByteArray(filePathStr))
                        != ERROR_SUCCESS) {
            throw new IOException("MSI Record Set Stream Failed!");
        }
    }

    /**
     * Reads bytes from a record stream field into a buffer.
     * @param hRecord Handle to the record.
     * @param iField Specifies the field of the record.
     * @return The read stream bytes.
     */
    public static byte[] winMsiRecordReadStream(int hRecord, int iField) {
        return msiRecordReadStream(hRecord, iField);
    }

    /**
     * Opens Summary Information stream.
     * @param hDatabase Handle of the msi database.
     * @return Handle to the opened summary information, errorcode.
     */
    public static int[] winMsiGetSummaryInformation(int hDatabase) {
        return msiGetSummaryInformation(hDatabase);
    }

    /**
     * Sets the property field of the msi summary information stream.
     * @param hSummaryInfo Handle of the msi summary information stream.
     * @param uiProperty Specify the property field.
     * @param strValue New value to be set.
     * @throws IOException If fail to set the property.
     */
    public static void winMsiSummaryInfoSetProperty(int hSummaryInfo,
                                                    int uiProperty,
                                                    String strValue)
                                                    throws IOException {
        if (msiSummaryInfoSetProperty(hSummaryInfo,
                                      uiProperty,
                                      stringToByteArray(strValue))
                                      != ERROR_SUCCESS) {
            throw new IOException("MSI SummaryInfo Set Property Failed!");
        }
    }

    /**
     * Flush the msi summary information stream.
     * @param hSummaryInfo Handle to the msi summary information stream.
     * @throws IOException If fail to do the flush.
     */
    public static void winMsiSummaryInfoPersist(int hSummaryInfo)
                throws IOException {
        if (msiSummaryInfoPersist(hSummaryInfo) != ERROR_SUCCESS) {
            throw new IOException("MSI Summaryinfo Persist Failed!");
        }
    }

    /**
     * Closes all open installation handles allocated by the current thread.
     * @return 0 if all handles are closed. Otherwise, the function returns
     *         the number of handles open prior to its call.
     */
    public static int winMsiCloseAllHandles() {
        return msiCloseAllHandles();
    }

    /**
     * Generates a UUID string.
     * @return The generated UUID string if succeed or null if failed.
     */
    public static String generateUUID() {
        byte[] newUUID = genUUID();
        if (newUUID != null) {
            return byteArrayToString(newUUID);
        } else {
            return null;
        }
    }

    /**
     * Generate the transform MST file.
     * @param hDatabase Handle of the database.
     * @param hDatabaseReference Handle of the referenced database.
     * @param transformFile Path of the transform file.
     * @throws IOException If failed to generate the transform.
     */
    public static void winMsiDatabaseGenerateTransform(int hDatabase,
                                                       int hDatabaseReference,
                                                       String transformFile)
                                                       throws IOException {
        int result = msiDatabaseGenerateTransform(
                        hDatabase,
                        hDatabaseReference,
                        stringToByteArray(transformFile));
        if (result != ERROR_SUCCESS) {
            throw new IOException("MSI Database Generate Transform Failed!");
        }
    }

    /**
     * Creates summary information of an existing transform to include
     * validation and error conditions.
     * @param hDatabase Handle of the database.
     * @param hDatabaseReference handle of the referenced database.
     * @param transformFile Path of the transform file.
     * @throws IOException If failed to generate the summary info transform.
     */
    public static void winMsiCreateTransformSummaryInfo(
                            int hDatabase,
                            int hDatabaseReference,
                            String transformFile)
                            throws IOException {
        int result = msiCreateTransformSummaryInfo(
                        hDatabase,
                        hDatabaseReference,
                        stringToByteArray(transformFile));
        if (result != ERROR_SUCCESS) {
            throw new IOException("MSI Database Generate Transform Failed!");
        }
    }

    /**
     * Windows API to import a database table from a txt file.
     * @param hDatabase   The given database handle.
     * @param folderPath  The directory path where the txt table file locates.
     * @param txtFileName The txt representation for the corresponding table.
     * @throws IOException If failed to import the database.
     */
    public static void winMsiDatabaseImport(
                            int hDatabase,
                            String folderPath,
                            String txtFileName)
                            throws IOException {
        int result = msiDatabaseImport(hDatabase,
                                       stringToByteArray(folderPath),
                                       stringToByteArray(txtFileName));
        if (ERROR_SUCCESS != result) {
            throw new IOException("MSI Databse import failed!");
        }
    }

    /**
     * Windows API wrapper to add/update an executable file's resource
     * string table.
     * @param appFilePath   The given application's file path.
     * @param contentStr    The string to be added.
     * @param resID         The given resource ID.
     * @throws IOException If fail to update the resource string.
     */
    public static void winUpdateResourceString(
                        String appFilePath,
                        String contentStr,
                        int resID)
                        throws IOException {
        int result = updateResourceString(stringToByteArray(appFilePath),
                                          stringToByteArray(contentStr),
                                          resID);
        if (result != ERROR_SUCCESS) {
            throw new IOException("Windows Update Resource String Failed!");
        }
    }

    /**
     * Windows API to add/update an executble file's binary resource.
     * @param appFilePath   The given application's file path.
     * @param dataFilePath  The given file containing the binary data
     * @param resID         The given resource ID
     * @throws IOException  If failed to update the binary resource field.
     */
    public static void winUpdateResourceData(
                        String appFilePath,
                        String dataFilePath,
                        int resID)
                        throws IOException {
        int result = updateResourceData(stringToByteArray(appFilePath),
                                        stringToByteArray(dataFilePath),
                                        resID);
        if (result != ERROR_SUCCESS) {
            throw new IOException(
                            "Windows Update Resource Binary Data Failed!");
        }
    }

    /**
     * Windows API to create a MSI database record.
     * @param numFields Number of the fields to be included in this record.
     * @return Handle to the created record.
     * @throws IOException If failed to create the record.
     */
    public static int winMsiCreateRecord(int numFields) throws IOException {
        int msiRecordHandle = msiCreateRecord(numFields);
        if (msiRecordHandle != -1) {
            return msiRecordHandle;
        } else {
            throw new IOException("MSI Create Record Failed!");
        }
    }
}
