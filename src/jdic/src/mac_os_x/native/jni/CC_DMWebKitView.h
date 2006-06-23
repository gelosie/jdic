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
  
#ifndef _H_CC_DMOPENGLVIEW
#define _H_CC_DMOPENGLVIEW

#import <JavaVM/jni.h>
#import <AppKit/AppKit.h>
#import <WebKit/WebView.h>
#import <WebKit/WebFrame.h>
#import <WebKit/WebPreferences.h>
#import <WebKit/WebDocument.h>
#import <WebKit/WebPolicyDelegate.h>
#include "JNISig.h"


enum{
	NSWebKitView_storeJavaObject     = 1,
	NSWebKitView_loadURL		 = 2,
	NSWebKitView_resizeFrame	 = 3,
        NSWebKitView_stopLoading         = 4,
        NSWebKitView_goBack              = 5,
        NSWebKitView_goForward           = 6,
        NSWebKitView_updateCursor        = 7,
        NSWebKitView_runJS               = 8,
        NSWebKitView_textLarger          = 9,
        NSWebKitView_textSmaller         = 10,
        NSWebKitView_search              = 11,
        NSWebKitView_dispose             = 12,
        NSWebKitView_ordercallback       = 13,
        NSWebKitView_loadURLFromString   = 14,
        NSWebKitView_reload              = 15,
};

typedef enum{
    WebViewPolicyNone                    = 0,
    WebViewPolicyInOldWindow             = 1,
    WebViewPolicyInNewWindow             = 2,
} CreatingViewPolicy;

// Interface to our custom NSView
@interface CC_DMWebKitView : WebView  {
	JavaVM 			*jvm;
	jobject 		javaOwner;
}
- (id) initWithFrame: (NSRect) frame;
-(void)awtMessage:(jint)messageID message:(jobject)message env:(JNIEnv *)env;
-(void)errorOccurred : (NSString *)desc  host : (NSString *)host;
-(void)startURLLoading : (NSString *)urlString;
-(void)finishURLLoading;
-(void)setURLTitle : (NSString *)title;
-(jobject)javaOwner;
-(void)setJavaOwner:(jobject)jOwner;
- (void) runMain : (NSObject *)arg;

-(JavaVM *)JVM;
-(void)setJVM:(JavaVM *)vm;
-(CreatingViewPolicy)getCreatingNewWebViewInWindowPolicy;

@end

JNIEnv *GetJEnv(JavaVM *vm,bool &wasAttached);

#endif