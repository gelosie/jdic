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
#include <bmp/beepctrl.h>
#include "org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer.h"


#define JSTRING(buf)  (*env)->NewStringUTF(env, buf)
#define SONG_INFO_CLASS "org/jdesktop/jdic/mpcontrol/bmp/BeepSongInfo"


/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    getRemoteSession0
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_getRemoteSession0
  (JNIEnv * env, jclass jcl) {
 
     gint session;
     for (session = 0; session<16; session++) {
         if (xmms_remote_is_running(session)) 
             return session;
     }

    return -1;     
  
}
/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    play0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_play0
  (JNIEnv * env, jclass clz, jint ses) {
      xmms_remote_play(ses);
}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    stop0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_stop0
  (JNIEnv * env, jclass clz, jint ses) {
      xmms_remote_stop(ses);

}
/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    pause0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_pause0
  (JNIEnv * env, jclass clz, jint ses) {
      xmms_remote_pause(ses);

}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    getVolume0
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_getVolume0
  (JNIEnv * env, jclass clz, jint ses) {
    return xmms_remote_get_main_volume(ses);    
}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    setVolume0
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_setVolume0
  (JNIEnv * env, jclass clz, jint ses,jint volume) {

    xmms_remote_set_main_volume(ses, volume);
}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    next0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_next0
  (JNIEnv * env, jclass clz, jint ses) {
    xmms_remote_playlist_next(ses);
}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    prev0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_prev0
  (JNIEnv * env, jclass clz, jint ses) {
    xmms_remote_playlist_prev(ses);
}

/*
 * Class:     org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer
 * Method:    getCurrentSong0
 * Signature: (I)Lorg/jdesktop/jdic/mpcontrol/rhytmbox/SongInfo;
 */

JNIEXPORT jobject JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_getCurrentSong0
  (JNIEnv * env, jclass clz, jint ses) {

    gint pos;
    jclass cclazz = (*env)->FindClass(env, SONG_INFO_CLASS);
    jmethodID mid = (*env)->GetMethodID(env, cclazz, "<init>", "(Ljava/lang/String;Ljava/lang/String;I)V");

    pos = xmms_remote_get_playlist_pos(ses);
    //printf("playlist pos is %d\n", pos);

    gchar* file = xmms_remote_get_playlist_file(ses,pos);
    //printf("file is %s\n", file);
    gchar* title = xmms_remote_get_playlist_title(ses,pos);
    //printf("title is %s\n", title);
    
    return (*env)->NewObject(env, cclazz, mid, 
        JSTRING(title),
        JSTRING(file),
        pos//song_info->track_number,
    );

  
}

JNIEXPORT void JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_addUrl
  (JNIEnv * env, jclass clz, jint ses, jstring str) {
  
   jboolean f = 0;
    jbyte* url =  (*env)->GetStringUTFChars(env,str,0);
    
    xmms_remote_playlist_add_url_string(ses, (gchar*)url);
    (*env)->ReleaseStringUTFChars(env,str, url);
    
    //gint pos = xmms_remote_get_playlist_pos(ses);
    gint len = xmms_remote_get_playlist_length(ses);
    //printf("TRACK  %d/%d \n", pos,len);
    xmms_remote_set_playlist_pos(ses, len-1);
    //pos = xmms_remote_get_playlist_pos(ses);
    //printf("TRACK  %d/%d \n", pos,len);
}


JNIEXPORT jboolean JNICALL Java_org_jdesktop_jdic_mpcontrol_bmp_BeepMediaPlayer_isPlaying0
  (JNIEnv * env, jclass clz, jint ses) {
     return xmms_remote_is_playing(ses);
}


