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
import java.util.List;


/**
 * Concrete implementation of the AppAssociationReader class for Gnome.
 */
public class GnomeAppAssociationReader implements AppAssociationReader {

    /**
     * Returns the description associated with the given mime type.
     *
     * @param mimeType Given mime type
     * @return String
     */
    public String getDescriptionByMimeType(String mimeType) {
        return GnomeAssociationUtil.getDescriptionByMimeType(mimeType);
    }
  
    /**
     * Returns the description associated with the given file extension.
     *
     * @param fileExt Given file extension
     * @return String
     */
    public String getDescriptionByFileExt(String fileExt) {
        // Removes the leading '.' character from the file extension if exists.
        fileExt = AppUtility.removeDotFromFileExtension(fileExt);        
        if (getMimeTypeByFileExt(fileExt) == null) {
            return null;
        } else {
            return getDescriptionByMimeType(getMimeTypeByFileExt(fileExt));
        }
    }

    /**
     * Returns the mime type associated with the given URL, by checking the content of 
     *
     * the URL.
     * @param url The specified URL
     * @return String
     */
    public String getMimeTypeByURL(URL url) {
        return GnomeAssociationUtil.getMimeTypeByURL(url);
    }        

    /**
     * Returns the file extensione list associated with the given mime type.
     *
     * @param mimeType Given mime type
     * @return String
     */
    public List getFileExtListByMimeType(String mimeType) {
        return GnomeAssociationUtil.getFileExtListByMimeType(mimeType);
    }
  
    /**
     * Returns the mime type associated with the given file extension.
     *
     * @param fileExt Given file extension
     * @return String
     */
    public String getMimeTypeByFileExt(String fileExt) {
        // Removes the leading '.' character from the file extension if exists.
        fileExt = AppUtility.removeDotFromFileExtension(fileExt);        
        return GnomeAssociationUtil.getMimeTypeByFileExt(fileExt);
    }
  
    /**
     * Returns the icon file name associated with the given mime type.
     *
     * @param mimeType Given mime type.
     * @return String
     */
    public String getIconFileNameByMimeType(String mimeType) {
        return GnomeAssociationUtil.getIconFileNameByMimeType(mimeType);
    }
  
    /**
     * Returns the icon file name associated with the given file extension.
     *
     * @param fileExt Given file extension.
     * @return String
     */
    public String getIconFileNameByFileExt(String fileExt) {
        // Remove the leading '.' character from the file extension if exists.
        fileExt = AppUtility.removeDotFromFileExtension(fileExt);        
        if (getMimeTypeByFileExt(fileExt) == null) {
            return null;
        } else {       
            return getIconFileNameByMimeType(getMimeTypeByFileExt(fileExt));
        }
    }
 
    /**
     * Returns the action list associated with the given file extension.
     *
     * @param mimeType the given mime type.
     * @return List the action list associated with the given mime type.
     */
    public List getActionListByMimeType(String mimeType) {
        return GnomeAssociationUtil.getActionListByMimeType(mimeType);
    }

    /**
     * Returns the action list associated with the given mime type.
     *
     * @param fileExt the given file extension.
     * @return List the action list associated with the given file extension.
     */
    public List getActionListByFileExt(String fileExt) {
        // Remove the leading '.' character from the file extension if exists.
        fileExt = AppUtility.removeDotFromFileExtension(fileExt);        
        if (getMimeTypeByFileExt(fileExt) == null) {
            return null;
        } else {
            return getActionListByMimeType(getMimeTypeByFileExt(fileExt));        
        }
    }

    /**
     * Returns true if the mime type exists in the MIME database.
     *
     * @param mimeType given mimeType
     * @return true if the mime type exists in the MIME database
     */
    public boolean isMimeTypeExist(String mimeType) {
        // The given mime type may exist in either .mime files or .keys files.
        // First check if it's registered in .mime files.
        boolean isExist = GnomeAssociationUtil.isMimeTypeExist(mimeType);

        if (!isExist) { 
            // Then check if it exists in .keys files by checking the association info.
            String iconFileName = getIconFileNameByMimeType(mimeType);
            String description = getDescriptionByMimeType(mimeType);
            List actionList = getActionListByMimeType(mimeType);

            // If no association info, it's supposed not exists.       
            if (iconFileName != null || description != null || actionList != null) {
                isExist = true;
            }
        }
        
        return isExist;
    }
    
    /**
     * Returns true if the file extension exists in the MIME database.
     * 
     * @param fileExt given file extension 
     * @return true if the file extension exists in the MIME database
     */
    public boolean isFileExtExist(String fileExt) {
        // Remove the leading '.' character from the file extension if exists.
        fileExt = AppUtility.removeDotFromFileExtension(fileExt);        
       
        return GnomeAssociationUtil.isFileExtExist(fileExt);
    }
}
