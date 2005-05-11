/*
 *  Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 *  subject to license terms.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
 
#include "jawt_md.h"
#include "alerter.h"

/*
* This method will blink the program's TaskBar once.
* It's based on  Joshua Marinacci's original code.
*/
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_misc_impl_WindowsAlerter_alertWindows
  (JNIEnv *env, jobject canvas, jobject frame) {    
    //(JAWT_Win32DrawingSurfaceInfo)
    JAWT awt;
    JAWT_DrawingSurface* ds;
    JAWT_DrawingSurfaceInfo* dsi;
    JAWT_Win32DrawingSurfaceInfo* dsi_win32;
    jint lock;
    jint result;
  
    BOOL retorno;
    
    awt.version = JAWT_VERSION_1_4;
    result = JAWT_GetAWT(env, &awt);
  
    ds = awt.GetDrawingSurface(env, frame);
    lock = ds->Lock(ds);
    dsi = ds->GetDrawingSurfaceInfo(ds);
    dsi_win32 = (JAWT_Win32DrawingSurfaceInfo*)dsi->platformInfo;
  
    retorno = FlashWindow(dsi_win32->hwnd, TRUE);
    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);
}

/*
* This method gets the Blink Rate. The GetCaretBlinkTime returns an unsigned int, hence the jlong return.
*/
JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_misc_impl_WindowsAlerter_getBlinkRate
  (JNIEnv *env, jobject obj) {
    jlong retorno = GetCaretBlinkTime();
    return retorno;
}

