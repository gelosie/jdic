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

/*
 * Class:     org_jdesktop_jdic_browser_WebBrowser
 * Method:    nativeGetWindow
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_browser_WebBrowser_nativeGetWindow
  (JNIEnv *env, jobject canvas)
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
    
    _hAWT = LoadLibrary("jawt.dll");
    if (_hAWT)
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

/*
 * Class:     org_jdesktop_jdic_browser_WebBrowser
 * Method:    nativeGetBrowserPath
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_browser_WebBrowser_nativeGetBrowserPath
  (JNIEnv *env, jobject)
{
    HKEY hkey;
    DWORD type, cb;
    char *p, value[256] = "\0";
    
    // use mozilla if MOZILLA_FIVE_HOME defined (this is useful for debugging)
    p = getenv("MOZILLA_FIVE_HOME");
    if (p) {
        strncpy(value, p, 244);
        strcat(value, "\\mozilla.exe");
        return env->NewStringUTF(value);
    }

    // get the default http protocol handler
    if (RegOpenKey(HKEY_CLASSES_ROOT, "http\\shell\\open\\command", &hkey) != ERROR_SUCCESS)
        return 0;
    
    cb = sizeof(value);
    if (RegQueryValueEx(hkey, "", 0, &type, (LPBYTE)value, &cb) != ERROR_SUCCESS)
        return 0;
    
    RegCloseKey(hkey);
   
    p = strstr(strlwr(value), "mozilla.exe");
    if (p) {
        char szName[256];
        cb = sizeof(szName);
        if (RegOpenKey(HKEY_LOCAL_MACHINE, "SOFTWARE\\mozilla.org\\GRE", &hkey) == ERROR_SUCCESS) {
            if (RegEnumKeyEx(hkey, 0, szName, &cb, NULL, NULL, NULL, NULL) == ERROR_SUCCESS) {
                if (RegOpenKey(hkey, szName, &hkey) == ERROR_SUCCESS) {
                    cb = sizeof(value);
                    if (RegQueryValueEx(hkey, "GreHome", 0, &type, (LPBYTE)value, &cb) == ERROR_SUCCESS) {
                        char szEnv[1000];
                        sprintf(szEnv, "GRE_HOME=%s", value);
                        _putenv(szEnv);
                        sprintf(szEnv, "PATH=%s;%s", getenv("PATH"), value);
                        _putenv(szEnv);
                    }
                }
            }
        }
        strcpy(value, "mozilla.exe");
    }
    else {
        _putenv("JAVA_PLUGIN_WEBCONTROL_ENABLE=1");
        strcpy(value, "iexplore.exe");
    }

    return env->NewStringUTF(value);
}

/*
 * Class:     org_jdesktop_jdic_browser_WebBrowser
 * Method:    nativeSetEnv
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_browser_WebBrowser_nativeSetEnv
  (JNIEnv *env, jclass)
{
    _putenv("JAVA_PLUGIN_WEBCONTROL_ENABLE=1");
}
