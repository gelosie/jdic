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


#include "stdafx.h"
#include <stdio.h>
#include <jni.h>
#include "wallpaper.h"

/*
 * Class:     org_jdesktop_jdic_misc_Wallpaper
 * Method:    nativeSetWallpaper
 * Signature: (Ljava/lang/String;I)J
 */
JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_misc_impl_WinWallpaper_nativeSetWallpaper
  (JNIEnv * env, jclass clazz, jstring prompt, jint mode)
{
    
    const char *wallpaperFileName = env->GetStringUTFChars(prompt, 0);
    WCHAR wszWallpaperFileName[255];
    unsigned int wallpaperFileNameLength = (unsigned int) strlen(wallpaperFileName) + 1;
    
    MultiByteToWideChar(CP_UTF8, 
                        0, 
                        wallpaperFileName, 
                        wallpaperFileNameLength, 
                        wszWallpaperFileName,
                        sizeof(wszWallpaperFileName)/sizeof(wszWallpaperFileName[0]));
    
    printf("Image File: %s \n", wallpaperFileName);
    
    // Pointer to IActiveDesktop interface.
	IActiveDesktop* pIActiveDesktop = NULL;
 
    InitCommonControls();
    CoInitialize ( NULL );
	HRESULT hr = CoCreateInstance(CLSID_ActiveDesktop, 
                                  NULL, 
                                  CLSCTX_INPROC_SERVER, 
		                          IID_IActiveDesktop, 
                                  (void**) &pIActiveDesktop);

	if (hr != S_OK)
    {
		pIActiveDesktop = NULL;
    }

    if (pIActiveDesktop == NULL)
	{  
       // Windows without Active Desktop
       // Bitmaps only.
       SystemParametersInfo (SPI_SETDESKWALLPAPER, (UINT) NULL, (PVOID) wallpaperFileName, SPIF_UPDATEINIFILE | SPIF_SENDCHANGE);       
       printf("No Active Desktop BMP files only.");
    }
    else
    {

        
     	WALLPAPEROPT wpOptions;
        COMPONENTSOPT compOptions;
    	compOptions.dwSize = sizeof(COMPONENTSOPT);
    	compOptions.fActiveDesktop = TRUE;
    	compOptions.fEnableComponents = TRUE;
        
        pIActiveDesktop->SetDesktopItemOptions(&compOptions, 0);
    
        wpOptions.dwSize = sizeof(WALLPAPEROPT);
 

        // todo put magic numbers in header.
    	if (mode == 8)
        {   
            wpOptions.dwStyle = WPSTYLE_TILE;
        }
        else if (mode == 2)
        {
    		wpOptions.dwStyle = WPSTYLE_CENTER;
        }
        else if (mode == 1)
        {
    		wpOptions.dwStyle = WPSTYLE_STRETCH;
        }
        else
        {
            wpOptions.dwStyle = WPSTYLE_CENTER;
        }
        
    	pIActiveDesktop->SetWallpaperOptions(&wpOptions, 0);
    	
        // Set background wallpaper
        pIActiveDesktop->SetWallpaper((LPCWSTR)wszWallpaperFileName, 0);
        pIActiveDesktop->ApplyChanges(AD_APPLY_ALL);

        pIActiveDesktop->Release();
        pIActiveDesktop = NULL;

     }
    
    CoUninitialize();

    // free up resources.
    env->ReleaseStringUTFChars(prompt, wallpaperFileName);

    return 0;
}
