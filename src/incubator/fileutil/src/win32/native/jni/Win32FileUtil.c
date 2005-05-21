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
#include <jni.h>
#include "Win32FileUtil.h"

/**
 * author: Fábio Castilho Martins
 */

JNIEXPORT jlongArray JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32FileUtil_getFreeSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {
    PWCHAR wcpFullPath = (PWCHAR) (*env)->GetStringChars(env, fullPath, NULL);
    PULARGE_INTEGER lpFreeBytesAvailable = (PULARGE_INTEGER) malloc(sizeof(ULARGE_INTEGER));
    PULARGE_INTEGER lpTotalNumberOfBytes = (PULARGE_INTEGER) malloc(sizeof(ULARGE_INTEGER));
    PULARGE_INTEGER lpTotalNumberOfFreeBytes = (PULARGE_INTEGER) malloc(sizeof(ULARGE_INTEGER));
    jlongArray retorno = (*env)->NewLongArray(env, 2);
    jlong buf[2];
    PWCHAR pTerminator = L"\0";

    wcpFullPath = (PWCHAR) realloc(wcpFullPath, sizeof(WCHAR) * (wcslen(wcpFullPath) + 1));
    wcsncat(wcpFullPath, pTerminator, 1);
    GetDiskFreeSpaceExW(wcpFullPath, lpFreeBytesAvailable, lpTotalNumberOfBytes, 
                             lpTotalNumberOfFreeBytes);
    buf[0] = (jlong) lpFreeBytesAvailable->LowPart;
    buf[1] = (jlong) lpFreeBytesAvailable->HighPart;
    (*env)->SetLongArrayRegion(env, retorno, 0, 2, buf);
    (*env)->ReleaseStringChars(env, fullPath, wcpFullPath);
    free(lpFreeBytesAvailable);    
    free(lpTotalNumberOfBytes);
    free(lpTotalNumberOfFreeBytes);
    
    return retorno;
}

JNIEXPORT jint JNICALL JNICALL Java_org_jdesktop_jdic_fileutil_impl_Win32FileUtil_recycle
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
