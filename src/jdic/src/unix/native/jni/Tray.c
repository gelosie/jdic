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

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xos.h>
#include <X11/Xatom.h>

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "GnomeSystemTrayService.h"
#include "GnomeTrayAppletService.h"

#include <X11/Intrinsic.h>
#include <X11/StringDefs.h>
#include <X11/Vendor.h>
#include <X11/Shell.h>
#include <X11/IntrinsicP.h>

#include <sys/types.h>
#include <sys/param.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dlfcn.h>

#include <poll.h>
#ifndef POLLRDNORM
#define POLLRDNORM POLLIN
#endif

#ifdef DEBUG
#define dprintf printf
#else
#define dprintf     
#endif

Display *     awt_display;

/* AWT interface functions. */
static void *res = NULL;
static int initialized_lock = 0;


/* AWT interface functions. */
static void (* LockIt)(JNIEnv *) = NULL;
static void (* UnLockIt)(JNIEnv *) = NULL;
static void (* NoFlushUnlockIt)(JNIEnv *) = NULL;

static void *awtHandle = NULL;

#ifdef __linux__
#define LIBARCH  "i386"
#else
#ifdef __i386
#define LIBARCH "i386"
#endif
#ifdef __sparc
#define LIBARCH "sparc"
#endif
#ifdef __sparcv9
#define LIBARCH "sparcv9"
#endif
#endif

static void initAwtHandle() {
    char awtPath[MAXPATHLEN];

    sprintf(awtPath,"%s/lib/%s/libawt.so",getenv("JAVA_HOME"),LIBARCH);
    dprintf("%s\n",awtPath);
    awtHandle = dlopen(awtPath, RTLD_LAZY);
    if (awtHandle == NULL) {
        /* must be JDK try JDK location */
        sprintf(awtPath,"%s/jre/lib/%s/libawt.so",getenv("JAVA_HOME"),LIBARCH);
        dprintf("JDK - %s\n",awtPath);
        awtHandle = dlopen(awtPath, RTLD_LAZY);

    }

    if (awtHandle == NULL) {
        fprintf(stderr,"reflect - bad awtHandle.\n");
        fprintf(stderr,"%s\n",dlerror());
        exit(123);
    }
    return;
}


#define REFLECT_VOID_FUNCTION(name, arglist, paramlist)			\
typedef name##_type arglist;						\
    static void name arglist						\
{									\
    static name##_type *name##_ptr = NULL;				\
        if (name##_ptr == NULL) {						\
            if (awtHandle == NULL) {					\
                initAwtHandle();						\
            }								\
            name##_ptr = (name##_type *) dlsym(awtHandle, #name);		\
                if (name##_ptr == NULL) {					\
                    fprintf(stderr,"reflect failed to find " #name ".\n");	\
                        exit(123);							\
                        return;							\
                }								\
        }									\
    (*name##_ptr)paramlist;						\
}

#define REFLECT_FUNCTION(return_type, name, arglist, paramlist)		\
typedef return_type name##_type arglist;				\
    static return_type name arglist						\
{									\
    static name##_type *name##_ptr = NULL;				\
        if (name##_ptr == NULL) {						\
            if (awtHandle == NULL) {					\
                initAwtHandle();						\
            }								\
            name##_ptr = (name##_type *) dlsym(awtHandle, #name);		\
                if (name##_ptr == NULL) {					\
                    fprintf(stderr,"reflect failed to find " #name ".\n");	\
                        exit(123);							\
                        return;							\
                }								\
        }									\
    return (*name##_ptr)paramlist;					\
}

REFLECT_VOID_FUNCTION(getAwtLockFunctions,
        (void (**AwtLock)(JNIEnv *), void (**AwtUnlock)(JNIEnv *),
         void (**AwtNoFlushUnlock)(JNIEnv *), void *reserved), 
        (AwtLock, AwtUnlock, AwtNoFlushUnlock, reserved))


    REFLECT_VOID_FUNCTION(getAwtData,
            (int *awt_depth, Colormap *awt_cmap, Visual **awt_visual,
             int *awt_num_colors, void *pReserved),
            (awt_depth, awt_cmap, awt_visual,
             awt_num_colors, pReserved))

REFLECT_FUNCTION(Display *, getAwtDisplay, (void), ())



    /*  Global variables  */

    Display *     display;
    int           screen_num;
    static char * appname;

    Atom net_system_tray=0;
    Atom _NET_WM_ICON=0;
    Window tray_owner=0;
    Atom embed_type;
    Atom embed_info;

#define SYSTEM_TRAY_REQUEST_DOCK    0
#define SYSTEM_TRAY_BEGIN_MESSAGE   1
#define SYSTEM_TRAY_CANCEL_MESSAGE  2


static int trapped_error_code = 0;
static int (*old_error_handler) (Display *, XErrorEvent *);

static int error_handler(Display  *display, XErrorEvent *error)
{
    trapped_error_code = error->error_code;
    return 0;
}

void trap_errors(void)
{
    trapped_error_code = 0;
    old_error_handler = XSetErrorHandler(error_handler);
}

int untrap_errors(void)
{
    XSetErrorHandler(old_error_handler);
    return trapped_error_code;
}


void send_message( Display* dpy, Window w,    long message, long data1 , long data2 , long data3 )
{
    XEvent ev;

    memset (&ev, 0, sizeof (ev));
    ev.xclient.type = ClientMessage;
    ev.xclient.window = w;
    ev.xclient.message_type =
        XInternAtom (dpy, "_NET_SYSTEM_TRAY_OPCODE", False);
    ev.xclient.format = 32;
    ev.xclient.data.l[0] = CurrentTime;
    ev.xclient.data.l[1] = message;
    ev.xclient.data.l[2] = data1;
    ev.xclient.data.l[3] = data2;
    ev.xclient.data.l[4] = data3;

    trap_errors ();
    XSendEvent (dpy, w, False, NoEventMask, &ev);
    XSync (dpy, False);
    if (untrap_errors ())
    {
        /* Handle failure */
    }
}

void ThreadYield(JNIEnv *env) {

    static jclass threadClass = NULL;
    static jmethodID yieldMethodID = NULL;

    /* Initialize our java identifiers once. Checking before locking 
     * is a huge performance win.
     */
    if (threadClass == NULL) {
        /* should enter a monitor here... */
        Boolean err = FALSE;
        if (threadClass == NULL) {
            jclass tc = (*env)->FindClass(env, "java/lang/Thread");
            threadClass = (*env)->NewGlobalRef(env, tc);
            (*env)->DeleteLocalRef(env, tc);
            if (threadClass != NULL) {
                yieldMethodID = (*env)->GetStaticMethodID(env,
                        threadClass,
                        "yield",
                        "()V"
                        );
            }
        }
        if (yieldMethodID == NULL) {
            threadClass = NULL;
            err = TRUE;
        }
        if (err) {
            return;
        }
    } /* threadClass == NULL*/

    (*env)->CallStaticVoidMethod(env, threadClass, yieldMethodID);
} 


void
configureNotify(JNIEnv *env,Window window,int x, int y, int w, int h) {

    static jclass gtaClass = NULL;
    static jmethodID confMethodID = NULL;

    if (gtaClass == NULL) {
        Boolean err = FALSE;
        if (gtaClass == NULL) {
            jclass tc = (*env)->FindClass(env, "org/jdesktop/jdic/tray/internal/impl/GnomeTrayAppletService");
            gtaClass = (*env)->NewGlobalRef(env, tc);
            (*env)->DeleteLocalRef(env, tc);
            if (gtaClass != NULL) {
                confMethodID = (*env)->GetStaticMethodID(env,
                        gtaClass,
                        "configureNotify",
                        "(JIIII)V"
                        );
            }
            else {
                dprintf("gtaClass == null");
            }
        }
        if (confMethodID == NULL) {
            dprintf("confMethodID == null");
            gtaClass = NULL;
            err = TRUE;
        }
        if (err) {
            return;
        }
    } /* gtaClass == NULL*/

    (*env)->CallStaticVoidMethod(env, gtaClass, confMethodID,(jlong)window,x,y,w,h);
} 





/*
 * Class:     GnomeSystemTrayService
 * Method:    eventLoop
 * Signature: ()V
 */
JNIEXPORT void JNICALL  Java_org_jdesktop_jdic_tray_internal_impl_GnomeSystemTrayService_eventLoop (JNIEnv *env, jclass klass) {

    XEvent        report;
    /*  Enter event loop  */
    static struct pollfd pollFds[2];
    int timeout = 100;
    int result;
    int fdX =  ConnectionNumber(display) ;
    pollFds[0].fd = fdX;
    pollFds[0].events = POLLRDNORM;
    pollFds[0].revents = 0;

    while ( 1 ) {

        (*LockIt)(env);
        while ((XEventsQueued(display, QueuedAfterReading) == 0) &&
                (XEventsQueued(display, QueuedAfterFlush) == 0)) {

            XFlush(display);
            (*UnLockIt)(env);
            ThreadYield(env);       
            result = poll( pollFds, 1, (int32_t) timeout );
            (*LockIt)(env);

        }
        XNextEvent(display, &report);
        (*UnLockIt)(env);

        dprintf("type = %x\n",report.type);
        switch ( report.type ) {

            case Expose:
                dprintf("Expose\n");
                break;
            case PropertyNotify:
                dprintf("PropertyNotify\n");
                break;
            case ClientMessage: 
                dprintf("ClientMessage\n");
                break;
            case ReparentNotify:
                dprintf("ReparentNotify\n");
                dprintf("window = %x parent = %x\n", report.xreparent.window,report.xreparent.parent);

                break;
            case ConfigureNotify:
                dprintf("ConfigureNotify x = %s y=%d w=%d h=%d\n",report.xconfigure.x,report.xconfigure.y, report.xconfigure.width, report.xconfigure.height);

                configureNotify(env,report.xconfigure.window,report.xconfigure.x,report.xconfigure.y, report.xconfigure.width, report.xconfigure.height);

                break;
            case ButtonPress:
                dprintf("ButtonPress\n");
                break;




        }
    }

}




/*
 * Class:     GnomeSystemTrayService
 * Method:    locateSystemTray
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_tray_internal_impl_GnomeSystemTrayService_locateSystemTray (JNIEnv *env, jclass klass) {


    if (initialized_lock == 0) {
        getAwtLockFunctions(&LockIt, &UnLockIt, &NoFlushUnlockIt, NULL);
        initialized_lock = 1 ;
    }


    (*LockIt)(env);

    /*  Connect to X server  */
    if ( (display = XOpenDisplay(NULL)) == NULL ) {
        fprintf(stderr, "Couldn't connect to X server !\n");
        return JNI_FALSE;
    }


    /*  Get screen size from display structure macro  */

    screen_num     = DefaultScreen(display);

    net_system_tray = XInternAtom(display,"_NET_SYSTEM_TRAY_S0",False);
    embed_type = XInternAtom(display,"_XEMBED_INFO",False);

    _NET_WM_ICON = XInternAtom(display,"_NET_WM_ICON", False);


    tray_owner = XGetSelectionOwner(display,net_system_tray);

    dprintf("Tray Owner = %x \n",tray_owner);


    (*UnLockIt)(env);	

    return JNI_TRUE;
}


/*
 * Class:     GnomeTrayAppletService
 * Method:    asjustSizeHints
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_GnomeTrayAppletService_adjustSizeHints (JNIEnv *env, jobject obj, jlong win, jint width, jint height) {

    XSizeHints *  size_hints;

    (*LockIt)(env);	
    
    if (!(size_hints  = XAllocSizeHints())) {
        fprintf(stderr, "Couldn't allocate memory.\n");
        (*UnLockIt)(env);	
        return;
    }

    size_hints->flags       = PSize | PMinSize;
    size_hints->min_width   = width;
    size_hints->min_height  = height;


    XSetWMProperties(display, win, NULL, NULL, NULL, 0,
            size_hints, NULL, NULL);

    (*UnLockIt)(env);	

}

/*
 * Class:     GnomeTrayAppletService
 * Method:    createIconWindow
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_tray_internal_impl_GnomeTrayAppletService_createAppletWindow (JNIEnv *env, jobject obj) {

    Window win;
    XSizeHints *  size_hints;
    XWMHints   *  wm_hints;
    XClassHint *  class_hints;
    XTextProperty windowName, iconName;

    char *       window_name = "JDIC Tray Icon";
    char *       icon_name   = "JDIC Tray Icon";

    unsigned int *data =  (unsigned int *) malloc(6*4);

    (*LockIt)(env);	
    /*  Allocate memory for our structures  */

    if ( !( size_hints  = XAllocSizeHints() ) || 
            !( wm_hints    = XAllocWMHints()   ) ||
            !( class_hints = XAllocClassHint() )    ) {
        fprintf(stderr, "Couldn't allocate memory.\n");
        (*UnLockIt)(env);	
        return 0;
    }

    win = XCreateWindow(display,RootWindow(display,screen_num),
            0,0,10,10,1,
            CopyFromParent, 
            CopyFromParent,
            CopyFromParent, 
            0,             
            0);   

    /*  Set hints for window manager before mapping window  */

    if ( XStringListToTextProperty(&window_name, 1, &windowName) == 0 ) {
        fprintf(stderr, "%s: structure allocation for windowName failed.\n",
                appname);
        (*UnLockIt)(env);	
        return 0;
    }

    if ( XStringListToTextProperty(&icon_name, 1, &iconName) == 0 ) {
        fprintf(stderr, "%s: structure allocation for iconName failed.\n",
                appname);
        (*UnLockIt)(env);	
        return 0;
    }

    size_hints->flags       = PPosition | PSize | PMinSize;
    size_hints->min_width   = 1;
    size_hints->min_height  = 1;

    wm_hints->flags         = StateHint | InputHint;
    wm_hints->initial_state = NormalState;
    wm_hints->input         = True;

    class_hints->res_name   = "JDIC Tray Icon";
    class_hints->res_class  = "JDIC Tray Icon";

    XSetWMProperties(display, win, &windowName, &iconName, NULL, 0,
            size_hints, wm_hints, class_hints);



    data[0]=2;  /* width */
    data[1]=2;  /* height */
    data[2]=0x00ff0000;
    data[3]=0x00ff0000;
    data[4]=0x00ff0000;
    data[5]=0x00ff0000;


    XChangeProperty(display,win,_NET_WM_ICON,XA_CARDINAL,32,PropModeReplace,(const unsigned char *) data,6);

    /*    XMapWindow(display,win); */

    XSync(display,False);

    /*  Choose which events we want to handle  */


    XSelectInput(display, win, ExposureMask | KeyPressMask |
            ButtonPressMask | StructureNotifyMask);

    dprintf("Window ID = %x \n",win);

    (*UnLockIt)(env);	

    return (jlong) win;
}



/*
 * Class:     GnomeSystemTrayService
 * Method:    dockWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL  Java_org_jdesktop_jdic_tray_internal_impl_GnomeSystemTrayService_dockWindow (JNIEnv *env, jclass klass, jlong win) {

    int data[2];
    data[0] = 0;
    data[1] = (1<<0);

    (*LockIt)(env);	
    XChangeProperty(display,win,embed_type,embed_type,32,PropModeReplace,(const unsigned char *)data,2);
    send_message(display,tray_owner,SYSTEM_TRAY_REQUEST_DOCK,win,0,0);
    XSync(display,False);

    (*UnLockIt)(env);	
}

/* Event Handler to correct for Shell position */
    static void
checkPos(Widget w, XtPointer data, XEvent *event)
{
    /* this is heinous, but necessary as we need to update
     ** the X,Y position of the shell if netscape has moved.
     ** we have to do this so that XtTranslateCoords used by
     ** popups and the like get the proper screen positions
     ** Additionally we can use XtSet/ XtMove/ConfigureWidget
     ** As the widget code will think the the shell has moved
     ** and generate a XConfigure which WILL move the window
     ** We are only trying to correct for the reparent hack.
     ** sigh.
     */

    w->core.x = event->xcrossing.x_root - event->xcrossing.x;
    w->core.y = event->xcrossing.y_root - event->xcrossing.y;
}
/* Event Handler to correct for Shell position */
    static void
propertyHandler(Widget w, XtPointer data, XEvent *event)
{
    /* this is heinous, but necessary as we need to update
     ** the X,Y position of the shell is changed to wrong value.
     ** we have to do this so that XtTranslateCoords used by
     ** popups and the like get the proper screen positions
     ** Additionally we can use XtSet/ XtMove/ConfigureWidget
     ** 
     */
    int px, py;
    Window dummy;

    XTranslateCoordinates(display, XtWindow(w), DefaultRootWindow(display), 0,0, &px, &py, &dummy);

    w->core.x=px;
    w->core.y=py;

}



/*
 * Create a local managed widget inside a given X window.
 * We allocate a top-level shell and then reparent it into the
 * given window id.
 *
 * This is used to take the X11 window ID that has been passed
 * to us by our parent Navigator plugin and return a widget
 * that can be used as the base for our Java EmbeddeFrame.
 *
 * Note that the ordering of the various calls is tricky here as
 * we have to cope with the variations between 1.1.3, 1.1.6,
 * and 1.2.
 */
JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_tray_internal_impl_GnomeTrayAppletService_getWidget (JNIEnv *env, jobject jobj , jlong winid, jint width, jint height, jint x, jint y)
{
    Arg args[40];
    int argc;
    Widget w;
    Window child, parent;
    Visual *visual;
    Colormap cmap;
    int depth;
    int ncolors;
    Display **awt_display_ptr;


    dprintf("getWidget \n");
    /* Initialize access to AWT lock functions. */
    if (initialized_lock == 0) {
        getAwtLockFunctions(&LockIt, &UnLockIt, &NoFlushUnlockIt, NULL);
        initialized_lock = 1 ;
    }

    dprintf("Finished initing AWT lock funxtions , Lockit = %x\n",LockIt);

    /*
     * Create a top-level shell.  Note that we need to use the
     * AWT's own awt_display to initialize the widget.  If we
     * try to create a second X11 display connection the Java
     * runtimes get very confused.
     */

    (*LockIt)(env);

    argc = 0;
    XtSetArg(args[argc], XtNsaveUnder, False); argc++;
    XtSetArg(args[argc], XtNallowShellResize, False); argc++;

    /* the awt initialization should be done by now (awt_GraphicsEnv.c) */

    getAwtData(&depth,&cmap,&visual,&ncolors,NULL);

    awt_display_ptr = (Display **) dlsym(awtHandle, "awt_display");
    if (awt_display_ptr == NULL)
        awt_display = getAwtDisplay();
    else
        awt_display = *awt_display_ptr;

    dprintf("awt_display = %x\n",awt_display);

    XtSetArg(args[argc], XtNvisual, visual); argc++;
    XtSetArg(args[argc], XtNdepth, depth); argc++;
    XtSetArg(args[argc], XtNcolormap, cmap); argc++;

    XtSetArg(args[argc], XtNwidth, width); argc++;
    XtSetArg(args[argc], XtNheight, height); argc++;
    /* The shell has to have relative coords of O,0? */
    XtSetArg(args[argc], XtNx, 0); argc++;
    XtSetArg(args[argc], XtNy, 0); argc++;

    /* The shell widget starts out as a top level widget.
     * Without intervention, it will be managed by the window
     * manager and will be its own widow. So, until it is reparented,
     *  we don't map it. 
     */
    XtSetArg(args[argc], XtNmappedWhenManaged, False); argc++;

    w = XtAppCreateShell("AWTapp","XApplication",
            vendorShellWidgetClass,
            awt_display,
            args,
            argc);
    XtRealizeWidget(w);

    /* 
     * i think the following 2 lines wont be needed because of fix of 4419207
     * the function checkPos and propertyHandler can also be deleted
     * please let me know if testing shows otherwise
     * see awt_addEmbeddedFrame in awt_util.c
     * tao.ma@eng
     */
    XtAddEventHandler(w, EnterWindowMask, FALSE,(XtEventHandler) checkPos, 0);
    XtAddEventHandler(w, PropertyChangeMask , FALSE,(XtEventHandler) propertyHandler, 0);
    /*
     * Now reparent our new Widget into our Navigator window
     */
    parent = (Window) winid;
    child = XtWindow(w);
    XReparentWindow(awt_display, child, parent, 0, 0);
    XFlush(awt_display);
    XSync(awt_display, False); 
    XtVaSetValues(w, XtNx, 0, XtNy, 0, NULL);
    XFlush(awt_display);
    XSync(awt_display, False);

    (*UnLockIt)(env);	

    dprintf("getWidget widget = %d\n",w);

    return (jlong)w;
}


/*
 * Class:     org_jdesktop_jdic_tray_internal_impl_GnomeTrayAppletService
 * Method:    dispose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_tray_internal_impl_GnomeTrayAppletService_dispose (JNIEnv *env, jobject object, jlong window) {

    (*LockIt)(env);	
    XDestroyWindow(display,(Window) window);
    XSync(display,False);
    (*UnLockIt)(env);	

}


