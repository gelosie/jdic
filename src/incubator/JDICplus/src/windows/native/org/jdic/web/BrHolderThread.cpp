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
#include "BrHolderThread.h"

ZZ::CHandlerSup BrowserThread::ms_hAtomMutex(CreateMutex(NULL, FALSE, NULL));

DWORD OLE_CoGetThreadingModel()
{
    return COINIT_APARTMENTTHREADED;
}

HRESULT OLE_CoWaitForMultipleHandles(
    IN DWORD dwFlags,
    IN DWORD dwMilliseconds,
    IN ULONG nCount,
    IN LPHANDLE lpHandles,
    OUT LPDWORD lpdwIndex
){
    HRESULT hr = S_OK;
    DWORD dwStartTime = ::GetTickCount();
    DWORD dwRes = ::WaitForMultipleObjects(
        nCount,         // number of handles in the handle array
        lpHandles,      // pointer to the object-handle array
        (dwFlags&COWAIT_WAITALL) == COWAIT_WAITALL,       // wait for all or wait for one
        0);
    if( WAIT_FAILED == dwRes ) {
        STRACE1(TEXT("WaitForMultipleObjects[%08x]"), hr = HRESULT_FROM_WIN32(::GetLastError()) );
    } else if( WAIT_TIMEOUT == dwRes ) {
        while( SUCCEEDED(hr) ) {
            dwRes = ::MsgWaitForMultipleObjects(
                nCount,         // number of handles in the handle array
                lpHandles,      // pointer to the object-handle array
                (dwFlags&COWAIT_WAITALL) == COWAIT_WAITALL,       // wait for all or wait for one
                dwMilliseconds, // time-out interval in milliseconds
                /*QS_POSTMESSAGE|QS_SENDMESSAGE|QS_PAINT*/
                QS_ALLINPUT);// type of input events to wait for
            if( WAIT_FAILED == dwRes) {
                STRACE1(TEXT("MsgWaitForMultipleObjects[%08x]"), hr = HRESULT_FROM_WIN32(::GetLastError()) );
            } else if( WAIT_TIMEOUT == dwRes) {
                hr = RPC_S_CALLPENDING;
            } else if( (WAIT_OBJECT_0 + nCount) == dwRes ) {
                DWORD dwCurTime = ::GetTickCount();
                DWORD dwDeltaTime = dwCurTime - dwStartTime;
                MSG msg;
                while( ::PeekMessage(&msg,NULL,0,0,PM_REMOVE) ) {
                    ::TranslateMessage(&msg);
                    ::DispatchMessage(&msg);
                    dwCurTime = ::GetTickCount();
                    dwDeltaTime = dwCurTime - dwStartTime;
                    if(dwDeltaTime>dwMilliseconds){
                        hr = RPC_S_CALLPENDING;
                        break;
                    }
                }
                if( SUCCEEDED(hr) ) {
                    if(dwDeltaTime>dwMilliseconds)
                        hr = RPC_S_CALLPENDING;
                    else{
                        if(INFINITE!=dwMilliseconds){
                            dwMilliseconds -= dwDeltaTime;
                        }
                        dwStartTime = dwCurTime;
                    }
                }
            } else {
                *lpdwIndex = dwRes - WAIT_OBJECT_0;
                break;
            }
        }
    }else{
        *lpdwIndex = dwRes - WAIT_OBJECT_0;
    }
    return hr;
}

DWORD OLE_CoWaitForSingleObject(
    IN HANDLE hHandle,
    IN DWORD dwMilliseconds)
{
    return OLE_CoWaitForMultipleObjects(
        1,
        &hHandle,
        FALSE,
        dwMilliseconds);
}

DWORD OLE_CoWaitForMultipleObjects(
    IN DWORD nCount,
    IN LPHANDLE lpHandles,
    IN BOOL bWaitAll,
    IN DWORD dwMilliseconds)
{
    DWORD dwRes;
    if( COINIT_APARTMENTTHREADED==OLE_CoGetThreadingModel() ){
        HRESULT hr;
        DWORD dwIndex = 0;
        hr = OLE_CoWaitForMultipleHandles(
            bWaitAll?COWAIT_WAITALL:0,
            dwMilliseconds,
            nCount,
            (LPHANDLE)lpHandles,
            &dwIndex
            );
        if(RPC_S_CALLPENDING==hr || RPC_E_TIMEOUT==hr){
            dwRes = WAIT_TIMEOUT;
        }else if(S_OK==hr){
            dwRes = WAIT_OBJECT_0 + dwIndex;
        }else{
            ::SetLastError(hr);
            dwRes = WAIT_FAILED;
        }
    }else{
        dwRes = ::WaitForMultipleObjects(
            nCount,
            lpHandles,
            bWaitAll,
            dwMilliseconds);
    }
    return dwRes;
}

void OLE_CoSleep(IN DWORD dwMilliseconds)
{
    if( COINIT_APARTMENTTHREADED==OLE_CoGetThreadingModel() ){
        HANDLE hEvent = ::CreateEvent(NULL,FALSE,FALSE,NULL);
        if(!hEvent){
            STRACE1(TEXT("CreateEvent[%08x]"), HRESULT_FROM_WIN32(::GetLastError()));
        }else{
            ::OLE_CoWaitForSingleObject(hEvent, dwMilliseconds);
            ::CloseHandle(hEvent);
        }
    }else{
        ::Sleep(dwMilliseconds);
    }
}

void OLE_CoPump()
{
    MSG msg;
    while( ::PeekMessage(&msg,NULL,0,0,PM_REMOVE) ) {
        ::TranslateMessage(&msg);
        ::DispatchMessage(&msg);
    }
}
