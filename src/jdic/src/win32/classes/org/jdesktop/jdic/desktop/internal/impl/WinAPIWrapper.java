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

package org.jdesktop.jdic.desktop.internal.impl;


/**
 * Bottom layer java wrapper for Windows registry relevant APIs
 * 
 * @version 0.9
 */
public class WinAPIWrapper {
    static {
        System.loadLibrary("jdic");

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {shutDown();}
		});
    }
 
    /**
     * Windows handles to hives.
     */
    public final static int  HKEY_CLASSES_ROOT = 0x80000000;
    public final static int  HKEY_CURRENT_USER = 0x80000001;
    public final static int  HKEY_LOCAL_MACHINE = 0x80000002;
  
    /* Windows error or status codes. */
    public static final int ERROR_SUCCESS = 0;
  
    /* Constants for Windows registry element size limits */
    public static final int MAX_KEY_LENGTH = 255;

    /* Constants used to interpret returns of native functions  */
    private static final int OPENED_KEY_HANDLE = 0;
    private static final int ERROR_CODE = 1;
    private static final int SUBKEYS_NUMBER = 0;
    
    /* Windows security masks */
    public static final int KEY_READ = 0x20019;
  
    /**
     * Java wrapper for Windows registry API RegOpenKey()
     * @param hKey Windows registry folder
     * @param subKey key name
     * @return ERROR_SUCCESS if succeed, or error code if fail
     */
    private static native int[] RegOpenKey(int hKey, byte[] subKey,
            int securityMask);
                                                 
    /**
     * Java wrapper for Windows registry API RegCloseKey()
     */
    private static native int RegCloseKey(int hKey);
  
    /**
     * Java wrapper for Windows registry API RegQueryValueEx()
     */
    private static native byte[] RegQueryValueEx(int hKey, byte[] valueName);    

    /**
     * Java wrapper for Windows AssocQueryString.
     */
    private static native byte[] AssocQueryString(byte[] fileExt, byte[] verb);
  
    /*
     * Java wrapper for Windows API ExpandEnvironmentStrings()
     */
    private static native byte[] ExpandEnvironmentStrings(byte[] envBytes);
    
	/**
	 * Resolves the target file from a link file.
	 * 
	 * @param filePath The path name of the given file.
	 * @return The target file path name.
	 */
	private static native String resolveLinkFile(byte[] filePath);
    
	/**
	 * Launch the application for the given file.
	 * @param filePath Path name of the given file.
	 * @param verb Specify the verb to be executed.
	 * @return error code
	 */
	private static native int shellExecute(byte[] filePath, byte[] verb);
	
	/**
	 * Opens the system default mailer with relevant information filled in.
	 * 
	 * @param toArray the email address array of the "To" field.
	 * @param ccArray the email address array of the "Cc" field.
	 * @param bccArray the email address array of the "Bcc" field.
	 * @param subject the string of the "Subject" field.
	 * @param body the string of the "Body" field.
	 * @param attachArray the array of the abosolute paths of the attached files.
	 */
	private static synchronized native void openMapiMailer(String[] toArray, String[] ccArray, 
		String[] bccArray, String subject, String body, String[] attachArray);

	/**
	 * Calls this method to uninitialize COM library.
	 */
	protected static native void shutDown();

	/**
	 * Native method to browser the given url in the given target window using IE.
	 *  
	 * @param urlStr the given url.
	 * @param target the given name of the target browser windows.
	 * @return true if the operation succeeds.
	 */
	private static native boolean nativeBrowseURLInIE(String urlStr, String target);


    /**
     * Returns this java string as a null-terminated byte array
     */
    private static byte[] stringToByteArray(String str) {
        if (str == null) {
            return null;
        }
    
        byte[] srcByte = str.getBytes();
        int srcLength = srcByte.length;
        byte[] result = new byte[srcLength + 1];

        System.arraycopy(srcByte, 0, result, 0, srcLength);
        result[srcLength] = 0; 
  
        return result;
    }
    
    /**
     * Converts a null-terminated byte array to java string
     */
    private static String byteArrayToString(byte[] array) {
        if (array != null) {
            String temString = new String(array);

            if (temString != null) {
                return temString.substring(0, temString.length() - 1);
            }
        }
        return null;
    }

    /**
     * Suppress default constructor for noninstantiability.
     */
    private WinAPIWrapper() {}

    /**
     * Retrieves the data associated with the default or unnamed value of a specified 
     * registry key. The data must be a null-terminated string.
     * @param hKey specified windows registry folder constant
     * @param subKey given sub key (not null)
     * @param valueName given value name (not null)
     * @return content of the value, or null if fail or not exist
     */
    public static String WinRegQueryValueEx(int hKey, String subKey, String valueName) {
        byte[] lpSubKey = stringToByteArray(subKey);
        int[] openResult = RegOpenKey(hKey, lpSubKey, KEY_READ);
    
        if (openResult == null) {
            return null; 
        }

        if (openResult[ERROR_CODE] != ERROR_SUCCESS) {
            return null;
        } else {
            byte[] valueBytes;
            byte[] lpValueName = stringToByteArray(valueName);

			valueBytes =
				RegQueryValueEx(openResult[OPENED_KEY_HANDLE], lpValueName);
            RegCloseKey(openResult[OPENED_KEY_HANDLE]);
      
            if (valueBytes != null) {
                if ((valueBytes.length == 1) && (valueBytes[0] == 0) && (valueName.equals("")) ){
                    return null;
                } else {
                    return byteArrayToString(valueBytes);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Searches for and retrieves a file association-related string from the registry.
     * 
     * @param fileOrProtocal The given file extension or url protocal name
     * @param verb	The given verb
     * @return The file association-related string for the given file extension and verb, or null
     */
    public static String WinAssocQueryString(String fileOrProtocal, String verb) {
    	byte[] fileOrProtocalBytes = stringToByteArray(fileOrProtocal);
    	byte[] verbBytes = stringToByteArray(verb);
    	
    	byte[] queryResult = AssocQueryString(fileOrProtocalBytes, verbBytes);
		if (queryResult != null) {
			if ((queryResult.length == 1) && (queryResult[0] == 0) && (queryResult.equals("")) ){
				return null;
			} else {
				return byteArrayToString(queryResult);
			}
		} else {
			return null;
		}
    }

	/**
	 * Resolves the target file from a link file.
	 * 
	 * @param filePath The path name of the given file.
	 * @return The target file path name.
	 */
	public static String WinResolveLinkFile(String filePath) {
		byte[] filePathBytes = stringToByteArray(filePath);
		return resolveLinkFile(filePathBytes);
	}
    
	/**
	 * Launch the application for the given file.
	 * @param filePath Path name of the given file.
	 * @param verb Specify the verb to be executed.
	 * @return true if succeed.
	 */
	public static boolean WinShellExecute(String filePath, String verb) {
		byte[] filePathBytes = stringToByteArray(filePath);
		byte[] verbBytes = stringToByteArray(verb);
		int exeResult = shellExecute(filePathBytes, verbBytes);
		if (exeResult > 32) {
			return true;	
		} else {
			return false;
		}
	}
	
	public static boolean WinBrowseURLInIE(String urlStr, String target) {
		return nativeBrowseURLInIE(urlStr, target);
	}

	/**
	 * Opens the system default mailer with relevant information filled in.
	 * 
	 * @param toArray the email address array of the "To" field.
	 * @param ccArray the email address array of the "Cc" field.
	 * @param bccArray the email address array of the "Bcc" field.
	 * @param subject the string of the "Subject" field.
	 * @param body the string of the "Body" field.
	 * @param attachArray the array of the abosolute paths of the attached files.
	 */
	public static synchronized void WinOpenMapiMailer(String[] toArray, String[] ccArray, 
		String[] bccArray, String subject, String body, String[] attachArray) {
		openMapiMailer(toArray, ccArray, bccArray, subject, body, attachArray);				
	}
}  


