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
 
#include <windows.h>
#include <msi.h>
#include <msiquery.h>

#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <direct.h>
#include <shlwapi.h>
#include <sys/stat.h>

#define MAX_PATH_SIZE 2048

char g_TEMP_BASE_PATH     [MAX_PATH_SIZE] = {0};  // C:\DOCUME~1\ADMINI~1\LOCALS~1\Temp
char g_TEMP_JAVAWS_PATH   [MAX_PATH_SIZE] = {0};  // C:\DOCUME~1\ADMINI~1\LOCALS~1\Temp\[javaws4c1.tmp]
char g_TEMP_INSTALLER_JAR [MAX_PATH_SIZE] = {0};  // C:\DOCUME~1\ADMINI~1\LOCALS~1\Temp\[javaws4c1.tmp]\installer.jar
char g_TEMP_JAVAWS_URL    [MAX_PATH_SIZE] = {0};  // file:///C:/DOCUME~1/ADMINI~1/LOCALS~1/Temp/[javaws4c1.tmp]
char g_JAVAWS_HOME        [MAX_PATH_SIZE] = {0};  // C:\program files\java\j2re1.5.0\bin
char g_SHORTCUT           [128] = {0};            // enough space to hold "-shortcut"
char g_ASSOCIATION        [128] = {0};            // enough space to hold "-association"
char g_CACHE_TYPE         [128] = {0};            // enough space to hold "-system"

FILE *zipfp;

char *dirname(char *path)
{
    char *dstr = strdup(path);

    char *p = dstr ;
    while ( (p = strchr(p,'\\')) != NULL)
    { 
        *p = '/';
        p++;
    }

    char *lastslash = strrchr(dstr, '/');
    if (lastslash != NULL)
    {
        *lastslash = '\0';
    }
    else
    {
        free(dstr) ;
        dstr = NULL ;
    }
    return dstr ;
}

void mkdirs(char* path)
{
    if (strlen(path) <= 0)
        return;
    char dir[MAX_PATH_SIZE];

    strcpy(dir, path);
    char* slash = strrchr(dir, '/');
    if (slash == 0)
        return;
    *slash = 0;
    mkdirs(dir);
    mkdir(path);
}

/* For documentation purposes.
struct zip_header {
  unsigned short  magic0 ; 2 2
  unsigned short  magic1 ; 2 4
  unsigned short  vers; 2 6
  unsigned short  flags; 2 8
  unsigned short  compression; 2 10
  unsigned short  modtime;2 12
  unsigned short  moddate;2 14
  int     crc; 4 18
  int     clen;4  22
  int     ulen; 4 26 
  unsigned short  filenamelen; 2 28
  unsigned short  extrafieldlen; 2 30
};
*/

int readAndWrite()
{
    char filename[MAX_PATH_SIZE];
    char filepath[MAX_PATH_SIZE];
    int rc;

    //Note: We have already read the magic number of 4 bytes.
    //Skip to compression if the file is compressed then abort.
    fseek(zipfp,4,SEEK_CUR);
    unsigned short compression;
    rc = fread(&compression,1,sizeof(unsigned short),zipfp);
    if (compression) 
    {
        //l_abort("Error: Cannot unzip deflated file entries.\n");
        return 0;  
    }

    // Skip over to file length
    fseek(zipfp,12,SEEK_CUR); 
    int file_len;
    rc = fread(&file_len,1,sizeof(int),zipfp);

    unsigned short filenamelen;
    rc = fread(&filenamelen,1,sizeof(unsigned short),zipfp);

    unsigned short extrafieldlen;
    rc = fread(&extrafieldlen,1, sizeof(unsigned short), zipfp);

    rc = fread(filename,filenamelen,sizeof(char),zipfp);
    filename[filenamelen]='\0';
    if (rc <=0) {
        //sprintf(message,"Error: Could not read filename <%s>  from zip header\n", filename);
        return 0;
    }

    bool isDir = (filename[filenamelen-1] == '/') ? true : false;
    sprintf(filepath, "%s\\%s", g_TEMP_JAVAWS_PATH, filename);
    char *pathname = dirname(filepath);
    if (pathname != NULL) {
        mkdirs(pathname);
        free(pathname);
    }

    FILE *filefp=NULL;

    if (!isDir) {
        filefp = fopen(filepath,"wb+");
        if (filefp == NULL) {
            //sprintf(message,"Error:fopen: while opening file <%s>\n",filename);
            return 0;
        }
    }

    if (extrafieldlen > 0) {
        fseek(zipfp,extrafieldlen,SEEK_CUR);
    }

    if (!isDir) {
        //Read and write our file entry
        rc = 1;
        {
            char *buffer = (char *) malloc(file_len+1);
            if(!buffer)
            {
                fclose(filefp);
                return 0;
            }
            rc = fread(buffer, 1, file_len, zipfp);
            int rc2 = fwrite(buffer,1,rc,filefp);
            if (buffer)
                free(buffer);
        }
        fclose(filefp);
    }
    return 1;
}


bool isNext() {
    unsigned short magic[2];
    int rc;
    rc = fread(&magic[0],1,2,zipfp);
    rc = fread(&magic[1],1,2,zipfp);

    return ( (magic[0] == 0x4B50) && (magic[1] == 0x0403) );
}

// Public API
//Open a Zip file and initialize.
int openZipFileReader(char *fname) {
    if (!zipfp) {
        zipfp = fopen(fname, "rb");
        if (!zipfp) {
            return 0;
        }
    }
    return 1;
}

//Close a Zip File Reader
void closeZipFileReader() {
    if (zipfp) {
        fflush(zipfp);
    fclose(zipfp);
    zipfp=NULL;
    }
}

//Read the file and extract its contents to 
//the directory of the zipfile.
int do_extract(char *inputzip)
{
    char *extract_dir = dirname(inputzip);

    if ( (extract_dir != NULL) && (strlen(extract_dir) > 0) ) 
    {
        // ensure directory is created first
        char *dir = (char *) malloc(strlen(extract_dir) + 2);
        if(!dir)
        {
            return 0;
        }

        sprintf(dir, "%s/", extract_dir);
        mkdirs(dir);
        free(dir);
        chdir(extract_dir);
        free(extract_dir);
    }
    if (! openZipFileReader(inputzip)) 
    {
        return 0;
    }

    bool next = isNext();

    // We must have at least one entry
    if (!next) {  
        //sprintf(message,"Error: no entries in zip file.\n");
        return 0;
    }

    while (next) {
        readAndWrite();
        next = isNext();
    }

    closeZipFileReader();
    return 1;
}

/**
 * Recursively remove the specified directory and all its files and sub-dirs.
 *
 * @param dirName Specify the directory name
 * @return 1 if succeed
 */
 
int RemoveDir(char * dirName)
{
    WIN32_FIND_DATA fileData;
    HANDLE hSearch;
    char filePattern[MAX_PATH_SIZE] = {0};
    sprintf(filePattern, "%s\\%s", dirName, "*");

    BOOL fFinished = false;

    hSearch = FindFirstFile(filePattern, &fileData);
    
    while (!fFinished) 
    {
        if (fileData.dwFileAttributes == FILE_ATTRIBUTE_DIRECTORY) 
        {
            if ((strcmp(fileData.cFileName, ".") != 0) && (strcmp(fileData.cFileName, "..") != 0))
            {
                //For none "." & ".." directory, take resursive handling mechanism
                char nextDirName[MAX_PATH_SIZE] = {0};
                sprintf(nextDirName, "%s\\%s", dirName, fileData.cFileName);
                RemoveDir(nextDirName);
            }
        }
        else 
        {
            //For general file, just delete
            char fullFileName[MAX_PATH_SIZE] = {0};
            sprintf(fullFileName, "%s\\%s", dirName, fileData.cFileName);
            DeleteFile(fullFileName);
            
        }
        if (!FindNextFile(hSearch, &fileData) && (GetLastError() == ERROR_NO_MORE_FILES))
                fFinished = true;
    }
    //Close the search handle
    FindClose(hSearch);
    //Then delete the directory
    RemoveDirectory(dirName);
    return 1;
}

/**
 * Generate a unique tempory directory name
 *
 * @param baseDir Specify the base directory (in)
 * @param tempDir String buffer containing the generated dir name (in, out)
 *
 * @return 1 if succeed
 */
int GetTempDirName(char* pBaseDir, char* pTempDir)
{
    char dirPrefix[] = "javaws";
    if (GetTempFileName(pBaseDir, dirPrefix, 0, pTempDir) != 0)
    {
        DeleteFile(pTempDir);
        return 1;
    }
    return 0;
}

/**
 * Extracts the jar file from the MSI database binary field
 *
 * @param hModule Specify the handle for the MSI database
 */
void ExtractBinaryFile (MSIHANDLE hModule, const char* pFieldName, const char* pDstFileName) {
    char cSelectSql[MAX_PATH_SIZE] = {0};
    MSIHANDLE hRecord, hView, hDatabase;

    // get hView & hDatabase
    hDatabase = MsiGetActiveDatabase(hModule);
    sprintf(cSelectSql, "select * from Binary where `Name`='%s'", pFieldName);
    MsiDatabaseOpenView(hDatabase, cSelectSql, &hView);
    MsiViewExecute(hView, NULL);
    char szTemp[256] = {0};
    DWORD dwLength = 256;
    if (MsiViewFetch(hView, &hRecord) != ERROR_SUCCESS)
    {
        return;
    }
    MsiRecordGetString(hRecord, 1, szTemp, &dwLength);
    if (strncmp(szTemp, pFieldName, strlen(pFieldName)))
    {
        return;
    }
    // write into installer.jar
    #define BUFFERSIZE 1024
    char  szBuffer[BUFFERSIZE] = {0};
    DWORD cbBuf = BUFFERSIZE;
    DWORD countWrite = 0;

    FILE* fp = NULL;
    fp = fopen(pDstFileName, "wb+");
    if (NULL == fp) {
        return;
    }

    do {
        if (MsiRecordReadStream(hRecord, 2, szBuffer, &cbBuf) !=ERROR_SUCCESS)
            break; // error 
        countWrite = fwrite(szBuffer, 1, cbBuf, fp);
    } while (countWrite == BUFFERSIZE);

    fclose(fp);

    // close all handles
    MsiCloseHandle(hRecord);
    MsiViewClose(hView);
    MsiCloseHandle(hView);
    MsiCloseHandle(hDatabase);
}

void GetMsiProperty (MSIHANDLE hModule, char* propertyValue, char* propertyName) {
    if(!propertyName)
        return;

    MSIHANDLE hRecord, hView, hDatabase;

    // get hView & hDatabase
    hDatabase = MsiGetActiveDatabase(hModule);
    char pSelectSql[256] = {0};
    sprintf(pSelectSql, "select * from Property where `Property`='%s'", propertyName);

    MsiDatabaseOpenView(hDatabase, pSelectSql, &hView);
    MsiViewExecute(hView, NULL);

    DWORD dwLength = 256;
    if (MsiViewFetch(hView, &hRecord) != ERROR_SUCCESS)
        return;
    MsiRecordGetString(hRecord, 2, propertyValue, &dwLength);

    // close all handles
    MsiCloseHandle(hRecord);
    MsiViewClose(hView);
    MsiCloseHandle(hView);
    MsiCloseHandle(hDatabase);
}

/**
 * Gets the javaws home path, e.g. c:\programe files\java\j2re1.5.0\bin
 */
unsigned int GetJavawsHome()
{
   char KEY_JAVAWS[] = "SOFTWARE\\JavaSoft\\Java Web Start"; 
   char KEY_JAVAWS_CURRENT_VERSION[1024] = {0};
   char VALUE_JAVAWS_CURRNET_VERSION[] = "CurrentVersion";
   char VALUE_JAVAWS_HOME[] = "Home";   
   char JAVAWS_VERSION[128] = {0};
   HKEY hKey;
   DWORD dwType;
   DWORD cbData;
   //Open the key HKEY_LOCAL_MACHINE\\SOFTWAER\JavaSoft\\Java Web Start
   if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, KEY_JAVAWS, 0, KEY_READ, &hKey) == ERROR_SUCCESS)
   {
       cbData = 128;
       //Get the value of CurrentVersion
       if (RegQueryValueEx(hKey, (LPCTSTR)VALUE_JAVAWS_CURRNET_VERSION, NULL, &dwType, (LPBYTE)&JAVAWS_VERSION, &cbData) == ERROR_SUCCESS)
       {   
           //Open the key HKEY_LOCAL_MACHINE\\SOFTWAER\JavaSoft\\Java Web Start\\1.5.0
           sprintf(KEY_JAVAWS_CURRENT_VERSION, "%s\\%s", KEY_JAVAWS, JAVAWS_VERSION);
           RegCloseKey(hKey);
           if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, KEY_JAVAWS_CURRENT_VERSION, 0, KEY_READ, &hKey) == ERROR_SUCCESS)
           {
               //Get the value of the current javaws home
               cbData = MAX_PATH_SIZE;
               if (RegQueryValueEx(hKey, (LPCTSTR)VALUE_JAVAWS_HOME, NULL, &dwType, (LPBYTE)&g_JAVAWS_HOME, &cbData) == ERROR_SUCCESS)
               {
                   RegCloseKey(hKey);
                   return 1;
               }
           }
       }
   }
   return 0;
}

/**
 * Initialize the global static environment variables
 *
 * @param none
 * @return true if succeed
 */
 
BOOL InitGlobal()
{
    //g_TEMP_BASE_PATH: Get the system temporary path
    DWORD dwLenInstallerPath = MAX_PATH_SIZE;
    GetTempPath(dwLenInstallerPath, (LPTSTR)g_TEMP_BASE_PATH);

    //g_TEMP_JAVAWS_PATH: Generate a unique temporary directory
    GetTempDirName(g_TEMP_BASE_PATH, g_TEMP_JAVAWS_PATH);
    CreateDirectory(g_TEMP_JAVAWS_PATH, NULL);

    //Get Javaws home path
    if(GetJavawsHome() == 0)
    {
        return false;
    };

    //g_TEMP_INSTALLER_JAR: Get the full path name for the jar file to be generated
    sprintf(g_TEMP_INSTALLER_JAR, "%s\\%s", g_TEMP_JAVAWS_PATH, "installer.jar");

    //g_TEMP_JAVAWS_URL: Get the url mode of installer.jar's path
    char tempUrl[MAX_PATH_SIZE] = {0};
    int iNumber = 0;
    while (g_TEMP_JAVAWS_PATH[iNumber]) {
        for(int i=0; i<sizeof(g_TEMP_JAVAWS_PATH); i++) {
            if(g_TEMP_JAVAWS_PATH[i]=='\\') {
                tempUrl[i] = '/';
            } else {
                tempUrl[i] = g_TEMP_JAVAWS_PATH[i];
            }
        }
        iNumber++;
    }
    sprintf(g_TEMP_JAVAWS_URL, "file:///%s", tempUrl);

    return true;
}

unsigned int __stdcall InstallAction ( MSIHANDLE hModule )
{
    // store the original path
    char ORIG_PATH[MAX_PATH_SIZE] = {0};  // c:\somewhere
    GetCurrentDirectory(MAX_PATH_SIZE, ORIG_PATH);

    if (InitGlobal() == false)
    {
        return 0;
    }
    
    ExtractBinaryFile(hModule, "JarPack", g_TEMP_INSTALLER_JAR);

    do_extract(g_TEMP_INSTALLER_JAR);

	// get JnlpFileName Property from msi Property table
    char jnlpFileName[256] = {0};
    GetMsiProperty(hModule, jnlpFileName, "JnlpFileName");
    char TEMP_JNLP_FILE[MAX_PATH_SIZE] = {0};  // C:\DOCUME~1\ADMINI~1\LOCALS~1\Temp\[javaws4c1.tmp]\draw.jnlp
    sprintf(TEMP_JNLP_FILE, "%s\\%s", g_TEMP_JAVAWS_PATH, jnlpFileName);

	// get Shortcut Property from msi Property table
    char shortcut[256] = {0};
    GetMsiProperty(hModule, shortcut, "Shortcut");
    if(shortcut[0] != '0')
    {
        strncpy(g_SHORTCUT, "-shortcut", strlen("-shortcut"));
    }

	// get CacheType Property from msi Property table
    char cachetype[256] = {0};
    GetMsiProperty(hModule, cachetype, "CacheType");
    if(0 == stricmp(cachetype, "system"))
    {
        strncpy(g_CACHE_TYPE, "-system", strlen("-system"));
    }

	// get Association Property from msi Property table
    char association[256] = {0};
    GetMsiProperty(hModule, association, "Association");
    if(association[0] != '0')
    {
        strncpy(g_ASSOCIATION, "-association", strlen("-association"));
    }

    // install the jnlp software
    STARTUPINFO stinfo = {0}; //info of the window
    stinfo.cb = sizeof(STARTUPINFO);
    PROCESS_INFORMATION procinfo; //info of the process
    char CREATEPROCESS[MAX_PATH_SIZE] = {0};  // javaws -silent -import -system -codebase %s
    sprintf(CREATEPROCESS, "%s\\javaws %s -silent -import %s %s -codebase \"%s\" %s", 
        g_JAVAWS_HOME, g_CACHE_TYPE, g_SHORTCUT, g_ASSOCIATION, g_TEMP_JAVAWS_URL, TEMP_JNLP_FILE);

    if(!CreateProcess(NULL, CREATEPROCESS, NULL, NULL, FALSE, CREATE_NO_WINDOW, NULL, NULL, &stinfo, &procinfo))
    {
        return 0;
    }
    
    // wait for the end of the process
    WaitForSingleObject(procinfo.hProcess, INFINITE);
    CloseHandle(procinfo.hProcess);
    CloseHandle(procinfo.hThread);

    // restore to original path & Remove the generated temporary directory
    SetCurrentDirectory(ORIG_PATH);
    RemoveDir(g_TEMP_JAVAWS_PATH);

    return ERROR_SUCCESS;
}

unsigned int __stdcall UninstallAction ( MSIHANDLE hModule )
{
      //Get Javaws home path
    if(GetJavawsHome() == 0)
    {
        return 0;
    }

    char CREATEPROCESS[MAX_PATH_SIZE] = {0};
    // javaws -silent -uninstall -system http://java.sun.com/products/javawebstart/apps/draw.jnlp

    char uninstallInfo[256] = {0};
    GetMsiProperty(hModule, uninstallInfo, "UninstallInfo");

    // get CacheType Property from msi Property table
    memset(g_CACHE_TYPE, 0, sizeof(g_CACHE_TYPE));
    char cachetype[256] = {0};
    GetMsiProperty(hModule, cachetype, "CacheType");
    if(0 == stricmp(cachetype, "system"))
    {
        strncpy(g_CACHE_TYPE, "-system", strlen("-system"));
    }

    // install the jnlp software
    STARTUPINFO stinfo = {0}; //info of the window
    stinfo.cb = sizeof(STARTUPINFO);
    PROCESS_INFORMATION procinfo; //info of the process
    sprintf(CREATEPROCESS, "%s\\javaws %s -silent -uninstall %s", g_JAVAWS_HOME, g_CACHE_TYPE, uninstallInfo);

    if(!CreateProcess(NULL, CREATEPROCESS, NULL, NULL, FALSE, CREATE_NO_WINDOW, NULL, NULL, &stinfo, &procinfo))
    {
        return 0;
    }

    // wait for the end of the first process
    WaitForSingleObject (procinfo.hProcess, INFINITE);
    CloseHandle(procinfo.hProcess);
    CloseHandle(procinfo.hThread);

    return ERROR_SUCCESS;
}

bool APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
                     )
{
    return TRUE;
}
