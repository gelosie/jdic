//////////////////////////////////
// Copyright (C) 2008 Sun Microsystems, Inc. All rights reserved. Use is
// subject to license terms.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the Lesser GNU General Public License as
// published by the Free Software Foundation; either version 2 of the
// License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA.
#ifndef _PACKEREXT_H_
#define _PACKEREXT_H_

enum FILE_ACCESS {
    ACCESS_READ    = 0x04,
    ACCESS_WRITE   = 0x02,
    ACCESS_EXECUTE = 0x01
};

jobject makeGlobal(
    JNIEnv* env,     
    jobject l_obj
);

void releaseGlobal(
    JNIEnv* env,     
    jobject g_obj
);

class CUnpackerExt {
public:
    enum BASE_PROP {
        arc_time,
        arc_attr,
        arc_original_size,
        arc_prop_count
    };

protected:
    static jclass    ms_jcidInputStream;
    static jmethodID ms_jcidInputStream_read;
    static jmethodID ms_jcidInputStream_readBytes;
    static jmethodID ms_jcidInputStream_mark;
    static jmethodID ms_jcidInputStream_reset;
    static jmethodID ms_jcidInputStream_skip;

    static jclass    ms_jcidZipEntry;
    static jmethodID ms_jcidZipEntry_ctor;


private:
    jobject m_is;

public:
    LPSTR     m_pEntryName;
    int       m_oflag;    // entry attributes
    long      m_osize;    // size of uncompressed data 
    long      m_csize;	  // size of compressed data (zero if uncompressed)
    long      m_crc;      // crc of uncompressed data 

    DWORD     m_dos_time; // entry modification date
    long      m_hint;

public:
    static void initIDs(JNIEnv *env);

    CUnpackerExt(
        JNIEnv *env, 
        jobject is,
        jlong   hint
    );
    virtual ~CUnpackerExt();

    virtual void close(JNIEnv *env);
    virtual bool checkValid(JNIEnv *env);
    virtual jint readNativeBytes(
        JNIEnv *env, 
        jbyteArray buf, 
        jint off, 
        jint len)
    {
        return env->CallIntMethod(
            m_is, 
            ms_jcidInputStream_readBytes,
            buf,
            off,
            len
        );    
    }
    virtual jobject readNextEntry(JNIEnv *env);
    virtual jstring getProperty(
        JNIEnv *env,
        jint iIndex);

//java helpers
    void markJavaStream(
        JNIEnv *env,
        jint readlimit)
    {
        return env->CallVoidMethod(
            m_is, 
            ms_jcidInputStream_mark,
            readlimit
        );    
    }

    void resetJavaStream(JNIEnv *env)
    {
        return env->CallVoidMethod(
            m_is, 
            ms_jcidInputStream_reset
        );    
    }
    jlong skipJavaStream(
        JNIEnv *env,
        jlong  n)
    {
        return env->CallLongMethod(
            m_is, 
            ms_jcidInputStream_skip,
            n
        );    
    }
};

class CPackerExt {
protected:
    static jclass    ms_jcidNativePackedOutputStream;
    static jmethodID ms_jcidNativePackedOutputStream_getEntrySuffix;
    static jmethodID ms_jcidNativePackedOutputStream_getEntrySuffixStub;

    static jclass    ms_jcidOutputStream;
    static jmethodID ms_jcidOutputStream_write;
    static jmethodID ms_jcidOutputStream_writeBytes;

    static jclass    ms_jcidZipEntry;
    static jfieldID  ms_jcidZipEntry_name;
    static jfieldID  ms_jcidZipEntry_time;
    static jfieldID  ms_jcidZipEntry_crc;
    static jfieldID  ms_jcidZipEntry_size;
    static jfieldID  ms_jcidZipEntry_csize;
    static jfieldID  ms_jcidZipEntry_method;
    static jfieldID  ms_jcidZipEntry_comment;

protected:
    jobject m_os;
    jobject m_ze;
    jobject m_this;

public:
    LPSTR     m_pEntryName; //file name
    int       m_oflag;    //file attributes
    long      m_osize;    //uncompressed size
    long      m_csize;    //compressed size
    long      m_crc;      //crc for entry
    long      m_osize_progress;    //uncompressed size (was currently read)
    long      m_csize_progress;    //compressed size (was currently written)
    DWORD     m_dos_time; //modification time
    long      m_hint; //0 - in memory; 1 - on disk; 2 - store crc & csize in name
    int       m_compressionType;// 
    int       m_compressionLevel;// [0..9], default 9

public:
    static void initIDs(
        JNIEnv *env,
        jclass jcidNativePackedOutputStream 
    );

    CPackerExt(
        JNIEnv *env, 
        jobject othis,
        jobject os,
        jlong   hint
    );
    virtual ~CPackerExt();

    virtual void close(JNIEnv *env);
    virtual void finish(JNIEnv *env);
    virtual bool checkValid(JNIEnv *env);
    virtual void writeNativeBytes(
        JNIEnv *env, 
        jbyteArray buf, 
        jint off, 
        jint len)
    {
        m_osize += len;
        env->CallVoidMethod(
            m_os, 
            ms_jcidOutputStream_writeBytes,
            buf,
            off,
            len
        );    
    }
    virtual void putNextEntry(
        JNIEnv *env, 
        jobject ze, 
        jint level);
    virtual jlong closeEntry(
        JNIEnv *env,
        jlong crc);
    void UpdateEntryInfo(JNIEnv *env);
    LPSTR getEntrySuffixStub(JNIEnv *env);

//helpers
    static inline long getTime(JNIEnv *env, jobject ze){
        return env->GetLongField(ze, ms_jcidZipEntry_time);
    }
    static inline long getCRC(JNIEnv *env, jobject ze){
        return env->GetLongField(ze, ms_jcidZipEntry_crc);
    }
    static inline long getSize(JNIEnv *env, jobject ze){
        return env->GetLongField(ze, ms_jcidZipEntry_size);
    }
    static inline long getCSize(JNIEnv *env, jobject ze){
        return env->GetLongField(ze, ms_jcidZipEntry_csize);
    }
    static inline int getMethod(JNIEnv *env, jobject ze){
        return env->GetIntField(ze, ms_jcidZipEntry_method);
    }

    //allocation: _strdup inside! have to be deallocated by free call
    static LPSTR getStringField(
        JNIEnv *env, 
        jobject ze, 
        jfieldID id);
        
    static inline LPSTR getName(JNIEnv *env, jobject ze){
        return getStringField(env, ze, ms_jcidZipEntry_name); 
    }


};

#endif //_PACKEREXT_H_
