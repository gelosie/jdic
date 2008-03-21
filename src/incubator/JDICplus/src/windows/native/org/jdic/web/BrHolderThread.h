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
DWORD  OLE_CoGetThreadingModel();

HRESULT  OLE_CoWaitForMultipleHandles(
  IN DWORD dwFlags,
  IN DWORD dwMilliseconds,
  IN ULONG nCount,
  IN LPHANDLE lpHandles,
  OUT LPDWORD lpdwIndex
);

DWORD  OLE_CoWaitForSingleObject(
    IN HANDLE hHandle,
    IN DWORD dwMilliseconds
);

DWORD  OLE_CoWaitForMultipleObjects(
  IN DWORD nCount,
  IN LPHANDLE lpHandles,
  IN BOOL bWaitAll,
  IN DWORD dwMilliseconds
);

void  OLE_CoSleep(IN DWORD dwMilliseconds);

void  OLE_CoPump();

struct COLEHolder
{
    COLEHolder()
    : m_hr(::OleInitialize(NULL))
    {}

    ~COLEHolder(){
        if(SUCCEEDED(m_hr))
            ::OleUninitialize();
    }
    HRESULT m_hr;
};

struct BrowserAction
{
    virtual HRESULT Do(JNIEnv *env) = 0;
    virtual void throwExeption(JNIEnv *env, const char *msg, HRESULT hr) {
        ThrowJNIErrorOnOleError(env, hr, msg);
    };
};

#define _HP_ OLESyncro __hpsync__(ms_hAtomMutex);
struct BrowserThread
{
private:
    ULONG m_cRef;
    HANDLE m_hThread;
    unsigned int  m_dwThreadId;
    ZZ::CHandlerSup m_hStart;
    ZZ::CHandlerSup m_hTerminate;
    ZZ::CHandlerSup m_hDone;
    ZZ::CHandlerSup m_hDo;
    static ZZ::CHandlerSup ms_hAtomMutex;

    //params
    BrowserAction *pAction;

    static unsigned __stdcall RunMe(LPVOID pThis){
        return ((BrowserThread *)pThis)->RunEx();
    }

    BrowserThread()
        :m_hStart(::CreateEvent(NULL, FALSE, FALSE, NULL)),
        m_hTerminate(::CreateEvent(NULL, FALSE, FALSE, NULL)),
        m_hDone(::CreateEvent(NULL, FALSE, FALSE, NULL)),
        m_hDo(::CreateEvent(NULL, FALSE, FALSE, NULL)),
        m_hThread(NULL),
        m_dwThreadId(0),
        m_hrAction(S_OK),
        m_cRef(0)
    {}

public:
    HRESULT      m_hrAction;

    ULONG AddRef(){
        return (ULONG)::InterlockedIncrement( (LPLONG)&m_cRef );
    }
    ULONG Release(){
        ULONG cRef = (ULONG)::InterlockedDecrement( (LPLONG)&m_cRef );
        if(cRef == 0)
            delete this;
        return cRef;
    }
    static BrowserThread &GetInstance()
    {
        //hack! static is not sync!
        //_HP_
        static BrowserThread hp;
        return hp;
    }

    ~BrowserThread()
    {
        Terminate();
    }

    unsigned RunEx(){
        STRACE1(_T("{Processor"));
        JNIEnv *env_nt = NULL;
        unsigned int ret = (unsigned int)jvm->AttachCurrentThread(
            (void**)&env_nt,
            NULL
        );
        if(env_nt){
            COLEHolder oh;
            {
                ::SetEvent(m_hStart);

                HANDLE hhActions[] = {
                    m_hTerminate,
                    m_hDo
                };
                while(1){
                    DWORD dwAction = OLE_CoWaitForMultipleObjects(
                        sizeof(hhActions)/sizeof(hhActions[0]),
                        hhActions,
                        FALSE,
                        INFINITE
                    );
                    switch(dwAction){
                    case WAIT_OBJECT_0:
                        goto M_DONE;
                    case WAIT_OBJECT_0 + 1:
                        m_hrAction = pAction->Do(env_nt);
                        env_nt->ExceptionClear();
                        break;
                    }
                    ::SetEvent(m_hDone);
                }
    M_DONE:
                STRACE1(_T("}Processor"));
            }
            jvm->DetachCurrentThread();
        }
        return m_hrAction;
    }

    HRESULT MakeAction(JNIEnv *env, const char *msg, BrowserAction &Action)
    {
        if(GetCurrentThreadId()==m_dwThreadId){
            return Action.Do(env);
        }
        _HP_
        if(NULL==m_hThread){
            Restart();
        }
        pAction = &Action;
        ::SetEvent(m_hDo);
        ::WaitForSingleObject(m_hDone, INFINITE);
        if(FAILED(m_hrAction)){
            if( 0x80010105==m_hrAction || 0x80070005==m_hrAction ){
                //Server made critical error - it is not alive any more
                Restart();
            }
            pAction->throwExeption(env, msg, m_hrAction);
        }
        return m_hrAction;
    }

    void Terminate()
    {
        _HP_
        if(m_hThread){
            ::SetEvent(m_hTerminate);
            ::WaitForSingleObject(m_hThread, INFINITE);
            ::CloseHandle(m_hThread);
            m_hThread = NULL;
            m_dwThreadId = 0;
        }
    }

    void Restart()
    {
        _HP_
        Terminate();
        m_hThread = (HANDLE)_beginthreadex(
            NULL,
            0,
            RunMe,
            this,
            0,
            &m_dwThreadId);
        ::WaitForSingleObject(m_hStart, INFINITE);
    }
};