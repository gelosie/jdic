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
#include "GnomeUtility.h"
#include <stdio.h>
#include <stdlib.h>

#include <glib.h>
#include <gconf/gconf-client.h>
  
#define GCONF_URL_HANDLER_PATH "/desktop/gnome/url-handlers/"
#define SCHEME "mailto"

JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_desktop_internal_impl_GnomeUtility_nativeGetDefaultMailerPath
  (JNIEnv *env, jclass obj) {
    g_type_init();
    GConfClient *client = gconf_client_get_default ();
    char* path = g_strconcat (GCONF_URL_HANDLER_PATH, SCHEME, "/command", NULL);
    char* command = gconf_client_get_string (client, path, NULL);

    char* mailerPathStr = NULL;
    if (command != NULL) {
        char **argv;
        int argc;
        if (g_shell_parse_argv (command, &argc, &argv, NULL)) {
            mailerPathStr = argv[0];
        }
    }

    return (mailerPathStr == NULL) ? NULL : env->NewStringUTF(mailerPathStr);
}
