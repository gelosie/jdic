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

#include "Dock.h"

#include <X11/Xlib.h>
#include <X11/Xatom.h>
#include <X11/Xutil.h>
#include <X11/Xos.h>
#include <dlfcn.h>
#include <stdio.h>
#include <sys/param.h>

#include <poll.h>
#ifndef POLLRDNORM
#define POLLRDNORM POLLIN
#endif

#define DEBUG
#ifdef DEBUG
#define dprintf printf
#else
#define dprintf
#endif

static Display *     awt_display;

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

#define REFLECT_VOID_FUNCTION(name, arglist, paramlist)                 \
typedef name##_type arglist;                                            \
    static void name arglist                                            \
{                                                                       \
    static name##_type *name##_ptr = NULL;                              \
        if (name##_ptr == NULL) {                                               \
            if (awtHandle == NULL) {                                    \
                initAwtHandle();                                                \
            }                                                           \
            name##_ptr = (name##_type *) dlsym(awtHandle, #name);               \
                if (name##_ptr == NULL) {                                       \
                    fprintf(stderr,"reflect failed to find " #name ".\n");      \
                        exit(123);                                                      \
                        return;                                                 \
                }                                                               \
        }                                                                       \
    (*name##_ptr)paramlist;                                             \
}

#define REFLECT_FUNCTION(return_type, name, arglist, paramlist)         \
typedef return_type name##_type arglist;                                \
    static return_type name arglist                                             \
{                                                                       \
    static name##_type *name##_ptr = NULL;                              \
        if (name##_ptr == NULL) {                                               \
            if (awtHandle == NULL) {                                    \
                initAwtHandle();                                                \
            }                                                           \
            name##_ptr = (name##_type *) dlsym(awtHandle, #name);               \
                if (name##_ptr == NULL) {                                       \
                    fprintf(stderr,"reflect failed to find " #name ".\n");      \
                        exit(123);                                                      \
                        return;                                                 \
                }                                                               \
        }                                                                       \
    return (*name##_ptr)paramlist;                                      \
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

static Display *     display;
static int	screen_num;
static Atom _NET_WM_STRUT;

void ThreadYield(JNIEnv *env) {

    static jclass threadClass = NULL;
    static jmethodID yieldMethodID = NULL;

    /* Initialize our java identifiers once. Checking before locking
     * is a huge performance win.
     */
    if (threadClass == NULL) {
        /* should enter a monitor here... */
        int err = 0;
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
            err = 1;
        }
        if (err) {
            return;
        }
    } /* threadClass == NULL*/

    (*env)->CallStaticVoidMethod(env, threadClass, yieldMethodID);
}

static void
configureNotify(JNIEnv *env, Window window, int x, int y, int w, int h) 
{
    static jclass gdsClass = NULL;
    static jmethodID confMethodID = NULL;

    if (gdsClass == NULL) {
        int err = 0;
        if (gdsClass == NULL) {
            jclass tc = (*env)->FindClass(env, "org/jdesktop/jdic/dock/internal/impl/UnixDockService");
            gdsClass = (*env)->NewGlobalRef(env, tc);
            (*env)->DeleteLocalRef(env, tc);
            if (gdsClass != NULL) {
                confMethodID = (*env)->GetStaticMethodID(env,
                        gdsClass,
                        "configureNotify",
                        "(JIIII)V"
                        );
            }
            else {
                dprintf("gdsClass == null");
            }
        }
        if (confMethodID == NULL) {
            dprintf("confMethodID == null");
            gdsClass = NULL;
            err = 1;
        }
        if (err) {
            return;
        }
    } /* gdsClass == NULL*/

    (*env)->CallStaticVoidMethod(env, gdsClass, confMethodID,(jlong)window,x,y,w,h);
}



/*
 * Class:     org_jdesktop_jdic_dock_internal_impl_UnixDockService
 * Method:    eventLoop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_dock_internal_impl_UnixDockService_eventLoop(JNIEnv *env, jclass klass)
{
    XEvent        report;
    /*  Enter event loop  */
    static struct pollfd pollFds[2];
    int timeout = 100;
    int result;
    int fdX =  ConnectionNumber(display) ;
    pollFds[0].fd = fdX;
    pollFds[0].events = POLLRDNORM;
    pollFds[0].revents = 0;

   dprintf("Starting event loop ...\n");

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
                dprintf("ConfigureNotify x = %d y=%d w=%d h=%d\n",report.xconfigure.x,report.xconfigure.y, report.xconfigure.width, report.xconfigure.height);

                configureNotify(env,report.xconfigure.window,report.xconfigure.x,report.xconfigure.y, report.xconfigure.width, report.xconfigure.height);

                break;
            case ButtonPress:
                dprintf("ButtonPress\n");
                break;
	    case MapRequest:
		dprintf("MapRequest\n");
		break;
	    case MapNotify:
                dprintf("MapNotify\n");
                break;
	    case UnmapNotify:
                dprintf("UnmapNotify\n");
                break;




        }
    }
}

/*
 * Class:     org_jdesktop_jdic_dock_internal_impl_UnixDockService
 * Method:    locateDock
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_dock_internal_impl_UnixDockService_locateDock(JNIEnv *env, jclass klass)
{
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

    /*  Get screen number from display structure macro  */
    screen_num     = DefaultScreen(display);

    _NET_WM_STRUT = XInternAtom(display,"_NET_WM_STRUT",False);

    (*UnLockIt)(env);

    return JNI_TRUE;
}

/*
 * Class:     org_jdesktop_jdic_dock_internal_impl_UnixDockService
 * Method:    createDockWindow
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_org_jdesktop_jdic_dock_internal_impl_UnixDockService_createDockWindow(JNIEnv *env, jobject obj)
{
    Window win;
    XSizeHints *  size_hints;
    XWMHints   *  wm_hints;
    XClassHint *  class_hints;
    XTextProperty windowName, iconName;
    Atom wm_delete_window;
    Atom _NET_WM_WINDOW_TYPE;
    Atom _NET_WM_WINDOW_TYPE_DOCK;
    Atom _NET_WM_STRUT;
    Atom _NET_WM_STATE;
    Atom _NET_WM_STATE_STICKY;
    Atom _MOTIF_WM_HINTS;
    int insets[4];
    int insets_partial[12];
    char *       window_name = "JDIC Dock";
    char *       icon_name = "JDIC Dock Icon";
    unsigned int display_width, display_height;

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

    display_width  = DisplayWidth(display, screen_num);
    display_height = DisplayHeight(display, screen_num);

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
                window_name);
        (*UnLockIt)(env);
        return 0;
    }
    if ( XStringListToTextProperty(&icon_name, 1, &iconName) == 0 ) {
        fprintf(stderr, "%s: structure allocation for iconName failed.\n",
                window_name);
        (*UnLockIt)(env);
        return 0;
    }

    size_hints->flags       = PPosition | PSize | PMinSize | PWinGravity;
    size_hints->x = 0;
    size_hints->y = 0;
    size_hints->min_width   = 10;
    size_hints->width   = 10;
    size_hints->min_height  = display_height;
    size_hints->height  = display_height;
    size_hints->win_gravity  = NorthWestGravity;

    wm_hints->flags         = StateHint | InputHint;
    wm_hints->initial_state = NormalState;
    wm_hints->input         = True;

    class_hints->res_name   = "JDIC Dock";
    class_hints->res_class  = "JDIC Dock";

    XSetWMProperties(display, win, &windowName, &iconName, NULL, 0,
            size_hints, wm_hints, class_hints);

    /*  Choose which events we want to handle  */

    XSelectInput(display, win, ExposureMask | KeyPressMask |
                 ButtonPressMask | StructureNotifyMask);


    wm_delete_window = XInternAtom(display,"WM_DELETE_WINDOW",False);
    _NET_WM_WINDOW_TYPE = XInternAtom(display,"_NET_WM_WINDOW_TYPE",False);
    _NET_WM_WINDOW_TYPE_DOCK = XInternAtom(display,"_NET_WM_WINDOW_TYPE_DOCK",False);
    _NET_WM_STATE = XInternAtom(display,"_NET_WM_STATE",False);
    _NET_WM_STATE_STICKY = XInternAtom(display,"_NET_WM_STATE_STICKY",False);
    _MOTIF_WM_HINTS=XInternAtom(display,"_MOTIF_WM_HINTS",True);

    XSetWMProtocols(display, win, &wm_delete_window, 1);

    XChangeProperty(display, win, _NET_WM_WINDOW_TYPE, XA_ATOM , 32, PropModeReplace,
                    (unsigned char *)&_NET_WM_WINDOW_TYPE_DOCK, 1);
    XChangeProperty(display, win, _NET_WM_STATE,XA_ATOM , 32, PropModeReplace,
                    (unsigned char *)&_NET_WM_STATE_STICKY, 1);

    memset (insets, 0, sizeof (insets));

    XChangeProperty(display, win, _NET_WM_STRUT, XA_CARDINAL , 32, PropModeReplace,
                    (unsigned char *)insets, 4);

    memset (insets_partial, 0, sizeof (insets_partial));

    XChangeProperty(display, win, _NET_WM_STRUT, XA_CARDINAL , 32, PropModeReplace,
                    (unsigned char *)insets_partial, 12);    

    dprintf("Window ID = %x \n",win);

    (*UnLockIt)(env);

    return (jlong) win;
}

/*
 * Class:     org_jdesktop_jdic_dock_internal_impl_UnixDockService
 * Method:    adjustSizeHints
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_dock_internal_impl_UnixDockService_adjustSizeHints (JNIEnv *env, jobject obj, jlong win, jint width, jint height)
{
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

    int insets[4];
    insets[0]=width;
    insets[1]=0;
    insets[2]=0;
    insets[3]=0;

    XChangeProperty(display, win, _NET_WM_STRUT, XA_CARDINAL , 32, PropModeReplace,
                    (unsigned char *)insets, 4);

    (*UnLockIt)(env);
}

/*
 * Class:     org_jdesktop_jdic_dock_internal_impl_UnixDockService
 * Method:    mapWindow
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_dock_internal_impl_UnixDockService_mapWindow(JNIEnv *env, jobject obj, jlong win, jboolean b)
{
    if (b) {
	dprintf("mapped window id is %x\n", win);
	XMapWindow(display, win);
    }
    else {
	dprintf("unmapped window id is %x\n", win);	
	XUnmapWindow(display, win);
    }
}

