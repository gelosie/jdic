/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */ 

package org.jdesktop.jdic.filetypes.internal;


/**
 * Java wrapper class for GnomeVFS API both on Linux and Solaris platforms.
 * <P>
 * GnomeVFS, a filesystem abstraction library, contains convenient file utilities,
 * including a comphrehensive MIME database/Application registry.
 * <P>
 * The MIME database/Application registry are stored as plain text files:
 * $gnome_prefix/share/mime-info/*.mime, *.keys and $gnome_prefix/share/application-registry/*.applications.
 */

/*
 * Following gives an example on mime type text/html stored in 
 * the MIME database/Application registry. Each of the Java wrapper method for native
 * functions gives the result corresponding to this example.
 * ----------------------------------------------------------------------
 * 1. $gnome_prefix/share/mime-info/gnome-vfs.keys: 
 *    text/html
 *      description=HTML page
 *      [az]description=...
 *     	......
 *      [zh_TW]description=...
 *      default_action_type=application
 *      default_application_id=htmlview
 *      short_list_component_iids=OAFIID:nautilus_mozilla_content_view:1ee70717-57bf-4079-aae5-922abdd576b1,OAFIID
 *          :Nautilus_Text_View
 *      short_list_component_iids_for_novice_user_level=OAFIID:nautilus_mozilla_content_view
 *          :1ee70717-57bf-4079-aae5-922abdd576b1,OAFIID:nautilus_text_view:fa466311-17c1-435c-8231-c9fc434b6437
 *      short_list_component_iids_for_intermediate_user_level=OAFIID:nautilus_mozilla_content_view
 *          :1ee70717-57bf-4079-aae5-922abdd576b1,OAFIID:nautilus_text_view:fa466311-17c1-435c-8231-c9fc434b6437
 *      short_list_component_iids_for_advanced_user_level=OAFIID:nautilus_mozilla_content_view
 *          :1ee70717-57bf-4079-aae5-922abdd576b1,OAFIID:nautilus_text_view:fa466311-17c1-435c-8231-c9fc434b6437
 *      short_list_application_ids_for_novice_user_level=htmlview,mozilla,netscape,galeon
 *      short_list_application_ids_for_intermediate_user_level=htmlview,mozilla,netscape,galeon
 *      short_list_application_ids_for_advanced_user_level=htmlview,mozilla,netscape,lynx,galeon
 *      category=Documents/World Wide Web
 *      use_category_default=yes
 *
 * 2. $gnome_prefix/share/mime-info/gnome-vfs.mime
 *    text/html
 *      ext: html htm HTML
 *
 * 3. $gnome_prefix/share/mime-info/test-html.keys: overrides the items in system default gnome-vfs.keys
 *      ext/html:
 *      open=vi %f
 *      view=vi %f
 * 	    icon-filename=/usr/share/pixmaps/html.png
 *
 * 4. $gnome_prefix/share/mime-info/test-html.mime: overrides the items in system default gnome-vfs.mime
 *	    text/html
 *	     ext: testhtml testhtm
 *
 * 5. $gnome_prefix/share/application-registry/gnome-vfs.application
 *	  htmlview
 *	    command=htmlview
 *	    name=htmlview
 *      can_open_multiple_files=true
 *      expects_uris=true
 *      requires_terminal=false
 *      supported_uri_schemes=file,http,ftp,telnet,gopher
 *      mime_types=text/html,x-directory/webdav,x-directory/webdav-prefer-directory,image/gif,
 *      image/jpeg,text/xml
 *
 * 6. $gnome_prefix/share/application-registry/test-html.application: 
 *     overrides the system default gnome-vfs.mime.
 * -----------------------------------------------------------------------
 */

public class GnomeVfsWrapper {
    static {
        System.loadLibrary("jdic");
    }

    /* Description and icon_filename keys used on GNOME desktop */
    public final static String  GNOME_VFS_MIME_KEY_DESCRIPTION = "description";
    public final static String  GNOME_VFS_MIME_DEFAULT_KEY_ICON_FILENAME = "icon_filename";
    
    /**
     * Suppress default constructor for noninstantiability.
     */
    private GnomeVfsWrapper() {}

    /**
     * Returns the the mime type of the specified uri. 
     * <PRE>
     * For example: 
     *     for uri file:/test.html, returns text/html.
     *     for uri http://sceri.prc.sun.com/index.html, it also returns text/html.
     * </PRE>
     */
    public static native String gnome_vfs_get_mime_type(String uri);

    /**
     * Query the MIME database for a description of the specified MIME type.
     * <PRE>
     * For example: 
     *     for text/html, returns HTML page.
     * </PRE>
     */
    public static native String gnome_vfs_mime_get_description(String mimeType);

    /**
     * Query the MIME database for an icon representing the specified MIME type.
     * <PRE>
     * For example: 
     *     for text/html, returns /usr/share/pixmaps/html.png.
     * </PRE>
     */
    public static native String gnome_vfs_mime_get_icon(String mimeType);

    /**
     * Return a string array with all of the keys associated with the mime_type.
     * <PRE>
     * For example:
     *   for text/html, returns
     *       short_list_application_ids_for_intermediate_user_level
     *       short_list_application_ids_for_novice_user_level
     *       short_list_component_iids_for_intermediate_user_level
     *       short_list_component_iids_for_novice_user_level
     *       default_action_type
     *       category
     *       short_list_application_ids_for_advanced_user_level
     *       short_list_component_iids
     *       short_list_component_iids_for_advanced_user_level
     *       default_application_id
     * </PRE>
     */
    public static native String[] gnome_vfs_mime_get_key_list(String mimeType);
  
    /**
     * Retrieve the value associated with key for te given mime type.
     * <P>
     * The following keys are used in GNOME desktop:
     * open, icon-filename, view, ascii-view, fm-open, fm-view, fm-ascii-view.
     * <PRE>
     * For example: 
     *     for text/html and key is "open", returns vi %f.
     *     if key is category, returns Documents/World Wide Web.
     * </PRE>
     */ 
    public static native String gnome_vfs_mime_get_value(String mimeType, String key);
  
    /**
     * Query the MIME database for the application to be executed on files of MIME type by default.
     * Returns the command string running this application.
     * <PRE>
     * For example: 
     *     for text/html, returns htmlview.
     * </PRE>
     */
    public static native String gnome_vfs_mime_get_default_application_command(String mimeType);

    /**
     * Return all the mime types registered in the MIME type database.
     */
    public static native String[] gnome_vfs_get_registered_mime_types();
  
    /**
     * Return a list of extensions for this mime-type.
     * <PRE>
     * For example:
     *     for text/html, returns testhtml and testhtm from test-html.mime)
     *     not the system default html, htm and HTML from gnome-vfs.mime.
     * </PRE>
     */
    public static native String[] gnome_vfs_mime_get_extensions_list(String mimeType);
  
    /**
     * Return the value of the specified environment variable, or NULL if there is no match.
     * This method is not related with Gnome VFS API or library.
     */
    public static native String getenv(String envName);
}
