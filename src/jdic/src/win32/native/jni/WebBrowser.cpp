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
#include "WebBrowser.h"
#include <stdlib.h>
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     org_jdesktop_jdic_browser_WebBrowser
 * Method:    nativeGetWindow
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_browser_WebBrowser_nativeGetWindow
  (JNIEnv *env, jobject canvas, jstring javaHome)
{
    typedef jboolean (JNICALL *PJAWT_GETAWT)(JNIEnv*, JAWT*);
    HMODULE _hAWT;     // JAWT module handle
    JAWT awt;
    JAWT_DrawingSurface* ds;
    JAWT_DrawingSurfaceInfo* dsi;
    JAWT_Win32DrawingSurfaceInfo* dsi_win;
    jboolean result;
    jint lock;
    HWND hWnd = 0;

    // Load jawt.dll from <java.home>\bin    
    char dllLocation[MAX_PATH] = {0};   
    const char* javaHomeStr = env->GetStringUTFChars(javaHome, NULL);
    sprintf(dllLocation, "%s%s", javaHomeStr, "\\bin\\jawt.dll");
    env->ReleaseStringUTFChars(javaHome, javaHomeStr);

    _hAWT = LoadLibrary((LPCTSTR)dllLocation);
    if (!_hAWT) 
    {
        return -1;
    }
    else 
    {
        PJAWT_GETAWT JAWT_GetAWT = (PJAWT_GETAWT)GetProcAddress(_hAWT, "_JAWT_GetAWT@8");
        if (JAWT_GetAWT)
        {
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
                        dsi_win = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
                        hWnd = dsi_win->hwnd;
                        ds->FreeDrawingSurfaceInfo(dsi);
                        ds->Unlock(ds);
                    }
                }
                awt.FreeDrawingSurface(ds);
            }
        }
    }
    return (jint)hWnd;
}

#ifdef __cplusplus
}
#endif
