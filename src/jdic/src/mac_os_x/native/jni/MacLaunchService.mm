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

#include <jni.h>
#include <Cocoa/Cocoa.h>
#include "org_jdesktop_jdic_desktop_internal_impl_MacLaunchService.h"

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_desktop_internal_impl_MacLaunchService_nativeOpenFile
  (JNIEnv* env, jobject /*obj*/, jstring pathString) 
{
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];

    const char* utf8 = env -> GetStringUTFChars(pathString, JNI_FALSE);

    NSString* fullPath = [NSString stringWithUTF8String: utf8];
    BOOL result = [[NSWorkspace sharedWorkspace] openFile:fullPath];

    env -> ReleaseStringUTFChars(pathString, utf8);

    [pool release];
    return result;
}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_desktop_internal_impl_MacLaunchService_nativePrintFile
  (JNIEnv* env, jobject /*obj*/, jstring pathString)
{
    NSAutoreleasePool* pool = [[NSAutoreleasePool alloc] init];

    const char* utf8 = env -> GetStringUTFChars(pathString, JNI_FALSE);

    // Apple's documentation is wrong: the 'urls' parameter is
    // an NSArray of NSURL, not NSString.
    NSString* fullPath = [NSString stringWithUTF8String:utf8];
    NSURL* url = [NSURL fileURLWithPath:fullPath];
    NSArray* urls = [NSArray arrayWithObject:url];
    BOOL result = [[NSWorkspace sharedWorkspace] openURLs:urls
                                  withAppBundleIdentifier:NULL
                                                  options:NSWorkspaceLaunchAndPrint
                           additionalEventParamDescriptor:NULL
                                        launchIdentifiers:NULL];

    env -> ReleaseStringUTFChars(pathString, utf8);

    [pool release];
    return result;
}
