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
 
#include <afx.h>
#include <rpc.h>
#include <Rpcdce.h>
#include <Msiquery.h>
#include <ShlObj.h>

#include "org_jdesktop_jdic_packager_impl_WinMsiWrapper.h"
#ifdef __cplusplus
extern "C" {
#endif

const int MAX_RECORD_STRING_LENGTH = 1024;

JNIEXPORT jintArray JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiOpenDatabase
  (JNIEnv * env, jclass jc, jbyteArray DatabasePath, jint Persist)
{
	jint tmpResult[2];
	int errorCode = -1;
	jintArray result;
	MSIHANDLE DatabaseHandle;
	//construct parameters
	LPCTSTR strDatabasePath = (LPCTSTR) env->GetByteArrayElements(DatabasePath, NULL);
	//Windows API invocation
	errorCode = MsiOpenDatabase(strDatabasePath, (LPCTSTR)Persist, &DatabaseHandle);
	//Release string
	env->ReleaseByteArrayElements(DatabasePath, (signed char*)strDatabasePath, 0);
	//constructs return value
	tmpResult[0] = errorCode;
	tmpResult[1] = (int) DatabaseHandle;
	result = env->NewIntArray(2);
	if (result != NULL) 
	{
		env->SetIntArrayRegion(result, 0, 2, tmpResult);
	}
	return result;
}

JNIEXPORT jintArray JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiDatabaseOpenView
  (JNIEnv * env, jclass jc, jint hDatabase, jbyteArray szQuery)
{
	jint tmpResult[2];
	int errorCode = -1;
	jintArray result;
	MSIHANDLE viewHandle;
	//construct paramter
	LPCTSTR strQuery = (LPCTSTR) env->GetByteArrayElements(szQuery, NULL);
	//Windows API invocation
	errorCode = MsiDatabaseOpenView(hDatabase, strQuery, &viewHandle);
	//Release String
	env->ReleaseByteArrayElements(szQuery, (signed char*)strQuery, 0);
	//Constructs return value
	tmpResult[0] = errorCode;
	tmpResult[1] = (int) viewHandle;
	result = env->NewIntArray(2);
	if (result != NULL) 
	{
		env->SetIntArrayRegion(result, 0, 2, tmpResult);
	}
	return result;
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiViewExecute
  (JNIEnv * env, jclass jc, jint hView, jint hRecord)
{
	return MsiViewExecute((MSIHANDLE)hView, (MSIHANDLE)hRecord);
}

JNIEXPORT jintArray JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiViewFetch
  (JNIEnv * env, jclass jc, jint hView)
{
	jint tmpResult[2];
	int errorCode = -1;
	jintArray result;
	MSIHANDLE hRecord;
	//Windows API invocation
	errorCode = MsiViewFetch((MSIHANDLE)hView, &hRecord);
	//Constructs the return value
	tmpResult[0] = errorCode;
	tmpResult[1] = (int) hRecord;
	result = env->NewIntArray(2);
	if (result != NULL)
	{
		env->SetIntArrayRegion(result, 0, 2, tmpResult);
	}
	return result;
}

JNIEXPORT jbyteArray JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiRecordGetString
  (JNIEnv * env, jclass jc, jint hRecord, jint iField)
{
	unsigned char szValueBuf[MAX_RECORD_STRING_LENGTH];
	jbyteArray result = NULL;
	DWORD recordSize = MAX_RECORD_STRING_LENGTH;
	//Windows API invocation
	if (MsiRecordGetString((MSIHANDLE)hRecord, iField, (LPTSTR) szValueBuf, &recordSize) == ERROR_SUCCESS)
	{
		if (recordSize > 0)
		{
			result = env->NewByteArray(recordSize);
			if (result != NULL) 
			{
				env->SetByteArrayRegion(result, 0, recordSize, (jbyte*) szValueBuf);
			}
		}
	}
	return result;
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiRecordSetString
  (JNIEnv * env, jclass jc, jint hRecord, jint iField, jbyteArray szValue)
{
	int errorCode = -1;
	//Constructs the paramter
	LPCTSTR strValue = (LPCTSTR) env->GetByteArrayElements(szValue, NULL);
	//Windows API invocation
	errorCode = MsiRecordSetString((MSIHANDLE) hRecord, iField, strValue);
	//Release the string
	env->ReleaseByteArrayElements(szValue, (signed char*)strValue, 0);
	return errorCode;
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiViewModify
  (JNIEnv * env, jclass jc, jint hView, jint eModifyMode, jint hRecord)
{
	return MsiViewModify((MSIHANDLE) hView, (MSIMODIFY) eModifyMode, (MSIHANDLE) hRecord);

}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiRecordGetFieldCount
  (JNIEnv * env, jclass jc, jint hRecord)
{
	return MsiRecordGetFieldCount((MSIHANDLE) hRecord);
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiViewClose
  (JNIEnv * env, jclass jc, jint hView)
{
	return MsiViewClose((MSIHANDLE) hView);
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiDatabaseCommit
  (JNIEnv * env, jclass jc, jint hDatabase)
{
	return MsiDatabaseCommit((MSIHANDLE) hDatabase);
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiCloseHandle
  (JNIEnv * env, jclass jc, jint hAny)
{
	return MsiCloseHandle((MSIHANDLE) hAny);
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiRecordSetStream
  (JNIEnv * env, jclass jc, jint hRecord, jint iField, jbyteArray szFilePath)
{
	int errorCode = -1;
	LPTSTR strFilePath;
	strFilePath = (LPTSTR) env->GetByteArrayElements(szFilePath, NULL);
	//Windows API invocation
	errorCode = MsiRecordSetStream((MSIHANDLE) hRecord, iField, strFilePath);
	//Release string buffer
	env->ReleaseByteArrayElements(szFilePath, (signed char*)strFilePath, 0);
	return errorCode;
}


JNIEXPORT jbyteArray JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiRecordReadStream
  (JNIEnv * env, jclass jc, jint hRecord, jint iField)
{
	int errorCode = -1;
	char szDataBuf[MAX_RECORD_STRING_LENGTH];
	DWORD bufCount;
	jbyteArray result = NULL;
	errorCode = MsiRecordReadStream((MSIHANDLE)hRecord, iField, szDataBuf, &bufCount);
	if (bufCount > 0) 
	{
		result = env->NewByteArray(bufCount);
		if (result != NULL) 
		{
			env->SetByteArrayRegion(result, 0, bufCount, (jbyte *) szDataBuf);
		}
	}
	return result;
}

JNIEXPORT jintArray JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiGetSummaryInformation
  (JNIEnv * env, jclass jc, jint hDatabase)
{
    jint tmpResult[2];
    int errorCode = -1;
    jintArray result = NULL;
    MSIHANDLE hSummaryInfo;
    errorCode = MsiGetSummaryInformation((MSIHANDLE)hDatabase, NULL, 10, &hSummaryInfo);
	tmpResult[0] = errorCode;
	tmpResult[1] = (int) hSummaryInfo;
	result = env->NewIntArray(2);
	if (result != NULL) 
	{
		env->SetIntArrayRegion(result, 0, 2, tmpResult);
	}
	return result; 
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiSummaryInfoSetProperty
  (JNIEnv * env, jclass jc, jint hSummaryInfo, jint uiProperty, jbyteArray szValue)
{
	int errorCode = -1;
	//Constructs the paramter
	LPCTSTR strValue = (LPCTSTR) env->GetByteArrayElements(szValue, NULL);
	//Windows API invocation
	errorCode = MsiSummaryInfoSetProperty((MSIHANDLE) hSummaryInfo, (UINT)uiProperty, (UINT)VT_LPSTR, 0, NULL, strValue);
	//Release the string
	env->ReleaseByteArrayElements(szValue, (signed char*)strValue, 0);
	return errorCode;
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiSummaryInfoPersist
  (JNIEnv * env, jclass jc, jint hSummaryInfo)
{
    int errorCode = -1;
    errorCode = MsiSummaryInfoPersist((MSIHANDLE) hSummaryInfo);
    return errorCode;   
}

/*
 * Class:     WinMsiWrapper
 * Method:    MsiCloseAllHandles
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiCloseAllHandles
  (JNIEnv * env, jclass jc)
{
    return MsiCloseAllHandles();
}    

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    MsiDatabaseGenerateTransform
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiDatabaseGenerateTransform
(JNIEnv * env, jclass jc, jint hDatabase, jint hDatabaseReference, jbyteArray transformFile)
{
	int errorCode = -1;
	//Constructs the paramter
	LPCTSTR szTransformFile = (LPCTSTR) env->GetByteArrayElements(transformFile, NULL);
	//Windows API invocation
	errorCode = MsiDatabaseGenerateTransform((MSIHANDLE) hDatabase, (MSIHANDLE) hDatabaseReference, (LPCTSTR)szTransformFile, 0, 0);
	//Release the string
	env->ReleaseByteArrayElements(transformFile, (signed char*)szTransformFile, 0);
	return errorCode;
}

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    MsiCreateTransformSummaryInfo
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiCreateTransformSummaryInfo
(JNIEnv * env, jclass jc, jint hDatabase, jint hDatabaseReference, jbyteArray transformFile)
{
	int errorCode = -1;
	//Constructs the paramter
	LPCTSTR szTransformFile = (LPCTSTR) env->GetByteArrayElements(transformFile, NULL);
	//Windows API invocation
	errorCode = MsiCreateTransformSummaryInfo((MSIHANDLE) hDatabase, (MSIHANDLE) hDatabaseReference, (LPCTSTR)szTransformFile, 0, 0);
	//Release the string
	env->ReleaseByteArrayElements(transformFile, (signed char*)szTransformFile, 0);
	return errorCode;
}

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    UpdateResourceString
 * Signature: ([B[BI)I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_updateResourceString
(JNIEnv * env, jclass jc, jbyteArray appName, jbyteArray contentStr, jint resID)
{
    //Construct the parameter
    LPCTSTR szAppName = (LPCTSTR)env->GetByteArrayElements(appName, NULL);
    LPCTSTR szContentStr = (LPCTSTR)env->GetByteArrayElements(contentStr, NULL);
    HANDLE hUpdateApp;
    //Get the handle to the application
    hUpdateApp = BeginUpdateResource(szAppName, false);
    if (hUpdateApp != NULL)
    {
        //Get the resource length
        int nStrLen = strlen(szContentStr);
        DWORD dwResSize = (nStrLen + 2) * sizeof(WCHAR);
        //prepare the string resources block
        LPVOID pRes = malloc(dwResSize);
        if (pRes != NULL)
        {
            WCHAR* pResChar = (WCHAR*)pRes;
            *pResChar++ = nStrLen;
            for (int i = 0; i < nStrLen; i++)
            {
                *pResChar++ = szContentStr[i];
            }
            UINT nBlockID = (resID / 16) + 1;
            if (UpdateResource(hUpdateApp,
                                RT_STRING,
                                MAKEINTRESOURCE(nBlockID),
                                MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                                (LPVOID)pRes,
                                dwResSize))
            {
                if (EndUpdateResource(hUpdateApp, false))
                {
                    //Release the string
	                env->ReleaseByteArrayElements(appName, (signed char*)szAppName, 0);
	                env->ReleaseByteArrayElements(contentStr, (signed char*)szContentStr, 0);
	                free(pRes);
                    return 0;
                }
            }
            free(pRes);
        }
    }
    //Release the string
	env->ReleaseByteArrayElements(appName, (signed char*)szAppName, 0);
	env->ReleaseByteArrayElements(contentStr, (signed char*)szContentStr, 0);    
    return -1;
}

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    UpdateResourceData
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_updateResourceData
(JNIEnv * env, jclass jc, jbyteArray appName, jbyteArray dataFileName, jint resID)
{
    //Construct the parameter
    LPCTSTR szAppName = (LPCTSTR)env->GetByteArrayElements(appName, NULL);
    LPCTSTR szDataFileName = (LPCTSTR)env->GetByteArrayElements(dataFileName, NULL);
    
    HANDLE hFile;
    DWORD dwFileSize, dwBytesRead;
    LPBYTE lpBuffer;
    //Open the data file
    hFile = CreateFile(szDataFileName,
                       GENERIC_READ,
                       0,
                       NULL,
                       OPEN_EXISTING,
                       FILE_ATTRIBUTE_NORMAL,
                       NULL);
    if (INVALID_HANDLE_VALUE != hFile)
    {
        dwFileSize = GetFileSize(hFile, NULL);
        lpBuffer = new BYTE[dwFileSize];
        if (ReadFile(hFile, lpBuffer, dwFileSize, &dwBytesRead, NULL) != FALSE)
        {
            HANDLE hResource;
            hResource = BeginUpdateResource(szAppName, false);
            if (hResource != NULL)
            {
                if (UpdateResource(hResource,
                                   RT_RCDATA,
                                   MAKEINTRESOURCE(104),
                                   MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT),
                                   (LPVOID) lpBuffer,
                                   dwFileSize) != FALSE) 
                {
                    EndUpdateResource(hResource, FALSE);
                }
            }
        }
        delete[] lpBuffer;
        CloseHandle(hFile);
        //Release the string
	    env->ReleaseByteArrayElements(appName, (signed char*)szAppName, 0);
	    env->ReleaseByteArrayElements(dataFileName, (signed char*)szDataFileName, 0);
        return 0;
    }
    //Release the string
	env->ReleaseByteArrayElements(appName, (signed char*)szAppName, 0);
	env->ReleaseByteArrayElements(dataFileName, (signed char*)szDataFileName, 0);
    return -1;
}

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    genUUID
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_genUUID
  (JNIEnv * env, jclass jc)
{
	jbyteArray result = NULL;
	unsigned char UUIDBuffer[MAX_RECORD_STRING_LENGTH];
	char UUIDString[MAX_RECORD_STRING_LENGTH];
    UUID newUUID;
	UuidCreate(&newUUID);
	unsigned char * pStrUUID = UUIDBuffer;
	if (UuidToString(&newUUID, (unsigned char **)&pStrUUID) != RPC_S_UUID_NO_ADDRESS)
	{
	    sprintf(UUIDString, "%s", pStrUUID);
	    int bufCount = strlen(UUIDString);
        result = env->NewByteArray(bufCount);
  		if (result != NULL) 
		{
			env->SetByteArrayRegion(result, 0, bufCount, (jbyte *) UUIDString);
		}
        return result;
	}
	return NULL;
}

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    MsiCreateRecord
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiCreateRecord
  (JNIEnv * env, jclass jc, jint numRecords)
{
    int msiRecordHandle;
    msiRecordHandle = MsiCreateRecord(numRecords);
    if ( msiRecordHandle != NULL) 
    {
        return msiRecordHandle;      
    }
    else 
    {
        return -1;
    }
}

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    MsiDatabaseImport
 * Signature: (I[B[B)I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiDatabaseImport
  (JNIEnv * env, jclass jc, jint hDatabase, jbyteArray folderPath, jbyteArray txtFileName)
{
    //Construct the parameter
    LPCTSTR szFolderPath = (LPCTSTR)env->GetByteArrayElements(folderPath, NULL);
    LPCTSTR szFileName = (LPCTSTR)env->GetByteArrayElements(txtFileName, NULL);
    int errorCode = MsiDatabaseImport(hDatabase, szFolderPath, szFileName);
    //Release the string
	env->ReleaseByteArrayElements(folderPath, (signed char*)szFolderPath, 0);
	env->ReleaseByteArrayElements(txtFileName, (signed char*)szFileName, 0);
	return errorCode;
}

/*
 * Class:     org_jdesktop_jdic_packager_impl_WinMsiWrapper
 * Method:    MsiDatabaseApplyTransform
 * Signature: (I[BI)I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_packager_impl_WinMsiWrapper_msiDatabaseApplyTransform
  (JNIEnv * env, jclass jc, jint hDatabase, jbyteArray tranformFile, jint iErrorConditions)
{
    //Construct the parameter
    LPCTSTR szTransformFile = (LPCTSTR)env->GetByteArrayElements(tranformFile, NULL);
    int errorCode = MsiDatabaseApplyTransform(hDatabase, szTransformFile, iErrorConditions);
    //Release the string
	env->ReleaseByteArrayElements(tranformFile, (signed char*)szTransformFile, 0);
    return errorCode;
}

#ifdef __cplusplus
}
#endif