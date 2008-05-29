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
#include "stdafx.h" 
#include <vector>
#include "BrIELWControl.h"       // main symbols
IMPLEMENT_SECURITY();

bool IsShiftKeyDown() { return GetKeyState(VK_SHIFT) < 0; }
bool IsAltKeyDown() { return GetKeyState(VK_MENU) < 0; }
bool IsCtrlKeyDown() { return GetKeyState(VK_CONTROL) < 0; }

RECT g_emptyRect = {0};

LPCTSTR CBrIELWControl::ms_lpPN = _T("awt_hook");
#define TID_LAIZYUPDATE 1234
CBrIELWControl::CBrIELWControl()
{
    m_cRef = 1L;
    m_hwndParent = NULL;
    m_hwndIE = NULL;
    m_hwndShell = NULL;

    m_pOldIEWndProc = NULL;

    m_rcIE2.left = 0;
    m_rcIE2.top = 0;
    m_rcIE2.right = 100;
    m_rcIE2.top = 100;

    m_dwAdvised = OLE_BAD_COOKIE;
    m_dwWebCPCookie = OLE_BAD_COOKIE;

    m_tidUpdate = 0;
    m_bNativeDraw = FALSE;
}

void CBrIELWControl::UpdateWindowRect()                       
{
    HWND hRel = m_hwndIE 
        ? m_hwndIE 
        : m_hwndShell;
    if(hRel && m_hwndParent){
        ::GetClientRect(hRel, &m_rcIE2);
        MapWindowPoints(
            hRel,
            m_hwndParent,
            (LPPOINT)&m_rcIE2,
            2);
        STRACE0(_T("UpdateWindowRect x:%d y:%d w:%d h:%d"),
            m_rcIE2.left,
            m_rcIE2.top,
            m_rcIE2.right - m_rcIE2.left,
            m_rcIE2.bottom - m_rcIE2.top
        );
    }
}

ULONG CBrIELWControl::Terminate() { 
    if(m_spIWebBrowser2){
        OLE_TRY
        OLE_HRT( DestroyControl() )
        OLE_CATCH
    }
    return Release();
}   

void CBrIELWControl::LaizyUpdate(DWORD dwDelayInMs){
    if(m_hwndIE){
        if(TID_LAIZYUPDATE == m_tidUpdate)
            ::KillTimer(m_hwndIE, m_tidUpdate);
        m_tidUpdate = ::SetTimer(m_hwndIE, TID_LAIZYUPDATE, dwDelayInMs, NULL);
    }
} 

HRESULT CBrIELWControl::InplaceActivate(BOOL bActivate)
{
    OLE_TRY                                    
    IOleObjectPtr spOleObject(m_spIWebBrowser2);
    OLE_CHECK_NOTNULLSP(spOleObject)

    if(!bActivate){
        OLE_HRT( spOleObject->Close(OLECLOSE_NOSAVE) )
    }
    OLE_HRT( spOleObject->SetClientSite( bActivate ? (IOleClientSite *)this : NULL ) )
    if( bActivate ){
        OLE_HRT( spOleObject->Advise((IAdviseSink *)this, &m_dwAdvised) )
    } else if(OLE_BAD_COOKIE != m_dwAdvised){
        OLE_HRT( spOleObject->Unadvise(m_dwAdvised) )
        m_dwAdvised = OLE_BAD_COOKIE;
    }

    IViewObjectPtr spViewObject(m_spIWebBrowser2);
    OLE_CHECK_NOTNULLSP(spViewObject)

    OLE_HRT( spViewObject->SetAdvise(
        DVASPECT_CONTENT,
        0,
        bActivate ? (IAdviseSink *)this : NULL))
    
    if( bActivate ){
        UpdateWindowRect();
        MSG msg; //fake param
        OLE_HRT( spOleObject->DoVerb(
            OLEIVERB_INPLACEACTIVATE, 
            &msg, 
            this, 
            0, 
            m_hwndParent, 
            &m_rcIE2))
/*
        OLE_HRT( spOleObject->DoVerb(
            OLEIVERB_HIDE, 
            &msg, 
            this, 
            0, 
            m_hwndParent, 
            &m_rcIE2))
*/
    }
    OLE_CATCH
    OLE_RETURN_HR
}

HRESULT CBrIELWControl::AdviseBrowser(IN BOOL bAdvise)
{
    OLE_TRY 
    IConnectionPointContainerPtr spConnectionPointContainer(m_spIWebBrowser2);
    OLE_CHECK_NOTNULLSP(spConnectionPointContainer)

    IConnectionPointPtr spConnectionPoint;
    OLE_HRT( spConnectionPointContainer->FindConnectionPoint(
	DIID_DWebBrowserEvents2,
	&spConnectionPoint))
    OLE_CHECK_NOTNULLSP(spConnectionPoint)

    if(bAdvise){
	OLE_HRT( spConnectionPoint->Advise((IDispatch *)this, &m_dwWebCPCookie) )
    }else{
        OLE_HRT( spConnectionPoint->Unadvise(m_dwWebCPCookie) )
        m_dwWebCPCookie = OLE_BAD_COOKIE;
    }
    OLE_CATCH
    OLE_RETURN_HR
}


HRESULT CBrIELWControl::DestroyControl()
{
    if(m_hwndIE){
        ::SetWindowLongPtr(m_hwndIE, GWLP_WNDPROC, m_pOldIEWndProc);
    }
    m_hwndIE = NULL;
    m_hwndShell = NULL;
    OLE_TRY 
    OLE_HRT( InplaceActivate(FALSE) )       
    OLE_HRT( AdviseBrowser(FALSE) )
    OLE_CATCH
    m_dwAdvised = OLE_BAD_COOKIE;
    m_dwWebCPCookie = OLE_BAD_COOKIE;
    m_spIWebBrowser2 = NULL;
    m_ccIWebBrowser2 = NULL;

    OLE_RETURN_HR
}

HRESULT CBrIELWControl::Connect(IN BSTR bsURL)
{
    OLE_TRY 
    static _bstr_t bsAboutBlank(L"about:blank");
    OLE_HRT( m_spIWebBrowser2->Navigate(
        bsURL ? bsURL : ((BSTR)bsAboutBlank),
        _PO, _PO, _PO, _PO))
    OLE_CATCH
    OLE_RETURN_HR
}

HRESULT CBrIELWControl::CreateControl(
    IN HWND     hParent,
    IN LPCRECT  prcIE,
    IN BSTR     bsURL)
{
    OLE_TRY 
    OLE_CHECK_NOTNULL(hParent)
    OLE_CHECK_NOTNULL(prcIE)
    m_hwndParent = hParent;
    m_rcIE2 = *prcIE;

    if(m_spIWebBrowser2){
        OLE_HRT( DestroyControl() )
    }                            

    OLE_HRT( CoCreateInstance(
        CLSID_WebBrowser, 
        NULL, 
        CLSCTX_INPROC, 
        IID_IWebBrowser2, 
        (void**)&m_spIWebBrowser2))
    OLE_CHECK_NOTNULLSP(m_spIWebBrowser2)        

    m_ccIWebBrowser2 = m_spIWebBrowser2;
    OLE_HRT( AdviseBrowser(TRUE) )
    OLE_HRT( InplaceActivate(TRUE) )       

    OLE_HRT( Connect(bsURL) )   

    IOleWindowPtr pOleWindow(m_spIWebBrowser2);
    OLE_CHECK_NOTNULLSP(pOleWindow)        
    OLE_HRT(pOleWindow->GetWindow(&m_hwndShell));

    OLE_CATCH
    OLE_RETURN_HR
}


HRESULT CBrIELWControl::OnPaint(HDC hdc, LPCRECT prcClip/*InParent*/)
{
    OLE_TRY
    const RECT &rc = *prcClip;
    IViewObjectPtr spViewObject(m_spIWebBrowser2);
    OLE_CHECK_NOTNULLSP(spViewObject)

    SEP(_T("Draw"));
    RECTL rcIE = {
        0, 0, 
        m_rcIE2.right - m_rcIE2.left, m_rcIE2.bottom - m_rcIE2.top
    };
    RECTL rcIEClip = {
        rc.left - m_rcIE2.left, rc.top - m_rcIE2.top, 
        rc.right - m_rcIE2.left, rc.bottom - m_rcIE2.top
    };

    CDCHolder shCopy;
    shCopy.Create(hdc, rcIE.right, rcIE.bottom, FALSE);
    //STRACE1(_T("shCopy.Create done"));
    OLE_HRT( spViewObject->Draw(
        DVASPECT_CONTENT,
        -1, 
        NULL, 
        NULL,
        NULL, 
        shCopy, 
        &rcIE, 
        &rcIEClip,
        NULL, 
        0
    ));
    OLE_HRW32_BOOL( ::BitBlt(
        hdc, 
        rc.left,
        rc.top,
        rc.right - rc.left,
        rc.bottom - rc.top,
        shCopy,
        rcIEClip.left,
        rcIEClip.top,
        SRCCOPY ));                      
    OLE_CATCH
    OLE_RETURN_HR
}


BOOL CBrIELWControl::IsSmoothScroll()
{
    HWND hScroll = ::GetCapture();
    if(hScroll){
#define _MAX_CLASS 64
        TCHAR szClassName[_MAX_CLASS] = {0};
        hScroll = (
            _MAX_CLASS > GetClassName(
                hScroll,
                szClassName,
                _MAX_CLASS - 1
            ) 
            && 0==_tcscmp(szClassName, _T("Internet Explorer_Hidden"))
        )
            ? hScroll
            : NULL;
    }
    return (BOOL)hScroll; 
}

LRESULT CBrIELWControl::NewIEProcStub(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    CBrIELWControl *pThis = (CBrIELWControl *)::GetProp(hWnd, ms_lpPN);
    return pThis
        ? pThis->NewIEProc(hWnd, msg, wParam, lParam)
        : 0;
}                                         

void CBrIELWControl::RedrawParentRect(LPRECT pRect)
{
    InvalidateRect(m_hwndParent, pRect, FALSE);
}

void CBrIELWControl::RedrawInParent()
{
    OLE_TRY
    UpdateWindowRect();
    MSG msg; //fake param
    OLE_HRT( IOleObjectPtr(m_spIWebBrowser2)->DoVerb(
        OLEIVERB_INPLACEACTIVATE, 
        &msg, 
        this, 
        0, 
        m_hwndParent, 
        &m_rcIE2))
    OLE_CATCH
}

LRESULT CBrIELWControl::NewIEProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    STRACE0(_T("msg:%08x hWnd:%08x"), msg, hWnd);
    if(!m_bNativeDraw && (WM_PAINT==msg || WM_NCPAINT==msg || WM_SYNCPAINT==msg) ){
        if( !IsSmoothScroll() ){
            UpdateWindowRect();       
            STRACE(_T("_PAINT %08x"), msg);
            RECT rc;
            if( !GetUpdateRect(hWnd, &rc, FALSE) ){
                GetClientRect(hWnd, &rc);
            } else {
                ValidateRect(hWnd, &rc);
            }

#ifdef _DEBUG
            {
                RECT rc = {0};
                int res = GetWindowRgnBox(m_hwndIE, &rc);
                //if( NULLREGION!=res && ERROR!=res )
                {
                    STRACE1(_T("Clip(x:%d y:%d w:%d h:%d)"), rc.left, rc.top, rc.right - rc.left, rc.bottom - rc.top);
                    STRACE1(_T("IE(x:%d y:%d w:%d h:%d)"), m_rcIE2.left, m_rcIE2.top, m_rcIE2.right - m_rcIE2.left, m_rcIE2.bottom - m_rcIE2.top);
                }
            }
#endif //_DEBUG

            if(0!=m_tidUpdate){
                ::KillTimer(m_hwndIE, m_tidUpdate);
                m_tidUpdate = 0;
                rc = m_rcIE2;
            } else {
                rc.top += m_rcIE2.top;
                rc.bottom += m_rcIE2.top;                                  
                rc.left += m_rcIE2.left;
                rc.right += m_rcIE2.left;
            }

            if(rc.top != rc.bottom && rc.left!=rc.right){
                RedrawParentRect(&rc);
                return 0;
            } 

            //seems empty redraw is a signal...
            return ::CallWindowProc((WNDPROC)m_pOldIEWndProc, hWnd, msg, wParam, lParam);      
        } else {
            STRACE(_T("_SPAINT %08x"), msg);
            LaizyUpdate();
        }
    } else if(WM_GETDLGCODE == msg){
        return DLGC_WANTALLKEYS 
            | DLGC_WANTARROWS
            | DLGC_HASSETSEL
            | DLGC_WANTCHARS
            | DLGC_WANTTAB;
    } else if(WM_TIMER == msg ){
        if(wParam == m_tidUpdate){
            if( IsSmoothScroll() ){
                LaizyUpdate();
            } else {
                UpdateWindowRect();
                SEP(_T("_InvalidateRect"));
                ::KillTimer(m_hwndIE, m_tidUpdate);
                m_tidUpdate = 0;
                RedrawParentRect(&m_rcIE2);
            }
            return 0;
        }
    } else if(WM_PARENTNOTIFY == msg && WM_CREATE==wParam){
        STRACE1(_T("Created Child hWnd:%08x"), hWnd);
    }

    LONG_PTR pHook = NULL;
    if(WM_NCDESTROY==msg){
        ::SetWindowLongPtr(hWnd, GWLP_WNDPROC, m_pOldIEWndProc);        
        ::RemoveProp(hWnd, ms_lpPN);
        m_pOldIEWndProc = NULL;

        m_hwndIE = NULL;        
        STRACE1(_T("}hook"));
    } else if(
        (msg >= WM_KEYFIRST 
        && msg <= WM_KEYLAST)
    ){
        MSG _msg = { hWnd, msg, wParam, lParam, 0, { 0, 0 } };
        LPMSG lpMsg = &_msg;
        IOleInPlaceActiveObjectPtr spInPlaceActiveObject(m_spIWebBrowser2);
        if(spInPlaceActiveObject){
            OLE_DECL;
            OLE_HR = spInPlaceActiveObject->TranslateAccelerator(lpMsg);
            if(WM_KEYDOWN == msg && VK_TAB==wParam && !IsCtrlKeyDown() && !IsAltKeyDown()){
                STRACE1(_T("WM_KEYDOWN tab %08x %08x"), lParam, OLE_HR);
                if(S_FALSE==OLE_HR){
                    SendIEEvent(
                        -2,
                        _T("FocusMove"),
                        IsShiftKeyDown() ? _T("false") : _T("true")
                    );
                }    
            }    
            if(OLE_HR == S_OK){
                return 0;
            }
        }
        pHook = ::SetWindowLongPtr(hWnd, GWLP_WNDPROC, m_pOldIEWndProc);        
    }

    LRESULT res = ::CallWindowProc((WNDPROC)m_pOldIEWndProc, hWnd, msg, wParam, lParam);      
    if(pHook && m_hwndIE==hWnd) //window can be disconnected ;)
        ::SetWindowLongPtr(hWnd, GWLP_WNDPROC, pHook);

    return res;
}

HRESULT CBrIELWControl::SendIEEvent(
    int iId,
    LPTSTR lpName, 
    LPTSTR lpValue,
    _bstr_t &bsResult)
{
    return S_OK;
}

// IDispatch
_bstr_t CreateParamString(DISPPARAMS  *pDispParams)
{
    _bstr_t ret(_T(""));
    for(UINT i = 0; i < pDispParams->cArgs; ++i )
    {
        if(0!=i)
            ret += _T(",");
        OLE_TRY
        ret += _bstr_t( _variant_t(pDispParams->rgvarg[i], true) );
        OLE_CATCH
        if(FAILED(OLE_HR)){
            ret += _T("<unknown>");
        }
    }
    return ret;
}

struct IE_EVENT {
   DISPID   dispid;
   LPCTSTR  lpEventName;
   static int __cdecl compare(const IE_EVENT *p1, const IE_EVENT *p2){
       return p1->dispid - p2->dispid;
   }
   static int sort(IE_EVENT *base, size_t count){
        qsort( 
            (void *)base, 
            count, 
            sizeof(IE_EVENT), 
            (int (*)(const void*, const void*))compare 
        );
        return 0;
   }
   static IE_EVENT *find(DISPID dispid, IE_EVENT *base, size_t count){
        IE_EVENT ev = {dispid, NULL};
        return (IE_EVENT *)bsearch( 
            (void *)&ev, 
            (void *)base, 
            count, 
            sizeof(IE_EVENT), 
            (int (*)(const void*, const void*))compare 
        );
   }
};

IE_EVENT g_aevSupported[] = {
    {DISPID_BEFORENAVIGATE                , _T("beforeNavigate")}, // this is sent before navigation to give a chance to abort
    {DISPID_NAVIGATECOMPLETE              , _T("navigateComplete")}, // in async, this is sent when we have enough to show
    {DISPID_STATUSTEXTCHANGE              , _T("statusTextChange")}, 
    {DISPID_QUIT                          , _T("quit")},
    {DISPID_DOWNLOADCOMPLETE              , _T("downloadComplete")},
    {DISPID_COMMANDSTATECHANGE            , _T("commandStateChange")},
    {DISPID_DOWNLOADBEGIN                 , _T("downloadBegin")},
    {DISPID_NEWWINDOW                     , _T("newWindow")},// sent when a new window should be created
    {DISPID_PROGRESSCHANGE                , _T("progressChange")},// sent when download progress is updated
    {DISPID_WINDOWMOVE                    , _T("windowMove")},// sent when main window has been moved
    {DISPID_WINDOWRESIZE                  , _T("windowResize")},// sent when main window has been sized
    {DISPID_WINDOWACTIVATE                , _T("windowActivate")},// sent when main window has been activated
    {DISPID_PROPERTYCHANGE                , _T("propertyChange")},// sent when the PutProperty method is called
    {DISPID_TITLECHANGE                   , _T("titleChange")},// sent when the document title changes
    {DISPID_TITLEICONCHANGE               , _T("titleIconChange")},// sent when the top level window icon may have changed.
                                          
    {DISPID_FRAMEBEFORENAVIGATE           , _T("frameBeforeNavigate")},  
    {DISPID_FRAMENAVIGATECOMPLETE         , _T("frameNavigateComplete")},
    {DISPID_FRAMENEWWINDOW                , _T("frameNewWindow")},
                                         
    {DISPID_BEFORENAVIGATE2               , _T("beforeNavigate2")},// hyperlink clicked on
    {DISPID_NEWWINDOW2                    , _T("newWindow2")},
    {DISPID_NAVIGATECOMPLETE2             , _T("navigateComplete2")},// UIActivate new document
    {DISPID_ONQUIT                        , _T("onQuit")},
    {DISPID_ONVISIBLE                     , _T("onVisible")},// sent when the window goes visible/hidden
    {DISPID_ONTOOLBAR                     , _T("onToolbar")},// sent when the toolbar should be shown/hidden
    {DISPID_ONMENUBAR                     , _T("ONMENUBAR")},// sent when the menubar should be shown/hidden
    {DISPID_ONSTATUSBAR                   , _T("ONSTATUSBAR")},// sent when the statusbar should be shown/hidden
    {DISPID_ONFULLSCREEN                  , _T("ONFULLSCREEN")},// sent when kiosk mode should be on/off
    {DISPID_DOCUMENTCOMPLETE              , _T("DOCUMENTCOMPLETE")},// new document goes ReadyState_Complete
    {DISPID_ONTHEATERMODE                 , _T("ONTHEATERMODE")},// sent when theater mode should be on/off
    {DISPID_ONADDRESSBAR                  , _T("ONADDRESSBAR")},// sent when the address bar should be shown/hidden
    {DISPID_WINDOWSETRESIZABLE            , _T("WINDOWSETRESIZABLE")},// sent to set the style of the host window frame
    {DISPID_WINDOWCLOSING                 , _T("WINDOWCLOSING")},// sent before script window.close closes the window 
    {DISPID_WINDOWSETLEFT                 , _T("WINDOWSETLEFT")},// sent when the put_left method is called on the WebOC
    {DISPID_WINDOWSETTOP                  , _T("WINDOWSETTOP")},// sent when the put_top method is called on the WebOC
    {DISPID_WINDOWSETWIDTH                , _T("WINDOWSETWIDTH")},// sent when the put_width method is called on the WebOC
    {DISPID_WINDOWSETHEIGHT               , _T("WINDOWSETHEIGHT")},// sent when the put_height method is called on the WebOC 
    {DISPID_CLIENTTOHOSTWINDOW            , _T("CLIENTTOHOSTWINDOW")},// sent during window.open to request conversion of dimensions
    {DISPID_SETSECURELOCKICON             , _T("SETSECURELOCKICON")},// sent to suggest the appropriate security icon to show
    {DISPID_FILEDOWNLOAD                  , _T("FILEDOWNLOAD")},// Fired to indicate the File Download dialog is opening
    {DISPID_NAVIGATEERROR                 , _T("NAVIGATEERROR")},// Fired to indicate the a binding error has occured
    {DISPID_PRIVACYIMPACTEDSTATECHANGE    , _T("PRIVACYIMPACTEDSTATECHANGE")},// Fired when the user's browsing experience is impacted
// Printing events
    {DISPID_PRINTTEMPLATEINSTANTIATION    , _T("PRINTTEMPLATEINSTANTIATION")},// Fired to indicate that a print template is instantiated
    {DISPID_PRINTTEMPLATETEARDOWN         , _T("PRINTTEMPLATETEARDOWN")},// Fired to indicate that a print templete is completely gone 
    {DISPID_UPDATEPAGESTATUS              , _T("UPDATEPAGESTATUS")},// Fired to indicate that the spooling status has changed
};

std::vector<_bstr_t> split(
    LPCOLESTR pstr, 
    LPCOLESTR delim = L",",
    bool empties = true)
{
    std::vector<_bstr_t> results;
    LPOLESTR r = NULL;
    r = wcsstr(pstr, delim);
    int dlen = wcslen(delim);
    while( NULL != r ){
        LPOLESTR cp = new OLECHAR[(r-pstr)+1];
        memcpy( cp, pstr, (r-pstr)*sizeof(OLECHAR) );
        cp[(r-pstr)] = L'\0';
        if( 0 < wcslen(cp) || empties ){
            results.push_back(cp);
        }
        delete[] cp;
        pstr = r + dlen;
        r = wcsstr(pstr, delim);
    }
    if( 0 < wcslen(pstr) || empties ){
        results.push_back(pstr);
    }
    return results;
}

HRESULT CBrIELWControl::Invoke(
        DISPID dispIdMember,
        REFIID riid,
        LCID lcid,
        WORD wFlags,
        DISPPARAMS  *pDispParams,
        VARIANT  *pVarResult,
        EXCEPINFO  *pExcepInfo,
        UINT  *puArgErr
){
    STRACE0(_T("DISPID_: %08x %d"), dispIdMember, dispIdMember);

    OLE_TRY
    BOOL bNotify = TRUE;
    switch(dispIdMember){
    case DISPID_HTMLWINDOWEVENTS_ONERROR:
        //STRACE0(_T("DISPID_NAVIGATECOMPLETE2"));
        ::MessageBoxA(NULL, "Error", "Error", MB_OK);
        break;
    case DISPID_NAVIGATECOMPLETE2:
    	STRACE0(_T("DISPID_NAVIGATECOMPLETE2"));
        //SendIEEvent(dispIdMember, _T("navigateComplete2"), _T("")/* CreateParamString(pDispParams)*/);
        {               
            UpdateWindowRect();
            STRACE0(_T("hwndShell:%08x"), m_hwndShell);
            if(m_hwndShell){
                HWND hwndFrame = ::GetWindow(m_hwndShell, GW_CHILD); 
                if(hwndFrame){
                    m_hwndIE = ::GetWindow(hwndFrame, GW_CHILD); 
                    WNDPROC pOldIEWndProc = (WNDPROC)::GetWindowLongPtr(m_hwndIE, GWLP_WNDPROC);
                    if(NewIEProcStub!=pOldIEWndProc){
                        STRACE1(_T("{hook"));
                        ::SetProp(m_hwndIE, ms_lpPN, this);
                        m_pOldIEWndProc = ::SetWindowLongPtr(m_hwndIE, GWLP_WNDPROC, (LONG_PTR)NewIEProcStub);
                        SendIEEvent(
                            -3, 
                            _T(""), 
                            _T("")
                        );
                    }
                }
            }            
            RedrawInParent();
        }
        break;
    case DISPID_PROGRESSCHANGE:
        break;
    case DISPID_HTMLDOCUMENTEVENTS_ONREADYSTATECHANGE:
        STRACE1(_T("document ready!"));
	break;
    case DISPID_WINDOWCLOSING:

#define INDEX_WINDOWCLOSING_bIsChildWindow 1
#define INDEX_WINDOWCLOSING_pbCancel       0
        
        *(pDispParams->rgvarg[INDEX_WINDOWCLOSING_pbCancel].pboolVal) =    
            VARIANT_TRUE==pDispParams->rgvarg[INDEX_WINDOWCLOSING_bIsChildWindow].boolVal
                ? VARIANT_FALSE
                : VARIANT_TRUE;

        OLE_HR = S_OK; 
        break;
    case DISPID_AMBIENT_DLCONTROL:
	// respond to this ambient to indicate that we only want to
	// download the page, but we don't want to run scripts,
	// Java applets, or ActiveX controls
        if(FALSE){
	    V_VT(pVarResult) = VT_I4;
	    V_I4(pVarResult) =
		DLCTL_DOWNLOADONLY |
		DLCTL_NO_JAVA |
		DLCTL_NO_SCRIPTS |
		DLCTL_NO_DLACTIVEXCTLS |
		DLCTL_NO_RUNACTIVEXCTLS;
            return S_OK;
        }
        return DISP_E_MEMBERNOTFOUND;
    default:
        OLE_HR = DISP_E_MEMBERNOTFOUND;
        break;
    }

    if(bNotify){
        //just ones and only if it needs
        static int __sortEvents__ = IE_EVENT::sort( 
            g_aevSupported, 
            sizeof(g_aevSupported)/sizeof(*g_aevSupported) 
        );

        IE_EVENT *pEvent = IE_EVENT::find( 
            dispIdMember,
            g_aevSupported, 
            sizeof(g_aevSupported)/sizeof(*g_aevSupported) 
        );

        if(NULL!=pEvent){
            SEP(_T("SendIEEvent"));
            _bstr_t bsRes;
            SendIEEvent(
                pEvent->dispid, 
                _bstr_t(pEvent->lpEventName), 
                CreateParamString(pDispParams),
                bsRes
            );
            
            switch(dispIdMember){
            case DISPID_NEWWINDOW2:
#define INDEX_NEWWINDOW2_ppDisp         1
#define INDEX_NEWWINDOW2_pbCancel       0
                if( 0 < bsRes.length() ){
                    SEP(_T("new window"));
                    std::vector<_bstr_t> par = split(bsRes);

                    if( INDEX_NEWWINDOW2_pbCancel < par.size() )
                        *(pDispParams->rgvarg[INDEX_NEWWINDOW2_pbCancel].pboolVal) = 
                            _V(par[INDEX_NEWWINDOW2_pbCancel]);

                    if( INDEX_NEWWINDOW2_ppDisp < par.size() ){
                        CBrIELWControl *pThis = (CBrIELWControl *)((LONG)_V(par[INDEX_NEWWINDOW2_ppDisp]));
                        if(NULL!=pThis && bool(pThis->m_spIWebBrowser2)){
                            *(pDispParams->rgvarg[INDEX_NEWWINDOW2_ppDisp].ppdispVal) = 
                                pThis->m_ccIWebBrowser2.GetThreadInstance();
                                //pThis->m_spIWebBrowser2;
                                //pThis->m_spIWebBrowser2->AddRef();
                        } else {
                            STRACE1(_T("bad new window"));
                        }
                    }

                    OLE_HR = S_OK; 
                }
                break;
            }
        }
    }
    OLE_CATCH
    OLE_RETURN_HR
}


////////////////////////
// struct CDCHolder

CDCHolder::CDCHolder()
: m_hMemoryDC(NULL),
    m_iWidth(0),
    m_iHeight(0),
    m_bForImage(FALSE),
    m_hBitmap(NULL),
    m_hOldBitmap(NULL),
    m_hAlphaBitmap(NULL),
    m_pPoints(NULL)
{}

void CDCHolder::Create(
    HDC hRelDC,
    int iWidth,
    int iHeght,
    BOOL bForImage
){
    OLE_DECL
/*
    if( bForImage && (iWidth & 0x03) ){
        iWidth &= ~0x03;
        iWidth += 0x04;
    }
*/
    m_iWidth = iWidth;
    m_iHeight = iHeght;
    m_bForImage = bForImage;
    m_hMemoryDC = ::CreateCompatibleDC(hRelDC);
    //NB: can not throw an error in non-safe stack!!! Just conversion and logging!
    //OLE_WINERROR2HR just set OLE_HR without any throw! 
    if( !m_hMemoryDC ){
        OLE_THROW_LASTERROR(_T("CreateCompatibleDC"))
    } 
    m_hBitmap = m_bForImage
        ? CreateJavaContextBitmap(hRelDC, m_iWidth, m_iHeight, &m_pPoints)
        : ::CreateCompatibleBitmap(hRelDC, m_iWidth, m_iHeight);
    if( !m_hBitmap ){
        OLE_THROW_LASTERROR(_T("CreateCompatibleBitmap"))
    }
    m_hOldBitmap = (HBITMAP)::SelectObject(m_hMemoryDC, m_hBitmap);
    if( !m_hOldBitmap ){
        OLE_THROW_LASTERROR(_T("SelectBMObject"))
    } 
}

CDCHolder::~CDCHolder(){
    if(m_hOldBitmap)
        ::SelectObject(m_hMemoryDC, m_hOldBitmap);
    if(m_hBitmap)
        ::DeleteObject(m_hBitmap);
    if(m_hAlphaBitmap)
        ::DeleteObject(m_hAlphaBitmap);
    if(m_hMemoryDC)
        ::DeleteDC(m_hMemoryDC);
};

//BITMAPINFO extended with 
typedef struct tagBITMAPINFOEX  {
    union{
        BITMAPV4HEADER    bmiV4Header;
        BITMAPINFOHEADER  bmiHeader;
    };
    DWORD bmiColors[4];
}   BITMAPINFOEX, *LPBITMAPINFOEX;

BOOL IsOSAlphaSupport(){
    DWORD dwVer = ::GetVersion();
    DWORD isNT = !(dwVer & 0x80000000);
    return !(!isNT && HIBYTE(LOWORD(dwVer)) < 10) //not before WIN 98
        && !(isNT && LOBYTE(LOWORD(dwVer)) < 5);  //not before WIN 2000
}

static BOOL g_bOSAlphaSupport = IsOSAlphaSupport();

void CDCHolder::CreateAlphaImageIfCan()
{
    if(NULL==m_pPoints || !g_bOSAlphaSupport)
        return;

    if( NULL!=m_hOldBitmap ){
        m_hBitmap = (HBITMAP)::SelectObject(m_hMemoryDC, m_hOldBitmap);
        m_hOldBitmap = NULL;
    } 

    BITMAPINFOEX    bmi = {0};
    bmi.bmiV4Header.bV4Width = m_iWidth;
    bmi.bmiV4Header.bV4Height = -m_iHeight;
    bmi.bmiV4Header.bV4Planes = 1;
    bmi.bmiV4Header.bV4BitCount = 32;

    bmi.bmiV4Header.bV4Size = sizeof(BITMAPV4HEADER);
    bmi.bmiV4Header.bV4V4Compression = BI_BITFIELDS;
    bmi.bmiV4Header.bV4XPelsPerMeter = 72;
    bmi.bmiV4Header.bV4YPelsPerMeter = 72;
    bmi.bmiV4Header.bV4RedMask   = 0x00FF0000;
    bmi.bmiV4Header.bV4GreenMask = 0x0000FF00;
    bmi.bmiV4Header.bV4BlueMask  = 0x000000FF;
    bmi.bmiV4Header.bV4AlphaMask = 0xFF000000;

    OLE_TRY
    m_hAlphaBitmap = CreateDIBitmap(
        m_hMemoryDC, 
        (BITMAPINFOHEADER*)&bmi,
        CBM_INIT,
        (void *)m_pPoints,
        (BITMAPINFO*)&bmi,
        DIB_RGB_COLORS);
    if( !m_hAlphaBitmap ){
        OLE_THROW_LASTERROR(_T("CreateDIBitmap"))
    } 
    OLE_CATCH
}

HBITMAP CDCHolder::CreateJavaContextBitmap(
    HDC hdc,
    int iWidth,
    int iHeight,
    void **ppPoints)
{
    BITMAPINFO    bitmapInfo = {0};
    bitmapInfo.bmiHeader.biWidth = iWidth;
    bitmapInfo.bmiHeader.biHeight = -iHeight;
    bitmapInfo.bmiHeader.biPlanes = 1;
    bitmapInfo.bmiHeader.biBitCount = 32;
    bitmapInfo.bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
    bitmapInfo.bmiHeader.biCompression = BI_RGB;

    return ::CreateDIBSection(
        hdc,
        (BITMAPINFO *)&bitmapInfo,
        DIB_RGB_COLORS,
        (void **)ppPoints,
        NULL,
        0
    );
}
