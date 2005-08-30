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
#include <sys/param.h>
#include <sys/mount.h>
#include "MacOSXNativeFileUtil.h"

/*
 * author: Fábio Castilho Martins
 */

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_fileutil_impl_MacOSXNativeFileUtil_getFreeSpace
  (JNIEnv *env, jclass jc, jstring fullPath) {      
      
    struct statfs* pStatfs  = (struct statfs*) malloc(sizeof(struct statfs));
    int status;
    jlong retorno;
    
	char* cpFullPath = (char*) (*env)->GetStringUTFChars(env, fullPath, NULL);
    
    status = statfs(cpFullPath, pStatfs);
    retorno = pStatfs->f_bavail * pStatfs->f_bsize;
    
    (*env)->ReleaseStringUTFChars(env, fullPath, cpFullPath);
    free(pStatfs);
    
    return retorno;
}

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_fileutil_impl_MacOSXNativeFileUtil_getTotalSpace
  (JNIEnv *env, jclass jc, jstring fullPath) {      
      
    struct statfs* pStatfs  = (struct statfs*) malloc(sizeof(struct statfs));
    int status;
    jlong retorno;
    
	char* cpFullPath = (char*) (*env)->GetStringUTFChars(env, fullPath, NULL);
    
    status = statfs(cpFullPath, pStatfs);
    retorno = pStatfs->f_blocks * pStatfs->f_bsize;
    
    (*env)->ReleaseStringUTFChars(env, fullPath, cpFullPath);
    free(pStatfs);
    
    return retorno;
}