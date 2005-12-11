/*
 * Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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

#include <windows.h>
#include "Win32NativeFileUtil.h"

/**
 * author: Fábio Castilho Martins
 */
 
JNIEXPORT jlongArray JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_getFreeSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    ULARGE_INTEGER freeBytesAvailable;
    ULARGE_INTEGER totalNumberOfBytes;
    ULARGE_INTEGER totalNumberOfFreeBytes;
    jlongArray retorno = (*env)->NewLongArray(env, 2);
    jlong buf[2];
    wchar_t* pTerminator = L"\0";

    pFullPath = (wchar_t*) realloc(pFullPath, sizeof(wchar_t) * (wcslen(pFullPath) + 1));
    wcsncat(pFullPath, pTerminator, 1);
    GetDiskFreeSpaceExW(pFullPath, &freeBytesAvailable, &totalNumberOfBytes, 
                             &totalNumberOfFreeBytes);
    buf[0] = (jlong) freeBytesAvailable.LowPart;
    buf[1] = (jlong) freeBytesAvailable.HighPart;
    (*env)->SetLongArrayRegion(env, retorno, 0, 2, buf);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    
    return retorno;
}

JNIEXPORT jlongArray JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_getTotalSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    ULARGE_INTEGER freeBytesAvailable;
    ULARGE_INTEGER totalNumberOfBytes;
    ULARGE_INTEGER totalNumberOfFreeBytes;
    jlongArray retorno = (*env)->NewLongArray(env, 2);
    jlong buf[2];
    wchar_t* pTerminator = L"\0";

    pFullPath = (wchar_t*) realloc(pFullPath, sizeof(wchar_t) * (wcslen(pFullPath) + 1));
    wcsncat(pFullPath, pTerminator, 1);
    GetDiskFreeSpaceExW(pFullPath, &freeBytesAvailable, &totalNumberOfBytes, 
                             &totalNumberOfFreeBytes);
    buf[0] = (jlong) totalNumberOfBytes.LowPart;
    buf[1] = (jlong) totalNumberOfBytes.HighPart;
    (*env)->SetLongArrayRegion(env, retorno, 0, 2, buf);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    
    return retorno;
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_recycle
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean confirm) {
        int retorno;
    wchar_t* wcpTemp = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    wchar_t* pFullPath;
    SHFILEOPSTRUCTW FileOp;    
    FileOp.hwnd = NULL;
    FileOp.wFunc = FO_DELETE;
    
    /*
    pFrom is may specify one or more source file names. Each file name must 
    be terminated by a single NULL character and an additional NULL character 
    must be appended to the end of the final name to indicate the end of pFrom.
    */
    pFullPath = (wchar_t*) malloc(sizeof(wchar_t) * (wcslen(wcpTemp) + 2));
    pFullPath = wcsncpy(pFullPath, wcpTemp, wcslen(wcpTemp) + 2);
    FileOp.pFrom = pFullPath;
    FileOp.pTo = NULL;
    FileOp.fFlags = (confirm == JNI_TRUE) ? FOF_ALLOWUNDO : FOF_ALLOWUNDO | FOF_NOCONFIRMATION;
    FileOp.hNameMappings = NULL;
    FileOp.lpszProgressTitle = NULL;
    retorno = SHFileOperationW(&FileOp);
    free(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpTemp);
    
    return retorno;
}

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_getFileSystem
  (JNIEnv *env, jobject obj, jstring rootPath) {
    wchar_t* pRootPath = (wchar_t*) (*env)->GetStringChars(env, rootPath, NULL);
    DWORD dwMaximumComponentLength;
    DWORD dwFileSystemFlags;
    DWORD dwLength;
    wchar_t pFileSystemNameBuffer[MAX_PATH + 1];
    jstring retorno;
    
    GetVolumeInformationW(pRootPath, NULL, 0, NULL, &dwMaximumComponentLength, &dwFileSystemFlags, pFileSystemNameBuffer, MAX_PATH + 1);
    
    retorno = (*env)->NewString(env, pFileSystemNameBuffer, wcslen(pFileSystemNameBuffer));
    (*env)->ReleaseStringChars(env, rootPath, pRootPath);
    
    return retorno;      
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isArchive
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_ARCHIVE) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setArchive
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(pFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_ARCHIVE;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_ARCHIVE) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_ARCHIVE);
    }
    
    if(SetFileAttributesW(pFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_FALSE;
    } 
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isNormal
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_NORMAL) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setNormal
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    
    if(SetFileAttributesW(pFullPath, FILE_ATTRIBUTE_NORMAL)) {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isReadOnly
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_READONLY) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isSystem
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_SYSTEM) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setSystem
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(pFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_SYSTEM;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_SYSTEM) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_SYSTEM);
    } 
            
    if(SetFileAttributesW(pFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isTemporary
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_TEMPORARY) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setTemporary
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(pFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_TEMPORARY;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_TEMPORARY) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_TEMPORARY);
    }    
    
    if(SetFileAttributesW(pFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isCompressed
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_COMPRESSED) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isEncrypted
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(pFullPath);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_ENCRYPTED) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setHidden
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) {
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(pFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_HIDDEN;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_HIDDEN) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_HIDDEN);
    }

    if(SetFileAttributesW(pFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, pFullPath);
        return JNI_FALSE;
    }  
}

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_findFirst
  (JNIEnv *env, jobject obj, jstring fullPath) {
    wchar_t* pFullPath = (wchar_t*) (*env)->GetStringChars(env, fullPath, NULL);
    pFullPath = (wchar_t*) realloc(pFullPath, sizeof(wchar_t) * (wcslen(pFullPath) + 3));
    wcsncat(pFullPath, L"\\*\0", 3);

    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid= (*env)->GetFieldID(env, cls, "handle", "I");
    
    if (fid == NULL) {
        return; /* failed to find the field */
    }
    jint handle = (*env)->GetIntField(env, obj, fid);
    
    WIN32_FIND_DATAW findFileData;
    
    HANDLE hFind = FindFirstFileW(pFullPath, &findFileData);
    (*env)->ReleaseStringChars(env, fullPath, pFullPath);
    if(hFind == INVALID_HANDLE_VALUE) {
        return NULL;
    }
    else {
        handle = (jint) hFind;
        (*env)->SetIntField(env, obj, fid, handle);
        return (*env)->NewString(env, findFileData.cFileName, wcslen(findFileData.cFileName));
    }
}
  
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_findNext
  (JNIEnv *env, jobject obj) {      
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid= (*env)->GetFieldID(env, cls, "handle", "I");
    
    if (fid == NULL) {
        return; /* failed to find the field */
    }
    jint handle = (*env)->GetIntField(env, obj, fid);
    
    WIN32_FIND_DATAW findFileData;
    HANDLE hFind = (HANDLE) handle;
      
    if(FindNextFileW(hFind, &findFileData) == 0) {
        return NULL;
    }
    else {
        handle = (jint) hFind;
        (*env)->SetIntField(env, obj, fid, handle);
        return (*env)->NewString(env, findFileData.cFileName, wcslen(findFileData.cFileName));
    }
}
  
JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_findClose
  (JNIEnv *env, jobject obj) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid = fid = (*env)->GetFieldID(env, cls, "handle", "I");
    
    if (fid == NULL) {
        return; /* failed to find the field */
    }
    
    jint handle = (*env)->GetIntField(env, obj, fid);
    
    HANDLE hFind = (HANDLE) handle;
        
    if(FindClose(hFind) != 0) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }
}
