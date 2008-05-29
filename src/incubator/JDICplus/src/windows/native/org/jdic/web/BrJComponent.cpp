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
#include "BrJComponent.h"
#include "BrMain.h"
#include "BrHolderThread.h"
#include "windowsx.h"


//struct __declspec(uuid("BB1A2AE1-A4F9-11CF-8F20-00805F2CD064")) IActiveScript;
_COM_SMARTPTR_TYPEDEF(IActiveScript, __uuidof(IActiveScript));

//struct __declspec(uuid("BB1A2AE2-A4F9-11CF-8F20-00805F2CD064")) IActiveScriptParse;
_COM_SMARTPTR_TYPEDEF(IActiveScriptParse, __uuidof(IActiveScriptParse));


#if defined(__IHTMLDocument2_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IHTMLDocument2, __uuidof(IHTMLDocument2));
#endif// #if defined(__IHTMLDocument2_INTERFACE_DEFINED__)
#if defined(__IHTMLDocument3_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IHTMLDocument3, __uuidof(IHTMLDocument3));
#endif// #if defined(__IHTMLDocument3_INTERFACE_DEFINED__)
#if defined(__IDisplayServices_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IDisplayServices, __uuidof(IDisplayServices));
#endif// #if defined(__IDisplayServices_INTERFACE_DEFINED__)
#if defined(__IHTMLCaret_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IHTMLCaret, __uuidof(IHTMLCaret));
#endif// #if defined(__IHTMLCaret_INTERFACE_DEFINED__)
#if defined(__IHTMLWindow2_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IHTMLWindow2, __uuidof(IHTMLWindow2));
#endif// #if defined(__IHTMLWindow2_INTERFACE_DEFINED__)
#if defined(__IHTMLElement_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IHTMLElement, __uuidof(IHTMLElement));
#endif// #if defined(__IHTMLElement_INTERFACE_DEFINED__)
/*
#if defined(__IActiveScript_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IActiveScript, __uuidof(IActiveScript));
#endif// #if defined(__IActiveScript_INTERFACE_DEFINED__)
#if defined(__IActiveScriptError_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IActiveScriptError, __uuidof(IActiveScriptError));
#endif// #if defined(__IActiveScriptError_INTERFACE_DEFINED__)
*/


#if defined(__IActiveScriptParseProcedure_INTERFACE_DEFINED__)
_COM_SMARTPTR_TYPEDEF(IActiveScriptParseProcedure, __uuidof(IActiveScriptParseProcedure));
#endif// #if defined(__IActiveScriptParseProcedure_INTERFACE_DEFINED__)


jclass    BrJComponent::ms_jcidBrComponent = NULL;
jfieldID  BrJComponent::ms_jcidWBrComponent_x = NULL;
jfieldID  BrJComponent::ms_jcidWBrComponent_y = NULL;
jfieldID  BrJComponent::ms_jcidWBrComponent_width = NULL;
jfieldID  BrJComponent::ms_jcidWBrComponent_height = NULL;
jmethodID BrJComponent::ms_jcidWBrComponent_getCursor = NULL;

jfieldID  BrJComponent::ms_jcidWBrComponentPeer_data = NULL;
jfieldID  BrJComponent::ms_jcidWBrComponentPeer_target = NULL;
jmethodID BrJComponent::ms_jcidWBrComponentPeer_postEvent = NULL;
jmethodID BrJComponent::ms_jcidWBrComponentPeer_handlePaint = NULL;

jclass    BrJComponent::ms_jcidCursor = NULL;
jfieldID  BrJComponent::ms_jcidCursor_pData = NULL;

void BrJComponent::initIDs(JNIEnv *env, jclass clazz)
{
    ms_jcidBrComponent = getGlobalJavaClazz(
        env,
        "org/jdic/web/BrComponent"
    );
    ms_jcidWBrComponent_x = env->GetFieldID(ms_jcidBrComponent, "x", "I");
    ms_jcidWBrComponent_y = env->GetFieldID(ms_jcidBrComponent, "y", "I");
    ms_jcidWBrComponent_width = env->GetFieldID(ms_jcidBrComponent, "width", "I");
    ms_jcidWBrComponent_height = env->GetFieldID(ms_jcidBrComponent, "height", "I");
    ms_jcidWBrComponent_getCursor = env->GetMethodID(
        ms_jcidBrComponent, 
        "getCursor", 
        "()Ljava/awt/Cursor;");


    ms_jcidCursor = getGlobalJavaClazz(
        env,
        "java/awt/Cursor"
    );
    ms_jcidCursor_pData = env->GetFieldID(ms_jcidCursor, "pData", "J");

    ms_jcidWBrComponentPeer_postEvent = env->GetMethodID(
        clazz, 
        "postEvent", 
        "(ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    ms_jcidWBrComponentPeer_handlePaint = env->GetMethodID(
        clazz, 
        "handlePaint", 
        "(IIII)V");
    ms_jcidWBrComponentPeer_data = env->GetFieldID(clazz, "data", "J");
    ms_jcidWBrComponentPeer_target = env->GetFieldID(clazz, "target", "Lorg/jdic/web/BrComponent;");
}


/************************************************************************
 * BrJComponent methods
 */

BrJComponent::BrJComponent(
    JNIEnv *env, 
    jobject othis
)
:m_synthetic(false),
 m_bBlockNativeInputHandler(false),
 m_this(makeGlobal(env, othis)),
 m_hChildArea(CreateRectRgn(0,0,0,0)),
 m_bTransparent(false),
 m_pThread(BrowserThread::GetInstance())
{}

HRESULT BrJComponent::getTargetRect(
    JNIEnv *env, 
    LPRECT prc)
{
    jobject target = env->GetObjectField(m_this, ms_jcidWBrComponentPeer_target);
    if(NULL!=target){
        prc->left = env->GetIntField(target, ms_jcidWBrComponent_x);
        prc->top = env->GetIntField(target, ms_jcidWBrComponent_y);
        prc->right = prc->left + env->GetIntField(target, ms_jcidWBrComponent_width);
        prc->bottom = prc->top + env->GetIntField(target, ms_jcidWBrComponent_height);
        env->DeleteLocalRef(target);
        return S_OK;
    }
    return E_INVALIDARG;
}

HRESULT BrJComponent::create(    
    JNIEnv *env, 
    HWND    hParent)
{
    SEP(_T("create"))
    OLE_TRY
    RECT rcIE = {0};
    SetWindowLong(
        hParent, 
        GWL_STYLE, 
        GetWindowLong(hParent, GWL_STYLE) & ~(WS_CLIPCHILDREN | WS_CLIPSIBLINGS) );

    OLE_HRT( getTargetRect(
        env, 
        &rcIE))
    OLE_HRT( CreateControl(
        hParent,
        &rcIE,
        NULL))

    HWND hFO = GetFocus();
    if( GetTopWnd()==hFO || GetIEWnd()==hFO ){
        SetFocus(GetParent());
    }
    m_bTransparent = false;
    setTransparent(true);
    OLE_CATCH
    OLE_RETURN_HR
}

BrJComponent::~BrJComponent()
{
    SEP0(_T("~BrJComponent"));
    if(!bool(m_spIWebBrowser2) || NULL!=m_this){
        STRACE1(_T("alarm!"));
    }
    if(m_pThread){
        m_pThread->Release();
    }
}

void BrJComponent::destroy(JNIEnv *env)
{
    if(m_spIWebBrowser2){
        OLE_TRY
        OLE_HRT( DestroyControl() )
        OLE_CATCH
    }
    releaseGlobal(env, m_this);
    if(m_hChildArea){
        DeleteObject((HGDIOBJ)m_hChildArea);
        m_hChildArea = NULL;
    }
    m_this = NULL;
}

void BrJComponent::updateTransparentMask(RECT *prc)
{
    if( !m_bTransparent ){
        HRGN hPaintRgn = NULL;
        RECT rc;
        BOOL bRepaint = TRUE;
        if(NULL==prc){
            ::GetClientRect(GetTopWnd(), &rc);
            prc = &rc;
            //SetWindowRgn(GetParent(), NULL, FALSE);
        }
        if(NULL!=m_hChildArea){
            //STRACE1(_T("CreateRectRgn(%d, %d, %d, %d)"), prc->left, prc->top, prc->right, prc->bottom);
            hPaintRgn = CreateRectRgn(prc->left, prc->top, prc->right, prc->bottom); 
            if( NULL!=hPaintRgn && NULL!=m_hChildArea){
                CombineRgn(
                    hPaintRgn,
                    hPaintRgn,
                    m_hChildArea,
                    RGN_XOR);
            }
        }        
        SetWindowRgn(
            GetTopWnd(), 
            hPaintRgn, 
            FALSE);
    }
}

void BrJComponent::setTransparent(boolean bTransparent)
{
    if( m_bTransparent == (int)bTransparent )
        return;
    m_bTransparent = bTransparent;
    if(m_bTransparent){
        SetWindowRgn(
            GetTopWnd(), 
            CreateRectRgn(0, 0, 0, 0), 
            FALSE);
        STRACE0(_T("[]->."));
    } else {
        updateTransparentMask(NULL);
        STRACE0(_T(".->[]"));
    }
}


void BrJComponent::RedrawParentRect(LPRECT pRect)
{  
    SEP0(_T("BrJComponent::RedrawParentRect"));
//   STRACE1(_T("BrJComponent::RedrawParentRect x:%d y:%d w:%d h:%d"),
//        pRect->left, pRect->top, 
//        pRect->right - pRect->left, pRect->bottom - pRect->top);
    //if(m_visible){
    {
        OLE_TRY
        //IViewObjectPtr spViewObject(m_spIWebBrowser2);
        //OLE_HRT( spViewObject->Freeze(
        //  DVASPECT_CONTENT,
        //  -1, 
        //  NULL,
        //  &m_dwKey
        //));

        //that is mandatory due to WS_CLIPCHILDREN or WS_CLIPCHILDREN style in parent
        SetWindowRgn(m_hwndParent, NULL, FALSE);

        //CBrIELWControl::RedrawParentRect(pRect);
        JNIEnv *env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
        if( NULL != env ){
            env->CallVoidMethod(
                m_this, 
                ms_jcidWBrComponentPeer_handlePaint,
                pRect->left - m_rcIE2.left, 
                pRect->top - m_rcIE2.top, 
                pRect->right - pRect->left, 
                pRect->bottom - pRect->top);
        }
        OLE_CATCH
    }    
}

HRESULT BrJComponent::SendIEEvent(
    int iId,
    LPTSTR lpName, 
    LPTSTR lpValue,
    _bstr_t &bsResult)
{
    OLE_DECL
    JNIEnv *env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
    if(NULL==env){
        OLE_HR = E_FAIL;
    } else {   
        jstring jsName = JNU_NewStringPlatform(env, lpName);
        if(NULL==jsName){
            OLE_HR = E_OUTOFMEMORY;
        } else {
            jstring jsValue = JNU_NewStringPlatform(env, lpValue);
            if(NULL==jsValue){
                OLE_HR = E_OUTOFMEMORY;
            } else {
                jstring jsRes = (jstring)env->CallObjectMethod(
                    m_this, 
                    ms_jcidWBrComponentPeer_postEvent,
                    iId, 
                    jsName, 
                    jsValue);
                if( NULL!=jsRes ){
                    bsResult = JStringBuffer(env, jsRes);
                    env->DeleteLocalRef(jsRes);
                }
                env->DeleteLocalRef(jsValue);
            }
            env->DeleteLocalRef(jsName);
        }
    }
    OLE_RETURN_HR                                 
}

LRESULT BrJComponent::NewIEProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    LRESULT lRes = 0;
    switch(msg){
    case WM_SETFOCUS:
        setTransparent(false);
        SendIEEvent(-1, _T("OnFocusMove"), _T("true"));
        STRACE0(_T("WM_SETFOCUS"));
        break;
    case WM_KILLFOCUS:
        SendIEEvent(-1, _T("OnFocusMove"), _T("false"));
        STRACE0(_T("WM_KILLFOCUS"));
        setTransparent(true);
        break;
    case WM_KEYDOWN:
    case WM_KEYUP:
    case WM_SYSKEYDOWN:
    case WM_SYSKEYUP:
    case WM_CHAR:
    case WM_SYSCHAR:
    //case WM_INPUT:
        lRes = CBrIELWControl::NewIEProc(hWnd, msg, wParam, lParam);
        return lRes;
    case WM_SETCURSOR:    
        lRes = CBrIELWControl::NewIEProc(hWnd, msg, wParam, lParam);
        {
            //POINT pt;
            //if( GetCursorPos(&pt) && hWnd == WindowFromPoint(pt) ){
                JNIEnv *env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
                if( NULL!=env ) {
                    jobject target = env->GetObjectField(m_this, ms_jcidWBrComponentPeer_target);
                    if(NULL!=target){
                        jobject ocursor = env->CallObjectMethod(
                            target, 
                            ms_jcidWBrComponent_getCursor);
                        if(NULL!=ocursor){
                            jlong pData = env->GetLongField(ocursor, ms_jcidCursor_pData);
                            if(0!=pData){
                                //Warning:HACK!!!
                                //pData is a pointer to AwtCursor
                                *(HCURSOR *)((LPBYTE)pData + 0x44) = GetCursor();
                                //STRACE1(_T("SET_CURSOR"));
                            }
                            env->DeleteLocalRef(ocursor);
                        }
                        env->DeleteLocalRef(target);
                    }    
                }
            //}
        }
        return lRes;

    case WM_MOUSEACTIVATE:

    case WM_MOUSEMOVE: 
    case WM_NCLBUTTONDBLCLK:
    case WM_NCLBUTTONDOWN:
    case WM_NCLBUTTONUP:

    case WM_NCRBUTTONDBLCLK:
    case WM_NCRBUTTONDOWN:
    case WM_NCRBUTTONUP:

    case WM_NCMBUTTONDBLCLK:
    case WM_NCMBUTTONDOWN:
    case WM_NCMBUTTONUP:

    case WM_LBUTTONDBLCLK:
    case WM_LBUTTONDOWN:
    case WM_LBUTTONUP:

    case WM_RBUTTONDBLCLK:
    case WM_RBUTTONDOWN:
    case WM_RBUTTONUP:

    case WM_MBUTTONDBLCLK: 
    case WM_MBUTTONDOWN:
    case WM_MBUTTONUP: 

    case WM_MOUSEWHEEL:
        //AwtComponent::WindowProc(msg, wParam, lParam);
        STRACE0(_T("mouse msg:%08x"), msg);
        if( !m_bBlockNativeInputHandler ) {
           lRes = CBrIELWControl::NewIEProc(hWnd, msg, wParam, lParam);
        }
        return lRes;
    default:
        break;
    }
    lRes = CBrIELWControl::NewIEProc(hWnd, msg, wParam, lParam);
    return lRes;
}

jintArray CopyDIBBytes(
    int iWidth, 
    int iHeight,
    void *pPoints)
{
    // copy pixels into Java array
    SEP0(_T("CopyDIBBytes"));
    JNIEnv *env = (JNIEnv *)JNU_GetEnv(jvm, JNI_VERSION_1_2);
    int iDIBSize = iWidth*iHeight;
    jintArray jaiGPoints = NULL;
    jintArray jaiLPoints = env->NewIntArray(iDIBSize + 2);
    if(NULL != jaiLPoints) {
        env->SetIntArrayRegion(jaiLPoints, 0, iDIBSize, (jint*)pPoints);
        env->SetIntArrayRegion(jaiLPoints, iDIBSize, 1, (jint*)&iWidth);
        env->SetIntArrayRegion(jaiLPoints, iDIBSize + 1, 1, (jint*)&iHeight);

        jaiGPoints = (jintArray)env->NewGlobalRef(jaiLPoints);
        env->DeleteLocalRef(jaiLPoints);
    }    
    return jaiGPoints;
}

jintArray BrJComponent::NativeDraw(LPRECT prcDraw, BOOL bToImage)
{
    SEP(_T("NativeDraw"));
    jintArray jaiGPoints = NULL;
    if( bool(m_spIWebBrowser2) ){
        OLE_TRY
        /*
        IViewObjectPtr spViewObject(m_spIWebBrowser2);
        OLE_HRT( spViewObject->Freeze(
            DVASPECT_CONTENT,
            -1, 
            NULL,
            &m_dwKey
        ));
        */
        UpdateWindowRect();
        RECT rcP = *prcDraw;
        prcDraw->left += m_rcIE2.left;
        prcDraw->right += m_rcIE2.left;
        prcDraw->top += m_rcIE2.top;
        prcDraw->bottom += m_rcIE2.top;

        RECT rc;
        if( 
            (prcDraw->left >= prcDraw->right) || 
            (prcDraw->top >= prcDraw->bottom) ||
            !IntersectRect(&rc, prcDraw, &m_rcIE2)
        )
            return NULL; //nothing to do - empty area or null-object        

        {
            HDC hdc = ::GetDC(m_hwndParent); //TODO: pParent->GetDCFromComponent();
            if(hdc) {
                OLE_NEXT_TRY
                CDCHolder shPicture;
                HDC hdcPaintTo = hdc;
                if(bToImage){
                    shPicture.Create(
                        hdc, 
                        rc.right - rc.left,
                        rc.bottom - rc.top,
                        TRUE
                    );
                    OLE_HRW32_BOOL( ::SetViewportOrgEx(
                        shPicture, 
                        -rc.left, 
                        -rc.top, 
                        NULL ));
                    hdcPaintTo = shPicture;
                }
                STRACE0(_T("Draw in %dx%d"), rc.right - rc.left, rc.bottom - rc.top);

                OLE_HRT( CBrIELWControl::OnPaint(
                    hdcPaintTo, 
                    &rc ));
                if(bToImage && NULL!=shPicture.m_pPoints){
                    jaiGPoints = CopyDIBBytes(
                        shPicture.m_iWidth, 
                        shPicture.m_iHeight,
                        shPicture.m_pPoints
                    );
                }
                OLE_CATCH
                ::ReleaseDC(m_hwndParent, hdc);
            }
        }
        OLE_CATCH
        //IViewObjectPtr spViewObject(m_spIWebBrowser2); 
        //OLE_HRT( spViewObject->Unfreeze(m_dwKey) );
    }
    return jaiGPoints;
}

struct JavaInputStream : public CStubStream
{
    static jclass ms_jcidInputStream;
    static jmethodID ms_jcidInputStream_readBytes; 

    static void initIDs(
        IN JNIEnv *env)
    {
        if(NULL==ms_jcidInputStream){
            ms_jcidInputStream = getGlobalJavaClazz(
                env,
                "java/io/InputStream"
            );
            ms_jcidInputStream_readBytes = env->GetMethodID(ms_jcidInputStream, "read", "([BII)I");
        }
    }

    JavaInputStream(
        IN JNIEnv *env,
        IN jobject jis
    ): CStubStream(),
       m_jis(jis),
       m_env(env)
    {
        initIDs(env);
    }

    virtual  HRESULT STDMETHODCALLTYPE Read(
        OUT void *pv,
        IN  ULONG cb,
        OUT ULONG *pcbRead)
    {
        OLE_DECL
        //java read
        //No more exceptions here!
        jbyteArray jba = m_env->NewByteArray( jsize(cb) );
        if( NULL == jba ){
            OLE_HR = E_OUTOFMEMORY;
        } else {
            jint ret = 0, read;
            while(0 < cb){
                read = m_env->CallIntMethod(
                    m_jis, 
                    ms_jcidInputStream_readBytes,
                    jba,
                    ret,
                    cb);
                if( m_env->ExceptionCheck() ){
                    OLE_HR = E_JAVAEXCEPTION;
                    break;
                }
                if( -1 == read ){
                    break;
                }
                cb -= read;
                ret += read;
            }

            //copy to stream
            if(SUCCEEDED(OLE_HR)){
                jbyte *pSrc = m_env->GetByteArrayElements(jba, NULL);
                if(NULL==pSrc){
                    OLE_HR = E_OUTOFMEMORY;
                } else {
                    memcpy(pv, pSrc, ret);
                    if(pcbRead){
                        *pcbRead = ret;
                    }
                    m_env->ReleaseByteArrayElements(jba, pSrc, JNI_ABORT);
                }
            }
            m_env->DeleteLocalRef(jba);
        }  
        return S_OK;
    }

    jobject m_jis;
    JNIEnv *m_env; 
};

jclass JavaInputStream::ms_jcidInputStream = NULL;
jmethodID JavaInputStream::ms_jcidInputStream_readBytes = NULL;


HRESULT BrJComponent::Connect(
    IN BSTR bsURL, 
    IN JNIEnv *env, 
    IN jobject jis)
{
    OLE_DECL
    if(NULL!=jis){
        OLE_NEXT_TRY
        IHTMLDocument2Ptr doc;     
        OLE_HRT( m_spIWebBrowser2->get_Document((LPDISPATCH *)&doc) )
        OLE_CHECK_NOTNULLSP(doc)
        IPersistStreamInitPtr ipsi(doc);
        JavaInputStream is(env, jis);
	OLE_HRT( ipsi->InitNew() )
	OLE_HRT( ipsi->Load(&is) )
        OLE_CATCH
    }else{
        OLE_HR = CBrIELWControl::Connect(bsURL);
    }
    OLE_RETURN_HR
}

/************************************************************************
 * WBrComponentPeer native methods
 */

extern "C" {

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    initIDs
  */
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_initIDs(
    JNIEnv *env, 
    jclass cls)
{
    BrJComponent::initIDs(env, cls);
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    create
  */
struct CreateAction : public BrowserAction
{
    HWND    m_parent;
    BrJComponent *m_pThis;

    CreateAction(
        BrJComponent *pThis,
        HWND parent)
    : m_pThis(pThis),
      m_parent(parent)
    {}

    virtual HRESULT Do(JNIEnv *env){
        return m_pThis->create(env, m_parent);
    }
};

JNIEXPORT jlong JNICALL Java_org_jdic_web_peer_WBrComponentPeer_create(
    JNIEnv *env, 
    jobject self,
    jlong parent)
{
    BrJComponent *pThis = new BrJComponent(env, self);
    if(pThis){
        OLE_TRY
        OLE_HRT(pThis->GetThread()->MakeAction(
            env,
            "Browser create error",
            CreateAction(
                pThis,
                (HWND)parent)));
        OLE_CATCH
        if(FAILED(OLE_HR)){
            pThis->destroy(env);
            delete pThis;
            pThis = NULL;
        }
    }
    return jlong(pThis);
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    destroy
  */
struct DestroyAction : public BrowserAction
{
    BrJComponent *m_pThis;
    DestroyAction(BrJComponent *pThis)
    : m_pThis(pThis)
    {}

    virtual HRESULT Do(JNIEnv *env){
        m_pThis->destroy(env);
        return S_OK;
    }
};
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_destroy(
    JNIEnv *env, 
    jobject self)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        pThis->GetThread()->MakeAction(
            env,
            "Browser destroy error",
            DestroyAction(pThis));
        delete pThis;
        env->SetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data, 0L);
    }
}

/*
 * Class:     sun.awt.windows.WBrComponentPeer
 * Method:    execJS
 */
struct ExecJSAction : public BrowserAction
{
    BrJComponent *m_pThis;
    JStringBuffer m_jstCode;
    _bstr_t m_bsResult;

    ExecJSAction(
        BrJComponent *pThis,
        JNIEnv *env,
        jstring jsCode
    ): m_pThis(pThis),
       m_jstCode(env, jsCode),
       m_bsResult(_T("error:Null interface"))
    {}

    virtual HRESULT Do(JNIEnv *env)
    {
        //SEP(_T("_ExecJS"))
        LPTSTR pCode = (LPTSTR)m_jstCode;
        STRACE0(_T("code:%s"), pCode);
        if(NULL==pCode){
            return S_FALSE;
        } else if( 0==_tcsncmp(pCode, _T("##"), 2) ){
            if( 0==_tcscmp(pCode, _T("##stop")) ){
                OLE_TRY
                OLE_HRT( m_pThis->m_spIWebBrowser2->Stop() );
                OLE_CATCH
                OLE_RETURN_HR
            } else if( 0==_tcsncmp(pCode, _T("##setFocus"), 10) ){
                OLE_TRY
                STRACE0(_T("##setFocus"));
                IOleObjectPtr spOleObject(m_pThis->m_spIWebBrowser2);
                OLE_CHECK_NOTNULLSP(spOleObject)
                OLE_HRT( spOleObject->DoVerb(
                    OLEIVERB_UIACTIVATE, 
                    NULL, 
                    m_pThis, 
                    0, 
                    NULL, 
                    NULL))
                if( 0!=_tcscmp(pCode, _T("##setFocus(false)")) ){
                    SendMessage(m_pThis->GetIEWnd(), WM_KEYDOWN, VK_TAB, 0x0000000);
                    SendMessage(m_pThis->GetIEWnd(), WM_KEYUP, VK_TAB, 0xc000000);
                }
                ::OLE_CoPump();
                OLE_CATCH
                OLE_RETURN_HR
            } else if( 0==_tcsncmp(pCode, _T("##setNativeDraw"), 15) ) {
                m_pThis->m_bNativeDraw = ( 0==_tcscmp(pCode, _T("##setNativeDraw(true)")) ); 
                return S_OK;
            } else if( 0==_tcsncmp(pCode, _T("##showCaret"), 11) ) {
                OLE_TRY
                IWebBrowser2Ptr br(m_pThis->m_spIWebBrowser2);
                OLE_CHECK_NOTNULLSP(br)

                IHTMLDocument2Ptr doc;     
                OLE_HRT( br->get_Document((LPDISPATCH *)&doc) )
                OLE_CHECK_NOTNULLSP(doc)
                
                IDisplayServicesPtr ds(doc);                     
                OLE_CHECK_NOTNULLSP(ds)

                IHTMLCaretPtr cr;                     
                OLE_HRT( ds->GetCaret(&cr) )
                OLE_CHECK_NOTNULLSP(cr)

                if( 0==_tcscmp(pCode, _T("##showCaret(true)")) ){
                   OLE_HRT( cr->Show(FALSE) )
                   STRACE1(_T("{Show------"));
                } else {
                   OLE_HRT( cr->Hide() )
                   STRACE1(_T("}Hide------"));
                }
                OLE_CATCH
                OLE_RETURN_HR
            }
            return S_FALSE;
        }
        OLE_TRY
        IWebBrowser2Ptr br(m_pThis->m_spIWebBrowser2);
        OLE_CHECK_NOTNULLSP(br)

        //that can be any type of document
        //Acrobat Reader for example
        IDispatchPtr docO;     
        OLE_HRT( br->get_Document(&docO) )

        //we are trying customize it to HTML document
        //empty document is a valid argument
        IHTMLDocument2Ptr doc(docO);     
        OLE_CHECK_NOTNULLSP(doc)

        IHTMLWindow2Ptr wnd;
        OLE_HRT( doc->get_parentWindow(&wnd) )
        OLE_CHECK_NOTNULLSP(wnd)

        _variant_t vtResult;
        STRACE0(_T("makeScript"));
        _bstr_t bsEval( ('#'!=*pCode && ':'!=*pCode)
            ? (_B(L"document.documentElement.setAttribute(\'javaEval\', eval(\'") + pCode + L"\'))")
            : _B(pCode+1)
        );
        
        STRACE0(_T("execScript"));
        OLE_HRT( wnd->execScript( bsEval, _B(""), &vtResult) )
        vtResult.Clear();

        if( ':'!=*pCode ){
            IHTMLDocument3Ptr doc3(doc);     
            OLE_CHECK_NOTNULLSP(doc3)
            
            IHTMLElementPtr el;
            OLE_HRT( doc3->get_documentElement(&el))
            OLE_CHECK_NOTNULLSP(el)

            OLE_HRT( el->getAttribute(_B("javaEval"), 0, &vtResult) )
        }

        if(VT_NULL!=vtResult.vt){
            m_bsResult = vtResult;
        } else {
            m_bsResult = "";
        }
        STRACE0(_T("result:%s"), (LPCTSTR)m_bsResult);
        OLE_CATCH
        if(FAILED(OLE_HR)){
            m_bsResult = _T("error:");
            m_bsResult += _B(_V(OLE_HR));
        }
        OLE_RETURN_HR
    }
};

JNIEXPORT jstring JNICALL Java_org_jdic_web_peer_WBrComponentPeer_execJS(
    JNIEnv *env, 
    jobject self,
    jstring jsCode)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        OLE_TRY
        ExecJSAction a(
            pThis,
            env,
            jsCode);
        OLE_HRT( pThis->GetThread()->MakeAction(
            env,
            "Browser JScript execution error",
            a));
        LPCTSTR lpRes = a.m_bsResult;
        return JNU_NewStringPlatform(env, lpRes ? lpRes : _T(""));
        OLE_CATCH
    }
    return 0;
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    setURL
 * Signature: (Ljava/lang/String;)V
 */
struct SetURLAction : public BrowserAction{
    JStringBuffer m_jstURL;
    BrJComponent *m_pThis;
    jobject m_jisURL;

    SetURLAction(
        BrJComponent *pThis,
        JNIEnv *env,
        jstring jURL,
        jobject jisURL
    ):m_pThis(pThis),
      m_jstURL(env, jURL),
      m_jisURL(makeGlobal(env, jisURL))
    {}
    virtual HRESULT Do(JNIEnv *env)
    {
        OLE_TRY
        OLE_HRT( m_pThis->Connect(_B(m_jstURL), env, m_jisURL) )
        OLE_CATCH_ALL
        releaseGlobal(env, m_jisURL);
        OLE_RETURN_HR
    }
};

JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_setURL(
    JNIEnv *env, 
    jobject self,
    jstring jsURL,
    jobject jisURL)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        pThis->GetThread()->MakeAction(
            env,
            "URL navigation error",
            SetURLAction(
                pThis,
                env,
                jsURL,
                jisURL));
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    laizyUpdate
 * Signature: (jint)
 */
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_laizyUpdate(
    JNIEnv *env, 
    jobject self,
    jint msDelay)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
       pThis->LaizyUpdate(msDelay);
    }
}


struct UpdateTransparentAction : public BrowserAction
{
    BrJComponent *m_pThis;
    RECT          m_rc;  

    UpdateTransparentAction(
        BrJComponent *pThis,
        jint x, jint y, jint w, jint h
    ):m_pThis(pThis)
    {
        m_rc.left = x;
        m_rc.top  = y;
        m_rc.right = m_rc.left + w;
        m_rc.bottom = m_rc.top + h;
    }
    virtual HRESULT Do(JNIEnv *env)
    {
        m_pThis->updateTransparentMask(&m_rc);
        return S_OK; 
    }
};

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    updateTransparentMask
  */
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_updateTransparentMask(
    JNIEnv *env, 
    jobject self,
    jint x, jint y, jint w, jint h)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        //pThis->updateTransparentMask();
        pThis->GetThread()->MakeAction(
            env,
            "updateTransparentMask error",
            UpdateTransparentAction(pThis, x, y, w, h)
        );
    }
}


struct SetTransparentAction : public BrowserAction
{
    BrJComponent *m_pThis;
    jboolean m_transparent;

    SetTransparentAction(
        BrJComponent *pThis,
        jboolean transparent
    ):m_pThis(pThis), 
      m_transparent(transparent)
    {}
    virtual HRESULT Do(JNIEnv *env)
    {
        m_pThis->setTransparent(m_transparent);
        return S_OK; 
    }
};
/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    setTransparent
  */
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_nativeSetTransparent(
    JNIEnv *env, 
    jobject self,
    jboolean transparent)
{
    SEP0(_T("nativeSetTransparent"))
    //STRACE1(_T("nativeSetTransparent %d"), transparent);
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        //pThis->setTransparent(transparent);
        pThis->GetThread()->MakeAction(
            env,
            "nativeSetTransparent error",
            SetTransparentAction(pThis, transparent)
        );
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    setVisible
 */
struct ShowAction : public BrowserAction
{
    BrJComponent *m_pThis;
    BOOL bShow;

    ShowAction(
        BrJComponent *pThis,
        BOOL _bShow
    ):m_pThis(pThis),
      bShow(_bShow)
    {}
    virtual HRESULT Do(JNIEnv *env)
    {
        ::ShowWindow(m_pThis->GetTopWnd(), bShow ? SW_SHOW : SW_HIDE );        
        return S_OK; 
    }
};

JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_setVisible(
    JNIEnv *env, 
    jobject self,
    jboolean aFlag)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        pThis->GetThread()->MakeAction(
            env,
            "ShowWindow error",
            ShowAction(
                pThis,
                aFlag));
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    setEnabled
 */
struct EnableAction : public BrowserAction
{
    BrJComponent *m_pThis;
    BOOL bEnable;

    EnableAction(
        BrJComponent *pThis,
        BOOL _bEnable
    ):m_pThis(pThis),
      bEnable(_bEnable)
    {}
    virtual HRESULT Do(JNIEnv *env)
    {
        ::EnableWindow(m_pThis->GetTopWnd(), bEnable);
        return S_OK; 
    }
};

JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_setEnabled(
    JNIEnv *env, 
    jobject self,
    jboolean enabled)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        pThis->GetThread()->MakeAction(
            env,
            "EnableWindow error",
            EnableAction(
                pThis,
                enabled));
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    nativeDraw
 */
struct NativeDrawAction : public BrowserAction
{
    BrJComponent *m_pThis;
    RECT m_rcDraw;
    BOOL m_bToImage;
    jintArray m_jaiPoints;

    NativeDrawAction(
        BrJComponent *pThis,
        jint x, jint y, jint w, jint h,
        BOOL bToImage
    ):m_pThis(pThis),
      m_bToImage(bToImage),
      m_jaiPoints(NULL)
    {
        SetRect(&m_rcDraw, x, y, x + w, y + h);
    }
    virtual HRESULT Do(JNIEnv *env)
    {
        m_jaiPoints = m_pThis->NativeDraw(&m_rcDraw, m_bToImage);
        return S_OK; 
    }
};

JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_nativeDraw(
    JNIEnv *env, 
    jobject self,
    jint x, jint y, jint w, jint h)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        pThis->GetThread()->MakeAction(
            env,
            "Native Draw error",
            NativeDrawAction(
                pThis,
                x, y, w, h, FALSE));
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    ImageData
 */
JNIEXPORT jintArray JNICALL Java_org_jdic_web_peer_WBrComponentPeer_ImageData(
    JNIEnv *env, 
    jobject self, 
    jint x, jint y, jint w, jint h)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        OLE_TRY
        NativeDrawAction a(
            pThis,
            x, y, w, h, true);
        OLE_HRT(pThis->GetThread()->MakeAction(
            env,
            "Native Draw error",
            a));
        jintArray jaiColors = (jintArray)env->NewLocalRef(a.m_jaiPoints);
        env->DeleteGlobalRef(a.m_jaiPoints);    
        return jaiColors;
        OLE_CATCH
    }
    return NULL;
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    nativePosOnScreen
 */
struct ReshapeAction : public BrowserAction
{
    BrJComponent *m_pThis;
    jint x, y, w, h;

    ReshapeAction(
        BrJComponent *pThis,
        jint _x, jint _y, jint _w, jint _h
    ):m_pThis(pThis),
      x(_x), y(_y), w(_w), h(_h)
    {}
    virtual HRESULT Do(JNIEnv *env)
    {
        MoveWindow(m_pThis->GetTopWnd(), x, y, w, h, FALSE);
        return S_OK; 
    }
};

JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_nativePosOnScreen(
    JNIEnv *env, 
    jobject self,
    jint x, jint y, jint w, jint h)
{
    SEP0(_T("nativePosOnScreen"))
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        STRACE0(_T("nativePosOnScreen x:%d y:%d w:%d h:%d"), x, y, w, h);
        POINT pt = {x, y};
        MapWindowPoints(
            NULL,
            pThis->GetParent(),
            &pt,
            1);
        if( -1==w || -1==h){
            RECT rc;
            GetWindowRect(pThis->GetTopWnd(), &rc);
            if( -1==w ){
                w = rc.right - rc.left;
            }    
            if( -1==h ){
                h = rc.bottom - rc.top;
            }
        }
        pThis->GetThread()->MakeAction(
            env,
            "Reshape error",
            ReshapeAction(
                        pThis,
                        pt.x, pt.y, w, h)
        );
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    clearRgn
 */
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_clearRgn(
    JNIEnv *env, 
    jobject self)
{
    SEP0(_T("clearRgn"))
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis && pThis->m_hChildArea){
        DeleteObject((HGDIOBJ)pThis->m_hChildArea);
        pThis->m_hChildArea = CreateRectRgn(0,0,0,0);
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    clearRgn
 */
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_clipChild(
    JNIEnv *env, 
    jobject self,
    jint x, jint y, jint w, jint h)
{
    SEP0(_T("clipChild"))
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis ){
        HRGN rg = CreateRectRgn(x, y, x+w, y+h);
        if(NULL!=rg){
            CombineRgn(
                pThis->m_hChildArea,
                pThis->m_hChildArea,
                rg,
                RGN_OR
            );
            DeleteObject((HGDIOBJ)rg);
        }
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    blockNativeInputHandler
 */
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_blockNativeInputHandler(
    JNIEnv *env, 
    jobject self, 
    jboolean bBlockNativeInputHandler)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        pThis->m_bBlockNativeInputHandler = bBlockNativeInputHandler; 
    }
}

/*
 * Class:     org_jdic_web_peer_WBrComponentPeer
 * Method:    nativeSendMouseEvent
 */
const static int WND_TOP = 0;
const static int WND_PARENT = 1;
const static int WND_IE = 2;
JNIEXPORT jlong JNICALL Java_org_jdic_web_peer_WBrComponentPeer_nativeSendMouseEvent(
    JNIEnv *env, 
    jobject self, 
    jint wnd, jint wm, jint wParam, jint lParam)
{
    jlong ret = 0;
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis){
        HWND hWnd = (WND_PARENT==wnd) ? pThis->GetParent() : pThis->GetIEWnd();
        if(hWnd){
            ret = (jlong)::SendMessage(hWnd, (UINT)wm, (WPARAM)wParam, (LPARAM)lParam);
        }
    }
    return ret;
}
JNIEXPORT void JNICALL Java_org_jdic_web_peer_WBrComponentPeer_nativeReleaseMouseCapture(
    JNIEnv *env, 
    jobject self)
{
    jlong ret = 0;
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    //if( pThis && pThis->GetParent()==GetCapture() ){
    if(!ReleaseCapture()){
        STRACE1(_T("Error:ReleaseCapture"));
    }
}


JNIEXPORT jint JNICALL Java_org_jdic_web_peer_WBrComponentPeer_setActionFiler(
    JNIEnv *env, 
    jobject self, 
    jint  flag, jboolean busyState)
{
    BrJComponent *pThis = (BrJComponent *)env->GetLongField(self, BrJComponent::ms_jcidWBrComponentPeer_data);
    if(pThis ){
        pThis->GetThread()->SetBusy( BOOL(busyState) );
    }
    return 0;
}

} /* extern "C" */
