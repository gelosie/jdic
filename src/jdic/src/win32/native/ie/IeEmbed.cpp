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
#include "MsgServer.h"
#include "BrowserWindow.h"
#include "resource.h"
#include "Message.h"
#include "prthread.h"
#include "Util.h"

#define WM_SOCKET_MSG   WM_USER + 1

CComModule _Module;

HWND gMainWnd;

//array of browserwindow
WBArray ABrowserWnd;

void SocketMsgHandler(const char *pMsg)
{
    char *msg = new char[strlen(pMsg) + 1];
    strcpy(msg, pMsg);
    ::PostMessage(gMainWnd, WM_SOCKET_MSG, 0, (long)msg);
}

BOOL IsDlgDisplayed() 
{
    HKEY hkey;
    DWORD type, cb;
    char *p, value[256] = "\0";
    
    // get IE's setting of "Error Dlg Displayed On Every Error"
    if (RegOpenKey(HKEY_CURRENT_USER, "Software\\Microsoft\\Internet Explorer\\Main", &hkey) != ERROR_SUCCESS)
        WBTRACE("Failed to query dialog display setting from IE!");
    
    cb = 256;
    if (RegQueryValueEx(hkey, "Error Dlg Displayed On Every Error", 0, &type, (LPBYTE)value, &cb) != ERROR_SUCCESS)
        WBTRACE("Failed to query dialog display setting from IE!");
    
    RegCloseKey(hkey);
   
    p = strstr(strlwr(value), "yes");
    return (p != NULL); 	
}

void CommandProc(char * pInputChar)
{
    BrowserWindow * pBrowserWnd;
    HRESULT hRes;
    int instanceNum;
    int eventID;
    char eventMessage[1024];

    int i = sscanf(pInputChar, "%d,%d,%s", &instanceNum, &eventID, &eventMessage);
    delete pInputChar;
    if (i < 2) return;

    switch (eventID)
    {
    case JEVENT_INIT:
        break;

    case JEVENT_CREATEWINDOW:
        {
        // only create new browser window when the instance does not exist
        if (instanceNum < ABrowserWnd.GetSize() && (BrowserWindow *) ABrowserWnd[instanceNum] != NULL)
			break;

        RECT rect;
        if (i != 3) 
			break;
		HWND hWnd = (HWND) atoi(eventMessage);
        pBrowserWnd = new BrowserWindow();
        if (!pBrowserWnd) 
			break;

        SetRect(&rect,200, 200, 800, 600);
        HWND hWndClient = pBrowserWnd->Create(hWnd,rect,
                _T("about:blank"),
                WS_CHILD | WS_VISIBLE | WS_VSCROLL | WS_HSCROLL | WS_CLIPSIBLINGS | WS_CLIPCHILDREN,
                WS_EX_CLIENTEDGE);

        hRes = pBrowserWnd->QueryControl(&(pBrowserWnd->m_pWB));
        if (pBrowserWnd->m_pWB == NULL) {
            WBTRACE("Failed to query pBrowserWnd->m_pWB!");
            break;
        }
        hRes = pBrowserWnd->DispEventAdvise(pBrowserWnd->m_pWB);

        pBrowserWnd->SetReady(instanceNum);
        SendSocketMessage(instanceNum, CEVENT_INIT_WINDOW_SUCC);
        
        //set silent mode 
        if (!IsDlgDisplayed()) 
            pBrowserWnd->m_pWB->put_Silent(VARIANT_TRUE);

        //save the pointer of BrowserWnd to array
        ABrowserWnd.SetAtGrow(instanceNum, pBrowserWnd);
        //show window
        ShowWindow(hWndClient, SW_SHOW);
        UpdateWindow(hWndClient);
        SetFocus(hWndClient);
        }
        break;

    case JEVENT_DESTROYWINDOW:
        pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
        hRes = pBrowserWnd->DispEventUnadvise(pBrowserWnd->m_pWB);
        break;

    case JEVENT_SHUTDOWN:
        ::PostMessage(gMainWnd, WM_QUIT, 0, 0);
        break;

    case JEVENT_SET_BOUNDS:
        {
        if (i != 3) break;
        int x, y, w, h;
        i = sscanf(eventMessage, "%d,%d,%d,%d", &x, &y, &w, &h);
        if (i == 4) {
            pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
            ATLASSERT(pBrowserWnd != NULL);
            pBrowserWnd->SetWindowPos(NULL, x, y, w, h, SWP_NOZORDER);
        }
        }
        break;

    case JEVENT_NAVIGATE:
        pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
        ATLASSERT(pBrowserWnd != NULL);
        pBrowserWnd->m_pWB->Navigate(CComBSTR(eventMessage), NULL, NULL, NULL, NULL);
        break;

    case JEVENT_GOBACK:
        pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
        ATLASSERT(pBrowserWnd != NULL);
        pBrowserWnd->m_pWB->GoBack();
        break;

    case JEVENT_GOFORWARD:
        pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
        ATLASSERT(pBrowserWnd != NULL);
        pBrowserWnd->m_pWB->GoForward();
        break;

    case JEVENT_REFRESH:
        pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
        ATLASSERT(pBrowserWnd != NULL);
        pBrowserWnd->m_pWB->Refresh();
        break;

    case JEVENT_STOP:
        pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
        ATLASSERT(pBrowserWnd != NULL);
        pBrowserWnd->m_pWB->Stop();
        break;

    case JEVENT_GETURL:
        USES_CONVERSION;
        BSTR bsUrl;
        pBrowserWnd = (BrowserWindow *) ABrowserWnd[instanceNum];
        ATLASSERT(pBrowserWnd != NULL);
        pBrowserWnd->m_pWB->get_LocationURL(&bsUrl);
        SendSocketMessage(instanceNum, CEVENT_RETURN_URL, W2A(bsUrl));
        SysFreeString(bsUrl);
        break;
    }
}


//this function is passed as parameter to struct WNDCLASSEX 
LRESULT CALLBACK MainWindowProc(HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)
{
    switch (uMsg)
    {
    case WM_SOCKET_MSG:
        CommandProc ((char*)lParam);
        return 0;
    }
    return DefWindowProc(hWnd, uMsg, wParam, lParam);
}


//this function is called in CreateHiddenWnd()
void RegisterWindowClass()
{
    WNDCLASSEX  wcx;

    ZeroMemory(&wcx, sizeof(WNDCLASSEX));
    wcx.cbSize = sizeof(WNDCLASSEX);
    wcx.lpfnWndProc = MainWindowProc;
    wcx.hInstance = GetModuleHandle(NULL);
    wcx.hIcon = NULL; // LoadIcon(wcx.hInstance, MAKEINTRESOURCE(IDR_MAIN));
    wcx.hCursor = LoadCursor(NULL, IDC_ARROW);
    wcx.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wcx.lpszClassName = "SampleWindowClass";
    wcx.lpszMenuName = NULL; //MAKEINTRESOURCE(IDR_MAIN);
    RegisterClassEx(&wcx);
}

void CreateHiddenWnd() 
{
    RegisterWindowClass();
    gMainWnd = CreateWindowEx(WS_EX_APPWINDOW,
        "SampleWindowClass", _T("Browser App"), WS_DISABLED,
        -10010, -10010, -10000, -10000, NULL, NULL, 0, NULL);
}

/////////////////////////////////////////////////////////////////////////////
//
extern "C" int WINAPI _tWinMain(HINSTANCE hInstance,
    HINSTANCE /*hPrevInstance*/, LPTSTR lpCmdLine, int /*nShowCmd*/)
{
    if (strstr(lpCmdLine, "-port=")) {
        int port = atoi(&lpCmdLine[6]);
        gMessenger.SetPort(port);
        gMessenger.CreateServerSocket();
    }
    if (gMessenger.IsFailed()) {
        fprintf(stderr, "Failed to create server socket\n");
        return -1;
    }

    lpCmdLine = GetCommandLine(); //this line necessary for _ATL_MIN_CRT
    //Initializes the COM library for use
#if _WIN32_WINNT >= 0x0400 & defined(_ATL_FREE_THREADED)
    HRESULT hRes = CoInitializeEx(NULL, COINIT_MULTITHREADED);
#else
    HRESULT hRes = CoInitialize(NULL);
#endif

    ATLASSERT(SUCCEEDED(hRes));
    _Module.Init(NULL, hInstance, &LIBID_SHDocVw);

    //create hidden window for purpose of handling messages
    CreateHiddenWnd();

    //init new thread for communication
    PRThread *socketListenThread = NULL;
    socketListenThread = PR_CreateThread(PR_USER_THREAD,
                                        PortListening,
                                        SocketMsgHandler,
                                        PR_PRIORITY_NORMAL,
                                        PR_GLOBAL_THREAD,
                                        PR_UNJOINABLE_THREAD,
                                        0);
    if (!socketListenThread) {
        SendSocketMessage(-1, CEVENT_INIT_FAILED);
        goto exit;
    }

    //init atl
    AtlAxWinInit();

    //process message
    MSG msg;
    BOOL bRet;
    while ((bRet = GetMessage(&msg, NULL, 0, 0)) != 0)
    { 
        if (bRet == -1)
        {
            // handle the error and possibly exit
        }
        else
        {
            int size = ABrowserWnd.GetSize();
            for (int i = 0; i < size; i++) {
                BrowserWindow *pBrowserWnd = (BrowserWindow *)ABrowserWnd[i];
                if (pBrowserWnd && pBrowserWnd->PreTranslateMessage(&msg)) {
                    break;
                }
            }
            if (i == size) {
                TranslateMessage(&msg); 
                DispatchMessage(&msg); 
            }
        }
    }
    
exit:
    _Module.Term();
    CoUninitialize();
    return 0;
}
