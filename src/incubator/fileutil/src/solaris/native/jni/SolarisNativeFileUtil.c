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

#include <jni.h>
#include <sys/statvfs.h>
#include <sys/types.h>
#include <dirent.h>
#include "SolarisNativeFileUtil.h"

/*
 * author: Fábio Castilho Martins
 */
 
JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_fileutil_SolarisNativeFileUtil_getFreeSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {      
      
    struct statvfs* pStatvfs  = (struct statvfs*) malloc(sizeof(struct statvfs));
    int status;
    jlong retorno;
    
	char* cpFullPath = (char*) (*env)->GetStringUTFChars(env, fullPath, NULL);
    
    status = statvfs(cpFullPath, pStatvfs);
    retorno = pStatvfs->f_bavail * pStatvfs->f_bsize;
    
    (*env)->ReleaseStringUTFChars(env, fullPath, cpFullPath);
    free(pStatvfs);
    
    return retorno;
}

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_fileutil_SolarisNativeFileUtil_getTotalSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {      
      
    struct statvfs* pStatvfs  = (struct statvfs*) malloc(sizeof(struct statvfs));
    int status;
    jlong retorno;
    
	char* cpFullPath = (char*) (*env)->GetStringUTFChars(env, fullPath, NULL);
    
    status = statvfs(cpFullPath, pStatvfs);
    retorno = pStatvfs->f_blocks * pStatvfs->f_frsize;
    
    (*env)->ReleaseStringUTFChars(env, fullPath, cpFullPath);
    free(pStatvfs);
    
    return retorno;
}

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_UnixNativeFileUtil_findFirst
  (JNIEnv *env, jobject obj, jstring fullPath) {
    char* cpFullPath = (char*) (*env)->GetStringUTFChars(env, fullPath, NULL);
    
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid= (*env)->GetFieldID(env, cls, "handle", "I");
    
    if (fid == NULL) {
        return; /* failed to find the field */
    }
    jint handle = (*env)->GetIntField(env, obj, fid);
    
    struct dirent *pDirEntry;
    
    DIR *pDir = opendir(cpFullPath);
    if(pDir == NULL) {
        return NULL;
    }    
    else {
        pDirEntry = readdir(pDir);
        if(pDirEntry == NULL) {
            return NULL;
        }
        else {
            handle = (jint) pDir;
            (*env)->SetIntField(env, obj, fid, handle);
            return (*env)->NewStringUTF(env, pDirEntry->d_name);
        }          
    }
}
  
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_fileutil_UnixNativeFileUtil_findNext
  (JNIEnv *env, jobject obj) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid= (*env)->GetFieldID(env, cls, "handle", "I");
    
    if (fid == NULL) {
        return; /* failed to find the field */
    }
    jint handle = (*env)->GetIntField(env, obj, fid);
    
    struct dirent *pDirEntry; 
    DIR *pDir = (DIR*) handle;
      
    pDirEntry = readdir(pDir);
        
    if(pDirEntry == NULL) {
        return NULL;
    }
    else {
        handle = (jint) pDir;
        (*env)->SetIntField(env, obj, fid, handle);
        return (*env)->NewStringUTF(env, pDirEntry->d_name);;
    }
}
  
JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_fileutil_UnixNativeFileUtil_findClose
  (JNIEnv *env, jobject obj) {
    jclass cls = (*env)->GetObjectClass(env, obj);
    jfieldID fid = fid = (*env)->GetFieldID(env, cls, "handle", "I");
    
    if (fid == NULL) {
        return; /* failed to find the field */
    }
    
    jint handle = (*env)->GetIntField(env, obj, fid);
    
    DIR *pDir = (DIR*) handle;
    
    if(closedir(pDir) == 0) {
        return JNI_TRUE;
    }
    else {
        return JNI_FALSE;
    }
}