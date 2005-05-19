/* vim:set ts=4 sw=4 sts=4 et cin: */
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
 * The Original Code is mozilla.org code.
 *
 * The Initial Developer of the Original Code is
 * Christopher Blizzard. Portions created by Christopher Blizzard are Copyright (C) Christopher Blizzard.  All Rights Reserved.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Christopher Blizzard <blizzard@mozilla.org>
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

#include "MozEmbed.h"
#include "MsgServer.h"
#include "Message.h"
#include "Common.h"

// These are included from the Gecko SDK
#include "prenv.h"
#include "prthread.h"
#include "nsXPCOM.h"
#include "nsEmbedString.h"
#include "nsDebug.h"
#include "nsMemory.h"
#include "nsIURI.h"
#include "nsIComponentManager.h"

// These are currently not part of the Gecko SDK, so we include our local
// copy of these files.  If these interfaces ever change in the future, then
// our application may stop working.
#include "nsIWebNavigation.h"
#include "nsIStringStream.h"

//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------

int gTestMode = 0;

// the list of pending messages
GList *gMessageList = NULL;
// the lock of locking the gMessageList
PRLock *gMsgLock;

// the array of browser windows currently open
WBArray gBrowserArray;

//cached url for post
char gCachedURL[1024];

// the new event source for socket message
#ifdef MOZ_WIDGET_GTK
static GSourceFuncs event_funcs = {
    gs_prepare_cb,
    gs_check_cb,
    gs_dispatch_cb,
};
#endif
#ifdef MOZ_WIDGET_GTK2
static GSourceFuncs event_funcs = {
    gs_prepare_cb,
    gs_check_cb,
    gs_dispatch_cb,
};
#endif

GtkBrowser *
new_gtk_browser(guint32 chromeMask)
{
    guint32 actualChromeMask = chromeMask;
    GtkBrowser *browser = 0;

    browser = g_new0(GtkBrowser, 1);

    browser->menuBarOn = FALSE;
    browser->toolBarOn = FALSE;
    browser->locationBarOn = FALSE;
    browser->statusBarOn = FALSE;

    g_print("new_gtk_browser\n");

    if (chromeMask == GTK_MOZ_EMBED_FLAG_DEFAULTCHROME)
        actualChromeMask = GTK_MOZ_EMBED_FLAG_ALLCHROME;

    if (actualChromeMask & GTK_MOZ_EMBED_FLAG_MENUBARON)
    {
        browser->menuBarOn = TRUE;
        g_print("\tmenu bar\n");
    }
    if (actualChromeMask & GTK_MOZ_EMBED_FLAG_TOOLBARON)
    {
        browser->toolBarOn = TRUE;
        g_print("\ttool bar\n");
    }
    if (actualChromeMask & GTK_MOZ_EMBED_FLAG_LOCATIONBARON)
    {
        browser->locationBarOn = TRUE;
        g_print("\tlocation bar\n");
    }
    if (actualChromeMask & GTK_MOZ_EMBED_FLAG_STATUSBARON)
    {
        browser->statusBarOn = TRUE;
        g_print("\tstatus bar\n");
    }

    // create our new toplevel window
    browser->topLevelWindow = gtk_window_new(GTK_WINDOW_TOPLEVEL);
    // new vbox
    browser->topLevelVBox = gtk_vbox_new(FALSE, 0);
    // add it to the toplevel window
    gtk_container_add(GTK_CONTAINER(browser->topLevelWindow),
        browser->topLevelVBox);
    // create our menu bar
    browser->menuBar = gtk_menu_bar_new();
    // create the file menu
    browser->fileMenuItem = gtk_menu_item_new_with_label("File");
    browser->fileMenu = gtk_menu_new();
    gtk_menu_item_set_submenu (GTK_MENU_ITEM(browser->fileMenuItem),
        browser->fileMenu);

    browser->fileClose =
        gtk_menu_item_new_with_label("Close");
    gtk_menu_append(GTK_MENU(browser->fileMenu),
        browser->fileClose);

    // append it
    gtk_menu_bar_append(GTK_MENU_BAR(browser->menuBar), browser->fileMenuItem);

    // add it to the vbox
    gtk_box_pack_start(GTK_BOX(browser->topLevelVBox),
        browser->menuBar,
        FALSE, // expand
        FALSE, // fill
        0);    // padding
    // create the hbox that will contain the toolbar and the url text entry bar
    browser->toolbarHBox = gtk_hbox_new(FALSE, 0);
    // add that hbox to the vbox
    gtk_box_pack_start(GTK_BOX(browser->topLevelVBox),
        browser->toolbarHBox,
        FALSE, // expand
        FALSE, // fill
        0);    // padding

    // new horiz toolbar with buttons + icons
#ifdef MOZ_WIDGET_GTK
    browser->toolbar = gtk_toolbar_new(GTK_ORIENTATION_HORIZONTAL,
        GTK_TOOLBAR_BOTH);
#endif /* MOZ_WIDGET_GTK */

#ifdef MOZ_WIDGET_GTK2
    browser->toolbar = gtk_toolbar_new();
    gtk_toolbar_set_orientation(GTK_TOOLBAR(browser->toolbar),
        GTK_ORIENTATION_HORIZONTAL);
    gtk_toolbar_set_style(GTK_TOOLBAR(browser->toolbar),
        GTK_TOOLBAR_BOTH);
#endif /* MOZ_WIDGET_GTK2 */

    // add it to the hbox
    gtk_box_pack_start(GTK_BOX(browser->toolbarHBox), browser->toolbar,
        FALSE, // expand
        FALSE, // fill
        0);    // padding
    // new back button
    browser->backButton =
        gtk_toolbar_append_item(GTK_TOOLBAR(browser->toolbar),
        "Back",
        "Go Back",
        "Go Back",
        0, // XXX replace with icon
        GTK_SIGNAL_FUNC(back_clicked_cb),
        browser);
    // new stop button
    browser->stopButton =
        gtk_toolbar_append_item(GTK_TOOLBAR(browser->toolbar),
        "Stop",
        "Stop",
        "Stop",
        0, // XXX replace with icon
        GTK_SIGNAL_FUNC(stop_clicked_cb),
        browser);
    // new forward button
    browser->forwardButton =
        gtk_toolbar_append_item(GTK_TOOLBAR(browser->toolbar),
        "Forward",
        "Forward",
        "Forward",
        0, // XXX replace with icon
        GTK_SIGNAL_FUNC(forward_clicked_cb),
        browser);
    // new reload button
    browser->reloadButton =
        gtk_toolbar_append_item(GTK_TOOLBAR(browser->toolbar),
        "Reload",
        "Reload",
        "Reload",
        0, // XXX replace with icon
        GTK_SIGNAL_FUNC(reload_clicked_cb),
        browser);
    // create the url text entry
    browser->urlEntry = gtk_entry_new();
    // add it to the hbox
    gtk_box_pack_start(GTK_BOX(browser->toolbarHBox), browser->urlEntry,
        TRUE, // expand
        TRUE, // fill
        0);    // padding

    // create our new gtk moz embed widget
    browser->mozEmbed = gtk_moz_embed_new();
    // add it to the toplevel vbox
    gtk_box_pack_start(GTK_BOX(browser->topLevelVBox), browser->mozEmbed,
        TRUE, // expand
        TRUE, // fill
        0);   // padding

    // create the new hbox for the progress area
    browser->progressAreaHBox = gtk_hbox_new(FALSE, 0);
    // add it to the vbox
    gtk_box_pack_start(GTK_BOX(browser->topLevelVBox), browser->progressAreaHBox,
        FALSE, // expand
        FALSE, // fill
        0);   // padding
    // create our new progress bar
    browser->progressBar = gtk_progress_bar_new();
    // add it to the hbox
    gtk_box_pack_start(GTK_BOX(browser->progressAreaHBox), browser->progressBar,
        FALSE, // expand
        FALSE, // fill
        0); // padding

    // create our status area and the alignment object that will keep it
    // from expanding
    browser->statusAlign = gtk_alignment_new(0, 0, 1, 1);
    gtk_widget_set_usize(browser->statusAlign, 1, -1);
    // create the status bar
    browser->statusBar = gtk_statusbar_new();
    gtk_container_add(GTK_CONTAINER(browser->statusAlign), browser->statusBar);
    // add it to the hbox
    gtk_box_pack_start(GTK_BOX(browser->progressAreaHBox), browser->statusAlign,
        TRUE, // expand
        TRUE, // fill
        0);   // padding
    // by default none of the buttons are marked as sensitive.
    gtk_widget_set_sensitive(browser->backButton, FALSE);
    gtk_widget_set_sensitive(browser->stopButton, FALSE);
    gtk_widget_set_sensitive(browser->forwardButton, FALSE);
    gtk_widget_set_sensitive(browser->reloadButton, FALSE);

    // catch the destruction of the toplevel window
    gtk_signal_connect(GTK_OBJECT(browser->topLevelWindow), "delete_event",
        GTK_SIGNAL_FUNC(delete_cb), browser);

    // hook up the activate signal to the right callback
    gtk_signal_connect(GTK_OBJECT(browser->urlEntry), "activate",
        GTK_SIGNAL_FUNC(url_activate_cb), browser);

    // close this window
    gtk_signal_connect(GTK_OBJECT(browser->fileClose), "activate",
        GTK_SIGNAL_FUNC(menu_close_cb), browser);

    install_mozembed_cb(browser);

    // set the chrome type so it's stored in the object
    gtk_moz_embed_set_chrome_mask(GTK_MOZ_EMBED(browser->mozEmbed),
        actualChromeMask);

    return browser;
}

void
set_browser_visibility (GtkBrowser *browser, gboolean visibility)
{
    if (!visibility)
    {
        gtk_widget_hide(browser->topLevelWindow);
        return;
    }

    if (browser->menuBar) {
        if (browser->menuBarOn)
            gtk_widget_show_all(browser->menuBar);
        else
            gtk_widget_hide_all(browser->menuBar);
    }

    if (browser->toolbarHBox) {
        // since they are on the same line here...
        if (browser->toolBarOn || browser->locationBarOn)
            gtk_widget_show_all(browser->toolbarHBox);
        else
            gtk_widget_hide_all(browser->toolbarHBox);
    }

    if (browser->progressAreaHBox) {
        if (browser->statusBarOn)
            gtk_widget_show_all(browser->progressAreaHBox);
        else
            gtk_widget_hide_all(browser->progressAreaHBox);
    }

    if (browser->mozEmbed)
        gtk_widget_show(browser->mozEmbed);
    if (browser->topLevelVBox)
        gtk_widget_show(browser->topLevelVBox);
    if (browser->topLevelWindow)
        gtk_widget_show(browser->topLevelWindow);
}

void
OpenURL(GtkBrowser *pBrowser, const char *pUrl, const char *pPostData, const char *pHeader)
{
    nsresult rv;
    nsCOMPtr<nsIInputStream> postDataStream;
    nsCOMPtr<nsIInputStream> headersStream;

    if (pPostData) 
    {
        unsigned long nSizeData = strlen(pPostData);
        if (nSizeData > 0) 
        {
            char szCL[64];
            sprintf(szCL, "Content-Length: %lu\r\n\r\n", nSizeData);
            unsigned long nSizeCL = strlen(szCL);
            unsigned long nSize = nSizeCL + nSizeData;

            char *tmp = (char *) nsMemory::Alloc(nSize + 1); // byte stream owns this mem
            if (tmp) 
            {
                memcpy(tmp, szCL, nSizeCL);
                memcpy(tmp + nSizeCL, pPostData, nSizeData);
                tmp[nSize] = '\0';

                nsCOMPtr<nsIStringInputStream> stream;
                rv = CreateInstance("@mozilla.org/io/string-input-stream;1",
                                    NS_GET_IID(nsIStringInputStream),
                                    getter_AddRefs(stream));
                if (NS_FAILED(rv) || !stream) 
                {
                    NS_ASSERTION(0, "cannot create PostData stream");
                    nsMemory::Free(tmp);
                    return;
                }

                stream->AdoptData(tmp, nSize);
                postDataStream = do_QueryInterface(stream);
            }
        }
    }
    
    if (pHeader) 
    {
        unsigned long nSize = strlen(pHeader) + 1;
        if (nSize > 0) 
        {
            char *tmp = (char *) nsMemory::Alloc(nSize); // byteArray stream owns this mem
            if (tmp) 
            {
                memcpy(tmp, pHeader, nSize);

                nsCOMPtr<nsIStringInputStream> stream;
                rv = CreateInstance("@mozilla.org/io/string-input-stream;1",
                                    NS_GET_IID(nsIStringInputStream),
                                    getter_AddRefs(stream));
                if (NS_FAILED(rv) || !stream) 
                {
                    NS_ASSERTION(0, "cannot create Header stream");
                    nsMemory::Free(tmp);
                    return;
                }

                stream->AdoptData(tmp, nSize);
                headersStream = do_QueryInterface(stream);
            }
        }
    }

    nsCOMPtr<nsIWebBrowser> webBrowser;
    gtk_moz_embed_get_nsIWebBrowser(GTK_MOZ_EMBED(pBrowser->mozEmbed), getter_AddRefs(webBrowser));
    nsCOMPtr<nsIWebNavigation> webNavigation(do_QueryInterface(webBrowser));
    if (!webNavigation)
        return;

    nsEmbedString unicodeUrl;
    ConvertAsciiToUtf16(pUrl, unicodeUrl);

    webNavigation->LoadURI(unicodeUrl.get(), // URI string
                           nsIWebNavigation::LOAD_FLAGS_NONE, // Load flags
                           nsnull,                            // Refering URI
                           postDataStream,                    // Post data
                           headersStream);                    // Extra headers
}

void 
HandleSocketMessage(gpointer data, gpointer user_data)
{
    int instance, type;
    char mMsgBuf[1024];
    char *msg = (char *)data;

    int i = sscanf(msg, "%d,%d,%s", &instance, &type, mMsgBuf);

    NS_ASSERTION(i >= 2, "Wrong message format\n");

    // In case that the last message string argument contains spaces, sscanf 
    // returns before the first space. Below line returns the complete message
    // string.
    char* mMsgString = (char*)strchr(msg, ',');
    mMsgString++;
    mMsgString = (char*)strchr(mMsgString, ',');
    mMsgString++;

    GtkBrowser *pBrowser;
    switch (type) {
    case JEVENT_INIT:
        break;
    case JEVENT_CREATEWINDOW:
        {
            // only create new browser window when the instance does not exist
            if (instance < gBrowserArray.GetSize() && gBrowserArray[instance] != NULL)
                break;
            if (i != 3)
                break;
            int javaXId = atoi(mMsgString);
            NS_ASSERTION(javaXId, "Invalid X window handle\n");
            pBrowser = g_new0(GtkBrowser, 1);
            pBrowser->topLevelWindow = gtk_plug_new(javaXId);
            pBrowser->mozEmbed = gtk_moz_embed_new();
            if (pBrowser->mozEmbed) {
                gtk_container_add(GTK_CONTAINER(pBrowser->topLevelWindow), pBrowser->mozEmbed);
                install_mozembed_cb(pBrowser);
                gtk_moz_embed_set_chrome_mask(GTK_MOZ_EMBED(pBrowser->mozEmbed),
                    GTK_MOZ_EMBED_FLAG_DEFAULTCHROME);
                gtk_widget_realize(pBrowser->topLevelWindow);
                gtk_widget_show_all(pBrowser->topLevelWindow);
                pBrowser->id = instance;
                gBrowserArray.SetAtGrow(instance, pBrowser);
                SendSocketMessage(instance, CEVENT_INIT_WINDOW_SUCC);
            }

            gtk_signal_connect(GTK_OBJECT(pBrowser->topLevelWindow), 
                  "set-focus", GTK_SIGNAL_FUNC(set_focus_cb), pBrowser);

        }
        break;
    case JEVENT_DESTROYWINDOW:
        pBrowser = (GtkBrowser *)gBrowserArray[instance];
        if(pBrowser != NULL){
            gtk_widget_destroy(pBrowser->mozEmbed);
            gtk_object_destroy((GtkObject *)pBrowser->topLevelWindow);
            gBrowserArray.SetAt(instance, NULL);
        }
        SendSocketMessage(instance, CEVENT_DISTORYWINDOW_SUCC);
        break;
    case JEVENT_SHUTDOWN:
        gtk_main_quit();
        break;
    case JEVENT_SET_BOUNDS:
        {
            NS_ASSERTION(i == 3, "Wrong message format\n");
            int x, y, w, h;
            i = sscanf(mMsgString, "%d,%d,%d,%d", &x, &y, &w, &h);
            if (i == 4) {
                pBrowser = (GtkBrowser *)gBrowserArray[instance];
                NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
                gtk_widget_set_usize(pBrowser->topLevelWindow, w, h);
            }
        }
        break;
    case JEVENT_NAVIGATE:
        NS_ASSERTION(i == 3, "Wrong message format\n");
        pBrowser = (GtkBrowser *)gBrowserArray[instance];
        NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
        gtk_moz_embed_load_url(GTK_MOZ_EMBED(pBrowser->mozEmbed), mMsgString);
        break;
    case JEVENT_NAVIGATE_POST:
        NS_ASSERTION(i == 3, "Wrong message format\n");
        strncpy(gCachedURL, mMsgString, sizeof(gCachedURL));
        break;
    case JEVENT_NAVIGATE_POSTDATA:
        NS_ASSERTION(i == 3, "Wrong message format\n");
        pBrowser = (GtkBrowser *)gBrowserArray[instance];
        OpenURL(pBrowser, gCachedURL, mMsgString, POST_HEADER);
        break;
    case JEVENT_GOBACK:
        pBrowser = (GtkBrowser *)gBrowserArray[instance];
        NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
        gtk_moz_embed_go_back(GTK_MOZ_EMBED(pBrowser->mozEmbed));
        break;
    case JEVENT_GOFORWARD:
        pBrowser = (GtkBrowser *)gBrowserArray[instance];
        NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
        gtk_moz_embed_go_forward(GTK_MOZ_EMBED(pBrowser->mozEmbed));
        break;
    case JEVENT_REFRESH:
        pBrowser = (GtkBrowser *)gBrowserArray[instance];
        NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
        gtk_moz_embed_reload(GTK_MOZ_EMBED(pBrowser->mozEmbed), GTK_MOZ_EMBED_FLAG_RELOADNORMAL);
        break;
    case JEVENT_STOP:
        pBrowser = (GtkBrowser *)gBrowserArray[instance];
        NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
        gtk_moz_embed_stop_load(GTK_MOZ_EMBED(pBrowser->mozEmbed));
        break;
    case JEVENT_GETURL:
        {
            pBrowser = (GtkBrowser *)gBrowserArray[instance];
            NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
            nsCOMPtr<nsIWebBrowser> webBrowser;
            gtk_moz_embed_get_nsIWebBrowser(GTK_MOZ_EMBED(pBrowser->mozEmbed), getter_AddRefs(webBrowser));
            nsCOMPtr<nsIWebNavigation> webNavigation(do_QueryInterface(webBrowser));
            nsCOMPtr<nsIURI> currentURI;
            webNavigation->GetCurrentURI(getter_AddRefs(currentURI));
            if (currentURI == NULL)
                SendSocketMessage(instance, CEVENT_RETURN_URL, "");
            else { 
            	nsEmbedCString uriString;
                currentURI->GetAsciiSpec(uriString);
                SendSocketMessage(instance, CEVENT_RETURN_URL, uriString.get());
            }
        }
        break;

    case JEVENT_FOCUSGAINED:
    case JEVENT_FOCUSLOST:
        {
            pBrowser = (GtkBrowser *)gBrowserArray[instance];
            NS_ASSERTION(pBrowser, "Can't get native browser instance\n");

            if (!pBrowser->topLevelWindow) {
                ReportError("Top level Window is Null!\n");
                break;
            }

            GtkWidget *widget = GTK_WIDGET (pBrowser->topLevelWindow);
            GdkEvent event;

            GtkWindowClass *parent_class = (GtkWindowClass*) gtk_type_class (GTK_TYPE_WINDOW);

            if (!widget) {
                ReportError("Failed to get browser's toplevel window !\n");
                break;
            }
            if (!parent_class) {
                ReportError("Failed to get gtk window class !\n");
                break;
            }

            event.focus_change.type = GDK_FOCUS_CHANGE;
            event.focus_change.window = widget->window;
            event.focus_change.send_event = TRUE;

            if (type == JEVENT_FOCUSGAINED) {
                event.focus_change.in = TRUE;
                GTK_WIDGET_CLASS (parent_class)->focus_in_event
                            (widget, (GdkEventFocus *)&event);
            }
            else {
                event.focus_change.in = FALSE;
                GTK_WIDGET_CLASS (parent_class)->focus_out_event
                            (widget, (GdkEventFocus *)&event);
            }
        }
        break;
    case JEVENT_GETCONTENT:
        {
            pBrowser = (GtkBrowser *)gBrowserArray[instance];
            NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
            nsCOMPtr<nsIWebBrowser> webBrowser;
            gtk_moz_embed_get_nsIWebBrowser(GTK_MOZ_EMBED(pBrowser->mozEmbed), 
                    getter_AddRefs(webBrowser));
            nsCOMPtr<nsIWebNavigation> 
                webNavigation(do_QueryInterface(webBrowser));
                                                                                                            
            char *retStr = GetContent(webNavigation);
            if (retStr == NULL)
                SendSocketMessage(instance, CEVENT_GETCONTENT, "");
            else
                SendSocketMessage(instance, CEVENT_GETCONTENT, retStr);
        } 
        break;
    case JEVENT_SETCONTENT:
        {
            NS_ASSERTION(i == 3, "Wrong message format\n");
            pBrowser = (GtkBrowser *)gBrowserArray[instance];
            NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
            nsCOMPtr<nsIWebBrowser> webBrowser;
            gtk_moz_embed_get_nsIWebBrowser(GTK_MOZ_EMBED(pBrowser->mozEmbed), 
                    getter_AddRefs(webBrowser));
            nsCOMPtr<nsIWebNavigation> 
                webNavigation(do_QueryInterface(webBrowser));
            
            SetContent(webNavigation, mMsgString);
        }
        break;
    case JEVENT_EXECUTESCRIPT:
        {
            NS_ASSERTION(i == 3, "Wrong message format\n");
            pBrowser = (GtkBrowser *)gBrowserArray[instance];
            NS_ASSERTION(pBrowser, "Can't get native browser instance\n");
            nsCOMPtr<nsIWebBrowser> webBrowser;
            gtk_moz_embed_get_nsIWebBrowser(GTK_MOZ_EMBED(pBrowser->mozEmbed), 
                    getter_AddRefs(webBrowser));
            nsCOMPtr<nsIWebNavigation> 
                webNavigation(do_QueryInterface(webBrowser));
                                                                                                            
            char *retStr = ExecuteScript(webNavigation, mMsgString);
            if (retStr == NULL)
                SendSocketMessage(instance, CEVENT_EXECUTESCRIPT, "");
            else
                SendSocketMessage(instance, CEVENT_EXECUTESCRIPT, retStr);
        } 
        break;
    }
}

extern "C" int mozembed_main(int argc, char **argv);

int
mozembed_main(int argc, char **argv)
{
    if (argc > 1) {
        if (strstr(argv[1], "-port=")) {
            int port = atoi(&(argv[1][6]));
            gMessenger.SetPort(port);
            gMessenger.CreateServerSocket();
        }
        else if (strcmp(argv[1], "-test") == 0) {
            gTestMode = 1;
        }
    }
    
    if (!gTestMode && gMessenger.IsFailed()) {
        ReportError("Failed to create server socket!");
        exit(1);
    }

    gtk_set_locale();
    gtk_init(&argc, &argv);

    // force the startup code to be executed because we need to start
    // the profile in a different way
    gtk_moz_embed_push_startup();

    if (NS_FAILED(InitializeProfile())) {
        ReportError("Failed to initialize profile!");
        exit(1);
    }

    gMsgLock = PR_NewLock();

    if (!gTestMode) {
        PRThread *socketListenThread = 
          PR_CreateThread(PR_USER_THREAD,
                          PortListening,
                          (void*)SocketMsgHandler,
                          PR_PRIORITY_NORMAL,
                          PR_GLOBAL_THREAD,
                          PR_UNJOINABLE_THREAD,
                          0);
        if (!socketListenThread) {
            ReportError("Failed to create socket listening thread!");
            exit(1);
        }

        // add event source to process socket messages
#ifdef MOZ_WIDGET_GTK
        g_source_add (GDK_PRIORITY_EVENTS, TRUE, &event_funcs, NULL, NULL, NULL);
#endif
#ifdef MOZ_WIDGET_GTK2
        GSource *newsource = g_source_new(&event_funcs, sizeof(GSource));
        g_source_attach(newsource, NULL);
#endif
    }
    else {
        GtkBrowser *browser = new_gtk_browser(GTK_MOZ_EMBED_FLAG_DEFAULTCHROME);
        
        // set our minimum size
        gtk_widget_set_usize(browser->mozEmbed, 400, 400);
        
        set_browser_visibility(browser, TRUE);
    }

    // get the singleton object and hook up to its new window callback
    // so we can create orphaned windows.

    GtkMozEmbedSingle *single;

    single = gtk_moz_embed_single_get();
    if (!single) {
        ReportError("Failed to get singleton embed object!");
        exit(1);
    }

    gtk_signal_connect(GTK_OBJECT(single), "new_window_orphan",
        GTK_SIGNAL_FUNC(new_window_orphan_cb), NULL);

    gtk_main();
    gtk_moz_embed_pop_startup();

    PR_DestroyLock(gMsgLock);
    return 0;
}

// this function is running in the socket listening thread
void 
SocketMsgHandler(const char *pMsg)
{
    char *msg = new char[strlen(pMsg) + 1];
    strcpy(msg, pMsg);
    PR_Lock(gMsgLock);
    gMessageList = g_list_append(gMessageList, msg);
    PR_Unlock(gMsgLock);
}

gboolean 
#ifdef MOZ_WIDGET_GTK
gs_prepare_cb(gpointer source_data,
              GTimeVal *current_time,
              gint     *timeout,
              gpointer  user_data)
#endif
#ifdef MOZ_WIDGET_GTK2
gs_prepare_cb(GSource  *source,
              gint     *timeout)
#endif
{
    *timeout = 10;

    PR_Lock(gMsgLock);
    gboolean ret = (g_list_first(gMessageList) != NULL);
    PR_Unlock(gMsgLock);

    return ret;
}

gboolean 
#ifdef MOZ_WIDGET_GTK
gs_check_cb(gpointer source_data,
            GTimeVal *current_time,
            gpointer  user_data)
#endif
#ifdef MOZ_WIDGET_GTK2
gs_check_cb(GSource *source)
#endif
{
    return FALSE;
}

gboolean 
#ifdef MOZ_WIDGET_GTK
gs_dispatch_cb(gpointer source_data,
               GTimeVal *current_time,
               gpointer  user_data)
#endif
#ifdef MOZ_WIDGET_GTK2
gs_dispatch_cb(GSource    *source,
               GSourceFunc callback,
               gpointer  user_data)
#endif
{
    PR_Lock(gMsgLock);
    GList *tmpList = g_list_copy(gMessageList);
    g_list_free(gMessageList);
    gMessageList = NULL;
    PR_Unlock(gMsgLock);

    g_list_foreach(tmpList, HandleSocketMessage, NULL);

    return TRUE;
}
