/*
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
#include "XdgDirectory.h"
#include <stdlib.h>
#include <glib.h>
#include <gconf/gconf-client.h>

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_icons_impl_XdgDirectory_gConfInit
  (JNIEnv *env, jclass) {  
    g_type_init();
}

/*
 * Class:     org_jdesktop_jdic_icons_impl_XdgDirectory
 * Method:    getEnvironmentVariable
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_icons_impl_XdgDirectory_getEnvironmentVariable
  (JNIEnv *env, jclass, jstring key) {

    jboolean isCopy;
    char const *utf= env->GetStringUTFChars(key, &isCopy);

    char const *value= getenv(utf);

    env->ReleaseStringUTFChars(key, utf);

    if(!value || !*value)
        return 0;

    return env->NewStringUTF(value);
}

/*
 * Class:     org_jdesktop_jdic_icons_impl_XdDirectory
 * Method:    getGconfValue
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_icons_impl_XdgDirectory_getGconfValue
  (JNIEnv *env, jclass, jstring jKey) {
    char const *cKey= env->GetStringUTFChars(jKey, 0);

    GConfClient *client = gconf_client_get_default();
    char *cValue= gconf_client_get_string (client, cKey, NULL);
    g_object_unref(client);

    env->ReleaseStringUTFChars(jKey, cKey);

    if(cValue==0)
        return 0;

    jstring jValue= env->NewStringUTF(cValue);
    g_free(cValue);
    return jValue;
}


