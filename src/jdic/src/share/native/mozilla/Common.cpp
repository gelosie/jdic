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

#include "ProfileDirServiceProvider.h"
#include "nsAppDirectoryServiceDefs.h"
#include "nsCRT.h"
#include "nsIFileStreams.h"
#include "nsILineInputStream.h"
#include "nsIPref.h"
#include "nsIProfileInternal.h"
#include "nsNetCID.h"
#include "nsNetUtil.h"
#include "nsString.h"
#include "MsgServer.h"
#include "Message.h"

static nsresult GetPrivateProfileDir(nsIFile **profileDir)
{
    nsresult rv;

    nsCOMPtr<nsIFile> profileRootDir;
    rv = NS_GetSpecialDirectory(NS_APP_USER_PROFILES_ROOT_DIR, getter_AddRefs(profileRootDir));
    NS_ENSURE_SUCCESS(rv, rv);
    rv = profileRootDir->AppendNative(NS_LITERAL_CSTRING("WebBrowser"));
    NS_ENSURE_SUCCESS(rv, rv);

    *profileDir = profileRootDir;
    NS_IF_ADDREF(*profileDir);
    return NS_OK;
}

static nsresult CopyPrefs(nsIFile *fromFile, nsIFile *toFile)
{
    nsresult rv;

    nsCOMPtr<nsIFileInputStream> fileInputStream = 
        do_CreateInstance(NS_LOCALFILEINPUTSTREAM_CONTRACTID, &rv);
    NS_ENSURE_SUCCESS(rv, rv);

    rv = fileInputStream->Init(fromFile, -1, -1, PR_FALSE);
    NS_ENSURE_SUCCESS(rv, rv);

    nsCOMPtr<nsILineInputStream> lineInputStream = do_QueryInterface(fileInputStream, &rv);
    NS_ENSURE_SUCCESS(rv, rv);

    nsCOMPtr<nsIFileOutputStream> fileOutputStream =
        do_CreateInstance(NS_LOCALFILEOUTPUTSTREAM_CONTRACTID, &rv);
    NS_ENSURE_SUCCESS(rv, rv);

    rv = fileOutputStream->Init(toFile, -1, -1, PR_FALSE);
    NS_ENSURE_SUCCESS(rv, rv);

    static const char kHeader[] = 
        "# Mozilla User Preferences"
        NS_LINEBREAK
        NS_LINEBREAK;
    fileOutputStream->Write(kHeader, sizeof(kHeader) - 1, &rv);

    static const char *interestedSettings[] = {
        "accessibility.typeaheadfind.",
        "browser.display.",
        "browser.enable_automatic_image_resizing",
        "config.use_system_prefs",
        "font.",
        "network.",
        "security.",
    };

    PRInt32 numSettings = sizeof(interestedSettings) / sizeof(char *);

    nsAutoString bufferUnicode;
    nsCAutoString buffer;
    PRBool isMore = PR_TRUE;
    while (isMore && NS_SUCCEEDED(lineInputStream->ReadLine(bufferUnicode, &isMore))) {
        CopyUCS2toASCII(bufferUnicode, buffer);
        for (PRInt32 i = 0; i < numSettings; i++) {
            if (buffer.Find(interestedSettings[i]) != -1) {
                PRUint32 realLength;
                buffer.Append(NS_LINEBREAK);
                rv = fileOutputStream->Write(buffer.get(), buffer.Length(), &realLength);
                NS_ENSURE_SUCCESS(rv, rv);
            }
        }
    }

    return NS_OK;
}

nsresult InitializeProfile()
{
    nsresult rv;
         
    NS_NAMED_LITERAL_CSTRING(copiedPrefs, "copiedprefs.js");

    nsCOMPtr<nsIFile> privateProfileDir;
    rv = GetPrivateProfileDir(getter_AddRefs(privateProfileDir));
    NS_ENSURE_SUCCESS(rv, rv);

    nsCOMPtr<nsIFile> privateProfilePrefs;
    rv = privateProfileDir->Clone(getter_AddRefs(privateProfilePrefs));
    NS_ENSURE_SUCCESS(rv, rv);
    rv = privateProfilePrefs->AppendNative(copiedPrefs);
    NS_ENSURE_SUCCESS(rv, rv);

    // activate our private profile
    nsCOMPtr<ProfileDirServiceProvider> locProvider;
    NS_NewMyProfileDirServiceProvider(getter_AddRefs(locProvider));
    NS_ENSURE_TRUE(locProvider, NS_ERROR_FAILURE);
    rv = locProvider->Register();
    NS_ENSURE_SUCCESS(rv, rv);
    rv = locProvider->SetProfileDir(privateProfileDir);
    NS_ENSURE_SUCCESS(rv, rv);

    nsCOMPtr<nsIProfileInternal> profileService = do_GetService(NS_PROFILE_CONTRACTID, &rv);
    NS_ENSURE_SUCCESS(rv, rv);
        
    // get the current (last used) profile
    nsXPIDLString currProfileName;
    rv = profileService->GetCurrentProfile(getter_Copies(currProfileName));
    if (NS_SUCCEEDED(rv)) {
        nsCOMPtr<nsIFile> currProfileDir;
        profileService->GetProfileDir(currProfileName.get(), getter_AddRefs(currProfileDir));
        NS_ENSURE_SUCCESS(rv, rv);

        rv = currProfileDir->AppendNative(NS_LITERAL_CSTRING("prefs.js"));
        NS_ENSURE_SUCCESS(rv, rv);
        PRBool exists;
        rv = currProfileDir->Exists(&exists);

        if (exists) {
            CopyPrefs(currProfileDir, privateProfilePrefs);
        }
    }

    // get prefs
    nsCOMPtr<nsIPref> pref;
    pref = do_GetService(NS_PREF_CONTRACTID);
    if (!pref)
      return NS_ERROR_FAILURE;

    // regenerate the nsIFile object here, because ReadUserPrefs needs to get the file size
    rv = privateProfileDir->Clone(getter_AddRefs(privateProfilePrefs));
    NS_ENSURE_SUCCESS(rv, rv);
    rv = privateProfilePrefs->AppendNative(copiedPrefs);
    NS_ENSURE_SUCCESS(rv, rv);

    // activate our copied prefs.js
    pref->ReadUserPrefs(privateProfilePrefs);

    return NS_OK;
}

void ReportError(const char* msg)
{
    SendSocketMessage(-1, CEVENT_INIT_FAILED, msg);
}
