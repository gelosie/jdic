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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.jdesktop.jdic.filetypes.Action;
import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.RegisterFailedException;


/**
 * Concrete implementation of the AppAssociationWriter class for Gnome.
 */
public class GnomeAppAssociationWriter implements AppAssociationWriter {
    // Gnome system MIME database dir on Linux.
    // First check the environment variable GNOMEDIR. If it's not set, 
    // use the default/hard-coded /usr/share as top-level dir of the mime-info subdirectory.
    static String GNOMEDIR_VALUE = GnomeAssociationUtil.getEnv("GNOMEDIR");
    static String GNOME_LINUX_SYSTEM_SHARE_DIR = 
       (GNOMEDIR_VALUE == null) ? "/usr/share/" : (GNOMEDIR_VALUE + "/share/");
    static String GNOME_SOLARIS_SYSTEM_SHARE_DIR = "/usr/share/gnome/";

    // System mime-info directory.
    static String GNOME_LINUX_SYSTEM_MIME_INFO_DIR = GNOME_LINUX_SYSTEM_SHARE_DIR + "mime-info/";
    static String GNOME_SOLARIS_SYSTEM_MIME_INFO_DIR = GNOME_SOLARIS_SYSTEM_SHARE_DIR + "mime-info/";
    
    // System application-registry directory.
    static String GNOME_LINUX_SYSTEM_APPLICATION_REGISTRY_DIR = GNOME_LINUX_SYSTEM_SHARE_DIR 
        + "application-registry/";
    static String GNOME_SOLARIS_SYSTEM_APPLICATION_REGISTRY_DIR = GNOME_SOLARIS_SYSTEM_SHARE_DIR 
        + "application-registry/";

    // Current system mime-info and application-registry directory.
    static String OSNAME = System.getProperty("os.name").toLowerCase();
    static String GNOME_SYSTEM_MIME_INFO_DIR = 
        OSNAME.equals("linux") ? GNOME_LINUX_SYSTEM_MIME_INFO_DIR : GNOME_SOLARIS_SYSTEM_MIME_INFO_DIR;
    static String GNOME_SYSTEM_APPLICATION_REGISTRY_DIR = 
        OSNAME.equals("linux") ? 
            GNOME_LINUX_SYSTEM_APPLICATION_REGISTRY_DIR : GNOME_SOLARIS_SYSTEM_APPLICATION_REGISTRY_DIR;

    // User mime-info and application-info directory.
    static String GNOME_USER_MIME_INFO_DIR = System.getProperty("user.home") + "/.gnome/mime-info/";
    static String GNOME_USER_APPLICATION_INFO_DIR = System.getProperty("user.home") + "/.gnome/application-info/";

    // Suffixes for the generated .mime, .keys and .applications files.
    static String MIME_SUFFIX = ".mime";
    static String KEYS_SUFFIX = ".keys";
    static String APPLICATIONS_SUFFIX = ".applications";

    // Generated default application ID and command from the given open action, which will be written into .keys and .applications files.
    private String defaultAppID = null;
    private String defaultAppCommand = null;
    
    /**
     * Converts the specified file extension list to a file extension string.
     * <P>
     * Notice: Since all the given file extensions have a leading '.' character,
     * remove this '.' before write it to .mime file.
     * 
     * @see madhatter.association.Association#addFileExtension 
     */
    private String fileExtListToString(List fileExtList) {
        String fileExtListString = "";
        Iterator fileExtIter = fileExtList.iterator();
        String temFileExt;

        if (fileExtIter != null) {
            while (fileExtIter.hasNext()) {
                temFileExt = (String) fileExtIter.next();
                
                if (temFileExt != null) {
                    // Remove the leading '.' character if exists.
                    temFileExt = AppUtility.removeDotFromFileExtension(temFileExt);
                    
                    if (fileExtListString.length() == 0) {
                        fileExtListString = fileExtListString.concat(temFileExt);
                    } else {
                        fileExtListString = fileExtListString.concat(' ' + temFileExt);
                    }
                }
            }
        } else {
            fileExtListString = null;
        }
    
        return fileExtListString;
    }

    /**
     * Returns the absolute system .mime file path by the name field of the specified association.
     */
    private String getSystemDotMimeFilePath(Association assoc) {
        return GNOME_SYSTEM_MIME_INFO_DIR + assoc.getName() + MIME_SUFFIX;
    }
    
    /**
     * Returns the absolute system .keys file path by the name field of the specified association.
     */
    private String getSystemDotKeysFilePath(Association assoc) {
        return GNOME_SYSTEM_MIME_INFO_DIR + assoc.getName() + KEYS_SUFFIX;
    }

    /**
     * Returns the absolute system .applications file path by the name field of the specified association.
     */
    private String getSystemDotApplicationsFilePath(Association assoc) {
        return GNOME_SYSTEM_APPLICATION_REGISTRY_DIR + assoc.getName() + APPLICATIONS_SUFFIX;
    }

    /**
     * Returns the absolute user .mime file path by the name field of the specified association.
     */
    private String getUserDotMimeFilePath(Association assoc) {
        return GNOME_USER_MIME_INFO_DIR + assoc.getName() + MIME_SUFFIX;
    }
    
    /**
     * Returns the absolute user .keys file path by the name field of the specified association.
     */
    private String getUserDotKeysFilePath(Association assoc) {
        return GNOME_USER_MIME_INFO_DIR + assoc.getName() + KEYS_SUFFIX;
    }

    /**
     * Returns the absolute user .applications file path by the name field of the specified association.
     */
    private String getUserDotApplicationsFilePath(Association assoc) {
        return GNOME_USER_APPLICATION_INFO_DIR + assoc.getName() + APPLICATIONS_SUFFIX;
    }

    /**
     * Check if the system MIME database exists and is writable, by checking
     * the mime-info and application-registry/application-info directories. By default,
     * these mime info directories should be installed and kept there.
     * 
     * @throws IOException if the system MIME info directory doesn't exist, or no write
     *         permission to access.
     */ 
    private void checkSystemMIMEDatabase() throws IOException {
        File tempFile = null;
        
        // Check if the system mime-info and application-registry directories exist.
        tempFile = new File(GNOME_SYSTEM_MIME_INFO_DIR);
        if (!tempFile.exists()) {
            throw new IOException("The system MIME info directory doesn't exist: "
                    + GNOME_SYSTEM_MIME_INFO_DIR
                    + ". Make sure Gnome 2.0+ is installed and env GNOMEDIR is set properly.");
        }

        tempFile = new File(GNOME_SYSTEM_APPLICATION_REGISTRY_DIR);
        if (!tempFile.exists()) {
            throw new IOException("The system MIME info directory doesn't exist: "
                    + GNOME_SYSTEM_APPLICATION_REGISTRY_DIR
                    + ". Make sure Gnome 2.0+ is installed and env GNOMEDIR is set properly.");
        }

        // Check if the system mime-info and application-registry directories are writable.
        tempFile = new File(GNOME_SYSTEM_MIME_INFO_DIR);
        if (!tempFile.canWrite()) {
            throw new IOException("No write permission to the system MIME info directory: "
                    + GNOME_SYSTEM_MIME_INFO_DIR);
        }

        tempFile = new File(GNOME_SYSTEM_APPLICATION_REGISTRY_DIR);
        if (!tempFile.canWrite()) {
            throw new IOException("No write permission to the system MIME info directory: "
                    + GNOME_SYSTEM_APPLICATION_REGISTRY_DIR);
        }
    }

    /**
     * Check if the user MIME database exists and is writable, by checking
     * the mime-info and application-registry directories. By default,
     * these directories should be installed and kept there.
     *
     * @throws IOException if the user MIME info directory doesn't exist, or no write
     *         permission to access.
     */ 
    private void checkUserMIMEDatabase() throws IOException {
        File tempFile = null;
    
        // Check if the user mime-info and application-info directories exist.
        tempFile = new File(GNOME_USER_MIME_INFO_DIR);
        if (!tempFile.exists()) {
            // Create the directory.
            boolean mksucceed = tempFile.mkdirs();
            if (mksucceed == false) {
                throw new IOException("The user MIME info directory doesn't exist, "
                        + "and fails to be created: " + GNOME_USER_MIME_INFO_DIR);
            }
        }

        tempFile = new File(GNOME_USER_APPLICATION_INFO_DIR);
        if (!tempFile.exists()) {
            // Create the directory.
            boolean mksucceed = tempFile.mkdirs();
            if (mksucceed == false) {
                throw new IOException("The user MIME info directory doesn't exist, "
                        + "and fails to be created: " + GNOME_USER_APPLICATION_INFO_DIR);
            }
        }

        // Check if the user mime-info and application-info directories is writable.
        tempFile = new File(GNOME_USER_MIME_INFO_DIR);
        if (!tempFile.canWrite()) {
            throw new IOException("No write permission to the user MIME info directory: "
                    + GNOME_USER_MIME_INFO_DIR);
        }

        tempFile = new File(GNOME_USER_APPLICATION_INFO_DIR);
        if (!tempFile.canWrite()) {
            throw new IOException("No write permission to the user MIME info directory: "
                    + GNOME_USER_MIME_INFO_DIR);
        }
    }


    /**
     * Creates one mime file(.mime, .keys or .applications) in MIME database if not exist.
     * 
     * @throws IOException if the given file fails to be created.
     */
    private void createFile(String mimeFilePath) throws IOException {
        boolean createSucceed = false;
        
        File mimeFile = new File(mimeFilePath);
        if (!mimeFile.exists()) {
            createSucceed = mimeFile.createNewFile();
            if (!createSucceed) {
                throw new IOException("Create MIME file: " + mimeFilePath + " failed.");        
            }
        }
    }

    /**
     * Here we only accept and write the "open" action and *ignore* other actions.
     * Since other actions are not applied/used on Gnome desktop at all.
     * The parsed application ID and application command info will be write into .keys and .application files.  
     */
    private void parseOpenAction(Association assoc) {
        List actionList = assoc.getActionList();
        
        if (actionList == null) {
            return;
        } else {
            String verb = null;
            Iterator actionIter = actionList.iterator();

            while (actionIter.hasNext() && defaultAppCommand == null) {
                Action oneAction = (Action) actionIter.next();
                verb = oneAction.getVerb();
                if (verb.equalsIgnoreCase("open")) {
                    defaultAppCommand = oneAction.getCommand().trim();
                }
            }
            
            if (defaultAppCommand != null) {
                // Check the application command and application id, which will be written into .applications file.
                int sepIndex = defaultAppCommand.lastIndexOf(File.separator);
                if (sepIndex == -1 || sepIndex == defaultAppCommand.length() - 1 ) {
                    defaultAppID = defaultAppCommand;
                } else {
                    defaultAppID = defaultAppCommand.substring(sepIndex + 1, defaultAppCommand.length());
                }
            }
        }
    }
    
    /**
     * Writes association fields into specified .mime file, including mime type, 
     * and extension list.
     *       
     * @throws IOException if the given association info fails to be write to the given 
     *         mime file.
     */
    private void writeDotMimeFile(Association assoc, String dotMimeFilePath) throws IOException {
        // Create file first.
        createFile(dotMimeFilePath);
        
        String mimeType = assoc.getMimeType();
        List fileExtList = assoc.getFileExtList();

        BufferedWriter mBufferWriter = null;
        try {
            // Appends new mime info into .mime file.
            mBufferWriter = new BufferedWriter(new FileWriter(dotMimeFilePath, true));

            mBufferWriter.write(mimeType + "\n");
            String fileExtensionString = null;

            if (fileExtList == null) {
                fileExtensionString = "";
            } else {
                fileExtensionString = fileExtListToString(fileExtList);
            }
            mBufferWriter.write("\t" + "ext: " + fileExtensionString + "\n");
            mBufferWriter.write("\n");            
        } catch (IOException e) {
            throw new IOException("Write mime info to " + dotMimeFilePath + " failed.");
        } finally {
            // No matter what happens, always close streams already opened.
            if (mBufferWriter != null) {
                try {
                   mBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Writes association fields into specified .keys file, including mime type, and icon file, and action list.      
     *
     * @throws IOException if the given association info fails to be write to the given 
     *         mime file.
     */
    private void writeDotKeysFile(Association assoc, String dotKeysFilePath) 
        throws IOException {
        // Create file first.
        createFile(dotKeysFilePath);

        String mimeType = assoc.getMimeType();
        String description = assoc.getDescription();      
        String iconFileName = assoc.getIconFileName();
        
        BufferedWriter kBufferWriter = null;
        try {
            // Appends new mime info into .keys file.
            kBufferWriter = new BufferedWriter(new FileWriter(dotKeysFilePath, true));

            kBufferWriter.write(mimeType + "\n");
            if (description != null) {
                kBufferWriter.write("\t"
                        + GnomeAssociationUtil.GNOME_VFS_MIME_KEY_DESCRIPTION 
                        + "=" + description + "\n");
            }

            if (iconFileName != null) {
                kBufferWriter.write("\t"
                        + GnomeAssociationUtil.GNOME_VFS_MIME_KEY_ICON_FILENAME
                        + "=" + iconFileName + "\n");
            }

            // Parse the given action list to get the application id and command.
            parseOpenAction(assoc);
            if (defaultAppID != null) {                
                    kBufferWriter.write("\t" + "default_action_type=application" + "\n");
                    kBufferWriter.write("\t" + "default_application_id=" + defaultAppID + "\n");
                    kBufferWriter.write("\t" + "short_list_application_user_additions="  + defaultAppID + "\n");
            }
            
            kBufferWriter.write("\n");            
        } catch (IOException e) {
            throw new IOException("Write mime info to " + dotKeysFilePath + " failed.");
        } finally {
            // No matter what happens, always close streams already opened.
            if (kBufferWriter != null) {
                try {
                   kBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Writes association fields into specified .applications file,
     *  
     * @throws IOException if the given association info fails to be write to the given 
     *         mime file.
     */
    private void writeDotApplicationsFile(Association assoc, String dotApplicationsFilePath) 
        throws IOException {
        // Create file first.
        createFile(dotApplicationsFilePath);

        BufferedWriter mBufferWriter = null;
        try {
            // Parse the given action list to get the application id and command.
            parseOpenAction(assoc);
            if (defaultAppID != null && defaultAppCommand != null) {
                // Appends new mime info into .applications file.
                mBufferWriter = new BufferedWriter(new FileWriter(dotApplicationsFilePath, true));

                mBufferWriter.write(defaultAppID + "\n");
                mBufferWriter.write("\t" + "command=" + defaultAppCommand + "\n");
                mBufferWriter.write("\t" + "name=" + defaultAppID + "\n");
                mBufferWriter.write("\t" + "can_open_multiple_files=false" + "\n");
                mBufferWriter.write("\t" + "requires_terminal=false" + "\n");

                String mimeType = assoc.getMimeType();
                mBufferWriter.write("\t" + "mime_types=" + mimeType + "\n");
            
                mBufferWriter.write("\n");
            }            
        } catch (IOException e) {
            throw new IOException("Write mime info to " + dotApplicationsFilePath + " failed.");
        } finally {
            // No matter what happens, always close streams already opened.
            if (mBufferWriter != null) {
                try {
                   mBufferWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }
    
    /**
     * Checks whether the specified .mime file contains the specified mime type.
     */
    private boolean dotMimeFileContainsMimeType(File dotMimeFile, String mimeType) {
        boolean isMimeTypeExist = false;

        try {
            BufferedReader mBufferReader = new BufferedReader(new FileReader(dotMimeFile));
            String oneLine;
    
            while ((oneLine = mBufferReader.readLine()) != null) {
                if (mimeType.equals(oneLine)) {
                    isMimeTypeExist = true;
                    break;
                }
            }
          
            mBufferReader.close();
            
            return isMimeTypeExist;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Checks whether the specified association object is valid for registration.
     * <P>
     * Both the name and mimeType fields must be specified to perform this operation.
     * If either of the fields is null, an IllegalArgumentException is thrown.
     *
     * @param assoc a given Association object.
     * @throws IllegalArgumentException if the given association is not valid for registration.
     */
    public void checkAssociationValidForRegistration(Association assoc) 
        throws IllegalArgumentException {
        if (assoc.getName() == null || assoc.getMimeType() == null) {
            throw new IllegalArgumentException("The given association is invalid. It should " + 
                "specify both the name and mimeType fields to perform this operation.");
        }
    }
  
    /**
     * Checks whether the specified association object is valid for unregistration.
     * <P>
     * The name field must be specified to perform this operation. Or else,
     * an IllegalArgumentException is thrown.
     *
     * @param assoc a given Association object.
     * @throws IllegalArgumentException if the given association is not valid for unregistration. 
     */
    public void checkAssociationValidForUnregistration(Association assoc) 
        throws IllegalArgumentException {
        if (assoc.getName() == null) {
            throw new IllegalArgumentException("The given association is invalid. It should " +
                "specify the name field to perform this operation.");
        }
    }

    /**
     * Checks whether or not the given assocation already existed in the MIME type database. 
     * <P>
     * If the mime files identified by the name field (name.mime and name.keys) already exists
     * in the specified MIME database, return true. 
     *
     * @param assoc a given Association
     * @param level a given MIME database level.     
     * @return true if the given Association already exists in the specified MIME database.
     */
    public boolean isAssociationExist(Association assoc, int level) {
        File dotMimeFile = null;
        if (level == SYSTEM_LEVEL) {
            // Check the mime files in system default MIME database.
            dotMimeFile = new File(getSystemDotMimeFilePath(assoc));
        } else {
            // Check the mime files in user MIME database.
            dotMimeFile = new File(getUserDotMimeFilePath(assoc));
        }

        if (dotMimeFile.exists()) {
            // The .mime file exist, check the mime type in the .mime file.
            if (assoc.getMimeType() == null) {
                return true;
            } else {
                return dotMimeFileContainsMimeType(dotMimeFile, assoc.getMimeType());
            } 
        } else {
            return false;    
        }
    }
      
    /**
     * Registers the given association info in the specified level.
     * <P>
     * Generate the mime files identified by the name field(name.mime and name.keys)
     * in the system or user MIME database. Then write the association info into the 
     * generated mime files.
     * 
     * @param assoc the given association.
     * @param level the given registration level.
     * @throws RegisterFailedException if the registration failed.
     */
    public void registerAssociation(Association assoc, int level) 
        throws RegisterFailedException {
        String dotMimeFilePath = null;
        String dotKeysFilePath = null;
        String dotApplicationsFilePath = null;
        
        try {
            if (level == SYSTEM_LEVEL) {
                checkSystemMIMEDatabase();
                
                dotMimeFilePath = getSystemDotMimeFilePath(assoc);
                dotKeysFilePath = getSystemDotKeysFilePath(assoc);
                dotApplicationsFilePath = getSystemDotApplicationsFilePath(assoc);
             } else {
                checkUserMIMEDatabase();
                
                dotMimeFilePath = getUserDotMimeFilePath(assoc);
                dotKeysFilePath = getUserDotKeysFilePath(assoc);
                dotApplicationsFilePath = getUserDotApplicationsFilePath(assoc);
             }
             
            // Create and write .mime file.
            writeDotMimeFile(assoc, dotMimeFilePath);

            // Create and write .keys file.
            writeDotKeysFile(assoc, dotKeysFilePath);
            
            // Create and write .applications file.
            writeDotApplicationsFile(assoc, dotApplicationsFilePath);
        } catch (IOException e) {
            // If there are errors, try to delete all the created mime files.
            if (dotMimeFilePath != null ) {
                (new File(dotMimeFilePath)).delete();
            }
            if (dotKeysFilePath != null) {
                (new File(dotKeysFilePath)).delete();
            }
            if (dotApplicationsFilePath != null) {
                (new File(dotApplicationsFilePath)).delete();
            }
            
            throw new RegisterFailedException(e.getMessage());
        }
    }
  
    /**
     * Unregisters the given association in the specified level.
     * <P>
     * Removes the mime files identified by the name field(name.mime and name.keys) from 
     * the system or user MIME type. 
     * 
     * @param assoc the given association.
     * @param level the given unregistration level.     
     * @throws RegisterFailedException if the unregistration failed.     
     */
    public void unregisterAssociation(Association assoc, int level) throws RegisterFailedException {
        String dotMimeFilePath = null;
        String dotKeysFilePath = null;
        String dotApplicationsFilePath = null;
                
        try {
            if (level == SYSTEM_LEVEL) {
                checkSystemMIMEDatabase();
                
                dotMimeFilePath = getSystemDotMimeFilePath(assoc);
                dotKeysFilePath = getSystemDotKeysFilePath(assoc);
                dotApplicationsFilePath = getSystemDotApplicationsFilePath(assoc);
             } else {
                checkUserMIMEDatabase();
                
                dotMimeFilePath = getUserDotMimeFilePath(assoc);
                dotKeysFilePath = getUserDotKeysFilePath(assoc);
                dotApplicationsFilePath = getUserDotApplicationsFilePath(assoc);
             }

            // Delete the mime files.
            (new File(dotMimeFilePath)).delete();
            (new File(dotKeysFilePath)).delete();
            (new File(dotApplicationsFilePath)).delete();                        
        } catch (IOException e) {
            throw new RegisterFailedException(e.getMessage());
        }
    }
}