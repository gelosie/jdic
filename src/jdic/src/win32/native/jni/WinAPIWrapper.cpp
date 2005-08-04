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

#include <afx.h>
#include <afxdisp.h>
#include <stdio.h>

#include <comdef.h>
#include <atlbase.h>
#include <ExDisp.h>

#include <mapi.h>
#include <stdlib.h>
#include <string.h>
#include <atlconv.h>
#include <shlwapi.h>
#include "WinAPIWrapper.h"
#ifdef __cplusplus
extern "C" {
#endif

const int MAX_VALUE_LENGTH = 4096;
const int MAX_MIME_DATA_LENGTH = 256;
const char *EXCEPTION_CLASS = "org/jdesktop/jdic/desktop/internal/LaunchFailedException";

// Native code for WinBrowserService
// Global IWebBrowser2 object.
CComQIPtr<IWebBrowser2, &IID_IWebBrowser2> m_spWebBrowser2;

JNIEXPORT void JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_shutDown
  (JNIEnv *, jclass) {
    m_spWebBrowser2.Release();

    CoUninitialize();
}

JNIEXPORT jboolean JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_nativeBrowseURLInIE
  (JNIEnv *env, jobject obj, jstring url, jstring target) {
    const char* urlStr = env->GetStringUTFChars(url, JNI_FALSE);
    const char* targetStr = env->GetStringUTFChars(target, JNI_FALSE);

    HRESULT hr;
    if(!m_spWebBrowser2) {
        hr = m_spWebBrowser2.CoCreateInstance(CLSID_InternetExplorer, NULL, CLSCTX_ALL);
    }

    CComVariant vDummy;
    CComVariant vURL((char*)urlStr);
    CComVariant vTarget((char*)targetStr);

    hr = m_spWebBrowser2->Navigate2(&vURL, &vDummy, &vTarget, &vDummy, &vDummy);
    
    env->ReleaseStringUTFChars(url, urlStr);
    env->ReleaseStringUTFChars(target, targetStr);

    return (hr == S_OK) ? true : false;
}

JNIEXPORT jintArray JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_RegOpenKey
  (JNIEnv* env, jclass cl, jint hKey, jbyteArray lpSubKey, jint securityMask) {
    HKEY handle;
    const char* str;
    jint tmp[2];
    int errorCode=-1;
    jintArray result;
    //Windows API invocation
    str = (const char*)env->GetByteArrayElements(lpSubKey, NULL);
    errorCode =  RegOpenKeyEx((HKEY)hKey, str, 0, securityMask, &handle);
    env->ReleaseByteArrayElements(lpSubKey, (signed char*)str, 0);
    //constructs return value
    tmp[0]= (int) handle;
    tmp[1]= errorCode;
    result = env->NewIntArray(2);
    if (result != NULL) {
        env->SetIntArrayRegion(result, 0, 2, tmp);
    }
    return result;
}
 
JNIEXPORT jint JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_RegCloseKey
  (JNIEnv* env, jclass cl, jint hKey) {
    return (jint) RegCloseKey((HKEY) hKey);        
};
     
JNIEXPORT jbyteArray JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_RegQueryValueEx
  (JNIEnv* env, jclass cl, jint hKey, jbyteArray valueName) {
    const char* valueNameStr;
    unsigned char buffer[MAX_VALUE_LENGTH];
    jbyteArray result = NULL;
    DWORD valueType;
    DWORD valueSize = MAX_VALUE_LENGTH;
  
    valueNameStr = (const char*)env->GetByteArrayElements(valueName, NULL);
    if (RegQueryValueEx((HKEY)hKey, valueNameStr, NULL, &valueType, buffer,
          &valueSize) == ERROR_SUCCESS) {
        if (valueSize > 0) {
            if ((valueType == REG_SZ)||(valueType == REG_EXPAND_SZ)) {
                result = env->NewByteArray(valueSize);
                if (result != NULL) {
                    env->SetByteArrayRegion(result, 0, valueSize,                    
                    (jbyte*)buffer);
                }
            }
        } else {
            result = NULL;
        }
    }
    env->ReleaseByteArrayElements(valueName, (signed char*)valueNameStr, 0);
    return result;
} 

JNIEXPORT jbyteArray JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_AssocQueryString
  (JNIEnv * env, jclass cl, jbyteArray fileOrProtocal, jbyteArray verb) {
    const TCHAR* fileOrProtocalStr;
    const TCHAR* verbStr;
    jbyteArray result = NULL;
    fileOrProtocalStr = (const char*)env->GetByteArrayElements(fileOrProtocal, NULL);
    verbStr = (const char*)env->GetByteArrayElements(verb, NULL);
    TCHAR pszOut[MAX_VALUE_LENGTH] = "\n";
    DWORD pcchout = MAX_VALUE_LENGTH;
    HRESULT rc = AssocQueryString(ASSOCF_NOTRUNCATE, ASSOCSTR_COMMAND, 
                                  fileOrProtocalStr, verbStr, pszOut, &pcchout);
    if (rc != S_OK) {
        result = NULL;   
    } else {
        result = env->NewByteArray(pcchout + 1);
        if (result != NULL) {
            env->SetByteArrayRegion(result, 0, pcchout + 1, (jbyte*)pszOut);   
        }    
    }
    return result;
}


JNIEXPORT jbyteArray JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_ExpandEnvironmentStrings
  (JNIEnv *env, jclass cl, jbyteArray envVariable) {
    const char* envVariableStr;
    jbyteArray result;
    int resultInt;
    jbyte buffer[MAX_VALUE_LENGTH];
    envVariableStr = (const char*)env->GetByteArrayElements(envVariable, NULL);
 
    // If the function succeeds, the return value is the number of TCHARs stored in the buffer.
    // Or else, the return value is zero. 
    resultInt = ExpandEnvironmentStrings(envVariableStr, (LPTSTR)buffer, MAX_VALUE_LENGTH);
    if(resultInt == ERROR_SUCCESS) {
        result = envVariable;
    } else {
        result = env->NewByteArray(sizeof(TCHAR)*resultInt);
        if (result != NULL) {
            env->SetByteArrayRegion(result, 0, sizeof(TCHAR)*resultInt, (jbyte*)buffer);
        }
    }
    env->ReleaseByteArrayElements(envVariable, (signed char*)envVariableStr, 0);
    return result;
}  

JNIEXPORT jstring JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_resolveLinkFile
  (JNIEnv *env, jclass cl, jbyteArray filePath) {
    const char* filePathStr = (const char*)env->GetByteArrayElements(filePath, NULL);

    HRESULT hr;
    IShellLink *psl = NULL;         
    IPersistFile *ppf = NULL;
    char szGotPath[MAX_PATH];

    hr = CoInitialize(NULL);
    if (SUCCEEDED(hr))
    {
        hr = CoCreateInstance(CLSID_ShellLink, NULL, CLSCTX_INPROC_SERVER,
                               IID_IShellLink, (LPVOID *)&psl);

        if (SUCCEEDED(hr))
        {
            hr = psl->QueryInterface(IID_IPersistFile, (LPVOID *)&ppf);

            if (SUCCEEDED(hr))
            {
                WORD wsz[MAX_PATH];
                // Ensure string is Unicode.
                MultiByteToWideChar(CP_ACP, 0, filePathStr, -1, wsz, MAX_PATH);
                hr = ppf->Load(wsz, STGM_READ);
                if (SUCCEEDED(hr))
                {
                    hr = psl->Resolve(0, SLR_ANY_MATCH);
                    if (SUCCEEDED(hr))
                    {
                        WIN32_FIND_DATA wfd;
                        strcpy(szGotPath, filePathStr);

                        hr = psl->GetPath(szGotPath, MAX_PATH,
                                (WIN32_FIND_DATA *)&wfd, SLGP_SHORTPATH );
                    } 
                } 
            } 
        } // CoCreateInstance() succeeds.
    } // CoInitialize(NULL) succeeds.

    if (ppf != NULL) 
        ppf->Release();

    if (psl != NULL) 
        psl->Release();

    CoUninitialize();
    env->ReleaseByteArrayElements(filePath, (signed char*)filePathStr, 0);
    
    if (SUCCEEDED(hr))
    {
        jstring targetFilePath = env->NewStringUTF(szGotPath);
        return targetFilePath;
    } 
    else 
        return NULL;
}
  
JNIEXPORT jint JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_shellExecute
  (JNIEnv *env, jclass cl, jbyteArray filePath, jbyteArray verb) {
    const char *filePathStr = (const char*)env->GetByteArrayElements(filePath, NULL);
    const char *verbStr = (const char*)env->GetByteArrayElements(verb, NULL);

    HINSTANCE retval = ShellExecute(NULL, verbStr, filePathStr, NULL, NULL, SW_SHOWNORMAL);
    
    env->ReleaseByteArrayElements(filePath, (signed char*)filePathStr, 0);
    env->ReleaseByteArrayElements(verb, (signed char*)verbStr, 0);
    
    return (jint) retval;
}

char * jstringToUnicode(JNIEnv * env, jstring jstr)
{
    int length = env->GetStringLength(jstr);    
    const unsigned short * jcstr = env->GetStringChars(jstr, 0);
    char * ustr = (char *) malloc (length*2 + 1);
    int size = 0;

    size = WideCharToMultiByte(CP_ACP, 0, (unsigned short *)jcstr, 
                               length, ustr, (length*2+1), NULL, NULL);
    if (!size)
        return NULL;
    env->ReleaseStringChars(jstr, jcstr);
    ustr[size]='\0';
    return ustr;
}


JNIEXPORT void JNICALL 
Java_org_jdesktop_jdic_desktop_internal_impl_WinAPIWrapper_openMapiMailer
  (JNIEnv * env, jclass obj, jobjectArray toArray, jobjectArray ccArray, 
   jobjectArray bccArray, jstring subject, jstring body, jobjectArray attachArray) {
    HINSTANCE hlibMAPI = 0;
    unsigned long lhSession, ulResult;
    LPMAPILOGON lpMapiLogon;
    LPMAPISENDMAIL lpMapiSendMail;
    LPMAPILOGOFF lpMapiLogoff;

    char currentDir[MAX_PATH];

    GetCurrentDirectory(MAX_PATH, currentDir);

    hlibMAPI = LoadLibrary("mapi32.dll");
    if (hlibMAPI)
    {
        lpMapiLogon = (LPMAPILOGON) GetProcAddress (hlibMAPI, "MAPILogon");
        lpMapiSendMail = (LPMAPISENDMAIL) GetProcAddress (hlibMAPI, "MAPISendMail");
        ulResult = lpMapiLogon (0 , NULL, NULL, MAPI_LOGON_UI, 0, &lhSession);
    }

    if(ulResult) {
        jclass excCls = env->FindClass(EXCEPTION_CLASS);
        if(excCls == 0) {
            printf("Cannot find class LaunchFailedException\n");
            return;
        }
        switch (ulResult) {
            case MAPI_E_FAILURE:
                env->ThrowNew(excCls, 
                    "Logon failed: One or more unspecified errors occurred during logon.");
                break;
            case MAPI_E_INSUFFICIENT_MEMORY:
                env->ThrowNew(excCls, 
                    "Logon failed: There was insufficient memory to proceed.");
                break;
            case MAPI_E_LOGIN_FAILURE:
                env->ThrowNew(excCls, 
                    "Logon failed: There was no default logon, and the user failed to log on successfully.");
                break;
            case MAPI_E_TOO_MANY_SESSIONS:
                env->ThrowNew(excCls, 
                    "Logon failed: The user had too many sessions open simultaneously.");
                break;
            case MAPI_E_USER_ABORT:
                env->ThrowNew(excCls, 
                    "Logon failed: The user canceled the logon dialog box.");
                break;
            default:
                env->ThrowNew(excCls, "Logon failed: Unexpected exception.");
        }
    }
    if(lhSession)
    {
        MapiMessage msgSend;
        memset(&msgSend, 0, sizeof(MapiMessage));

        if (subject) 
            msgSend.lpszSubject = jstringToUnicode(env, subject);
    
        // Set Recip list to MapiMessage structure
        int toLen = 0;
        if(toArray)
            toLen = env->GetArrayLength(toArray);
        int ccLen = 0;
        if(ccArray)
            ccLen = env->GetArrayLength(ccArray);
        int bccLen = 0;
        if(bccArray)
            bccLen = env->GetArrayLength(bccArray);
        int length = toLen + ccLen + bccLen;
        
        if (length) {
            lpMapiRecipDesc recipSend;
            msgSend.nRecipCount = 0;
            recipSend = (lpMapiRecipDesc) malloc (length * sizeof(MapiRecipDesc));
            memset(recipSend, 0, length*sizeof(MapiRecipDesc));

            for(int i = 0; i < toLen; i ++) {
                jstring to = (jstring) env->GetObjectArrayElement(toArray, i);
                if (to) {
                    char * temp = jstringToUnicode(env, to);
                    recipSend[msgSend.nRecipCount].lpszName = temp;
                    recipSend[msgSend.nRecipCount].lpszAddress = strdup(temp);
                    recipSend[msgSend.nRecipCount].ulRecipClass = MAPI_TO;
                    msgSend.nRecipCount ++;
                }
            }

            for(i = 0; i < ccLen; i ++) {
                jstring cc = (jstring) env->GetObjectArrayElement(ccArray, i);
                if (cc) {
                    char * temp = jstringToUnicode(env, cc);
                    recipSend[msgSend.nRecipCount].lpszName = temp;
                    recipSend[msgSend.nRecipCount].lpszAddress = strdup(temp);
                    recipSend[msgSend.nRecipCount].ulRecipClass = MAPI_CC;
                    msgSend.nRecipCount ++;
                }
            }

            for(i = 0; i < bccLen; i ++) {
                jstring bcc = (jstring) env->GetObjectArrayElement(bccArray, i);
                if (bcc) {
                    char * temp = jstringToUnicode(env, bcc);
                    recipSend[msgSend.nRecipCount].lpszName = temp;
                    recipSend[msgSend.nRecipCount].lpszAddress = strdup(temp);
                    recipSend[msgSend.nRecipCount].ulRecipClass = MAPI_BCC;
                    msgSend.nRecipCount ++;
                }
            }
          
            msgSend.lpRecips = recipSend;
        }
        
        // Set body to MapiMessage structure
        if (body)
            msgSend.lpszNoteText = jstringToUnicode(env, body);
        // Fix the Single-html-attachment problem for the Outlook mailer
        else  
            msgSend.lpszNoteText = " ";
    
            
        //Set attachments to MapiMessage structure
        int attachLen = 0;
        if(attachArray) 
            attachLen = env->GetArrayLength(attachArray);
        if (attachLen) {
            lpMapiFileDesc fileSend;
            msgSend.nFileCount = 0;
            fileSend = (lpMapiFileDesc) malloc (attachLen * sizeof(MapiFileDesc));
            memset(fileSend, 0, attachLen*sizeof(MapiFileDesc));
            for(int i = 0; i < attachLen; i++) {
                jstring file = (jstring) env->GetObjectArrayElement(attachArray, i);
                if (file) {
                    fileSend[i].lpszPathName = jstringToUnicode(env, file);
                    fileSend[i].nPosition    = -1;
                    msgSend.nFileCount++;
                }
            }
            msgSend.lpFiles = fileSend;
        }        
        
        // open message compose window with Mapi API
        ulResult = lpMapiSendMail(lhSession, 0, &msgSend, MAPI_DIALOG | MAPI_LOGON_UI, 0L);

        if(ulResult) {
            jclass excCls = env->FindClass(EXCEPTION_CLASS);
            if(excCls == 0) {
                printf("Cannot find class LaunchFailedException\n");
                return;
            }
            switch (ulResult) {
                case MAPI_E_AMBIGUOUS_RECIPIENT:
                    env->ThrowNew(excCls, 
                        "A recipient matched more than one of the recipient descriptor structures.");
                    break;
                case MAPI_E_ATTACHMENT_NOT_FOUND:
                    env->ThrowNew(excCls, 
                        "The specified attachment was not found.");
                    break;
                case MAPI_E_ATTACHMENT_OPEN_FAILURE:
                    env->ThrowNew(excCls, 
                        "The specified attachment could not be opened.");
                    break;
                case MAPI_E_BAD_RECIPTYPE:
                    env->ThrowNew(excCls, 
                        "The type of a recipient was not MAPI_TO, MAPI_CC, or MAPI_BCC.");
                    break;
                case MAPI_E_FAILURE:
                    env->ThrowNew(excCls, 
                        "One or more unspecified errors occurred.");
                    break;
                case MAPI_E_INSUFFICIENT_MEMORY:
                    env->ThrowNew(excCls, 
                        "There was insufficient memory to proceed.");
                    break;
                case MAPI_E_INVALID_RECIPS:
                    env->ThrowNew(excCls, 
                        "One or more recipients were invalid or did not resolve to any address.");
                    break;
                case MAPI_E_LOGIN_FAILURE:
                    env->ThrowNew(excCls, 
                        "There was no default logon, and the user failed to log on successfully.");
                    break;
                case MAPI_E_TEXT_TOO_LARGE:
                    env->ThrowNew(excCls, 
                        "The text in the message was too large.");
                    break;
                case MAPI_E_TOO_MANY_FILES:
                    env->ThrowNew(excCls, 
                        "There were too many file attachments.");
                    break;
                case MAPI_E_TOO_MANY_RECIPIENTS:
                    env->ThrowNew(excCls, 
                        "There were too many recipients.");
                    break;
                case MAPI_E_UNKNOWN_RECIPIENT:
                    env->ThrowNew(excCls, 
                        "A recipient did not appear in the address list.");
                    break;
                case MAPI_E_USER_ABORT:
                    break;
                default:
                    env->ThrowNew(excCls, 
                        "One or more undefined errors occurred.");
            }
        }
    }

    lpMapiLogoff = (LPMAPILOGOFF) GetProcAddress (hlibMAPI, "MAPILogoff");
    lpMapiLogoff(lhSession, 0, 0, 0);
    if (hlibMAPI)
    {
        FreeLibrary (hlibMAPI);
    }

    SetCurrentDirectory(currentDir);
}

  
#ifdef __cplusplus
}
#endif
