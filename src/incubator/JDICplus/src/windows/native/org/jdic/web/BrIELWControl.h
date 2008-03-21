//////////////////////////////////
// Copyright (C) 2008 Sun Microsystems, Inc. All rights reserved. Use is
// subject to license terms.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the Lesser GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA.
#ifndef _BrIELWControl_H_
#define _BrIELWControl_H_

#include "stdafx.h"
#include <comutil.h>
#include <oledlg.h>
#include <mshtml.h>
#include <mshtmdid.h>
#include <exdisp.h>
#include <exdispid.h>
#include <activscp.h>
#include <comdef.h>
#include <comdefsp.h>
#include <math.h>

#include "awt_DCHolder.h"

#define _AWT_OLE_EX_
#include "awt_ole.h"

_COM_SMARTPTR_TYPEDEF(IWebBrowser2, __uuidof(IWebBrowser2));

#define WM_POST_REDRAW (WM_USER + 0x500)

class CBrIELWControl :
    public IOleClientSite,
    public IOleInPlaceSite,
    public IAdviseSink,
    public IDispatch,
    public DWebBrowserEvents2
{
public:
    IWebBrowser2Ptr m_spIWebBrowser2;
    BOOL m_bNativeDraw;

protected:
    HWND m_hwndParent;
    HWND m_hwndIE;
    HWND m_hwndShell;
    RECT m_rcIE2;//rectangle of IE in parent coordinates
    void UpdateWindowRect();

private:
    volatile LONG m_cRef;
    DWORD m_dwAdvised;
    DWORD m_dwWebCPCookie;
    LONG_PTR m_pOldIEWndProc;
    static LPCTSTR ms_lpPN;
    ULONG m_tidUpdate;


public:
    CBrIELWControl();
    ULONG Terminate(); // Disconnect and release
    void LaizyUpdate(DWORD dwDelayInMs = 10);

private:
    HRESULT InplaceActivate(BOOL bActivate = FALSE);
    HRESULT AdviseBrowser(IN BOOL bAdvise);

    BOOL IsSmoothScroll();
    static LRESULT CALLBACK NewIEProcStub(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam);

public:
    HRESULT DestroyControl();
    HRESULT Connect(IN BSTR bsURL);
    HRESULT CreateControl(
        IN HWND     hParent,
        IN LPCRECT  prcIE,
        IN BSTR     bsURL = NULL);
    HRESULT OnPaint(HDC hdcPaint, LPCRECT prcClipParent);
    virtual void RedrawInParent();
    virtual void RedrawParentRect(LPRECT pRect);
    virtual LRESULT NewIEProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam);
    virtual HRESULT SendIEEvent(
        int iId,
        LPTSTR lpName,
        LPTSTR lpValue);

public:
//IUnknown
    STDMETHOD(QueryInterface)(REFIID riid, void **ppvObject)
    {
        *ppvObject = NULL;
        if( IsEqualGUID(riid, IID_IUnknown)  || IsEqualGUID(riid, IID_IOleClientSite) ){
           *ppvObject = (IOleClientSite *)this;
        } else if( IsEqualGUID(riid, IID_IOleWindow) || IsEqualGUID(riid, IID_IOleInPlaceSite) ){
           *ppvObject = (IOleInPlaceSite *)this;
        } else if(IsEqualGUID(riid, IID_IAdviseSink)){
           *ppvObject = (IAdviseSink *)this;
        } else if(IsEqualGUID(riid, IID_IDispatch)){
           *ppvObject = (IDispatch *)this;
        } else
            return E_NOTIMPL;
            //DWebBrowserEvents2 with 34A715A0-6587-11D0-924A-0020AFC7AC4D
            //can be implemented
        AddRef();
        return S_OK;
    }
    STDMETHOD_(ULONG, AddRef)()
    {
        return InterlockedIncrement(&m_cRef);
    }
    STDMETHOD_(ULONG, Release)()
    {
        LONG cRef = InterlockedDecrement(&m_cRef);
        if(0==cRef)
                delete this;
        return cRef;
    }

private:
// IOleClientSite
    STDMETHOD(SaveObject)()
    {
        OLE_TRACENOTIMPL(_T("IOleClientSite::SaveObject"))
    }
    STDMETHOD(GetMoniker)(DWORD dwAssign, DWORD dwWhichMoniker, IMoniker **ppmk)
    {
        OLE_TRACENOTIMPL(_T("IOleClientSite::GetMoniker"))
    }
    STDMETHOD(GetContainer)(IOleContainer **ppContainer)
    {
        OLE_TRACENOTIMPL(_T("IOleClientSite::GetContainer"))
    }
    STDMETHOD(ShowObject)(){
        OLE_TRACENOTIMPL(_T("IOleClientSite::ShowObject"));
    }
    STDMETHOD(OnShowWindow)(BOOL fShow)
    {
        OLE_TRACENOTIMPL(_T("IOleClientSite::OnShowWindow"));
    }
    STDMETHOD(RequestNewObjectLayout)()
    {
        OLE_TRACENOTIMPL(_T("IOleClientSite::RequestNewObjectLayout"));
    }


// IOleWindow
    STDMETHOD(GetWindow)(HWND *phwnd)
    {
        *phwnd = m_hwndParent;
        return S_OK;
    }
    STDMETHOD(ContextSensitiveHelp)(BOOL fEnterMode)
    {
        OLE_TRACENOTIMPL(_T("IOleWindow::CanInPlaceActivate"));
    }

// IOleInPlaceSite
    STDMETHOD(CanInPlaceActivate)()
    {
        return S_OK;
    }
    STDMETHOD(OnInPlaceActivate)()
    {
        return S_OK;
    }
    STDMETHOD(OnUIActivate)()
    {
        OLE_TRACEOK(_T("IOleInPlaceSite::OnUIActivate"));
    }
    STDMETHOD(GetWindowContext)(
        IOleInPlaceFrame **ppFrame,
        IOleInPlaceUIWindow **ppDoc,
        LPRECT lprcPosRect,
        LPRECT lprcClipRect,
        LPOLEINPLACEFRAMEINFO lpFrameInfo)
    {
        STRACE0(_T("IOleInPlaceSite::GetWindowContext"));
        *lprcPosRect = m_rcIE2;
        *lprcClipRect = m_rcIE2;
        if(NULL!=lpFrameInfo){
            lpFrameInfo->cb = sizeof(OLEINPLACEFRAMEINFO);
            lpFrameInfo->fMDIApp = FALSE;
            lpFrameInfo->hwndFrame = m_hwndParent;
        }
        return S_OK;
    }
    STDMETHOD(Scroll)(SIZE scrollExtant)
    {
        OLE_TRACENOTIMPL(_T("IOleInPlaceSite::Scroll"));
    }
    STDMETHOD(OnUIDeactivate)(BOOL fUndoable)
    {
        OLE_TRACEOK(_T("IOleInPlaceSite::OnUIDeactivate"));
    }
    STDMETHOD(OnInPlaceDeactivate)( void)
    {
        return S_OK;
    }
    STDMETHOD(DiscardUndoState)( void)
    {
        OLE_TRACENOTIMPL(_T("IOleInPlaceSite::DiscardUndoState"));
    }
    STDMETHOD(DeactivateAndUndo)( void)
    {
        OLE_TRACENOTIMPL(_T("IOleInPlaceSite::DeactivateAndUndo"));
    }
    STDMETHOD(OnPosRectChange)(LPCRECT lprcPosRect)
    {
        OLE_TRACENOTIMPL(_T("IOleInPlaceSite::OnPosRectChange"));
    }

//IAdviseSink
    virtual void STDMETHODCALLTYPE OnDataChange(
        FORMATETC *pFormatetc,
        STGMEDIUM *pStgmed)
    {
        STRACE0(_T("IAdviseSink::OnDataChange"));
    }

    virtual void STDMETHODCALLTYPE OnViewChange(
        IN DWORD dwAspect,
        IN LONG lindex)
    {
        STRACE0(_T("IAdviseSink::OnDataChange"));
        LaizyUpdate();
    }

    virtual void STDMETHODCALLTYPE OnRename(
        IN IMoniker *pmk)
    {
        STRACE0(_T("IAdviseSink::OnRename"));
    }

    virtual void STDMETHODCALLTYPE OnSave()
    {
        STRACE0(_T("IAdviseSink::OnSave"));
    }

    virtual void STDMETHODCALLTYPE OnClose()
    {
        STRACE0(_T("IAdviseSink::OnClose"));
    }

// IDispatch
    STDMETHOD(GetTypeInfoCount)(UINT* pctinfo)
    {
        return E_NOTIMPL;
    }
    STDMETHOD(GetTypeInfo)(
            UINT iTInfo,
            LCID lcid,
            ITypeInfo** ppTInfo)
    {
        return E_NOTIMPL;
    }
    STDMETHOD(GetIDsOfNames)(
            REFIID riid,
            LPOLESTR* rgszNames,
            UINT cNames,
            LCID lcid,
            DISPID* rgDispId)
    {
        return E_NOTIMPL;
    }

    STDMETHOD(Invoke)(
            DISPID dispIdMember,
            REFIID riid,
            LCID lcid,
            WORD wFlags,
            DISPPARAMS  *pDispParams,
            VARIANT  *pVarResult,
            EXCEPINFO  *pExcepInfo,
            UINT  *puArgErr
    );
};
#endif //_BrIELWControl_H_
