/*
 * DO NOT EDIT.  THIS FILE IS GENERATED FROM /builds/moz-1-7-branch/mozilla/profile/public/nsIProfileInternal.idl
 */

#ifndef __gen_nsIProfileInternal_h__
#define __gen_nsIProfileInternal_h__


#ifndef __gen_nsIProfile_h__
#include "nsIProfile.h"
#endif

/* For IDL files that don't want to include root IDL files. */
#ifndef NS_NO_VTABLE
#define NS_NO_VTABLE
#endif
class nsICmdLineService; /* forward declaration */

class nsIFile; /* forward declaration */

class nsILocalFile; /* forward declaration */

class nsISupportsArray; /* forward declaration */

#define NS_ERROR_PROFILE_REQUIRES_INTERACTION NS_ERROR_GENERATE_FAILURE(NS_ERROR_MODULE_PROFILE, 1)

/* starting interface:    nsIProfileInternal */
#define NS_IPROFILEINTERNAL_IID_STR "2f977d42-5485-11d4-87e2-0010a4e75ef2"

#define NS_IPROFILEINTERNAL_IID \
  {0x2f977d42, 0x5485, 0x11d4, \
    { 0x87, 0xe2, 0x00, 0x10, 0xa4, 0xe7, 0x5e, 0xf2 }}

class NS_NO_VTABLE nsIProfileInternal : public nsIProfile {
 public: 

  NS_DEFINE_STATIC_IID_ACCESSOR(NS_IPROFILEINTERNAL_IID)

  /**
     * Starts up the profile manager
     *
     * @param cmdLine       Arguments passed from command line - can be null
     * @param canInteract   Whether dialogs can be shown during startup
     *                      If false and dialogs would need to be shown, returns NS_ERROR_PROFILE_REQUIRES_INTERRACTION
     */
  /* [noscript] void startupWithArgs (in nsICmdLineService cmdLine, in boolean canInteract); */
  NS_IMETHOD StartupWithArgs(nsICmdLineService *cmdLine, PRBool canInteract) = 0;

  /**
     * Returns TRUE if in the midst of startupWithArgs
     */
  /* [noscript] readonly attribute boolean isStartingUp; */
  NS_IMETHOD GetIsStartingUp(PRBool *aIsStartingUp) = 0;

  /* long get4xProfileCount (); */
  NS_IMETHOD Get4xProfileCount(PRInt32 *_retval) = 0;

  /**
    * The following values are used with getProfileListX
    *
    * LIST_ONLY_NEW     - the list will contain only migrated profiles
    * LIST_ONLY_OLD     - the list will contain only un-migrated profiles
    * LIST_ALL          - the list will contain all profiles
    * LIST_FOR_IMPORT   - the list is generated from 4.x registry and contains
    *                     all the 4.x profiles
    *                     do not use this except from the import module
    * 
    */
  enum { LIST_ONLY_NEW = 1U };

  enum { LIST_ONLY_OLD = 2U };

  enum { LIST_ALL = 3U };

  enum { LIST_FOR_IMPORT = 4U };

  /* void getProfileListX (in unsigned long which, out unsigned long length, [array, size_is (length), retval] out wstring profileNames); */
  NS_IMETHOD GetProfileListX(PRUint32 which, PRUint32 *length, PRUnichar ***profileNames) = 0;

  /* void migrateProfileInfo (); */
  NS_IMETHOD MigrateProfileInfo(void) = 0;

  /* void migrateAllProfiles (); */
  NS_IMETHOD MigrateAllProfiles(void) = 0;

  /* void migrateProfile (in wstring profileName); */
  NS_IMETHOD MigrateProfile(const PRUnichar *profileName) = 0;

  /* void remigrateProfile (in wstring profileName); */
  NS_IMETHOD RemigrateProfile(const PRUnichar *profileName) = 0;

  /* void forgetCurrentProfile (); */
  NS_IMETHOD ForgetCurrentProfile(void) = 0;

  /* void createDefaultProfile (); */
  NS_IMETHOD CreateDefaultProfile(void) = 0;

  /**
     * Returns the actual profile directory
     */
  /* nsIFile getProfileDir (in wstring profileName); */
  NS_IMETHOD GetProfileDir(const PRUnichar *profileName, nsIFile **_retval) = 0;

  /**
     * Returns the path to a profile which can be shown to the user.
     * If the actual profile directory is salted, returns the unsalted parent.
     * This is the directory which will be deleted by deleteProfile(name, true).
     */
  /* wstring getProfilePath (in wstring profileName); */
  NS_IMETHOD GetProfilePath(const PRUnichar *profileName, PRUnichar **_retval) = 0;

  /**
     * Returns a profile directory of the importType. The path will point to 
     * the 4.x profile directory. Should be used only from the import module.
     */
  /* nsILocalFile getOriginalProfileDir (in wstring profileName); */
  NS_IMETHOD GetOriginalProfileDir(const PRUnichar *profileName, nsILocalFile **_retval) = 0;

  /**
     * Returns the date on which a profile was last used.
     * value is in milliseconds since midnight Jan 1, 1970 GMT (same as nsIFile)
     */
  /* PRInt64 getProfileLastModTime (in wstring profileName); */
  NS_IMETHOD GetProfileLastModTime(const PRUnichar *profileName, PRInt64 *_retval) = 0;

  /* attribute boolean automigrate; */
  NS_IMETHOD GetAutomigrate(PRBool *aAutomigrate) = 0;
  NS_IMETHOD SetAutomigrate(PRBool aAutomigrate) = 0;

  /* readonly attribute nsIFile defaultProfileParentDir; */
  NS_IMETHOD GetDefaultProfileParentDir(nsIFile * *aDefaultProfileParentDir) = 0;

  /* readonly attribute wstring firstProfile; */
  NS_IMETHOD GetFirstProfile(PRUnichar * *aFirstProfile) = 0;

  /**
     * Affects startup behavior when there are multiple profiles.
     *  If FALSE, the profile selection dialog will be shown.
     *  If TRUE, the last used profile will be chosen automatically.
     */
  /* attribute boolean startWithLastUsedProfile; */
  NS_IMETHOD GetStartWithLastUsedProfile(PRBool *aStartWithLastUsedProfile) = 0;
  NS_IMETHOD SetStartWithLastUsedProfile(PRBool aStartWithLastUsedProfile) = 0;

  /* [noscript] void updateRegistry (in nsIFile regName); */
  NS_IMETHOD UpdateRegistry(nsIFile *regName) = 0;

  /* [noscript] void getRegStrings (in wstring profileName, out wstring regString, out wstring regName, out wstring regEmail, out wstring regOption); */
  NS_IMETHOD GetRegStrings(const PRUnichar *profileName, PRUnichar **regString, PRUnichar **regName, PRUnichar **regEmail, PRUnichar **regOption) = 0;

  /* [noscript] void setRegStrings (in wstring profileName, in wstring regString, in wstring regName, in wstring regEmail, in wstring regOption); */
  NS_IMETHOD SetRegStrings(const PRUnichar *profileName, const PRUnichar *regString, const PRUnichar *regName, const PRUnichar *regEmail, const PRUnichar *regOption) = 0;

  /* [noscript] string isRegStringSet (in wstring profileName); */
  NS_IMETHOD IsRegStringSet(const PRUnichar *profileName, char **_retval) = 0;

  /* void createNewProfileWithLocales (in wstring profileName, in wstring nativeProfileDir, in wstring UILocale, in wstring contentLocale, in boolean useExistingDir); */
  NS_IMETHOD CreateNewProfileWithLocales(const PRUnichar *profileName, const PRUnichar *nativeProfileDir, const PRUnichar *UILocale, const PRUnichar *contentLocale, PRBool useExistingDir) = 0;

  /**
    * The remaining methods are deprecated. DO NOT USE THEM.
    */
  /* boolean isCurrentProfileAvailable (); */
  NS_IMETHOD IsCurrentProfileAvailable(PRBool *_retval) = 0;

  /* [noscript] void getCurrentProfileDir (out nsIFile profileDir); */
  NS_IMETHOD GetCurrentProfileDir(nsIFile **profileDir) = 0;

};

/* Use this macro when declaring classes that implement this interface. */
#define NS_DECL_NSIPROFILEINTERNAL \
  NS_IMETHOD StartupWithArgs(nsICmdLineService *cmdLine, PRBool canInteract); \
  NS_IMETHOD GetIsStartingUp(PRBool *aIsStartingUp); \
  NS_IMETHOD Get4xProfileCount(PRInt32 *_retval); \
  NS_IMETHOD GetProfileListX(PRUint32 which, PRUint32 *length, PRUnichar ***profileNames); \
  NS_IMETHOD MigrateProfileInfo(void); \
  NS_IMETHOD MigrateAllProfiles(void); \
  NS_IMETHOD MigrateProfile(const PRUnichar *profileName); \
  NS_IMETHOD RemigrateProfile(const PRUnichar *profileName); \
  NS_IMETHOD ForgetCurrentProfile(void); \
  NS_IMETHOD CreateDefaultProfile(void); \
  NS_IMETHOD GetProfileDir(const PRUnichar *profileName, nsIFile **_retval); \
  NS_IMETHOD GetProfilePath(const PRUnichar *profileName, PRUnichar **_retval); \
  NS_IMETHOD GetOriginalProfileDir(const PRUnichar *profileName, nsILocalFile **_retval); \
  NS_IMETHOD GetProfileLastModTime(const PRUnichar *profileName, PRInt64 *_retval); \
  NS_IMETHOD GetAutomigrate(PRBool *aAutomigrate); \
  NS_IMETHOD SetAutomigrate(PRBool aAutomigrate); \
  NS_IMETHOD GetDefaultProfileParentDir(nsIFile * *aDefaultProfileParentDir); \
  NS_IMETHOD GetFirstProfile(PRUnichar * *aFirstProfile); \
  NS_IMETHOD GetStartWithLastUsedProfile(PRBool *aStartWithLastUsedProfile); \
  NS_IMETHOD SetStartWithLastUsedProfile(PRBool aStartWithLastUsedProfile); \
  NS_IMETHOD UpdateRegistry(nsIFile *regName); \
  NS_IMETHOD GetRegStrings(const PRUnichar *profileName, PRUnichar **regString, PRUnichar **regName, PRUnichar **regEmail, PRUnichar **regOption); \
  NS_IMETHOD SetRegStrings(const PRUnichar *profileName, const PRUnichar *regString, const PRUnichar *regName, const PRUnichar *regEmail, const PRUnichar *regOption); \
  NS_IMETHOD IsRegStringSet(const PRUnichar *profileName, char **_retval); \
  NS_IMETHOD CreateNewProfileWithLocales(const PRUnichar *profileName, const PRUnichar *nativeProfileDir, const PRUnichar *UILocale, const PRUnichar *contentLocale, PRBool useExistingDir); \
  NS_IMETHOD IsCurrentProfileAvailable(PRBool *_retval); \
  NS_IMETHOD GetCurrentProfileDir(nsIFile **profileDir); 

/* Use this macro to declare functions that forward the behavior of this interface to another object. */
#define NS_FORWARD_NSIPROFILEINTERNAL(_to) \
  NS_IMETHOD StartupWithArgs(nsICmdLineService *cmdLine, PRBool canInteract) { return _to StartupWithArgs(cmdLine, canInteract); } \
  NS_IMETHOD GetIsStartingUp(PRBool *aIsStartingUp) { return _to GetIsStartingUp(aIsStartingUp); } \
  NS_IMETHOD Get4xProfileCount(PRInt32 *_retval) { return _to Get4xProfileCount(_retval); } \
  NS_IMETHOD GetProfileListX(PRUint32 which, PRUint32 *length, PRUnichar ***profileNames) { return _to GetProfileListX(which, length, profileNames); } \
  NS_IMETHOD MigrateProfileInfo(void) { return _to MigrateProfileInfo(); } \
  NS_IMETHOD MigrateAllProfiles(void) { return _to MigrateAllProfiles(); } \
  NS_IMETHOD MigrateProfile(const PRUnichar *profileName) { return _to MigrateProfile(profileName); } \
  NS_IMETHOD RemigrateProfile(const PRUnichar *profileName) { return _to RemigrateProfile(profileName); } \
  NS_IMETHOD ForgetCurrentProfile(void) { return _to ForgetCurrentProfile(); } \
  NS_IMETHOD CreateDefaultProfile(void) { return _to CreateDefaultProfile(); } \
  NS_IMETHOD GetProfileDir(const PRUnichar *profileName, nsIFile **_retval) { return _to GetProfileDir(profileName, _retval); } \
  NS_IMETHOD GetProfilePath(const PRUnichar *profileName, PRUnichar **_retval) { return _to GetProfilePath(profileName, _retval); } \
  NS_IMETHOD GetOriginalProfileDir(const PRUnichar *profileName, nsILocalFile **_retval) { return _to GetOriginalProfileDir(profileName, _retval); } \
  NS_IMETHOD GetProfileLastModTime(const PRUnichar *profileName, PRInt64 *_retval) { return _to GetProfileLastModTime(profileName, _retval); } \
  NS_IMETHOD GetAutomigrate(PRBool *aAutomigrate) { return _to GetAutomigrate(aAutomigrate); } \
  NS_IMETHOD SetAutomigrate(PRBool aAutomigrate) { return _to SetAutomigrate(aAutomigrate); } \
  NS_IMETHOD GetDefaultProfileParentDir(nsIFile * *aDefaultProfileParentDir) { return _to GetDefaultProfileParentDir(aDefaultProfileParentDir); } \
  NS_IMETHOD GetFirstProfile(PRUnichar * *aFirstProfile) { return _to GetFirstProfile(aFirstProfile); } \
  NS_IMETHOD GetStartWithLastUsedProfile(PRBool *aStartWithLastUsedProfile) { return _to GetStartWithLastUsedProfile(aStartWithLastUsedProfile); } \
  NS_IMETHOD SetStartWithLastUsedProfile(PRBool aStartWithLastUsedProfile) { return _to SetStartWithLastUsedProfile(aStartWithLastUsedProfile); } \
  NS_IMETHOD UpdateRegistry(nsIFile *regName) { return _to UpdateRegistry(regName); } \
  NS_IMETHOD GetRegStrings(const PRUnichar *profileName, PRUnichar **regString, PRUnichar **regName, PRUnichar **regEmail, PRUnichar **regOption) { return _to GetRegStrings(profileName, regString, regName, regEmail, regOption); } \
  NS_IMETHOD SetRegStrings(const PRUnichar *profileName, const PRUnichar *regString, const PRUnichar *regName, const PRUnichar *regEmail, const PRUnichar *regOption) { return _to SetRegStrings(profileName, regString, regName, regEmail, regOption); } \
  NS_IMETHOD IsRegStringSet(const PRUnichar *profileName, char **_retval) { return _to IsRegStringSet(profileName, _retval); } \
  NS_IMETHOD CreateNewProfileWithLocales(const PRUnichar *profileName, const PRUnichar *nativeProfileDir, const PRUnichar *UILocale, const PRUnichar *contentLocale, PRBool useExistingDir) { return _to CreateNewProfileWithLocales(profileName, nativeProfileDir, UILocale, contentLocale, useExistingDir); } \
  NS_IMETHOD IsCurrentProfileAvailable(PRBool *_retval) { return _to IsCurrentProfileAvailable(_retval); } \
  NS_IMETHOD GetCurrentProfileDir(nsIFile **profileDir) { return _to GetCurrentProfileDir(profileDir); } 

/* Use this macro to declare functions that forward the behavior of this interface to another object in a safe way. */
#define NS_FORWARD_SAFE_NSIPROFILEINTERNAL(_to) \
  NS_IMETHOD StartupWithArgs(nsICmdLineService *cmdLine, PRBool canInteract) { return !_to ? NS_ERROR_NULL_POINTER : _to->StartupWithArgs(cmdLine, canInteract); } \
  NS_IMETHOD GetIsStartingUp(PRBool *aIsStartingUp) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetIsStartingUp(aIsStartingUp); } \
  NS_IMETHOD Get4xProfileCount(PRInt32 *_retval) { return !_to ? NS_ERROR_NULL_POINTER : _to->Get4xProfileCount(_retval); } \
  NS_IMETHOD GetProfileListX(PRUint32 which, PRUint32 *length, PRUnichar ***profileNames) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetProfileListX(which, length, profileNames); } \
  NS_IMETHOD MigrateProfileInfo(void) { return !_to ? NS_ERROR_NULL_POINTER : _to->MigrateProfileInfo(); } \
  NS_IMETHOD MigrateAllProfiles(void) { return !_to ? NS_ERROR_NULL_POINTER : _to->MigrateAllProfiles(); } \
  NS_IMETHOD MigrateProfile(const PRUnichar *profileName) { return !_to ? NS_ERROR_NULL_POINTER : _to->MigrateProfile(profileName); } \
  NS_IMETHOD RemigrateProfile(const PRUnichar *profileName) { return !_to ? NS_ERROR_NULL_POINTER : _to->RemigrateProfile(profileName); } \
  NS_IMETHOD ForgetCurrentProfile(void) { return !_to ? NS_ERROR_NULL_POINTER : _to->ForgetCurrentProfile(); } \
  NS_IMETHOD CreateDefaultProfile(void) { return !_to ? NS_ERROR_NULL_POINTER : _to->CreateDefaultProfile(); } \
  NS_IMETHOD GetProfileDir(const PRUnichar *profileName, nsIFile **_retval) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetProfileDir(profileName, _retval); } \
  NS_IMETHOD GetProfilePath(const PRUnichar *profileName, PRUnichar **_retval) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetProfilePath(profileName, _retval); } \
  NS_IMETHOD GetOriginalProfileDir(const PRUnichar *profileName, nsILocalFile **_retval) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetOriginalProfileDir(profileName, _retval); } \
  NS_IMETHOD GetProfileLastModTime(const PRUnichar *profileName, PRInt64 *_retval) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetProfileLastModTime(profileName, _retval); } \
  NS_IMETHOD GetAutomigrate(PRBool *aAutomigrate) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetAutomigrate(aAutomigrate); } \
  NS_IMETHOD SetAutomigrate(PRBool aAutomigrate) { return !_to ? NS_ERROR_NULL_POINTER : _to->SetAutomigrate(aAutomigrate); } \
  NS_IMETHOD GetDefaultProfileParentDir(nsIFile * *aDefaultProfileParentDir) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetDefaultProfileParentDir(aDefaultProfileParentDir); } \
  NS_IMETHOD GetFirstProfile(PRUnichar * *aFirstProfile) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetFirstProfile(aFirstProfile); } \
  NS_IMETHOD GetStartWithLastUsedProfile(PRBool *aStartWithLastUsedProfile) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetStartWithLastUsedProfile(aStartWithLastUsedProfile); } \
  NS_IMETHOD SetStartWithLastUsedProfile(PRBool aStartWithLastUsedProfile) { return !_to ? NS_ERROR_NULL_POINTER : _to->SetStartWithLastUsedProfile(aStartWithLastUsedProfile); } \
  NS_IMETHOD UpdateRegistry(nsIFile *regName) { return !_to ? NS_ERROR_NULL_POINTER : _to->UpdateRegistry(regName); } \
  NS_IMETHOD GetRegStrings(const PRUnichar *profileName, PRUnichar **regString, PRUnichar **regName, PRUnichar **regEmail, PRUnichar **regOption) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetRegStrings(profileName, regString, regName, regEmail, regOption); } \
  NS_IMETHOD SetRegStrings(const PRUnichar *profileName, const PRUnichar *regString, const PRUnichar *regName, const PRUnichar *regEmail, const PRUnichar *regOption) { return !_to ? NS_ERROR_NULL_POINTER : _to->SetRegStrings(profileName, regString, regName, regEmail, regOption); } \
  NS_IMETHOD IsRegStringSet(const PRUnichar *profileName, char **_retval) { return !_to ? NS_ERROR_NULL_POINTER : _to->IsRegStringSet(profileName, _retval); } \
  NS_IMETHOD CreateNewProfileWithLocales(const PRUnichar *profileName, const PRUnichar *nativeProfileDir, const PRUnichar *UILocale, const PRUnichar *contentLocale, PRBool useExistingDir) { return !_to ? NS_ERROR_NULL_POINTER : _to->CreateNewProfileWithLocales(profileName, nativeProfileDir, UILocale, contentLocale, useExistingDir); } \
  NS_IMETHOD IsCurrentProfileAvailable(PRBool *_retval) { return !_to ? NS_ERROR_NULL_POINTER : _to->IsCurrentProfileAvailable(_retval); } \
  NS_IMETHOD GetCurrentProfileDir(nsIFile **profileDir) { return !_to ? NS_ERROR_NULL_POINTER : _to->GetCurrentProfileDir(profileDir); } 

#if 0
/* Use the code below as a template for the implementation class for this interface. */

/* Header file */
class nsProfileInternal : public nsIProfileInternal
{
public:
  NS_DECL_ISUPPORTS
  NS_DECL_NSIPROFILEINTERNAL

  nsProfileInternal();

private:
  ~nsProfileInternal();

protected:
  /* additional members */
};

/* Implementation file */
NS_IMPL_ISUPPORTS1(nsProfileInternal, nsIProfileInternal)

nsProfileInternal::nsProfileInternal()
{
  /* member initializers and constructor code */
}

nsProfileInternal::~nsProfileInternal()
{
  /* destructor code */
}

/* [noscript] void startupWithArgs (in nsICmdLineService cmdLine, in boolean canInteract); */
NS_IMETHODIMP nsProfileInternal::StartupWithArgs(nsICmdLineService *cmdLine, PRBool canInteract)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* [noscript] readonly attribute boolean isStartingUp; */
NS_IMETHODIMP nsProfileInternal::GetIsStartingUp(PRBool *aIsStartingUp)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* long get4xProfileCount (); */
NS_IMETHODIMP nsProfileInternal::Get4xProfileCount(PRInt32 *_retval)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void getProfileListX (in unsigned long which, out unsigned long length, [array, size_is (length), retval] out wstring profileNames); */
NS_IMETHODIMP nsProfileInternal::GetProfileListX(PRUint32 which, PRUint32 *length, PRUnichar ***profileNames)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void migrateProfileInfo (); */
NS_IMETHODIMP nsProfileInternal::MigrateProfileInfo()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void migrateAllProfiles (); */
NS_IMETHODIMP nsProfileInternal::MigrateAllProfiles()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void migrateProfile (in wstring profileName); */
NS_IMETHODIMP nsProfileInternal::MigrateProfile(const PRUnichar *profileName)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void remigrateProfile (in wstring profileName); */
NS_IMETHODIMP nsProfileInternal::RemigrateProfile(const PRUnichar *profileName)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void forgetCurrentProfile (); */
NS_IMETHODIMP nsProfileInternal::ForgetCurrentProfile()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void createDefaultProfile (); */
NS_IMETHODIMP nsProfileInternal::CreateDefaultProfile()
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* nsIFile getProfileDir (in wstring profileName); */
NS_IMETHODIMP nsProfileInternal::GetProfileDir(const PRUnichar *profileName, nsIFile **_retval)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* wstring getProfilePath (in wstring profileName); */
NS_IMETHODIMP nsProfileInternal::GetProfilePath(const PRUnichar *profileName, PRUnichar **_retval)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* nsILocalFile getOriginalProfileDir (in wstring profileName); */
NS_IMETHODIMP nsProfileInternal::GetOriginalProfileDir(const PRUnichar *profileName, nsILocalFile **_retval)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* PRInt64 getProfileLastModTime (in wstring profileName); */
NS_IMETHODIMP nsProfileInternal::GetProfileLastModTime(const PRUnichar *profileName, PRInt64 *_retval)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* attribute boolean automigrate; */
NS_IMETHODIMP nsProfileInternal::GetAutomigrate(PRBool *aAutomigrate)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}
NS_IMETHODIMP nsProfileInternal::SetAutomigrate(PRBool aAutomigrate)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* readonly attribute nsIFile defaultProfileParentDir; */
NS_IMETHODIMP nsProfileInternal::GetDefaultProfileParentDir(nsIFile * *aDefaultProfileParentDir)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* readonly attribute wstring firstProfile; */
NS_IMETHODIMP nsProfileInternal::GetFirstProfile(PRUnichar * *aFirstProfile)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* attribute boolean startWithLastUsedProfile; */
NS_IMETHODIMP nsProfileInternal::GetStartWithLastUsedProfile(PRBool *aStartWithLastUsedProfile)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}
NS_IMETHODIMP nsProfileInternal::SetStartWithLastUsedProfile(PRBool aStartWithLastUsedProfile)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* [noscript] void updateRegistry (in nsIFile regName); */
NS_IMETHODIMP nsProfileInternal::UpdateRegistry(nsIFile *regName)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* [noscript] void getRegStrings (in wstring profileName, out wstring regString, out wstring regName, out wstring regEmail, out wstring regOption); */
NS_IMETHODIMP nsProfileInternal::GetRegStrings(const PRUnichar *profileName, PRUnichar **regString, PRUnichar **regName, PRUnichar **regEmail, PRUnichar **regOption)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* [noscript] void setRegStrings (in wstring profileName, in wstring regString, in wstring regName, in wstring regEmail, in wstring regOption); */
NS_IMETHODIMP nsProfileInternal::SetRegStrings(const PRUnichar *profileName, const PRUnichar *regString, const PRUnichar *regName, const PRUnichar *regEmail, const PRUnichar *regOption)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* [noscript] string isRegStringSet (in wstring profileName); */
NS_IMETHODIMP nsProfileInternal::IsRegStringSet(const PRUnichar *profileName, char **_retval)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* void createNewProfileWithLocales (in wstring profileName, in wstring nativeProfileDir, in wstring UILocale, in wstring contentLocale, in boolean useExistingDir); */
NS_IMETHODIMP nsProfileInternal::CreateNewProfileWithLocales(const PRUnichar *profileName, const PRUnichar *nativeProfileDir, const PRUnichar *UILocale, const PRUnichar *contentLocale, PRBool useExistingDir)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* boolean isCurrentProfileAvailable (); */
NS_IMETHODIMP nsProfileInternal::IsCurrentProfileAvailable(PRBool *_retval)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* [noscript] void getCurrentProfileDir (out nsIFile profileDir); */
NS_IMETHODIMP nsProfileInternal::GetCurrentProfileDir(nsIFile **profileDir)
{
    return NS_ERROR_NOT_IMPLEMENTED;
}

/* End of implementation class template. */
#endif


#endif /* __gen_nsIProfileInternal_h__ */
