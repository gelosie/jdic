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

#include <jni.h>
#include <sys/types.h>
#include <sys/statvfs.h>
#include "SolarisFileUtil.h"

/*
 * author: Fábio Castilho Martins
 */

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_fileutil_impl_SolarisFileUtil_getFreeSpace
  (JNIEnv *env, jobject obj, jstring fullPath) {
      
    struct statvfs* pStatvfs  = (struct statvfs*) malloc(sizeof(struct statvfs));
    int status;
    jlong retorno;
    
	char* cpFullPath = (char*) (*env)->GetStringUTFChars(env, fullPath, NULL);
    
    status = statvfs(cpFullPath, pStatvfs);
    retorno = pStatvfs->f_bavail;
    
    (*env)->ReleaseStringUTFChars(env, fullPath, cpFullPath);
    free(pStatvfs);
    
    return retorno;
}