/*
 * Copyright (C) 2008 Sun Microsystems, Inc. All rights reserved. Use is
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

package org.jdic.web;

/**
 * The Internet Explorer constans from MSDN.
 * @author uta
 */    
public interface BrComponentConsts {
    /**
     * Images will be downloaded from the server and displayed if 
     * these flags are set. They will not be downloaded if the 
     * flags are not set.
     */ 
    int DLCTL_DLIMAGES = 0x00000010;
    /**
     * Videos will be downloaded from the server and displayed or played if 
     * these flags are set. They will not be downloaded if the 
     * flags are not set.
     */ 
    int DLCTL_VIDEOS = 0x00000020;
    /**
     * Sounds will be downloaded from the server and played if 
     * these flags are set. They will not be downloaded if the 
     * flags are not set.
     */ 
    int DLCTL_BGSOUNDS = 0x00000040;

    /**
     * Scripts will not be executed.
     */
    int DLCTL_NO_SCRIPTS = 0x00000080;
    
    /**
     * Java applets will not be executed.
     */
    int DLCTL_NO_JAVA = 0x00000100;

    /**
     * ActiveX controls will not be executed.
     */
    int DLCTL_NO_RUNACTIVEXCTLS = 0x00000200;
    
    /**
     * ActiveX controls will not be downloaded.
     */
    int DLCTL_NO_DLACTIVEXCTLS = 0x00000400;

    /**
     * The page will only be downloaded, not displayed. No rendering involved
     */
    int DLCTL_DOWNLOADONLY = 0x00000800;

    /**
     * The WebBrowser Control will download and parse a frameSet, but not 
     * the individual frame objects within the frameSet.
     */ 
    int DLCTL_NO_FRAMEDOWNLOAD = 0x00001000;

    /**
     * These flags cause cache refreshes. With DLCTL_RESYNCHRONIZE, the server 
     * will be asked for update status. Cached files will be used if the server 
     * indicates that the cached information is up-to-date.
     */ 
    int DLCTL_RESYNCHRONIZE = 0x00002000;
     /**
     * With DLCTL_PRAGMA_NO_CACHE, files will be re-downloaded from the server 
     * regardless of the update status of the files.
     */
    int DLCTL_PRAGMA_NO_CACHE = 0x00004000;

    /**
     * Behaviors are not downloaded and are disabled in the document
     */
    int DLCTL_NO_BEHAVIORS = 0x00008000;

    /**
     * Behaviors are not downloaded and are disabled in the document
     */
    int DLCTL_NO_METACHARSET = 0x00010000;

    /**
     * Internet Explorer 5 or later. Controls how nonnative URLs are 
     * transmitted over the Internet. Nonnative refers to characters outside 
     * the multibyte encoding of the URL. If this flag is set, the URL is not 
     * submitted to the server in UTF-8 encoding.
     */
    int DLCTL_URL_ENCODING_DISABLE_UTF8 = 0x00020000;

    /**
     * Internet Explorer 5 or later. Controls how nonnative URLs are 
     * transmitted over the Internet. Nonnative refers to characters outside 
     * the multibyte encoding of the URL. If this flag is set, the URL is not 
     * submitted to the server in UTF-8 encoding.
     */
    int DLCTL_URL_ENCODING_ENABLE_UTF8 = 0x00040000;

    int DLCTL_NOFRAMES = 0x00080000;

    /**
     * The WebBrowser Control always operates in offline mode.
     */
    int DLCTL_FORCEOFFLINE = 0x10000000;

    //???
    int DLCTL_NO_CLIENTPULL = 0x20000000;

    /**
     * No user interface will be displayed during downloads.
     */
    int DLCTL_SILENT = 0x40000000;

    /**
     * The WebBrowser Control will operate in offline mode if not connected 
     * to the Internet.
     */
    int DLCTL_OFFLINEIFNOTCONNECTED = 0x80000000;
    /**
     * the default action
     */
    int DLCTL_DEFAULT = 0x80000000;
}
