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

#ifndef _MozEmbed_H_
#define _MozEmbed_H_

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <gdk/gdkx.h>
#include <gtk/gtk.h>
#include "gtkmozembed.h"
#include "gtkmozembed_internal.h"
#include "nsCOMPtr.h"
#include "nsIWebBrowser.h"
#include "nsIWebBrowserFocus.h"
#include "nsIInterfaceRequestorUtils.h"
#include "nsIDOMMouseEvent.h"
#include "Util.h"

typedef struct _GtkBrowser {
    int id;
    GtkWidget  *topLevelWindow;
    GtkWidget  *topLevelVBox;
    GtkWidget  *menuBar;
    GtkWidget  *fileMenuItem;
    GtkWidget  *fileMenu;
    GtkWidget  *fileOpenNewBrowser;
    GtkWidget  *fileStream;
    GtkWidget  *fileClose;
    GtkWidget  *fileQuit;
    GtkWidget  *toolbarHBox;
    GtkWidget  *toolbar;
    GtkWidget  *backButton;
    GtkWidget  *stopButton;
    GtkWidget  *forwardButton;
    GtkWidget  *reloadButton;
    GtkWidget  *urlEntry;
    GtkWidget  *mozEmbed;
    GtkWidget  *progressAreaHBox;
    GtkWidget  *progressBar;
    GtkWidget  *statusAlign;
    GtkWidget  *statusBar;
    const char *statusMessage;
    int         loadPercent;
    int         bytesLoaded;
    int         maxBytesLoaded;
    char       *tempMessage;
    gboolean menuBarOn;
    gboolean toolBarOn;
    gboolean locationBarOn;
    gboolean statusBarOn;
} GtkBrowser;

// the list of browser windows currently open
extern WBArray gBrowserArray;

GtkBrowser *new_gtk_browser    (guint32 chromeMask);
void        set_browser_visibility (GtkBrowser *browser,
                                    gboolean visibility);

void install_mozembed_cb(GtkBrowser *browser);

// callbacks from the UI
void     back_clicked_cb    (GtkButton   *button,
                             GtkBrowser *browser);
void     stop_clicked_cb    (GtkButton   *button,
                             GtkBrowser *browser);
void     forward_clicked_cb (GtkButton   *button,
                             GtkBrowser *browser);
void     reload_clicked_cb  (GtkButton   *button,
                             GtkBrowser *browser);
void     url_activate_cb    (GtkEditable *widget,
                             GtkBrowser *browser);
void     menu_open_new_cb   (GtkMenuItem *menuitem,
                             GtkBrowser *browser);
void     menu_stream_cb     (GtkMenuItem *menuitem,
                             GtkBrowser *browser);
void     menu_close_cb      (GtkMenuItem *menuitem,
                             GtkBrowser *browser);
void     menu_quit_cb       (GtkMenuItem *menuitem,
                             GtkBrowser *browser);

gboolean delete_cb          (GtkWidget *widget, GdkEventAny *event,
                             GtkBrowser *browser);

void set_focus_cb(GtkWindow *window, GtkWidget *focus, 
                   GtkBrowser * browser);

void     destroy_cb         (GtkWidget *widget,
                             GtkBrowser *browser);

// callbacks from the widget
void location_changed_cb  (GtkMozEmbed *embed, GtkBrowser *browser);
void load_started_cb      (GtkMozEmbed *embed, GtkBrowser *browser);
void load_finished_cb     (GtkMozEmbed *embed, GtkBrowser *browser);
void net_state_change_cb  (GtkMozEmbed *embed, gint flags,
                           guint status, GtkBrowser *browser);
void net_state_change_all_cb (GtkMozEmbed *embed, const char *uri,
                              gint flags, guint status,
                              GtkBrowser *browser);
void progress_change_cb   (GtkMozEmbed *embed, gint cur, gint max,
                           GtkBrowser *browser);
void progress_change_all_cb (GtkMozEmbed *embed, const char *uri,
                             gint cur, gint max,
                             GtkBrowser *browser);
void new_window_cb        (GtkMozEmbed *embed,
                           GtkMozEmbed **retval, guint chromemask,
                           GtkBrowser *browser);
void visibility_cb        (GtkMozEmbed *embed,
                           gboolean visibility,
                           GtkBrowser *browser);
void destroy_brsr_cb      (GtkMozEmbed *embed, GtkBrowser *browser);
gint open_uri_cb          (GtkMozEmbed *embed, const char *uri,
                           GtkBrowser *browser);
void size_to_cb           (GtkMozEmbed *embed, gint width,
                           gint height, GtkBrowser *browser);

// callbacks from the singleton object
void new_window_orphan_cb (GtkMozEmbedSingle *embed,
                           GtkMozEmbed **retval, guint chromemask,
                           gpointer data);

// some utility functions
void update_status_bar_text  (GtkBrowser *browser);
void update_temp_message     (GtkBrowser *browser,
                              const char *message);
void update_nav_buttons      (GtkBrowser *browser);

gint dom_mouse_click_cb   (GtkMozEmbed *embed, nsIDOMMouseEvent *event, GtkBrowser *browser);

void title_change_cb(GtkMozEmbed *embed, GtkBrowser *browser);

void status_text_change_cb(GtkMozEmbed *embed, gpointer request,
                                gint status, gpointer message, GtkBrowser *browser);

// socket message callbak functions
void SocketMsgHandler(const char *pMsg);
void HandleSocketMessage(gpointer data, gpointer user_data);

#ifdef MOZ_WIDGET_GTK
gboolean gs_prepare_cb  (gpointer  source_data,
                         GTimeVal *current_time,
                         gint     *timeout,
                         gpointer  user_data);

gboolean gs_check_cb (gpointer  source_data,
                      GTimeVal *current_time,
                      gpointer  user_data);

gboolean gs_dispatch_cb (gpointer  source_data,
                         GTimeVal *current_time,
                         gpointer  user_data);
#else
gboolean gs_prepare_cb  (GSource  *source,
                         gint     *timeout);

gboolean gs_check_cb (GSource    *source);

gboolean gs_dispatch_cb (GSource    *source,
                         GSourceFunc callback,
                         gpointer  user_data);
#endif /* MOZ_WIDGET_GTK */

#endif
