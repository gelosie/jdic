/* -*- Mode: C++; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is mozilla.org Code.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Chak Nanga <chak@netscape.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

// BrowserFrm.h : interface of the CBrowserFrame class
//
/////////////////////////////////////////////////////////////////////////////

#ifndef _IBROWSERFRM_H
#define _IBROWSERFRM_H

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "BrowserView.h"
#include "BrowserToolTip.h"

// A simple UrlBar class...
class CUrlBar : public CComboBoxEx
{
public:
    inline void GetEnteredURL(CString& url) {
        GetEditCtrl()->GetWindowText(url);
    }
    inline void GetSelectedURL(CString& url) {
        GetLBText(GetCurSel(), url);
    }
    inline void SetCurrentURL(LPCTSTR pUrl) {
        SetWindowText(pUrl);
    }
    inline BOOL EditCtrlHasFocus() {
        return (GetEditCtrl()->m_hWnd == CWnd::GetFocus()->m_hWnd);
    }
    inline BOOL EditCtrlHasSelection() {
        int nStartChar = 0, nEndChar = 0;
        if(EditCtrlHasFocus())
            GetEditCtrl()->GetSel(nStartChar, nEndChar);
        return (nEndChar > nStartChar) ? TRUE : FALSE;
    }
    inline BOOL CanCutToClipboard() {
        return EditCtrlHasSelection();
    }
    inline void CutToClipboard() {
        GetEditCtrl()->Cut();
    }
    inline BOOL CanCopyToClipboard() {
        return EditCtrlHasSelection();
    }
    inline void CopyToClipboard() {
        GetEditCtrl()->Copy();
    }
    inline BOOL CanPasteFromClipboard() {
        return EditCtrlHasFocus();
    }
    inline void PasteFromClipboard() {
        GetEditCtrl()->Paste();
    }
    inline BOOL CanUndoEditOp() {
        return EditCtrlHasFocus() ? GetEditCtrl()->CanUndo() : FALSE;
    }
    inline void UndoEditOp() {        
        if(EditCtrlHasFocus())
            GetEditCtrl()->Undo();
    }
};

// CMyStatusBar class
class CMyStatusBar : public CStatusBar
{
public:
    CMyStatusBar();
    virtual ~CMyStatusBar();

protected:
    afx_msg void OnLButtonDown(UINT nFlags, CPoint point);

    DECLARE_MESSAGE_MAP()
};

class CBrowserFrame : public CFrameWnd
{    
public:
    CBrowserFrame();
    CBrowserFrame(PRUint32 chromeMask);

protected: 
    DECLARE_DYNAMIC(CBrowserFrame)

public:
    inline CBrowserImpl *GetBrowserImpl() { return m_wndBrowserView.mpBrowserImpl; }

    CToolBar        m_wndToolBar;
    CMyStatusBar    m_wndStatusBar;
    CProgressCtrl   m_wndProgressBar;
    CUrlBar         m_wndUrlBar;
    CReBar          m_wndReBar;
    CBrowserToolTip m_wndTooltip;

    // The view inside which the embedded browser will
    // be displayed in
    CBrowserView    m_wndBrowserView;

    // Wrapper functions for UrlBar clipboard operations
    inline BOOL CanCutUrlBarSelection() { return m_wndUrlBar.CanCutToClipboard(); }
    inline void CutUrlBarSelToClipboard() { m_wndUrlBar.CutToClipboard(); }
    inline BOOL CanCopyUrlBarSelection() { return m_wndUrlBar.CanCopyToClipboard(); }
    inline void CopyUrlBarSelToClipboard() { m_wndUrlBar.CopyToClipboard(); }
    inline BOOL CanPasteToUrlBar() { return m_wndUrlBar.CanPasteFromClipboard(); }
    inline void PasteFromClipboardToUrlBar() { m_wndUrlBar.PasteFromClipboard(); }
    inline BOOL CanUndoUrlBarEditOp() { return m_wndUrlBar.CanUndoEditOp(); }
    inline void UndoUrlBarEditOp() { m_wndUrlBar.UndoEditOp(); }

    // This specifies what UI elements this frame will sport
    // w.r.t. toolbar, statusbar, urlbar etc.
    PRUint32 m_chromeMask;

public:
    /////////////////////////////////////////////////////////////////
    // Helper methods
    //
    void UpdateSecurityStatus(PRInt32 aState);
    void ShowSecurityInfo();
    void SetupFrameChrome();
    void SetBrowserId(int id) { m_Id = id; }
    int GetBrowserId() { return m_Id; }

    // Progress Related Methods
    void UpdateStatusBarText(const PRUnichar *aMessage);
    void UpdateProgress(PRInt32 aCurrent, PRInt32 aMax);
    void UpdateBusyState(PRBool aBusy);
    void UpdateCurrentURI(nsIURI *aLocation);

    // BrowserFrame Related Methods
    PRBool CreateNewBrowserFrame(
                            PRUint32 chromeMask, 
                            PRInt32 x, PRInt32 y, 
                            PRInt32 cx, PRInt32 cy,
                            nsIWebBrowser ** aWebBrowser);
    void DestroyBrowserFrame();
    void GetBrowserFrameTitle(PRUnichar **aTitle);
    void SetBrowserFrameTitle(const PRUnichar *aTitle);
    void GetBrowserFramePosition(PRInt32 *aX, PRInt32 *aY);
    void SetBrowserFramePosition(PRInt32 aX, PRInt32 aY);
    void GetBrowserFrameSize(PRInt32 *aCX, PRInt32 *aCY);
    void SetBrowserFrameSize(PRInt32 aCX, PRInt32 aCY);
    void GetBrowserFramePositionAndSize(PRInt32 *aX, PRInt32 *aY, PRInt32 *aCX, PRInt32 *aCY);
    void SetBrowserFramePositionAndSize(PRInt32 aX, PRInt32 aY, PRInt32 aCX, PRInt32 aCY, PRBool fRepaint);
    void ShowBrowserFrame(PRBool aShow);
    void FocusAvailable(PRBool *aFocusAvail);
    void GetBrowserFrameVisibility(PRBool *aVisible);

    // ContextMenu Related Methods
    void ShowContextMenu(PRUint32 aContextFlags, nsIContextMenuInfo *aInfo);

    // Tooltip Methods
    void ShowTooltip(PRInt32 aXCoords, PRInt32 aYCoords, const PRUnichar *aTipText);
    void HideTooltip();

// Overrides
    // ClassWizard generated virtual function overrides
    //{{AFX_VIRTUAL(CBrowserFrame)
	public:
    virtual BOOL PreCreateWindow(CREATESTRUCT& cs);
    virtual BOOL OnCmdMsg(UINT nID, int nCode, void* pExtra, AFX_CMDHANDLERINFO* pHandlerInfo);
	//}}AFX_VIRTUAL

// Implementation
public:
    virtual ~CBrowserFrame();
#ifdef _DEBUG
    virtual void AssertValid() const;
    virtual void Dump(CDumpContext& dc) const;
#endif

// Generated message map functions
protected:
    //{{AFX_MSG(CBrowserFrame)
    afx_msg int OnCreate(LPCREATESTRUCT lpCreateStruct);
    afx_msg void OnSetFocus(CWnd *pOldWnd);
    afx_msg void OnSize(UINT nType, int cx, int cy);
    afx_msg void OnActivate(UINT nState, CWnd* pWndOther, BOOL bMinimized);
	//}}AFX_MSG
    DECLARE_MESSAGE_MAP()

private:
    int m_Id;
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif //_IBROWSERFRM_H
