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
 
WIN32_FIND_DATA FindFileData;
HANDLE hFind = INVALID_HANDLE_VALUE;
DWORD dwError;

JNIEXPORT jlongArray JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_getFreeSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {
    PWCHAR wcpFullPath = (PWCHAR) (*env)->GetStringChars(env, fullPath, NULL);
    ULARGE_INTEGER freeBytesAvailable;
    ULARGE_INTEGER totalNumberOfBytes;
    ULARGE_INTEGER totalNumberOfFreeBytes;
    jlongArray retorno = (*env)->NewLongArray(env, 2);
    jlong buf[2];
    PWCHAR pTerminator = L"\0";

    wcpFullPath = (PWCHAR) realloc(wcpFullPath, sizeof(WCHAR) * (wcslen(wcpFullPath) + 1));
    wcsncat(wcpFullPath, pTerminator, 1);
    GetDiskFreeSpaceExW(wcpFullPath, &freeBytesAvailable, &totalNumberOfBytes, 
                             &totalNumberOfFreeBytes);
    buf[0] = (jlong) freeBytesAvailable.LowPart;
    buf[1] = (jlong) freeBytesAvailable.HighPart;
    (*env)->SetLongArrayRegion(env, retorno, 0, 2, buf);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    
    return retorno;
}

JNIEXPORT jlongArray JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_getTotalSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {
    PWCHAR wcpFullPath = (PWCHAR) (*env)->GetStringChars(env, fullPath, NULL);
    ULARGE_INTEGER freeBytesAvailable;
    ULARGE_INTEGER totalNumberOfBytes;
    ULARGE_INTEGER totalNumberOfFreeBytes;
    jlongArray retorno = (*env)->NewLongArray(env, 2);
    jlong buf[2];
    PWCHAR pTerminator = L"\0";

    wcpFullPath = (PWCHAR) realloc(wcpFullPath, sizeof(WCHAR) * (wcslen(wcpFullPath) + 1));
    wcsncat(wcpFullPath, pTerminator, 1);
    GetDiskFreeSpaceExW(wcpFullPath, &freeBytesAvailable, &totalNumberOfBytes, 
                             &totalNumberOfFreeBytes);
    buf[0] = (jlong) totalNumberOfBytes.LowPart;
    buf[1] = (jlong) totalNumberOfBytes.HighPart;
    (*env)->SetLongArrayRegion(env, retorno, 0, 2, buf);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    
    return retorno;
}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_recycle
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean confirm) {
        int retorno;
    PWCHAR wcpTemp = (PWCHAR) (*env)->GetStringChars(env, fullPath, NULL);
    PWCHAR wcpFullPath;
    SHFILEOPSTRUCTW FileOp;    
    FileOp.hwnd = NULL;
    FileOp.wFunc = FO_DELETE;
    
    /*
    pFrom is may specify one or more source file names. Each file name must 
    be terminated by a single NULL character and an additional NULL character 
    must be appended to the end of the final name to indicate the end of pFrom.
    */
    wcpFullPath = (PWCHAR) malloc(sizeof(WCHAR) * (wcslen(wcpTemp) + 2));
    wcpFullPath = wcsncpy(wcpFullPath, wcpTemp, wcslen(wcpTemp) + 2);
    FileOp.pFrom = wcpFullPath;
    FileOp.pTo = NULL;
    FileOp.fFlags = (confirm == JNI_TRUE) ? FOF_ALLOWUNDO : FOF_ALLOWUNDO | FOF_NOCONFIRMATION;
    FileOp.hNameMappings = NULL;
    FileOp.lpszProgressTitle = NULL;
    retorno = SHFileOperationW(&FileOp);
    free(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpTemp);
    
    return retorno;
}

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_getFileSystem
  (JNIEnv *env, jobject obj, jstring rootPath) {
    LPCWSTR wcpRootPath = (LPCWSTR) (*env)->GetStringChars(env, rootPath, NULL);
    DWORD dwMaximumComponentLength;
    DWORD dwFileSystemFlags;
    DWORD dwLength;
    WCHAR lpFileSystemNameBuffer[MAX_PATH + 1];
    jstring retorno;
    
    GetVolumeInformationW(wcpRootPath, NULL, 0, NULL, &dwMaximumComponentLength, &dwFileSystemFlags, lpFileSystemNameBuffer, MAX_PATH + 1);
    
    dwLength = wcslen(lpFileSystemNameBuffer);
    
    retorno = (*env)->NewString(env, lpFileSystemNameBuffer, dwLength);
    (*env)->ReleaseStringChars(env, rootPath, wcpRootPath);
    
    return retorno;      
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isArchive
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_ARCHIVE) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setArchive
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(wcpFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_ARCHIVE;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_ARCHIVE) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_ARCHIVE);
    }
    
    if(SetFileAttributesW(wcpFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    } 
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isNormal
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_NORMAL) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setNormal
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    
    if(SetFileAttributesW(wcpFullPath, FILE_ATTRIBUTE_NORMAL)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isReadOnly
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_READONLY) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isSystem
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_SYSTEM) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setSystem
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(wcpFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_SYSTEM;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_SYSTEM) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_SYSTEM);
    } 
            
    if(SetFileAttributesW(wcpFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isTemporary
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_TEMPORARY) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setTemporary
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(wcpFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_TEMPORARY;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_TEMPORARY) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_TEMPORARY);
    }    
    
    if(SetFileAttributesW(wcpFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isCompressed
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_COMPRESSED) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_isEncrypted
  (JNIEnv *env, jobject obj, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes;
    
    dwFileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(dwFileAttributes & FILE_ATTRIBUTE_ENCRYPTED) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_setHidden
  (JNIEnv *env, jobject obj, jstring fullPath, jboolean status) {
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD dwFileAttributes = GetFileAttributesW(wcpFullPath);
    
    if(status) {
        dwFileAttributes = dwFileAttributes | FILE_ATTRIBUTE_HIDDEN;
    }
    else if(dwFileAttributes & FILE_ATTRIBUTE_HIDDEN) {
        dwFileAttributes = dwFileAttributes & (~FILE_ATTRIBUTE_HIDDEN);
    }

    if(SetFileAttributesW(wcpFullPath, dwFileAttributes)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    }  
}

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_findFirst
  (JNIEnv *env, jobject obj, jstring fullPath) {
    char *wcpFullPath = (char*) (*env)->GetStringUTFChars(env, fullPath, NULL);
    wcpFullPath = (char*) realloc(wcpFullPath, sizeof(WCHAR) * (strlen(wcpFullPath) + 3));
    strncat(wcpFullPath, "\\*\0", 3);    
    
    hFind = FindFirstFile(wcpFullPath, &FindFileData);
    if(hFind == INVALID_HANDLE_VALUE) {
        return NULL;
    }
    else {
        return (*env)->NewStringUTF(env, FindFileData.cFileName);
    }
}
  
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_findNext
  (JNIEnv *env, jobject obj) {
    if(FindNextFile(hFind, &FindFileData) == 0) {
        return NULL;
    }
    else {
        return (*env)->NewStringUTF(env, FindFileData.cFileName);
    }
}
  
JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_Win32NativeFileUtil_findClose
  (JNIEnv *env, jobject obj) {
    if(FindClose(hFind) != 0) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }
}
