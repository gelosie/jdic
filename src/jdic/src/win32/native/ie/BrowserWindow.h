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

#ifndef browserwindow_h
#define browserwindow_h

#include <Exdisp.h>
#include <Atlwin.h> // for AtlWin
#include <Exdisp.h>  //for IWebBrowser2
#include <exdispid.h>
#include <atlhost.h>
#include "MsgServer.h"

class BrowserWindow:
    public CAxWindow,
    public IDispEventImpl<1, BrowserWindow, &DIID_DWebBrowserEvents2,&LIBID_SHDocVw, 1, 0>
{
public:
    BrowserWindow();
    virtual ~BrowserWindow();

    CComPtr<IWebBrowser2> m_pWB;

    BOOL PreTranslateMessage(MSG* pMsg);

    void SetReady(int pInsNum);

    void __stdcall OnCommandStateChange(long lCommand, VARIANT_BOOL bEnable);
    void __stdcall OnBeforeNavigate(IDispatch *pDisp,VARIANT *URL,
                                    VARIANT *Flags,VARIANT *TargetFrameName,VARIANT *PostData,VARIANT *Headers,
                                    VARIANT_BOOL *Cancel);
    void __stdcall OnNewWindow2(IDispatch **ppDisp,VARIANT_BOOL *Cancel);
    void __stdcall OnDownloadBegin();
    void __stdcall OnNavigateComplete(IDispatch* pDisp, CComVariant& URL);
    void __stdcall OnNavigateProgress(long Progress,long ProgressMax);
    void __stdcall OnNavigateError(IDispatch *pDisp,VARIANT *URL,VARIANT *TargetFrameName,
                                   VARIANT *StatusCode,VARIANT_BOOL *Cancel);

    BEGIN_SINK_MAP(BrowserWindow)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_COMMANDSTATECHANGE, OnCommandStateChange)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_DOWNLOADBEGIN, OnDownloadBegin)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_BEFORENAVIGATE2, OnBeforeNavigate)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_NEWWINDOW2, OnNewWindow2)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_NAVIGATECOMPLETE2, OnNavigateComplete)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_PROGRESSCHANGE, OnNavigateProgress)
        //SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_NAVIGATEERROR, OnNavigateError)
    END_SINK_MAP()

protected:
    int  m_InstanceID;
    bool initialized;
};

#endif // browserwindow_h
