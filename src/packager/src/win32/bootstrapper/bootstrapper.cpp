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
 
// bootstrapper.cpp : Defines the entry point for the console application.

#include "bootstrapper_util.h"
#include <strsafe.h>

int     osMajorVersion = 0;
bool    fWin9X         = false;

int main(int argc, char* argv[])
{
    GetCodePageInfo();
    fWin9X = IsOSWin9X(osMajorVersion);
    TCHAR   msiFileDir[MAX_PATH_SIZE] = "\0";
    TCHAR   msiFileName[MAX_PATH_SIZE] = "\0";
    TCHAR   infoMsg[MAX_INFO_SIZE] = "\0";
    TCHAR strLocalName[MAX_LOCALE_NAME_SIZE];
    UINT codePage;
    WORD langID;
    bool inProgress = AlreadyInProgress(fWin9X, osMajorVersion);
    if (inProgress) 
    {
        //Another instance has been running, simply quit.
        printf("Another instance is running at the same time.\n");
        return -1;
    }

    bool isMSIVersionOK = !IsMsiUpgradeNecessary();
    if (!isMSIVersionOK) 
    {
        //MSI version error
        StringCchPrintf( \
            infoMsg,
            MAX_INFO_SIZE,
            "%s%s\n",
            "Error: MSI version error, "
            "please install MSI version 1.5 or later.");
        printf(infoMsg);
        return -1;
    }

    int javawsVersionOK = GetJavawsHome();
    if (javawsVersionOK == ERROR_JAVAWS_VERSION_ERROR)
    {
        //Java WS version less then 1.5.0
        printf("Error: Javaws version 1.5.0 or later is required!\n");
        return -1;
    }
    else if (javawsVersionOK == ERROR_MSI_DB_ERROR)
    {
        printf("Error accessing registry table!\n");
        return -1;
    }

    getLocalName((TCHAR *) strLocalName, langID, codePage);
#ifdef _DEBUG
    printf("[info] Currnt locale is: %s\n", strLocalName);
    printf("[info] System code page is %d\n", codePage);
    printf("[info] System language ID is %d\n", langID);
#endif
    CreateTempDir(msiFileDir);
#ifdef _DEBUG
    printf("[info] Temp dir (%s) created...\n", msiFileDir);
#endif
    GetMsiFileName(msiFileDir, msiFileName);
    ExtractMsiFromResource(msiFileName);
#ifdef _DEBUG
    printf("[info] Temp msi file has been extracted as (%s)...\n", msiFileName);
#endif
    UpdateMsiSummaryInfo(msiFileName, codePage, langID);
#ifdef _DEBUG
    StringCchPrintf( \
        infoMsg,
        MAX_INFO_SIZE,
        "%s%d,%s%d ...\n"
        "[info] MSI Summary Info has been updated as codepage = ",
        codePage,
        "langID = ",
        langID);
    printf(infoMsg);
#endif
    LaunchMsi(msiFileName, strLocalName);
    RemoveDir(msiFileDir);
	return 0;
}

