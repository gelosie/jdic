/* -*- Mode: C++; tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is mozilla.org code.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Conrad Carlen <ccarlen@netscape.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

// kyle.yuan@sun.com
// the original nsProfileDirServiceProvider depends on XPCOM library,
// so we move and rewrite it here to make our private profile.

#include "Common.h"
#include "ProfileDirServiceProvider.h"

// from the Gecko SDK:
#include "nsDirectoryServiceDefs.h"
#include "nsILocalFile.h"
#include "nsISupportsUtils.h"
#include "nsIObserverService.h"
#include "nsEmbedString.h"

// from nsAppDirectoryServiceDefs.h (which is not included in the
// Gecko SDK v1.4):
#define NS_APP_PREFS_50_DIR                     "PrefD"
#define NS_APP_PREFS_50_FILE                    "PrefF"
#define NS_APP_USER_PROFILE_50_DIR              "ProfD"
#define NS_APP_USER_CHROME_DIR                  "UChrm"
#define NS_APP_LOCALSTORE_50_FILE               "LclSt"
#define NS_APP_HISTORY_50_FILE                  "UHist"
#define NS_APP_USER_PANELS_50_FILE              "UPnls"
#define NS_APP_USER_MIMETYPES_50_FILE           "UMimTyp"
#define NS_APP_BOOKMARKS_50_FILE                "BMarks"
#define NS_APP_DOWNLOADS_50_FILE                "DLoads"
#define NS_APP_SEARCH_50_FILE                   "SrchF"
#define NS_APP_PROFILE_DEFAULTS_50_DIR          "profDef"

// File Name Defines

#define PREFS_FILE_50_NAME           nsEmbedCString("prefs.js")
#define USER_CHROME_DIR_50_NAME      nsEmbedCString("chrome")
#define LOCAL_STORE_FILE_50_NAME     nsEmbedCString("localstore.rdf")
#define HISTORY_FILE_50_NAME         nsEmbedCString("history.dat")
#define PANELS_FILE_50_NAME          nsEmbedCString("panels.rdf")
#define MIME_TYPES_FILE_50_NAME      nsEmbedCString("mimeTypes.rdf")
#define BOOKMARKS_FILE_50_NAME       nsEmbedCString("bookmarks.html")
#define DOWNLOADS_FILE_50_NAME       nsEmbedCString("downloads.rdf")
#define SEARCH_FILE_50_NAME          nsEmbedCString("search.rdf" )

//*****************************************************************************
// ProfileDirServiceProvider::ProfileDirServiceProvider
//*****************************************************************************   

ProfileDirServiceProvider::ProfileDirServiceProvider()
{
}


ProfileDirServiceProvider::~ProfileDirServiceProvider()
{
}

nsresult
ProfileDirServiceProvider::SetProfileDir(nsIFile* aProfileDir)
{
  if (mProfileDir) {    
    PRBool isEqual;
    if (aProfileDir &&
        NS_SUCCEEDED(aProfileDir->Equals(mProfileDir, &isEqual)) && isEqual) {
      NS_WARNING("Setting profile dir to same as current");
      return NS_OK;
    }
  }
  mProfileDir = aProfileDir;
  if (!mProfileDir)
    return NS_OK;
    
  nsresult rv = InitProfileDir(mProfileDir);
  NS_ENSURE_SUCCESS(rv, rv);

  nsCOMPtr<nsIObserverService> observerService;
  rv = GetService("@mozilla.org/observer-service;1",
                  NS_GET_IID(nsIObserverService),
                  getter_AddRefs(observerService));
  if (!observerService)
    return NS_ERROR_FAILURE;

  PRUnichar context[] = {'s','t','a','r','t','u','p','\0'};
  // Notify observers that the profile has changed - Here they respond to new profile
  observerService->NotifyObservers(nsnull, "profile-do-change", context);
  // Now observers can respond to something another observer did on "profile-do-change"
  observerService->NotifyObservers(nsnull, "profile-after-change", context);
  
  return NS_OK;
}

nsresult
ProfileDirServiceProvider::Register()
{
  nsCOMPtr<nsIDirectoryService> directoryService;
  GetService("@mozilla.org/file/directory_service;1",
             NS_GET_IID(nsIDirectoryService),
             getter_AddRefs(directoryService));
  if (!directoryService)
    return NS_ERROR_FAILURE;
  return directoryService->RegisterProvider(this);
}

nsresult
ProfileDirServiceProvider::Shutdown()
{
  nsCOMPtr<nsIObserverService> observerService;
  GetService("@mozilla.org/observer-service;1",
             NS_GET_IID(nsIObserverService),
             getter_AddRefs(observerService));
  if (!observerService)
    return NS_ERROR_FAILURE;
    
  PRUnichar context[] =
    {'s','h','u','t','d','o','w','n','-','p','e','r','s','i','s','t','\0'};
  observerService->NotifyObservers(nsnull, "profile-before-change", context);        
  return NS_OK;
}

//*****************************************************************************
// ProfileDirServiceProvider::nsISupports
//*****************************************************************************   

NS_IMPL_THREADSAFE_ISUPPORTS1(ProfileDirServiceProvider,
                              nsIDirectoryServiceProvider)

//*****************************************************************************
// ProfileDirServiceProvider::nsIDirectoryServiceProvider
//*****************************************************************************   

NS_IMETHODIMP
ProfileDirServiceProvider::GetFile(const char *prop, PRBool *persistant, nsIFile **_retval)
{
  NS_ENSURE_ARG(prop);
  NS_ENSURE_ARG_POINTER(persistant);
  NS_ENSURE_ARG_POINTER(_retval);
  
  // Don't assert - we can be called many times before SetProfileDir() has been called.
  if (!mProfileDir)
    return NS_ERROR_FAILURE;
    
  *persistant = PR_TRUE;
  nsIFile* domainDir = mProfileDir;

  nsCOMPtr<nsIFile>  localFile;
  nsresult rv = NS_ERROR_FAILURE;
      
  if (strcmp(prop, NS_APP_PREFS_50_DIR) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
  }
  else if (strcmp(prop, NS_APP_PREFS_50_FILE) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv))
      rv = localFile->AppendNative(PREFS_FILE_50_NAME);
  }
  else if (strcmp(prop, NS_APP_USER_PROFILE_50_DIR) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
  }
  else if (strcmp(prop, NS_APP_USER_CHROME_DIR) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv))
      rv = localFile->AppendNative(USER_CHROME_DIR_50_NAME);
  }
  else if (strcmp(prop, NS_APP_LOCALSTORE_50_FILE) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv)) {
      rv = localFile->AppendNative(LOCAL_STORE_FILE_50_NAME);
      if (NS_SUCCEEDED(rv))
        rv = EnsureProfileFileExists(localFile, domainDir);
    }
  }
  else if (strcmp(prop, NS_APP_HISTORY_50_FILE) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv))
      rv = localFile->AppendNative(HISTORY_FILE_50_NAME);
  }
  else if (strcmp(prop, NS_APP_USER_PANELS_50_FILE) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv)) {
      rv = localFile->AppendNative(PANELS_FILE_50_NAME);
      if (NS_SUCCEEDED(rv))
        rv = EnsureProfileFileExists(localFile, domainDir);
    }
  }
  else if (strcmp(prop, NS_APP_USER_MIMETYPES_50_FILE) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv)) {
      rv = localFile->AppendNative(MIME_TYPES_FILE_50_NAME);
      if (NS_SUCCEEDED(rv))
        rv = EnsureProfileFileExists(localFile, domainDir);
    }
  }
  else if (strcmp(prop, NS_APP_BOOKMARKS_50_FILE) == 0) {
#ifdef XP_MACOSX
    *persistant = PR_FALSE; // See bug 192124
#endif
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv))
      rv = localFile->AppendNative(BOOKMARKS_FILE_50_NAME);
  }
  else if (strcmp(prop, NS_APP_DOWNLOADS_50_FILE) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv))
      rv = localFile->AppendNative(DOWNLOADS_FILE_50_NAME);
  }
  else if (strcmp(prop, NS_APP_SEARCH_50_FILE) == 0) {
    rv = domainDir->Clone(getter_AddRefs(localFile));
    if (NS_SUCCEEDED(rv)) {
      rv = localFile->AppendNative(SEARCH_FILE_50_NAME);
      if (NS_SUCCEEDED(rv))
        rv = EnsureProfileFileExists(localFile, domainDir);
    }
  }
  
  if (localFile && NS_SUCCEEDED(rv))
    return CallQueryInterface(localFile, _retval);
  
  return rv;
}

//*****************************************************************************
// Protected methods
//*****************************************************************************   

nsresult
ProfileDirServiceProvider::InitProfileDir(nsIFile *profileDir)
{    
  // Make sure our "Profile" folder exists.
  // If it does not, copy the profile defaults to its location.
  
  nsresult rv;    
  PRBool exists;
  rv = profileDir->Exists(&exists);
  NS_ENSURE_SUCCESS(rv, rv);

  if (!exists) {
    nsCOMPtr<nsIFile> profileDefaultsDir;
    nsCOMPtr<nsIFile> profileDirParent;
    nsEmbedCString profileDirName;
    
    profileDir->GetParent(getter_AddRefs(profileDirParent));
    if (!profileDirParent)
      return NS_ERROR_FAILURE;
    rv = profileDir->GetNativeLeafName(profileDirName);
    NS_ENSURE_SUCCESS(rv, rv);

    rv = GetSpecialDirectory(NS_APP_PROFILE_DEFAULTS_50_DIR, getter_AddRefs(profileDefaultsDir));
    NS_ENSURE_SUCCESS(rv, rv);

    rv = profileDefaultsDir->CopyToNative(profileDirParent, profileDirName);
    if (NS_FAILED(rv)) {
      // if copying failed, lets just ensure that the profile directory exists.
      profileDirParent->AppendNative(profileDirName);
      rv = profileDirParent->Create(nsIFile::DIRECTORY_TYPE, 0700);
      NS_ENSURE_SUCCESS(rv, rv);
    }
      
#ifndef XP_MAC
    rv = profileDir->SetPermissions(0700);
    NS_ENSURE_SUCCESS(rv, rv);
#endif

  }
  else {
    PRBool isDir;
    rv = profileDir->IsDirectory(&isDir);
    NS_ENSURE_SUCCESS(rv, rv);
    if (!isDir)
      return NS_ERROR_FILE_NOT_DIRECTORY;
  }

  return rv;
}

nsresult
ProfileDirServiceProvider::EnsureProfileFileExists(nsIFile *aFile, nsIFile *destDir)
{
  nsresult rv;
  PRBool exists;
    
  rv = aFile->Exists(&exists);
  NS_ENSURE_SUCCESS(rv, rv);
  if (exists)
    return NS_OK;
  
  nsCOMPtr<nsIFile> defaultsFile;

  // Attempt first to get the localized subdir of the defaults
  rv = GetSpecialDirectory(NS_APP_PROFILE_DEFAULTS_50_DIR, getter_AddRefs(defaultsFile));
  NS_ENSURE_SUCCESS(rv, rv);
    
  nsEmbedCString leafName;
  rv = aFile->GetNativeLeafName(leafName);
  NS_ENSURE_SUCCESS(rv, rv);
  rv = defaultsFile->AppendNative(leafName);
  NS_ENSURE_SUCCESS(rv, rv);
  
  return defaultsFile->CopyTo(destDir, nsEmbedString());
}

//*****************************************************************************
// Global creation function
//*****************************************************************************   

nsresult NS_NewMyProfileDirServiceProvider(ProfileDirServiceProvider** aProvider)
{
  NS_ENSURE_ARG_POINTER(aProvider);
  *aProvider = nsnull;
  
  ProfileDirServiceProvider *prov = new ProfileDirServiceProvider();
  if (!prov)
    return NS_ERROR_OUT_OF_MEMORY;
  NS_ADDREF(*aProvider = prov);
  return NS_OK;  
}
