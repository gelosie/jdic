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

HHOOK 	keyboardHook = NULL;
HHOOK 	mouseHook = NULL;
LONG	mouseX = -1;
LONG	mouseY = -1;
DWORD	lastEvent = 0;

HINSTANCE handleInstance = NULL;

LRESULT CALLBACK KeyboardTracker(int code, WPARAM wParam, LPARAM lParam)
{
	if (code==HC_ACTION) lastEvent = GetTickCount();
	return ::CallNextHookEx(keyboardHook, code, wParam, lParam);
}

LRESULT CALLBACK MouseTracker(int code, WPARAM wParam, LPARAM lParam)
{
	if (code==HC_ACTION) {
		MOUSEHOOKSTRUCT* pointer = (MOUSEHOOKSTRUCT*)lParam;
		if (pointer->pt.x != mouseX || pointer->pt.y != mouseY) {
			mouseX = pointer->pt.x;
			mouseY = pointer->pt.y;
			lastEvent = GetTickCount();
		}
	}
	return ::CallNextHookEx(mouseHook, code, wParam, lParam);
}

__declspec(dllexport) BOOL load()
{
	if (keyboardHook == NULL)
		keyboardHook = SetWindowsHookEx(WH_KEYBOARD, KeyboardTracker, handleInstance, 0);
	if (mouseHook == NULL)
		mouseHook = SetWindowsHookEx(WH_MOUSE, MouseTracker, handleInstance, 0);

	lastEvent = GetTickCount();

	if (!keyboardHook || !mouseHook) return FALSE;
	else return TRUE;
}

__declspec(dllexport) void unload()
{
	if (keyboardHook) {
		UnhookWindowsHookEx(keyboardHook);
		keyboardHook = NULL;
	}
	if (mouseHook) {
		UnhookWindowsHookEx(mouseHook);
		mouseHook = NULL;
	}
}

int WINAPI DllMain(HINSTANCE hInstance, DWORD dwReason, LPVOID lpReserved)
{
	switch(dwReason) {
		case DLL_PROCESS_ATTACH:
			handleInstance = hInstance;
			DisableThreadLibraryCalls(handleInstance);
			load();
			break;
		case DLL_PROCESS_DETACH:
			unload();
			break;
	}
	return TRUE;
}

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_systeminfo_SystemInfo_nativeGetSessionIdleTime
  (JNIEnv *, jclass) {
	return (GetTickCount() - lastEvent);
}
