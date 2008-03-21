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
#include "cache.h"
#include <shlwapi.h>

//////////////////////////////////////////////////////////////////////////
// class CCache
//
#define  MIN_CACHE_SIZE  64
CCache::CCache(DWORD dwCacheSize)
{
    m_dwCacheSize = max(dwCacheSize,MIN_CACHE_SIZE);
    m_pData = NULL;
    m_dwStartOffset = 0;
    m_dwData = 0;
}


// CCache::~CCache - Free up the cache object
CCache::~CCache()
{
    if(m_pData){
        free(m_pData);
    }
}


// CCache::MakeSureTheresMemory - This makes sure that there's memory
//    allocated for the object. If it fails (and theres no memory)
//    then this returns a HRESULT.
// 
// returns
//    HRESULT - 0 if there's memory. error if an error occured.
HRESULT CCache::MakeSureTheresMemory()
{
    if( m_pData ) {
        return S_OK;
    }
    m_pData = (LPBYTE)malloc(m_dwCacheSize);
    return m_pData
       ? S_OK
       : E_OUTOFMEMORY;
}


// CCache::DataGet - This gets data from the beginning (oldest part) of
//    the cache. It then removes that memory from the cache.
// 
// inputs
//    PVOID    pMem - Memory to fill in
//    DWORD    dwSize - Number of bytes to fill in.
//    DWORD    *pdwCopied - Number of bytes actually copied.
// returns
//    HRESULT - Error value
HRESULT CCache::DataGet(
    void *pMem, 
    DWORD dwSize, 
    DWORD *pdwCopied)
{
    HRESULT hr;
    DWORD   dwTemp;

    if(hr = MakeSureTheresMemory()){
        *pdwCopied = 0;
        return hr;
    }

    if(dwSize > m_dwData) dwSize = m_dwData;

    // since this is a circular buffer, copy the first half
    dwTemp = min(m_dwCacheSize - m_dwStartOffset, dwSize);
    memcpy (pMem, m_pData + m_dwStartOffset, dwTemp);
    pMem = (void*) ((LPBYTE)pMem + dwTemp);

    // copy the second half if there was any
    if((dwSize + m_dwStartOffset) >= m_dwCacheSize) {
        dwTemp = dwSize + m_dwStartOffset - m_dwCacheSize;
        memcpy(pMem, m_pData, dwTemp);
    }

    // update the values
    m_dwStartOffset = (m_dwStartOffset + dwSize) % m_dwCacheSize;
    m_dwData -= dwSize;
    if(pdwCopied) *pdwCopied = dwSize;

    // done
    return S_OK;
}


// CCache::DataAdd - This adds data to the end (newest part) of
//    the cache.
// 
// inputs
//    PVOID    pMem - Memory to copy over
//    DWORD    dwSize - Number of bytes to copy over
//    DWORD    *pdwCopied - Number of bytes actually copied.
// returns
//    HRESULT - Error value
HRESULT CCache::DataAdd(
    const void *pMem, 
    DWORD dwSize, 
    DWORD *pdwCopied)
{
    HRESULT  hr;
    DWORD    dwTemp, dwCopyTo;

    if( hr = MakeSureTheresMemory()) {
        if(pdwCopied) 
            *pdwCopied = 0;
        return hr;
    }

    if (dwSize > (m_dwCacheSize - m_dwData))
        dwSize = m_dwCacheSize - m_dwData;

    // since this is a circular buffer, copy the first half
    dwCopyTo = (m_dwStartOffset + m_dwData) % m_dwCacheSize;
    dwTemp   = ((dwSize + dwCopyTo) >= m_dwCacheSize) 
        ? m_dwCacheSize - dwCopyTo
        : dwSize;
    memcpy(m_pData + dwCopyTo, pMem, dwTemp);
    pMem = (const void*) ((LPBYTE)pMem + dwTemp);

    // copy the second half if there was any
    if ((dwSize + dwCopyTo) >= m_dwCacheSize) {
        dwTemp = dwCopyTo + dwSize - m_dwCacheSize;
        memcpy(m_pData, pMem, dwTemp);
    }

    // update the values
    m_dwData  += dwSize;
    if(pdwCopied) {
        *pdwCopied = dwSize;
    }

    // done
    return S_OK;
}

HRESULT CCache::DataAddCycle(
    const void *pMem, 
    DWORD dwSize, 
    DWORD *pdwCopied)
{
    HRESULT  hr;
    DWORD    dwTemp, dwCopyTo;

    if(hr = MakeSureTheresMemory()) {
        if(pdwCopied) {
            *pdwCopied = 0;
        }
        return hr;
    }

    DWORD dwSSize = dwSize;
    if(dwSize > m_dwCacheSize){
        pMem = (const void*) ((LPBYTE)pMem + dwSize - m_dwCacheSize);
        dwSize = m_dwCacheSize;
    }

    // since this is a circular buffer, copy the first half
    dwCopyTo = (m_dwStartOffset + m_dwData) % m_dwCacheSize;
    dwTemp   = ((dwSize + dwCopyTo) >= m_dwCacheSize) 
        ? m_dwCacheSize - dwCopyTo
        : dwSize;
    memcpy(m_pData + dwCopyTo, pMem, dwTemp);
    pMem = (const void*) ((LPBYTE)pMem + dwTemp);

    // copy the second half if there was any
    if((dwSize + dwCopyTo) >= m_dwCacheSize) {
        dwTemp = dwCopyTo + dwSize - m_dwCacheSize;
        memcpy(m_pData, pMem, dwTemp);
    }

    // update the values
    if(dwSSize > (m_dwCacheSize - m_dwData)) {
        m_dwData  = m_dwCacheSize;
    } else {
        m_dwData  += dwSSize;
    }
    if( pdwCopied ) {
        *pdwCopied = dwSSize;
    }

    // done
    return S_OK;
}


// CCache::DataFlush - This flushes out any data in the queue.
// 
// inputs
//    void
// returns
//    HRESULT - Error value
HRESULT CCache::DataFlush(void)
{
    m_dwData = 0;
    return S_OK;
}



// CCache::Space - This returns the amount of free space in the queue.
// 
// inputs
//    DWORD * pdwSpace - Filled in with the amount of free space.
// returns
//    HRESULT - Error value
HRESULT CCache::Space(DWORD *pdwFreeSpace)
{
    *pdwFreeSpace = m_dwCacheSize - m_dwData;
    return S_OK;
}


// CCache::Used - This returns the amount of used space in the queue.
// 
// inputs
//    DWORD * pdwSpace - Filled in with the amount of used space.
// returns
//    HRESULT - Error value
HRESULT CCache::Used(DWORD *pdwFilledSpace)
{
    *pdwFilledSpace = m_dwData;
    return S_OK;
}


// CCache::GetSize - This returns the cache size.
// 
// inputs
//    DWORD * pdwCacheSize - Filled in with the cache size.
// returns
//    HRESULT - Error value
HRESULT CCache::GetSize(DWORD *pdwCacheSize)
{
    *pdwCacheSize = m_dwCacheSize;
    return S_OK;
}


// CCache::SetSize - This returns the cache size.
// 
// inputs
//    DWORD dwCacheSize - New cache size.
// returns
//    HRESULT - Error value
HRESULT CCache::SetSize(DWORD dwCacheSize)
{
    dwCacheSize = max(dwCacheSize,MIN_CACHE_SIZE);  
    if( m_pData == NULL ){
        //if was not used before...
        m_dwCacheSize = dwCacheSize;
        return S_OK;
    }

    //else we should store old buffer...
    LPBYTE pData = (LPBYTE)malloc(dwCacheSize);
    if(!pData) {
        return E_OUTOFMEMORY;
    }    

    DWORD dwCopiedData;
    DWORD dwData = min(m_dwData, dwCacheSize);
    HRESULT hr = DataGet(pData, dwData, &dwCopiedData);
    if(SUCCEEDED(hr)) {
        free(m_pData);
        m_pData = pData;     
        m_dwData = dwCopiedData;
        m_dwCacheSize = dwCacheSize;
        m_dwStartOffset = 0;
    } else {
        free(pData);
    }    
    return hr;
}

HRESULT CTempFileStream::Create(
        LPCTSTR dir,
        LPCTSTR pfx
){
    OLE_DECL
    if( NULL != m_ps ){
        m_ps->Release();
        m_ps = NULL;
    }
    m_pTempName = _ttempnam(dir, pfx);
    if( NULL == m_pTempName ){
        OLE_HR = E_NOTIMPL;
    } else {
        //STGM_DELETEONRELEASE not implemented by MS :(
        OLE_HR = SHCreateStreamOnFile(
            m_pTempName,
            STGM_READWRITE | STGM_CREATE | STGM_SHARE_EXCLUSIVE,
            &m_ps
        );
    }
    OLE_RETURN_HR;
}
CTempFileStream::~CTempFileStream()
{
    //have to release stream first
    if( NULL != m_ps ){
        m_ps->Release();
        m_ps = NULL;
    }
    if( NULL != m_pTempName ){
        DeleteFile(m_pTempName);
        free(m_pTempName);
    }
}