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

// File Overview....
//
// The typical MFC View, toolbar, statusbar creation done 
// in CBrowserFrame::OnCreate()
//
// Code to update the Status/Tool bars in response to the
// Web page loading progress(called from methods in CBrowserImpl)
//
// SetupFrameChrome() determines what, if any, UI elements this Frame
// will sport based on the current "chromeMask" 
//
// Also take a look at OnClose() which gets used when you close a browser
// window. This needs to be overrided mainly to handle supporting multiple
// browser frame windows via the "New Browser Window" menu item
// Without this being overridden the MFC framework handles the OnClose and
// shutsdown the complete application when a frame window is closed.
// In our case, we want the app to shutdown when the File/Exit menu is chosen
//
// Another key functionality this object implements is the IBrowserFrameGlue
// interface - that's the interface the Gecko embedding interfaces call
// upong to update the status bar etc.
// (Take a look at IBrowserFrameGlue.h for the interface definition and
// the BrowserFrm.h to see how we implement this interface - as a nested
// class)
// We pass this Glue object pointer to the CBrowserView object via the 
// SetBrowserFrameGlue() method. The CBrowserView passes this on to the
// embedding interface implementaion
//
// Please note the use of the macro METHOD_PROLOGUE in the implementation
// of the nested BrowserFrameGlue object. Essentially what this macro does
// is to get you access to the outer (or the object which is containing the
// nested object) object via the pThis pointer.
// Refer to the AFXDISP.H file in VC++ include dirs
//
// Next suggested file to look at : BrowserView.cpp

#include "stdafx.h"
#include "MozEmbed.h"
#include "BrowserFrm.h"
#include "BrowserImpl.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

/////////////////////////////////////////////////////////////////////////////
// CBrowserFrame

IMPLEMENT_DYNAMIC(CBrowserFrame, CFrameWnd)

BEGIN_MESSAGE_MAP(CBrowserFrame, CFrameWnd)
    //{{AFX_MSG_MAP(CBrowserFrame)
    ON_WM_CREATE()
    ON_WM_SETFOCUS()
    ON_WM_SIZE()
    ON_WM_ACTIVATE()
    //}}AFX_MSG_MAP
END_MESSAGE_MAP()

static UINT indicators[] =
{
    ID_SEPARATOR,           // For the Status line
    ID_SEPARATOR,           // For the Progress Bar
    ID_SEPARATOR,           // For the padlock image
};

/////////////////////////////////////////////////////////////////////////////
// CBrowserFrame construction/destruction

CBrowserFrame::CBrowserFrame()
{
    m_Id = -1;
}

CBrowserFrame::CBrowserFrame(PRUint32 chromeMask)
{
    // Save the chromeMask off. It'll be used
    // later to determine whether this browser frame
    // will have menubar, toolbar, statusbar etc.

    m_Id = -1;
    m_chromeMask = chromeMask;
}

CBrowserFrame::~CBrowserFrame()
{
}

// This is where the UrlBar, ToolBar, StatusBar, ProgressBar
// get created
//
int CBrowserFrame::OnCreate(LPCREATESTRUCT lpCreateStruct)
{
    if (CFrameWnd::OnCreate(lpCreateStruct) == -1)
        return -1;

    // Pass "this" to the View for later callbacks
    // and/or access to any public data members, if needed
    //
    m_wndBrowserView.SetBrowserFrame(this);

    // create a view to occupy the client area of the frame
    // This will be the view in which the embedded browser will
    // be displayed in
    //
    if (!m_wndBrowserView.Create(NULL, NULL, AFX_WS_DEFAULT_VIEW,
        CRect(0, 0, 0, 0), this, AFX_IDW_PANE_FIRST, NULL))
    {
        TRACE0("Failed to create view window\n");
        return -1;
    }

    // create the URL bar (essentially a ComboBoxEx object)
    if (!m_wndUrlBar.Create(CBS_DROPDOWN | WS_CHILD, CRect(0, 0, 200, 150), this, ID_URL_BAR))
    {
        TRACE0("Failed to create URL Bar\n");
        return -1;      // fail to create
    }
    
    UINT resID = IDR_MAINFRAME;

    // Create the toolbar with Back, Fwd, Stop, etc. buttons..
    //                     or
    // Create a toolbar with the Editor toolbar buttons - Bold, Italic etc.
    if (!m_wndToolBar.CreateEx(this, TBSTYLE_FLAT, WS_CHILD | WS_VISIBLE | CBRS_ALIGN_TOP
        | CBRS_TOOLTIPS | CBRS_FLYBY | CBRS_SIZE_DYNAMIC) ||
        !m_wndToolBar.LoadToolBar(resID))
    {
        TRACE0("Failed to create toolbar\n");
        return -1;      // fail to create
    }

    // Create a ReBar window to which the toolbar and UrlBar 
    // will be added
    if (!m_wndReBar.Create(this))
    {
        TRACE0("Failed to create ReBar\n");
        return -1;      // fail to create
    }
    
    //Add the ToolBar and UrlBar windows to the rebar
    m_wndReBar.AddBar(&m_wndToolBar);
    m_wndReBar.AddBar(&m_wndUrlBar, _T("Enter URL:"));

    // Create the status bar with two panes - one pane for actual status
    // text msgs. and the other for the progress control
    if (!m_wndStatusBar.CreateEx(this) ||
        !m_wndStatusBar.SetIndicators(indicators,
          sizeof(indicators)/sizeof(UINT)))
    {
        TRACE0("Failed to create status bar\n");
        return -1;      // fail to create
    }

    // Create the progress bar as a child of the status bar.
    // Note that the ItemRect which we'll get at this stage
    // is bogus since the status bar panes are not fully
    // positioned yet i.e. we'll be passing in an invalid rect
    // to the Create function below
    // The actual positioning of the progress bar will be done
    // in response to OnSize()
    RECT rc;
    m_wndStatusBar.GetItemRect (1, &rc);
    if (!m_wndProgressBar.Create(WS_CHILD|WS_VISIBLE|PBS_SMOOTH, rc, &m_wndStatusBar, ID_PROG_BAR))
    {
        TRACE0("Failed to create progress bar\n");
        return -1;      // fail to create
    }

    // The third pane(i.e. at index 2) of the status bar will have 
    // the security lock icon displayed in it. Set up it's size(16) 
    // and style(no border)so that the padlock icons can be properly drawn
    m_wndStatusBar.SetPaneInfo(2, -1, SBPS_NORMAL|SBPS_NOBORDERS, 16);

    // Create a tooltip window. (the MFC tooltip is not really suitable)
    m_wndTooltip.Create(CWnd::GetDesktopWindow());

    // Also, set the padlock icon to be the insecure icon to begin with
    UpdateSecurityStatus(nsIWebProgressListener::STATE_IS_INSECURE);

    // Based on the "chromeMask" we were supplied during construction
    // hide any requested UI elements - statusbar, menubar etc...
    // Note that the window styles (WM_RESIZE etc) are set inside
    // of PreCreateWindow()

    SetupFrameChrome(); 

    return 0;
}

void CBrowserFrame::SetupFrameChrome()
{
    if(m_chromeMask == nsIWebBrowserChrome::CHROME_ALL)
        return;

    if(! (m_chromeMask & nsIWebBrowserChrome::CHROME_MENUBAR) )
        SetMenu(NULL); // Hide the MenuBar

    if(! (m_chromeMask & nsIWebBrowserChrome::CHROME_TOOLBAR) )
        m_wndReBar.ShowWindow(SW_HIDE); // Hide the ToolBar

    if(! (m_chromeMask & nsIWebBrowserChrome::CHROME_STATUSBAR) )
        m_wndStatusBar.ShowWindow(SW_HIDE); // Hide the StatusBar
}

BOOL CBrowserFrame::PreCreateWindow(CREATESTRUCT& cs)
{
    if( !CFrameWnd::PreCreateWindow(cs) )
        return FALSE;

    cs.dwExStyle &= ~WS_EX_CLIENTEDGE;

    if (m_chromeMask == nsIWebBrowserChrome::CHROME_DEFAULT) {
        cs.style = WS_CHILD;
    }
    else if (m_chromeMask & nsIWebBrowserChrome::CHROME_OPENAS_DIALOG) {
        cs.style = WS_OVERLAPPED | WS_BORDER | WS_DLGFRAME | WS_SYSMENU |
                  DS_3DLOOK | DS_MODALFRAME;
        cs.dwExStyle = WS_EX_WINDOWEDGE;
    }
    else if (m_chromeMask & nsIWebBrowserChrome::CHROME_WINDOW_POPUP) {
        cs.style = WS_OVERLAPPED | WS_POPUP;
        cs.dwExStyle = WS_EX_TOPMOST | WS_EX_TOOLWINDOW;
    }
    else {
        cs.style = WS_OVERLAPPEDWINDOW;
        cs.dwExStyle = WS_EX_WINDOWEDGE;
    }

    // Change window style based on the chromeMask

    if(! (m_chromeMask & nsIWebBrowserChrome::CHROME_TITLEBAR) )
        cs.style &= ~WS_CAPTION; // No caption        

    if(! (m_chromeMask & nsIWebBrowserChrome::CHROME_WINDOW_RESIZE) )
    {
        // Can't resize this window
        cs.style &= ~WS_SIZEBOX;
        cs.style &= ~WS_THICKFRAME;
        cs.style &= ~WS_MINIMIZEBOX;
        cs.style &= ~WS_MAXIMIZEBOX;
    }

    cs.lpszClass = AfxRegisterWndClass(0);

    return TRUE;
}

/////////////////////////////////////////////////////////////////////////////
// CBrowserFrame message handlers
void CBrowserFrame::OnSetFocus(CWnd* pOldWnd)
{
    // forward focus to the view window
    m_wndBrowserView.SetFocus();
}

BOOL CBrowserFrame::OnCmdMsg(UINT nID, int nCode, void* pExtra, AFX_CMDHANDLERINFO* pHandlerInfo)
{
    // let the view have first crack at the command
    if (m_wndBrowserView.OnCmdMsg(nID, nCode, pExtra, pHandlerInfo))
        return TRUE;

    // otherwise, do default handling
    return CFrameWnd::OnCmdMsg(nID, nCode, pExtra, pHandlerInfo);
}

// Needed to properly position/resize the progress bar
//
void CBrowserFrame::OnSize(UINT nType, int cx, int cy) 
{
    CFrameWnd::OnSize(nType, cx, cy);
    
    // Get the ItemRect of the status bar's Pane 1
    // That's where the progress bar will be located
    RECT rc;
    m_wndStatusBar.GetItemRect(1, &rc);
    
    // Move the progress bar into it's correct location
    //
    m_wndProgressBar.MoveWindow(&rc);
}

#ifdef _DEBUG
void CBrowserFrame::AssertValid() const
{
    CFrameWnd::AssertValid();
}

void CBrowserFrame::Dump(CDumpContext& dc) const
{
    CFrameWnd::Dump(dc);
}

#endif //_DEBUG


void CBrowserFrame::OnActivate(UINT nState, CWnd* pWndOther, BOOL bMinimized) 
{
    CFrameWnd::OnActivate(nState, pWndOther, bMinimized);
    
    m_wndBrowserView.Activate(nState, pWndOther, bMinimized);
}

#define IS_SECURE(state) ((state & 0xFFFF) == nsIWebProgressListener::STATE_IS_SECURE)
void CBrowserFrame::UpdateSecurityStatus(PRInt32 aState)
{
    int iResID = nsIWebProgressListener::STATE_IS_INSECURE;
    
    if(IS_SECURE(aState)){
        iResID = IDR_SECURITY_LOCK;
        m_wndBrowserView.m_SecurityState = CBrowserView::SECURITY_STATE_SECURE;
    }
    else if(aState == nsIWebProgressListener::STATE_IS_INSECURE) {
        iResID = IDR_SECURITY_UNLOCK;
        m_wndBrowserView.m_SecurityState = CBrowserView::SECURITY_STATE_INSECURE;
    }
    else if(aState == nsIWebProgressListener::STATE_IS_BROKEN) {
        iResID = IDR_SECURITY_BROKEN;
        m_wndBrowserView.m_SecurityState = CBrowserView::SECURITY_STATE_BROKEN;
    }

    CStatusBarCtrl& sb = m_wndStatusBar.GetStatusBarCtrl();
    sb.SetIcon(2, //2 is the pane index of the status bar where the lock icon will be shown
        (HICON)::LoadImage(AfxGetResourceHandle(),
        MAKEINTRESOURCE(iResID), IMAGE_ICON, 16,16,0));       
}

void CBrowserFrame::ShowSecurityInfo()
{   
    m_wndBrowserView.ShowSecurityInfo();
}

// CMyStatusBar Class
CMyStatusBar::CMyStatusBar()
{
}

CMyStatusBar::~CMyStatusBar()
{
}

BEGIN_MESSAGE_MAP(CMyStatusBar, CStatusBar)
    //{{AFX_MSG_MAP(CMyStatusBar)
    ON_WM_LBUTTONDOWN()
    //}}AFX_MSG_MAP
END_MESSAGE_MAP()

void CMyStatusBar::OnLButtonDown(UINT nFlags, CPoint point) 
{
    // Check to see if the mouse click was within the
    // padlock icon pane(at pane index 2) of the status bar...

    RECT rc;
    GetItemRect(2, &rc );

    if (PtInRect(&rc, point)) 
    {
        CBrowserFrame *pFrame = (CBrowserFrame *)GetParent();
        if (pFrame != NULL)
            pFrame->ShowSecurityInfo();
    }
        
    CStatusBar::OnLButtonDown(nFlags, point);
}

/////////////////////////////////////////////////////////////////////////////
// CBrowserFrame helpers

void CBrowserFrame::UpdateStatusBarText(const PRUnichar *aMessage)
{
    nsCString strStatus;

    if (aMessage)
        strStatus.AssignWithConversion(aMessage);

    m_wndStatusBar.SetPaneText(0, strStatus.get());
}

void CBrowserFrame::UpdateProgress(PRInt32 aCurrent, PRInt32 aMax)
{
    m_wndProgressBar.SetRange32(0, aMax);
    m_wndProgressBar.SetPos(aCurrent);
}

void CBrowserFrame::UpdateBusyState(PRBool aBusy)
{
    // Just notify the view of the busy state
    // There's code in there which will take care of
    // updating the STOP toolbar btn. etc

    m_wndBrowserView.UpdateBusyState(aBusy);
}

// Called from the OnLocationChange() method in the nsIWebProgressListener
// interface implementation in CBrowserImpl to update the current URI
// Will get called after a URI is successfully loaded in the browser
// We use this info to update the URL bar's edit box
//
void CBrowserFrame::UpdateCurrentURI(nsIURI *aLocation)
{
    if (aLocation)
    {
        nsCAutoString uriString;
        aLocation->GetSpec(uriString);

        m_wndUrlBar.SetCurrentURL(uriString.get());
    }
}

void CBrowserFrame::GetBrowserFrameTitle(PRUnichar **aTitle)
{
    CString title;
    GetWindowText(title);

    if (!title.IsEmpty())
    {
        nsString nsTitle;
        nsTitle.AssignWithConversion(title.GetBuffer(0));

        *aTitle = ToNewUnicode(nsTitle);
    }
}

void CBrowserFrame::SetBrowserFrameTitle(const PRUnichar *aTitle)
{
    USES_CONVERSION;

    if (W2T(aTitle))
    {
        SetWindowText(W2T(aTitle));
    }
    else
    {
        // Use the AppName i.e. mfcembed as the title if we
        // do not get one from GetBrowserWindowTitle()
        //
        CString cs;
        cs.LoadString(AFX_IDS_APP_TITLE);
        SetWindowText(cs);
    }
}

void CBrowserFrame::SetBrowserFrameSize(PRInt32 aCX, PRInt32 aCY)
{
    SetWindowPos(NULL, 0, 0, aCX, aCY,
                SWP_NOMOVE | SWP_NOACTIVATE | SWP_NOZORDER);
}

void CBrowserFrame::GetBrowserFrameSize(PRInt32 *aCX, PRInt32 *aCY)
{
    RECT wndRect;
    GetWindowRect(&wndRect);

    if (aCX)
        *aCX = wndRect.right - wndRect.left;

    if (aCY)
        *aCY = wndRect.bottom - wndRect.top;
}

void CBrowserFrame::SetBrowserFramePosition(PRInt32 aX, PRInt32 aY)
{
    SetWindowPos(NULL, aX, aY, 0, 0,
                SWP_NOSIZE | SWP_NOACTIVATE | SWP_NOZORDER);
}

void CBrowserFrame::GetBrowserFramePosition(PRInt32 *aX, PRInt32 *aY)
{
    RECT wndRect;
    GetWindowRect(&wndRect);

    if (aX)
        *aX = wndRect.left;

    if (aY)
        *aY = wndRect.top;
}

void CBrowserFrame::GetBrowserFramePositionAndSize(PRInt32 *aX, PRInt32 *aY, PRInt32 *aCX, PRInt32 *aCY)
{
    RECT wndRect;
    GetWindowRect(&wndRect);

    if (aX)
        *aX = wndRect.left;

    if (aY)
        *aY = wndRect.top;

    if (aCX)
        *aCX = wndRect.right - wndRect.left;

    if (aCY)
        *aCY = wndRect.bottom - wndRect.top;
}

void CBrowserFrame::SetBrowserFramePositionAndSize(PRInt32 aX, PRInt32 aY, PRInt32 aCX, PRInt32 aCY, PRBool fRepaint)
{
    SetWindowPos(NULL, aX, aY, aCX, aCY,
                SWP_NOACTIVATE | SWP_NOZORDER);
}

void CBrowserFrame::FocusAvailable(PRBool *aFocusAvail)
{
    HWND focusWnd = GetFocus()->m_hWnd;

    if ((focusWnd == m_hWnd) || ::IsChild(m_hWnd, focusWnd))
        *aFocusAvail = PR_TRUE;
    else
        *aFocusAvail = PR_FALSE;
}

void CBrowserFrame::ShowBrowserFrame(PRBool aShow)
{
    if (aShow)
    {
        ShowWindow(SW_SHOW);
        SetActiveWindow();
        UpdateWindow();
    }
    else
    {
        ShowWindow(SW_HIDE);
    }
}

void CBrowserFrame::GetBrowserFrameVisibility(PRBool *aVisible)
{
    // Is the current BrowserFrame the active one?
    if (GetActiveWindow()->m_hWnd != m_hWnd)
    {
        *aVisible = PR_FALSE;
        return;
    }

    // We're the active one
    //Return FALSE if we're minimized
    WINDOWPLACEMENT wpl;
    GetWindowPlacement(&wpl);

    if ((wpl.showCmd == SW_RESTORE) || (wpl.showCmd == SW_MAXIMIZE))
        *aVisible = PR_TRUE;
    else
        *aVisible = PR_FALSE;
}

PRBool CBrowserFrame::CreateNewBrowserFrame(
                            PRUint32 chromeMask,
                            PRInt32 x, PRInt32 y,
                            PRInt32 cx, PRInt32 cy,
                            nsIWebBrowser** aWebBrowser)
{
    NS_ENSURE_ARG_POINTER(aWebBrowser);

   *aWebBrowser = nsnull;

    MozEmbedApp *pApp = (MozEmbedApp *)AfxGetApp();
    if (!pApp)
        return PR_FALSE;

    // Note that we're calling with the last param set to "false" i.e.
    // this instructs not to show the frame window
    // This is mainly needed when the window size is specified in the window.open()
    // JS call. In those cases Gecko calls us to create the browser with a default
    // size (all are -1) and then it calls the SizeBrowserTo() method to set
    // the proper window size. If this window were to be visible then you'll see
    // the window size changes on the screen causing an unappealing flicker
    //

    CBrowserFrame* pFrm = pApp->CreateNewBrowserFrame(
        chromeMask, NULL, x, y, cx, cy, PR_FALSE);

    if (!pFrm)
        return PR_FALSE;

    // At this stage we have a new CBrowserFrame and a new CBrowserView
    // objects. The CBrowserView also would have an embedded browser
    // object created. Get the mWebBrowser member from the CBrowserView
    // and return it. (See CBrowserView's CreateBrowser() on how the
    // embedded browser gets created and how it's mWebBrowser member
    // gets initialized)

    NS_IF_ADDREF(*aWebBrowser = pFrm->m_wndBrowserView.mWebBrowser);

    return PR_TRUE;
}

void CBrowserFrame::DestroyBrowserFrame()
{
    PostMessage(WM_CLOSE);
}

#define GOTO_BUILD_CTX_MENU { bContentHasFrames = FALSE; goto BUILD_CTX_MENU; }

void CBrowserFrame::ShowContextMenu(PRUint32 aContextFlags, nsIContextMenuInfo *aInfo)
{
    BOOL bContentHasFrames = FALSE;
    UINT nIDResource = IDR_CTXMENU_DOCUMENT;

    // Reset the values from the last invocation
    // Clear image src & link url
    nsAutoString empty;
    m_wndBrowserView.SetCtxMenuImageSrc(empty);
    m_wndBrowserView.SetCtxMenuLinkUrl(empty);
    m_wndBrowserView.SetCurrentFrameURL(empty);

    if (aContextFlags & nsIContextMenuListener2::CONTEXT_DOCUMENT)
    {
        nIDResource = IDR_CTXMENU_DOCUMENT;

        // Background image?
        if (aContextFlags & nsIContextMenuListener2::CONTEXT_BACKGROUND_IMAGE)
        {
            // Get the IMG SRC
            nsCOMPtr<nsIURI> imgURI;
            aInfo->GetBackgroundImageSrc(getter_AddRefs(imgURI));
            if (!imgURI)
                return;
            nsCAutoString uri;
            imgURI->GetSpec(uri);

            m_wndBrowserView.SetCtxMenuImageSrc(NS_ConvertUTF8toUCS2(uri)); // Set the new Img Src
        }
    }
    else if (aContextFlags & nsIContextMenuListener2::CONTEXT_TEXT)
        nIDResource = IDR_CTXMENU_TEXT;
    else if (aContextFlags & nsIContextMenuListener2::CONTEXT_LINK)
    {
        nIDResource = IDR_CTXMENU_LINK;

        // Since we handle all the browser menu/toolbar commands
        // in the View, we basically setup the Link's URL in the
        // BrowserView object. When a menu selection in the context
        // menu is made, the appropriate command handler in the
        // BrowserView will be invoked and the value of the URL
        // will be accesible in the view

        nsAutoString strUrlUcs2;
        nsresult rv = aInfo->GetAssociatedLink(strUrlUcs2);
        if (NS_FAILED(rv))
            return;

        // Update the view with the new LinkUrl
        // Note that this string is in UCS2 format
        m_wndBrowserView.SetCtxMenuLinkUrl(strUrlUcs2);
    }
    else if (aContextFlags & nsIContextMenuListener2::CONTEXT_IMAGE)
    {
        nIDResource = IDR_CTXMENU_IMAGE;

        // Get the IMG SRC
        nsCOMPtr<nsIURI> imgURI;
        aInfo->GetImageSrc(getter_AddRefs(imgURI));
        if (!imgURI)
            return;
        nsCAutoString strImgSrcUtf8;
        imgURI->GetSpec(strImgSrcUtf8);
        if (strImgSrcUtf8.IsEmpty())
            return;

        // Set the new Img Src
        m_wndBrowserView.SetCtxMenuImageSrc(NS_ConvertUTF8toUCS2(strImgSrcUtf8));
    }

    // Determine if we need to add the Frame related context menu items
    // such as "View Frame Source" etc.
    //
    if (m_wndBrowserView.ViewContentContainsFrames())
    {
        bContentHasFrames = TRUE;

        //Determine the current Frame URL
        //
        nsresult rv = NS_OK;
        nsCOMPtr<nsIDOMNode> node;
        aInfo->GetTargetNode(getter_AddRefs(node));
        if (!node)
            GOTO_BUILD_CTX_MENU;

        nsCOMPtr<nsIDOMDocument> domDoc;
        rv = node->GetOwnerDocument(getter_AddRefs(domDoc));
        if (NS_FAILED(rv))
            GOTO_BUILD_CTX_MENU;

        nsCOMPtr<nsIDOMHTMLDocument> htmlDoc(do_QueryInterface(domDoc, &rv));
        if (NS_FAILED(rv))
            GOTO_BUILD_CTX_MENU;

        nsAutoString strFrameURL;
        rv = htmlDoc->GetURL(strFrameURL);
        if (NS_FAILED(rv))
            GOTO_BUILD_CTX_MENU;

        m_wndBrowserView.SetCurrentFrameURL(strFrameURL); //Set it to the new URL
    }

BUILD_CTX_MENU:

    CMenu ctxMenu;
    if (ctxMenu.LoadMenu(nIDResource))
    {
        //Append the Frame related menu items if content has frames
        if (bContentHasFrames)
        {
            CMenu* pCtxMenu = ctxMenu.GetSubMenu(0);
            if (pCtxMenu)
            {
                pCtxMenu->AppendMenu(MF_SEPARATOR);

                CString strMenuItem;
                strMenuItem.LoadString(IDS_VIEW_FRAME_SOURCE);
                pCtxMenu->AppendMenu(MF_STRING, ID_VIEW_FRAME_SOURCE, strMenuItem);

                strMenuItem.LoadString(IDS_OPEN_FRAME_IN_NEW_WINDOW);
                pCtxMenu->AppendMenu(MF_STRING, ID_OPEN_FRAME_IN_NEW_WINDOW, strMenuItem);
            }
        }

        POINT cursorPos;
        GetCursorPos(&cursorPos);

        (ctxMenu.GetSubMenu(0))->TrackPopupMenu(TPM_LEFTALIGN, cursorPos.x, cursorPos.y, this);
    }
}

void CBrowserFrame::ShowTooltip(PRInt32 aXCoords, PRInt32 aYCoords, const PRUnichar *aTipText)
{
    m_wndTooltip.SetTipText(CString(aTipText));
    m_wndTooltip.Show(&m_wndBrowserView, aXCoords, aYCoords);
}

void CBrowserFrame::HideTooltip()
{
    m_wndTooltip.Hide();
}
