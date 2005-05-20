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
    HKEY hKey;
    const char* mozExe = "mozilla.exe";
    char browserPath[_MAX_PATH+1] = "\0";      
    DWORD cb = sizeof(browserPath);

    // Get the default browser path by checking the http protocol handler.
    if (RegOpenKeyEx(HKEY_CLASSES_ROOT, "http\\shell\\open\\command", 0, 
        KEY_READ, &hKey) != ERROR_SUCCESS)
        return PR_FALSE;
    
    if (RegQueryValueEx(hKey, "", NULL, NULL, (BYTE*)browserPath,
          &cb) != ERROR_SUCCESS) {
        RegCloseKey(hKey); 
        return PR_FALSE;
    } 
    RegCloseKey(hKey);

    // Check if the default browser is mozilla.
    char* exePtr;
    char* lwrBrowserPath = strlwr(strdup(browserPath));
    if (!(exePtr = strstr(lwrBrowserPath, mozExe)))
        // The default browser is not Mozilla.
        return PR_FALSE;
   
    // Remove the trailing part after the first space character at the rail 
    // of mozilla.exe.
    cb = strlen(browserPath);
    for (int i = (exePtr - lwrBrowserPath + strlen(mozExe)); i < (int)cb; i++) {
        if (browserPath[i] == ' ') {
            browserPath[i] = '\0';
            break;   
        }
    }
    
    // Check if xpcom.dll exists under the same parent directory as mozilla.exe.   
    char* ptr = strrchr(browserPath, '\\');
    char parentPath[_MAX_PATH+1] = "\0";
    strncpy(parentPath, browserPath, ptr - browserPath);
    char* xpcomFilePath = strcat(parentPath, "\\xpcom.dll");

    if (fopen(xpcomFilePath,"r")) {
        // xpcom.dll exists, Mozilla is installed with a .zip package.        
        strncpy(pathBuf, browserPath, ptr - browserPath);
        pathBuf[ptr - browserPath] = '\0';
    } else {
        // xpcom.dll doesn't exist, Mozilla is installed with an .exe package.
        // Check the installed GRE directory:
        // [Common Files]\mozilla.org\GRE\<GRE version>       
        const char* greParentKey = "Software\\mozilla.org\\GRE\\";
        DWORD loop = 0;
        char  greVersionKey[256];   
        DWORD greVersionKeyLen = 256;
        char *lastVersionKey = NULL;

        // !!!Note: here it returns the latest GRE directory, which should be 
        // fixed to return the GRE directory matching the currently running
        // Mozilla binary.
        if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, greParentKey, 0, KEY_READ, &hKey) 
            == ERROR_SUCCESS) {
            while(RegEnumKeyEx(hKey, loop++, greVersionKey, &greVersionKeyLen,
                               NULL, NULL, NULL, NULL) != ERROR_NO_MORE_ITEMS) {
                lastVersionKey = greVersionKey;                   
                greVersionKeyLen = 256;
            }
            RegCloseKey(hKey);     
        }

        if (lastVersionKey == NULL) {
            return PR_FALSE;
        }

        char szKey[256], greHome[_MAX_PATH+1];
        strcpy(szKey, greParentKey);
        strcat(szKey, lastVersionKey);       
        cb = sizeof(greHome);   

        if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, szKey, 0, KEY_QUERY_VALUE, &hKey) 
            != ERROR_SUCCESS) {
            return PR_FALSE;
        }
     
        if (RegQueryValueEx(hKey, "GreHome", NULL, NULL, (BYTE *)greHome,
            &cb) != ERROR_SUCCESS) {
            RegCloseKey(hKey); 
            return PR_FALSE;
        } 
        RegCloseKey(hKey); 
   
        strncpy(pathBuf, greHome, pathBufSize);
    } // end of xpcom.dll doesn't exist.
   
    return PR_TRUE;
}

BOOL MozEmbedApp::InitMozilla()
{   
    nsresult rv;

    TCHAR greDirPath[_MAX_PATH+1] = "\0";
    TCHAR xpcomFilePath[_MAX_PATH+1] = "\0";   
    
    if (!GetGREPath(greDirPath, sizeof(greDirPath))) {
        ReportError("GetGREPath failed: can't locate the GRE path of the \
                     installed Mozilla binary!");
        return FALSE;
    }
    strcpy(xpcomFilePath, greDirPath);
    strcat(xpcomFilePath, "\\xpcom.dll");

    rv = XPCOMGlueStartup(xpcomFilePath);
    if (NS_FAILED(rv)) {
        ReportError("XPCOMGlueStartup failed!");
        return FALSE;
    }

    nsCOMPtr<nsILocalFile> greDir;
    rv = NS_NewNativeLocalFile(nsEmbedCString(T2A(greDirPath)), TRUE, getter_AddRefs(greDir));
    if (NS_FAILED(rv)) {
        ReportError("NS_NewNativeLocalFile failed!");
        return FALSE;
    }

    rv = NS_InitXPCOM2(nsnull, greDir, nsnull); 
    if (NS_FAILED(rv)) {
        ReportError("NS_InitXPCOM2 failed!");
        return FALSE;
    }

    rv = OverrideComponents();
    if (NS_FAILED(rv))
    {
        ReportError("OverrideComponents failed!");
        return FALSE;
    }

    rv = InitializeWindowCreator();
    if (NS_FAILED(rv))
    {
        ReportError("InitializeWindowCreator failed!");
        return FALSE;
    }

    rv = InitializeProfile();
    if (NS_FAILED(rv))
    {
        ReportError("InitializeProfiles failed!");
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
    int instance, type;
    char mMsgBuf[1024];

    if (mInitFailed)
        return;

    int i = sscanf(msg, "%d,%d,%s", &instance, &type, mMsgBuf);
    ASSERT(i >= 2);

    // In case that the last message string argument contains spaces, sscanf 
    // returns before the first space. Below line returns the complete message
    // string.
    char* mMsgString = (char*)strchr(msg, ',');
    mMsgString++;
    mMsgString = (char*)strchr(mMsgString, ',');
    mMsgString++;

    switch (type) {
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
        if (instance < m_FrameWndArray.GetSize() && m_FrameWndArray[instance] != NULL)
            break;

        if (i != 3) 
            break;
        HWND hWnd = (HWND) atoi(mMsgString);
        CBrowserFrame *pBrowserFrame = CreateEmbeddedBrowserFrame(hWnd);
        if (pBrowserFrame) {
            m_FrameWndArray.SetAtGrow(instance, pBrowserFrame);
            pBrowserFrame->SetBrowserId(instance);
            SendSocketMessage(instance, CEVENT_INIT_WINDOW_SUCC);
        }
        }
        break;
    case JEVENT_DESTROYWINDOW:
        if( m_FrameWndArray[instance] != NULL){
            ((CBrowserFrame *)m_FrameWndArray[instance])->DestroyBrowserFrame();
            m_FrameWndArray.SetAt(instance, NULL);
        }
        SendSocketMessage(instance, CEVENT_DISTORYWINDOW_SUCC);
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
            ((CBrowserFrame *)m_FrameWndArray[instance])->SetWindowPos(NULL, x, y, w, h, SWP_NOMOVE | SWP_NOZORDER);
        }
        break;
    case JEVENT_NAVIGATE:
        ASSERT(i == 3);
        ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.OpenURL(mMsgString);
        break;
    case JEVENT_NAVIGATE_POST:
        ASSERT(i == 3);
        mURL = mMsgString;
        break;
    case JEVENT_NAVIGATE_POSTDATA:
        ASSERT(i == 3);
        ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.OpenURL(mURL, mMsgString, POST_HEADER);
        break;
    case JEVENT_GOBACK:
        ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_BACK);
        break;
    case JEVENT_GOFORWARD:
        ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_FORWARD);
        break;
    case JEVENT_REFRESH:
        ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_RELOAD);
        break;
    case JEVENT_STOP:
        ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.PostMessage(WM_COMMAND, ID_NAV_STOP);
        break;
    case JEVENT_GETURL:
        {
        nsCAutoString uriString;
        nsresult ret = ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.GetURL(uriString);
        if (ret == NS_OK)
            SendSocketMessage(instance, CEVENT_RETURN_URL, uriString.get());
        else 
            SendSocketMessage(instance, CEVENT_RETURN_URL, "");
        }
        break;
    case JEVENT_FOCUSGAINED:
        ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.Activate(WA_ACTIVE, 0, 0);
        break;
    case JEVENT_FOCUSLOST:
        //((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.Activate(WA_INACTIVE, 0, 0);
        break;
    case JEVENT_GETCONTENT:
        {
        nsIWebNavigation* mWebNav =
            ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.mWebNav;

        char *retStr = GetContent(mWebNav);
        if (retStr == NULL)
            SendSocketMessage(instance, CEVENT_GETCONTENT, "");
        else 
            SendSocketMessage(instance, CEVENT_GETCONTENT, retStr);
        }
        break;
    case JEVENT_SETCONTENT:
        {
        ASSERT(i == 3);
        nsIWebNavigation* mWebNav =
            ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.mWebNav;
        SetContent(mWebNav, mMsgString);
        }
        break;
    case JEVENT_EXECUTESCRIPT:
        {
        ASSERT(i == 3);
        nsIWebNavigation* mWebNav =
            ((CBrowserFrame *)m_FrameWndArray[instance])->m_wndBrowserView.mWebNav;
       
        char *retStr = ExecuteScript(mWebNav, mMsgString);
        if (retStr == NULL)
            SendSocketMessage(instance, CEVENT_EXECUTESCRIPT, "");
        else 
            SendSocketMessage(instance, CEVENT_EXECUTESCRIPT, retStr);
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
