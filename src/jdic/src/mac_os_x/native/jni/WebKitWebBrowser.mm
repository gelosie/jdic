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

/**
 * @author Dmitry Markman
 */
 
#include "CC_DMWebKitView.h"
#include "org_jdesktop_jdic_browser_WebKitWebBrowser.h"

JavaVM *JVM = NULL;


// Simple JNI_OnLoad api
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JVM = vm;
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
    printf("JNI_OnUnload\n");
}


/*
 * Class:     
 * Method:    createNSView1
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_browser_WebKitWebBrowser_createNSView1
  (JNIEnv *env, jobject nsWebKitViewView)
{
    CC_DMWebKitView *webKitView = nil;
    NS_DURING;
    // Here we create our custom NSView
    int initWidth = 200;
    int initHeight = 200;

    jclass clazz = env->FindClass(JRISigClass("org/jdesktop/jdic/browser/WebKitWebBrowser"));
    if(clazz != 0){
        jfieldID fID = env->GetStaticFieldID(clazz,"INIT_WIDTH",JRISigInt);
        if(fID != 0){
            initWidth = env->GetStaticIntField(clazz,fID);
        }
        fID = env->GetStaticFieldID(clazz,"INIT_HEIGHT",JRISigInt);
        if(fID != 0){
            initHeight = env->GetStaticIntField(clazz,fID);
        }
    }
    webKitView = [[CC_DMWebKitView alloc] initWithFrame : NSMakeRect(0,0,initWidth,initHeight)];
    //fprintf(stderr,"OK : WebKitView\n");
    [webKitView setJVM : JVM];
    NS_HANDLER;
    fprintf(stderr,"ERROR : Failed to create WebKitView\n");
    NS_VALUERETURN(0, jlong);
    NS_ENDHANDLER;

    return (jint) webKitView;
}