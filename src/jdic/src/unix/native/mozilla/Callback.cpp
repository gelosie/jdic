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
 * The Initial Developer of the Original Code is Christopher Blizzard.
 *
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

#include "Common.h"
#include "Message.h"
#include "MozEmbed.h"
#include "MsgServer.h"
#include "Util.h"
#include "xembed.h"

// from the Gecko SDK
#include "prthread.h"
#include "nsIInterfaceRequestorUtils.h"
#include "nsIURI.h"
#include "nsIWebBrowser.h"
#include "nsIWebBrowserFocus.h"
#include "nsEmbedString.h"

// copied from Mozilla 1.7
#include "gtkmozembed_internal.h"
#include "nsIWebNavigation.h"

extern int gTestMode;

void
install_mozembed_cb(GtkBrowser *browser)
{
    // hook up the location change to update the urlEntry
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "location",
        GTK_SIGNAL_FUNC(location_changed_cb), browser);
    // hook up the start and stop signals
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "net_start",
        GTK_SIGNAL_FUNC(load_started_cb), browser);
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "net_stop",
        GTK_SIGNAL_FUNC(load_finished_cb), browser);
    // hook up to the change in network status
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "net_state",
        GTK_SIGNAL_FUNC(net_state_change_cb), browser);
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "net_state_all",
        GTK_SIGNAL_FUNC(net_state_change_all_cb), browser);
    // hookup to changes in progress
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "progress",
        GTK_SIGNAL_FUNC(progress_change_cb), browser);
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "progress_all",
        GTK_SIGNAL_FUNC(progress_change_all_cb), browser);
    // hookup to see whenever a new window is requested
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "new_window",
        GTK_SIGNAL_FUNC(new_window_cb), browser);
    // hookup to any requested visibility changes
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "visibility",
        GTK_SIGNAL_FUNC(visibility_cb), browser);
    // hookup to the signal that says that the browser requested to be
    // destroyed
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "destroy_browser",
        GTK_SIGNAL_FUNC(destroy_brsr_cb), browser);
    // hookup to the signal that is called when someone clicks on a link
    // to load a new uri
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "open_uri",
        GTK_SIGNAL_FUNC(open_uri_cb), browser);
    // this signal is emitted when there's a request to change the
    // containing browser window to a certain height, like with width
    // and height args for a window.open in javascript
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "size_to",
        GTK_SIGNAL_FUNC(size_to_cb), browser);
    // hookup to when the window is destroyed
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "destroy",
        GTK_SIGNAL_FUNC(destroy_cb), browser);
    // title and status text change
	gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "title",
        GTK_SIGNAL_FUNC(title_change_cb), browser);
    gtk_signal_connect(GTK_OBJECT(browser->mozEmbed), "status_change",
        GTK_SIGNAL_FUNC(status_text_change_cb), browser);
}

void
back_clicked_cb(GtkButton *button, GtkBrowser *browser)
{
    gtk_moz_embed_go_back(GTK_MOZ_EMBED(browser->mozEmbed));
}

void
stop_clicked_cb(GtkButton *button, GtkBrowser *browser)
{
    gtk_moz_embed_stop_load(GTK_MOZ_EMBED(browser->mozEmbed));
}

void
forward_clicked_cb(GtkButton *button, GtkBrowser *browser)
{
    gtk_moz_embed_go_forward(GTK_MOZ_EMBED(browser->mozEmbed));
}

void
reload_clicked_cb(GtkButton *button, GtkBrowser *browser)
{
    GdkModifierType state = (GdkModifierType)0;
    gint x, y;
    gdk_window_get_pointer(NULL, &x, &y, &state);

    gtk_moz_embed_reload(GTK_MOZ_EMBED(browser->mozEmbed),
        (state & GDK_SHIFT_MASK) ?
        GTK_MOZ_EMBED_FLAG_RELOADBYPASSCACHE :
        GTK_MOZ_EMBED_FLAG_RELOADNORMAL);
}

void
stream_clicked_cb(GtkButton   *button, GtkBrowser *browser)
{
    const char *data;
    const char *data2;
    data = "<html>Hi";
    data2 = " there</html>\n";
    WBTRACE("stream_clicked_cb\n");
    gtk_moz_embed_open_stream(GTK_MOZ_EMBED(browser->mozEmbed),
        "file://", "text/html");
    gtk_moz_embed_append_data(GTK_MOZ_EMBED(browser->mozEmbed),
        data, strlen(data));
    gtk_moz_embed_append_data(GTK_MOZ_EMBED(browser->mozEmbed),
        data2, strlen(data2));
    gtk_moz_embed_close_stream(GTK_MOZ_EMBED(browser->mozEmbed));
}

void
url_activate_cb(GtkEditable *widget, GtkBrowser *browser)
{
    gchar *text = gtk_editable_get_chars(widget, 0, -1);
    WBTRACE("loading url %s\n", text);
    gtk_moz_embed_load_url(GTK_MOZ_EMBED(browser->mozEmbed), text);
    g_free(text);
}

void
menu_close_cb(GtkMenuItem *menuitem, GtkBrowser *browser)
{
    gtk_widget_destroy(browser->topLevelWindow);
}

gboolean
delete_cb(GtkWidget *widget, GdkEventAny *event, GtkBrowser *browser)
{
    WBTRACE("delete_cb\n");
    gtk_widget_destroy(widget);
    return TRUE;
}

void set_focus_cb(GtkWindow *window, GtkWidget *focus, GtkBrowser *browser)
{
  WBTRACE("focus_event_cb\n");
  #ifdef MOZ_WIDGET_GTK
    if (focus && !window->window_has_focus)
  #endif
  #ifdef MOZ_WIDGET_GTK2
    if (focus && !window->has_focus)
  #endif
    SendSocketMessage(browser->id, CEVENT_FOCUS_REQUEST);
} 

void
destroy_cb(GtkWidget *widget, GtkBrowser *browser)
{
    WBTRACE("destroy_cb\n");
    int count = 0, size = gBrowserArray.GetSize();
    for (int i = size - 1; i >= 0; i--) {
        GtkBrowser *tmpBrowser = (GtkBrowser *)gBrowserArray[i];
        if (tmpBrowser == browser)
            gBrowserArray.RemoveAt(i);
        else if (tmpBrowser)
            count++;
    }
    if (browser->tempMessage)
        g_free(browser->tempMessage);
    if (gTestMode && count == 0)
        gtk_main_quit();
}

void
location_changed_cb(GtkMozEmbed *embed, GtkBrowser *browser)
{
    char *newLocation;
    int   newPosition = 0;
    WBTRACE("location_changed_cb\n");
    newLocation = gtk_moz_embed_get_location(embed);
    if (newLocation) {
        if (browser->urlEntry) {
            gtk_editable_delete_text(GTK_EDITABLE(browser->urlEntry), 0, -1);
            gtk_editable_insert_text(GTK_EDITABLE(browser->urlEntry),
                newLocation, strlen(newLocation), &newPosition);
        }
        g_free(newLocation);
    }
    else
        WBTRACE("failed to get location!\n");
    // always make sure to clear the tempMessage.  it might have been
    // set from the link before a click and we wouldn't have gotten the
    // callback to unset it.
    update_temp_message(browser, 0);
    // update the nav buttons on a location change
    update_nav_buttons(browser);
}

void
load_started_cb(GtkMozEmbed *embed, GtkBrowser *browser)
{
    if (browser->stopButton)
        gtk_widget_set_sensitive(browser->stopButton, TRUE);
    if (browser->reloadButton)
        gtk_widget_set_sensitive(browser->reloadButton, FALSE);
    browser->loadPercent = 0;
    browser->bytesLoaded = 0;
    browser->maxBytesLoaded = 0;
    update_status_bar_text(browser);

    SendSocketMessage(browser->id, CEVENT_DOWNLOAD_STARTED);
}

void
load_finished_cb(GtkMozEmbed *embed, GtkBrowser *browser)
{
    if (browser->stopButton)
        gtk_widget_set_sensitive(browser->stopButton, FALSE);
    if (browser->reloadButton)
        gtk_widget_set_sensitive(browser->reloadButton, TRUE);
    browser->loadPercent = 0;
    browser->bytesLoaded = 0;
    browser->maxBytesLoaded = 0;
    update_status_bar_text(browser);
    if (browser->progressBar)
        gtk_progress_set_percentage(GTK_PROGRESS(browser->progressBar), 0);

    // hacking, force to set focus to mozarea(gtk1.2)/gtkplug(gtk2) so that the keyboard event
    // can be propagated into our event handler.
#ifdef MOZ_WIDGET_GTK
    GtkWidget *mozarea = GTK_BIN(browser->mozEmbed)->child;
    GTK_WIDGET_SET_FLAGS(mozarea, GTK_HAS_FOCUS);
#endif
#ifdef MOZ_WIDGET_GTK2
    GtkPlug *plug = GTK_PLUG(browser->topLevelWindow);
    GdkNativeWindow plugId = gtk_plug_get_id(plug);
    GdkWindow *recipient = GDK_WINDOW(GTK_WIDGET(plug)->window);
    GdkDisplay *display = gdk_drawable_get_display(recipient);
    XEvent xevent;
  
    xevent.xclient.window = plugId;
    xevent.xclient.type = ClientMessage;
    xevent.xclient.message_type = gdk_x11_get_xatom_by_name_for_display (display, "_XEMBED");
    xevent.xclient.format = 32;
    xevent.xclient.data.l[0] = gtk_get_current_event_time ();
    xevent.xclient.data.l[1] = XEMBED_WINDOW_ACTIVATE;
    xevent.xclient.data.l[2] = 0;
    xevent.xclient.data.l[3] = 0;
    xevent.xclient.data.l[4] = 0;
    XSendEvent(GDK_WINDOW_XDISPLAY(recipient), plugId, False, NoEventMask, &xevent);

    xevent.xclient.data.l[0] = gtk_get_current_event_time ();
    xevent.xclient.data.l[1] = XEMBED_FOCUS_IN;
    xevent.xclient.data.l[2] = XEMBED_FOCUS_CURRENT;
    XSendEvent(GDK_WINDOW_XDISPLAY(recipient), plugId, False, NoEventMask, &xevent);
#endif

    nsCOMPtr<nsIWebBrowser> webBrowser;
    gtk_moz_embed_get_nsIWebBrowser(embed, getter_AddRefs(webBrowser));

    nsCOMPtr<nsIWebBrowserFocus> focus(do_GetInterface(webBrowser));
    if (focus)
        focus->Activate();

    // In this case, the downloadComplete and documentComplete events
    // are fired at the same time. On *Windows*, it's different.
    SendSocketMessage(browser->id, CEVENT_DOWNLOAD_COMPLETED);
    SendSocketMessage(browser->id, CEVENT_DOCUMENT_COMPLETED);
}


void
net_state_change_cb(GtkMozEmbed *embed, gint flags, guint status,
                    GtkBrowser *browser)
{
    WBTRACE("net_state_change_cb %d\n", flags);
    if (flags & GTK_MOZ_EMBED_FLAG_IS_REQUEST) {
        if (flags & GTK_MOZ_EMBED_FLAG_REDIRECTING)
            browser->statusMessage = "Redirecting to site...";
        else if (flags & GTK_MOZ_EMBED_FLAG_TRANSFERRING)
            browser->statusMessage = "Transferring data from site...";
        else if (flags & GTK_MOZ_EMBED_FLAG_NEGOTIATING)
            browser->statusMessage = "Waiting for authorization...";
    }

    if (status == GTK_MOZ_EMBED_STATUS_FAILED_DNS)
        browser->statusMessage = "Site not found.";
    else if (status == GTK_MOZ_EMBED_STATUS_FAILED_CONNECT)
        browser->statusMessage = "Failed to connect to site.";
    else if (status == GTK_MOZ_EMBED_STATUS_FAILED_TIMEOUT)
        browser->statusMessage = "Failed due to connection timeout.";
    else if (status == GTK_MOZ_EMBED_STATUS_FAILED_USERCANCELED)
        browser->statusMessage = "User canceled connecting to site.";

    if (flags & GTK_MOZ_EMBED_FLAG_IS_DOCUMENT) {
        if (flags & GTK_MOZ_EMBED_FLAG_START)
            browser->statusMessage = "Loading site...";
        else if (flags & GTK_MOZ_EMBED_FLAG_STOP)
            browser->statusMessage = "Done.";
    }
    else if (flags & GTK_MOZ_EMBED_FLAG_IS_NETWORK) {
        if (flags & GTK_MOZ_EMBED_FLAG_STOP) {
            nsCOMPtr<nsIWebBrowser> webBrowser;
            gtk_moz_embed_get_nsIWebBrowser(embed, getter_AddRefs(webBrowser));

            char buf[20];
            nsCOMPtr<nsIWebNavigation> webNav(do_QueryInterface(webBrowser));

            PRBool aCanGoForward = PR_FALSE;
            webNav->GetCanGoForward(&aCanGoForward);
            sprintf(buf, "forward=%d", aCanGoForward ? 1 : 0);
            SendSocketMessage(browser->id, CEVENT_COMMAND_STATE_CHANGE, buf);

            PRBool aCanGoBack = PR_FALSE;
            webNav->GetCanGoBack(&aCanGoBack);
            sprintf(buf, "back=%d", aCanGoBack ? 1 : 0);
            SendSocketMessage(browser->id, CEVENT_COMMAND_STATE_CHANGE, buf);
        }
    }

    update_status_bar_text(browser);
}

void
net_state_change_all_cb(GtkMozEmbed *embed, const char *uri,
                        gint flags, guint status,
                        GtkBrowser *browser)
{
    //  WBTRACE("net_state_change_all_cb %s %d %d\n", uri, flags, status);
}

void
progress_change_cb(GtkMozEmbed *embed, gint cur, gint max,
                   GtkBrowser *browser)
{
    WBTRACE("progress_change_cb cur %d max %d\n", cur, max);

    // avoid those pesky divide by zero errors
    if (max < 1) {
        if (browser->progressBar)
            gtk_progress_set_activity_mode(GTK_PROGRESS(browser->progressBar), FALSE);
        browser->loadPercent = 0;
        browser->bytesLoaded = cur;
        browser->maxBytesLoaded = 0;
        update_status_bar_text(browser);
    }
    else {
        browser->bytesLoaded = cur;
        browser->maxBytesLoaded = max;
        if (cur > max)
            browser->loadPercent = 100;
        else
            browser->loadPercent = (cur * 100) / max;
        update_status_bar_text(browser);
        if (browser->progressBar)
            gtk_progress_set_percentage(GTK_PROGRESS(browser->progressBar), browser->loadPercent / 100.0);
    }

    char buf[20];
    sprintf(buf, "%d", browser->loadPercent);
    SendSocketMessage(browser->id, CEVENT_DOWNLOAD_PROGRESS, buf);
}

void
progress_change_all_cb(GtkMozEmbed *embed, const char *uri,
                       gint cur, gint max,
                       GtkBrowser *browser)
{
    //WBTRACE("progress_change_all_cb %s cur %d max %d\n", uri, cur, max);
}

void
new_window_cb(GtkMozEmbed *embed, GtkMozEmbed **newEmbed, guint chromemask, GtkBrowser *browser)
{
    WBTRACE("new_window_cb\n");
    WBTRACE("embed is %p chromemask is %d\n", (void *)embed, chromemask);

    int bCmdCanceled = -1, waitCount = 0;
    AddTrigger(browser->id, CEVENT_BEFORE_NEWWINDOW, &bCmdCanceled);
    SendSocketMessage(browser->id, CEVENT_BEFORE_NEWWINDOW);

    PRIntervalTime sleepInterval = PR_MillisecondsToInterval(1);
    while (bCmdCanceled < 0 && waitCount++ < MAX_WAIT) {
        PR_Sleep(sleepInterval);
    }

    // do not create new window
    if (bCmdCanceled == 1)
        return;

    GtkBrowser *newBrowser = new_gtk_browser(chromemask);
    gtk_widget_set_usize(newBrowser->mozEmbed, 400, 400);
    *newEmbed = GTK_MOZ_EMBED(newBrowser->mozEmbed);
    WBTRACE("new browser is %p\n", (void *)*newEmbed);
}

void
visibility_cb(GtkMozEmbed *embed, gboolean visibility, GtkBrowser *browser)
{
    set_browser_visibility(browser, visibility);
}

void
destroy_brsr_cb(GtkMozEmbed *embed, GtkBrowser *browser)
{
    WBTRACE("destroy_brsr_cb\n");
    gtk_widget_destroy(browser->topLevelWindow);
}

gint
open_uri_cb(GtkMozEmbed *embed, const char *uri, GtkBrowser *browser)
{
    WBTRACE("open_uri_cb\n");

    int bCmdCanceled = -1, waitCount = 0;
    AddTrigger(browser->id, CEVENT_BEFORE_NAVIGATE, &bCmdCanceled);
    SendSocketMessage(browser->id, CEVENT_BEFORE_NAVIGATE, uri);

    PRIntervalTime sleepInterval = PR_MillisecondsToInterval(1);
    while (bCmdCanceled < 0 && waitCount++ < MAX_WAIT) {
        PR_Sleep(sleepInterval);
    }

    // do not load this URI
    if (bCmdCanceled == 1)
        return TRUE;

    // don't interrupt anything
    return FALSE;
}

void
size_to_cb(GtkMozEmbed *embed, gint width, gint height,
           GtkBrowser *browser)
{
    WBTRACE("*** size_to_cb %d %d\n", width, height);
    gtk_widget_set_usize(browser->mozEmbed, width, height);
}

void new_window_orphan_cb(GtkMozEmbedSingle *embed,
                          GtkMozEmbed **retval, guint chromemask,
                          gpointer data)
{
    WBTRACE("new_window_orphan_cb\n");
    WBTRACE("chromemask is %d\n", chromemask);
    GtkBrowser *newBrowser = new_gtk_browser(chromemask);
    *retval = GTK_MOZ_EMBED(newBrowser->mozEmbed);
    WBTRACE("new browser is %p\n", (void *)*retval);
}

void title_change_cb(GtkMozEmbed *embed, GtkBrowser *browser)
{
    nsEmbedCString buf;
    PRUnichar *unicode_title = gtk_moz_embed_get_title_unichar(embed);
    nsEmbedString title(unicode_title);
    ConvertUtf16ToUtf8(title, buf);
    SendSocketMessage(browser->id, CEVENT_TITLE_CHANGE, buf.get());
}

void status_text_change_cb(GtkMozEmbed *embed, gpointer request,
				gint status, gpointer message, GtkBrowser *browser)
{
    nsEmbedCString buf;
    nsEmbedString title((PRUnichar *)message);
    ConvertUtf16ToUtf8(title, buf);
    SendSocketMessage(browser->id, CEVENT_STATUSTEXT_CHANGE, buf.get());
}

// utility functions

void
update_status_bar_text(GtkBrowser *browser)
{
    gchar message[256];

    if (!browser->statusBar)
        return;

    gtk_statusbar_pop(GTK_STATUSBAR(browser->statusBar), 1);
    if (browser->tempMessage)
        gtk_statusbar_push(GTK_STATUSBAR(browser->statusBar), 1, browser->tempMessage);
    else {
        if (browser->loadPercent)
            g_snprintf(message, 255, "(%d%% complete, %d bytes of %d loaded)", browser->loadPercent, browser->bytesLoaded, browser->maxBytesLoaded);
        else if (browser->bytesLoaded)
            g_snprintf(message, 255, "(%d bytes loaded)", browser->bytesLoaded);
        else
            g_snprintf(message, 255, " ");
        gtk_statusbar_push(GTK_STATUSBAR(browser->statusBar), 1, message);
    }
}

void
update_temp_message(GtkBrowser *browser, const char *message)
{
    if (browser->tempMessage)
        g_free(browser->tempMessage);
    if (message)
        browser->tempMessage = g_strdup(message);
    else
        browser->tempMessage = 0;
    // now that we've updated the temp message, redraw the status bar
    update_status_bar_text(browser);
}

void
update_nav_buttons(GtkBrowser *browser)
{
    gboolean can_go_back;
    gboolean can_go_forward;
    can_go_back = gtk_moz_embed_can_go_back(GTK_MOZ_EMBED(browser->mozEmbed));
    can_go_forward = gtk_moz_embed_can_go_forward(GTK_MOZ_EMBED(browser->mozEmbed));
    if (can_go_back) {
        if (browser->backButton)
            gtk_widget_set_sensitive(browser->backButton, TRUE);
    }
    else {
        if (browser->backButton)
            gtk_widget_set_sensitive(browser->backButton, FALSE);
    }
    if (can_go_forward) {
        if (browser->forwardButton)
            gtk_widget_set_sensitive(browser->forwardButton, TRUE);
    }
    else {
        if (browser->forwardButton)
            gtk_widget_set_sensitive(browser->forwardButton, FALSE);
    }
}
