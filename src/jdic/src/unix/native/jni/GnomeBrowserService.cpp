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
#include "GnomeBrowserService.h"
#include <string.h>

#include <libgnome/libgnome.h>
#include <gconf/gconf-client.h>
  
#define GCONF_URL_HANDLER_PATH "/desktop/gnome/url-handlers/"

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_desktop_internal_impl_GnomeBrowserService_nativeBrowseURL
  (JNIEnv *env, jobject obj, jstring url) 
{
    const char* urlStr = env->GetStringUTFChars(url, JNI_FALSE);

    char *command = NULL;
    gboolean result;

    g_type_init();
    GConfClient *client = gconf_client_get_default ();
    char *schemes[2] = {"http", "unknown"};
    char *path;
    for (int i = 0; i < sizeof(schemes); i++) {
        path = g_strconcat (GCONF_URL_HANDLER_PATH, schemes[i], "/command", NULL);
        command = gconf_client_get_string (client, path, NULL);
        if (command != NULL) {
            break;
        }
    }
  
    char **argv;
    int argc;
    
    if (command == NULL) {
        result = false;
    } else {
        if (!g_shell_parse_argv (command, &argc, &argv, NULL)) {
            result = false;
        } else {       
            for (int i = 0; i < argc; i++) {
                char *arg;
                if (strcmp (argv[i], "%s") != 0)
                    continue;

                arg = argv[i];
                argv[i] = g_strdup (urlStr);
                g_free (arg);
            }

            result = g_spawn_async (NULL,
                                    argv,
                                    NULL,
                                    G_SPAWN_SEARCH_PATH,
                                    NULL,
                                    NULL,
                                    NULL,
                                    NULL);
        }
    }

    env->ReleaseStringUTFChars(url, urlStr);
    return result;
}
