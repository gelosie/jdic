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


/////////////////////////////////////
//   UNPACKER
/////////////////////////////////////

#define MIN_CACHE_SIZE 1024
static LARGE_INTEGER _ZERO64 = {0};

HRESULT CreateStream(
    LPSTREAM *ppstm,
    long hint)
{
    OLE_DECL;
    switch(hint & 0x1){
    case HINT_IN_MEMORY:
        //??? SHCreateMemStream
        OLE_HR = CreateStreamOnHGlobal(
            NULL,
            TRUE,
            ppstm
        );
        break;
    case HINT_ON_DISK:
        {
            CTempFileStream *ps = new CTempFileStream();
            if(NULL==ps){
                OLE_HR = E_OUTOFMEMORY;
            } else {
                OLE_HR = ps->Create( _T(""), _T("cab") );
                if(FAILED(OLE_HR)){
                    ps->Release();
                } else {
                   *ppstm = ps;
                }
            }
        }
        break;
    }
    OLE_RETURN_HR
}

char *return_fdi_error_string(FDIERROR err)
{
    switch(err){
    case FDIERROR_NONE:
        return "No error";
    case FDIERROR_CABINET_NOT_FOUND:
        return "Cabinet not found";
    case FDIERROR_NOT_A_CABINET:
        return "Not a cabinet";
    case FDIERROR_UNKNOWN_CABINET_VERSION:
        return "Unknown cabinet version";
    case FDIERROR_CORRUPT_CABINET:
        return "Corrupt cabinet";
    case FDIERROR_ALLOC_FAIL:
        return "Memory allocation failed";
    case FDIERROR_BAD_COMPR_TYPE:
        return "Unknown compression type";
    case FDIERROR_MDI_FAIL:
        return "Failure decompressing data";
    case FDIERROR_TARGET_FILE:
        return "Failure writing to target file";
    case FDIERROR_RESERVE_MISMATCH:
        return "Cabinets in set have different RESERVE sizes";
    case FDIERROR_WRONG_CABINET:
        return "Cabinet returned on fdintNEXT_CABINET is incorrect";
    case FDIERROR_USER_ABORT:
        return "User aborted";
    default:
        return "Unknown error";
    }
}

int HR2CABERR(
    JNIEnv *env, 
    HRESULT hr, 
    const char *msg, 
    int ret)
{
    switch(hr){
    case E_OUTOFMEMORY:
        JNU_ThrowOutOfMemoryError(env, msg);
        errno = ENOMEM;
        return -1;
    case E_JAVAEXCEPTION:
        errno = EINVAL;
        return -1;
    default:
        ThrowJNIErrorOnOleError(env, hr, msg);
    }
    return FAILED(hr) ? -1 : ret;
}

void CCabUnpacker::checkInCache(DWORD size)
{
    OLE_DECL
    if( !bool(m_inCache) ){
        //INFO: we can open a stream to temp file here as an alternative
        OLE_HRT(CreateStream(
            &m_inCache,
             m_hint
        ));
    }
    STATSTG st;
    OLE_HRT(m_inCache->Stat(&st, STATFLAG_NONAME));
    if( st.cbSize.LowPart < size ){
        OLE_HRT(m_inCache->Seek(_ZERO64, SEEK_END, NULL));

        //java read
        //No more exceptions here!
        jsize cb = jsize(size) - st.cbSize.LowPart;
        jbyteArray jba = m_env_nt->NewByteArray( jsize(cb) );
        if( NULL == jba ){
            OLE_HR = E_OUTOFMEMORY;
        } else {
            jint ret = 0, read;
            while(0 < cb){
                read = CUnpackerExt::readNativeBytes(
                    m_env_nt,
                    jba,
                    ret,
                    cb
                );
                if( m_env_nt->ExceptionCheck() ){
                    OLE_HR = E_JAVAEXCEPTION;
                    break;
                }
                if( -1 == read ){
                    break;
                }
                cb -= read;
                ret += read;
            }

            //copy to stream
            if(SUCCEEDED(OLE_HR)){
                jbyte *pSrc = m_env_nt->GetByteArrayElements(jba, NULL);
                if(NULL==pSrc){
                    OLE_HR = E_OUTOFMEMORY;
                } else {
                    OLE_HR = m_inCache->Write(pSrc, ret, NULL);
                    m_env_nt->ReleaseByteArrayElements(jba, pSrc, JNI_ABORT);
                }
            }
            m_env_nt->DeleteLocalRef(jba);
        }  

        //here we can safely throw an exception if problem happens
        OLE_HRT(OLE_HR);
    }
}

UINT CCabUnpacker::read(
    CCabUnpacker::JStreamPointer *p, 
    void *pv, 
    UINT cb)
{
    ULONG read = (UINT)-1;
    OLE_TRY
    checkInCache(p->m_cbPos + cb);
    LARGE_INTEGER newpos = {p->m_cbPos, 0};
    OLE_HRT(m_inCache->Seek(newpos, SEEK_SET, NULL));
    OLE_HRT(m_inCache->Read(pv, cb, &read));
    p->m_cbPos += cb;
    OLE_CATCH
    return HR2CABERR(m_env_nt, OLE_HR, "stream read error", read);
}

long CCabUnpacker::seek(
    CCabUnpacker::JStreamPointer *p, 
    long dist, 
    int seektype)
{
    OLE_DECL;
    switch(seektype){
    case SEEK_END:
        OLE_HR = E_INVALIDARG;
        break;
    case SEEK_SET:
        p->m_cbPos = dist; 
        break;
    case SEEK_CUR:
        p->m_cbPos += dist; 
        break;
    }
    return HR2CABERR(m_env_nt, OLE_HR, "stream seek error", p->m_cbPos);    
}


HRESULT CCabUnpacker::writeOutCache(
    void *pv, 
    DWORD dwSize, 
    DWORD *pdwCopied)
{
    OLE_TRY
    HANDLE hhSignals[] = { m_hevClose, m_hevOutWriteReady, m_hCabThread };
    if(pdwCopied){
        *pdwCopied = 0;
    }
    while(1){
        {
            //out cache locked operation
            OLESyncro _lk(m_hmtLock);           
            DWORD dwFreeSpace;
            OLE_HRT(m_outCache.Space(&dwFreeSpace));
            if( 0 < dwFreeSpace ){
                DWORD dwWritten;
                OLE_HRT(m_outCache.DataAdd(pv, min(dwFreeSpace, dwSize), &dwWritten));

                dwSize -= dwWritten;
                pv = (LPBYTE)pv + dwWritten;
                if(pdwCopied){
                    *pdwCopied += dwWritten;
                }
                ::SetEvent(m_hevOutReadReady);
            }
        }
        //wait only if it needs 
        if( 0 == dwSize )
            break;

        switch( ::WaitForMultipleObjects(
            sizeof(hhSignals)/sizeof(*hhSignals),
            hhSignals,
            FALSE,
            INFINITE))
        {
        case WAIT_OBJECT_0:
            //closed, user cancel from 3-d thread            
            OLE_HR = S_FALSE;
            goto M_EXIT;
        case WAIT_OBJECT_0+1:
            //read happened 
            break;
        case WAIT_OBJECT_0+2:
            //seems error happens. details are m_ex 
            OLE_HR = S_FALSE;
            goto M_EXIT;
        default:
            //system error
            OLE_HRT(E_POINTER);
            goto M_EXIT;
        }
    }
M_EXIT: ;
    OLE_CATCH
    OLE_RETURN_HR
}

HRESULT CCabUnpacker::readOutCache(
    void *pv, 
    DWORD dwSize, 
    DWORD *pdwCopied)
{
    OLE_TRY
    HANDLE hhSignals[] = { m_hevClose, m_hevOutReadReady, m_hevEndOfEntry };
    if(pdwCopied){
        *pdwCopied = 0;
    }
    while(1){
        {
            //out cache locked operation
            OLESyncro _lk(m_hmtLock);           
            DWORD dwUsed;
            OLE_HRT(m_outCache.Used(&dwUsed));
            if( 0 < dwUsed ){
                DWORD dwRead;
                OLE_HRT(m_outCache.DataGet(pv, min(dwUsed, dwSize), &dwRead));

                dwSize -= dwRead;
                pv = (LPBYTE)pv + dwRead;
                if(pdwCopied){
                    *pdwCopied += dwRead;
                }
                ::SetEvent(m_hevOutWriteReady);
            }
        }
        //wait only if it needs 
        if(0 == dwSize)
            break;

        switch( ::WaitForMultipleObjects(
            sizeof(hhSignals)/sizeof(*hhSignals),
            hhSignals,
            FALSE,
            INFINITE))
        {
        case WAIT_OBJECT_0:
            //closed, user cancel from 3-d thread            
            OLE_HR = S_FALSE;
            goto M_EXIT;
        case WAIT_OBJECT_0+1:
            //write happened
            break;
        case WAIT_OBJECT_0+2:
            //no more read - end of file reached 
            goto M_EXIT;
        default:
            //system error
            OLE_HRT(E_POINTER);
            goto M_EXIT;
        }
    }
M_EXIT: ;
    OLE_CATCH
    OLE_RETURN_HR
}

UINT CCabUnpacker::write(
    CCabUnpacker::JStreamPointer *p, 
    void *pv, 
    UINT cb)
{
    OLE_DECL;
    DWORD uiTotal;
    OLE_HR = writeOutCache(pv, cb, &uiTotal);
    //all was written or error happened
    return HR2CABERR(m_env_nt, OLE_HR, "stream write error", uiTotal);
}

jint CCabUnpacker::readNativeBytes(
    JNIEnv *env, 
    jbyteArray buf, 
    jint off, 
    jint len)
{
    if( !checkValid(env) )
        return -1;

    OLE_DECL;
    DWORD dwCopied = (DWORD)-1;
    jbyte *pSrc = env->GetByteArrayElements(buf, NULL);
    //jbyte *pSrc = new jbyte[len];
    if(NULL==pSrc){
        OLE_HR = E_OUTOFMEMORY;
    } else {
        OLE_HR = readOutCache(pSrc + off, len, &dwCopied);
        env->ReleaseByteArrayElements(buf, pSrc, 0);
        //delete[] pSrc;
    }
    jint ret = HR2CABERR(env, OLE_HR, "stream out error", dwCopied);
    if( 0 < len && 0 == ret ){
        //STRACE1("no more read");
        ret = -1;
    }
    if( -1 == ret ){
        //STRACE1("no more read or error");
        ::SetEvent(m_hevLastEntryRead);
    }    
    if( !checkValid(env) )
        return -1;
    return ret;
}

/*
bool isHexSubstr(LPSTR pPos, int len, long *pdw)
{
    CHAR szHex[] = "0123456789abcdefABCDEF";
    BYTE Nibbles[] = {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 
        0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 
        0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F 
    };
    *pdw = 0;
    for(int i = 0; i < len; ++i){
        LPSTR p = strchr(szHex, pPos[i]);
        if( NULL==p ){
            *pdw = -1;
            return false;
        } else {
            *pdw <<= 4;
            *pdw |= Nibbles[(int)(p-szHex)];
        }
    }
    return true;
}


void CCabUnpacker::ParseEntryName()
{
    //extract string like this nnnn{01234567-01234567xxxx}.eeee\0
    LPCSTR pDotPos = strrchr(m_pEntryName, '.');
    if(NULL==pDotPos){
        //there is not any extension
        pDotPos = m_pEntryName + strlen(m_pEntryName);
    }
    LPSTR pCloseInfoPos = strrchr(m_pEntryName, '}');
    if(NULL==pCloseInfoPos){
        return;
    }

    LPSTR pOpenInfoPos = strrchr(pCloseInfoPos, '{');
    //8hex+8hex+1minus=17 
    if(
        NULL!=pOpenInfoPos && 
        (pCloseInfoPos - pOpenInfoPos)>17 &&
        '-'==pCloseInfoPos[9] &&
        isHexSubstr(pCloseInfoPos+1, 8, &m_crc) &&
        isHexSubstr(pCloseInfoPos+10, 8, &m_csize) )
    {
       //truncate file name to original form nnnn.eeee\0
       *pOpenInfoPos = 0;
       strcat(m_pEntryName, pDotPos);
    }
}
*/

void CCabUnpacker::signalNewEntrtyFound(
    LPCSTR lpEntryName,
    USHORT time,
    USHORT date,
    int    oflag,
    long   osize)
{
    m_dos_time = MAKELONG(time, date);

    //Mask out attribute bits other than read-only,
    //hidden, system, and archive, since the other
    //attribute bits are reserved for use by
    //the cabinet format.
    //_A_RDONLY | _A_HIDDEN | _A_SYSTEM | _A_ARCH

    m_oflag = oflag; //attributes of original
    m_osize = osize;//uncompressed of size

    free(m_pEntryName);
    m_pEntryName = lpEntryName ? _strdup(lpEntryName) : NULL;
    if(NULL!=m_pEntryName){
        for(LPSTR pPos = m_pEntryName; *pPos; ++pPos){
            if('\\'==*pPos){
                *pPos = '/';
            }
        }
    }
    
    ::ResetEvent(m_hevEndOfEntry);
    ::SetEvent(m_hevNewEntryFound);
}

INT_PTR  CCabUnpacker::notification(
    FDINOTIFICATIONTYPE fdint, 
    PFDINOTIFICATION pfdin)
{
    INT_PTR ret = 0;
    switch(fdint){
    case fdintCABINET_INFO:// general information about the cabinet
        setProperty(cab_next_file, pfdin->psz1);//next cabinet
        setProperty(cab_next_disk, pfdin->psz2);//next disk
        setProperty(cab_path,      pfdin->psz3);//cabinet path
        setProperty(cab_set_ID,    pfdin->setID); //cabinet set ID (int)
        setProperty(cab_number,    pfdin->iCabinet); //cabinet # in set (int)
        break;
    case fdintPARTIAL_FILE:// first file in cabinet is continuation
        setProperty(cab_torn_file, pfdin->psz1);//name of continued file
        setProperty(cab_prev_file, pfdin->psz2);//name of cabinet where file starts 
        setProperty(cab_prev_disk, pfdin->psz3);//name of disk where file starts
        //signal here that new synthetic folder-section has come
        if(NULL!=pfdin->psz3){
            CHAR szSyntheticPath[1024];
            if( 0 > _snprintf(
                szSyntheticPath, 1024, "cab_prev://%s|/%s/%s/", 
                pfdin->psz3,
                pfdin->psz2,
                pfdin->psz1))
            {
                szSyntheticPath[1023] = 0;
            }
            signalNewEntrtyFound(szSyntheticPath);
        }
        break;
    case fdintCOPY_FILE:// file to be copied
        signalNewEntrtyFound(
            pfdin->psz1,
            pfdin->time,
            pfdin->date,
            ACCESS_READ | ACCESS_EXECUTE | ((pfdin->attribs & _A_RDONLY) ? 0 : ACCESS_WRITE),
            pfdin->cb
        );
        {
            CHAR szName[100];        
            ret = file_open(
                ptoa((LONG_PTR)this, szName, 10),
                0, //fake
                0  //fake
            );
        }
        break;
    case fdintCLOSE_FILE_INFO:	// close the file, set relevant info
        file_close(pfdin->hf);
        ::SetEvent(m_hevEndOfEntry);
        {
            //wait here for last read or close
            HANDLE hhSignals[] = { m_hevLastEntryRead, m_hevClose };
            ::WaitForMultipleObjects(
                sizeof(hhSignals)/sizeof(*hhSignals),
                hhSignals,
                FALSE,
                INFINITE);
        }
        ++ret;//TRUE
        break;
    case fdintNEXT_CABINET:	// file continued to next cabinet
        setProperty(cab_next_file, pfdin->psz1);//name of next cabinet where file continued
        setProperty(cab_next_disk, pfdin->psz2);//name of next disk where file continued
        setProperty(cab_path,      pfdin->psz3);//cabinet path name
        
        //signal here that new synthetic folder-section has come
        if(NULL!=pfdin->psz1){
            CHAR szSyntheticPath[1024];
            if( 0 > _snprintf(
                szSyntheticPath, 1024, "cab_next://%s|/%s/", 
                pfdin->psz2,
                pfdin->psz1))
            {
                szSyntheticPath[1023] = 0;
            }
            signalNewEntrtyFound(szSyntheticPath);
        }
        break;
    }

    //no wait, just check
    if( WAIT_OBJECT_0 == ::WaitForSingleObject(m_hevClose,0) ){
        //user cancel if close signaled
        return -1;
    }
    return ret;
}

///////////////////////////////////
// class CCabUnpacker
CCabUnpacker::CCabUnpacker(
    JNIEnv *env, 
    jobject is,
    jlong   hint
)
: CUnpackerExt(env, is, hint),
  m_hevNewEntryFound(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevLastEntryRead(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevEndOfEntry(::CreateEvent(NULL, TRUE, TRUE, NULL)), //manual reset, signaled
  m_hevOutWriteReady(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevOutReadReady(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevClose(::CreateEvent(NULL, TRUE, FALSE, NULL)), //set ones, can not be re-opened
  m_hmtLock(::CreateMutex(NULL, FALSE, NULL)),
  m_hCabThread(NULL),
  m_CabThreadId(0),
  m_env_nt(NULL),
  m_bFirstRead(TRUE),
  m_outCache(MIN_CACHE_SIZE),
  m_ex(NULL)
{
    m_csize = -1;//compressed, but unknown

    //there is not default entry in CAB
    memset(m_props, 0, sizeof(m_props));
    m_hfdi = FDICreate(
        mem_alloc,
        mem_free,
        file_open,
        file_read,
        file_write,
        file_close,
        file_seek,
        cpu80386,
        &m_erf
    );
    if( NULL==m_hfdi ) {
        JNU_ThrowIOException(env, "FDICreate error");
    }
}

bool CCabUnpacker::checkValid(JNIEnv *env)
{
    if(NULL!=m_ex){
        env->Throw(m_ex);
        releaseGlobal(env, m_ex);
        m_ex = NULL;
        return false;
    }
    return true;
}

void CCabUnpacker::close(JNIEnv *env)
{
    if(m_hCabThread){
        ::SetEvent(m_hevClose);
        ::WaitForSingleObject(m_hCabThread, INFINITE);
        ::CloseHandle(m_hCabThread);
        m_hCabThread = NULL;
    }

    if( NULL!=m_hfdi && !FDIDestroy(m_hfdi) ){
        JNU_ThrowIOException(env, "FDIDestroy error");
    }
    m_hfdi = NULL;

    releaseGlobal(env, m_ex);
    m_ex = NULL;

    m_inCache = NULL;
    CUnpackerExt::close(env);
}

CCabUnpacker::~CCabUnpacker()
{
    for(int i=0; i < sizeof(m_props)/sizeof(*m_props); ++i ){
        free(m_props[i]);
    }
    if(NULL != m_hfdi){
        //alarm message
    }
}


unsigned  __stdcall  CCabUnpacker::ThreadAction(void *pThis)
{
    return ((CCabUnpacker*)pThis)->Run();
}
void CCabUnpacker::CreateThreadIfNeed()
{
    if(m_bFirstRead){
        m_hCabThread = (HANDLE)_beginthreadex(
            NULL, 
            0, 
            ThreadAction, 
            this, 
            0, 
            (unsigned int *)&m_CabThreadId
        );
        m_bFirstRead = FALSE;
    }
}

unsigned int CCabUnpacker::Run()
{
    unsigned int ret = (unsigned int)jvm->AttachCurrentThread(
        (void**)&m_env_nt, 
        NULL
    );
    if( 0 == ret ){
        CHAR szName[64];        
        if( !FDICopy(
            m_hfdi,
            ptoa((LONG_PTR)this, szName, 10),
            "",
            0,
            notification_function,
            NULL,
            this))
        {
             ret = m_erf.erfOper;
            jthrowable lex = m_env_nt->ExceptionOccurred();
            if( NULL == lex ){
                JNU_ThrowIOException(
                    m_env_nt,
                    return_fdi_error_string((FDIERROR)ret)
                );
                lex = m_env_nt->ExceptionOccurred();
            }
            m_ex = (jthrowable) makeGlobal(
                m_env_nt, 
                lex
            );
             //m_env_nt->ExceptionDescribe();
             m_env_nt->ExceptionClear();
        }
        jvm->DetachCurrentThread();   
        m_env_nt = NULL;
    }
    
    signalNewEntrtyFound();//last null-section was found
    return ret; 
}

jobject CCabUnpacker::readNextEntry(
    JNIEnv *env 
){
    CreateThreadIfNeed();
    if(NULL==m_hCabThread){
        JNU_ThrowIOException(env, "Invalid unpacker state");
    } else {
        HANDLE hhSignals[] = { m_hevClose, m_hevNewEntryFound };
        DWORD dwRes = ::WaitForMultipleObjects(
            sizeof(hhSignals)/sizeof(*hhSignals),
            hhSignals,
            FALSE,
            INFINITE
        );
        if( !checkValid(env) || WAIT_OBJECT_0 == dwRes ){
            return NULL;
        }
        return CUnpackerExt::readNextEntry(env);
    }
    return NULL;
}

void CCabUnpacker::setProperty(int iIndex, LPSTR pValue)
{
   free(m_props[iIndex]);
   m_props[iIndex] = _strdup(pValue);
}

void CCabUnpacker::setProperty(int iIndex, LONG_PTR iValue)
{
   CHAR szDig[32];
   setProperty(iIndex, ptoa(iValue, szDig, 16));
}


jstring CCabUnpacker::getProperty(
    JNIEnv *env,
    jint iIndex
){
    if(iIndex < arc_prop_count){
        return CUnpackerExt::getProperty(env, iIndex);
    }

    iIndex -= arc_prop_count;
    LPSTR pValue = NULL;
    if(iIndex < cab_prop_count){
        pValue = m_props[iIndex];
    }
    return pValue
        ? CAB_NewStringPlatform(env, pValue)
        : NULL;
}


/////////////////////////////////////
//   PACKER
/////////////////////////////////////

char *return_fci_error_string(FCIERROR err)
{
    switch(err){
    case FCIERR_NONE:
        return "No error";
    case FCIERR_OPEN_SRC:
        return "Failure opening file to be stored in cabinet";
    case FCIERR_READ_SRC:
        return "Failure reading file to be stored in cabinet";
    case FCIERR_ALLOC_FAIL:
        return "Insufficient memory in FCI";
    case FCIERR_TEMP_FILE:
        return "Could not create a temporary file";
    case FCIERR_BAD_COMPR_TYPE:
        return "Unknown compression type";
    case FCIERR_CAB_FILE:
        return "Could not create cabinet file";
    case FCIERR_USER_ABORT:
        return "Client requested abort";
    case FCIERR_MCI_FAIL:
        return "Failure compressing data";
    default:
        return "Unknown error";
    }
}



///////////////////////////////////
// class CCabPacker
CCabPacker::CCabPacker(
    JNIEnv *env, 
    jobject othis,
    jobject os,
    jlong   hint
)
: CPackerExt(env, othis, os, hint),
  m_hevCreateNewEntry(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevDropToJavaOS(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevDropToJavaOSComplite(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevEndOfEntry(::CreateEvent(NULL, TRUE, TRUE, NULL)), //manual reset, signaled
  m_hevOutWriteReady(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevOutReadReady(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hevCompressionStarted(::CreateEvent(NULL, FALSE, FALSE, NULL)),
  m_hmtEntryInProgress(::CreateMutex(NULL, FALSE, NULL)),
  m_hevClose(::CreateEvent(NULL, TRUE, FALSE, NULL)), //set ones, can not be re-opened
  m_hmtLock(::CreateMutex(NULL, FALSE, NULL)),
  m_hCabThread(NULL),
  m_CabThreadId(0),
  m_env_nt(NULL),
  m_bFirstRead(TRUE),
  m_inCache(MIN_CACHE_SIZE),
  m_ex(NULL)
{
    m_pEntrySuffixStub = getEntrySuffixStub(env);
    m_compressionType = tcompTYPE_LZX;

    //there is not default entry in CAB
    OLE_TRY
    OLE_HRT(CreateStream(
        &m_outCache,
        m_hint
    ));

    memset(&m_ccab, 0, sizeof(CCAB));

    // When a CAB file reaches this size, a new CAB will be created
    // automatically.  This is useful for fitting CAB files onto disks.
    // If you want to create just one huge CAB file with everything in
    // it, change this to a very very large number.
    m_ccab.cb = 0x7FFFFFFF;//(ULONG)-1; //we want
    //m_ccab.cb = 2000000;//(ULONG)-1; //we want     

    // When a folder has this much compressed data inside it,
    // automatically flush the folder.
    // Flushing the folder hurts compression a little bit, but
    // helps random access significantly.
    m_ccab.cbFolderThresh = 0x7FFFFFFF;//(ULONG)-1; //never try
    //m_ccab.cbFolderThresh = 2000000;//(ULONG)-1; //never try

    //Don't reserve space for any extensions
    m_ccab.cbReserveCFHeader = 0;
    m_ccab.cbReserveCFFolder = 0;
    m_ccab.cbReserveCFData   = 0;

    //We use this to create the cabinet name
    m_ccab.iCab = 1;

    //If you want to use disk names, use this to count disks
    m_ccab.iDisk = 0;

    //Choose your own number
    m_ccab.setID = 0x414A; //"SN"

    //Only important if CABs are spanning multiple
    //disks, in which case you will want to use a
    //real disk name.

    //Can be left as an empty string.
    //strcpy(m_ccab.szDisk, "MyDisk");

    // where to store the created CAB files
    //strcpy(m_ccab.szCabPath, "c:\\");

    // store name of first CAB file 
    //store_cab_name(cab_parms->szCab, cab_parms->iCab);
    strcpy(m_ccab.szCab, OUT_NAME);

    m_hfci = FCICreate(
        &m_erf,
        fci_placed,
        mem_alloc,
        mem_free,
        fci_open,
        fci_read,
        fci_write,
        fci_close,
        fci_seek,
        fci_delete,
        fci_temp_file,
        &m_ccab,
        this
    );
    OLE_CATCH;

    if( NULL==m_hfci || FAILED(OLE_HR) ) {
        if( FAILED(OLE_HR) ){
            HR2CABERR(env, OLE_HR, "FCI create error", 0);
        } else {
            JNU_ThrowIOException(env, "FCI create error");
        }
    }
}

bool CCabPacker::checkValid(JNIEnv *env)
{
    if(NULL!=m_ex){
        env->Throw(m_ex);
        releaseGlobal(env, m_ex);
        m_ex = NULL;
        return false;
    }
    return true;
}

void CCabPacker::finish(JNIEnv *env)
{
    closeEntry(env, -1);//alarm termination, crc unknown
    if( !env->ExceptionCheck() && m_hCabThread){
        ::SetEvent(m_hevDropToJavaOS);
        HANDLE hhSignals[] = { m_hCabThread,  m_hevDropToJavaOSComplite };
        ::WaitForMultipleObjects(
            sizeof(hhSignals)/sizeof(*hhSignals),
            hhSignals,
            FALSE,
            INFINITE);
        checkValid(env);
    }
}

void CCabPacker::close(JNIEnv *env)
{
    if(m_hCabThread){
        ::SetEvent(m_hevClose);
        ::WaitForSingleObject(m_hCabThread, INFINITE);
        ::CloseHandle(m_hCabThread);
        m_hCabThread = NULL;
    }
    if( NULL!=m_hfci && !FCIDestroy(m_hfci) ){
        JNU_ThrowIOException(env, return_fci_error_string((FCIERROR)m_erf.erfOper));
    }
    m_hfci = NULL;

    releaseGlobal(env, m_ex);
    m_ex = NULL;

    m_outCache = NULL;
    CPackerExt::close(env);
}

CCabPacker::~CCabPacker()
{
    free(m_pEntrySuffixStub);
    if(NULL != m_hfci){
        //alarm message
    }
}

unsigned  __stdcall  CCabPacker::ThreadAction(void *pThis)
{
    return ((CCabPacker*)pThis)->Run();
}

void CCabPacker::CreateThreadIfNeed()
{
    if(m_bFirstRead){
        m_hCabThread = (HANDLE)_beginthreadex(
            NULL, 
            0, 
            ThreadAction, 
            this, 
            0, 
            (unsigned int *)&m_CabThreadId
        );
        m_bFirstRead = FALSE;
    }
}

HRESULT CCabPacker::DropJavaStream(JNIEnv *env)
{
    OLE_DECL;
    jbyteArray jba = env->NewByteArray( jsize(1024) );
    if( NULL == jba ){
        OLE_HR = E_OUTOFMEMORY;
    } else {
        //copy to stream
        OLE_HR = m_outCache->Seek(_ZERO64, SEEK_SET, NULL);
        while(SUCCEEDED(OLE_HR)){
            jbyte *pDst = env->GetByteArrayElements(jba, NULL);
            if(NULL==pDst){
                OLE_HR = E_OUTOFMEMORY;
            } else {
                ULONG read;
                OLE_HR = m_outCache->Read(
                    pDst, 
                    1024, 
                    &read);
                env->ReleaseByteArrayElements(jba, pDst, 0);
                if(0==read)
                    break;
                CPackerExt::writeNativeBytes(
                    env,
                    jba,
                    0,
                    read
                );
                if(env->ExceptionCheck())
                    break;
            }
        }
        env->DeleteLocalRef(jba);
    }
    if(SUCCEEDED(m_outCache)){
        m_outCache = NULL;
        OLE_NEXT_TRY
        OLE_HRT(CreateStream(
            &m_outCache,
             m_hint
        ));
        OLE_CATCH
    }
    OLE_RETURN_HR
}
TCOMP CCabPacker::GetCompressionType(){
    switch(m_compressionType){
    case tcompTYPE_NONE: // No compression
        return tcompTYPE_NONE;
    case tcompTYPE_MSZIP:// MSZIP
        return tcompTYPE_MSZIP;
    case tcompTYPE_QUANTUM:// Quantum
        return tcompTYPE_QUANTUM | tcompQUANTUM_MEM_HI | 
            (tcompMASK_QUANTUM_LEVEL & ( tcompQUANTUM_LEVEL_LO +
              ((tcompQUANTUM_LEVEL_HI - tcompQUANTUM_LEVEL_LO)*(m_compressionLevel%10))/9));
    case tcompTYPE_LZX:// LZX
    default:
        return tcompTYPE_LZX | 
            (tcompMASK_LZX_WINDOW & (tcompLZX_WINDOW_LO + 
              ((tcompLZX_WINDOW_HI - tcompLZX_WINDOW_LO)*(m_compressionLevel%10))/9));
    }
}

unsigned int CCabPacker::Run()
{
    unsigned int ret = (unsigned int)jvm->AttachCurrentThread(
        (void**)&m_env_nt, 
        NULL
    );
    if( 0 == ret ){
        bool bReportError(false);
        while(1){
            HANDLE hhSignals[] = { m_hevCreateNewEntry, m_hevDropToJavaOS, m_hevClose };
            DWORD res = ::WaitForMultipleObjects(
                sizeof(hhSignals)/sizeof(*hhSignals),
                hhSignals,
                FALSE,
                INFINITE);
            switch( res ){
            case WAIT_OBJECT_0:
                //new file
                {
                    OLESyncro _lk(m_hmtEntryInProgress);           
                    ::SetEvent(m_hevCompressionStarted);
                    m_osize_progress = 0;
                    m_csize_progress = 0;
                    if( !FCIAddFile(
                        m_hfci,
                        m_pEntryName,
                        m_pEntryName,
                        FALSE,
                        fci_next_cabinet,
                        progress,
                        fci_open_info,
                        GetCompressionType()))
                    {
                        bReportError = true;
                        goto M_EXIT;
                    }
                } 
                break;
            case WAIT_OBJECT_0 + 1:
                //finish the job to JAVA file 
                {
                    OLESyncro _lk(m_hmtEntryInProgress);           
                    if( !FCIFlushCabinet(
                        m_hfci,
                        FALSE,
                        fci_next_cabinet,
                        progress))
                    {
                        bReportError = true;
                        goto M_EXIT;
                    } else {
                        //drop the stream to Java 
                        OLE_DECL
                        OLE_HR = DropJavaStream(m_env_nt);
                        if( -1 == HR2CABERR(m_env_nt, OLE_HR, "java stream write error", 0) ){
                            bReportError = true;
                            goto M_EXIT;
                        } else {
                            ::SetEvent(m_hevDropToJavaOSComplite);
                        }
                    }
                }
                break;
            case WAIT_OBJECT_0 + 2:
            default:
                goto M_EXIT;
            }
        }
M_EXIT: ;
        if(bReportError){
            ret = m_erf.erfOper;
            jthrowable lex = m_env_nt->ExceptionOccurred();
            if( NULL == lex ){
                JNU_ThrowIOException(
                    m_env_nt,
                    return_fci_error_string((FCIERROR)ret)
                );
                lex = m_env_nt->ExceptionOccurred();
            }
            m_ex = (jthrowable) makeGlobal(
                m_env_nt, 
                lex
            );
            //m_env_nt->ExceptionDescribe();
            m_env_nt->ExceptionClear();
        }
        jvm->DetachCurrentThread();   
        m_env_nt = NULL;
    }
    ::SetEvent(m_hevEndOfEntry);
    return ret; 
}

void CCabPacker::putNextEntry(
    JNIEnv *env,
    jobject ze,
    jint level
){
    CreateThreadIfNeed();
    if(NULL==m_hCabThread){
        JNU_ThrowIOException(env, "Invalid packer state");
    } else {
        {   //atomic critical section 
            //covered by m_hmtEntryInProgress    
            OLESyncro _lk(m_hmtEntryInProgress);
            CPackerExt::putNextEntry(env, ze, level);
            int cb = strlen(m_pEntryName);
            BYTE last_ch = m_pEntryName[cb-1];
            if( (m_hint & HINT_SORE_EINFO) && 
                '/'!=last_ch && '\\'!=last_ch && //if it is not a directory entry
                NULL!=m_pEntrySuffixStub && //if we like to have an additional space
                0 != *m_pEntrySuffixStub )
            {
                //reserve additional bytes in file name
                //not "new" because "free"!!!!
                LPSTR pEN = (LPSTR)malloc( 
                    (cb + strlen(m_pEntrySuffixStub) + 1)*sizeof(CHAR) ); 
                if(NULL==pEN){
                    JNU_ThrowOutOfMemoryError(env, "cannot allocate space for CRC and etc");
                    return;
                }
                strcpy(pEN, m_pEntryName);
                strcat(pEN, m_pEntrySuffixStub);
                free(m_pEntryName);
                m_pEntryName = pEN;
                STRACE1(m_pEntryName);
            }
            ::ResetEvent(m_hevEndOfEntry);
            ::SetEvent(m_hevCreateNewEntry);
        }
        //wait for action start
        ::WaitForSingleObject(m_hevCompressionStarted, INFINITE);
    }
}

jlong CCabPacker::closeEntry(
    JNIEnv *env,
    jlong crc)
{
    if(NULL!=m_ze){
        ::SetEvent(m_hevEndOfEntry);
        {
            OLESyncro _lk(m_hmtEntryInProgress);
            CPackerExt::closeEntry(env, crc);
            checkValid(env);
            return m_csize_progress;
        }
    }
    return -1;
}
HRESULT CCabPacker::writeInCache(
    void *pv, 
    DWORD dwSize, 
    DWORD *pdwCopied)
{
    OLE_TRY
    HANDLE hhSignals[] = { m_hevClose, m_hevOutWriteReady, m_hCabThread };
    if(pdwCopied){
        *pdwCopied = 0;
    }
    while(1){
        {
            //in cache locked operation
            OLESyncro _lk(m_hmtLock);           
            DWORD dwFreeSpace;
            OLE_HRT(m_inCache.Space(&dwFreeSpace));
            if( 0 < dwFreeSpace ){
                DWORD dwWritten;
                OLE_HRT(m_inCache.DataAdd(pv, min(dwFreeSpace, dwSize), &dwWritten));

                dwSize -= dwWritten;
                if( pdwCopied ){
                    *pdwCopied += dwWritten;
                }
                pv = (LPBYTE)pv + dwWritten;
                ::SetEvent(m_hevOutReadReady);
            }
        }
        //wait only if it needs 
        if( 0 == dwSize )
            break;

        switch( ::WaitForMultipleObjects(
            sizeof(hhSignals)/sizeof(*hhSignals),
            hhSignals,
            FALSE,
            INFINITE))
        {
        case WAIT_OBJECT_0:
            //closed, user cancel from 3-d thread            
            OLE_HR = S_FALSE;
            goto M_EXIT;
        case WAIT_OBJECT_0+1:
            //read happens
            break;
        case WAIT_OBJECT_0+2:
            OLE_HR = S_FALSE;
            //seems error happens. details in m_ex
            goto M_EXIT;
        default:
            //system error
            OLE_HR = E_POINTER;
            goto M_EXIT;
        }
    }
M_EXIT: ;
    OLE_CATCH
    OLE_RETURN_HR
}

HRESULT CCabPacker::readInCache(
    void *pv, 
    DWORD dwSize, 
    DWORD *pdwCopied)
{
    OLE_DECL
    //read from cyclic buffer filled by Java calls
    HANDLE hhSignals[] = { m_hevClose, m_hevOutReadReady, m_hevEndOfEntry };
    if(pdwCopied){
        *pdwCopied = 0;
    }
    while(1){
        {
            //out cache locked operation
            OLESyncro _lk(m_hmtLock);           
            DWORD dwUsed;
            OLE_HRT(m_inCache.Used(&dwUsed));
            if( 0 < dwUsed ){
                DWORD dwRead;
                OLE_HRT(m_inCache.DataGet(pv, min(dwUsed, dwSize), &dwRead));

                dwSize -= dwRead;
                pv = (LPBYTE)pv + dwRead;
                if(pdwCopied){
                    *pdwCopied += dwRead;
                }
                ::SetEvent(m_hevOutWriteReady);
            }
        }
        //wait only if it needs 
        if(0 == dwSize)
            break;

        switch( ::WaitForMultipleObjects(
            sizeof(hhSignals)/sizeof(*hhSignals),
            hhSignals,
            FALSE,
            INFINITE))
        {
        case WAIT_OBJECT_0:
            //closed, user cancel from 3-d thread            
            OLE_HR = S_FALSE;
            goto M_EXIT;
        case WAIT_OBJECT_0+1:
            //write happens
            break;
        case WAIT_OBJECT_0+2:
            //no more read - end of file reached
            goto M_EXIT;
        default:
            //system error
            OLE_HR = E_POINTER;
            goto M_EXIT;
        }
    }
M_EXIT:     ;
    OLE_RETURN_HR
}


void CCabPacker::writeNativeBytes(
    JNIEnv *env, 
    jbyteArray buf, 
    jint off, 
    jint len)
{
    if( !checkValid(env) )
        return;
    OLE_DECL;
    DWORD dwCopied = (DWORD)-1;
    jbyte *pSrc = env->GetByteArrayElements(buf, NULL);
    if(NULL==pSrc){
        OLE_HR = E_OUTOFMEMORY;
    } else {
        OLE_HR = writeInCache(pSrc + off, len, &dwCopied);
        env->ReleaseByteArrayElements(buf, pSrc, JNI_ABORT);
    }
    HR2CABERR(env, OLE_HR, "cyclic buffer error", dwCopied);
    checkValid(env);
}


UINT CCabPacker::read(JStreamPointer *p, void *pv, UINT cb)
{
    ULONG read = (UINT)-1;
    OLE_TRY
    switch(p->m_eFileType){
    case JStreamPointer::FT::ft_temp:
        //temp file read
        OLE_HRT(p->m_Cache->Read(pv, cb, &read));
        break;
    case JStreamPointer::FT::ft_out:
        {
            //drop to out stream.
            LARGE_INTEGER dlibMove = {p->m_cbPos, 0};
            OLE_HRT(p->m_Cache->Seek(dlibMove, SEEK_SET, NULL));
            OLE_HRT(p->m_Cache->Read(pv, cb, &read));
            p->m_cbPos += read;
        }
        break;
    case JStreamPointer::FT::ft_in:
        OLE_HRT(readInCache(pv, cb, &read));
        break;
    }
    OLE_CATCH
    return HR2CABERR(m_env_nt, OLE_HR, "stream read error", read);
}

UINT CCabPacker::write(JStreamPointer *p, void *pv, UINT cb)
{
    ULONG written = (UINT)-1;
    OLE_TRY
    switch(p->m_eFileType){
    case JStreamPointer::FT::ft_temp:
        OLE_HRT(p->m_Cache->Write(pv, cb, &written));
        break;
    case JStreamPointer::FT::ft_out:
        {
            LARGE_INTEGER dlibMove = {p->m_cbPos, 0};
            OLE_HRT(p->m_Cache->Seek(dlibMove, SEEK_SET, NULL));
            OLE_HRT(p->m_Cache->Write(pv, cb, &written));
            p->m_cbPos += written;
        }
        break;
    case JStreamPointer::FT::ft_in:
        OLE_HR = E_NOTIMPL;
        break;
    }
    OLE_CATCH
    return HR2CABERR(m_env_nt, OLE_HR, "stream write error", written);
}

long CCabPacker::seek(JStreamPointer *p, long dist, int seektype)
{
    long newpos = -1;
    OLE_TRY
    switch(p->m_eFileType){
    case JStreamPointer::FT::ft_temp:
        {
            LARGE_INTEGER dlibMove = {dist, 0};
            ULARGE_INTEGER streamPos = {0, 0};
            OLE_HRT(p->m_Cache->Seek(dlibMove, seektype, &streamPos));
            newpos = streamPos.LowPart;
        }
        break;
    case JStreamPointer::FT::ft_out:
        switch(seektype){
        case SEEK_END:
            //bad seek, but it was used by MS
            //hope we are in tail
            p->m_cbPos += dist; 
            break;
        case SEEK_SET:
            p->m_cbPos = dist; 
            break;
        case SEEK_CUR:
            p->m_cbPos += dist; 
            break;
        }
        newpos = p->m_cbPos;
        break;
    case JStreamPointer::FT::ft_in:
        OLE_HR = E_NOTIMPL;
        break;
    }
    OLE_CATCH
    return HR2CABERR(m_env_nt, OLE_HR, "stream seek error", newpos);    
}

long CCabPacker::progress(UINT typeStatus, ULONG cb1, ULONG cb2)
{
    if (typeStatus == statusFile){
        //Compressing a block into a folder
        //cb2 = uncompressed size of block
        m_csize_progress += cb1;
        m_osize_progress += cb2;
        //STRACE1(_T("progress: %d->%d"), (int)cb1, (int)cb2);
    } else if( typeStatus == statusFolder ){
        //STRACE1(_T("progress: %d%% complite"),(int)(cb1*100/cb2) );
    }
    return 0;
}
int  CCabPacker::placed(PCCAB pccab, char *pszFile, long cbFile, BOOL fContinuation)
{
    static LPCSTR err = "error while store CRC and etc.";
    int cb = strlen(pszFile);
    CHAR last_ch = pszFile[cb-1];
    if( (m_hint & HINT_SORE_EINFO) && '/'!=last_ch && '\\'!=last_ch){
        pszFile[cb - strlen(m_pEntrySuffixStub)] = 0;
        jstring fn = m_env_nt->NewStringUTF(pszFile);
        if(NULL==fn){
            JNU_ThrowOutOfMemoryError(m_env_nt, err);
        } else {
            jstring suf = (jstring)m_env_nt->CallObjectMethod(
                m_this, 
                ms_jcidNativePackedOutputStream_getEntrySuffix,
                fn);
            if( NULL != suf ){
                LPSTR pEN = (LPSTR)malloc( 
                    ( strlen(pszFile) + strlen(m_pEntrySuffixStub) + 1)*sizeof(CHAR) ); 
                if( NULL == pEN){
                    JNU_ThrowOutOfMemoryError(m_env_nt, err);
                } else {
                    LPSTR pDot = strrchr(pszFile, '.');
                    if(NULL!=pDot){
                        strcpy(pEN, pDot);
                        *pDot = 0;
                    }
                    LPCSTR pSuf = m_env_nt->GetStringUTFChars(suf, NULL);
                    if( NULL == pSuf){
                        JNU_ThrowOutOfMemoryError(m_env_nt, err);
                    } else {
                        strcat(pszFile, pSuf);
                        if(NULL!=pDot){
                            strcat(pszFile, pEN);
                        }
                        m_env_nt->ReleaseStringUTFChars(suf, pSuf);
                    }
                    free(pEN);
                }
                m_env_nt->DeleteLocalRef(suf);
            }
            m_env_nt->DeleteLocalRef(fn);
        }
        if(m_env_nt->ExceptionCheck()){
            return -1;
        }
    }
    return 0;
}
