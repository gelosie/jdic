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

#include <stdlib.h>
#include <string.h>
#include <libbonobo-2.0/libbonobo.h>
#include "Rhythmbox.h"
#include "org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl.h"

#define RB_IID "OAFIID:GNOME_Rhythmbox"

#define SONG_INFO_CLASS "org/jdesktop/jdic/mpcontrol/rhythmbox/SongInfo"

#define JSTRING(buf)  (*env)->NewStringUTF(env, buf)

void throwException(JNIEnv *env, char *msg) {
    jclass newExcCls = (*env)->FindClass(env, "java/lang/RuntimeException");
    if (newExcCls == NULL) {
        return;
    }
    (*env)->ThrowNew(env, newExcCls, msg);
}


JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_createCorbaEnvironment
  (JNIEnv * env, jclass clz)  {
 
    char* argv;
    int argc;
 
    CORBA_Environment*  ev;

    argc = 0;
    argv = "";    
    
    if (!bonobo_init (&argc, &argv)) {
        throwException(env,"Could not initialize Bonobo");
        return 0;
    }
    
    ev = malloc(sizeof(CORBA_Environment));

    CORBA_exception_init (ev);

    return (jint) ev;
  
}


JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_acquireRhythmboxInstance
  (JNIEnv * env, jclass clz, jint jev) {
  
    CORBA_Environment*  ev;
    GNOME_Rhythmbox rb;

    ev =  (CORBA_Environment*) jev;

  
    rb = bonobo_activation_activate_from_id (RB_IID, 0, NULL, ev);
    if (rb == CORBA_OBJECT_NIL) {
        throwException(env,"Could not create an instance of Rhythmbox");
        return 0;
    }
  
      return (jint)rb;
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_releaseRhythmboxInstance
  (JNIEnv * env, jclass clz, jint jev, jint jrb) {
    CORBA_Environment*  ev;
    GNOME_Rhythmbox rb;

    ev =  (CORBA_Environment*) jev;
    rb = (GNOME_Rhythmbox) jrb;

    //printf("org_jdesktop_jdic_RhytmboxControl_releaseRhythmboxInstance\n");
    //bonobo_object_release_unref (rb, NULL);

}

JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_freeCorbaEnvironment
  (JNIEnv * env, jclass clz, jint jev) {
  
    CORBA_Environment*  ev;
    ev = (CORBA_Environment*) jev;
    CORBA_exception_free (ev);

    return (jint) bonobo_debug_shutdown ();
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_playPause0
  (JNIEnv * env, jclass clz, jint jev,jint jrb) {
      GNOME_Rhythmbox_playPause ((GNOME_Rhythmbox)jrb, (CORBA_Environment*)jev);
  
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_setVolume0
  (JNIEnv * env, jclass clz, jint jev, jint jrb, jfloat jvolume) {
 
      GNOME_Rhythmbox_setVolume ((GNOME_Rhythmbox)jrb, (CORBA_float) jvolume, (CORBA_Environment*)jev );
  
}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl
 * Method:    select
 * Signature: (IILjava/lang/String;)Z
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_select
  (JNIEnv * env, jclass clz, jint jev, jint jrb, jstring str) {
  
    jbyte* buf ;
    
    buf = (*env)->GetStringUTFChars(env,str,0);

    printf("SELECT %s\n", buf);
    GNOME_Rhythmbox_select((GNOME_Rhythmbox)jrb, (CORBA_char *) buf, (CORBA_Environment*)jev );
    
    (*env)->ReleaseStringUTFChars(env,str, buf);
  
  return;
}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl
 * Method:    play
 * Signature: (IILjava/lang/String;)Z
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_play
  (JNIEnv * env, jclass clz, jint jev, jint jrb, jstring str) {
    jbyte* buf =  (*env)->GetStringUTFChars(env,str,0);
    printf("PLAY %s\n", buf);
    GNOME_Rhythmbox_play((GNOME_Rhythmbox)jrb, (CORBA_char *) buf, (CORBA_Environment*)jev );
    (*env)->ReleaseStringUTFChars(env,str, buf);
  
    return;
}



/*
 * Class:     org_jdesktop_jdic_RhytmboxControl
 * Method:    next0
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_next0
  (JNIEnv * env, jclass clz, jint jev, jint jrb) {
  
      GNOME_Rhythmbox_next ((GNOME_Rhythmbox)jrb, (CORBA_Environment*)jev);
  
}

/*
 * Class:     org_jdesktop_jdic_RhytmboxControl
 * Method:    previous0
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_previous0
  (JNIEnv * env, jclass clz, jint jev, jint jrb) {

      GNOME_Rhythmbox_previous ((GNOME_Rhythmbox)jrb, (CORBA_Environment*)jev);
}

jobject createSongInfo(JNIEnv * env, GNOME_Rhythmbox_SongInfo *song_info,jclass cclazz) {
    //printf("createSongInfo env: %d songInfo: %d\n",env, song_info);
    //printf("find class %s = %d\n", SONG_INFO_CLASS, cclazz);
    jmethodID mid = (*env)->GetMethodID(env, cclazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IIIIIII)V");
    //printf("method is %d\n", mid);

    return (*env)->NewObject(env, cclazz, mid, 
        JSTRING(song_info->title),
        JSTRING(song_info->artist),
        JSTRING(song_info->genre),
        JSTRING(song_info->album),
        JSTRING(song_info->path),
        song_info->track_number,
        song_info->duration,
        song_info->bitrate,
        song_info->filesize,
        song_info->rating,
        song_info->play_count,
        song_info->last_played
    );
}

JNIEXPORT jobject JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_getCurrentSong0
  (JNIEnv * env, jclass clz, jint jev, jint jrb) {
 
     //printf("size of CORBA_long : %d \n", sizeof(CORBA_long));
 
        CORBA_Environment*  ev;
        Bonobo_PropertyBag pb;
        GNOME_Rhythmbox rb;
 
        GNOME_Rhythmbox_SongInfo *song_info;
        CORBA_any *any;

        ev =  (CORBA_Environment*) jev;
        rb = (GNOME_Rhythmbox) jrb;
        
        CORBA_exception_init (ev);
        pb = GNOME_Rhythmbox_getPlayerProperties (rb, ev);

        any = Bonobo_PropertyBag_getValue (pb, "song", ev);
        if (!CORBA_TypeCode_equivalent (any->_type, TC_GNOME_Rhythmbox_SongInfo, NULL)) { 
            throwException(env, "Unexpected type for playerProperties.song");
            return NULL;
        }

        song_info = (GNOME_Rhythmbox_SongInfo *)any->_value;
        if (song_info == NULL) {
            throwException(env, "Unexpected value for playerProperties.song");
            return NULL;
        }

        jobject jobj= createSongInfo(env, song_info, (*env)->FindClass(env, SONG_INFO_CLASS));
        bonobo_object_release_unref (pb, NULL);
        
        return jobj;
  
}


// callback implementation, for song change ...

typedef struct on_song_change_callback {
    JNIEnv* env;
    jobject obj;
    jmethodID method;
    jclass songInfoClass;
} on_song_change_callback;

static void on_song_change (BonoboListener *listener, const char *event_name,
        const CORBA_any *any, CORBA_Environment *ev,
        gpointer user_data)
{
    GNOME_Rhythmbox_SongInfo *song_info;
    on_song_change_callback * ud;
    JNIEnv* env;

    if (!CORBA_TypeCode_equivalent (any->_type, 
                    TC_GNOME_Rhythmbox_SongInfo, 
                    NULL)) { 
        g_warning ("Unexpected type\n");
    }
    song_info = (GNOME_Rhythmbox_SongInfo *)any->_value;
    if (song_info == NULL) {
        g_warning ("Unexpected error\n");
    }


    ud = user_data;
    env = ud->env;
    
    jobject songInfo = createSongInfo(env, song_info,ud->songInfoClass);

    (*env)->CallVoidMethod(env, ud->obj, ud->method, songInfo);
    
}


/*
 * Class:     org_jdesktop_jdic_RhytmboxControl
 * Method:    bonoboEventLoop
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_bonoboEventLoop
  (JNIEnv * env, jobject obj, jint jev, jint jrb) {


    CORBA_Environment*  ev;
    Bonobo_PropertyBag pb;
    GNOME_Rhythmbox rb;
    
    on_song_change_callback* callbackInfo;

    ev =  (CORBA_Environment*) jev;
    rb = (GNOME_Rhythmbox) jrb;

    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID mid = (*env)->GetMethodID(env, cls, "onChange", "(Lorg/jdesktop/jdic/mpcontrol/rhythmbox/SongInfo;)V");

    CORBA_exception_init (ev);
    pb = GNOME_Rhythmbox_getPlayerProperties (rb, ev);
  
      
    callbackInfo = malloc(sizeof(on_song_change_callback));
    callbackInfo->env = env;
    callbackInfo->obj = obj;
    callbackInfo->method = mid;
    callbackInfo->songInfoClass = (*env)->FindClass(env, SONG_INFO_CLASS);
    //printf("Song info class is %d\n",     callbackInfo->songInfoClass );
  
    bonobo_event_source_client_add_listener (pb, on_song_change,
                         "Bonobo/Property:change:song",
                         ev, callbackInfo);
  
    bonobo_main ();
    
    CORBA_exception_free (ev);

    bonobo_object_release_unref (pb, NULL);
    

}

JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_mpcontrol_rhythmbox_RhytmboxControl_isPlaying 
  (JNIEnv * env,jclass clz,  jint jev, jint jrb) {
        CORBA_Environment*  ev;
        Bonobo_PropertyBag pb;
        GNOME_Rhythmbox rb;
 
        CORBA_any *any;
        
        CORBA_boolean result;

        ev =  (CORBA_Environment*) jev;
        rb = (GNOME_Rhythmbox) jrb;
        
        CORBA_exception_init (ev);
        pb = GNOME_Rhythmbox_getPlayerProperties (rb, ev);
  
        any = Bonobo_PropertyBag_getValue (pb, "playing", ev);
        if (!CORBA_TypeCode_equivalent (any->_type, TC_CORBA_boolean, NULL)) { 
            throwException(env, "Unexpected type for playerProperties.playing");
            return FALSE;
        }

        result = BONOBO_ARG_GET_BOOLEAN(any);

        bonobo_object_release_unref (pb, NULL);
        return (jboolean) result;
  
}



