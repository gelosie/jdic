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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.jdic.filetypes.Action;


/**
 * Utility class for accessing the system association info for Gnome.
 */
public class GnomeAssociationUtil {
    /* Description and icon_filename keys used on GNOME desktop */
    public final static String  GNOME_VFS_MIME_KEY_DESCRIPTION
            = GnomeVfsWrapper.GNOME_VFS_MIME_KEY_DESCRIPTION;
    public final static String  GNOME_VFS_MIME_KEY_ICON_FILENAME
            = GnomeVfsWrapper.GNOME_VFS_MIME_DEFAULT_KEY_ICON_FILENAME;

    /**
     * Suppress default constructor for noninstantiability.
     */
    private GnomeAssociationUtil() {}
  
    /**
     * Returns the mime type associated with the given file extension.
     * If the file extension doesn't exist in the MIME database, no mime type is returned.
     *
     */
    public static String getMimeTypeByFileExt(String fileExt) {
        String resultMimeType = null;
        String[] allMimeTypes = GnomeVfsWrapper.gnome_vfs_get_registered_mime_types();

        if (allMimeTypes == null) {
            return null;
        } 

        for (int i = 0; i < allMimeTypes.length; i++) {
            String curMimeType = allMimeTypes[i];
            String[] fileExtensions = GnomeVfsWrapper.gnome_vfs_mime_get_extensions_list(curMimeType);

            if (fileExtensions != null) {
                for (int j = 0; j < fileExtensions.length; j++) {
                    if (fileExtensions[j].equals(fileExt)) {
                        resultMimeType = allMimeTypes[i];
                        break;
                    }
                }
            }
            if (resultMimeType != null) { 
                break;
            }
        }

        return resultMimeType;
    }
  
    /**
     * Returns the file extension list associated with the given mime type.
     *
     * @param mimeType Given mime type
     */
    public static List getFileExtListByMimeType(String mimeType) {
        String[] fileExtensions = GnomeVfsWrapper.gnome_vfs_mime_get_extensions_list(mimeType);

        if (fileExtensions == null) {
            return null;
        } else {
            // All returned file extensions has no leading "." character.
            List fileExtList = new ArrayList();
            for (int index = 0; index < fileExtensions.length; index++) {
                fileExtList.add(fileExtensions[index]);
            }
            
            return fileExtList;
        }
    }

    /**
     * Returns the icon file name associated with the given mime type.
     *
     */
    public static String getIconFileNameByMimeType(String mimeType) {
        return GnomeVfsWrapper.gnome_vfs_mime_get_icon(mimeType);
    }
  
    /**
     * Returns the description associated with the given mime type.
     *
     * @param mimeType Given mime type
     * @return String
     */
    public static String getDescriptionByMimeType(String mimeType) {
        return GnomeVfsWrapper.gnome_vfs_mime_get_description(mimeType);
    }

    /**
     * Returns the action list associated with the given mime type.
     */
    public static List getActionListByMimeType(String mimeType) {
        List actionList = new ArrayList();
        Action oneAction = null;

        // Check the keys associated with the mime type which may include below keys:
        //   short_list_application_ids_for_intermediate_user_level
        //   short_list_application_ids_for_novice_user_level
        //   short_list_application_ids_for_advanced_user_level
        //   short_list_component_iids_for_intermediate_user_level
        //   short_list_component_iids_for_novice_user_level
        //   short_list_component_iids
        //   short_list_component_iids_for_advanced_user_level
        //   default_action_type
        //   category
        //   default_application_id
        String[] keys = GnomeVfsWrapper.gnome_vfs_mime_get_key_list(mimeType);
        if (keys != null) {
            String command = null;
            for (int i = 0; i < keys.length; i++) {
                command = GnomeVfsWrapper.gnome_vfs_mime_get_value(mimeType, keys[i]);
                if (command != null) {
                    oneAction = new Action(keys[i], command);
                    actionList.add(oneAction);
                }
            }
        } 
        
        // --- As from the spec for gnome-vfs---
        // If the default application for a mime type is specified, it will always be used.
        // and an error will be given if the application is not available. If the default application is not specified, the first
        // *available* application on the short list of viewers will be used.
         
        // Get the default/first available application's command to construct an "open" action.
        // Which will be used by upper level launcher code.
        String defaultCmd = GnomeVfsWrapper.gnome_vfs_mime_get_default_application_command(mimeType);
        if (defaultCmd != null) {
            actionList.add(new Action("open", defaultCmd));
        }
      
        if (actionList.isEmpty()) {
            return null;
        } else {
            return actionList;
        }
    }
  
    /**
     * Returns the mime type associated with the given URL, by checking the content of the URL.
     *
     */
    public static String getMimeTypeByURL(URL url) {
        // The URL object specified by the user shouls be converted from an File object,
        // such as file:/user/local/test.html, or file:///user/local/test.html.
        return GnomeVfsWrapper.gnome_vfs_get_mime_type(url.toString());
    }

    /**
     * Returns true if the given mime type exists in the GnomeVFS MIME database.
     */
    public static boolean isMimeTypeExist(String mimeType) {
        // Check that the mime type is known and not deleted.
        boolean isMimeTypeExist = false;
        String[] allMimeTypes = GnomeVfsWrapper.gnome_vfs_get_registered_mime_types();

        if (allMimeTypes == null) {
            return false;
        }

        for (int i = 0; i < allMimeTypes.length; i++) {
            if (mimeType.equals(allMimeTypes[i])) {
                isMimeTypeExist = true;
                break;
            }
        }

        return isMimeTypeExist;
}               

    /**
     * Returns true if the given file extension exists in the GnomeVFS MIME database.
     */
    public static boolean isFileExtExist(String fileExt) {
        return (getMimeTypeByFileExt(fileExt) != null) ? true : false;
    }

    /**
     * Returns the value of an environment variable.
     * It's not related with GnomeVFS API or library. 
     */
    public static String getEnv(String envName) {
        return GnomeVfsWrapper.getenv(envName);
    }
}
