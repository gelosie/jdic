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

// mozembed.h : main header file for the MOZEMBED application
//

#ifndef _MOZEMBED_H
#define _MOZEMBED_H

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#ifndef __AFXWIN_H__
    #error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"       // main symbols
#include "MsgServer.h"
#include "Message.h"
#include "Util.h"

#define WM_SOCKET_MSG   WM_USER + 101

extern int gQuitMode;

/////////////////////////////////////////////////////////////////////////////
// MozEmbedApp:
// See mozembed.cpp for the implementation of this class
//

class CBrowserFrame;

class MozEmbedApp : public CWinApp,
                    public nsIWindowCreator,
                    public nsSupportsWeakReference
{
public:
    MozEmbedApp();
    virtual ~MozEmbedApp();
    
    NS_DECL_ISUPPORTS
    NS_DECL_NSIWINDOWCREATOR

    CBrowserFrame* CreateNewBrowserFrame(
                            PRUint32 chromeMask = nsIWebBrowserChrome::CHROME_ALL, 
                            HWND hParent = NULL,
                            PRInt32 x = -1, PRInt32 y = -1, 
                            PRInt32 cx = -1, PRInt32 cy = -1, 
                            PRBool bShowWindow = PR_TRUE);                            
    CBrowserFrame* CreateEmbeddedBrowserFrame(HWND hParent);

    void ShowDebugConsole();

    // Overrides
    // ClassWizard generated virtual function overrides
    //{{AFX_VIRTUAL(MozEmbedApp)
	public:
    virtual BOOL InitInstance();
    virtual int ExitInstance();
	virtual BOOL PreTranslateMessage(MSG* pMsg);
	//}}AFX_VIRTUAL

    WBArray m_FrameWndArray;

// Implementation

public:
    //{{AFX_MSG(MozEmbedApp)
    afx_msg void OnNewBrowser();
        // NOTE - the ClassWizard will add and remove member functions here.
        //    DO NOT EDIT what you see in these blocks of generated code !
    //}}AFX_MSG
    DECLARE_MESSAGE_MAP()

private:
    nsresult        OverrideComponents();
    nsresult        InitializeWindowCreator();
    BOOL            InitMozilla();
    BOOL            CreateHiddenWindow();

    BOOL mInitFailed;

    void MessageReceived(const char * msg);
};

/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // _MOZEMBED_H
