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

#ifndef _CACH_H_
#define _CACH_H_

class CCache{
public:
    DWORD  m_dwCacheSize;    // Cache size (in bytes)
    BYTE   *m_pData;         // pointer to the data buffer
    DWORD  m_dwStartOffset;  // starting offset of cache
    DWORD  m_dwData;         // amount of valid data
    HRESULT MakeSureTheresMemory();

public:
    CCache(DWORD dwCacheSize);
    ~CCache();
    HRESULT DataGet(void *pMem, DWORD dwSize, DWORD *pdwCopied);
    HRESULT DataAdd(const void *pMem, DWORD dwSize, DWORD *pdwCopied);
    HRESULT DataAddCycle(const void *pMem, DWORD dwSize, DWORD *pdwCopied);
    HRESULT DataFlush();
    HRESULT Space(DWORD *pdwFreeSpace);
    HRESULT Used(DWORD *pdwFilledSpace);
    HRESULT GetSize(DWORD *pdwCacheSize);
    HRESULT SetSize(DWORD dwCacheSize);
};
typedef CCache *PCCache;

class CWrapperStream: public IStream
{
public:
    //ISequentialStream
    virtual  HRESULT STDMETHODCALLTYPE Read(
        OUT void *pv,
        IN  ULONG cb,
        OUT ULONG *pcbRead)
    {
       return m_ps->Read(pv, cb, pcbRead);
    }

    virtual  HRESULT STDMETHODCALLTYPE Write(
        IN const void *pv,
        IN ULONG cb,
        OUT ULONG *pcbWritten)
    {
       return m_ps->Write(pv, cb, pcbWritten);
    }


    //IStream
    virtual  HRESULT STDMETHODCALLTYPE Seek(
        IN LARGE_INTEGER dlibMove,
        IN DWORD dwOrigin,
        OUT ULARGE_INTEGER *plibNewPosition)
    {
        return m_ps->Seek(dlibMove, dwOrigin, plibNewPosition);
    }

    virtual HRESULT STDMETHODCALLTYPE SetSize(
        IN ULARGE_INTEGER libNewSize)
    {
        return m_ps->SetSize(libNewSize);
    }

    virtual  HRESULT STDMETHODCALLTYPE CopyTo(
        IN  IStream* pstm,
        IN  ULARGE_INTEGER cb,
        OUT ULARGE_INTEGER* pcbRead,
        OUT ULARGE_INTEGER* pcbWritten)
    {
        return m_ps->CopyTo(pstm, cb, pcbRead, pcbWritten);
    }

    virtual HRESULT STDMETHODCALLTYPE Commit(IN DWORD grfCommitFlags)
    {
        return m_ps->Commit(grfCommitFlags);
    }
    virtual HRESULT STDMETHODCALLTYPE Revert()
    {
        return m_ps->Revert();
    }

    virtual HRESULT STDMETHODCALLTYPE LockRegion(
        IN ULARGE_INTEGER libOffset, 
        IN ULARGE_INTEGER cb, 
        IN DWORD dwLockType)
    {
        return m_ps->LockRegion(libOffset, cb, dwLockType);
    }

    virtual HRESULT STDMETHODCALLTYPE UnlockRegion(
        IN ULARGE_INTEGER libOffset, 
        IN ULARGE_INTEGER cb, 
        IN DWORD dwLockType)
    {
        return m_ps->UnlockRegion(libOffset, cb, dwLockType);
    }


    virtual HRESULT STDMETHODCALLTYPE Stat(
        OUT STATSTG *pstatstg, 
        IN DWORD grfStatFlag)  
    {
        return m_ps->Stat(pstatstg, grfStatFlag);
    }

    virtual HRESULT STDMETHODCALLTYPE Clone(OUT IStream**)
    {
        return E_NOTIMPL;
    }

    IStream *m_ps;

    CWrapperStream(IN IStream *ps = NULL, BOOL bAddRef = FALSE)
    : m_ps(ps)
    {
        if(NULL!=m_ps && bAddRef)
            m_ps->AddRef();
    }

    virtual ~CWrapperStream()
    {
       if(m_ps) 
           m_ps->Release();
    }
};

class CTempFileStream: public CWrapperStream
{
public:
    //IUnknown
    virtual HRESULT STDMETHODCALLTYPE QueryInterface( 
            IN REFIID riid,
            OUT void **ppvObject)
    {
        if( ::IsEqualIID(IID_IUnknown,riid) || ::IsEqualIID(IID_IStream,riid) ){
            *ppvObject = (void *)this;
            AddRef();
            return S_OK;
        }
        return E_NOINTERFACE;
    }
    virtual ULONG STDMETHODCALLTYPE AddRef(){
        return (ULONG)::InterlockedIncrement( (LPLONG)&m_cRef );
    }
    virtual ULONG STDMETHODCALLTYPE Release(){
        ULONG cRef = (ULONG)::InterlockedDecrement( (LPLONG)&m_cRef );
        if(cRef == 0)
            delete this;
        return cRef;
    }

    CTempFileStream()
    : CWrapperStream(NULL),
      m_pTempName(NULL),
      m_cRef(1)
    {}

    HRESULT Create(        
        LPCTSTR dir,
        LPCTSTR pfx);

    virtual ~CTempFileStream();
protected:
    LPTSTR m_pTempName;
    ULONG  m_cRef;
};

#endif//_CACH_H_
