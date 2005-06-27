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
#include <commctrl.h>
#include <shlwapi.h>
#include "WinTrayIconService.h"
#include "DisplayThread.h"
#include "JNIloader.h"

// The following three definition shoud be consistent with class java.awt.Event
#define JAVA_SHIFT_MASK     0x1
#define JAVA_CTRL_MASK      0x2
#define JAVA_ATL_MASK       0x8

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

jmethodID notifyEventMID;
jmethodID restartTaskbarMID;
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
    
    notifyEventMID = env->GetStaticMethodID(peerCls, "notifyEvent", "(IIIII)V");
	restartTaskbarMID = env->GetStaticMethodID(peerCls, "restartTaskbar", "()V");
	
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

static UINT msgRestartTaskbar = 0;
LRESULT CALLBACK WndProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
	if (msgRestartTaskbar == 0 && uMsg == WM_CREATE)
        {
            msgRestartTaskbar = RegisterWindowMessage("TaskbarCreated");
        }
	if (msgRestartTaskbar != 0 && uMsg == msgRestartTaskbar)
        {
            JNIEnv *env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
            env->CallStaticVoidMethod(peerCls,restartTaskbarMID);
            return 1;
        }
	if (uMsg >=  TRAY_NOTIFYICON && lParam != WM_MOUSEMOVE)
        {
            POINT pt;
            ::GetCursorPos(&pt);

            // calculate the modifiers.
            int modifiers = 0;
            SHORT state = GetKeyState(VK_SHIFT);
            modifiers |= state < 0 ? JAVA_SHIFT_MASK : 0;

            state = GetKeyState(VK_CONTROL);
            modifiers |= state < 0 ? JAVA_CTRL_MASK : 0;

            state = GetKeyState(VK_MENU);
            modifiers |= state < 0 ? JAVA_ATL_MASK : 0;

            JNIEnv *env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
            env->CallStaticVoidMethod(peerCls,notifyEventMID, uMsg-TRAY_NOTIFYICON, lParam,pt.x,pt.y, modifiers);
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

/******************************************************************************
 * The following code is used to calculate the Rectangle of trayicon on screen
 ******************************************************************************/
BOOL CALLBACK FindTrayNotifyWnd(HWND hwnd, LPARAM lParam)
{    
	TCHAR szClassName[256];
    GetClassName(hwnd, szClassName, 255); 
	if (strcmp(szClassName, "TrayNotifyWnd") == 0)    
	{        
		HWND* pWnd = (HWND*)lParam;
		*pWnd = hwnd;
        return FALSE;    
	}    
	return TRUE;
}
BOOL CALLBACK FindToolBarInTrayWnd(HWND hwnd, LPARAM lParam)
{    
	TCHAR szClassName[256];
    GetClassName(hwnd, szClassName, 255);
	if (strcmp(szClassName, "ToolbarWindow32") == 0)    
	{        
		HWND* pWnd = (HWND*)lParam;
		*pWnd = hwnd;
        return FALSE;    
	}    
	return TRUE;
}

// return HWND of the window which is used to place the tray icons.
HWND FindTrayWnd(){
	HWND hWndShellTrayWnd = NULL;
	HWND hWndTrayNotifyWnd = NULL;
	HWND hWndTrayIconOwnerWnd = NULL;
	
	// find the window whose class name is "Shell_TrayWnd". --- Taskbar
	hWndShellTrayWnd = FindWindow(("Shell_TrayWnd"), NULL);
	if( hWndShellTrayWnd == NULL )
		return NULL;

	// find the window whose class name is "TrayNotifyWnd" --- Notification Area.
	EnumChildWindows(hWndShellTrayWnd, FindTrayNotifyWnd, (LPARAM)&hWndTrayNotifyWnd); 
	if( !hWndTrayNotifyWnd || !IsWindow(hWndTrayNotifyWnd))
		return hWndShellTrayWnd;

	// find the window whose class name is "ToolbarWindow32" --- The window used to place the tray icons.
	EnumChildWindows(hWndTrayNotifyWnd, FindToolBarInTrayWnd, (LPARAM)&hWndTrayIconOwnerWnd);   
	if( !hWndTrayIconOwnerWnd || !IsWindow(hWndTrayIconOwnerWnd))
		return hWndShellTrayWnd;

	return hWndTrayIconOwnerWnd;
}

BOOL GetTrayIconRect(HWND hWnd, int iconID, RECT *rect){
	HWND hWndTrayWnd = FindTrayWnd();
	if(hWndTrayWnd == NULL)
		return FALSE;

	GetWindowRect(hWndTrayWnd, rect); // Get the Rectangle of the Tray Icon owner window.
	
	// Check how many buttons there are - should be more than 0
	int iButtonsCount = SendMessage(hWndTrayWnd, TB_BUTTONCOUNT, 0, 0);
	// Get an ID of the parent process for system tray
	DWORD dwTrayProcessID = -1;
	GetWindowThreadProcessId(hWndTrayWnd, &dwTrayProcessID);
	if(dwTrayProcessID <= 0)
	{
		return FALSE;
	}
	HANDLE hTrayProc = OpenProcess(PROCESS_ALL_ACCESS, 0, dwTrayProcessID);
	if(hTrayProc == NULL)
	{
		return FALSE;
	}

	LPVOID lpData = VirtualAllocEx(hTrayProc, NULL, sizeof(TBBUTTON), MEM_COMMIT, PAGE_READWRITE);
	if( lpData == NULL || iButtonsCount < 1 )
	{
		CloseHandle(hTrayProc);
		return FALSE;
	}
	
	BOOL bIconFound = FALSE;
	for(int iButton=0; iButton<iButtonsCount; iButton++)
	{
		// Read TBUTTON information about each button in a task bar of tray
		DWORD dwBytesRead = -1;
		TBBUTTON buttonData;
		SendMessage(hWndTrayWnd, TB_GETBUTTON, iButton, (LPARAM)lpData);
		ReadProcessMemory(hTrayProc, lpData, &buttonData, sizeof(TBBUTTON), &dwBytesRead);
		if(dwBytesRead < sizeof(TBBUTTON))
		{
			continue;
		}

		// Read extra data associated with each button: there will be a HWND of the window that created an icon and icon ID
		DWORD dwExtraData[2] = { 0,0 };
		ReadProcessMemory(hTrayProc, (LPVOID)buttonData.dwData, dwExtraData, sizeof(dwExtraData), &dwBytesRead);
		if(dwBytesRead < sizeof(dwExtraData))
		{
			continue;
		}

		HWND hWndOfIconOwner = (HWND) dwExtraData[0];
		int  iIconId		 = (int)  dwExtraData[1];
		
		if(hWndOfIconOwner != hWnd || iIconId != iconID)
		{
			continue;
		}
		
		// Found our icon - in WinXP it could be hidden - let's check it:
		if( buttonData.fsState & TBSTATE_HIDDEN )
		{
			break;
		}

		// Convert the point from the owner window to screen.
		SendMessage(hWndTrayWnd, TB_GETITEMRECT, iButton, (LPARAM)lpData);
		ReadProcessMemory(hTrayProc, lpData, rect, sizeof(RECT), &dwBytesRead);

		if(dwBytesRead < sizeof(RECT))
		{
			continue;
		}

		MapWindowPoints(hWndTrayWnd, NULL, (LPPOINT)rect, 2);
		bIconFound = TRUE;
		break;
	}
	VirtualFreeEx(hTrayProc, lpData, NULL, MEM_RELEASE);
	CloseHandle(hTrayProc);
	return TRUE;
}

DWORD GetDllVersion(LPCTSTR lpszDllName)
{
    HINSTANCE hinstDll;
    DWORD dwVersion = 0;

    hinstDll = LoadLibrary(lpszDllName);
	
    if(hinstDll)
    {
        DLLGETVERSIONPROC pDllGetVersion;
        pDllGetVersion = (DLLGETVERSIONPROC)GetProcAddress(hinstDll, 
                          "DllGetVersion");

        /* Because some DLLs might not implement this function, you
        must test for it explicitly. Depending on the particular 
        DLL, the lack of a DllGetVersion function can be a useful
        indicator of the version. */

        if(pDllGetVersion)
        {
            DLLVERSIONINFO dvi;
            HRESULT hr;

            ZeroMemory(&dvi, sizeof(dvi));
            dvi.cbSize = sizeof(dvi);

            hr = (*pDllGetVersion)(&dvi);

            if(SUCCEEDED(hr))
            {
 				dwVersion = dvi.dwMajorVersion;
            }
        }

        FreeLibrary(hinstDll);
    }
    return dwVersion;
}


/************************************************************************
* WCustomCursor native methods
*/

JNIEXPORT jintArray JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_getRectangleOnScreen
(JNIEnv *env , jobject obj, jint id) 
{
	jintArray result = env->NewIntArray(4);
	
	RECT rect;
	GetTrayIconRect(messageWindow, id, &rect);
	jint rect_int[] = { rect.left, rect.top, rect.right, rect.bottom };
	env->SetIntArrayRegion(result, 0, 4, rect_int);

	return result;
}

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

char* ConvertJByteArray(JNIEnv *env, jbyteArray arr )
{
	int len =  env->GetArrayLength(arr);
	if(len == 0)
		return NULL;
	char *temp = (char *)env->GetByteArrayElements(arr, 0);
	char *buffer = (char *)malloc(len+1);
	strncpy(buffer, temp, len);
	*(buffer+len)='\0';
	env->ReleaseByteArrayElements(arr, (signed char*)temp, 0);
	return buffer;
}
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_createIcon
(JNIEnv *env , jobject obj, jlong icon, jint id, jbyteArray tooltip)
{
	char *buffer = ConvertJByteArray(env, tooltip);
	TrayMessage(messageWindow,NIM_ADD,id,(HICON)icon, buffer);
	free(buffer);
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_updateNativeIcon
(JNIEnv *env , jobject obj, jlong icon, jint id, jbyteArray tooltip)
{
	char *buffer = ConvertJByteArray(env, tooltip);
	TrayMessage(messageWindow,NIM_MODIFY,id,(HICON)icon,buffer);
	free(buffer);
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_showBalloonMessage
(JNIEnv *env , jobject obj, jlong icon, jint id, jbyteArray title, jbyteArray message, jint type)
{
	NOTIFYICONDATA tnd;
	DWORD dll_version = GetDllVersion("Shell32.dll");
	int tnd_size = dll_version >= 5 ? sizeof(tnd) : NOTIFYICONDATA_V1_SIZE;

	::ZeroMemory(&tnd, tnd_size);
	tnd.cbSize		= tnd_size;
	tnd.hWnd		= messageWindow;
	tnd.uID			= id;
	tnd.uFlags		= NIF_INFO;
	switch (type) {
		case 0 :
			tnd.dwInfoFlags = NIIF_INFO;
			break;
		case 1 :
			tnd.dwInfoFlags = NIIF_ERROR;
			break;
		case 2 :
			tnd.dwInfoFlags = NIIF_WARNING;
			break;
		case 3 :
			tnd.dwInfoFlags = NIIF_NONE;
	}
	tnd.hIcon		= (HICON)icon;

	char *buffer = ConvertJByteArray(env, title);
	if (buffer) {
		lstrcpyn(tnd.szInfoTitle, buffer, ARRAYSIZE(tnd.szInfoTitle));
	}
	free(buffer);

	buffer = ConvertJByteArray(env, message);
	if (buffer) {
		lstrcpyn(tnd.szInfo, buffer, ARRAYSIZE(tnd.szInfo));
	}
	free(buffer);

	Shell_NotifyIcon(NIM_MODIFY, &tnd);
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_WinTrayIconService_removeIcon

(JNIEnv *env , jclass klass, jint id) {
	TrayMessage(messageWindow,NIM_DELETE,id,NULL,NULL);
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_DisplayThread_initTray
(JNIEnv *env, jclass klass) {
	if (!inited) {
		if (!Initialize(env)) {
			return;
		}
	inited = 1;
	}
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_DisplayThread_eventLoop
(JNIEnv *env, jclass klass) {
	MSG   msg;
	while(GetMessage(&msg, NULL,0, 0))     
	{
		TranslateMessage(&msg);
		DispatchMessage(&msg);
	}
	return;
}

