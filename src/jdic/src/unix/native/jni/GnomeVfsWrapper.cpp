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
#include "GnomeVfsWrapper.h"
#include <stdio.h>
#include <stdlib.h>

#include <libgnomevfs/gnome-vfs-init.h>
#include <libgnomevfs/gnome-vfs-mime.h>
#include <libgnomevfs/gnome-vfs-mime-handlers.h>
#include <libgnomevfs/gnome-vfs-mime-info.h>
  
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1get_1mime_1type
  (JNIEnv *env, jclass cl, jstring url) {
  gnome_vfs_init();

  const char* urlStr = env->GetStringUTFChars(url, JNI_FALSE);
  const char* mimeTypeStr = gnome_vfs_get_mime_type(urlStr);
  env->ReleaseStringUTFChars(url, urlStr);

  if(mimeTypeStr == NULL) {
    return NULL;
  } else {
    jstring mimeType = env->NewStringUTF(mimeTypeStr);
    return mimeType;
  }
}  


JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1mime_1get_1value
  (JNIEnv *env, jclass cl, jstring mimeType, jstring key) {
  gnome_vfs_init();	  

  const char* mimeTypeStr = env->GetStringUTFChars(mimeType, JNI_FALSE);
  const char* keyStr = env->GetStringUTFChars(key, JNI_FALSE);
  const char* keyValueStr = gnome_vfs_mime_get_value(mimeTypeStr, keyStr);
  env->ReleaseStringUTFChars(mimeType, mimeTypeStr);
  env->ReleaseStringUTFChars(key, keyStr);  

  if(keyValueStr == NULL) {
    return NULL;
  } else {  
    jstring keyValue = env->NewStringUTF(keyValueStr); 
    return keyValue;
  }   
}
  
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1mime_1get_1description
  (JNIEnv *env, jclass cl, jstring mimeType) {
  gnome_vfs_init();

  const char* mimeTypeStr = env->GetStringUTFChars(mimeType, JNI_FALSE);
  const char* descStr = gnome_vfs_mime_get_description(mimeTypeStr);
  env->ReleaseStringUTFChars(mimeType, mimeTypeStr);

  if(descStr == NULL) {
    return NULL;
  } else {
    jstring desc = env->NewStringUTF(descStr);
    return desc;
  }
}

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1mime_1get_1icon
  (JNIEnv *env, jclass cl, jstring mimeType) {
  gnome_vfs_init();

  const char* mimeTypeStr = env->GetStringUTFChars(mimeType, JNI_FALSE);
  const char* iconFileStr = gnome_vfs_mime_get_icon(mimeTypeStr);
  env->ReleaseStringUTFChars(mimeType, mimeTypeStr);

  if(iconFileStr == NULL) {
    return NULL;
  } else {
    jstring iconFile = env->NewStringUTF(iconFileStr);
    return iconFile;
  }
}	  

JNIEXPORT jobjectArray JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1mime_1get_1key_1list
  (JNIEnv *env, jclass cl, jstring mimeType) {
  gnome_vfs_init();

  const char* mimeTypeStr = env->GetStringUTFChars(mimeType, JNI_FALSE);
  GList* keyList = gnome_vfs_mime_get_key_list(mimeTypeStr);
  env->ReleaseStringUTFChars(mimeType, mimeTypeStr);

  int listLen;
  if (keyList != NULL) {
    listLen = g_list_length(keyList);
  } else {
    listLen = 0;
  }
  
  if(listLen == 0) {
    return NULL;
  } else {
    jobjectArray retArray;
    const char* keyStr;
    retArray = (jobjectArray)env->NewObjectArray(listLen, env->FindClass("java/lang/String"),
		    				 env->NewStringUTF(""));
    for(int i = 0; i < listLen; i++) {
      keyStr = (const char*)g_list_nth_data(keyList, i);
      env->SetObjectArrayElement(retArray, i, env->NewStringUTF(keyStr));
    }
  
    return retArray;
  }    
}

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1mime_1get_1default_1application_1command
  (JNIEnv *env, jclass cl, jstring mimeType) {
  gnome_vfs_init();

  const char* mimeTypeStr = env->GetStringUTFChars(mimeType, JNI_FALSE);
  GnomeVFSMimeApplication *mimeApp = gnome_vfs_mime_get_default_application(mimeTypeStr);
  env->ReleaseStringUTFChars(mimeType, mimeTypeStr);

  if(mimeApp == NULL) {
    return NULL;
  } else {
    const char* mimeAppCommandStr = mimeApp->command;
    if(mimeAppCommandStr == NULL) {
      return NULL;
    } else {      
      jstring mimeAppCommand = env->NewStringUTF(mimeAppCommandStr);
      return mimeAppCommand;
    }
  }
}

JNIEXPORT jobjectArray JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1get_1registered_1mime_1types
  (JNIEnv *env, jclass cl) {
  gnome_vfs_init();

  GList* mimeTypeList = gnome_vfs_get_registered_mime_types();

  int listLen;
  if (mimeTypeList != NULL) {
    listLen = g_list_length(mimeTypeList);
  } else {
    listLen = 0;
  }
  if(listLen == 0) {
    return NULL;
  } else {
    jobjectArray retArray;
    const char* mimeTypeStr;
    retArray = (jobjectArray)env->NewObjectArray(listLen, env->FindClass("java/lang/String"),
			                                                   env->NewStringUTF(""));
    for(int i = 0; i < listLen; i++) {
      mimeTypeStr = (const char*)g_list_nth_data(mimeTypeList, i);
      if(mimeTypeStr != NULL) {
            env->SetObjectArrayElement(retArray, i, env->NewStringUTF(mimeTypeStr));
      }
    }

   return retArray;
  }
}	 

JNIEXPORT jobjectArray JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_gnome_1vfs_1mime_1get_1extensions_1list
    (JNIEnv *env, jclass cl, jstring mimeType) {
  gnome_vfs_init();
  
  const char* mimeTypeStr = env->GetStringUTFChars(mimeType, JNI_FALSE);
  GList* extList = gnome_vfs_mime_get_extensions_list(mimeTypeStr);
  env->ReleaseStringUTFChars(mimeType, mimeTypeStr);

  int listLen;
  if (extList != NULL) {
    listLen = g_list_length(extList);
  } else {
    listLen = 0;
  }
  if(listLen == 0) {
    return NULL;
  } else {
    jobjectArray retArray;
    const char* extensionStr;
    retArray = (jobjectArray)env->NewObjectArray(listLen, env->FindClass("java/lang/String"),			                                                 env->NewStringUTF(""));
    for(int i = 0; i < listLen; i++) {
      extensionStr = (const char*)g_list_nth_data(extList, i);
      if(extensionStr != NULL) {
        env->SetObjectArrayElement(retArray, i, env->NewStringUTF(extensionStr));
      }
    }

   return retArray;
  }  
}	    

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_filetypes_internal_GnomeVfsWrapper_getenv
    (JNIEnv *env, jclass cl, jstring envName) {
  const char* envNameStr = env->GetStringUTFChars(envName, JNI_FALSE);
  const char* envValueStr = getenv(envNameStr);
  env->ReleaseStringUTFChars(envName, envNameStr);

  return (envValueStr == NULL) ? 0 : env->NewStringUTF(envValueStr);
}
