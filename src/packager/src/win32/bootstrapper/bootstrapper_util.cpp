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

#include "bootstrapper_util.h"
#include <strsafe.h>
#include <Msiquery.h>
#include <wtypes.h>

/////////////////////////////////////////////////////////////////////////////
/**
 * Get the local name based on current system locale.
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
void getLocalName(TCHAR* pStrLocalSuffix, LANGID& langID, UINT& codePage)
{
    langID = GetSystemDefaultLangID();
    codePage = GetCodePageInfo();
    switch (langID) 
    {
        case 0x0404:    //Chinese (Taiwan)
        case 0x0c04:    //Chinese (Hong Kong SAR, PRC)
        case 0x1404:    //Chinese: Macau SAR (Traditional)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "zh_TW");
            break;
        case 0x0804:    //Chinese (PRC)
        case 0x1004:    //Chinese (Singapore)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "zh_CN");
            break;
        case 0x0409:    //English (United States)
        case 0x0809:    //English (United Kingdom)
        case 0x0c09:    //English (Australian)
        case 0x1009:    //English (Canadian)
        case 0x1409:    //English (New Zealand)
        case 0x1809:    //English (Ireland)
        case 0x1c09:    //English (South Afica)
        case 0x2009:    //English (Jamaica)
        case 0x2409:    //English (Caribbean)
        case 0x2809:    //English (Belize)
        case 0x2c09:    //English (Trinidad)
        case 0x3009:    //English (Zimbabwe)
        case 0x3409:    //English (Phlippines)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "en");
            break;
        case 0x040c:    //French (Standard)
        case 0x080c:    //French (Belgian)
        case 0x0c0c:    //French (Canadian)
        case 0x100c:    //French (Switzerland)
        case 0x140c:    //French (Luxembourg)
        case 0x180c:    //French (Monaco)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "fr");
            break;
        case 0x0407:    //German (Standard)
        case 0x0807:    //German (Switzerland)
        case 0x0c07:    //German (Australia)
        case 0x1007:    //German (Luxembourg)
        case 0x1407:    //German (Liechtenstein)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "de");
            break;
        case 0x0410:    //Italian (Standard)
        case 0x0810:    //Italian (Switzerland)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "it");
            break;
        case 0x0411:    //Japanese
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "ja");
            break;
        case 0x0412:    //Korean
        case 0x0812:    //Korean (Johab)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "ko");
            break;
        case 0x040a:    //Spanish (Spain, Traditional Sort)
        case 0x080a:    //Spanish (Mexican)
        case 0x0c0a:    //Spanish (Spain, Modern Sort)
        case 0x100a:    //Spanish (Guatemala)
        case 0x140a:    //Spanish (Costa Rica)
        case 0x180a:    //Spanish (Panama)
        case 0x1c0a:    //Spanish (Dominican Republic)
        case 0x200a:    //Spanish (Venezuela)
        case 0x240a:    //Spanish (Colombia)
        case 0x280a:    //Spanish (Peru)
        case 0x2c0a:    //Spanish (Argentina)
        case 0x300a:    //Spanish (Ecuador)
        case 0x340a:    //Spanish (Chile)
        case 0x380a:    //Spanish (Uruguay)
        case 0x3c0a:    //Spanish (Paraguay)
        case 0x400a:    //Spanish (Bolivia)
        case 0x440a:    //Spanish (EI Salvador)
        case 0x480a:    //Spanish (Honduras)
        case 0x4c0a:    //Spanish (Nicaragua)    
        case 0x500a:    //Spanish (Puerto Rico)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "es");
            break;
        case 0x041d:    //Swedish
        case 0x081d:    //Swedish (Finland)
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "sv");
            break;
        default:         //English
            StringCchCopy(pStrLocalSuffix, MAX_LOCALE_NAME_SIZE, "en");
            break;
    }
}


//////////////////////////////////////////////////////////////////////////////
/**
 * Get current codepage information
 */
UINT GetCodePageInfo()
{
    CPINFOEX sysCodePage;
    GetCPInfoEx(CP_THREAD_ACP, 0, (LPCPINFOEX) &sysCodePage);
    return sysCodePage.CodePage;
}
/////////////////////////////////////////////////////////////////////////////
/**
 * IsOSWin9X
 *
 *  Returns true if running on a Win9X platform
 *  Returns false if running on a WinNT platform
 */
bool IsOSWin9X(int& osMajorVersion)
{
    OSVERSIONINFO sInfoOS;
    memset((void*)&sInfoOS, 0x00, sizeof(OSVERSIONINFO));

    sInfoOS.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
    GetVersionEx(&sInfoOS);

    osMajorVersion = sInfoOS.dwMajorVersion;

    if (VER_PLATFORM_WIN32_NT == sInfoOS.dwPlatformId)
        return false;
    else
        return true;
}

/////////////////////////////////////////////////////////////////////////////
/** AlreadyInProgress
 *
 *  Attempts to create the MSISETUP mutex. Returns TRUE
 *  if mutex already exists or failed to create mutex
 */
bool AlreadyInProgress(bool fWin9X, int iMajorVersion)
{                                                    
    //To be done, we need to put the UUID in the resources
    //Different setup should be allowed to be runned at the same time
    TCHAR myUUID[80] = "\0";
    ExtractUUIDFromResource((LPSTR)myUUID);
    TCHAR szTSUniqueName[200] = "Global\\_MSISETUP_{";
    TCHAR szUniqueName[200] = "_MSISETUP_{";
    StringCchCat(myUUID, 80, "}");
    StringCchCat(szTSUniqueName, 200, myUUID);
    StringCchCat(szUniqueName, 200, myUUID);
    // if Windows 2000 or greater or Terminal Server installed, must use
    // Global prefix
    const TCHAR *szMutexName = NULL;
    if ((!fWin9X && iMajorVersion >= 5) || \
         IsTerminalServerInstalled(fWin9X, iMajorVersion))
        szMutexName = szTSUniqueName;
    else
        szMutexName = szUniqueName;

    HANDLE hMutex = 0;


    hMutex = CreateMutex(NULL /*default security descriptor*/, \
                         FALSE, szMutexName);
    if (!hMutex || ERROR_ALREADY_EXISTS == GetLastError())
        return true;


    return false;
}

/////////////////////////////////////////////////////////////////////////////
/**
 * IsTerminalServerInstalled
 *
 *  Determines whether terminal services are installed
 */
bool IsTerminalServerInstalled(bool fWin9X, int iMajorVersion)
{
    const TCHAR szTSSearchStr[] = \
                TEXT("Terminal Server"); // Not localized
    const TCHAR szKey[] = \
                TEXT("System\\CurrentControlSet\\Control\\ProductOptions");
    const TCHAR szValue[] = TEXT("ProductSuite");

    DWORD dwSize = 0;
    HKEY  hKey = 0;
    DWORD dwType = 0;

    // Win9X is not terminal server
    if (fWin9X)
        return false;

    bool fIsTerminalServer = false;

    // On Windows 2000 and greater, the ProductSuite "Terminal Server"
    // value will always be present. Use GetVersionEx to get the right
    // answer.
    if (iMajorVersion > 4)
    {
        OSVERSIONINFOEX osVersionInfo;
        DWORDLONG dwlConditionMask = 0;

        ZeroMemory(&osVersionInfo, sizeof(OSVERSIONINFOEX));
        osVersionInfo.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);

        if (GetVersionEx((OSVERSIONINFO*)&osVersionInfo)
            && (osVersionInfo.wSuiteMask & VER_SUITE_TERMINAL)
            && !(osVersionInfo.wSuiteMask & VER_SUITE_SINGLEUSERTS))
            fIsTerminalServer = true;
    }
    else
    {
        // Other NT versions, check the registry key
        // If the value we want exists and has a non-zero size...

        if (ERROR_SUCCESS == RegOpenKeyEx(HKEY_LOCAL_MACHINE, szKey, 0, \
                                          KEY_READ, &hKey)
            && ERROR_SUCCESS == RegQueryValueEx(hKey, szValue, NULL, \
                                                &dwType, NULL, &dwSize)
            && dwSize > 0
            && REG_MULTI_SZ == dwType)
        {
            TCHAR* szSuiteList = new TCHAR[dwSize];
            if (szSuiteList)
            {
                ZeroMemory(szSuiteList, dwSize);
                if (ERROR_SUCCESS == RegQueryValueEx(hKey, szValue, NULL, \
                                                     &dwType, \
                                                     (LPBYTE)szSuiteList, \
                                                     &dwSize))
                {
                    // Length of current member
                    DWORD cchMulti = 0;
                    // Constant during search
                    DWORD cchSrch  = lstrlen(szTSSearchStr);
                    // pointer to current substring
                    const TCHAR *szSubString = szSuiteList;

                    while (*szSubString) // Break on consecutive zero bytes
                    {
                        cchMulti = lstrlen(szSubString);
                        if (cchMulti == cchSrch && 0 == \
                            lstrcmp(szTSSearchStr, szSubString))
                        {
                            fIsTerminalServer = true;
                            break;
                        }

                        // substring does not match, skip forward the length
                        // of the substring
                        // plus 1 for the terminating null.
                        szSubString += (cchMulti + 1);
                    }
                }
                delete [] szSuiteList;
            }
        }

        if (hKey)
            RegCloseKey(hKey);
    }

    return fIsTerminalServer;
}

/////////////////////////////////////////////////////////////////////////////
/**
 * IsMsiUpgradeNecessary
 *
 *  Currently, we only support MSI version 1.5 or later
 */
bool IsMsiUpgradeNecessary()
{
    // attempt to load msi.dll in the system directory

    TCHAR szSysMsiDll[MAX_PATH] = "\0";
    TCHAR szSystemFolder[MAX_PATH] = "\0";
    TCHAR szInfo[MAX_INFO_SIZE] = "\0";
    

    DWORD dwRet = GetSystemDirectory(szSystemFolder, MAX_PATH);
    if (0 == dwRet || MAX_PATH < dwRet)
    {
        // failure or buffer too small; assume upgrade is necessary
        StringCchPrintf(
            szInfo,
            MAX_INFO_SIZE,
            "%s%s",
            "[Info] Can't obtain system directory; ",
            "assuming upgrade is necessary \n");
        printf(szInfo);
        return true;
    }

    if (FAILED(StringCchCopy(szSysMsiDll, MAX_PATH, szSystemFolder))
        || FAILED(StringCchCat(szSysMsiDll, MAX_PATH, "\\MSI.DLL")))
    {
        // failure to get path to msi.dll; assume upgrade is necessary
        StringCchPrintf(
            szInfo,
            MAX_INFO_SIZE,
            "%s%s",
            "[Info] Can't obtain msi.dll path; ",
            "assuming upgrade is necessary. \n");
        printf(szInfo);
        return true;
    }

    HINSTANCE hinstMsiSys = LoadLibrary(szSysMsiDll);
    if (0 == hinstMsiSys)
    {
        // can't load msi.dll; assume upgrade is necessary
        StringCchPrintf(
            szInfo,
            MAX_INFO_SIZE,
            "%s",
            "[Info] Can't load msi.dll; assuming upgrade is necessary");
        printf(szInfo);
        return true;
    }
    FreeLibrary(hinstMsiSys);

    // get version on msi.dll
    DWORD dwInstalledMSVer;
    dwRet = GetFileVersionNumber(szSysMsiDll, &dwInstalledMSVer, NULL);
    if (ERROR_SUCCESS != dwRet)
    {
        // can't obtain version information; assume upgrade is necessary
        StringCchPrintf(
            szInfo,
            MAX_INFO_SIZE,
            "%s%s",
            "[Info] Can't obtain version information; ",
            "assuming upgrade is necessary. \n");
        printf(szInfo);
        return true;
    }

    // compare version in system to the required minimum
    ULONG ulInstalledVer = HIWORD(dwInstalledMSVer) * 100 \
                           + LOWORD(dwInstalledMSVer);
    if (ulInstalledVer < msiMinimumVersion)
    {
        // upgrade is necessary
        StringCchPrintf(
            szInfo,
            MAX_INFO_SIZE,
            "%s%d,%s%d \n",
            "[Info] Windows Installer upgrade is required.  System Version = ",
            ulInstalledVer,
            " Minimum Version = ",
            msiMinimumVersion);
        printf(szInfo);

        return true;
    }

    // no upgrade is necessary
#ifdef _DEBUG
    StringCchPrintf(
        szInfo,
        MAX_INFO_SIZE,
        "%s%s",
        "[Info] No upgrade is necessary. ",
        "System MSI version meets minimum requirements. \n");
    printf(szInfo);
#endif
    return false;
}



/////////////////////////////////////////////////////////////////////////////
/**
 * GetFileVersionNumber
 *
 */
DWORD GetFileVersionNumber(LPSTR szFilename, DWORD * pdwMSVer, DWORD * pdwLSVer)
{
    DWORD             dwResult = NOERROR;
    unsigned          uiSize;
    DWORD             dwVerInfoSize;
    DWORD             dwHandle;
    BYTE              *prgbVersionInfo = NULL;
    VS_FIXEDFILEINFO  *lpVSFixedFileInfo = NULL;

    DWORD dwMSVer = 0xffffffff;
    DWORD dwLSVer = 0xffffffff;

    dwVerInfoSize = GetFileVersionInfoSize(szFilename, &dwHandle);
    if (0 != dwVerInfoSize)
    {
        prgbVersionInfo = (LPBYTE) GlobalAlloc(GPTR, dwVerInfoSize);
        if (NULL == prgbVersionInfo)
        {
            dwResult = ERROR_NOT_ENOUGH_MEMORY;
            goto Finish;
        }

        // Read version stamping info
        if (GetFileVersionInfo(szFilename, dwHandle, dwVerInfoSize, \
                               prgbVersionInfo))
        {
            // get the value for Translation
            if (VerQueryValue(prgbVersionInfo, "\\", \
                              (LPVOID*)&lpVSFixedFileInfo, &uiSize) \
                              && (uiSize != 0))
            {
                dwMSVer = lpVSFixedFileInfo->dwFileVersionMS;
                dwLSVer = lpVSFixedFileInfo->dwFileVersionLS;
            }
        }
        else
        {
            dwResult = GetLastError();
            goto Finish;
        }
    }
    else
    {
        dwResult = GetLastError();
    }

Finish:
    if (NULL != prgbVersionInfo)
        GlobalFree(prgbVersionInfo);
    if (pdwMSVer)
        *pdwMSVer = dwMSVer;
    if (pdwLSVer)
        *pdwLSVer = dwLSVer;

    return dwResult;
}

///////////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Extract the specified resources (The MSI file) from the executable file
 *
 */
void ExtractMsiFromResource(TCHAR* msiFileName)
{
    HRSRC hResource = FindResource(NULL, MAKEINTRESOURCE(RES_ID_MSI), \
                                   RT_RCDATA);
    HGLOBAL hResourceLoaded;
    LPBYTE lpBuffer;
    if (NULL != hResource)
    {
        hResourceLoaded = LoadResource(NULL, hResource);
        if (NULL != hResourceLoaded)
        {
            lpBuffer = (LPBYTE) LockResource(hResourceLoaded);
            if (NULL != lpBuffer)
            {
                //Get the file size
                DWORD dwFileSize = SizeofResource(NULL, hResource);
                //create the MSI file
                HANDLE hMsiFile = CreateFile(msiFileName, 
                                             GENERIC_WRITE,
                                             0,
                                             NULL,
                                             CREATE_ALWAYS,
                                             FILE_ATTRIBUTE_NORMAL,
                                             NULL);
                DWORD dwBytesWritten = 0;
                if (INVALID_HANDLE_VALUE != hMsiFile)
                {
                    //Copy data into the file
                    WriteFile(hMsiFile, lpBuffer, dwFileSize, \
                              &dwBytesWritten, NULL);
                    CloseHandle(hMsiFile);
                }
            }
        }
    }
}

///////////////////////////////////////////////////////////////////////////////////////////
/**
 * Extract the UUID info from resource
 */
void ExtractUUIDFromResource(LPTSTR uuid)
{
    HRSRC hResource = FindResource(NULL, MAKEINTRESOURCE(BLOCK_ID_UUID), \
                                   RT_STRING);
    if (NULL != hResource)
    {
        LoadString(NULL, RES_ID_UUID, uuid, 256);
    }
}

//////////////////////////////////////////////////////////////////////////////////////////
/**
 * Extract the localization flag from resource
 */
bool IsLocalizationSupported()
{
    TCHAR localizationFlag[80];
    //Extract the localization flag from resource
    HRSRC hResource = FindResource(NULL, MAKEINTRESOURCE(BLOCK_ID_FLAG), \
                                   RT_STRING);
    if (NULL != hResource)
    {
        LoadString(NULL, RES_ID_LOCALIZATION_FLAG, localizationFlag, 256);
    }
    if (0 == strcmp(localizationFlag, "localization supported") ) 
    {
        return true;
    }
    else 
    {
        return false;
    }
}


///////////////////////////////////////////////////////////////////////////////
/**
 * Create the temporary file path, the extracted msi file will be placed there
 *
 * @param pTempDir Pointer to the string of the temprary directory name
 */
void CreateTempDir(TCHAR* pTempDir)
{
    //BASE_PATH: Get the system temporary path
    TCHAR BASE_PATH[MAX_PATH_SIZE] = "\0";
    DWORD dwLenInstallerPath = MAX_PATH_SIZE;
    GetTempPath(dwLenInstallerPath, (LPTSTR)BASE_PATH);

    //Generate a unique temporary directory
    GetTempDirName(BASE_PATH, pTempDir);
    CreateDirectory(pTempDir, NULL);
}

////////////////////////////////////////////////////////////////////////////
/**
 * Generate a unique tempory directory name
 *
 * @param baseDir Specify the base directory (in)
 * @param tempDir String buffer containing the generated dir name (in, out)
 *
 * @return 1 if succeed
 */
int GetTempDirName(TCHAR* pBaseDir, TCHAR* pTempDir)
{
    TCHAR dirPrefix[] = "javaws";
    if (0 != GetTempFileName(pBaseDir, dirPrefix, 0, pTempDir))
    {
        DeleteFile(pTempDir);
        return 1;
    }
    return 0;
}

///////////////////////////////////////////////////////////////////////////////
/**
 * Get the temporary msi file name
 */
void GetMsiFileName(TCHAR * msiFileDir, TCHAR* msiFileName)
{
    StringCchCopy(msiFileName, MAX_PATH_SIZE, msiFileDir);
    StringCchCat(msiFileName, MAX_PATH_SIZE, "\\install.msi");
}

//////////////////////////////////////////////////////////////////////////////
/**
 * Recursively remove the specified directory and all its files and sub-dirs.
 *
 * @param dirName Specify the directory name
 * @return 1 if succeed
 */
int RemoveDir(TCHAR * dirName)
{
    WIN32_FIND_DATA fileData;
    HANDLE hSearch;
    TCHAR filePattern[MAX_PATH_SIZE] = {0};
    StringCchPrintf(filePattern, MAX_PATH_SIZE, "%s\\%s", dirName, "*");

    BOOL fFinished = false;

    hSearch = FindFirstFile(filePattern, &fileData);
    
    while (!fFinished) 
    {
        if (fileData.dwFileAttributes == FILE_ATTRIBUTE_DIRECTORY) 
        {
            if ((strcmp(fileData.cFileName, ".") != 0) \
                 && (strcmp(fileData.cFileName, "..") != 0))
            {
                //For none "." & ".." directory, take resursive
                //handling mechanism
                TCHAR nextDirName[MAX_PATH_SIZE] = {0};
                StringCchPrintf(nextDirName, MAX_PATH_SIZE, "%s\\%s", dirName, \
                                fileData.cFileName);
                RemoveDir(nextDirName);
            }
        }
        else 
        {
            //For general file, just delete
            TCHAR fullFileName[MAX_PATH_SIZE] = {0};
            StringCchPrintf(fullFileName, MAX_PATH_SIZE, "%s\\%s", dirName, \
                            fileData.cFileName);
            DeleteFile(fullFileName);
            
        }
        if (!FindNextFile(hSearch, &fileData) && (GetLastError() == \
                          ERROR_NO_MORE_FILES))
                fFinished = true;
    }
    //Close the search handle
    FindClose(hSearch);
    //Then delete the directory
    RemoveDirectory(dirName);
    return 1;
}


//////////////////////////////////////////////////////////////////////////////
/**
 * Modify the relevant field in the MSI file
 * They are Codepage & Template of Table Summary Info
 */
void UpdateMsiSummaryInfo(LPCTSTR msiFileName, UINT codePage, WORD langID)
{
    if (!IsLocalizationSupported())
    {
        //No localization support, no changes to the MSI file
        return;
    }
    int errorCode;
    MSIHANDLE msiDatabase, msiSummaryInfo;
    errorCode = MsiOpenDatabase(msiFileName, MSIDBOPEN_TRANSACT, &msiDatabase);
    if (ERROR_SUCCESS == errorCode) //Database open successfully
    {
        errorCode = MsiGetSummaryInformation(msiDatabase, 0, 5, \
                                             &msiSummaryInfo);
        //Open summary Info successfully
        if (ERROR_SUCCESS == errorCode)
        {
            int errorCode1, errorCode2;
            //Update codepage field
            errorCode1 = MsiSummaryInfoSetProperty(msiSummaryInfo, 1, VT_I2, \
                                                   codePage, NULL, NULL);
            //Get the string for the template, which is ";"+langID
            TCHAR strTemplate[80] = "\0";
            StringCchPrintf(strTemplate, 80, ";%d", langID);
            //Update template field
            errorCode2 = MsiSummaryInfoSetProperty(msiSummaryInfo, 7, \
                                                   VT_LPSTR, 0, NULL, \
                                                   strTemplate);
            if ((ERROR_SUCCESS == errorCode1) && (ERROR_SUCCESS == errorCode2))
            {
                MsiSummaryInfoPersist(msiSummaryInfo);
                MsiDatabaseCommit(msiDatabase);
            }
            MsiCloseHandle(msiSummaryInfo);
        }
        MsiCloseHandle(msiDatabase);
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////
/**
 * Get JavaWS home directory
 */
unsigned int GetJavawsHome()
{
   TCHAR KEY_JAVAWS[] = "SOFTWARE\\JavaSoft\\Java Web Start"; 
   TCHAR KEY_JAVAWS_CURRENT_VERSION[1024] = {0};
   TCHAR VALUE_JAVAWS_CURRNET_VERSION[] = "CurrentVersion";
   TCHAR VALUE_JAVAWS_HOME[] = "Home";   
   TCHAR JAVAWS_VERSION[128] = {0};

   HKEY hKey;
   DWORD dwType;
   DWORD cbData;
   //Open the key HKEY_LOCAL_MACHINE\\SOFTWAER\JavaSoft\\Java Web Start
   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, KEY_JAVAWS, 0, KEY_READ, &hKey) \
                    == ERROR_SUCCESS)
   {
       cbData = 128;
       //Get the value of CurrentVersion
       if (RegQueryValueEx(hKey, (LPCTSTR)VALUE_JAVAWS_CURRNET_VERSION, NULL, \
                           &dwType, (LPBYTE)&JAVAWS_VERSION, &cbData) \
                           == ERROR_SUCCESS)
       {   
           RegCloseKey(hKey);
#ifdef _DEBUG
           printf("[Info]Java WS current version is %s\n", JAVAWS_VERSION);
#endif
           if (strncmp(JAVAWS_VERSION, MINIMUM_JAVAWS_VERSION, 128) < 0) 
           {
               //JavaWS version Less then 1.5.0, does not meet the 
               // minimum reqiurement
               return ERROR_JAVAWS_VERSION_ERROR; 
           }
           return 0;
       }
   }
   return ERROR_MSI_DB_ERROR;
}



///////////////////////////////////////////////////////////////////////////////
/**
 * Execute MSIexec to install the msi file
 */
int LaunchMsi(LPCTSTR msiFileName, TCHAR* pStrLocalName)
{
    // install the jnlp software
    STARTUPINFO stinfo = {0}; //info of the window
    stinfo.cb = sizeof(STARTUPINFO);
    PROCESS_INFORMATION procinfo; //info of the process
    TCHAR CREATEPROCESS[200] = {0};  
    if (0 != strcmp(pStrLocalName, "en"))
    {   
        //None English local, need to apply transform
        TCHAR sysMsiTransform[80];
        TCHAR custMsiTransform[80];
        //System Transform naming schema is sys_localname, e.g. sys_zh_CN
        StringCchPrintf(sysMsiTransform, 80, ":sys_%s", pStrLocalName);
        //Customized Transform naming schema is cust_localname, e.g. cust_zh_cn
        StringCchPrintf(custMsiTransform, 80, ":cust_%s", pStrLocalName);
        if (IsLocalizationSupported())
        {
            //Localization supported
            StringCchPrintf(CREATEPROCESS, 200, "msiexec /i %s TRANSFORMS=%s", \
                            msiFileName, custMsiTransform);
        }
        else
        {
            //No localization support
            StringCchPrintf(CREATEPROCESS, 200, "msiexec /i %s", msiFileName);
        }
    }
    else 
    {
        //English locale, no transform is need
        StringCchPrintf(CREATEPROCESS, 200, "msiexec /i %s", msiFileName);
    }
#ifdef _DEBUG
    printf("[Info] Will now launch msi installation command: %s...\n", \
            CREATEPROCESS);
#endif
    //Create the process
    if(!CreateProcess(NULL, CREATEPROCESS, NULL, NULL, FALSE, \
                      CREATE_NO_WINDOW, NULL, NULL, &stinfo, &procinfo))
    {
        return -1;
    }
    // wait for the end of the process
    WaitForSingleObject(procinfo.hProcess, INFINITE);
#ifdef _DEBUG
    TCHAR strInfo[MAX_INFO_SIZE] = "\0";
    StringCchPrintf( \
        strInfo,
        MAX_INFO_SIZE,
        "%s%s \n",
        "[Info] The software has been successfully installed/uninstalled. ",
        "Thank you for using this software and Have a good day!");
    printf(strInfo);
#endif
    CloseHandle(procinfo.hProcess);
    CloseHandle(procinfo.hThread);
    return 0;
}

