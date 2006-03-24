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
#include "BrowserWindow.h"
#include "Message.h"

BrowserWindow::BrowserWindow() 
{
    initialized = FALSE;
    m_InstanceID = -1;

    glpDisp = NULL;
    isDefaultPage = true;
}

BrowserWindow::~BrowserWindow() 
{
}

void BrowserWindow::CreateChildBrowserWindow(IDispatch **ppDisp)
{
    HWND hWndClient = 0;

    RECT rect;
	LogMsg("CreateChildBrowserWindow: Default client rect will be used");
	//SetRect(&rect, 0, 0, 400, 400);//bad here
	GetClientRect(&rect);//use parent's size
	
    
    CBrowserFrameWindow *pChild = new CBrowserFrameWindow;
         
    
    pChild->Create(NULL, rect, _T("about:blank") );
    pChild->ShowWindow(SW_SHOW);   
     
    pChild->m_pWB->put_RegisterAsBrowser(VARIANT_TRUE);
    HRESULT hr = pChild->m_pWB->get_Application(ppDisp);
}

BOOL BrowserWindow::PreTranslateMessage(MSG* pMsg)
{
    // Pass keyboard messages along to the child window that has the focus.
    // When the browser has the focus, the message is passed to its containing
    // CAxWindow, which lets the control handle the message.

    if ((pMsg->message < WM_KEYFIRST || pMsg->message > WM_KEYLAST) &&
        (pMsg->message < WM_MOUSEFIRST || pMsg->message > WM_MOUSELAST))
        return FALSE;

    if (IsChild(::GetFocus())) {
        // give control a chance to translate this message
        return ::SendMessage(m_hWnd, WM_FORWARDMSG, 0, (LPARAM)pMsg);
    }

    return FALSE;
}

void BrowserWindow::SetReady(int pInsNum)
{
    initialized = true;
    m_InstanceID = pInsNum;
}

void __stdcall BrowserWindow::OnCommandStateChange(long lCommand, VARIANT_BOOL bEnable)
{
    char buf[20];
    switch (lCommand)
    {
    case CSC_NAVIGATEFORWARD:
        sprintf(buf, "forward=%d", bEnable ? 1 : 0);
        break;
    case CSC_NAVIGATEBACK:
        sprintf(buf, "back=%d", bEnable ? 1 : 0);
        break;
    default:
        return;
    }
    SendSocketMessage(m_InstanceID, CEVENT_COMMAND_STATE_CHANGE, buf);
}

void __stdcall BrowserWindow::OnBeforeNavigate(IDispatch *pDisp,VARIANT *URL,
    VARIANT *Flags,VARIANT *TargetFrameName,VARIANT *PostData,VARIANT *Headers,
    VARIANT_BOOL *Cancel)
{
    char url[1024];
    int len = wcslen(URL->bstrVal);
    WideCharToMultiByte(CP_ACP, 0, URL->bstrVal, -1, url, sizeof(url) - 1, NULL, NULL);

    int bCmdCanceled = -1, waitCount = 0;
    AddTrigger(m_InstanceID, CEVENT_BEFORE_NAVIGATE, &bCmdCanceled); 
    SendSocketMessage(m_InstanceID, CEVENT_BEFORE_NAVIGATE, url);

    while (bCmdCanceled < 0 && waitCount++ < MAX_WAIT) {
        Sleep(1);
    } 

    if (bCmdCanceled == 1) {
        *Cancel = VARIANT_TRUE;
    }
    else {
        *Cancel = VARIANT_FALSE;
    } 
}
//new added by michael
void __stdcall BrowserWindow::OnNewWindow3(IDispatch **ppDisp,VARIANT_BOOL *Cancel,DWORD dwFlags,BSTR bstrUrlContext,
		BSTR bstrUrl)
{	
	int bCmdCanceled = -1, waitCount = 0;
    AddTrigger(m_InstanceID, CEVENT_BEFORE_NEWWINDOW, &bCmdCanceled); 

	char buf[1024];
    int len = wcslen(bstrUrl);
	if (len > 0) {
		if (WideCharToMultiByte(CP_ACP, 0, bstrUrl, -1, buf, sizeof(buf) - 1, NULL, NULL) > 0){
			LogMsg("A new window will be opened with URL:");
			LogMsg(buf);
			SendSocketMessage(m_InstanceID, CEVENT_BEFORE_NEWWINDOW,buf);
		}			
	}    

    while (bCmdCanceled < 0 && waitCount++ < MAX_WAIT) {
        Sleep(1);
    } 

    if (bCmdCanceled == 1 || waitCount >= MAX_WAIT) {
		LogMsg("New window is suppressed.");
        *Cancel = VARIANT_TRUE;
    }
    else {
        *Cancel = VARIANT_FALSE;
		LogMsg("New window is permitted to open.");
		CreateChildBrowserWindow(ppDisp);
    }
}

/*
void __stdcall BrowserWindow::OnNewWindow2(IDispatch **ppDisp,VARIANT_BOOL *Cancel)
{
	int bCmdCanceled = -1, waitCount = 0;
    AddTrigger(m_InstanceID, CEVENT_BEFORE_NEWWINDOW, &bCmdCanceled); 
    SendSocketMessage(m_InstanceID, CEVENT_BEFORE_NEWWINDOW);


    while (bCmdCanceled < 0 && waitCount++ < MAX_WAIT) {
        Sleep(1);
    } 

    if (bCmdCanceled == 1) {
        *Cancel = VARIANT_TRUE;
    }
    else {
        *Cancel = VARIANT_FALSE;
    } 
}
*/

void __stdcall BrowserWindow::OnDownloadBegin()
{
    SendSocketMessage(m_InstanceID, CEVENT_DOWNLOAD_STARTED);
}

void __stdcall BrowserWindow::OnNavigateComplete(IDispatch* pDisp, VARIANT* URL)
{
    // If glpDisp is NULL, this is the top level OnNavigateComplete event,
    // save pDisp to check the final OnDocumentComplete event.
    if (!glpDisp) {
        glpDisp = pDisp;
    }
    
    SendSocketMessage(m_InstanceID, CEVENT_DOWNLOAD_COMPLETED);
}

void __stdcall BrowserWindow::OnNavigateProgress(long Progress,long ProgressMax)
{
    SendSocketMessage(m_InstanceID, CEVENT_DOWNLOAD_PROGRESS);
}

void __stdcall BrowserWindow::OnNavigateError(IDispatch *pDisp,VARIANT *URL,VARIANT *TargetFrameName,
                                              VARIANT *StatusCode,VARIANT_BOOL *Cancel)
{
    SendSocketMessage(m_InstanceID, CEVENT_DOWNLOAD_ERROR);
}

void __stdcall BrowserWindow::OnDocumentComplete(IDispatch* pDisp, VARIANT* URL)
{
    // In pages where multiple frames are loaded, this event fires for each 
    // frame. The highest level frame fires the final event. At this time, 
    // the pDisp parameter will be the same as the IDispatch interface pointer 
    // of the highest level frame.
    if (glpDisp && glpDisp == pDisp) {
        glpDisp = NULL;

        // The default, predefined page for creating the initial browser window
        // will fire this event, ignore it. 
        if (isDefaultPage) {
            isDefaultPage = false;
        } else {   
            SendSocketMessage(m_InstanceID, CEVENT_DOCUMENT_COMPLETED);
        }
    }
}

void __stdcall BrowserWindow::OnTitleChange(BSTR Text)
{
    char buf[1024];
    int len = wcslen(Text);
	if (len > 0) {
		if (WideCharToMultiByte(CP_ACP, 0, Text, -1, buf, sizeof(buf) - 1, NULL, NULL) > 0)
			SendSocketMessage(m_InstanceID, CEVENT_TITLE_CHANGE, buf);
	}
}

void __stdcall BrowserWindow::OnStatusTextChange(BSTR Text)
{
    char buf[1024];
    int len = wcslen(Text);
	if (len > 0) {
		if (WideCharToMultiByte(CP_ACP, 0, Text, -1, buf, sizeof(buf) - 1, NULL, NULL) > 0)
			SendSocketMessage(m_InstanceID, CEVENT_STATUSTEXT_CHANGE, buf);
	}
}
