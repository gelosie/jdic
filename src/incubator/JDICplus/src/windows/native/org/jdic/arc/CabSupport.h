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
#ifndef _CABSUPPORT_H_
#define _CABSUPPORT_H_

#include "cache.h"

enum{
    HINT_IN_MEMORY = 0, //Hold all temporary buffers in memory
    HINT_ON_DISK = 1,   //Hold all temporary buffers in temp folder as files
    HINT_SORE_EINFO = 2 //Hold CRC32 and Compressed Size in entry name
};

HRESULT CreateStream(
    LPSTREAM *ppstm,
    long hint);

class CCabUnpacker : public CUnpackerExt {
public:
    enum CAB_PROP_EX {
        cab_next_file,
        cab_next_disk,
        cab_path,     
        cab_set_ID,   
        cab_number,   

        cab_torn_file,
        cab_prev_file,
        cab_prev_disk,
        cab_prop_count
    };
protected:
    //file-handler analogue
    struct JStreamPointer{
        CCabUnpacker *m_pCCabUnpacker;
        DWORD         m_cbPos;

        JStreamPointer(CCabUnpacker *pCCabUnpacker)
        : m_pCCabUnpacker(pCCabUnpacker),
        m_cbPos(0)  
        {
            //STRACE1("{ %08x", this);
        }
        ~JStreamPointer(){
            //STRACE1("} %08x", this);
        }
        
        inline UINT read(void *pv, UINT cb) {
            return m_pCCabUnpacker->read(this, pv, cb);
        }
        inline UINT write(void *pv, UINT cb) {
            return m_pCCabUnpacker->write(this, pv, cb);
        }
        inline long seek(long dist, int seektype) {
            return m_pCCabUnpacker->seek(this, dist, seektype);
        }
    };

private:
    HFDI            m_hfdi;  //CAB handler structure
    ERF             m_erf;   //error status 
    FDICABINETINFO  m_fdici; //cab info collector
    LPSTR           m_props[cab_prop_count]; 


    HANDLE          m_hCabThread;
    unsigned int    m_CabThreadId; 
    JNIEnv         *m_env_nt;
    BOOL            m_bFirstRead;
    jthrowable      m_ex;

    ZZ::CHandlerSup     m_hmtLock;
    ZZ::CHandlerSup     m_hevNewEntryFound;
    ZZ::CHandlerSup     m_hevLastEntryRead;
    ZZ::CHandlerSup     m_hevEndOfEntry;
    ZZ::CHandlerSup     m_hevOutWriteReady;
    ZZ::CHandlerSup     m_hevOutReadReady;
    ZZ::CHandlerSup     m_hevClose;
    IStreamPtr          m_inCache;
    CCache              m_outCache;

public:
    CCabUnpacker(
        JNIEnv *env, 
        jobject is,
        jlong   hint
    );
    virtual ~CCabUnpacker();
    virtual void close(JNIEnv *env);
    virtual bool checkValid(JNIEnv *env);
    virtual jint readNativeBytes(
        JNIEnv *env, 
        jbyteArray buf, 
        jint off, 
        jint len);
    virtual jobject readNextEntry(JNIEnv *env);
    virtual jstring getProperty(
        JNIEnv *env,
        jint iIndex);


private:
    static FNALLOC(mem_alloc) { return malloc(cb); } 
    static FNFREE(mem_free)   { free(pv); } 
    static FNOPEN(file_open)  { 
        //INT_PTR => WIN64 safe
        return (INT_PTR)new CCabUnpacker::JStreamPointer(
            (CCabUnpacker *)atop( pszFile )
        );    
    }
    static FNCLOSE(file_close){
        delete (JStreamPointer *)hf;
        return 0;
    }
    static FNREAD(file_read){
        return ((JStreamPointer *)hf)->read(pv, cb);
    }
    static FNWRITE(file_write){
        return ((JStreamPointer *)hf)->write(pv, cb);
    }
    static FNSEEK(file_seek){
        return ((JStreamPointer *)hf)->seek(dist, seektype);
    }
    static FNFDINOTIFY(notification_function){
        return ((CCabUnpacker *)pfdin->pv)->notification(fdint, pfdin);
    }

    void setProperty(int iIndex, LPSTR pValue);
    void setProperty(int iIndex, LONG iValue);
    void CreateThreadIfNeed();

    unsigned int Run();
    static unsigned  __stdcall  ThreadAction(void *pThis);

    void checkInCache(DWORD size);
    UINT read(JStreamPointer *p, void *pv, UINT cb);
    UINT write(JStreamPointer *p, void *pv, UINT cb);
    long seek(JStreamPointer *p, long dist, int seektype);
    HRESULT readOutCache(void *pv, DWORD dwSize, DWORD *pdwCopied);
    HRESULT writeOutCache(void *pv, DWORD dwSize, DWORD *pdwCopied);
    
    INT_PTR notification(FDINOTIFICATIONTYPE fdint, PFDINOTIFICATION pfdin);
    void signalNewEntrtyFound(
        LPCSTR lpEntryName = NULL, //no more sections in file
        USHORT time = (USHORT)-1, //undefined
        USHORT date = (USHORT)-1, //undefined
        int    oflag = (USHORT)-1,//undefined
        long   osize = (USHORT)-1 //undefined
    );
    //void ParseEntryName();
};

#define FNFCI_CHECK_RET_VALUE(TI, x) TI ret = (TI)(x);\
    if( ((TI)-1)==ret ) *err = errno;\
    return ret;

#define FNFCI_CHECK_RET_VALUE_D(TI, x) TI ret = (TI)(x);\
    if( ((TI)-1)==ret ) *err = errno;

//length is fixed by 5!!!
#define TEMP_NAME "$tmp$"
#define OUT_NAME "+JAVA"

class CCabPacker : public CPackerExt {
protected:
    //file-handler analogue
    struct JStreamPointer{
        enum FT {
            ft_temp,
            ft_out,
            ft_in
        };
        CCabPacker   *m_pCCabPacker;
        DWORD         m_cbPos;
        IStreamPtr    m_Cache;
        FT            m_eFileType;

        JStreamPointer(CCabPacker *pCCabPacker, FT eFileType)
        : m_pCCabPacker(pCCabPacker),
          m_eFileType(eFileType),
          m_cbPos(0)  
        {
            //STRACE1("{ %08x", this);
            OLE_TRY
            if(ft_temp == m_eFileType){  
                OLE_HRT(CreateStream(
                    &m_Cache,
                    m_pCCabPacker->m_hint
                ));
            } else if(ft_out == m_eFileType){
                m_Cache = m_pCCabPacker->m_outCache;
            }
            OLE_CATCH
        }
        ~JStreamPointer(){
            //STRACE1("} %08x", this);
        }
        
        inline UINT read(void *pv, UINT cb) {
            return m_pCCabPacker->read(this, pv, cb);
        }
        inline UINT write(void *pv, UINT cb) {
            return m_pCCabPacker->write(this, pv, cb);
        }
        inline long seek(long dist, int seektype) {
            return m_pCCabPacker->seek(this, dist, seektype);
        }
    };

private:
    HFCI            m_hfci;
    ERF             m_erf;
    CCAB            m_ccab;
    HANDLE          m_hCabThread;
    unsigned int    m_CabThreadId;
    JNIEnv         *m_env_nt;
    BOOL            m_bFirstRead;
    jthrowable      m_ex;

    IStreamPtr          m_outCache;
    CCache              m_inCache;

    ZZ::CHandlerSup     m_hmtLock;
    ZZ::CHandlerSup     m_hevClose;
    ZZ::CHandlerSup     m_hevCreateNewEntry;
    ZZ::CHandlerSup     m_hevDropToJavaOS;
    ZZ::CHandlerSup     m_hevDropToJavaOSComplite;
    ZZ::CHandlerSup     m_hmtEntryInProgress;
    ZZ::CHandlerSup     m_hevCompressionStarted;
    ZZ::CHandlerSup     m_hevEndOfEntry;
    ZZ::CHandlerSup     m_hevOutWriteReady;
    ZZ::CHandlerSup     m_hevOutReadReady;

    //model for attribute extension in file name 
    //placeholder
    LPSTR     m_pEntrySuffixStub; 
public:
    CCabPacker(
        JNIEnv *env, 
        jobject othis,
        jobject os,
        jlong   hint
    );
    virtual ~CCabPacker();
    virtual void close(JNIEnv *env);
    virtual void finish(JNIEnv *env);
    virtual bool checkValid(JNIEnv *env);
    virtual void writeNativeBytes(
        JNIEnv *env, 
        jbyteArray buf, 
        jint off, 
        jint len);
    virtual void putNextEntry(
        JNIEnv *env, 
        jobject ze, 
        jint level);
    virtual jlong closeEntry(
        JNIEnv *env,
        jlong crc);


private:
    static FNFCIALLOC(mem_alloc) { 
        return malloc(cb); 
    } 
    static FNFCIFREE(mem_free)   { 
        free(memory); 
    } 
    static FNFCIOPEN(fci_open)  { 
        //INT_PTR => WIN64 safe
        //WARNING! 
        //second attempt to open the same temp-file 
        //and seek over input stream do not covered!
        INT_PTR p = (INT_PTR) new JStreamPointer(
            (CCabPacker *)pv,
            (0==strncmp(TEMP_NAME, pszFile, 5))
                ?  JStreamPointer::FT::ft_temp
                :  (0==strncmp(OUT_NAME, pszFile, 5))
                    ? JStreamPointer::FT::ft_out 
                    : JStreamPointer::FT::ft_in);
        if( NULL==p ){
            *err = ENOMEM;
            return -1;
        }
        return p;
    }                              
    static FNFCICLOSE(fci_close){
        delete (JStreamPointer *)hf;
        return 0;
    }
    static FNFCIDELETE(fci_delete){
        //do nothing: we are using IStream
        return 0;
    }
    static FNFCIREAD(fci_read){
        FNFCI_CHECK_RET_VALUE(
            UINT,
            ((JStreamPointer *)hf)->read(memory, cb)
        );
    }
    static FNFCIWRITE(fci_write){
        FNFCI_CHECK_RET_VALUE(
            UINT,
            ((JStreamPointer *)hf)->write(memory, cb)
        );
    }
    static FNFCISEEK(fci_seek){
        FNFCI_CHECK_RET_VALUE(
            long,
            ((JStreamPointer *)hf)->seek(dist, seektype)
        );
    }
    static FNFCIFILEPLACED(fci_placed){
        //STRACE1( "File '%s' (size %d) stored in '%s'", pszFile, cbFile, pccab->szCab);
        //if(fContinuation)
        //    STRACE1("(Above file is a later segment of a continued file)");
        //return 0;
        return ((CCabPacker *)pv)->placed(pccab, pszFile, cbFile, fContinuation);
    }
    static FNFCISTATUS(progress){
        return ((CCabPacker *)pv)->progress(typeStatus, cb1, cb2);
    }

    static FNFCIGETNEXTCABINET(fci_next_cabinet)
    {
        //Cabinet counter has been incremented already by FCI
        //Store/generate next cabinet name from counter here
        //like sprintf(pccab->szCab, "TEST%d.CAB", pccab->iCab);
        //return TRUE;
        return FALSE;//multi-cabs are not supported!
    }


    static FNFCIGETOPENINFO(fci_open_info)
    {
        CCabPacker *pThis = (CCabPacker *)pv;
        *pdate = HIWORD(pThis->m_dos_time);
        *ptime = LOWORD(pThis->m_dos_time);
        //(int) (attrs & (_A_RDONLY | _A_SYSTEM | _A_HIDDEN | _A_ARCH));
        *pattribs = _A_ARCH;
        return fci_open(pszName, _O_RDONLY | _O_BINARY, _S_IREAD | _S_IWRITE, err, pv);
    }


    static FNFCIGETTEMPFILE(fci_temp_file){
        if ( 30 < cbTempName ) {
            static long iCounter = 0;
            strcpy(pszTempName, TEMP_NAME);        // Copy to caller's buffer
            itoa(iCounter++, pszTempName+5, 10);
            return TRUE;
        }
        return FALSE;
    }

    //internal helpers
    TCOMP GetCompressionType();
    void CreateThreadIfNeed();
    unsigned int Run();
    static unsigned  __stdcall  ThreadAction(void *pThis);
    UINT read(JStreamPointer *p, void *pv, UINT cb);
    UINT write(JStreamPointer *p, void *pv, UINT cb);
    long seek(JStreamPointer *p, long dist, int seektype);
    HRESULT writeInCache(void *pv, DWORD dwSize, DWORD *pdwCopied);
    HRESULT readInCache(void *pv, DWORD dwSize, DWORD *pdwCopied);
    HRESULT DropJavaStream(JNIEnv *env);
    long progress(UINT typeStatus, ULONG cb1, ULONG cb2);
    int  placed(PCCAB pccab, char *pszFile, long cbFile, BOOL fContinuation);
};


#endif// _CABSUPPORT_H_

