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

#ifndef BrJComponent_H
#define BrJComponent_H

#include "BrIELWControl.h"
#include <ole2.h>
#include <richedit.h>
#include <richole.h>                                                           

/************************************************************************
 * BrJComponent class
 */
enum EBrComponentEvent {
    DOCUMENT_COMPLITE = 1
};

class BrJComponent 
:  public CBrIELWControl
{
public:
    static jclass    ms_jcidBrComponent;
    static jfieldID  ms_jcidWBrComponent_x;
    static jfieldID  ms_jcidWBrComponent_y;
    static jfieldID  ms_jcidWBrComponent_width;
    static jfieldID  ms_jcidWBrComponent_height;
    static jmethodID ms_jcidWBrComponent_getCursor;

    static jfieldID  ms_jcidWBrComponentPeer_data;
    static jfieldID  ms_jcidWBrComponentPeer_target;
    static jmethodID ms_jcidWBrComponentPeer_postEvent;
    static jmethodID ms_jcidWBrComponentPeer_handlePaint;

    static jclass    ms_jcidCursor;
    static jfieldID  ms_jcidCursor_pData;


public:
    static void initIDs(JNIEnv *env, jclass clazz);


    HRESULT getTargetRect(
        JNIEnv *env, 
        LPRECT prc);
    HRESULT create(    
        JNIEnv *env, 
        HWND    hParent);
    BrJComponent(
        JNIEnv *env, 
        jobject othis);

    virtual void destroy(JNIEnv *env);
    virtual void updateTransparentMask();
    virtual void setTransparent(boolean bTransparent);
    virtual ~BrJComponent();

    virtual HRESULT SendIEEvent(
        int iId,
        LPTSTR lpName, 
        LPTSTR lpValue);
    virtual HRESULT BrJComponent::Connect(
        IN BSTR bsURL, 
        IN JNIEnv *env, 
        IN jobject jis);


private:
    //IELWComp    
    virtual void RedrawParentRect(LPRECT pRect);
    virtual LRESULT NewIEProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam);

public:
    // some methods invoked on Toolkit thread
    HWND GetIEWnd() { return m_hwndIE; }
    HWND GetTopWnd() { return m_hwndShell; }
    HWND GetParent() { return m_hwndParent; }

    //actors     
    jintArray NativeDraw(LPRECT prcDraw, BOOL bToImage);
       
private:
    RECT    m_rcInvalid;
    DWORD   m_dwKey;
    jobject m_this;
    boolean m_bTransparent;
public:
    boolean m_synthetic;
    boolean m_bBlockNativeInputHandler;
    HRGN    m_hChildArea;
};

#endif /* BrJComponent_H */
