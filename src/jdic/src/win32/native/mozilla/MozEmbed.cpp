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
 *   Conrad Carlen <ccarlen@netscape.com>
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
// The typical MFC app, frame creation code + AboutDlg handling
//
// NS_InitEmbedding() is called in InitInstance()
// 
// NS_TermEmbedding() is called in ExitInstance()
// ExitInstance() also takes care of cleaning up of
// multiple browser frame windows on app exit
//
// Code to handle the creation of a new browser window

// Next suggested file to look at : BrowserFrm.cpp

// Local Includes
#include "stdafx.h"
#include "MozEmbed.h"
#include "BrowserFrm.h"
#include "BrowserImpl.h"
#include "nsIWindowWatcher.h"
#include "nsIProfile.h"
#include "plstr.h"
#include "nsCRT.h"
#include <io.h>
#include <fcntl.h>
#include "prthread.h"
#include "nsXPCOMGlue.h"
#include "Common.h"
#include "nsEmbedString.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

#ifdef WIN32
#include <windows.h>
#endif
#include <stdlib.h>

// this is for overriding the Mozilla default PromptService component
#include "nsIComponentRegistrar.h"
#include "PromptService.h"

int gTestMode = FALSE;
int gQuitMode = FALSE;

class MozEmbedCommandLine : public CCommandLineInfo
{
public:

    MozEmbedCommandLine(MozEmbedApp& app) : CCommandLineInfo(),
                                              mApp(app)
    {
    }

    // generic parser which bundles up flags and their parameters, to
    // pass to HandleFlag() or HandleNakedParameter()
    // if you're adding new parameters, please don't touch this
    // function and instead add your own handler below
    virtual void ParseParam(LPCTSTR szParam, BOOL bFlag, BOOL bLast)
    {
        CCommandLineInfo::ParseParam(szParam, bFlag, bLast);
        if (bFlag) {
            // advance past extra stuff like --foo
            while (*szParam && *szParam == '-')
                szParam++;

            // previous argument was a flag too, so process that first
            if (!mLastFlag.IsEmpty())
                HandleFlag(mLastFlag);
            
            mLastFlag = szParam;

            // oops, no more arguments coming, so handle this now
            if (bLast)
                HandleFlag(mLastFlag);
            
        } else {
            if (!mLastFlag.IsEmpty())
                HandleFlag(mLastFlag, szParam);
                
            mLastFlag.Truncate();
        }
    }

    // handle flag-based parameters
    void HandleFlag(const nsACString& flag, const char* param=nsnull)
    {
        if (flag.Equals("console")) {
            DoConsole();
        }
        else if (flag.Equals("test")) {
            gTestMode = TRUE;
        }
        else if (Substring(flag, 0, 4).Equals("port")) {
            gMessenger.SetPort(atoi(PromiseFlatCString(Substring(flag, 5, flag.Length() - 5)).get()));
        }
        // add new flag handlers here (please add a DoFoo() method below!)
    }

    void HandleNakedParameter(const char* flag) {
        // handle non-flag arguments here
    }

    // add your specific handlers here
    void DoConsole() {
        mApp.ShowDebugConsole();
    }

private:
    // autostring is fine, this is a stack based object anyway
    nsCAutoString mLastFlag;

    MozEmbedApp& mApp;
};

BEGIN_MESSAGE_MAP(MozEmbedApp, CWinApp)
    //{{AFX_MSG_MAP(MozEmbedApp)
    ON_COMMAND(ID_NEW_BROWSER, OnNewBrowser)
    // NOTE - the ClassWizard will add and remove mapping macros here.
    //    DO NOT EDIT what you see in these blocks of generated code!
    //}}AFX_MSG_MAP
END_MESSAGE_MAP()

MozEmbedApp theApp;

void SocketMsgHandler(const char *pMsg);

MozEmbedApp::MozEmbedApp()
{
    mRefCnt = 1; // Start at one - nothing is going to addref this object
    mInitFailed = FALSE;
}

MozEmbedApp::~MozEmbedApp()
{
}

void MozEmbedApp::ShowDebugConsole()
{
#ifdef _DEBUG
    // Show console only in debug mode

    if (! AllocConsole())
        return;

    // Redirect stdout to the console
    int hCrtOut = _open_osfhandle(
                (long) GetStdHandle(STD_OUTPUT_HANDLE),
                _O_TEXT);
    if (hCrtOut == -1)
        return;

    FILE *hfOut = _fdopen(hCrtOut, "w");
    if (hfOut != NULL)
    {
        // Setup for unbuffered I/O so the console 
        // output shows up right away
        *stdout = *hfOut;
        setvbuf(stdout, NULL, _IONBF, 0); 
    }

    // Redirect stderr to the console
    int hCrtErr = _open_osfhandle(
                (long) GetStdHandle(STD_ERROR_HANDLE),
                _O_TEXT);
    if (hCrtErr == -1)
        return;

    FILE *hfErr = _fdopen(hCrtErr, "w");
    if (hfErr != NULL)
    {
        // Setup for unbuffered I/O so the console 
        // output shows up right away
        *stderr = *hfErr;
        setvbuf(stderr, NULL, _IONBF, 0); 
    }
#endif
}

BOOL MozEmbedApp::InitInstance()
{
    MozEmbedCommandLine cmdLine(*this);
    ParseCommandLine(cmdLine);
    
    Enable3dControls();

    if (! gTestMode) {
        gMessenger.CreateServerSocket();
    
        PRThread *socketListenThread = nsnull;

        if (!CreateHiddenWindow())
            return FALSE;

        socketListenThread = PR_CreateThread(PR_USER_THREAD,
                                            PortListening,
                                            SocketMsgHandler,
                                            PR_PRIORITY_NORMAL,
                                            PR_GLOBAL_THREAD,
                                            PR_UNJOINABLE_THREAD,
                                            0);
        if (!socketListenThread)
            return FALSE;
    }
    else {
        MessageReceived("0,0,");
        CBrowserFrame *pBrowserFrame = CreateNewBrowserFrame(
            nsIWebBrowserChrome::CHROME_ALL);
        m_pMainWnd = pBrowserFrame;
        pBrowserFrame->m_wndBrowserView.OpenURL("about:blank");
    }

    return TRUE;
}

int MozEmbedApp::ExitInstance()
{
    if (m_pMainWnd)
        m_pMainWnd->DestroyWindow();

    NS_ShutdownXPCOM(nsnull);
    XPCOMGlueShutdown();

    return 1;
}

BOOL MozEmbedApp::PreTranslateMessage(MSG* pMsg) 
{
    if (pMsg->message == WM_SOCKET_MSG) {
        char *p = (char *)pMsg->lParam;
        if (p) {
            WBTRACE("Event from socket: %s\n", p);
            MessageReceived(p);
        }
        else {
            WBTRACE("Wrong Event\n");
        }
        delete p;
    }

    return CWinApp::PreTranslateMessage(pMsg);
}

CBrowserFrame* MozEmbedApp::CreateNewBrowserFrame(PRUint32 chromeMask,
                                                  HWND hParent,
                                                  PRInt32 x, PRInt32 y,
                                                  PRInt32 cx, PRInt32 cy,
                                                  PRBool bShowWindow)
{
    UINT resId = IDR_MAINFRAME;

    // Setup a CRect with the requested window dimensions
    CRect winSize(x, y, cx, cy);

    // Use the Windows default if all are specified as -1
    if (x == -1 && y == -1 && cx == -1 && cy == -1)
        winSize = CFrameWnd::rectDefault;

    // Load the window title from the string resource table
    CString strTitle;
    strTitle.LoadString(IDR_MAINFRAME);

    // Now, create the browser frame
    CBrowserFrame* pFrame = new CBrowserFrame(chromeMask);

    if (!pFrame->Create(NULL, strTitle, WS_OVERLAPPEDWINDOW, 
        winSize, CWnd::FromHandle(hParent), MAKEINTRESOURCE(resId), 0L, NULL))
    {
        delete pFrame;
        return NULL;
    }

    // load accelerator resource
    pFrame->LoadAccelTable(MAKEINTRESOURCE(IDR_MAINFRAME));

    // Show the window...
    if (bShowWindow)
    {
        pFrame->ShowWindow(SW_SHOW);
        pFrame->UpdateWindow();
    }

    return pFrame;
}

CBrowserFrame* MozEmbedApp::CreateEmbeddedBrowserFrame(HWND hParent)
{
    ASSERT(hParent != NULL);
    CBrowserFrame *pBrowserFrame = CreateNewBrowserFrame(
        nsIWebBrowserChrome::CHROME_DEFAULT, hParent, 0, 0, 0, 0);
    return pBrowserFrame;
}

void MozEmbedApp::OnNewBrowser()
{
    CreateNewBrowserFrame(nsIWebBrowserChrome::CHROME_ALL);
}

// Check the GRE directory path if Mozilla is set as the default browser.
// XXX Below code is partly duplicate of WebBrowserUtil.cpp, which needs to 
// refactored somehow.
PRBool GetGREPath(char* pathBuf, size_t pathBufSize)
{	
	//must have been set to the right path
	char * greHome="MOZILLA_FIVE_HOME";
	char * pEnvValue = getenv(greHome);
	if(!pEnvValue) {
	 //shound'n get here
		LogMsg("MOZILLA_FIVE_HOME isn't set!");
		return PR_FALSE;
	}	
	strncpy(pathBuf,pEnvValue,pathBufSize);
	LogMsg("greHome got:");
	LogMsg(pathBuf);	
	return PR_TRUE;
}

BOOL MozEmbedApp::InitMozilla()
{   
    nsresult rv;

    TCHAR greDirPath[_MAX_PATH+1] = "\0";
    TCHAR xpcomFilePath[_MAX_PATH+1] = "\0";   
    
    if (!GetGREPath(greDirPath, sizeof(greDirPath))) {
        LogMsg("GetGREPath failed: can't locate the GRE path of the \
                     installed Mozilla binary!");
        return FALSE;
    }	
    strcpy(xpcomFilePath, greDirPath);
    strcat(xpcomFilePath, "\\xpcom.dll");
	LogMsg("xpcomFilePath is:");
	LogMsg(xpcomFilePath);

    rv = XPCOMGlueStartup(xpcomFilePath);	
    if (NS_FAILED(rv)) {
        LogMsg("XPCOMGlueStartup failed!");
        return FALSE;
    }

    nsCOMPtr<nsILocalFile> greDir;
    rv = NS_NewNativeLocalFile(nsEmbedCString(T2A(greDirPath)), TRUE, getter_AddRefs(greDir));
    if (NS_FAILED(rv)) {
        LogMsg("NS_NewNativeLocalFile failed!");
        return FALSE;
    }

    rv = NS_InitXPCOM2(nsnull, greDir, nsnull); 
    if (NS_FAILED(rv)) {
        LogMsg("NS_InitXPCOM2 failed!");
        return FALSE;
    }

    rv = OverrideComponents();
    if (NS_FAILED(rv))
    {
        LogMsg("OverrideComponents failed!");
        return FALSE;
    }

    rv = InitializeWindowCreator();
    if (NS_FAILED(rv))
    {
        LogMsg("InitializeWindowCreator failed!");
        return FALSE;
    }

    rv = InitializeProfile();
    if (NS_FAILED(rv))
    {
        LogMsg("InitializeProfiles failed!");
        return FALSE;
    }

    return TRUE;
}

// When the profile switch happens, all open browser windows need to be 
// closed. 
// In order for that not to kill off the app, we just make the MFC app's 
// mainframe be an invisible window which doesn't get closed on profile 
// switches
BOOL MozEmbedApp::CreateHiddenWindow()
{
    CFrameWnd *hiddenWnd = new CFrameWnd;
    if (!hiddenWnd)
        return FALSE;

    RECT bounds = { -10010, -10010, -10000, -10000 };
    hiddenWnd->Create(NULL, _T("main"), WS_DISABLED, bounds, NULL, NULL, 0, NULL);
    m_pMainWnd = hiddenWnd;

    return TRUE;
}

#define NS_PROMPTSERVICE_CID \
    {0xa2112d6a, 0x0e28, 0x421f, {0xb4, 0x6a, 0x25, 0xc0, 0xb3, 0x8, 0xcb, 0xd0}}

NS_GENERIC_FACTORY_CONSTRUCTOR(CPromptService);

/* Some Gecko interfaces are implemented as components, automatically
   registered at application initialization. nsIPrompt is an example:
   the default implementation uses XUL, not native windows. Embedding
   apps can override the default implementation by implementing the
   nsIPromptService interface and registering a factory for it with
   the same CID and Contract ID as the default's.

   Note that this example implements the service in a separate DLL,
   replacing the default if the override DLL is present. This could
   also have been done in the same module, without a separate DLL.
   See the PowerPlant example for, well, an example.
*/
nsresult MozEmbedApp::OverrideComponents()
{
    nsresult rv = NS_OK;

    static const nsModuleComponentInfo components[] = 
    {
        {
            "Prompt Service",
            NS_PROMPTSERVICE_CID,
            "@mozilla.org/embedcomp/prompt-service;1",
            CPromptServiceConstructor
        }
    };

    InitPromptService(m_hInstance);

    nsCOMPtr<nsIComponentRegistrar> cr;
    NS_GetComponentRegistrar(getter_AddRefs(cr));
    if (!cr)
        return NS_ERROR_FAILURE;

    int numComponents = sizeof(components) / sizeof(components[0]);
    for (int i = 0; i < numComponents; ++i) {
        nsCOMPtr<nsIGenericFactory> componentFactory;
        rv = NS_NewGenericFactory(getter_AddRefs(componentFactory), &(components[i]));
        if (NS_FAILED(rv)) {
            NS_ASSERTION(PR_FALSE, "Unable to create factory for component");
            continue;
        }

        rv = cr->RegisterFactory(components[i].mCID,
                             components[i].mDescription,
                             components[i].mContractID,
                             componentFactory);
        NS_ASSERTION(NS_SUCCEEDED(rv), "Unable to register factory for component");
    }

    return rv;
}

/* InitializeWindowCreator creates and hands off an object with a callback
   to a window creation function. This will be used by Gecko C++ code
   (never JS) to create new windows when no previous window is handy
   to begin with. This is done in a few exceptional cases, like PSM code.
   Failure to set this callback will only disable the ability to create
   new windows under these circumstances. */
nsresult MozEmbedApp::InitializeWindowCreator()
{
   // give an nsIWindowCreator to the WindowWatcher service
   nsCOMPtr<nsIWindowCreator> windowCreator(NS_STATIC_CAST(nsIWindowCreator *, this));
   if (windowCreator) {
       nsCOMPtr<nsIWindowWatcher> wwatch(do_GetService(NS_WINDOWWATCHER_CONTRACTID));
       if (wwatch) {
           wwatch->SetWindowCreator(windowCreator);
           return NS_OK;
       }
   }
   return NS_ERROR_FAILURE;
}

// ---------------------------------------------------------------------------
//  MozEmbedApp : nsISupports
// ---------------------------------------------------------------------------

NS_IMPL_THREADSAFE_ISUPPORTS2(MozEmbedApp, nsIWindowCreator, nsISupportsWeakReference);

// ---------------------------------------------------------------------------
//  MozEmbedApp : nsIWindowCreator
// ---------------------------------------------------------------------------
//
// Note: this method seems never being called. Instead 
//     MozEmbedApp::CreateNewBrowserFrame 
// is always called to create a new browser frame window.
//
NS_IMETHODIMP MozEmbedApp::CreateChromeWindow(nsIWebBrowserChrome *parent,
                                              PRUint32 chromeFlags,
                                              nsIWebBrowserChrome **_retval)
{
    NS_ENSURE_ARG_POINTER(_retval);
    *_retval = 0;

    HWND hParent = 0;
    if (parent) {
        CBrowserImpl *pImpl = NS_STATIC_CAST(CBrowserImpl *, parent);
        PRInt32 id = pImpl->m_pBrowserFrame->GetBrowserId();
        hParent = pImpl->m_pBrowserFrame->GetSafeHwnd();
        if (id >= 0) {
            // native browser needs a yes or no confirmation from the 
            // Java side for event CEVENT_BEFORE_NEWWINDOW.
            int bCmdCanceled = -1, waitCount = 0;
            AddTrigger(id, CEVENT_BEFORE_NEWWINDOW, &bCmdCanceled);
            SendSocketMessage(id, CEVENT_BEFORE_NEWWINDOW);

            while (bCmdCanceled < 0 && waitCount++ < MAX_WAIT) {
                Sleep(1);
            }

            if (bCmdCanceled == 1) {
                return NS_ERROR_FAILURE;
            }
        }
    }

    CBrowserFrame *pBrowserFrame = CreateNewBrowserFrame(chromeFlags, hParent);
    if (pBrowserFrame) {
        *_retval = NS_STATIC_CAST(nsIWebBrowserChrome *, pBrowserFrame->GetBrowserImpl());
        NS_ADDREF(*_retval);
    }
    return NS_OK;
}

void MozEmbedApp::MessageReceived(const char * msg)
{
    int instanceNum;
    int eventID;
    char eventMessage[1024];

    if (mInitFailed)
        return;

    int i = sscanf(msg, "%d,%d,%s", &instanceNum, &eventID, eventMessage);
    ASSERT(i >= 2);

    // In case that the last message string argument contains spaces, sscanf 
    // returns before the first space. Below line returns the complete message
    // string.
    char* mMsgString = (char*)strchr(msg, ',');
    mMsgString++;
    mMsgString = (char*)strchr(mMsgString, ',');
    mMsgString++;
    LogMsg("eventMessage:");
    LogMsg(eventMessage);  //need to visit eventMessage.

    switch (eventID) {
    case JEVENT_INIT:
        if (!InitMozilla()) {
            mInitFailed = TRUE;
            NS_ShutdownXPCOM(nsnull);
            XPCOMGlueShutdown();
            gQuitMode = TRUE;
            if (m_pMainWnd)
                m_pMainWnd->PostMessage(WM_QUIT);
        }
        break;
    case JEVENT_CREATEWINDOW:
        {
        // only create new browser window when the instance does not exist
        if (instanceNum < m_FrameWndArray.GetSize() && m_FrameWndArray[instanceNum] != NULL)
            break;

        if (i != 3) 
            break;
        HWND hWnd = (HWND) atoi(mMsgString);
        CBrowserFrame *pBrowserFrame = CreateEmbeddedBrowserFrame(hWnd);
        if (pBrowserFrame) {
            m_FrameWndArray.SetAtGrow(instanceNum, pBrowserFrame);
            pBrowserFrame->SetBrowserId(instanceNum);
            SendSocketMessage(instanceNum, CEVENT_INIT_WINDOW_SUCC);
        }
        }
        break;
    case JEVENT_DESTROYWINDOW:
        if( m_FrameWndArray[instanceNum] != NULL){
            ((CBrowserFrame *)m_FrameWndArray[instanceNum])->DestroyBrowserFrame();
            m_FrameWndArray.SetAt(instanceNum, NULL);
        }
        SendSocketMessage(instanceNum, CEVENT_DISTORYWINDOW_SUCC);
        break;
    case JEVENT_SHUTDOWN:
        gQuitMode = TRUE;
        m_pMainWnd->PostMessage(WM_QUIT);
        break;
    case JEVENT_SET_BOUNDS:
        {
        ASSERT(i == 3);
        int x, y, w, h;
        i = sscanf(mMsgString, "%d,%d,%d,%d", &x, &y, &w, &h);
        if (i == 4)
            ((CBrowserFrame *)m_FrameWndArray[instanceNum])->SetWindowPos(NULL, x, y, w, h, SWP_NOMOVE | SWP_NOZORDER);
        }
        break;
    case JEVENT_NAVIGATE:
        ASSERT(i == 3);
        ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.OpenURL(mMsgString);
        break;
    case JEVENT_NAVIGATE_POST:
        ASSERT(i == 3);

        // Parse the post fields including url, post data and headers.
        char urlBuf[1024], postDataBuf[1024], headersBuf[1024];
        memset(urlBuf, '\0', 1024);
        memset(postDataBuf, '\0', 1024);
        memset(headersBuf, '\0', 1024);

        ParsePostFields(mMsgString, instanceNum, eventID, 
                        urlBuf, postDataBuf, headersBuf);

        char tmpHeadersBuf[2048];
        memset(tmpHeadersBuf, '\0', 2048);
        strcpy(tmpHeadersBuf, POST_HEADER);
        if (strlen(headersBuf) != 0) {
            strcat(tmpHeadersBuf, headersBuf);
        }

        char* postDataParam;
        postDataParam = (strlen(postDataBuf) == 0) ? NULL : postDataBuf;
        
        ((CBrowserFrame *)m_FrameWndArray[instanceNum])
            ->m_wndBrowserView.OpenURL(urlBuf, postDataParam, tmpHeadersBuf);

        break;
    case JEVENT_GOBACK:
        ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_BACK);
        break;
    case JEVENT_GOFORWARD:
        ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_FORWARD);
        break;
    case JEVENT_REFRESH:
        ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_RELOAD);
        break;
    case JEVENT_STOP:
        ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_STOP);
        break;
    case JEVENT_GETURL:
        {
        nsCAutoString uriString;
        nsresult ret = ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.GetURL(uriString);
        if (ret == NS_OK)
            SendSocketMessage(instanceNum, CEVENT_RETURN_URL, uriString.get());
        else 
            SendSocketMessage(instanceNum, CEVENT_RETURN_URL, "");
        }
        break;
    case JEVENT_FOCUSGAINED:
        ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.Activate(WA_ACTIVE, 0, 0);
        break;
    case JEVENT_FOCUSLOST:
        //((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.Activate(WA_INACTIVE, 0, 0);
        break;
    case JEVENT_GETCONTENT:
        {
        nsIWebNavigation* mWebNav =
            ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.mWebNav;

        char *retStr = GetContent(mWebNav);
        if (retStr == NULL)
            SendSocketMessage(instanceNum, CEVENT_GETCONTENT, "");
        else 
            SendSocketMessage(instanceNum, CEVENT_GETCONTENT, retStr);
        }
        break;
    case JEVENT_SETCONTENT:
        {
        ASSERT(i == 3);
        nsIWebNavigation* mWebNav =
            ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.mWebNav;
        SetContent(mWebNav, mMsgString);
        }
        break;
    case JEVENT_EXECUTESCRIPT:
        {
        ASSERT(i == 3);
        nsIWebNavigation* mWebNav =
            ((CBrowserFrame *)m_FrameWndArray[instanceNum])->m_wndBrowserView.mWebNav;
       
        char *retStr = ExecuteScript(mWebNav, mMsgString);
        if (retStr == NULL)
            SendSocketMessage(instanceNum, CEVENT_EXECUTESCRIPT, "");
        else 
            SendSocketMessage(instanceNum, CEVENT_EXECUTESCRIPT, retStr);
        }
        break;
    }
}

void SocketMsgHandler(const char *pMsg)
{
    char *msg = new char[strlen(pMsg) + 1];
    strcpy(msg, pMsg);
    ::PostMessage(AfxGetApp()->GetMainWnd()->GetSafeHwnd(), WM_SOCKET_MSG, 0, (long)msg);
}
