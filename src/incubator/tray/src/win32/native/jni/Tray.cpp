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
 
#include <windows.h>
#include <windowsx.h> 
#include <jni.h>
#include "WinTrayIconService.h"
#include "WinSystemTrayService.h"

HINSTANCE hInstance;

#define IS_NT      (!(::GetVersion() & 0x80000000))

#define TRAY_NOTIFYICON (WM_APP+100) 

#ifndef ARRAYSIZE 
#define ARRAYSIZE(a)    (sizeof(a)/sizeof(a[0])) 
#endif  

static int inited=0;
ATOM wndClass;

HWND messageWindow;
TCHAR       g_szClassName[] = TEXT("JDIC_Tray");
TCHAR       g_szTitle[] = TEXT("Tray Window");

LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam);


typedef struct tagBitmapheader  {
    BITMAPINFOHEADER bmiHeader;
    DWORD            dwMasks[256];
}   Bitmapheader, *LPBITMAPHEADER;

/* Initialize the Java VM instance variable when the library is 
first loaded */
JavaVM *jvm;

JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved)
{
    jvm = vm;
    return JNI_VERSION_1_2;
	
}

jmethodID notifyEventMID;
jclass peerCls;

int Initialize(JNIEnv *env) {
	
	WNDCLASS  wc;
	
    ZeroMemory(&wc, sizeof(wc));
	
	wc.style          = CS_HREDRAW | CS_VREDRAW;
	wc.lpfnWndProc    = (WNDPROC)WndProc;
	wc.cbClsExtra     = 0;
	wc.cbWndExtra     = 0;
	wc.hInstance      = hInstance;
	wc.hIcon          = NULL;
	wc.hCursor        = LoadCursor(NULL, IDC_ARROW);
	wc.hbrBackground  = (HBRUSH) GetStockObject(WHITE_BRUSH);
	wc.lpszClassName  = g_szClassName;
	
	wndClass =  ::RegisterClass(&wc);
	
	messageWindow = CreateWindowEx(  0,
		g_szClassName,
		g_szTitle,
		WS_OVERLAPPEDWINDOW | WS_CLIPCHILDREN,
		0,
		0,
		0,
		0,
		NULL,
		NULL,
		hInstance,
		NULL);
	
    peerCls = env->FindClass("org/jdesktop/jdic/tray/internal/impl/WinTrayIconService");
    
    notifyEventMID = env->GetStaticMethodID(peerCls, "notifyEvent", "(IIII)V");
    
	
	return (messageWindow != NULL); 
}



BOOL TrayMessage(HWND hWnd, DWORD dwMessage, UINT uID, HICON hIcon, PSTR pszTip)
{
    BOOL res;
	NOTIFYICONDATA tnd;
	
	tnd.cbSize		= sizeof(tnd);
	tnd.hWnd		= hWnd;
	tnd.uID			= uID;
	
	tnd.uFlags		= NIF_MESSAGE|NIF_ICON|NIF_TIP;
	tnd.uCallbackMessage	= TRAY_NOTIFYICON+uID;
	tnd.hIcon		= hIcon;

	if (pszTip) {
		lstrcpyn(tnd.szTip, pszTip, ARRAYSIZE(tnd.szTip));
    }  
	else {
		tnd.szTip[0] = '\0';
	}
	
	res = Shell_NotifyIcon(dwMessage, &tnd);
	return res;
}

JNIEXPORT void * JNICALL
JNU_GetEnv(JavaVM *vm, jint version)
{
    void *env;
    vm->GetEnv(&env, version);
    return env;
}


LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	if (uMsg >=  TRAY_NOTIFYICON)
        {
            POINT pt;
            ::GetCursorPos(&pt);
            JNIEnv *env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
            env->CallStaticVoidMethod(peerCls,notifyEventMID, uMsg-TRAY_NOTIFYICON, lParam,pt.x,pt.y);
            return 1;
        }
	
    return DefWindowProc(hWnd, uMsg, wParam, lParam);
}




extern "C" BOOL APIENTRY DllMain(HANDLE hInst, DWORD ul_reason_for_call, 
                                 LPVOID)
{
	
    switch (ul_reason_for_call) {
    case DLL_PROCESS_ATTACH:
		hInstance = (HINSTANCE) hInst;
		break;
    case DLL_PROCESS_DETACH:
		break;
    }
    return TRUE;
}



HBITMAP create_BMP(HWND hW,int* imageData,int nSS, int nW, int nH)
{
    Bitmapheader    bmhHeader;
    HDC             hDC;
    char            *ptrImageData;
    HBITMAP         hbmpBitmap;
    HBITMAP         hBitmap;
	
    int             nNumChannels    = 3;
	
    if (!hW) {
        hW = ::GetDesktopWindow();
    }
    hDC = ::GetDC(hW);
    if (!hDC) {
        return NULL;
    }
	
    memset(&bmhHeader, 0, sizeof(Bitmapheader));
    bmhHeader.bmiHeader.biSize              = sizeof(BITMAPINFOHEADER);
    bmhHeader.bmiHeader.biWidth             = nW;
    bmhHeader.bmiHeader.biHeight            = -nH;
    bmhHeader.bmiHeader.biPlanes            = 1;
	
    bmhHeader.bmiHeader.biBitCount          = 24;
    bmhHeader.bmiHeader.biCompression       = BI_RGB;
	
    hbmpBitmap = ::CreateDIBSection(hDC, (BITMAPINFO*)&(bmhHeader),
		DIB_RGB_COLORS,
		(void**)&(ptrImageData),
		NULL, 0);
    int  *srcPtr = imageData;
    char *dstPtr = ptrImageData;
    if (!dstPtr) {
		ReleaseDC(hW, hDC);
        return NULL;
    }
    for (int nOutern = 0; nOutern < nH; nOutern++ ) {
        for (int nInner = 0; nInner < nSS; nInner++ ) {
            dstPtr[2] = (*srcPtr >> 0x10) & 0xFF;
            dstPtr[1] = (*srcPtr >> 0x08) & 0xFF;
            dstPtr[0] = *srcPtr & 0xFF;
			
            srcPtr++;
            dstPtr += nNumChannels;
        }
    }
	
    // convert it into DDB to make CustomCursor work on WIN95
    hBitmap = CreateDIBitmap(hDC, 
			     (BITMAPINFOHEADER*)&bmhHeader,
				 CBM_INIT,
				 (void *)ptrImageData,
				 (BITMAPINFO*)&bmhHeader,
				 DIB_RGB_COLORS);
	
    ::DeleteObject(hbmpBitmap);
    ::ReleaseDC(hW, hDC);
    ::GdiFlush();
    return hBitmap;
}

void destroy_BMP(HBITMAP hBMP)
{
    if (hBMP) {
        ::DeleteObject(hBMP);
    }
}


/************************************************************************
* WCustomCursor native methods
*/

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_createIconIndirect(
																										   JNIEnv *env, jobject self, jintArray intRasterData, jbyteArray andMask, 
																										   jint nSS, jint nW, jint nH, jint xHotSpot, jint yHotSpot)
{
    
    int length = env->GetArrayLength(andMask);
    jbyte *andMaskPtr = new jbyte[length];
    env->GetByteArrayRegion(andMask, 0, length, andMaskPtr);
	
    HBITMAP hMask = ::CreateBitmap(nW, nH, 1, 1, (BYTE *)andMaskPtr);
    ::GdiFlush();

	delete(andMaskPtr);
	
    jint *intRasterDataPtr = NULL;
    HBITMAP hColor = NULL;
    try {
        intRasterDataPtr = 
			(jint *)env->GetPrimitiveArrayCritical(intRasterData, 0);
		hColor = create_BMP(NULL, (int *)intRasterDataPtr, nSS, nW, nH);
    } catch (...) {
        if (intRasterDataPtr != NULL) {
			env->ReleasePrimitiveArrayCritical(intRasterData,
				intRasterDataPtr, 0);
		}
		throw;
    }
	
    env->ReleasePrimitiveArrayCritical(intRasterData, intRasterDataPtr, 0);
    intRasterDataPtr = NULL;
    
    HICON hIcon = NULL;
	
    if (hMask && hColor) {
        ICONINFO icnInfo;
		memset(&icnInfo, 0, sizeof(ICONINFO));
		icnInfo.hbmMask = hMask;
		icnInfo.hbmColor = hColor;
		icnInfo.fIcon = FALSE;
		icnInfo.xHotspot = xHotSpot;
		icnInfo.yHotspot = yHotSpot;
		
		hIcon = ::CreateIconIndirect(&icnInfo);
		
		destroy_BMP(hColor);
		destroy_BMP(hMask);
    }
	
	return (jlong) hIcon; 
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_deleteHIcon
(JNIEnv *env , jobject obj, jlong icon)
{
    DestroyIcon((HICON)icon);
}


JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_createIcon
(JNIEnv *env , jobject obj, jlong icon, jint id, jstring str)
{
    LPTSTR buffer = (LPTSTR) env->GetStringUTFChars(str, NULL);
	TrayMessage(messageWindow,NIM_ADD,id,(HICON)icon, buffer);
	env->ReleaseStringUTFChars(str, buffer);
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_updateNativeIcon
(JNIEnv *env , jobject obj, jlong icon, jint id, jstring str)
{
	LPTSTR buffer = (LPTSTR) env->GetStringUTFChars( str, NULL);
	TrayMessage(messageWindow,NIM_MODIFY,id,(HICON)icon,buffer);
	env->ReleaseStringUTFChars(str, buffer);
}


JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_removeIcon

(JNIEnv *env , jclass klass, jint id)

{
	TrayMessage(messageWindow,NIM_DELETE,id,NULL,NULL);
}



JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinSystemTrayService_eventLoop
(JNIEnv *env, jclass klass) {
	MSG   msg;
	if (!inited) {
		if (!Initialize(env)) {
			return;
		}
	}
	while(GetMessage(&msg, NULL,0, 0))     
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}
	return;
}

