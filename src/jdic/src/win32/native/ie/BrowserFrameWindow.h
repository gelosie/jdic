/*
 Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 subject to license terms.
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the Lesser GNU General Public License as
 published by the Free Software Foundation; either version 2 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 USA.
*/

#ifndef BROWSERFRAMEWINDOW_H
#define BROWSERFRAMEWINDOW_H
#endif

#include "stdafx.h"
#include <atlbase.h>
#include <atlwin.h>
#include <atlhost.h> 

#include <Exdisp.h>
#include <Atlwin.h> // for AtlWin
#include <Exdisp.h>  //for IWebBrowser2
#include <exdispid.h>
#include <atlhost.h>

#include "Util.h"
//#pragma warning(disable : 4192)
//#pragma warning(disable : 4146)
	
//#import  <mshtml.tlb> 
//#import  <shdocvw.dll> 

// Child window class that will be subclassed for hosting Active X control
class CChildWindow : public CWindowImpl<CChildWindow>
{
public:
    BEGIN_MSG_MAP(CChildWindow)
    END_MSG_MAP()
};

// Helper Macro for Main Frame window class
#define DECLARE_MAINFRAME_WND_CLASS(WndClassName, style, bkgnd, menuid) \
    static ATL::CWndClassInfo& GetWndClassInfo() \
{ \
    static ATL::CWndClassInfo wc = \
{ \
  { sizeof(WNDCLASSEX), style, StartWindowProc, \
    0, 0, NULL, NULL, NULL, (HBRUSH)(bkgnd + 1), menuid, WndClassName, NULL }, \
    NULL, NULL, IDC_ARROW, TRUE, 0, _T("") \
 }; \
    return wc; \
}

class CBrowserFrameWindow : public CWindowImpl<CBrowserFrameWindow, CWindow, CFrameWinTraits>,
public IDispEventImpl<1, CBrowserFrameWindow, &__uuidof(DWebBrowserEvents2), &LIBID_SHDocVw, 1, 0>
{
public :
    
    CChildWindow m_wndChild;
    
    CComPtr<IWebBrowser2> m_pWB;
    DECLARE_MAINFRAME_WND_CLASS("MainWindow", CS_HREDRAW | CS_VREDRAW | CS_DBLCLKS, COLOR_WINDOW, 0)
        
        BEGIN_MSG_MAP(CBrowserFrameWindow)
        MESSAGE_HANDLER(WM_CREATE, OnCreate)
        MESSAGE_HANDLER(WM_DESTROY, OnDestroy)
        MESSAGE_HANDLER(WM_SIZE, OnSize)
        END_MSG_MAP()
        
        BEGIN_SINK_MAP(CBrowserFrameWindow)
        SINK_ENTRY_EX(1, __uuidof(DWebBrowserEvents2), DISPID_WINDOWCLOSING, OnWindowClosing)
        SINK_ENTRY_EX(1, __uuidof(DWebBrowserEvents2), DISPID_WINDOWSETHEIGHT, OnWindowSetHeight)
        SINK_ENTRY_EX(1, __uuidof(DWebBrowserEvents2), DISPID_WINDOWSETTOP, OnWindowSetTop)
        SINK_ENTRY_EX(1, __uuidof(DWebBrowserEvents2), DISPID_WINDOWSETLEFT, OnWindowSetLeft)
        SINK_ENTRY_EX(1, __uuidof(DWebBrowserEvents2), DISPID_WINDOWSETWIDTH, OnWindowSetWidth)
        END_SINK_MAP()
        
    
    void __stdcall OnWindowSetHeight(long Height);

    void __stdcall OnWindowSetWidth(long Width);
    
    void __stdcall OnWindowSetLeft(long Left);

    void __stdcall OnWindowSetTop(long Top);


    STDMETHOD(OnWindowClosing)();

    
    LRESULT OnSize(UINT, WPARAM, LPARAM, BOOL&);

    
    LRESULT OnCreate(UINT, WPARAM, LPARAM, BOOL&);
    
    
    LRESULT OnDestroy(UINT, WPARAM, LPARAM, BOOL&);

};