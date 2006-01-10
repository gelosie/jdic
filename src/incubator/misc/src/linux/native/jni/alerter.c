/*
 *  Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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

#include "alerter.h"
#include <jawt.h>
#include <jawt_md.h>

/*
* This method will set the urgency hint. Some Window Managers use this hint to alert the user.
*/
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_misc_impl_LinuxAlerter_setUrgencyHint
  (JNIEnv *env, jobject canvas, jobject frame, jboolean alert)
{
    JAWT awt;
    JAWT_DrawingSurface* ds;
    JAWT_DrawingSurfaceInfo* dsi;
    JAWT_X11DrawingSurfaceInfo* dsi_x11;
    jboolean result;
    jint lock;
    XWMHints* hints;
    
    awt.version = JAWT_VERSION_1_4;
    
    result = JAWT_GetAWT(env, &awt);
    /*Error checking*/
    if(result == JNI_FALSE) {
        return;
    }

    ds = awt.GetDrawingSurface(env, frame);
    /*Error checking*/
    if(ds == NULL) {
        return;
    }
    
    lock = ds->Lock(ds);
    /*Error checking*/
    if((lock & JAWT_LOCK_ERROR) != 0) {
        return;
    }
    
    dsi = ds->GetDrawingSurfaceInfo(ds);
    dsi_x11 = (JAWT_X11DrawingSurfaceInfo*)dsi->platformInfo;
    
    /*Setting the urgency hint*/
    hints = XGetWMHints(dsi_x11->display, dsi_x11->drawable);
    /*Error checking*/
    if(hints == NULL) {
        return;
    }
    
    if(alert) {
        hints->flags = hints->flags | XUrgencyHint;
    }
    else {
        hints->flags = hints->flags ^ XUrgencyHint;   
    }

    XSetWMHints(dsi_x11->display, dsi_x11->drawable, hints);        
    XFree(hints);

    ds->FreeDrawingSurfaceInfo(dsi);
    ds->Unlock(ds);
    awt.FreeDrawingSurface(ds);
}
