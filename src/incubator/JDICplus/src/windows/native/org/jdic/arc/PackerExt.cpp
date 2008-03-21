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
#include "stdafx.h"
#include "PackerExt.h"
#include "CabSupport.h"

//taken from j2se\src\share\native\java\util\zip\zip_util.h
/*
 * Support for reading ZIP/JAR files. Some things worth noting:
 *
 * - Zip file entries larger than 2**32 bytes are not supported.
 * - jzentry time and crc fields are signed even though they really
 *   represent unsigned quantities.
 * - If csize is zero then the entry is uncompressed.
 * - If extra != 0 then the first two bytes are the length of the extra
 *   data in intel byte order.
 * - If pos <= 0 then it is the position of entry LOC header.
 *   If pos > 0 then it is the position of entry data.
 *   pos should not be accessed directly, but only by ZIP_GetEntryDataOffset.
 */

typedef struct jzentry {  /* Zip file entry */
    LPCSTR name;		  /* entry name */
    jlong time;		  /* modification time */
    jlong size;		  /* size of uncompressed data */
    jlong csize;	  /* size of compressed data (zero if uncompressed) */
    jint crc;		  /* crc of uncompressed data */
    LPCSTR comment;	  /* optional zip file comment */
    jbyte *extra;	  /* optional extra data */
    jlong pos;		  /* position of LOC header or entry data */
} jzentry;


JavaVM *jvm;

BOOL APIENTRY DllMain( 
    HANDLE hModule, 
    DWORD  ul_reason_for_call, 
    LPVOID lpReserved
){
    switch( ul_reason_for_call ){
    case DLL_PROCESS_ATTACH:
    case DLL_THREAD_ATTACH:
    case DLL_THREAD_DETACH:
    case DLL_PROCESS_DETACH:
        break;
    }
    return TRUE;
}


jobject makeGlobal(
    JNIEnv* env,     
    jobject l_obj
){
    if(!JNU_IsNull(env, l_obj)) {
        jobject g_obj = env->NewGlobalRef(l_obj);
        env->DeleteLocalRef(l_obj);
        if (JNU_IsNull(env, g_obj)) {
            JNU_ThrowOutOfMemoryError(env, "");
        } else {
            return g_obj;
        }
    }                                                               
    return NULL;
}

void releaseGlobal(
    JNIEnv* env,     
    jobject g_obj
){
    if(!JNU_IsNull(env, g_obj)) {
        env->DeleteGlobalRef(g_obj);
    }
}

jclass getGlobalJavaClazz(
    JNIEnv* env, 
    const char *name
){               
    return (jclass)makeGlobal(env, env->FindClass(name) );
}

jclass    CUnpackerExt::ms_jcidInputStream = NULL;
jmethodID CUnpackerExt::ms_jcidInputStream_read = NULL;//()I: int read()
jmethodID CUnpackerExt::ms_jcidInputStream_readBytes = NULL; //([BII)I: int read(byte[], int, int)
jmethodID CUnpackerExt::ms_jcidInputStream_mark = NULL;//(I)V: void mark(int)
jmethodID CUnpackerExt::ms_jcidInputStream_reset = NULL;//()V: void reset()
jmethodID CUnpackerExt::ms_jcidInputStream_skip = NULL;//(J)J: long skip(long)

jclass    CUnpackerExt::ms_jcidZipEntry = NULL;
jmethodID CUnpackerExt::ms_jcidZipEntry_ctor = NULL;

void CUnpackerExt::initIDs(JNIEnv *env)
{
    ms_jcidInputStream = getGlobalJavaClazz(
        env,
        "java/io/InputStream"
    );
    ms_jcidInputStream_read = env->GetMethodID(ms_jcidInputStream, "read", "()I");
    ms_jcidInputStream_readBytes = env->GetMethodID(ms_jcidInputStream, "read", "([BII)I");
    ms_jcidInputStream_mark = env->GetMethodID(ms_jcidInputStream, "mark", "(I)V");
    ms_jcidInputStream_reset = env->GetMethodID(ms_jcidInputStream, "reset", "()V");
    ms_jcidInputStream_skip = env->GetMethodID(ms_jcidInputStream, "skip", "(J)J");

    ms_jcidZipEntry = getGlobalJavaClazz(
        env,
        "java/util/zip/ZipEntry"
    );
    ms_jcidZipEntry_ctor = env->GetMethodID(ms_jcidZipEntry, "<init>", "(J)V");
}

CUnpackerExt::CUnpackerExt(
    JNIEnv *env, 
    jobject is,
    jlong   hint
)
{
    m_hint = hint;
    m_pEntryName = NULL;
    m_is = makeGlobal(env, is);
    m_oflag = ACCESS_READ | ACCESS_WRITE | ACCESS_EXECUTE;
    m_dos_time = -1;//unknown
    m_osize = -1;//unknown
    m_csize = 0;//uncompressed
    m_crc = -1;//unknown
}

void CUnpackerExt::close(JNIEnv *env)
{
    releaseGlobal(env, m_is);
    m_is = NULL;
}

bool CUnpackerExt::checkValid(JNIEnv *env)
{
    return true;
}

CUnpackerExt::~CUnpackerExt()
{
    free(m_pEntryName);
    if(NULL != m_is) {
        //alarm message
    }   
}

jobject CUnpackerExt::readNextEntry(JNIEnv *env)
{
    if(NULL==m_pEntryName)
        return NULL;

    jzentry zs = {        /* Zip file entry */
        m_pEntryName,	  /* entry name */
        m_dos_time,      /* modification time */
        m_osize,          /* size of uncompressed data */
        -1,               /* size of compressed data (zero if uncompressed) */
        -1,		  /* crc of uncompressed data */
        NULL,	          /* optional zip file comment */
        NULL,	          /* optional extra data */
        NULL		  /* position of LOC header or entry data */
    };
    return env->NewObject(
        ms_jcidZipEntry, 
        ms_jcidZipEntry_ctor, 
        (jlong)&zs
    );
}

jstring CUnpackerExt::getProperty(
    JNIEnv *env,
    jint iIndex
){
    LPCSTR pValue = NULL;
    CHAR buff[64];
    switch(iIndex){
    case arc_time:
        pValue = ptoa(m_dos_time, buff, 16);
        pValue = buff;
        break;
    case arc_attr:
        pValue = ptoa(m_oflag, buff, 16);
        break;
    case arc_original_size:
        pValue = ptoa(m_osize, buff, 16);
        break;
    }
    return pValue
        ? CAB_NewStringPlatform(env, pValue)
        : NULL;
}

extern "C" {

JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedInputStream_initIDs(
    JNIEnv* env, 
    jclass)
{
    CUnpackerExt::initIDs(env);
}

 
JNIEXPORT jlong JNICALL Java_org_jdic_arc_NativePackedInputStream_createNativeStream(
    JNIEnv* env, 
    jclass,
    jobject is,
    jint iFormat, 
    jlong hint)
{
    CUnpackerExt *pUnp = NULL;
    switch(iFormat){
    case 0: //primitive copy
        pUnp = new CUnpackerExt(env, is, hint);
        break;
    case 1: //cab
        pUnp = new CCabUnpacker(env, is, hint);
        break;
    }
    return (jlong)pUnp;
}

JNIEXPORT jint JNICALL Java_org_jdic_arc_NativePackedInputStream_readNativeBytes(
    JNIEnv* env, 
    jclass,
    jlong isNative, 
    jbyteArray buf, 
    jint off, 
    jint len)
{
    return ((CUnpackerExt *)isNative)->readNativeBytes(
        env,
        buf, 
        off, 
        len
    );
}

JNIEXPORT jobject JNICALL Java_org_jdic_arc_NativePackedInputStream_readNextEntryNative(
    JNIEnv* env, 
    jclass,
    jlong isNative)
{
    return ((CUnpackerExt *)isNative)->readNextEntry(env);
}

JNIEXPORT jstring JNICALL Java_org_jdic_arc_NativePackedInputStream_getPropertyNative(
    JNIEnv* env, 
    jclass,
    jlong isNative,
    jint iIndex)
{
    return ((CUnpackerExt *)isNative)->getProperty(env, iIndex);
}

JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedInputStream_checkNativeValid(
    JNIEnv* env, 
    jclass,
    jlong isNative)
{
    ((CUnpackerExt *)isNative)->checkValid(env);
}

JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedInputStream_closeNative(
    JNIEnv* env, 
    jclass,
    jlong isNative)
{
    ((CUnpackerExt *)isNative)->close(env);
    delete (CUnpackerExt *)isNative;
}

/* Initialize the Java VM instance variable when the library is 
   first loaded */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
    jvm = vm;
    return JNI_VERSION_1_2;
}

}; /* extern "C" */


////////////////////////////////
// class CPackerExt
jclass    CPackerExt::ms_jcidNativePackedOutputStream = NULL;
jmethodID CPackerExt::ms_jcidNativePackedOutputStream_getEntrySuffix = NULL;//String getEntrySuffix(String)
jmethodID CPackerExt::ms_jcidNativePackedOutputStream_getEntrySuffixStub = NULL;//String getEntrySuffixStub()

jclass    CPackerExt::ms_jcidOutputStream = NULL;
jmethodID CPackerExt::ms_jcidOutputStream_write = NULL;//()I: int read()
jmethodID CPackerExt::ms_jcidOutputStream_writeBytes = NULL; //([BII)I: int read(byte[], int, int)

jclass    CPackerExt::ms_jcidZipEntry = NULL;
jfieldID  CPackerExt::ms_jcidZipEntry_name = NULL;  //String
jfieldID  CPackerExt::ms_jcidZipEntry_time = NULL;  //long
jfieldID  CPackerExt::ms_jcidZipEntry_crc = NULL;   //long
jfieldID  CPackerExt::ms_jcidZipEntry_size = NULL;  //long
jfieldID  CPackerExt::ms_jcidZipEntry_csize = NULL; //long
jfieldID  CPackerExt::ms_jcidZipEntry_method = NULL;//int
jfieldID  CPackerExt::ms_jcidZipEntry_comment = NULL;//String

void CPackerExt::initIDs(
    JNIEnv *env, 
    jclass jcidNativePackedOutputStream)
{
    ms_jcidNativePackedOutputStream = (jclass)makeGlobal(env, jcidNativePackedOutputStream);
    ms_jcidNativePackedOutputStream_getEntrySuffix = env->GetMethodID(
        ms_jcidNativePackedOutputStream, "getEntrySuffix", "(Ljava/lang/String;)Ljava/lang/String;");
    ms_jcidNativePackedOutputStream_getEntrySuffixStub  = env->GetMethodID(
        ms_jcidNativePackedOutputStream, "getEntrySuffixStub", "()Ljava/lang/String;");

    ms_jcidOutputStream = getGlobalJavaClazz(
        env,
        "java/io/OutputStream"
    );
    ms_jcidOutputStream_write = env->GetMethodID(ms_jcidOutputStream, "write", "(I)V");
    ms_jcidOutputStream_writeBytes = env->GetMethodID(ms_jcidOutputStream, "write", "([BII)V");

    ms_jcidZipEntry = getGlobalJavaClazz(
        env,
        "java/util/zip/ZipEntry"
    );    
    ms_jcidZipEntry_name = env->GetFieldID(ms_jcidZipEntry, "name", "Ljava/lang/String;");
    ms_jcidZipEntry_time = env->GetFieldID(ms_jcidZipEntry, "time", "J");
    ms_jcidZipEntry_crc = env->GetFieldID(ms_jcidZipEntry, "crc", "J");
    ms_jcidZipEntry_size = env->GetFieldID(ms_jcidZipEntry, "size", "J");
    ms_jcidZipEntry_csize = env->GetFieldID(ms_jcidZipEntry, "csize", "J");
    ms_jcidZipEntry_method = env->GetFieldID(ms_jcidZipEntry, "method", "I");        
    ms_jcidZipEntry_comment = env->GetFieldID(ms_jcidZipEntry, "comment", "Ljava/lang/String;");        
}

CPackerExt::CPackerExt(
    JNIEnv *env, 
    jobject othis,    
    jobject os,
    jlong   hint
){
    m_hint = hint;
    m_ze = NULL;
    m_os = makeGlobal(env, os);
    m_this = makeGlobal(env, othis);
    m_pEntryName = NULL;
    m_oflag = ACCESS_READ | ACCESS_WRITE | ACCESS_EXECUTE;
    m_osize = 0;
    m_dos_time = -1;//unknown
    m_crc = -1;//unknown
    m_compressionLevel = 9;//max
    m_compressionType = 0;//store
}

void CPackerExt::close(JNIEnv *env)
{
    closeEntry(env, -1); //alarm termination, crc not defined

    releaseGlobal(env, m_this);
    m_this = NULL;

    releaseGlobal(env, m_os);
    m_os = NULL;
}

void CPackerExt::finish(JNIEnv *env)
{
    //copy is synchronous, so do nothing
}

bool CPackerExt::checkValid(JNIEnv *env)
{
    return true;
}

CPackerExt::~CPackerExt()
{
    free(m_pEntryName);
    if(NULL != m_os || NULL != m_this) {
        //alarm message
    }   
}
void CPackerExt::UpdateEntryInfo(JNIEnv *env)
{
    if(NULL != m_ze){
        free(m_pEntryName);
        m_pEntryName = getName(env, m_ze);
        if(NULL!=m_pEntryName){
            m_dos_time = getTime(env, m_ze);
            m_osize = getSize(env, m_ze);
            m_compressionType = getMethod(env, m_ze);
        }
    }
}
void CPackerExt::putNextEntry(
    JNIEnv *env, 
    jobject ze, 
    jint level)
{
    m_compressionLevel = level;
    m_ze = makeGlobal(env, ze);
    UpdateEntryInfo(env);
    //STRACE1(_T("Name: %s Size: %d Date: %d"), m_pEntryName, m_dos_time, m_osize);
}

jlong CPackerExt::closeEntry(
    JNIEnv *env,
    jlong crc)
{
    m_crc = crc;
    releaseGlobal(env, m_ze);
    m_ze = NULL;

    free(m_pEntryName);
    m_pEntryName = NULL;

    return m_osize;
}

LPSTR CPackerExt::getStringField(
    JNIEnv *env, 
    jobject ze, 
    jfieldID id)
{ 
    LPSTR ret = NULL;
    jstring js = (jstring)env->GetObjectField(ze, id);
    if( NULL!=js ) {
        LPCSTR pBuf = env->GetStringUTFChars(js, NULL);
        if( NULL != pBuf){
            ret = _strdup(pBuf);
            env->ReleaseStringUTFChars(js, pBuf);
        }    
        env->DeleteLocalRef(js);
    }
    return ret;
}

LPSTR CPackerExt::getEntrySuffixStub(JNIEnv *env)
{ 
    LPSTR ret = NULL;
    jstring js = (jstring)env->CallObjectMethod(
        m_this, 
        ms_jcidNativePackedOutputStream_getEntrySuffixStub);

    if( NULL!=js ) {
        LPCSTR pBuf = env->GetStringUTFChars(js, NULL);
        if( NULL != pBuf){
            ret = _strdup(pBuf);
            env->ReleaseStringUTFChars(js, pBuf);
        }    
        env->DeleteLocalRef(js);
    }
    return ret;
}


// native section
extern "C" {
                                            
JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedOutputStream_initIDs(
    JNIEnv* env, 
    jclass jcidNativePackedOutputStream)
{
    CPackerExt::initIDs(env, jcidNativePackedOutputStream);
}

//non-static!!!! 
JNIEXPORT jlong JNICALL Java_org_jdic_arc_NativePackedOutputStream_createNativeStream(
    JNIEnv* env, 
    jobject othis,
    jobject os,
    jint iFormat,
    jlong hint)
{
    CPackerExt *pPkg = NULL;
    switch(iFormat){
    case 0: //primitive copy
        pPkg = new CPackerExt(env, othis, os, hint);
        break;
    case 1: //cab
        pPkg = new CCabPacker(env, othis, os, hint);
        break;
    }
    return (jlong)pPkg;
}

JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedOutputStream_writeNativeBytes(
    JNIEnv* env, 
    jclass,
    jlong osNative, 
    jbyteArray buf, 
    jint off, 
    jint len)
{
    ((CPackerExt *)osNative)->writeNativeBytes(
        env,
        buf, 
        off, 
        len);
}

JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedOutputStream_putNextEntryNative(
    JNIEnv* env, 
    jclass,
    jlong osNative,
    jobject ze,
    jint level)
{
    ((CPackerExt *)osNative)->putNextEntry(env, ze, level);
}

JNIEXPORT jlong JNICALL Java_org_jdic_arc_NativePackedOutputStream_closeEntryNative(
    JNIEnv* env, 
    jclass,
    jlong osNative,
    jlong crc)
{
    return ((CPackerExt *)osNative)->closeEntry(env, crc);
}


JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedOutputStream_checkNativeValid(
    JNIEnv* env, 
    jclass,
    jlong osNative)
{
    ((CPackerExt *)osNative)->checkValid(env);
}

JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedOutputStream_finishNative(
    JNIEnv* env, 
    jclass,
    jlong osNative)
{
    ((CPackerExt *)osNative)->finish(env);
}
JNIEXPORT void JNICALL Java_org_jdic_arc_NativePackedOutputStream_closeNative(
    JNIEnv* env, 
    jclass,
    jlong osNative)
{
    ((CPackerExt *)osNative)->close(env);
    delete (CPackerExt *)osNative;
}

}; /* extern "C" */
