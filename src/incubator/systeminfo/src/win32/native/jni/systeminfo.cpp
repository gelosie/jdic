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
#include <stdio.h>
#include <windows.h>
#include <jni.h>
#include "systeminfo.h"

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_systeminfo_SystemInfo_nativeGetSessionIdleTime
  (JNIEnv *, jclass) {
  DWORD tickCount;
  LASTINPUTINFO lastInputInfo;
  lastInputInfo.cbSize = sizeof(LASTINPUTINFO);

  GetLastInputInfo(&lastInputInfo);
  tickCount = GetTickCount();

  return (jlong)(tickCount - lastInputInfo.dwTime);
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_systeminfo_SystemInfo_nativeIsSessionLocked
  (JNIEnv *, jclass) {
  
  HDESK hDesktop;

  
   // Try to open the desktop that user is currently on. This
   // desktop will return a zero for access error which means 
   // windows workstation is locked.
   hDesktop = OpenInputDesktop(0, // DWORD dwFlags
                               0, // BOOL fInherit
                               0  // ACCESS_MASK dwDesiredAccess
                               );
                        
   if(hDesktop == 0L)
   {
      // If the call fails due to access denied, the windowslogon
      // is running because the specified desktop exists you just
      // don't have any access.
      return JNI_TRUE;      
   }

   CloseDesktop(hDesktop);
   return JNI_FALSE;

}

