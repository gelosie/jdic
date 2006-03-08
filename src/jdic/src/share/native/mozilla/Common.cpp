/* vim:set ts=4 sw=4 sts=4 et cin: */
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "Common.h"
#include "ProfileDirServiceProvider.h"
#include "MsgServer.h"
#include "Message.h"
#include "Util.h"

// from the Gecko SDK
#include "nsXPCOM.h"
#include "nsXPCOMCID.h"
#include "nsEmbedString.h"
#include "nsMemory.h"
#include "nsIServiceManager.h"
#include "nsIComponentManager.h"
#include "nsIProperties.h"
#include "nsIPrefService.h"
#include "nsILocalFile.h"
#include "plstr.h"
#include "nsIDOMDocument.h"
#include "nsIDOMNodeList.h"
#include "nsIDOMElement.h"
#include "nsIDOMHTMLDocument.h"

// below files are copied from the mozilla source tree (not part of the Gecko 
// SDK and subject to change in future versions of Mozilla)

// copied from the mozilla 1.7 source tree to support 1.7+.
#include "nsIProfileInternal.h"

// copied from the mozilla 1.4 source tree to support 1.4 through 1.6.
#include "nsIHttpProtocolHandler.h"
#include "nsIProfileInternalOld.h"

// copied from the mozilla 1.7 source tree to support the conversion of
// UTF-16 to UTF-8 or UTF-8 to UTF-16 for mozilla 1.4, as there is no frozen 
// API for the conversion for 1.4. It's not needed for 1.7.
#ifndef USING_GECKO_SDK_1_7
#include "nsUTF8Utils.h"
#endif

// from nsAppDirectoryServiceDefs.h (which is not part of the
// Gecko SDK v1.4)
#define NS_APP_USER_PROFILES_ROOT_DIR "DefProfRt"

// Check the Mozilla version that is being used.
nsresult GetMozillaVersion(char* versionBuf, size_t versionBufSize)
{
    nsresult rv;

    nsCOMPtr<nsIHttpProtocolHandler> httpHandler;
    rv = GetService("@mozilla.org/network/protocol;1?name=http",
                    NS_GET_IID(nsIHttpProtocolHandler),
                    getter_AddRefs(httpHandler));
    NS_ENSURE_SUCCESS(rv, rv);

    // UserAgent string looks like:
    //   Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7) Gecko/20040616 
    // or:
    //   Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.7) Gecko/20040616
    //
    // Extract the "rv" field, which is returned by the GetMisc method.

    nsEmbedCString misc;
    rv = httpHandler->GetMisc(misc);
    if (NS_FAILED(rv)) {
        return rv;
    }        

    // As verified with Mozilla 1.4, the returned UserAgent string is:
    //   Mozilla/5.0 (Windows; ; Windows NT 5.1) Gecko/20030624
    // the "rv" field is not included in it. Which is fixed in 
    //   https://bugzilla.mozilla.org/show_bug.cgi?id=220220
    // and checked into Mozilla 1.6a. If misc is empty, assume it's 1.4, as 
    // we just need to identify if the Mozilla version is above 1.7 or not.
    const char* miscString;
    if (misc.Length() == 0) {
        miscString = "1.4";
    } else {
        miscString = misc.get(); 
        if (miscString[0] != 'r' || miscString[1] != 'v' || miscString[2] != ':')
            return NS_ERROR_UNEXPECTED;
    }

    PL_strncpyz(versionBuf, &miscString[3], versionBufSize);
    return NS_OK;
}

nsresult GetSpecialDirectory(const char *key, nsIFile **result)
{
    nsresult rv;

    nsCOMPtr<nsIProperties> dirSvc;
    rv = GetService(NS_DIRECTORY_SERVICE_CONTRACTID,
                    NS_GET_IID(nsIProperties),
                    getter_AddRefs(dirSvc));
    if (NS_FAILED(rv))
        return rv;

    return dirSvc->Get(key, NS_GET_IID(nsIFile), (void **) result);
}

static nsresult GetPrivateProfileDir(nsIFile **profileDir)
{
    nsresult rv;

    nsCOMPtr<nsIFile> profileRootDir;	
    rv = GetSpecialDirectory(NS_APP_USER_PROFILES_ROOT_DIR, 
                             getter_AddRefs(profileRootDir));
    NS_ENSURE_SUCCESS(rv, rv);
    rv = profileRootDir->AppendNative(nsEmbedCString("WebBrowser"));
    NS_ENSURE_SUCCESS(rv, rv);

    *profileDir = profileRootDir;
    NS_IF_ADDREF(*profileDir);
    return NS_OK;
}

static nsresult CopyPrefs(nsIFile *fromFile, nsIFile *toFile)
{
    nsresult rv;

    nsCOMPtr<nsILocalFile> fromLocalFile
            = do_QueryInterface(fromFile, &rv);
    NS_ENSURE_SUCCESS(rv, rv);

    nsCOMPtr<nsILocalFile> toLocalFile
            = do_QueryInterface(toFile, &rv);
    NS_ENSURE_SUCCESS(rv, rv);

    FILE *from_fp, *to_fp;

    rv = fromLocalFile->OpenANSIFileDesc("r", &from_fp);
    NS_ENSURE_SUCCESS(rv, rv);

    rv = toLocalFile->OpenANSIFileDesc("w", &to_fp);
    if (NS_FAILED(rv)) {
        NS_WARNING("unable to open output file");
        fclose(from_fp);
        return rv;
    }

    static const char kHeader[] = 
        "# Mozilla User Preferences\n\n";
    fwrite(kHeader, sizeof(kHeader) - 1, 1, to_fp);

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

    char buf[1024];
    while (fgets(buf, sizeof(buf), from_fp) != NULL) {
        for (PRInt32 i = 0; i < numSettings; i++) {
            if (strstr(buf, interestedSettings[i]) != NULL)
                fwrite(buf, strlen(buf), 1, to_fp);
        }
    }

    fclose(from_fp);
    fclose(to_fp);
    return NS_OK;
}

nsresult InitializeProfile()
{
    nsresult rv;
         
    nsEmbedCString copiedPrefs("copiedprefs.js");

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

    // XXX because nsIProfileInternal was altered breaking binary 
    //     compatibility, and the interface UUID was not changed.  
    //     thus there is no way to use QueryInterface to get around 
    //     this problem.

    // Check the Mozilla version that is being used.
    char versionBuf[32];
    rv = GetMozillaVersion(versionBuf, sizeof(versionBuf));
    NS_ENSURE_SUCCESS(rv, rv);

    int MOZILLA_1_7 = 1;
    if (strncmp(versionBuf, "1.7", 3) < 0) {
        MOZILLA_1_7 = 0;
    }

    nsCOMPtr<nsIProfileInternal> profileService;
    nsCOMPtr<nsIProfileInternalOld> profileServiceOld;
    if (MOZILLA_1_7) {
        rv = GetService(NS_PROFILE_CONTRACTID,
                        NS_GET_IID(nsIProfileInternal),
                        getter_AddRefs(profileService));
    } else {
        rv = GetService(NS_PROFILE_CONTRACTID,
                        NS_GET_IID(nsIProfileInternalOld),
                        getter_AddRefs(profileServiceOld));
    }    
    NS_ENSURE_SUCCESS(rv, rv);
        
    // get the current (last used) profile
    PRUnichar *currProfileName;
    if (MOZILLA_1_7) {
        rv = profileService->GetCurrentProfile(&currProfileName);
    } else {
        rv = profileServiceOld->GetCurrentProfile(&currProfileName);
    }    
    if (NS_SUCCEEDED(rv)) {
        nsCOMPtr<nsIFile> currProfileDir;
        if (MOZILLA_1_7) {
            profileService->GetProfileDir(currProfileName, getter_AddRefs(currProfileDir));
        } else {
            profileServiceOld->GetProfileDir(currProfileName, getter_AddRefs(currProfileDir));
        }    
        nsMemory::Free(currProfileName);
        NS_ENSURE_SUCCESS(rv, rv);
        if (NS_FAILED(rv)) {
            nsMemory::Free(currProfileName);
            return rv;
        }

        rv = currProfileDir->AppendNative(nsEmbedCString("prefs.js"));
        NS_ENSURE_SUCCESS(rv, rv);
        PRBool exists;
        rv = currProfileDir->Exists(&exists);

        if (NS_SUCCEEDED(rv) && exists) {
            CopyPrefs(currProfileDir, privateProfilePrefs);
        }
    }

    // get prefs
    nsCOMPtr<nsIPrefService> pref;
    rv = GetService("@mozilla.org/preferences-service;1",
                    NS_GET_IID(nsIPrefService),
                    getter_AddRefs(pref));
    NS_ENSURE_SUCCESS(rv, rv);

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
	LogMsg(msg);
    SendSocketMessage(-1, CEVENT_INIT_FAILED, msg);
}

// copy ASCII characters from |str| into |result|.  use a temporary buffer.
PRBool
ConvertAsciiToUtf16(const char *str, nsEmbedString &result)
{
    int len = strlen(str);
    PRUnichar *buf = (PRUnichar *) malloc(len * sizeof(PRUnichar));
    if (!buf)
        return PR_FALSE;
    for (int i=0; i<len; ++i)
        buf[i] = PRUnichar(str[i]);
    result.Assign(buf, len);
    free(buf);
    return PR_TRUE;
}

// helper function for converting UTF-8 chars to UTF-16 chars.
PRBool
ConvertUtf8ToUtf16(const nsEmbedCString &aSource, nsEmbedString &aDest)
{
#ifdef USING_GECKO_SDK_1_7    
    // Note: Below function was frozen for Mozilla 1.7, but not available for 1.4.
    // See bug 239123 for details:
    //   http://bugzilla.mozilla.org/show_bug.cgi?id=239123    
    NS_CStringToUTF16(aSource, NS_CSTRING_ENCODING_UTF8, aDest); 
#else    
    nsEmbedCString::const_iterator source_start, source_end;
    CalculateUTF8Length calculator;
    copy_string(aSource.BeginReading(source_start),
                aSource.EndReading(source_end), calculator);
    
    PRUint32 count = calculator.Length();  
    aDest.SetLength(count);

    nsEmbedString::iterator dest;
    aDest.BeginWriting(dest);
    dest.advance(0);
    
    ConvertUTF8toUTF16 converter(dest.get());
    copy_string(aSource.BeginReading(source_start),
                aSource.EndReading(source_end), 
                converter);

    if (converter.Length() != count) {
        aDest.SetLength(0);
        return PR_FALSE;
    }
#endif

    return PR_TRUE;
}

// helper function for converting UTF-16 chars to UTF-8 chars.
PRBool
ConvertUtf16ToUtf8(const nsEmbedString &aSource, nsEmbedCString &aDest)
{
#ifdef USING_GECKO_SDK_1_7    
    // Note: Below function was frozen for Mozilla 1.7, but not available for 1.4.
    // See bug 239123 for details:
    //   http://bugzilla.mozilla.org/show_bug.cgi?id=239123    
    NS_UTF16ToCString(aSource, NS_CSTRING_ENCODING_UTF8, aDest); 
#else    
    nsEmbedString::const_iterator source_start, source_end;
    CalculateUTF8Size calculator;
    copy_string(aSource.BeginReading(source_start),
                aSource.EndReading(source_end), calculator);
    
    PRUint32 count = calculator.Size(); 
    aDest.SetLength(count);
    
    nsEmbedCString::iterator dest;
    aDest.BeginWriting(dest);
    dest.advance(0);
        
    ConvertUTF16toUTF8 converter(dest.get());
    copy_string(aSource.BeginReading(source_start),
                aSource.EndReading(source_end), 
                converter);
    
    if (converter.Size() != count) {
        aDest.SetLength(0);
        return PR_FALSE;
    }
#endif
    
    return PR_TRUE;
}

// helper function for getting xpcom services
nsresult
GetService(const char *aContractID, const nsIID &aIID, void **aResult)
{
    nsCOMPtr<nsIServiceManager> svcMgr;
    nsresult rv = NS_GetServiceManager(getter_AddRefs(svcMgr));
    if (NS_FAILED(rv))
        return rv;

    return svcMgr->GetServiceByContractID(aContractID, aIID, aResult);
}

nsresult
CreateInstance(const char *aContractID, const nsIID &aIID, void **aResult)
{
    nsCOMPtr<nsIComponentManager> compMgr;
    nsresult rv = NS_GetComponentManager(getter_AddRefs(compMgr));
    if (NS_FAILED(rv))
        return rv;

    return compMgr->CreateInstanceByContractID(aContractID, nsnull, aIID, aResult);
}

// helper function for getting the HTML page content.
char* 
GetContent(nsIWebNavigation *aWebNav)
{        
    // JavaScript to return the content of the currently loaded URL
    // in *Mozilla*, which is different from the JavaScript for IE.
    char* MOZ_GETCONTENT_SCRIPT
        = "(new XMLSerializer()).serializeToString(document);";
    return ExecuteScript(aWebNav, MOZ_GETCONTENT_SCRIPT);
}

// helper function for seting the HTML page content.
nsresult 
SetContent(nsIWebNavigation *aWebNav, const char *htmlContent) 
{
    nsCOMPtr<nsIDOMDocument> domDoc;
    aWebNav->GetDocument(getter_AddRefs(domDoc));

    nsCOMPtr<nsIDOMHTMLDocument> domHtmlDoc(do_QueryInterface(domDoc)); 
    domHtmlDoc->Open();

    // The htmlContent must be encoded with "UTF-8" charset from the Java side.
    nsEmbedString unicodeContent;
    ConvertUtf8ToUtf16(nsEmbedCString(htmlContent), unicodeContent);
    domHtmlDoc->Write(unicodeContent);
    
    domHtmlDoc->Close();

    return NS_OK;
}

// helper function for executing javascript string
char* 
ExecuteScript(nsIWebNavigation *aWebNav, const char *jscript)
{   
    // Use JavaScript command 
    //     eval("<the user input JavaScript string>"); 
    // to evaluate the JavaScript contained within the brackets, in some cases 
    // it may return a value. 
    // 
    // To execute this "eval" command and retrieve its returned value, 
    // nsIWebNavigation::LoadURI() is used to load below complete URI:
    //    javascript:var retValue = eval("<the user input JavaScript string>"); \
    //        var heads = document.getElementsByTagName('head');"); \
    //        heads[0].setAttribute(<JDIC_BROWSER_INTERMEDIATE_PROP>, retValue); \
    //        ;void(0);
    const int MAX_JSCRIPTURI_LENGTH = 8192; 
    char jscriptURI[MAX_JSCRIPTURI_LENGTH];
    memset(jscriptURI, '\0', MAX_JSCRIPTURI_LENGTH);

    strcat(jscriptURI, "javascript:");       
    // Tune the given jscript to assign the returned value to a predefine 
    // DOM property of the currently loaded webapge:
    //     JDIC_BROWSER_INTERMEDIATE_PROP
    strcat(jscriptURI, TuneJavaScript(jscript));
    strcat(jscriptURI, ";void(0);");

    nsEmbedString unicodeURI;
    ConvertAsciiToUtf16(jscriptURI, unicodeURI);
    aWebNav->LoadURI(unicodeURI.get(), 
                   nsIWebNavigation::LOAD_FLAGS_NONE,
                   nsnull, 
                   nsnull, 
                   nsnull); 

    // Retrieve the returned value of eval command.
    nsCOMPtr<nsIDOMDocument> doc;
    aWebNav->GetDocument(getter_AddRefs(doc));
        
    nsIDOMNodeList* elements = nsnull;
    nsCOMPtr<nsIDOMNode> node;
        
    nsEmbedString unicodeTag;
    char *elementTag = "head";
    ConvertAsciiToUtf16(elementTag, unicodeTag);
    nsresult rv = doc->GetElementsByTagName(unicodeTag, &elements);  
    if (NS_FAILED(rv)) {
        return NULL;
    }        
 
    rv = elements->Item(0, getter_AddRefs(node));
    nsCOMPtr<nsIDOMElement> elt(do_QueryInterface(node, &rv));
    if (NS_FAILED(rv)) {
        return NULL;
    }        

    nsEmbedString attrValue;
    nsEmbedString unicodeProp;
    char *attrProp = JDIC_BROWSER_INTERMEDIATE_PROP;
    ConvertAsciiToUtf16(attrProp, unicodeProp);
    rv = elt->GetAttribute(unicodeProp, attrValue);
    // Remove the attribute created by JDIC Browser.
    elt->RemoveAttribute(unicodeProp);
    if (attrValue.Length() ==0) 
        return NULL;

    nsEmbedCString utf8AttrValue;
    ConvertUtf16ToUtf8(attrValue, utf8AttrValue);

    // Return the result encoded with "UTF-8" charset, which must be decoded
    // with the same charset in the Java side.
    char* resultStr = strdup(utf8AttrValue.get());
    if (resultStr != NULL && 
        strncmp(resultStr, "undefined", strlen(resultStr)))
        return resultStr;
    else
        return NULL;
}
