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
// This is the class which implements all the interfaces
// required(and optional) by the mozilla embeddable browser engine
//
// Note that this obj gets passed in the IBrowserFrameGlue* using the
// Init() method. Many of the interface implementations use this
// to get the actual task done. Ex: to update the status bar
//
// Look at the INTERFACE_MAP_ENTRY's below for the list of
// the currently implemented interfaces
//
// This file currently has the implementation for all the interfaces
// which are required of an app embedding Gecko
// Implementation of other optional interfaces are in separate files
// to avoid cluttering this one and to demonstrate other embedding
// principles. For example, nsIPrompt is implemented in a separate DLL.
//
//    nsIWebBrowserChrome    - This is a required interface to be implemented
//                          by embeddors
//
//    nsIEmbeddingSiteWindow - This is a required interface to be implemented
//                         by embedders
//                       - SetTitle() gets called after a document
//                         load giving us the chance to update our
//                         titlebar
//
// Some points to note...
//
// nsIWebBrowserChrome interface's SetStatus() gets called when a user
// mouses over the links on a page
//
// nsIWebProgressListener interface's OnStatusChange() gets called
// to indicate progress when a page is being loaded
//
// Next suggested file(s) to look at :
//            Any of the BrowserImpl*.cpp files for other interface
//            implementation details
//

#include "stdafx.h"
#include "nsICategoryManager.h"
#include "nsIDOMWindow.h"
#include "BrowserImpl.h"
#include "MozEmbed.h"
#include "Util.h"

CBrowserImpl::CBrowserImpl()
{
    m_pBrowserFrame = NULL;
    mWebBrowser = nsnull;
    mIsModal = PR_FALSE;
}

CBrowserImpl::~CBrowserImpl()
{
}

// It's very important that the creator of this CBrowserImpl object
// Call on this Init() method with proper values after creation
//
NS_METHOD CBrowserImpl::Init(CBrowserFrame* pBrowserFrameGlue,
                             nsIWebBrowser* aWebBrowser)
{
    m_pBrowserFrame = pBrowserFrameGlue;
    SetWebBrowser(aWebBrowser);
    return NS_OK;
}

//*****************************************************************************
// CBrowserImpl::nsISupports
//*****************************************************************************

NS_IMPL_ADDREF(CBrowserImpl)
NS_IMPL_RELEASE(CBrowserImpl)

NS_INTERFACE_MAP_BEGIN(CBrowserImpl)
   NS_INTERFACE_MAP_ENTRY_AMBIGUOUS(nsISupports, nsIWebBrowserChrome)
   NS_INTERFACE_MAP_ENTRY(nsIInterfaceRequestor)
   NS_INTERFACE_MAP_ENTRY(nsIWebBrowserChrome)
   NS_INTERFACE_MAP_ENTRY(nsIWebBrowserChromeFocus)
   NS_INTERFACE_MAP_ENTRY(nsIEmbeddingSiteWindow)
   NS_INTERFACE_MAP_ENTRY(nsIEmbeddingSiteWindow2)
   NS_INTERFACE_MAP_ENTRY(nsIWebProgressListener)
   NS_INTERFACE_MAP_ENTRY(nsIURIContentListener)
   NS_INTERFACE_MAP_ENTRY(nsIContextMenuListener2)
   NS_INTERFACE_MAP_ENTRY(nsITooltipListener)
   NS_INTERFACE_MAP_ENTRY(nsISupportsWeakReference)
NS_INTERFACE_MAP_END

//*****************************************************************************
// CBrowserImpl::nsIInterfaceRequestor
//*****************************************************************************

NS_IMETHODIMP CBrowserImpl::GetInterface(const nsIID &aIID, void** aInstancePtr)
{
    if (aIID.Equals(NS_GET_IID(nsIDOMWindow)))
    {
        if (mWebBrowser)
            return mWebBrowser->GetContentDOMWindow((nsIDOMWindow **) aInstancePtr);
        return NS_ERROR_NOT_INITIALIZED;
    }

    return QueryInterface(aIID, aInstancePtr);
}

//*****************************************************************************
// CBrowserImpl::nsIWebBrowserChrome
//*****************************************************************************

//Gets called when you mouseover links etc. in a web page
//
NS_IMETHODIMP CBrowserImpl::SetStatus(PRUint32 aType, const PRUnichar* aStatus)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->UpdateStatusBarText(aStatus);

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::GetWebBrowser(nsIWebBrowser** aWebBrowser)
{
   NS_ENSURE_ARG_POINTER(aWebBrowser);

   *aWebBrowser = mWebBrowser;

   NS_IF_ADDREF(*aWebBrowser);

   return NS_OK;
}

// Currently called from Init(). I'm not sure who else
// calls this
//
NS_IMETHODIMP CBrowserImpl::SetWebBrowser(nsIWebBrowser* aWebBrowser)
{
   NS_ENSURE_ARG_POINTER(aWebBrowser);

   mWebBrowser = aWebBrowser;

   return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::GetChromeFlags(PRUint32* aChromeMask)
{
   return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP CBrowserImpl::SetChromeFlags(PRUint32 aChromeMask)
{
   return NS_ERROR_NOT_IMPLEMENTED;
}

// Will get called in response to JavaScript window.close()
//
NS_IMETHODIMP CBrowserImpl::DestroyBrowserWindow()
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->DestroyBrowserFrame();

    return NS_OK;
}

// Gets called in response to set the size of a window
// Ex: In response to a JavaScript Window.Open() call of
// the form
//
//        window.open("http://www.mozilla.org", "theWin", "width=200, height=400");
//
// First the CreateBrowserWindow() call will be made followed by a
// call to this method to resize the window
//
NS_IMETHODIMP CBrowserImpl::SizeBrowserTo(PRInt32 aCX, PRInt32 aCY)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    HWND w = m_pBrowserFrame->GetSafeHwnd();

    CRect rcNewFrame(CPoint(0, 0), CSize(aCX, aCY));
    CRect rcFrame;
    CRect rcClient;

    // Adjust for 3D edge on client area
    AdjustWindowRectEx(&rcNewFrame, WS_VISIBLE, FALSE, WS_EX_CLIENTEDGE);

    GetClientRect(w, &rcClient);
    GetWindowRect(w, &rcFrame);

    rcNewFrame.right += rcFrame.Width() - rcClient.Width();
    rcNewFrame.bottom += rcFrame.Height() - rcClient.Height();

    m_pBrowserFrame->SetBrowserFrameSize(rcNewFrame.Width(), rcNewFrame.Height());

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::ShowAsModal(void)
{
    HWND h = m_pBrowserFrame->GetSafeHwnd();
    MSG msg;
    HANDLE hFakeEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
    bool aRunCondition = true;
    while (aRunCondition) {
        // Process pending messages
        while (PeekMessage(&msg, NULL, 0, 0, PM_NOREMOVE)) {
            if (msg.hwnd == h) {
                if (msg.message == WM_CLOSE) {
                    aRunCondition = PR_FALSE;
                    break;
                }
            }
         
            if (!GetMessage(&msg, NULL, 0, 0)) {
                // WM_QUIT
                aRunCondition = PR_FALSE;
                break;
            }
                             
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
      
        // Do idle stuff
        MsgWaitForMultipleObjects(1, &hFakeEvent, FALSE, 100, QS_ALLEVENTS);
    }
    CloseHandle(hFakeEvent);
    return msg.wParam;
}

NS_IMETHODIMP CBrowserImpl::IsWindowModal(PRBool *retval)
{   
    *retval = PR_FALSE;
    return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP CBrowserImpl::ExitModalEventLoop(nsresult aStatus)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

//*****************************************************************************
// CBrowserImpl::nsIWebBrowserChromeFocus
//*****************************************************************************

NS_IMETHODIMP CBrowserImpl::FocusNextElement()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP CBrowserImpl::FocusPrevElement()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

//*****************************************************************************
// CBrowserImpl::nsIEmbeddingSiteWindow
//*****************************************************************************

NS_IMETHODIMP CBrowserImpl::SetDimensions(PRUint32 aFlags, PRInt32 x, PRInt32 y, PRInt32 cx, PRInt32 cy)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    if (aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_POSITION &&
        (aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_SIZE_INNER ||
         aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_SIZE_OUTER))
    {
        m_pBrowserFrame->SetBrowserFramePositionAndSize(x, y, cx, cy, PR_TRUE);
    }
    else
    {
        if (aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_POSITION)
        {
            m_pBrowserFrame->SetBrowserFramePosition(x, y);
        }
        if (aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_SIZE_INNER ||
            aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_SIZE_OUTER)
        {
            m_pBrowserFrame->SetBrowserFrameSize(cx, cy);
        }
    }

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::GetDimensions(PRUint32 aFlags, PRInt32 *x, PRInt32 *y, PRInt32 *cx, PRInt32 *cy)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    if (aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_POSITION)
    {
        m_pBrowserFrame->GetBrowserFramePosition(x, y);
    }
    if (aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_SIZE_INNER ||
        aFlags & nsIEmbeddingSiteWindow::DIM_FLAGS_SIZE_OUTER)
    {
        m_pBrowserFrame->GetBrowserFrameSize(cx, cy);
    }

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::GetSiteWindow(void** aSiteWindow)
{
    if (!aSiteWindow)
        return NS_ERROR_NULL_POINTER;

    *aSiteWindow = 0;
    if (m_pBrowserFrame) {
        HWND w = m_pBrowserFrame->GetSafeHwnd();
        *aSiteWindow = NS_REINTERPRET_CAST(void *, w);
    }
    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::SetFocus()
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->SetFocus();

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::GetTitle(PRUnichar** aTitle)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->GetBrowserFrameTitle(aTitle);

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::SetTitle(const PRUnichar* aTitle)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->SetBrowserFrameTitle(aTitle);

	char buf[1024];
    PRInt32 id = m_pBrowserFrame->GetBrowserId();
    sprintf(buf, "%s", NS_ConvertUCS2toUTF8(aTitle).get());
    SendSocketMessage(id, CEVENT_TITLE_CHANGE, buf);

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::GetVisibility(PRBool *aVisibility)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->GetBrowserFrameVisibility(aVisibility);

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::SetVisibility(PRBool aVisibility)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->ShowBrowserFrame(aVisibility);

    return NS_OK;
}

//*****************************************************************************
// CBrowserImpl::nsIEmbeddingSiteWindow2
//*****************************************************************************

NS_IMETHODIMP CBrowserImpl::Blur()
{
    return NS_OK;
}


//*****************************************************************************
// CBrowserImpl::nsITooltipListener
//*****************************************************************************

/* void onShowTooltip (in long aXCoords, in long aYCoords, in wstring aTipText); */
NS_IMETHODIMP CBrowserImpl::OnShowTooltip(PRInt32 aXCoords, PRInt32 aYCoords, const PRUnichar *aTipText)
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->ShowTooltip(aXCoords, aYCoords, aTipText);

    return NS_OK;
}

/* void onHideTooltip (); */
NS_IMETHODIMP CBrowserImpl::OnHideTooltip()
{
    if (! m_pBrowserFrame || gQuitMode)
        return NS_ERROR_FAILURE;

    m_pBrowserFrame->HideTooltip();

    return NS_OK;
}

//*****************************************************************************
// CBrowserImpl::nsIWebProgressListener Implementation
//*****************************************************************************
//
// - Implements browser progress update functionality
//   while loading a page into the embedded browser
//
// - Calls methods via the IBrowserFrameGlue interace
//    (available thru' the m_pBrowserFrame member var)
//    to do the actual statusbar/progress bar updates.
//

NS_IMETHODIMP CBrowserImpl::OnProgressChange(nsIWebProgress *progress,
                                             nsIRequest *request,
                                             PRInt32 curSelfProgress,
                                             PRInt32 maxSelfProgress,
                                             PRInt32 curTotalProgress,
                                             PRInt32 maxTotalProgress)
{
    if (! m_pBrowserFrame || gQuitMode) {
        // always return NS_OK
        return NS_OK;
    }

    PRInt32 nProgress = curTotalProgress;
    PRInt32 nProgressMax = maxTotalProgress;

    if (nProgressMax == 0)
        nProgressMax = LONG_MAX;

    if (nProgress > nProgressMax)
        nProgress = nProgressMax; // Progress complete

    m_pBrowserFrame->UpdateProgress(nProgress, nProgressMax);

    char buf[20];
    PRInt32 id = m_pBrowserFrame->GetBrowserId();
    sprintf(buf, "%d", nProgress * 100 / nProgressMax);
    SendSocketMessage(id, CEVENT_DOWNLOAD_PROGRESS, buf);

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::OnStateChange(nsIWebProgress *progress,
                                          nsIRequest *request,
                                          PRUint32 progressStateFlags,
                                          nsresult status)
{
    if (! m_pBrowserFrame || gQuitMode) {
        // always return NS_OK
        return NS_OK;
    }

    PRInt32 id = m_pBrowserFrame->GetBrowserId();
    if (progressStateFlags & STATE_IS_DOCUMENT) {
        if (progressStateFlags & STATE_START) {
            // Navigation has begun
            m_pBrowserFrame->UpdateBusyState(PR_TRUE);

            // Fire a DownloadBegin event
            SendSocketMessage(id, CEVENT_DOWNLOAD_STARTED);
        }

        else if (progressStateFlags & STATE_STOP) {
            nsCOMPtr<nsIWebBrowserFocus> focus(do_GetInterface(mWebBrowser));
            if (focus)
                focus->Activate();

            // We've completed the navigation
            m_pBrowserFrame->UpdateBusyState(PR_FALSE);
            m_pBrowserFrame->UpdateProgress(0, 100);       // Clear the prog bar
            m_pBrowserFrame->UpdateStatusBarText(nsnull);  // Clear the status bar

            // Fire a DownloadComplete event
            SendSocketMessage(id, CEVENT_DOWNLOAD_COMPLETED);
        }
    }

    if (progressStateFlags & STATE_IS_NETWORK) {
        if (progressStateFlags & STATE_START) {
        }
        else if (progressStateFlags & STATE_STOP) {
            // Fire a DocumentComplete event, the document finishes loading.
            SendSocketMessage(id, CEVENT_DOCUMENT_COMPLETED);

            // Fire a CommandStateChange event
            char buf[20];
            nsCOMPtr<nsIWebNavigation> webNav(do_QueryInterface(mWebBrowser));

            PRBool aCanGoForward = PR_FALSE;
            webNav->GetCanGoForward(&aCanGoForward);
            sprintf(buf, "forward=%d", aCanGoForward ? 1 : 0);
            SendSocketMessage(id, CEVENT_COMMAND_STATE_CHANGE, buf);

            PRBool aCanGoBack = PR_FALSE;
            webNav->GetCanGoBack(&aCanGoBack);
            sprintf(buf, "back=%d", aCanGoBack ? 1 : 0);
            SendSocketMessage(id, CEVENT_COMMAND_STATE_CHANGE, buf);
        }
    }

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::OnLocationChange(nsIWebProgress* aWebProgress,
                                                 nsIRequest* aRequest,
                                                 nsIURI *location)
{
    if (! m_pBrowserFrame || gQuitMode) {
        // always return NS_OK
        return NS_OK;
    }

    PRBool isSubFrameLoad = PR_FALSE; // Is this a subframe load
    if (aWebProgress) {
        nsCOMPtr<nsIDOMWindow>  domWindow;
        nsCOMPtr<nsIDOMWindow>  topDomWindow;
        aWebProgress->GetDOMWindow(getter_AddRefs(domWindow));
        if (domWindow) { // Get root domWindow
            domWindow->GetTop(getter_AddRefs(topDomWindow));
        }
        if (domWindow != topDomWindow)
            isSubFrameLoad = PR_TRUE;

    }

    if (!isSubFrameLoad) // Update urlbar only if it is not a subframe load
        m_pBrowserFrame->UpdateCurrentURI(location);

    return NS_OK;
}

NS_IMETHODIMP
CBrowserImpl::OnStatusChange(nsIWebProgress* aWebProgress,
                                 nsIRequest* aRequest,
                                 nsresult aStatus,
                                 const PRUnichar* aMessage)
{
    if (m_pBrowserFrame) {
        m_pBrowserFrame->UpdateStatusBarText(aMessage);

        char buf[1024];
        PRInt32 id = m_pBrowserFrame->GetBrowserId();
        sprintf(buf, "%s", NS_ConvertUCS2toUTF8(aMessage).get());
        SendSocketMessage(id, CEVENT_STATUSTEXT_CHANGE, buf);
    }

    return NS_OK;
}

NS_IMETHODIMP
CBrowserImpl::OnSecurityChange(nsIWebProgress *aWebProgress,
                                    nsIRequest *aRequest,
                                    PRUint32 state)
{
    m_pBrowserFrame->UpdateSecurityStatus(state);

    return NS_OK;
}


//*****************************************************************************
// CBrowserImpl::nsIURIContentListener Implementation
//*****************************************************************************
NS_IMETHODIMP CBrowserImpl::OnStartURIOpen(nsIURI *pURI, PRBool *aAbortOpen)
{
    *aAbortOpen = PR_FALSE;

    nsCAutoString uriString;
    nsresult rv = pURI->GetAsciiSpec(uriString);
    if (NS_FAILED(rv))
        return NS_ERROR_ABORT;

    PRInt32 id = m_pBrowserFrame->GetBrowserId();
    if (id >= 0) {
        // native browser needs a yes or no confirmation from the 
        // Java side for event CEVENT_BEFORE_NAVIGATE.
        int bCmdCanceled = -1, waitCount = 0;
        AddTrigger(id, CEVENT_BEFORE_NAVIGATE, &bCmdCanceled);
        SendSocketMessage(id, CEVENT_BEFORE_NAVIGATE, uriString.get());

        while (bCmdCanceled < 0 && waitCount++ < MAX_WAIT) {
            Sleep(1);
        }
        WBTRACE("waitCount = %d\n", waitCount);

        if (bCmdCanceled == 1) {
            *aAbortOpen = PR_TRUE;
            return NS_ERROR_ABORT;
        }
    }

    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::DoContent(const char *aContentType, PRBool aIsContentPreferred, nsIRequest *request, nsIStreamListener **aContentHandler, PRBool *aAbortProcess)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP CBrowserImpl::IsPreferred(const char *aContentType, char **aDesiredContentType, PRBool *_retval)
{
    return CanHandleContent(aContentType, PR_TRUE, aDesiredContentType, _retval);
}

NS_IMETHODIMP CBrowserImpl::CanHandleContent(const char *aContentType, PRBool aIsContentPreferred, char **aDesiredContentType, PRBool *_retval)
{
    if (aContentType)
    {
        nsCOMPtr<nsICategoryManager> catMgr;
        nsresult rv;
        catMgr = do_GetService(NS_CATEGORYMANAGER_CONTRACTID, &rv);
        nsXPIDLCString value;
        rv = catMgr->GetCategoryEntry("Gecko-Content-Viewers",
            aContentType,
            getter_Copies(value));

        // If the category manager can't find what we're looking for
        // it returns NS_ERROR_NOT_AVAILABLE, we don't want to propagate
        // that to the caller since it's really not a failure

        if (NS_FAILED(rv) && rv != NS_ERROR_NOT_AVAILABLE)
            return rv;

        if (value && *value)
            *_retval = PR_TRUE;
    }
    return NS_OK;
}

NS_IMETHODIMP CBrowserImpl::GetLoadCookie(nsISupports * *aLoadCookie)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP CBrowserImpl::SetLoadCookie(nsISupports * aLoadCookie)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP CBrowserImpl::GetParentContentListener(nsIURIContentListener * *aParentContentListener)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

NS_IMETHODIMP CBrowserImpl::SetParentContentListener(nsIURIContentListener * aParentContentListener)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

//*****************************************************************************
// CBrowserImpl::nsIContextMenuListener2
//*****************************************************************************

NS_IMETHODIMP CBrowserImpl::OnShowContextMenu(PRUint32 aContextFlags, nsIContextMenuInfo *aInfo)
{
    if(m_pBrowserFrame)
        m_pBrowserFrame->ShowContextMenu(aContextFlags, aInfo);

    return NS_OK;
}
