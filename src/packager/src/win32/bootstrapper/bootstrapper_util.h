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
 
#if !defined _bootstrapper_util_h
#define _bootstrapper_util_h
// Exclude rarely-used stuff from Windows headers
#define WIN32_LEAN_AND_MEAN

#include <windows.h>
#include <stdio.h>

const int MAX_PATH_SIZE = 2048;
const int MAX_LOCALE_NAME_SIZE = 50;
const int MAX_INFO_SIZE = 256;
const ULONG msiMinimumVersion = 150;
const WORD  RES_ID_MSI = 104;
const WORD  BLOCK_ID_UUID = 101;
const WORD  BLOCK_ID_FLAG = 111;
const WORD  RES_ID_UUID = 1600;
const WORD  RES_ID_LOCALIZATION_FLAG = 1760;
const TCHAR MINIMUM_JAVAWS_VERSION[] = "1.5.0";
const ERROR_JAVAWS_VERSION_ERROR = -1;
const ERROR_MSI_DB_ERROR = -2;


/////////////////////////////////////////////////////////////////////////////
/**
 * Get the local suffix based on current system locale.
 * The locale suffix should be
 *    en      - English
 *    de      - German
 *    es      - Spanish
 *    fr      - French
 *    it      - Italian
 *    ja      - Japanese
 *    ko      - Korean
 *    sv      - Swishish
 *    zh_CN   - Simplified Chinese
 *    zh_TW   - Traditional Chinese
 */
void getLocalName(TCHAR* pStrLocalSuffix, LANGID& langID, UINT& codePage);

/////////////////////////////////////////////////////////////////////////////
/**
 * IsOSWin9X
 *
 *  Returns true if running on a Win9X platform
 *  Returns false if running on a WinNT platform
 */
bool IsOSWin9X(int& osMajorVersion);

/////////////////////////////////////////////////////////////////////////////
/** AlreadyInProgress
 *
 *  Attempts to create the MSISETUP mutex. Returns TRUE
 *  if mutex already exists or failed to create mutex
 */
bool AlreadyInProgress(bool fWin9X, int iMajorVersion);

/////////////////////////////////////////////////////////////////////////////
/**
 * IsTerminalServerInstalled
 *
 *  Determines whether terminal services are installed
 */
bool IsTerminalServerInstalled(bool fWin9X, int iMajorVersion);

/////////////////////////////////////////////////////////////////////////////
/**
 * IsMsiUpgradeNecessary
 *
 *  Currently, we only support MSI version 1.5 or later
 */
bool IsMsiUpgradeNecessary();

/////////////////////////////////////////////////////////////////////////////
/**
 * GetFileVersionNumber
 *
 */
DWORD GetFileVersionNumber(LPSTR szFilename, DWORD * pdwMSVer, \
                           DWORD * pdwLSVer);

///////////////////////////////////////////////////////////////////////////////
/**
 * Extract the specified resources (The MSI file) from the executable file
 *
 */
void ExtractMsiFromResource(TCHAR* msiFileName);

///////////////////////////////////////////////////////////////////////////////
/**
 * Extract the UUID info from resource
 */
void ExtractUUIDFromResource(LPTSTR uuid);

///////////////////////////////////////////////////////////////////////////////
/**
 * Extract the localization flag from resource
 */
void ExtractLocalizationFlagFromResource(LPTSTR lFlag);

///////////////////////////////////////////////////////////////////////////////
/**
 * Create the temporary file path, the extracted msi file will be placed there
 *
 * @param pTempDir Pointer to the string of the temprary directory name
 */
void CreateTempDir(TCHAR* pTempDir);

////////////////////////////////////////////////////////////////////////////
/**
 * Generate a unique tempory directory name
 *
 * @param baseDir Specify the base directory (in)
 * @param tempDir String buffer containing the generated dir name (in, out)
 *
 * @return 1 if succeed
 */
int GetTempDirName(TCHAR* pBaseDir, TCHAR* pTempDir);

///////////////////////////////////////////////////////////////////////////////
/**
 * Get the temporary msi file name
 * @param msiFileDir The given parent dir name
 * @param msiFileName The generated temporary msi full path file name
 */
void GetMsiFileName(TCHAR * msiFileDir, TCHAR* msiFileName);

//////////////////////////////////////////////////////////////////////////////
/**
 * Get current codepage information
 */
UINT GetCodePageInfo();

//////////////////////////////////////////////////////////////////////////////
/**
 * Modify the relevant field in the MSI file
 * They are Codepage & Template of Table Summary Info
 */
void UpdateMsiSummaryInfo(LPCTSTR msiFileName, UINT codePage, WORD langID);

//////////////////////////////////////////////////////////////////////////////
/**
 * Recursively remove the specified directory and all its files and sub-dirs.
 *
 * @param dirName Specify the directory name
 * @return 1 if succeed
 */
int RemoveDir(char * dirName);

//////////////////////////////////////////////////////////////////////////////
/**
 * Get JavaWS home directory
 */
unsigned int GetJavawsHome();

///////////////////////////////////////////////////////////////////////////////
/**
 * Execute MSIexec to install the msi file
 */
int LaunchMsi(LPCTSTR msiFileName, TCHAR* pStrLocalName);


#endif