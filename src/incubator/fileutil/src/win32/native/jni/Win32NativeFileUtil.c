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

#include <windows.h>
#include "Win32NativeFileUtil.h"

/**
 * author: Fábio Castilho Martins
 */

JNIEXPORT jlongArray JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_getFreeSpace
  (JNIEnv *env, jclass jc, jstring fullPath) {
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

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_recycle
  (JNIEnv *env, jclass jc, jstring fullPath, jboolean confirm) {
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

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_getFileSystem
  (JNIEnv *env, jclass jc, jstring rootPath) {
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

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_isArchive
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD fileAttributes;
    
    fileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(fileAttributes & FILE_ATTRIBUTE_ARCHIVE) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_setArchive
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    
    if(SetFileAttributesW(wcpFullPath, FILE_ATTRIBUTE_ARCHIVE)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    } 
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_isNormal
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD fileAttributes;
    
    fileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(fileAttributes & FILE_ATTRIBUTE_NORMAL) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_setNormal
  (JNIEnv *env, jclass jc, jstring fullPath) { 
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

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_isReadOnly
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD fileAttributes;
    
    fileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(fileAttributes & FILE_ATTRIBUTE_READONLY) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_isSystem
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD fileAttributes;
    
    fileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(fileAttributes & FILE_ATTRIBUTE_SYSTEM) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_setSystem
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
            
    if(SetFileAttributesW(wcpFullPath, FILE_ATTRIBUTE_SYSTEM)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_isTemporary
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD fileAttributes;
    
    fileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(fileAttributes & FILE_ATTRIBUTE_TEMPORARY) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_setTemporary
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);    
    
    if(SetFileAttributesW(wcpFullPath, FILE_ATTRIBUTE_TEMPORARY)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    }
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_isCompressed
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD fileAttributes;
    
    fileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(fileAttributes & FILE_ATTRIBUTE_COMPRESSED) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_isEncrypted
  (JNIEnv *env, jclass jc, jstring fullPath) { 
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    DWORD fileAttributes;
    
    fileAttributes = GetFileAttributesW(wcpFullPath);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    if(fileAttributes & FILE_ATTRIBUTE_ENCRYPTED) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }    
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32NativeFileUtil_setHidden
  (JNIEnv *env, jclass jc, jstring fullPath) {
    LPCWSTR wcpFullPath = (LPCWSTR) (*env)->GetStringChars(env, fullPath, NULL);
    
    if(SetFileAttributesW(wcpFullPath, FILE_ATTRIBUTE_HIDDEN)) {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_TRUE;
    }
    else {
        (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
        return JNI_FALSE;
    }  
}
