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

#ifdef WIN32
#include <windows.h>
#endif

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "InitUtility.h"
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     org_jdesktop_jdic_init_InitUtility
 * Method:    getEnv
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_init_InitUtility_getEnv
  (JNIEnv * env, jclass /*obj*/, jstring envVar)
{
    const char* pEnvVar = env->GetStringUTFChars(envVar, JNI_FALSE);
    char * pEnvValue;
    if (NULL != pEnvVar)
    {
        pEnvValue = getenv(pEnvVar);
        env->ReleaseStringUTFChars(envVar, pEnvVar);
        if (NULL != pEnvValue) {
            jstring envValue = env->NewStringUTF(pEnvValue);
            return envValue;
        }
    } 
    return NULL;   
}

/*
 * Class:     org_jdesktop_jdic_init_InitUtility
 * Method:    setEnv
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_init_InitUtility_setEnv
  (JNIEnv * env, jclass /*obj*/, jstring envVar, jstring envValue)
{
    const char* pEnvVar = env->GetStringUTFChars(envVar, JNI_FALSE);
    const char* pEnvValue = env->GetStringUTFChars(envValue, JNI_FALSE);
    if (NULL != pEnvVar) 
    {
        if (NULL != pEnvValue) 
        {
#ifdef WIN32
            SetEnvironmentVariable(pEnvVar, pEnvValue);
#else
            char *newEnv = (char*)malloc(strlen(pEnvVar) + strlen(pEnvValue) + 2);
            strcpy(newEnv, pEnvVar);
            strcat(newEnv, "=");
            strcat(newEnv, pEnvValue);
            putenv(newEnv);
#endif
               env->ReleaseStringUTFChars(envValue, pEnvValue);
        }
        env->ReleaseStringUTFChars(envVar, pEnvVar);
    }
}

#ifdef __cplusplus
}
#endif
