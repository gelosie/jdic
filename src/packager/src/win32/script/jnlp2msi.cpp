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
 
// jnlp2msi.cpp : Defines the entry point for the console application.

#include <string.h>
#include <stdlib.h>
#include <fcntl.h>
#include <direct.h>
#include <stdio.h>
#include <io.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <windows.h>

//////////////////////////////////////////////////////////////////////////////
#define BUFFER_SIZE 2048
#define OPT_SIZE 100
#define OPTS_COUNT 13
// Java executable file location indicator
#define JAVA_EXE_IN_SYSTEM    1
#define JAVA_EXE_IN_SYSTEM32  2
#define JAVA_EXE_IN_JAVA_HOME 3
// Enum for each options
enum opts{ResourceDir, PackageName, OutputDir, Version, Release, LicenseDir, BannerJpgFile,
          PanelJpgFile, MSSDKPath, EnableShortcut, EnableAssociation, EnableSystemCache, Echo};
//acronym option indicator, such as "-pn" for packagename
char sOpts[OPTS_COUNT][OPT_SIZE] = {0};
//full length option indicator, such as "-packagename" for packagename
char lOpts[OPTS_COUNT][OPT_SIZE] = {0};
//Option names, such as "PackageName"
char optNames[OPTS_COUNT][BUFFER_SIZE] = {0};
//Option values, such as "c:\Program files\Microsoft SDK"
char optValues[OPTS_COUNT][BUFFER_SIZE] = {0};
//The given JNLP File path
char JnlpFile [BUFFER_SIZE]  = {0};
//Class to be used to generate the MSI file
char ClassName[] = "org.jdesktop.jdic.packager.Jnlp2Msi";
//Whether to print the java process cmd on terminal
bool echoJavaCmd = false;

//////////////////////////////////////////////////////////////////////////////
/**
 * Init one option relevant arrays
 */
void initOpt(int optIndex, char* loptValue, char* uoptValue, char* nameValue)
{
    strcpy(sOpts[optIndex], loptValue);
    strcpy(lOpts[optIndex], uoptValue);
    strcpy(optNames[optIndex], nameValue);
    strcpy(optValues[optIndex], "\0");
}

//////////////////////////////////////////////////////////////////////////////
/**
 * Initialize arrays: lOpts, uOpts, optNames;
 */
void initOpts()
{
    //      optIndex          acronym    fullname             -D name
    initOpt(ResourceDir,        "-rd",  "-resourcedir",       "ResourceDir");
    initOpt(PackageName,        "-pn",  "-packagename",       "PackageName");
    initOpt(OutputDir,          "-od",  "-outputdir",         "OutputDir");
    initOpt(Version,            "-v",   "-version",           "Version");
    initOpt(Release,            "-r",   "-release",           "Release");
    initOpt(LicenseDir,         "-ld",  "-licensedir",        "LicenseDir");
    initOpt(BannerJpgFile,      "-bjf", "-bannerjpgfile",     "BannerJpgFile");
    initOpt(PanelJpgFile,       "-pjf", "-paneljpgfile",      "PanelJpgFile");
    initOpt(MSSDKPath,          "-msd", "-mssdkdir",          "MSSDKDir");
    initOpt(EnableShortcut,     "-es",  "-enableshortcut",    "EnableShortcut");
    initOpt(EnableAssociation,  "-ea",  "-enableassociation", "EnableAssociation");
    initOpt(EnableSystemCache,  "-esc", "-enablesystemcache", "EnableSystemCache");
    initOpt(Echo,               "-e",   "-echo",              "Echo");
}

//////////////////////////////////////////////////////////////////////////////
/**
 * Process option parametes and generate the target value
 */
int optprocess(char* lopt, char* uopt, char* paraName, char* param, char* argv[], int& iLeft)
{
    if (!strncmp(lopt, argv[iLeft], strlen(lopt)) || !strncmp(uopt, argv[iLeft], strlen(uopt)))
    {   
        if (!strncmp(argv[iLeft], sOpts[Echo], strlen(lopt)) || !strncmp(argv[iLeft], lOpts[Echo], strlen(lopt)))
        {
            if (!strncmp(argv[++iLeft], "1", strlen(lopt)))
            {
                echoJavaCmd = true;
            }
        }
        else if ((!strncmp(argv[iLeft], sOpts[ResourceDir], strlen(lopt)) || !strncmp(argv[iLeft], lOpts[ResourceDir], strlen(lopt))) ||
            (!strncmp(argv[iLeft], sOpts[LicenseDir], strlen(lopt)) || !strncmp(argv[iLeft], lOpts[LicenseDir], strlen(lopt))) ||
            (!strncmp(argv[iLeft], sOpts[BannerJpgFile], strlen(lopt)) || !strncmp(argv[iLeft], lOpts[BannerJpgFile], strlen(lopt))) ||
            (!strncmp(argv[iLeft], sOpts[PanelJpgFile], strlen(lopt)) || !strncmp(argv[iLeft], lOpts[PanelJpgFile], strlen(lopt))) ||
            (!strncmp(argv[iLeft], sOpts[MSSDKPath], strlen(lopt)) || !strncmp(argv[iLeft], lOpts[MSSDKPath], strlen(lopt))))
        {
            //For param MSSDKPath, we need to set param as -DMSSDKPath="... ..."
            sprintf(param, "%s%s%s\"%s\"", "-D", paraName, "=", argv[++iLeft]);
        }
        else 
        {
            //For general case, we just set param as -DBannerJpgFile=... ...
            sprintf(param, "%s%s%s%s", "-D", paraName, "=", argv[++iLeft]);
        }
        iLeft ++;
        return 1;
    }
    //return 0: no param gets handled in this process
    return 0;
}

//////////////////////////////////////////////////////////////////////////////
/**
 * Evaluate if the the JDK version is 1.5.0+
 */
BOOL isJavaVersionOK(char* pFirstPath, char* pMidPath)
{
    // if no JAVA_HOME, exit
    char pJavaPath[BUFFER_SIZE] = {0};
    strcat(pJavaPath, pFirstPath);
    strcat(pJavaPath, "\\");
    strcat(pJavaPath, pMidPath);
    strcat(pJavaPath, "\\java.exe");

    // We determine the version through "java -fullversion"
    //
    TCHAR szCommandLine[BUFFER_SIZE];

    wsprintf(szCommandLine, "%s -fullversion", pJavaPath);

    HANDLE hread[3], hwrite[3];
    SECURITY_ATTRIBUTES sa;

    sa.nLength = sizeof(sa);
    sa.lpSecurityDescriptor = 0;
    sa.bInheritHandle = TRUE;

    memset(hread, 0, sizeof(hread));
    memset(hwrite, 0, sizeof(hwrite));

    // Create pipe to read stdin/stdout/stderr from another process
    //
    if (!(CreatePipe(&hread[0], &hwrite[0], &sa, 1024) &&
          CreatePipe(&hread[1], &hwrite[1], &sa, 1024) &&
          CreatePipe(&hread[2], &hwrite[2], &sa, 1024))) 
    {
        if (hread[0] != NULL) CloseHandle(hread[0]);
        if (hread[1] != NULL) CloseHandle(hread[1]);
        if (hread[2] != NULL) CloseHandle(hread[2]);
        if (hwrite[0] != NULL) CloseHandle(hwrite[0]);
        if (hwrite[1] != NULL) CloseHandle(hwrite[1]);
        if (hwrite[2] != NULL) CloseHandle(hwrite[2]);
    }
    
    STARTUPINFO si;

    ::ZeroMemory(&si, sizeof(STARTUPINFO));
    si.cb = sizeof(STARTUPINFO);
    si.dwFlags = STARTF_USESTDHANDLES;
    si.hStdInput  = hread[0];
    si.hStdOutput = hwrite[1];
    si.hStdError  = hwrite[2];

    SetHandleInformation(hwrite[0], HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(hread[1],  HANDLE_FLAG_INHERIT, FALSE);
    SetHandleInformation(hread[2],  HANDLE_FLAG_INHERIT, FALSE);

    PROCESS_INFORMATION pi;
    ::ZeroMemory(&pi, sizeof(PROCESS_INFORMATION));

    // Run "java -fullversion"
    //
    BOOL ret = ::CreateProcess(NULL, szCommandLine, NULL, NULL, 
                   TRUE, CREATE_NO_WINDOW | DETACHED_PROCESS,
                   NULL, NULL, &si, &pi);

    if (ret) 
    {
        char szOutput[256];

        DWORD dwByteRead = 0;

        // Read output
        ReadFile(hread[2], szOutput, 256, &dwByteRead, NULL);

        // Extract version info
        //
        const char* versionInfo = strstr(szOutput, "\"");
        const char* milestoneInfo = strstr(versionInfo + 1, "-");
        const char* endInfo;
        char lpszUpdVersionInfo[BUFFER_SIZE] = {0};
        char lpszFullVersionInfo[BUFFER_SIZE] = {0};
        char lpszVersionInfo[BUFFER_SIZE] = {0};

        if (milestoneInfo == NULL)
        {
        endInfo = strstr(versionInfo+1, "\"");
        strncpy(lpszUpdVersionInfo, versionInfo + 1, 
             endInfo - versionInfo - 1);
          lpszUpdVersionInfo[endInfo - versionInfo - 1] = '\0';
        }
        else
        {
        endInfo = strstr(milestoneInfo+1, "\"");
        strncpy(lpszUpdVersionInfo, versionInfo + 1, 
             milestoneInfo - versionInfo - 1);
          lpszUpdVersionInfo[milestoneInfo - versionInfo - 1] = '\0';
        }
        strncpy(lpszFullVersionInfo, versionInfo + 1, endInfo - versionInfo - 1);
        lpszFullVersionInfo[endInfo - versionInfo - 1] = '\0';
        const char* updateInfo = strstr(lpszUpdVersionInfo, "_");
        if (updateInfo == NULL)
        {
        strcpy(lpszVersionInfo, lpszUpdVersionInfo);
        }
        else 
        {
        strncpy(lpszVersionInfo, lpszUpdVersionInfo, 
                updateInfo-lpszUpdVersionInfo);
        lpszVersionInfo[updateInfo - lpszUpdVersionInfo] = '\0';
        }

        // If Java Version >= 1.5.0
        if (0 > strncmp(lpszVersionInfo, "1.5.0", strlen("1.5.0")))
            ret = false;
    }

    CloseHandle(hread[0]);
    CloseHandle(hread[1]);
    CloseHandle(hread[2]);
    CloseHandle(hwrite[0]);
    CloseHandle(hwrite[1]);
    CloseHandle(hwrite[2]);

    CloseHandle(pi.hThread);
    CloseHandle(pi.hProcess);

    return ret;
}

//////////////////////////////////////////////////////////////////////////////
/**
 *  Check whether we've got the correct Java version
 */
int checkJavaVersion(void)
{
    char* pJavaExe = NULL;

    // If there is java.exe in system32\ or system\, check it
    if (NULL != (pJavaExe = getenv("windir")))
        if (isJavaVersionOK(pJavaExe, "system"))
            return JAVA_EXE_IN_SYSTEM;
        else if (isJavaVersionOK(pJavaExe, "system32"))
            return JAVA_EXE_IN_SYSTEM32;

    // If there is java.exe in JAVA_HOME, check it
    if ( (NULL != (pJavaExe = getenv("JAVA_HOME"))) &&
        isJavaVersionOK(pJavaExe, "bin") )
        return JAVA_EXE_IN_JAVA_HOME;

    return 0;
}

//////////////////////////////////////////////////////////////////////////////
/**
 * Launch java to create the destination msi
 */
int launchJava(int iLevel)
{
    char JavaPath[BUFFER_SIZE] = {0};
    char* pJavaPath = NULL;
    switch(iLevel)
    {
    case JAVA_EXE_IN_SYSTEM:
        if (NULL == (pJavaPath = getenv("windir")))
            return -1;
        strncpy(JavaPath, pJavaPath, sizeof(JavaPath)-1);
        strcat(JavaPath, "\\system\\java.exe");
        break;
    case JAVA_EXE_IN_SYSTEM32:
        if (NULL == (pJavaPath = getenv("windir")))
            return -1;
        strncpy(JavaPath, pJavaPath, sizeof(JavaPath)-1);
        strcat(JavaPath, "\\system32\\java.exe");
        break;
    case JAVA_EXE_IN_JAVA_HOME:
        if (NULL == (pJavaPath = getenv("JAVA_HOME")))
            return -1;
        strncpy(JavaPath, pJavaPath, sizeof(JavaPath)-1);
        strcat(JavaPath, "\\bin\\java.exe");
        break;
    default:
        return -1;
    }

    // fork a process to launch the java program
    STARTUPINFO stinfo = {0}; //info of the window
    stinfo.cb = sizeof(STARTUPINFO);
    PROCESS_INFORMATION procinfo; //info of the process

    char CREATEPROCESS[BUFFER_SIZE];

    sprintf(CREATEPROCESS, "%s %s %s %s %s %s %s %s %s %s %s %s %s %s %s", 
        JavaPath, optValues[ResourceDir], optValues[PackageName], optValues[OutputDir], 
        optValues[Version], optValues[Release], optValues[LicenseDir], 
        optValues[BannerJpgFile], optValues[PanelJpgFile], optValues[MSSDKPath], 
        optValues[EnableShortcut], optValues[EnableAssociation], 
        optValues[EnableSystemCache], ClassName, JnlpFile);

    if (echoJavaCmd) 
    {
        printf("%s\n", CREATEPROCESS);
    }

    if(!CreateProcess(NULL, CREATEPROCESS, NULL, NULL, FALSE, NULL, NULL, NULL, &stinfo, &procinfo))
    {
        return -1;
    }
    // wait for the end of the process
    WaitForSingleObject(procinfo.hProcess, INFINITE);
    CloseHandle(procinfo.hProcess);
    CloseHandle(procinfo.hThread);
    return 0;
}

//////////////////////////////////////////////////////////////////////////////
/**
 * Program entry point.
 */
int main(int argc, char* argv[])
{
    if (argc == 1)
    {
        printf("No Parameters!\n");
    }
    initOpts();
    int iLeft = 1;
    while (iLeft < argc)
    {   
        int i = 0;
        for (; i < OPTS_COUNT; i++)
        {
            if (optprocess(sOpts[i], lOpts[i], optNames[i], optValues[i], argv, iLeft))
            {
                break;   
            }
        }
        if (i < OPTS_COUNT)
        {
            //one option gets handled in this loop
            continue;
        }
        else 
        {
            //no option gets handled in this loop
            strncpy(JnlpFile, argv[iLeft++], sizeof(JnlpFile)-1);
            break;
        }
    }
    
    if (JnlpFile[0] == 0)
    {
        printf("No JnlpFile!\n");
        return -1;
    }

    // Check whether the version of JDK is 1.5.0+
    int iJavaPlace;
    if(0 == (iJavaPlace = checkJavaVersion()))
    {
        printf("This software runs only on JDK 1.5.0+,  \n");
        printf("Please install JDK 1.5.0+ into your system\n");
        return -1;
    }

    if (0 != launchJava(iJavaPlace))
    {
        printf("Create Java process failed!");   
    }
    return 0;
}
