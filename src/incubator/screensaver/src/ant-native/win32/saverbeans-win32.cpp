/*
 * Copyright © 2004 Sun Microsystems, Inc. All rights reserved. Use is
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
 * USA
 */

/*
 * Bridges xscreensaver (Windows) to Java
 * 
 * Author: Mark Roth <mark.roth@sun.com>
 * Contributor: Bino George <bino.george@sun.com>
 */

#include <windows.h>
#include <windowsx.h>
#include <commctrl.h>
#include <jni.h>
#include <scrnsave.h>
#include <sys/stat.h>

#define DEBUG 0
#define PATH_SEPARATOR ';'
#define SCREENSAVER_BASE_CLASS "org/jdesktop/jdic/screensaver/ScreensaverBase"
#define SCREENSAVER_CONTEXT_CLASS "org/jdesktop/jdic/screensaver/ScreensaverContext"
#define EMBEDDED_FRAME_CLASS "sun/awt/windows/WEmbeddedFrame"

#define SCREENSAVER_CONFIG_CLASS "org/jdesktop/jdic/screensaver/SettingsDialog"

/* XXX - This hard-codes the VM to be Sun's VM - should come up with 
 * a more generic bridge */
#define JRE_KEY	    "Software\\JavaSoft\\Java Runtime Environment"
#define JVM_DLL     "jvm.dll"

/** Copyright notice to appear in generated binaries */
char *copyright = 
"Copyright (c) 2004 Sun Microsystems, Inc., 4150 Network Circle, Santa \n"
"Clara, California 95054, U.S.A. All rights reserved.  Use is subject \n"
"to license terms specified in the file COPYING included in this \n"
"distribution. Sun, Sun Microsystems and the Sun logo are \n"
"trademarks or registered trademarks of Sun Microsystems, Inc. in the \n"
"U.S. and other countries. \n"
"\n"
"This product is covered and controlled by U.S. Export Control laws and \n" 
"may be subject to the export or import laws in other countries. \n"
"Nuclear, missile, chemical biological weapons or nuclear maritime end \n"
"uses or end users, whether direct or indirect, are strictly \n"
"prohibited.  Export or reexport to countries subject to U.S. embargo \n"
"or to entities identified on U.S. export exclusion lists, including, \n"
"but not limited to, the denied persons and specially designated \n"
"nationals lists is strictly prohibited.";

/***************************************************************************/
/* Begin substituted parameters                                            */
/***************************************************************************/

/** 
 * All the blank spaces below make it so that we can easily post-process the
 * final executable template and fill in the actual parameters.  That makes
 * it so that a screensaver developer does not need Visual C++ to build
 * the screensaver, and it also means the developer can build screensavers
 * for all platforms from one platform.
 */

char *jarName=
"[[jar                                                                   ]]";
char *className=
"[[class                                                                   "
"                                                                        ]]";
char *screensaverName=
"[[name                                                                  ]]";
/* Config files can be about 30K max, for now */
char *configData = {
"[[config                                                                  "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                          "
"                                                                        ]]"
};
/***************************************************************************/
/* End substituted parameters                                              */
/***************************************************************************/


/*
 * Pointers to the needed JNI invocation API, initialized by LoadJavaVM.
 * Borrowed from src.zip invoker/java.h
 */

typedef jint (JNICALL *CreateJavaVM_t)(JavaVM **pvm, void **env, void *args);
typedef jint (JNICALL *GetDefaultJavaVMInitArgs_t)(void *args);
typedef struct {
    CreateJavaVM_t CreateJavaVM;
    GetDefaultJavaVMInitArgs_t GetDefaultJavaVMInitArgs;
} InvocationFunctions;

InvocationFunctions ifn;

BOOL             InitApplication( HINSTANCE );
BOOL             InitInstance( HINSTANCE, int );
LRESULT CALLBACK MainWndProc( HWND, UINT, WPARAM, LPARAM );
int              prev_width = 0;
int              prev_height = 0;
JNIEnv *env = 0;
JavaVM *jvm = 0;
jmethodID render_frame_mid = 0;
jobject saver = 0; /* the screen saver instance */
FILE *log;

HANDLE hstdout;
HINSTANCE g_hInst;
HWND g_hwndMain;
BOOL g_bWin95;
TCHAR g_szClassName[] = TEXT( "SaverBeans" );
BOOL started = FALSE;
char szAppName[40] = TEXT( "SaverBeans" );

/* 
 * Create the Graphics object.  The current strategy for this is to 
 * create an EmbeddedFrame that is a child of this native window
 * and snag the Graphics object from it.  The screen saver will be
 * rendering to an embedded frame but that shouldn't matter.
 *
 * Note: This isn't 100% portable since it requires the Sun VM.  If this
 * is an issue, an alternative implementation of saverbeans can be provided
 * without changing the Screensavers themselves (since how we get the
 * Graphics object is an implementation detail of saverbeans.c).  I've been told
 * that realistically VMs other than Sun's VM include the sun.awt packages
 * anyway.
 */

int create_frame( JNIEnv *env, jlong window,
    jclass *class_embedded_frame_result, jobject *frame_result ) 
{
    jmethodID constructor;
    jobject frame = 0;
    jclass cls;
    int valid = 1;
    
    /* Create an embedded frame that is a child of this window */
    cls = env->FindClass( EMBEDDED_FRAME_CLASS );
    if( cls == NULL ) {
        if(DEBUG) fprintf( log, "Could not find class %s\n", EMBEDDED_FRAME_CLASS );
        valid = 0;
    }

    /* Find constructor */
    if( valid ) {
        *class_embedded_frame_result = cls;
        constructor = env->GetMethodID(  cls, "<init>", "(J)V" );
        if( constructor == NULL ) {
            if(DEBUG) fprintf( log, "Could not find constructor for %s\n",
                EMBEDDED_FRAME_CLASS );
            valid = 0;
        }
    }

    /* Create embedded frame */
    if( valid ) {
        frame = env->NewObject( cls, constructor, (jlong)window );
        *frame_result = frame;
        if( frame == NULL ) {
            if(DEBUG) fprintf( log, "Could not create %s\n", EMBEDDED_FRAME_CLASS );
            valid = 0;
        }
    }

    return valid;
}

/* Create the information in the ScreensaverContext object */
int create_context( JNIEnv *env, jclass *class_context_result, 
    jobject *context_result, jclass class_embedded_frame,
    jobject frame, jlong win ) 
{
    int valid = 1;
    jclass class_context;
    jmethodID mid;
    jmethodID constructor;
    jobject context = 0;
    jstring screensaverNameStr;

    /* Find ScreensaverContext class: */
    class_context = env->FindClass( SCREENSAVER_CONTEXT_CLASS );
    if( class_context == NULL ) {
        if(DEBUG) fprintf( log, "Could not find class %s\n", 
            SCREENSAVER_CONTEXT_CLASS );
        valid = 0;
    }
    
    /* Find constructor */
    if( valid ) {
        *class_context_result = class_context;
        constructor = env->GetMethodID( class_context, 
            "<init>", "()V" );
        if( constructor == NULL ) {
            if(DEBUG) fprintf( log, "Could not find constructor for %s\n",
                SCREENSAVER_CONTEXT_CLASS );
            valid = 0;
        }
    }

    /* Create instance */
    if( valid ) {
        context = env->NewObject( class_context, constructor );
        if( context == NULL ) {
            if(DEBUG) fprintf( log, "Could not construct ScreensaverContext\n" );
            valid = 0;
        }
        else {
            *context_result = context;
        }
    }
    
    /* Create an instance of the ScreensaverContext */
    if( valid ) {
        if( context == NULL ) {
            if(DEBUG) fprintf( log, "Error creating screensaver context\n" );
            valid = 0;
        }
    }

    /* Find and call setComponent() */
    if( valid ) {
        mid = env->GetMethodID( class_context, "setComponent", 
            "(Ljava/awt/Component;)V" );
        if( mid == NULL ) {
            if(DEBUG) fprintf( log, 
                "Could not find ScreensaverContext.setComponent()\n");
            valid = 0;
        }
        else {
            env->CallVoidMethod( context, mid, frame );
        }
    }

    /* Tell context to load settings from user's home directory */
    if( valid ) {
        mid = env->GetMethodID( class_context, "loadOptions", 
            "(Ljava/lang/String;)V" );
        if( mid == NULL ) {
            if(DEBUG) fprintf( log, 
                "Could not find ScreensaverContext.loadOptions()\n");
            valid = 0;
        }
        else {
            screensaverNameStr = env->NewStringUTF( screensaverName );
            env->CallVoidMethod( context, mid, screensaverNameStr );
        }
    }

    return valid;
}

int update_context( JNIEnv *env, jclass context_class, jobject context,
    jclass class_embedded_frame, jobject frame, HWND win ) 
{
    int valid = 1;
    jmethodID mid;
    int width, height;
    RECT rc;
    GetClientRect( win, &rc );
    width = rc.right-rc.left;
    height = rc.bottom-rc.top;

    /* If either the width or height changed, change the bounds of the
     * embedded frame as well. */
    if( (prev_width != width) || (prev_height != height) ) {
        /* Set bounds to the bounds of the enclosing window */
        if( valid ) {
            mid = env->GetMethodID( class_embedded_frame, 
                "setBounds", "(IIII)V" );
            if( mid == NULL ) {
                if(DEBUG) fprintf( log, "Could not find setBounds()\n" );
                valid = 0;
            }
            else {
                env->CallVoidMethod( frame, mid, 0, 0, 
                    width, height );
            }
        }
        
        prev_width = width;
        prev_height = height;
    }

    return valid;
}


static jboolean
GetStringFromRegistry(HKEY key, const char *name, char *buf, jint bufsize)
{
    DWORD type, size;

    if (RegQueryValueEx(key, name, 0, &type, 0, &size) == 0
	&& type == REG_SZ
	&& (size < (unsigned int)bufsize)) {
        if (RegQueryValueEx(key, name, 0, 0, (unsigned char *)buf, &size) == 0) {
	    return JNI_TRUE;
	}
    }
    return JNI_FALSE;
}

/*
 * Load a jvm from "jvmpath" and intialize the invocation functions.
 */
jboolean LoadJavaVM( const char *jvmpath ) {
    HINSTANCE handle;

    /* Load the Java VM DLL */
    if( (handle = LoadLibrary(jvmpath)) == 0 ) {
	if(DEBUG) fprintf( log, "Error loading: %s", (char *)jvmpath );
	return JNI_FALSE;
    }

    /* Now get the function addresses */
    ifn.CreateJavaVM =
        (CreateJavaVM_t)GetProcAddress(handle, "JNI_CreateJavaVM");
    ifn.GetDefaultJavaVMInitArgs =
        (GetDefaultJavaVMInitArgs_t)GetProcAddress(handle, "JNI_GetDefaultJavaVMInitArgs");
    if (ifn.CreateJavaVM == 0 || ifn.GetDefaultJavaVMInitArgs == 0) {
	if(DEBUG) fprintf( log, "Error: can't find JNI interfaces in: %s", 
			    (char *)jvmpath, JNI_TRUE);
	return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Find path to JRE based on registry settings.
 * (Collapsed version of the one provided in java_md.c)
 */
jboolean GetJREPath( char *path, jint pathsize ) {
    /* Look for a public JRE on this machine. */
    HKEY key, subkey;
    char version[MAX_PATH];

    /* Find the current version of the JRE */
    if (RegOpenKeyEx(HKEY_LOCAL_MACHINE, JRE_KEY, 0, KEY_READ, &key) != 0) {
	if(DEBUG) fprintf( log, "Error opening registry key '" JRE_KEY "'\n" );
	return JNI_FALSE;
    }

    if (!GetStringFromRegistry(key, "CurrentVersion",
			       version, sizeof(version))) {
	if(DEBUG) fprintf( log, "Failed reading value of registry key:\n\t"
		JRE_KEY "\\CurrentVersion\n" );
	RegCloseKey(key);
	return JNI_FALSE;
    }

    /* Ignore DOTRELEASE - accept any version the user has for now */
    /* XXX - Check that version is at least 1.4 */

    /* Find directory where the current version is installed. */
    if (RegOpenKeyEx(key, version, 0, KEY_READ, &subkey) != 0) {
	if(DEBUG) fprintf( log, "Error opening registry key '"
		JRE_KEY "\\%s'\n", version );
	RegCloseKey(key);
	return JNI_FALSE;
    }

    if (!GetStringFromRegistry(subkey, "JavaHome", path, pathsize)) {
	if(DEBUG) fprintf( log, "Failed reading value of registry key:\n\t"
		JRE_KEY "\\%s\\JavaHome\n", version );
	RegCloseKey(key);
	RegCloseKey(subkey);
	return JNI_FALSE;
    }

    RegCloseKey(key);
    RegCloseKey(subkey);
    return JNI_TRUE;
}

int start_java() {
    JavaVMInitArgs vm_args;
    jint res;
    int valid = 1;
    int tick = 0;
    char jrepath[MAX_PATH];
    char jvmpath[MAX_PATH];
    char *jvmtype = strdup( "client" );
    struct stat s;

	if(DEBUG) fprintf( log, "***\n" );

    if( !GetJREPath(jrepath, sizeof( jrepath )) ) {
        if(DEBUG) fprintf( log, "Error: could not find Java 2 Runtime Environment." );
	valid = 0;
    }
    
    /* Don't read known VM types from jvm.cfg - just use client VM */
    
    /* Look for jvm.dll in jre\bin\client */
    sprintf( jvmpath, "%s\\bin\\%s\\" JVM_DLL, jrepath, jvmtype );
    if( stat( jvmpath, &s ) != 0 ) {
        if(DEBUG) fprintf( log, "Error: could not find " JVM_DLL "\n" );
        valid = 0;
    }
    
    if( valid && !LoadJavaVM( jvmpath ) ) {
        if(DEBUG) fprintf( log, "Error: could not load " JVM_DLL "\n" );
        valid = 0;
    }
    
    if( valid ) {
        started = TRUE;
        vm_args.version = JNI_VERSION_1_2;
        vm_args.options = NULL;
        vm_args.nOptions = 0;
        vm_args.ignoreUnrecognized = JNI_TRUE;

        // XXX - Get foreground, background, delay, jar, class params

        if( jarName == 0 ) {
            if(DEBUG) fprintf( log, "Error: -jar parameter not specified.\n" );
            valid = 0;
        }
    }
    
    if( valid ) {
        /* We will assume the JARs are located in the same dir as the .scr */
        char lpFilename[MAX_PATH];
        char drive[_MAX_DRIVE];
        char dir[_MAX_DIR];
        char fname[_MAX_FNAME];
        char ext[_MAX_EXT];
        GetModuleFileName(GetModuleHandle(NULL), (char*)lpFilename,
            (DWORD)sizeof(lpFilename));
        _splitpath((const char *)lpFilename, drive, dir, fname, ext);
        char jarDirectory[MAX_PATH];
        sprintf(jarDirectory, "%s%s", drive, dir);
        if(DEBUG) fprintf( log, "Detected .scr directory: %s\n", jarDirectory );

        /* Alternate if you want to use C:\WINDOWS\SYSTEM32 or 
         * C:\WINNT\SYSTEM32 */
        //GetSystemDirectory(jarDirectory, MAX_PATH);

        #ifdef JNI_VERSION_1_2
        JavaVMInitArgs vm_args;
        JavaVMOption options[1];
        /* System directory, e.g. C:\\windows\\system32 */
        options[0].optionString = (char *)malloc( MAX_PATH * 2 + 32 );
        sprintf( options[0].optionString, 
            "-Djava.class.path=%s\\%s%c%s\\%s", 
            jarDirectory, jarName,
            PATH_SEPARATOR, 
            jarDirectory, "saverbeans-api.jar" );
        if(DEBUG) fprintf( log, "%s\n", options[0].optionString );
        vm_args.version = 0x00010002;
        vm_args.options = options;
        vm_args.nOptions = 1;
        vm_args.ignoreUnrecognized = JNI_TRUE;
        /* Create the Java VM */
        /* Use dynamically-loaded DLL instead of JNI_CreateJavaVM */
        res = ifn.CreateJavaVM(&jvm, (void**)&env, &vm_args);
        #else
        JDK1_1InitArgs vm_args;
        char *classpath = malloc( 32 + strlen( vm_args.classpath ) + 
            strlen( jarName ) );
        vm_args.version = 0x00010001;
        ifn.GetDefaultJavaVMInitArgs(&vm_args);
        /* Append jarName to the default system class path */
        sprintf( classpath, "%s%c%s\\%s%c%s\\%s",
            vm_args.classpath, 
            PATH_SEPARATOR, 
            systemDirectory, jarName, 
            PATH_SEPARATOR, 
            systemDirectory, "saverbeans-api.jar" );
        vm_args.classpath = classpath;
        /* Create the Java VM */
        /* Use dynamically-loaded DLL instead of JNI_CreateJavaVM */
        res = ifn.CreateJavaVM(&jvm, &env, &vm_args);
        #endif /* JNI_VERSION_1_2 */
        if (res < 0) {
            if(DEBUG) fprintf(log, "Can't create Java VM\n");
            valid = 0;
        }
    }

    return valid;
}

int start_screensaver( HWND win ) {
    int valid = 1;
    jclass cls;
    jclass class_screensaver;
    jclass class_context;
    jclass class_embedded_frame;
    jmethodID constructor;
    jobject context;
    jobject frame;
    jmethodID mid;

    /* Find the class */
    if( valid ) {
        cls = env->FindClass( className );
        if (cls == NULL) {
            if(DEBUG) fprintf( log, "Can't find class %s\n", className );
            valid = 0;
        }
    }
    
    /* Find the org.jdesktop.jdic.screensaver.Screensaver base class */
    if( valid ) {
        class_screensaver = env->FindClass( SCREENSAVER_BASE_CLASS );
        if( class_screensaver == NULL ) {
            if(DEBUG) fprintf( log, "Can't find base class %s\n", 
                SCREENSAVER_BASE_CLASS );
            valid = 0;
        }
    }
    
    /* Make sure the specified class is a subclass of SCREENSAVER_BASE_CLASS */
    if( valid ) {
        if( !(env->IsAssignableFrom( cls, class_screensaver ) ) ) {
            if(DEBUG) fprintf( log, "Error: Class %s is not a subclass of %s\n",
                className, SCREENSAVER_BASE_CLASS );
            valid = 0;
        }
    }
    
    /* Find the no-args constructor */
    if( valid ) {
        constructor = env->GetMethodID( cls, "<init>", "()V" );
        if( constructor == NULL ) {
            if(DEBUG) fprintf( log, 
                "Can't find no-args constructor for screensaver\n" );
            valid = 0;
        }
    }
    
    /* Create an instance of the screensaver */
    if( valid ) {
        saver = env->NewObject( cls, constructor );
        if( saver == NULL ) {
            if(DEBUG) fprintf( log, "Error creating instance of %s\n", className );
            valid = 0;
        }
    }
    
    /* Create embedded frame */
    if( valid ) {
        valid = create_frame( env, (jlong)win,
            &class_embedded_frame, &frame );
    }

    
    /* Create context object */
    if( valid ) {
        valid = create_context( env, &class_context, &context, 
            class_embedded_frame, frame, (jlong)win );
    }

    /* Update size of context object */
    if( valid ) {
        valid = update_context( env, class_context, context,
            class_embedded_frame, frame, win );
    }
    
    /* Initialize screen saver */
    if( valid ) {
        mid = env->GetMethodID( cls, "baseInit", 
            "(Lorg/jdesktop/jdic/screensaver/ScreensaverContext;)V" );
        if( mid == NULL ) {
            if(DEBUG) fprintf( log, "Could not find baseInit() method\n" );
            valid = 0;
        }
        else {
            env->CallVoidMethod( saver, mid, context );
        }
    }
    
    /* Find renderFrame() method */
    if( valid ) {
        render_frame_mid = env->GetMethodID( cls, "renderFrame", 
            "()V" );
        if( render_frame_mid == NULL ) {
            if(DEBUG) fprintf( log, "Could not find renderFrame() method\n" );
            valid = 0;
        }
    }

    return valid;
}

HWND master_hwnd;

void CALLBACK TimerCallback( UINT uID, UINT UMsg, DWORD dwUser, DWORD dw1,
    DWORD dw2 )
{
    static int firstTime = 0;
    int valid = 0;

    if( firstTime == 0 ) {
        firstTime = 1;
        valid = start_java() && start_screensaver( master_hwnd );
        if( valid ) {
            firstTime = 2;
        }
    }
    else if( firstTime == 1 ) {
        return;
    }

    env->CallVoidMethod( saver, render_frame_mid );
}


LRESULT WINAPI ScreenSaverProc( HWND hWnd, UINT uMessage,
                                WPARAM wParam, LPARAM lParam )
{
    static UINT timer;
    static int tick = 60;
    static UINT timerID;
    int valid = 1;

    switch( uMessage ) {
        case WM_CREATE:
            if(DEBUG) log = fopen( "C:\\log.txt", "w" );
	    if(DEBUG) fprintf( log, "Opened log.\n" );
            master_hwnd = hWnd;
            timerID = timeSetEvent( 1000/60, 1000/60, TimerCallback, 0, 
                TIME_PERIODIC );
            break;
        case WM_ERASEBKGND:
            /* Default handler will do it */
            break; 
	case WM_CLOSE:
            if(DEBUG) fprintf( log, "WM_CLOSE\n" );
	    DestroyWindow( hWnd );
	    break;
	case WM_DESTROY:
            /* Finish the screensaver */
            if(DEBUG) fprintf( log, "WM_DESTROY\n" );
            valid = 0;
	    break;
    }

    if( !valid ) {
        if(DEBUG) fprintf( log, "Calling timeKillEvent...\n" );
        timeKillEvent( timerID );
        if(DEBUG) fprintf( log, "timeKillEvent called.\n" );
        //KillTimer( hWnd, timer );
        if( env ) {
            if (env->ExceptionOccurred()) {
                env->ExceptionDescribe();
            }
        }
        /*
        if(DEBUG) fprintf( log, "Destroying Java VM...\n" );
        if(DEBUG) fflush( log );
        if( jvm ) {
            // The VM waits for all threads to exit
            jvm->DestroyJavaVM();
        }
        if(DEBUG) fprintf( log, "Java VM destroyed.\n" );
        if(DEBUG) fflush( log );
        */
  
        if(DEBUG) fclose( log );
        PostQuitMessage( 0 );
        exit( 0 );
    }

    return DefScreenSaverProc( hWnd, uMessage, wParam, lParam );
}

/////////////////////////////////////////////////////////////////////////////
// CONFIGURATION

int start_config() {
    int valid = 1;
    jclass cls;
    jmethodID constructor;
    jobject dialog;
    jstring configDataString;

    if(DEBUG) fprintf( log, "In start_config()\n" );

    /* Find the configuration dialog class */
    if( valid ) {
        cls = env->FindClass( SCREENSAVER_CONFIG_CLASS  );
        if (cls == NULL) {
            if(DEBUG) fprintf( log, "Can't find class %s\n", className );
            valid = 0;
        }
    }
    
    /* Find the one-arg constructor */
    if( valid ) {
        constructor = env->GetMethodID( cls, "<init>", 
            "(Ljava/lang/String;)V" );
        if( constructor == NULL ) {
            if(DEBUG) fprintf( log, 
                "Can't find no-args constructor for config dialog\n" );
            valid = 0;
        }
    }
    
    /* Create an instance of the screensaver config dialog */
    if( valid ) {
        configDataString = env->NewStringUTF( (const char *)configData );
        dialog = env->NewObject( cls, constructor, configDataString );
        if( dialog == NULL ) {
            if(DEBUG) fprintf( log, "Error creating instance of config dialog\n" );
            valid = 0;
        }
        else {
            if(DEBUG) fprintf( log, "Config dialog created.\n" );
        }
    }
    
    return valid;
}

/*
 * Note: Windows needs a resource file with a dialog with id 
 * DLG_SCRNSAVECONFIGURE otherwise the ScreenSaverConfigureDialog 
 * method is not called.  Thus the need for saverbeans.rc.
 * We simply immediately destroy the default dialog and present one
 * created using Java instead.
 */
BOOL WINAPI ScreenSaverConfigureDialog( HWND hDlg, UINT msg, WPARAM wParam,
                                        LPARAM lParam )
{
    BOOL result = FALSE;
    int valid = 0;

    switch(msg) {
        case WM_INITDIALOG:
            // Close the standard window and open our own
            DestroyWindow(hDlg);
            if(DEBUG) log = fopen( "C:\\log.txt", "w" );
            if(DEBUG) fprintf( log, "Opened config.\n" );
            result = TRUE;
            valid = start_java() && start_config();
            break;
        case WM_CLOSE:
            break;
    }
    return result;
}

BOOL WINAPI RegisterDialogClasses( HANDLE hInst ) {
    return TRUE;
}
