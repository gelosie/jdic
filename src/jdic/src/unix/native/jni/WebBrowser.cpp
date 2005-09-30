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

#include <jawt_md.h>
#include <jawt.h>
#include <X11/Xlib.h>
#include "WebBrowser.h"
#include <stdlib.h>
#include <dlfcn.h>

/*
 * Class:     org_jdesktop_jdic_browser_WebBrowser
 * Method:    nativeGetWindow
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_browser_WebBrowser_nativeGetWindow
  (JNIEnv *env, jobject canvas, jstring javaHome)
{
    JAWT awt;
    JAWT_DrawingSurface* ds;
    JAWT_DrawingSurfaceInfo* dsi;
    JAWT_X11DrawingSurfaceInfo* dsi_x11;
    jboolean result;
    jint lock;
    Drawable handle_x11 = 0;

    awt.version = JAWT_VERSION_1_3;
    result = JAWT_GetAWT(env, &awt);
    if (result != JNI_FALSE)
    {
        ds = awt.GetDrawingSurface(env, canvas);
        if (ds != NULL)
        {
            lock = ds->Lock(ds);
            if ((lock & JAWT_LOCK_ERROR) == 0)
            {
                dsi = ds->GetDrawingSurfaceInfo(ds);
                dsi_x11 = (JAWT_X11DrawingSurfaceInfo*)dsi->platformInfo;
                handle_x11 = (Drawable) dsi_x11->drawable;
                ds->FreeDrawingSurfaceInfo(dsi);
                ds->Unlock(ds);
            }
        }
        awt.FreeDrawingSurface(ds);
    }
    return (jint)handle_x11;
}

