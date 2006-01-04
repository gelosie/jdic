/*
 * Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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

#include <jni.h>
#include <windows.h>
#include "wa_ipc.h"
#include "org_jdesktop_jdic_mpcontrol_winamp_Util.h"
#include "org_jdesktop_jdic_mpcontrol_winamp_WinampControl.h"


/*JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_mpcontrol_winamp_Util_findWindow
  (JNIEnv * env, jclass clz, jstring str) {

    HWND hw;
    jchar* chr;
    jsize leng;
    char * wn;
    jboolean bool;
    
    chr     = (*env)->GetStringChars(env, str, &bool);
    leng = (*env)->GetStringLength(env, str);

    wn = malloc(sizeof(char)*leng);

    
    
    hw = FindWindow((LPSTR) wn,NULL);
    
    free(wn);
    
    return (jint)hw;  
  
}*/


JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_mpcontrol_winamp_WinampControl_findWindow
  (JNIEnv * env, jclass clz) {

    return (jint) FindWindow("Winamp v1.x",NULL);
  
}

jstring createString(JNIEnv * env, char* buf) { 
	int i;
	
	int size = strlen(buf)+1;
    jchar* temp = malloc(sizeof(jchar)*size);
	for (i=0;i<size;i++) {
		temp[i] = (jchar) buf[i];
	}
    jstring ret = (*env)->NewString(env, temp, size-1);
	free(temp);
	return ret;
}

char* createCharBuf(JNIEnv * env, jstring str) { 
    const jchar* chr;
    jsize size;
    char * result;
    int i;

    chr     = (jchar*) ((*env)->GetStringChars(env, str, (jboolean*)NULL));
    size = (*env)->GetStringLength(env, str);

    result = malloc(sizeof(char)*(size+1));

	for(i=0;i<size;i++) {
		result[i] = (char) chr[i];
	}
	result[size] = '\0';

	(*env)->ReleaseStringChars (env, str, chr);
	
	return result;
} 


JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_mpcontrol_winamp_WinampControl_getFileNameFromPlayList
   (JNIEnv * env, jclass clz, jint hwnd_winamp, jint position) { 
   
   DWORD winampProcId;
   HANDLE winampProc; 
   char * winampFileName;
   
   char * buffer;
   
   jstring result = NULL;
   
   GetWindowThreadProcessId( (HWND)hwnd_winamp,&winampProcId);
   
   winampProc = OpenProcess(PROCESS_VM_OPERATION|PROCESS_VM_WRITE|PROCESS_VM_READ,FALSE,winampProcId);

   winampFileName=(char *) SendMessage((HWND)hwnd_winamp,WM_WA_IPC,position,IPC_GETPLAYLISTFILE);



   if (winampFileName != NULL) { 
		buffer = malloc(1024);
   		ReadProcessMemory(winampProc,winampFileName,buffer,1024,NULL);
   		result = createString(env, buffer);
   		free(buffer);
   		
   }
   CloseHandle(winampProc);
   
   return result;
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_winamp_WinampControl_addToPlayList
   (JNIEnv * env, jclass clz, jint hwnd_winamp, jstring str) {
 
   char * buf;
 
   buf = createCharBuf(env,str);
 
   COPYDATASTRUCT data;
   data.dwData = 100;
   data.lpData = buf;
   data.cbData = (*env)->GetStringLength(env, str);
   
   SendMessage((HWND)hwnd_winamp, 0x4A,0,(long) &data);
   
}



JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_mpcontrol_winamp_WinampControl_getVersion
  (JNIEnv * env, jclass clz, jint hwnd_winamp) {

    return (jlong)SendMessage((HWND)hwnd_winamp,WM_WA_IPC,0,IPC_GETVERSION);
}

JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_mpcontrol_winamp_Util_sendMessage
  (JNIEnv * env, jclass clz, jint hwnd , jint com, jint par1, jint par2) {
    
    return (jlong)SendMessage((HWND)hwnd,com,par1,par2);
}


JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_mpcontrol_winamp_Util_getWindowText
  (JNIEnv * env, jclass clz, jint hwnd) {
  
  jchar* buf = malloc(sizeof(jchar)*256);
  int leng = GetWindowTextW((HWND)hwnd, (LPWSTR) buf, 256);

  jstring ret = (*env)->NewString(env, buf, leng);
  
  free(buf);
  return ret;
  
}  

