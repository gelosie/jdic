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
void setObjectStringField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,NSString *value);
void setObjectBooleanField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,jboolean value);
void setObjectIntField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,jint value);
void setObjectRectField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,NSRect rect);

Boolean debug = NO;

// Implementation of our custom NSView
@implementation CC_DMWebKitView

- (void) runMain : (NSObject *)arg{
    if(arg == NULL) return;
    WebView *webView = (WebView *)arg;
    NSWindow *window = [webView window];
    //WebFrame *frame = [webView mainFrame];
    [window setAcceptsMouseMovedEvents : YES];
    //BOOL fr = [window makeFirstResponder : [[frame frameView] documentView]];
    //NSLog(@"was first responder set %d\n",fr);
    //fprintf(stderr,"from mainRunner %d\n",fr);
}


- (id) initWithFrame: (NSRect) frame
{
	javaOwner = NULL;
	self = [super initWithFrame:frame frameName:nil groupName:nil];
    [self setResourceLoadDelegate:self];
    [self setFrameLoadDelegate:self];
    [self setUIDelegate:self];
    [self setPolicyDelegate:self];
    [self setDownloadDelegate : self];

    //NSLog(@"initWithFrame self %@",self);
   /*
    WebPreferences *pref = [WebPreferences standardPreferences];
    if(pref != nil){
        NSLog(@"%@",pref);
        fprintf(stderr,"isJavaEnabled  %d\n",[pref isJavaEnabled]);
        fprintf(stderr,"isJavaScriptEnabled  %d\n",[pref isJavaScriptEnabled]);
        fprintf(stderr,"javaScriptCanOpenWindowsAutomatically  %d\n",[pref javaScriptCanOpenWindowsAutomatically]);
        
    }*/
	return self;
	
}

-(JavaVM *)JVM
{
	return jvm;
}
-(void)setJVM:(JavaVM *)vm
{
	jvm = vm;
}


-(void)setJavaOwner:(jobject)jOwner
{
	javaOwner = jOwner;
}


-(jobject)javaOwner
{
	return javaOwner;
}

-(CreatingViewPolicy)getCreatingNewWebViewInWindowPolicy
{
    if(javaOwner == NULL || [self JVM] == NULL) return WebViewPolicyNone;
    //
    bool 	wasAttached = false;
    JNIEnv *env = GetJEnv([self JVM],wasAttached);
    if(env == NULL) return WebViewPolicyNone;
    jclass clazz = env->GetObjectClass(javaOwner);
    CreatingViewPolicy policy = WebViewPolicyNone;
    if(clazz != NULL){
        jmethodID mID = env->GetMethodID(clazz,"getNewWebViewCreatingPolicy",JRISigMethod() JRISigInt); 
        if(mID != NULL){
            int javaPolicy = env->CallIntMethod(javaOwner,mID);
            switch(javaPolicy){
                case 0: policy = WebViewPolicyNone;         break;
                case 1: policy = WebViewPolicyInOldWindow;  break;
                case 2: policy = WebViewPolicyInNewWindow;  break;
                default: policy = WebViewPolicyInOldWindow; break;
            }
        }else{
            if (debug)
                fprintf(stderr,"getCreatingNewWebViewInWindowPolicy mID == null\n");
        }
    }
    if(wasAttached) [self JVM]->DetachCurrentThread();
    return policy;
}


-(void)setURLTitle : (NSString *)title
{
	if(javaOwner == NULL || [self JVM] == NULL) return;
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env == NULL) return;
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL){
	    jmethodID mID = env->GetMethodID(clazz,"setURLTitle",JRISigMethod(JRISigClass("java/lang/String")) JRISigVoid); 
	    if(mID != NULL){
	        jstring jstr1 = env->NewStringUTF([title UTF8String]);
	        env->CallVoidMethod(javaOwner,mID,jstr1);
                env->DeleteLocalRef(jstr1);
	    }else{
            if (debug)
                fprintf(stderr,"setURLTitle mID == null\n");
	    }
	}
	if(wasAttached) [self JVM]->DetachCurrentThread();
}

-(void)finishURLLoading
{
	if(javaOwner == NULL || [self JVM] == NULL) return;
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env == NULL) return;
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL){
            WebFrame *webFrame = [self mainFrame];
            BOOL canDoSource = [[[webFrame dataSource] representation] canProvideDocumentSource];
            if(canDoSource){
                NSString *htmlSource = [[[webFrame dataSource] representation] documentSource];
                jstring resString = env->NewStringUTF([htmlSource UTF8String]);
                jmethodID mID = env->GetMethodID(clazz,"setHTMLSource",JRISigMethod(JRISigClass("java/lang/String")) JRISigVoid); 
                if(mID != NULL) env->CallVoidMethod(javaOwner,mID,resString);
                env->DeleteLocalRef(resString);
            }
	    jmethodID mID = env->GetMethodID(clazz,"finishURLLoading",JRISigMethod() JRISigVoid); 
	    if(mID != NULL){
	        env->CallVoidMethod(javaOwner,mID);
	    }else{
                if (debug)
                    fprintf(stderr,"finishURLLoading mID == null\n");
	    }
	}
        if(wasAttached) [self JVM]->DetachCurrentThread();
        [self setNeedsDisplay:TRUE];
}

-(void)startURLLoading : (NSString *)urlString
{
	if(javaOwner == NULL || [self JVM] == NULL) return;
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env == NULL) return;
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL){
	    jmethodID mID = env->GetMethodID(clazz,"startURLLoading",JRISigMethod(JRISigClass("java/lang/String")) JRISigVoid); 
	    if(mID != NULL){
	        jstring jstr1 = env->NewStringUTF([urlString UTF8String]);
	        env->CallVoidMethod(javaOwner,mID,jstr1);
                env->DeleteLocalRef(jstr1);
	    }else{
            if (debug)
                fprintf(stderr,"startURLLoading mID == null\n");
	    }
	}
	if(wasAttached) [self JVM]->DetachCurrentThread();
}


-(void)errorOccurred : (NSString *)desc  host : (NSString *)host
{
	if(javaOwner == NULL || [self JVM] == NULL) return;
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env == NULL) return;
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL){
	    jmethodID mID = env->GetMethodID(clazz,"errorOccurred",JRISigMethod(JRISigClass("java/lang/String") JRISigClass("java/lang/String")) JRISigVoid); 
	    if(mID != NULL){
	        jstring jstr1 = env->NewStringUTF([desc UTF8String]);
	        jstring jstr2 = env->NewStringUTF([host UTF8String]);
	        env->CallVoidMethod(javaOwner,mID,jstr1,jstr2);
                env->DeleteLocalRef(jstr1);
                env->DeleteLocalRef(jstr2);
	    }else{
            if (debug)
                fprintf(stderr,"didFailLoadingWithError mID == null\n");
	    }
	}
	if(wasAttached) [self JVM]->DetachCurrentThread();
}


// Messaging api which is sent async from CocoaComponent's sendMessage api
-(void)awtMessage:(jint)messageID message:(jobject)message env:(JNIEnv *)env
{
    switch (messageID) {
        case NSWebKitView_resizeFrame:
            if (debug)
                fprintf(stderr,"NSWebKitView_resizeFrame!\n");
            //[self setNeedsDisplay:YES];
            [[self window] display];
            break;
        case NSWebKitView_textLarger:
            {
                WebFrame *webFrame = [self mainFrame];
                [[webFrame webView] makeTextLarger:self];
            }
            break;
        case NSWebKitView_textSmaller:
            {
                WebFrame *webFrame = [self mainFrame];
                [[webFrame webView] makeTextSmaller:self];
            }
            break;
        case NSWebKitView_dispose:
            if(javaOwner != NULL){
                env->DeleteGlobalRef(javaOwner);
                javaOwner = NULL;
            }
            break;
        case NSWebKitView_ordercallback:
            if(message == NULL || env->IsInstanceOf(message,env->FindClass(JRISigClass("java/lang/Runnable")))){
                if(javaOwner == NULL || [self JVM] == NULL) break;
                jclass clazz = env->GetObjectClass(javaOwner);
                if(clazz != NULL){
                    jmethodID mID = env->GetMethodID(clazz,"callback",JRISigMethod(JRISigClass("java/lang/Runnable")) JRISigVoid); 
                    if(mID != NULL){
                        env->CallVoidMethod(javaOwner,mID,message);
                    }else{
                        if (debug)
                            fprintf(stderr,"callback mID == null\n");
                    }
                }
            }
            break;
        case NSWebKitView_search:
            {
                if (debug)
                    fprintf(stderr,"NSWebKitView_search\n");
                if(!env->IsInstanceOf(message,env->FindClass(JRISigArray(JRISigClass("java/lang/Object"))))) break;
                jobjectArray objs = (jobjectArray)message;
                int objs_length = env->GetArrayLength(objs);
                if(objs_length != 2) break;
                jobject searchForString = env->GetObjectArrayElement(objs,0);
                if(!env->IsInstanceOf(searchForString,env->FindClass(JRISigClass("java/lang/String")))) break;
                jobject booleanArray = env->GetObjectArrayElement(objs,1);
                if(!env->IsInstanceOf(booleanArray,env->FindClass(JRISigArray(JRISigBoolean)))) break;
                int booleanLength = env->GetArrayLength((jarray)booleanArray);
                if(booleanLength != 4) break;
                jboolean *booleanValues = env->GetBooleanArrayElements((jbooleanArray)booleanArray,NULL);
                if(booleanValues == NULL) break;
                const char *utf8String = env->GetStringUTFChars((jstring)searchForString,0);
                BOOL forward = (booleanValues[0] == JNI_FALSE)?FALSE:TRUE;
                BOOL caseFlag = (booleanValues[1] == JNI_FALSE)?FALSE:TRUE;
                BOOL wrap = (booleanValues[2] == JNI_FALSE)?FALSE:TRUE;
                WebFrame *webFrame = [self mainFrame];
                WebView *webView = [webFrame webView];
                NSString *nsString = [NSString stringWithUTF8String : utf8String];
                BOOL retValue = [webView searchFor:nsString direction:forward caseSensitive:caseFlag wrap:wrap];
                booleanValues[3] = (retValue == TRUE)?JNI_TRUE:JNI_FALSE;
                env->SetBooleanArrayRegion((jbooleanArray)booleanArray,3,1,booleanValues);
                env->ReleaseStringUTFChars((jstring)searchForString,utf8String);
                env->ReleaseBooleanArrayElements((jbooleanArray)booleanArray,booleanValues,0);
            }
            break;
    	case NSWebKitView_storeJavaObject:
		[self setJavaOwner:env->NewGlobalRef(message)];
    		break;
        case NSWebKitView_loadURLFromString:
    	    if(message != nil && env != nil){
                if(!env->IsInstanceOf(message,env->FindClass(JRISigArray(JRISigClass("java/lang/String"))))) break;
                int objs_length = env->GetArrayLength((jobjectArray)message);
                if(objs_length != 2) break;
                jstring jHTML = (jstring)env->GetObjectArrayElement((jobjectArray)message,0);
                if(jHTML == NULL) return;
                jstring jBaseURL = (jstring)env->GetObjectArrayElement((jobjectArray)message,1);
                const char *cHTML = env->GetStringUTFChars(jHTML,0);
                const char *cBaseURL = (jBaseURL != NULL)?env->GetStringUTFChars(jBaseURL,0):NULL;
                NSString *nsHTML = [NSString stringWithUTF8String : cHTML];
                NSURL *nsBaseURL = NULL;
                if(cBaseURL != NULL){
                    nsBaseURL = [NSURL URLWithString : [NSString stringWithUTF8String : cBaseURL]];
                }
                WebFrame *webFrame = [self mainFrame];
                [webFrame loadHTMLString : nsHTML baseURL : nsBaseURL];

                //fprintf(stderr,"NSWebKitView_loadURLFromString\n");
                env->ReleaseStringUTFChars(jHTML,cHTML);
                if(jBaseURL != NULL) env->ReleaseStringUTFChars(jBaseURL,cBaseURL);
           }
            break;
    	case NSWebKitView_loadURL:
    	    if(message != nil && env != nil){
                jboolean isCopy;
                const char *urlString = env->GetStringUTFChars((jstring)message,&isCopy);
                NSURL *url = [NSURL URLWithString : [NSString stringWithCString : urlString]];
                NSURLRequest *request = [NSURLRequest requestWithURL : url];
                WebFrame *webFrame = [self mainFrame];
                [webFrame loadRequest:request];
                //fprintf(stderr,"NSWebKitView_loadURL\n");
                if(isCopy == JNI_TRUE){
                    env->ReleaseStringUTFChars((jstring)message,urlString);
                }
            }
    		break;
        case NSWebKitView_stopLoading:
            {
                WebFrame *webFrame = [self mainFrame];
                [webFrame stopLoading];
                if (debug)
                    fprintf(stderr,"NSWebKitView_stopLoading\n");
            }
            break;
        case NSWebKitView_reload:
            {
                WebFrame *webFrame = [self mainFrame];
                [webFrame reload];
                if (debug)
                    fprintf(stderr,"NSWebKitView_reload\n");
            }
            break;
        case NSWebKitView_goBack:
            {
                 WebFrame *webFrame = [self mainFrame];
                [[webFrame webView] goBack];
                if (debug)
                    fprintf(stderr,"NSWebKitView_goBack\n");
            }
            break;
        case NSWebKitView_goForward:
           {
                WebFrame *webFrame = [self mainFrame];
                [[webFrame webView] goForward];
                if (debug)
                    fprintf(stderr,"NSWebKitView_goForward\n");
            }
            break;
        case NSWebKitView_updateCursor:
            {
                
               NSWindow *window = [self window];
               // WebFrame *frame = [self mainFrame];
               // if([window firstResponder] != [[frame frameView] documentView]){
               //     fprintf(stderr,"NSWebKitView_updateCursor runMain\n");
               //    [self performSelectorOnMainThread:@selector(runMain:) withObject:self waitUntilDone:NO];
               // }
                [window invalidateCursorRectsForView : [[[self mainFrame] frameView] documentView]];
                if (debug)
                    fprintf(stderr,"NSWebKitView_updateCursor\n");

            }
            break;
        case NSWebKitView_runJS:
            {
                if(!env->IsInstanceOf(message,env->FindClass(JRISigArray(JRISigClass("java/lang/Object"))))) break;
                jobjectArray objs = (jobjectArray)message;
                int objs_length = env->GetArrayLength(objs);
                if(objs_length != 2) break;
                jobject js = env->GetObjectArrayElement(objs,0);
                if(!env->IsInstanceOf(js,env->FindClass(JRISigClass("java/lang/String")))) break;
                jobject sb = env->GetObjectArrayElement(objs,1);
                if(!env->IsInstanceOf(sb,env->FindClass(JRISigClass("java/lang/StringBuffer")))) break;
                const char *jsString = env->GetStringUTFChars((jstring)js,0);
                WebView *webView = [[self mainFrame] webView];
                NSString *str = [webView stringByEvaluatingJavaScriptFromString:[NSString stringWithCString : jsString]];
                jstring resString = env->NewStringUTF([str UTF8String]);
                jmethodID mID = env->GetMethodID(env->FindClass(JRISigClass("java/lang/StringBuffer")),"append",
                JRISigMethod(JRISigClass("java/lang/String")) JRISigClass("java/lang/StringBuffer"));
                if(mID != 0){
                    env->CallObjectMethod(sb,mID,resString);
                }
                env->DeleteLocalRef(resString);
                env->ReleaseStringUTFChars((jstring)js,jsString);
           }
            break;
        default:
        {
            if (debug)
                fprintf(stderr,"MyNSWebKitView Error : Unknown Message Received (%d)\n", (int)messageID);
        }
            
    }
}

- (void) keyDown: (NSEvent *) theEvent
{
	[super keyDown:theEvent];
	if (debug)
	   printf("keyDown\n");
}

// WebFrameLoadDelegate Methods didFinishLoadForFrame

- (void)webView:(WebView *)sender didStartProvisionalLoadForFrame:(WebFrame *)frame
{
    if (debug)
        NSLog(@"didStartProvisionalLoadForFrame sender %@ frame %@",sender,frame);
    if (frame == [sender mainFrame]){
        //NSLog(@"didStartProvisionalLoadForFrame inside %@",[[[[frame provisionalDataSource] request] URL] absoluteString]);
        [self startURLLoading : [[[[frame provisionalDataSource] request] URL] absoluteString]];
       // [self performSelectorOnMainThread:@selector(runMain:) withObject:self waitUntilDone:NO];
        [self runMain : self];
    }
}

- (void)webView:(WebView *)sender didReceiveTitle:(NSString *)title forFrame:(WebFrame *)frame
{
    if (frame == [sender mainFrame]){
        [self setURLTitle:title];
    }
}

- (void)webView:(WebView *)sender didReceiveIcon:(NSImage *)image forFrame:(WebFrame *)frame
{
    if(javaOwner == NULL || [self JVM] == NULL) return;
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env != NULL){
    	jclass clazz = env->GetObjectClass(javaOwner);
        jmethodID mID = 0;
    	if(clazz != NULL){
            mID = env->GetMethodID(clazz,"setURLIcon",JRISigMethod(JRISigClass("java/nio/ByteBuffer")) JRISigVoid); 
    	}
    	if(mID == 0){
	        if(wasAttached) [self JVM]->DetachCurrentThread();
    	    return;
        }
        if (frame == [sender mainFrame]){
            NSBitmapImageRep    *bmRep = NULL;
            NSArray             *repr  = NULL;
            const void          *data  = NULL;
            int dataLength             = 0;
            if(image != NULL){
                repr = [image representations];
            }
            if(repr != NULL){
                for(int i = 0; i < [repr count]; i++){
                    NSImageRep *imRep = (NSImageRep *)[repr objectAtIndex:i];
                    if([imRep isKindOfClass : [NSBitmapImageRep class]]){
                        bmRep = (NSBitmapImageRep *)imRep;
                        break;
                    }
                }
            }
            if(bmRep != NULL){
                NSData *pngData = [bmRep representationUsingType : NSPNGFileType properties : NULL];
                if(pngData != NULL){
                    dataLength = [pngData length];
                    data = [pngData bytes];
                }
            }
            jobject directBuffer = NULL;
            if(data != NULL && dataLength > 0){
                directBuffer = env->NewDirectByteBuffer((void *)data,(jlong)dataLength);
            }
	        env->CallVoidMethod(javaOwner,mID,directBuffer);
            if(directBuffer != NULL) env->DeleteLocalRef(directBuffer);
        }
        if(wasAttached) [self JVM]->DetachCurrentThread();
    }
}

- (void)webView:(WebView *)sender didFinishLoadForFrame:(WebFrame *)frame
{
    if(javaOwner == NULL || [self JVM] == NULL) return;
    if (frame == [sender mainFrame]){
        jboolean goBackEnabled = ([sender canGoBack] == FALSE)?JNI_FALSE:JNI_TRUE;
        jboolean goForwardEnabled = ([sender canGoForward] == FALSE)?JNI_FALSE:JNI_TRUE;
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env == NULL) return;
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL){
	    jmethodID mID = env->GetMethodID(clazz,"setBackButtonEnable",JRISigMethod(JRISigBoolean) JRISigVoid); 
	    if(mID != NULL){
	        env->CallVoidMethod(javaOwner,mID,goBackEnabled);
	    }
	}
        clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL){
	    jmethodID mID = env->GetMethodID(clazz,"setForwardButtonEnable",JRISigMethod(JRISigBoolean) JRISigVoid); 
	    if(mID != NULL){
	        env->CallVoidMethod(javaOwner,mID,goForwardEnabled);
	    }
	}
	if(wasAttached) [self JVM]->DetachCurrentThread();
        [self finishURLLoading];
        //NSLog(@"finishURLLoading %@",self);
   }
}

//policy delegate
- (void)webView:(WebView *)sender decidePolicyForNewWindowAction:(NSDictionary *)actionInformation request:(NSURLRequest *)request newFrameName:(NSString *)frameName decisionListener:(id<WebPolicyDecisionListener>)listener 
{
	NSString *path = [[request URL] absoluteString];
	NSLog(path);
	
	jboolean shouldOpenLink = true;
	
	if(javaOwner == NULL || [self JVM] == NULL) 
		return;
	
	bool wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM], wasAttached);
	
	if(env == NULL) 
		return;
		
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL) {
	    jmethodID mID = env->GetMethodID(clazz, "shouldOpenLink", "(Ljava/lang/String;Z)Z"); 
	    if(mID != NULL) {
	        jstring jstr1 = env->NewStringUTF([path UTF8String]);
	        shouldOpenLink = env->CallBooleanMethod(javaOwner, mID, jstr1, JNI_TRUE);
			env->DeleteLocalRef(jstr1);
	    }
	}
	
	if(wasAttached) 
		[self JVM]->DetachCurrentThread();
	
	if (shouldOpenLink) {
     CreatingViewPolicy viewPolicy = [self getCreatingNewWebViewInWindowPolicy];
     if(viewPolicy == WebViewPolicyNone){
         [listener ignore];
         return;
     }
     if(viewPolicy != WebViewPolicyInNewWindow){
         [listener use];
         return;
     }
     [sender webView:sender createWebViewWithRequest:request];
	} else {
		[listener ignore];
	}
}

 - (void)webView:(WebView *)sender decidePolicyForNavigationAction:(NSDictionary *)actionInformation request:(NSURLRequest *)request frame:(WebFrame *)frame decisionListener:(id<WebPolicyDecisionListener>)listener
{
   if (debug)
      NSLog(@"\n!!!!!!!!!!!!!!!!!!!!!!!!!!!-----\nwebView:%@ decidePolicyForNavigationAction:request:%@ actionInformation %@\n-----\n", sender, request,actionInformation);	
	NSString *path = [[request URL] absoluteString];
	NSLog(path);	
	jboolean shouldOpenLink = true;	
	if(javaOwner == NULL || [self JVM] == NULL) 
            return;	
	bool wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM], wasAttached);	
	if(env == NULL) 
             return;		
	jclass clazz = env->GetObjectClass(javaOwner);
	if(clazz != NULL) {
	    jmethodID mID = env->GetMethodID(clazz, "shouldOpenLink", "(Ljava/lang/String;Z)Z"); 
	    if(mID != NULL) {
	        jstring jstr1 = env->NewStringUTF([path UTF8String]);
	        shouldOpenLink = env->CallBooleanMethod(javaOwner, mID, jstr1, JNI_FALSE);
			env->DeleteLocalRef(jstr1);
         }
     }
	
	if(wasAttached)
		[self JVM]->DetachCurrentThread();
	
	if (shouldOpenLink) {
     [listener use];
	} else {
		[listener ignore];
	}
}
 


// WebUIDelegate Methods

- (BOOL)webViewIsResizable:(WebView *)sender
{
    if (debug)
        NSLog(@"<<<<<<<<<<<<<<<<<<<<<<<<webViewIsResizable>>>>>>>>>>>>>>>>>>>>>>>>>>>>");	
    return FALSE;
}

- (void)webView:(WebView *)sender setFrame:(NSRect)frame
{
    if (debug)
        NSLog(@"!!!!!!!!!!!! setFrame %g %g %g %g ",frame.origin.x,frame.origin.y,frame.size.width,frame.size.height);	
}

- (void)webView:(WebView *)sender makeFirstResponder:(NSResponder *)responder
{
    if ( (responder != nil) && ![responder isKindOfClass : [NSTextField class]] &&
         ![responder isKindOfClass : [NSTextView class]] ) {
        responder = [self window];
    }
    [[self window] makeFirstResponder : responder];
    
/*
 if(![responder isKindOfClass : [NSTextField class]]){
        responder = [self window];
    }
    [[self window] makeFirstResponder : responder];
 */
    //NSLog(@"makeFirstResponder %@\n",responder);
}

- (void)webView:(WebView *)sender mouseDidMoveOverElement:(NSDictionary *)elementInformation modifierFlags:(unsigned int)modifierFlags
{
}

- (void)webViewFocus:(WebView *)sender
{
    if (debug)
        NSLog(@"webViewFocus\n");
}

- (void)webViewShow:(WebView *)sender
{
    if (debug)
        NSLog(@"webViewShow sender %@\n",sender);
    NSWindow *window = [sender window];
    [window makeKeyAndOrderFront:NULL];
}

- (WebView *)webView:(WebView *)sender createWebViewWithRequest:(NSURLRequest *)request
 {
    CreatingViewPolicy viewPolicy = [self getCreatingNewWebViewInWindowPolicy];
    if(viewPolicy == WebViewPolicyNone)         return NULL;
    if(viewPolicy != WebViewPolicyInNewWindow)  return self;
    
    //return NULL;
    WebView *newView = NULL;
    //NSLog(@"createWebViewWithRequest %@ sender %@\n",request,sender);
    if(request == NULL || javaOwner == NULL || [self JVM] == NULL) return newView;
    bool 	wasAttached = false;
    JNIEnv *env = GetJEnv([self JVM],wasAttached);
    if(env != NULL){
    	jclass clazz = env->GetObjectClass(javaOwner);
        jmethodID mID = 0;
    	if(clazz != NULL){
            mID = env->GetMethodID(clazz,"createNewWebKit",JRISigMethod() JRISigInt); 
    	}
    	if(mID == 0){
            if(wasAttached) [self JVM]->DetachCurrentThread();
    	    return newView;
        }
        WebView *newView = (WebView *)env->CallIntMethod(javaOwner,mID);
        //NSLog(@"createWebViewWithRequest newView %d\n",(int)newView);
        if(newView == NULL){
            if(wasAttached) [self JVM]->DetachCurrentThread();
    	    return newView;
        }
        if(newView != NULL){
            WebFrame *webFrame = [newView mainFrame];
            //[[newView window] orderOut : NULL];
            //NSLog(@"createWebViewWithRequest webFrame (!!!) %@ view %@",webFrame,newView);
            [newView setResourceLoadDelegate:newView];
            [newView setFrameLoadDelegate:newView];
            [newView setUIDelegate:newView];
            [newView setPolicyDelegate:newView];
            [webFrame loadRequest:request];
            //[webFrame performSelectorOnMainThread:@selector(loadRequest:) withObject:request waitUntilDone:NO];
        }
	if(wasAttached) [self JVM]->DetachCurrentThread();
    }
    
    
    return newView;
 }
- (void)webView:(WebView *)sender runJavaScriptAlertPanelWithMessage:(NSString *)message
{
    BOOL doAlert = TRUE;
    if(javaOwner != NULL && [self JVM] != NULL){
	bool 	wasAttached = false;
	JNIEnv *env = GetJEnv([self JVM],wasAttached);
	if(env == NULL) return;
	jclass ownerClazz = env->GetObjectClass(javaOwner);
        if(ownerClazz == NULL){
            if(wasAttached) [self JVM]->DetachCurrentThread();
            return;
        }
        jmethodID mID = env->GetMethodID(ownerClazz,"runJavaScriptAlertPanelWithMessage",JRISigMethod(JRISigClass("java/lang/String")) JRISigBoolean); 
        if(mID == NULL){
            if(wasAttached) [self JVM]->DetachCurrentThread();
            return;
        }
        jstring jstr1 = (message == NULL)?NULL:env->NewStringUTF([message UTF8String]);
        doAlert = env->CallBooleanMethod(javaOwner,mID,jstr1);
        env->DeleteLocalRef(jstr1);
	if(wasAttached) [self JVM]->DetachCurrentThread();
    }
    if(doAlert){
        NSRunInformationalAlertPanel(@"JavaScript",message,@"OK",nil,nil);
/* this will worlk only for 10.3 and above
        NSAlert *alert = [[NSAlert alloc] init];
        [[alert window] setTitle : @"JS Alert"];
        [alert addButtonWithTitle:@"OK"];
        [alert setMessageText:@"JavaScript"];
        [alert setInformativeText:message];
        [alert setAlertStyle:NSInformationalAlertStyle];
        [alert runModal];
        [alert release];
*/
    }
}
- (BOOL)webView:(WebView *)sender runJavaScriptConfirmPanelWithMessage:(NSString *)message
{
    if (debug)
        NSLog(@"runJavaScriptConfirmPanelWithMessage\n");
    return FALSE;
}

- (NSString *)webView:(WebView *)sender runJavaScriptTextInputPanelWithPrompt:(NSString *)prompt defaultText:(NSString *)defaultText
{
    if (debug)
        NSLog(@"runJavaScriptTextInputPanelWithPrompt\n");
    return NULL;
}


// WebResourceLoadDelegate Methods


-(id)webView:(WebView *)sender identifierForInitialRequest:(NSURLRequest *)request fromDataSource:(WebDataSource *)dataSource
{
    if (debug)
        NSLog(@"identifierForInitialRequest %@ from data source %@\n",request,dataSource);
    return self;
}

-(void)webView:(WebView *)sender resource:(id)identifier didReceiveContentLength:(unsigned)length fromDataSource:(WebDataSource *)dataSource
{
    if (debug)
        NSLog(@"didReceiveContentLength %d from data source %@\n",length,dataSource);
}


-(void)webView:(WebView *)sender resource:(id)identifier didFinishLoadingFromDataSource:(WebDataSource *)dataSource
{
    //fprintf(stderr,"didFinishLoadingFromDataSource \n");
   // NSURLRequest *initialRequest = [dataSource request];
   // NSString *docSource = [[dataSource representation] documentSource];
   // NSString *absoluteString = [[initialRequest URL] absoluteString];
   // NSLog(@"dataSource %@",absoluteString);
}

-(void)webView:(WebView *)sender resource:(id)identifier didFailLoadingWithError:(NSError *)error fromDataSource:(WebDataSource *)dataSource
{
    // Increment the failed count and update the status message
    NSDictionary *userInfo = [error userInfo];
    if(userInfo != nil){
        id errorURLString = [userInfo objectForKey : NSErrorFailingURLStringKey];
        id localDesc = [userInfo objectForKey : NSLocalizedDescriptionKey];
        
        if([localDesc isKindOfClass : [NSString class]]){
            if (debug)
                fprintf(stderr,"localDesc %s\n",[(NSString *)localDesc cString]);
        }
        
        if([errorURLString isKindOfClass : [NSString class]]){
            if (debug)
                fprintf(stderr,"errorURLString %s\n",[(NSString *)errorURLString cString]);
        }
    
        //NSString *errorDesc = [error localizedDescription];
        [self errorOccurred : (NSString *)localDesc host : (NSString *)errorURLString];
    }

}


//setDownloadDelegate
- (void)downloadDidBegin:(NSURLDownload *)download
{
    //[self setDownloading:YES];
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!!!! downloadDidBegin download %@",download);
 //   [myDownload setDestination:@"/Users/dima/testwebkit.sit" allowOverwrite:YES];
}


 - (void)download:(NSURLDownload *)theDownload decideDestinationWithSuggestedFilename:(NSString *)filename
{
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!!!! decideDestinationWithSuggestedFilename %@",filename);
    [theDownload setDestination:@"/Users/dima/webkitdownloadtest/testwebkit.sit" allowOverwrite:YES];
   // [theDownload setDestination:@"testwebkit.sit" allowOverwrite:YES];
// [[NSSavePanel savePanel] beginSheetForDirectory:NSHomeDirectory()
//                                               file:filename
//                                     modalForWindow:[self window]
//                                      modalDelegate:self
//                                     didEndSelector:@selector(savePanelDidEnd:returnCode:contextInfo:)
//                                        contextInfo:nil];

}


- (void)download:(NSURLDownload *)download didCreateDestination:(NSString *)path
{
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!!!! didCreateDestination path %@",path);
}

- (void)download:(NSURLDownload *)theDownload didReceiveResponse:(NSURLResponse *)response
{
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!!!! didReceiveResponse path %@",response);
}

- (void)download:(NSURLDownload *)theDownload didReceiveDataOfLength:(unsigned)length
{
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!!!! didReceiveDataOfLength  %d",length);
}

- (BOOL)download:(NSURLDownload *)download shouldDecodeSourceDataOfMIMEType:(NSString *)encodingType;
{
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!!!! shouldDecodeSourceDataOfMIMEType  %d",encodingType);
    return FALSE;
}

- (void)downloadDidFinish:(NSURLDownload *)theDownload
{
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!! Download SUCCESS");
}


- (void)download:(NSURLDownload *)theDownload didFailWithError:(NSError *)error
{    
    if (debug)
        NSLog(@"!!!!!!!!!!!!!!!! Download Failed error %@", error);
}


@end


void setObjectStringField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,NSString *value){
    if(env == NULL || clazz == NULL || methodName == NULL) return;
    jstring jstr1 = (value == NULL)?NULL:env->NewStringUTF([value UTF8String]);
    jfieldID fID = env->GetFieldID(clazz,methodName,JRISigClass("java/lang/String"));
    if(fID != 0) env->SetObjectField(object,fID,jstr1);
}
        
void setObjectBooleanField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,jboolean value){
    if(env == NULL || clazz == NULL || methodName == NULL) return;
    jfieldID fID = env->GetFieldID(clazz,methodName,JRISigBoolean);
    if(fID != 0) env->SetBooleanField(object,fID,value);
}
        
void setObjectIntField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,jint value){
    if(env == NULL || clazz == NULL || methodName == NULL) return;
    jfieldID fID = env->GetFieldID(clazz,methodName,JRISigInt);
    if(fID != 0) env->SetIntField(object,fID,value);
}
        
void setObjectRectField(JNIEnv *env,jobject object,jclass clazz,const char *methodName,NSRect rect){
    if(env == NULL || clazz == NULL || methodName == NULL) return;
    jfieldID fID = env->GetFieldID(clazz,methodName,JRISigClass("java/awt/Rectangle"));
    if(fID == 0) return;
    NSPoint point = rect.origin;
    NSSize  size = rect.size;
    jint x = (jint)point.x;
    jint y = (jint)point.y;
    jint w = (jint)size.width;
    jint h = (jint)size.height;
    jclass rectClazz = env->FindClass(JRISigClass("java/awt/Rectangle"));
    if(rectClazz == NULL) return;
    jmethodID cID = env->GetMethodID(rectClazz,"<init>",JRISigMethod(JRISigInt JRISigInt JRISigInt JRISigInt) JRISigVoid); 
    if(cID == NULL) return;
    jobject jrect = env->NewObject(rectClazz,cID,x,y,w,h);
    if(jrect == NULL) return;
    env->SetObjectField(object,fID,jrect);
}


JNIEnv *GetJEnv(JavaVM *vm,bool &wasAttached){
	JNIEnv *env = NULL;
	if(vm == NULL) return env;
	wasAttached = false;

	jint errGetEnv = vm->GetEnv((void **)&env, JNI_VERSION_1_4);
	if(errGetEnv == JNI_ERR) return NULL;
	if(errGetEnv == JNI_EDETACHED){
		vm->AttachCurrentThread((void **)&env,(void *)NULL);
		if(env != NULL) wasAttached = true;
	}else if(errGetEnv != JNI_OK) return NULL;
	return env;
}
/*
 WebFrame's 
 [dataSource [representation documentSource]]
  [dataSource [representation canProvideDocumentSource]]
 */
