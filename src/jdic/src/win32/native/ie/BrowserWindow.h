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

#include <Atlwin.h> // for AtlWin
#include <Exdisp.h>  //for IWebBrowser2
#include <exdispid.h>
#include <atlhost.h>
#include "MsgServer.h"
#include "Util.h"
#include "BrowserFrameWindow.h"


class BrowserWindow:
    public CAxWindow,
    public IDispEventImpl<1, BrowserWindow, &DIID_DWebBrowserEvents2, &LIBID_SHDocVw, 1, 0>
{
public:
    BrowserWindow();
    virtual ~BrowserWindow();

	
    CComPtr<IWebBrowser2> m_pWB;
    CComPtr<IHTMLDocument2> m_pHD2;
    CComPtr<IHTMLDocument3> m_pHD3;
    CComPtr<IHTMLWindow2> m_pHW;

    BOOL PreTranslateMessage(MSG* pMsg);

    void SetReady(int pInsNum);

    void __stdcall OnCommandStateChange(long lCommand, VARIANT_BOOL bEnable);
    void __stdcall OnBeforeNavigate(IDispatch *pDisp,VARIANT *URL,
                                    VARIANT *Flags,VARIANT *TargetFrameName,VARIANT *PostData,VARIANT *Headers,
                                    VARIANT_BOOL *Cancel);    
	void __stdcall OnNewWindow3(IDispatch **ppDisp,VARIANT_BOOL *Cancel,DWORD dwFlags,BSTR bstrUrlContext,
		BSTR bstrUrl);
    void __stdcall OnDownloadBegin();
    void __stdcall OnNavigateComplete(IDispatch* pDisp, VARIANT* URL);
    void __stdcall OnDocumentComplete(IDispatch* pDisp, VARIANT* URL);
    void __stdcall OnNavigateProgress(long Progress,long ProgressMax);
    void __stdcall OnNavigateError(IDispatch *pDisp,VARIANT *URL,VARIANT *TargetFrameName,
                                   VARIANT *StatusCode,VARIANT_BOOL *Cancel);
	void __stdcall OnTitleChange(BSTR Text);
	void __stdcall OnStatusTextChange(BSTR Text);
	void CreateChildBrowserWindow(IDispatch **ppDisp);

    BEGIN_SINK_MAP(BrowserWindow)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_COMMANDSTATECHANGE, OnCommandStateChange)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_DOWNLOADBEGIN, OnDownloadBegin)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_BEFORENAVIGATE2, OnBeforeNavigate)       
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_NEWWINDOW3, OnNewWindow3)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_NAVIGATECOMPLETE2, OnNavigateComplete)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_DOCUMENTCOMPLETE, OnDocumentComplete)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_PROGRESSCHANGE, OnNavigateProgress)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_TITLECHANGE, OnTitleChange)
        SINK_ENTRY_EX(1, DIID_DWebBrowserEvents2, DISPID_STATUSTEXTCHANGE, OnStatusTextChange)
    END_SINK_MAP()

protected:
    int  m_InstanceID;
    bool initialized;

    // Below variables are used to check the final OnDocumentComplete event.
    // Global IDispatch pointer.
    IDispatch* glpDisp;
    // Flag for the default, preloaded page ("about:blank") while 
    // creating a browser window for JEVENT_CREATEWINDOW event.
    bool isDefaultPage;
};

#endif // browserwindow_h
