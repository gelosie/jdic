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
#include "BrowserFrameWindow.h"

void __stdcall CBrowserFrameWindow::OnWindowSetHeight( long Height)    
{    
RECT r;
GetClientRect(&r);
m_pWB->get_Height(&Height);
r.bottom = r.top +  Height;

SetWindowPos(NULL, &r,
    SWP_NOZORDER | SWP_NOACTIVATE );  

}

void __stdcall CBrowserFrameWindow::OnWindowSetWidth( long Width)
{
RECT r;
            
GetClientRect(&r);

m_pWB->get_Width(&Width);
r.right = r.left + Width;  

SetWindowPos(NULL, &r,
    SWP_NOZORDER | SWP_NOACTIVATE ); 
}

void __stdcall CBrowserFrameWindow::OnWindowSetLeft( long Left)
{

RECT r;


GetClientRect(&r);


long Top;
long Width;
long Height;

m_pWB->get_Width(&Width);
m_pWB->get_Height(&Height);
m_pWB->get_Top(&Top);

r.top = Top;
r.left = Left;
r.bottom = Top + Height;
r.right = Left + Width;
SetWindowPos(NULL, &r,
    SWP_SHOWWINDOW | SWP_NOCOPYBITS ); 

RECT r2;
r2.top = 0;
r2.bottom = Height;
r2.left = 0;
r2.right = Width;
m_wndChild.SetWindowPos(NULL, &r2,
    SWP_SHOWWINDOW | SWP_NOCOPYBITS );

InvalidateRect(&r,true);
UpdateWindow();
}

void __stdcall CBrowserFrameWindow::OnWindowSetTop( long Top)
{

RECT r;
GetClientRect(&r);    
}

long __stdcall CBrowserFrameWindow::OnWindowClosing()
{

::CloseWindow(*this);
::DestroyWindow(*this);

return S_OK;
}

LRESULT CBrowserFrameWindow::OnSize(UINT, WPARAM, LPARAM, BOOL&)
{
RECT r;
GetClientRect(&r);

m_wndChild.SetWindowPos(NULL, &r,
    SWP_NOZORDER | SWP_NOACTIVATE );

return 0;
}

LRESULT CBrowserFrameWindow::OnCreate(UINT, WPARAM, LPARAM, BOOL&)
{
LogMsg("CBrowserFrameWindow::OnCreate");
RECT rect;
GetClientRect(&rect);

RECT rect2;
rect2 = rect;    

CAxWindow axwnd;

LogMsg("CBrowserFrameWindow:Will Create a child Window its parent is:");
LogIntMsg((int)m_hWnd);
// Create a child window.
// AtlAx functions will subclass this window.
m_wndChild.Create(m_hWnd, rect2, NULL, WS_CHILD | WS_VISIBLE | WS_BORDER, 0, 1);
// Attach the child window to the CAxWindow so we can access the 
// host that subclasses the child window.
axwnd.Attach(m_wndChild);

// Create a Axhost directly as the child of the main window
//     axwnd.Create(m_hWnd, rect2, NULL, WS_CHILD | WS_VISIBLE | WS_BORDER, 0, 1);

if (axwnd.m_hWnd != NULL)
{
    CComPtr<IUnknown> spControl;
    
    
    HRESULT hr = AtlAxCreateControl(
        OLESTR("Shell.Explorer.2"), 
        m_wndChild.m_hWnd , 
        NULL, 
        NULL
        );
    
    
    // have to obtain an interface to the control and set 
    // up the sink
    if (SUCCEEDED(hr))
    {
        hr = axwnd.QueryControl(&spControl);
        if (SUCCEEDED(hr))
        {
            // Sink events form the control
            DispEventAdvise(spControl, &__uuidof(DWebBrowserEvents2));
        }
    }
    
    
    if (SUCCEEDED(hr))
    {
        // Use the returned IUnknown pointer.
        hr = spControl.QueryInterface(&m_pWB);
    }
}

return 0;
}

LRESULT CBrowserFrameWindow::OnDestroy(UINT, WPARAM, LPARAM, BOOL&)
{
//m_pWB->put_RegisterAsBrowser(VARIANT_FALSE);
DispEventAdvise(m_pWB, &__uuidof(DWebBrowserEvents2));
return 0;
}