/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
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

#include <stdio.h>
#include <string.h>
#include "MsgServer.h"
#include "Message.h"
#include "Util.h"

MsgServer gMessenger;

#ifdef WIN32
CRITICAL_SECTION CriticalSection; 
#else
pthread_mutex_t gServerMutex;
#endif

int MsgServer::mPort = 0;

MsgServer::MsgServer()
{
#ifdef WIN32
    WSADATA wsaData;
    if (WSAStartup(WINSOCK_VERSION, &wsaData)) {
        WBTRACE("Can't load Windows Sockets DLL!\n");
    }
#endif

    mFailed = 1;
    mCounter = 0;

    mHandler = NULL;
    mSendBuffer = new char[BUFFER_SIZE];
    mRecvBuffer = new char[BUFFER_SIZE];
    // predefine the long receiver buffer. If it's not big enough, 
    // alloc more space.
    mLongRecvBufferSize = BUFFER_SIZE * 4;
    mLongRecvBuffer = new char[mLongRecvBufferSize];
    mSendBuffer[0] = mRecvBuffer[0] = mLongRecvBuffer[0] = 0;

    int i;
    mTriggers = new Trigger[MAX_TRIGGER];
    for (i = 0; i < MAX_TRIGGER; i++) {
        mTriggers[i].mInstance = EMPTY_TRIGGER;
    }

    mServerSock = -1;
    mMsgSock = -1;

    FD_ZERO(&readfds); 
    FD_ZERO(&writefds); 
    FD_ZERO(&exceptfds); 

#ifdef WIN32
    InitializeCriticalSection(&CriticalSection);
#else
    pthread_mutex_init(&gServerMutex,NULL);
#endif
}

MsgServer::~MsgServer()
{
#ifdef WIN32
    DeleteCriticalSection(&CriticalSection);
#else
    pthread_mutex_destroy(&gServerMutex);
#endif

    delete [] mSendBuffer;
    delete [] mRecvBuffer;
    delete [] mLongRecvBuffer;
    delete [] mTriggers;

    WBTRACE("Closing socket ...\n");

    if (mServerSock >= 0) {
#ifdef WIN32
        closesocket(mServerSock);
#else
        close(mServerSock);
#endif
    }
    
    if (mMsgSock >= 0) {
#ifdef WIN32
        closesocket(mMsgSock);
#else
        close(mMsgSock);
#endif
    }

#ifdef WIN32
        WSACleanup();
#endif
}

int MsgServer::CreateServerSocket()
{
    u_long nbio = 1;
    int bReuseaddr = 1;

    mServerSock = socket(AF_INET, SOCK_STREAM, 0);

#ifdef WIN32
    if (mServerSock == INVALID_SOCKET) {
#else
    if (mServerSock < 0) {
#endif
        WBTRACE("socket failed!");
        goto failed;
    }

#ifdef WIN32
    ioctlsocket(mServerSock, FIONBIO, &nbio);
#else
    fcntl(mServerSock, F_SETFL, O_NONBLOCK);
#endif

    setsockopt(mServerSock, SOL_SOCKET, SO_REUSEADDR, 
        (const char*)&bReuseaddr, sizeof(bReuseaddr));

    struct sockaddr_in server_addr;
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(mPort);
    server_addr.sin_addr.s_addr = htonl (INADDR_LOOPBACK);

    if (bind(mServerSock, (struct sockaddr *)&server_addr, 
        sizeof(server_addr)) < 0) {
        LogMsg("bind failed!");
        goto failed;
    } 

    if (listen(mServerSock, MAX_CONN) == -1) {
        LogMsg("listen failed!");
        goto failed;
    }  

    WBTRACE("Listening port %d ...\n", mPort);

    mFailed = 0;
    return 0;

failed:
    WBTRACE("CreateServerSocket failed!");

#ifdef WIN32
    closesocket(mServerSock);
#else
    close(mServerSock);
#endif

    return -1;
}

int MsgServer::Send(const char *pData)
{
    if (strlen(pData) + strlen(mSendBuffer) < BUFFER_SIZE) {
        strcat(mSendBuffer, pData);
        return 0;
    }
    else {
        //buffer overflow
        return -1;
    }
}

int MsgServer::AddTrigger(int instance, int msg, int *trigger)
{
    for (int i = 0; i < MAX_TRIGGER; i++) {
        if (mTriggers[i].mInstance == EMPTY_TRIGGER) {
            mTriggers[i].mInstance = instance;
            mTriggers[i].mMsg = msg;
            mTriggers[i].mTrigger = trigger;
            return 0;
        }
    }

    //no more room
    return -1;
}

int MsgServer::Listen()
{
    if (mFailed)
        return -1;

    mCounter++;

    int ret = 0;

    if (mCounter >= 200 && mMsgSock < 0) {
        // haven't received any connection request after 200 times. quit
        return -1;
    }

    FD_ZERO(&readfds);
    FD_ZERO(&writefds);
    FD_ZERO(&exceptfds);

#ifdef WIN32
    // Type cast to avoid warning message:
    //   warning C4018: '==' : signed/unsigned mismatch
    FD_SET((UINT32)mServerSock, &readfds);
    FD_SET((UINT32)mServerSock, &writefds);
    FD_SET((UINT32)mServerSock, &exceptfds);
#else
    FD_SET(mServerSock, &readfds);
    FD_SET(mServerSock, &writefds);
    FD_SET(mServerSock, &exceptfds);
#endif

    // the value of the highest file descriptor plus one.
    int maxfdp1 = mServerSock + 1;

    if (mMsgSock >= 0) {
#ifdef WIN32
        // Type cast to avoid warning message:
        //   warning C4018: '==' : signed/unsigned mismatch
        FD_SET((UINT32)mMsgSock, &readfds);
        FD_SET((UINT32)mMsgSock, &writefds);
        FD_SET((UINT32)mMsgSock, &exceptfds); 
#else
        FD_SET(mMsgSock, &readfds);
        FD_SET(mMsgSock, &writefds);
        FD_SET(mMsgSock, &exceptfds); 
#endif

        maxfdp1 = mMsgSock + 1;
    }

    // wait for 1 second if no connect or recv/send socket requests.
    struct timeval tv; 
    tv.tv_sec = 1; 
    tv.tv_usec = 0; 

    int n = select(maxfdp1, &readfds, &writefds, &exceptfds, &tv);
    if (n < 0) {
        WBTRACE("Exception occurred!\n");
        ret = -1;
    } else if (n > 0) {
        if (FD_ISSET(mServerSock, &readfds)) {
            struct sockaddr_in peer_addr;
            int len = sizeof(peer_addr);

#ifdef WIN32
            if ((mMsgSock = accept(mServerSock, (struct sockaddr*)&peer_addr, 
                &len)) == -1) {
#else
            if ((mMsgSock = accept(mServerSock, (struct sockaddr*)&peer_addr, 
                (socklen_t*)&len)) == -1) {
#endif
                WBTRACE("accept fail!\n");
                ret = -1;
            }
        } else if (FD_ISSET(mServerSock, &exceptfds)) {
            WBTRACE("Exception occurred!\n");
            ret = -1;
        } else if (FD_ISSET(mMsgSock, &readfds)) {
            ret = RecvData();
        } else if (FD_ISSET(mMsgSock, &writefds)) {
            ret = SendData();
        } else if (FD_ISSET(mMsgSock, &exceptfds)) {
            WBTRACE("Exception occurred!\n");
            ret = -1;
        }
    }

    return ret;
}

int MsgServer::RecvData()
{    
    char recvDataBuf[BUFFER_SIZE] = "\0";
    char unfinishedMsgBuf[BUFFER_SIZE] = "\0";

    int len = recv(mMsgSock, recvDataBuf, BUFFER_SIZE_HALF, 0);
    if (len == 0) {
        // value 0 means the network connection is closed.
        WBTRACE("client socket has been closed!\n");
        return -1;
    }
    else if (len < 0) {
        // value -1 indicates a failure
        WBTRACE("receive fail!\n");
        return len;
    }
    
    recvDataBuf[len] = 0;

    WBTRACE("Client socket recv %s\n", recvDataBuf);
    if (len + strlen(mRecvBuffer) < BUFFER_SIZE) {
        strcat(mRecvBuffer, recvDataBuf);
        memset(recvDataBuf, '\0', strlen(recvDataBuf));
    } 
   
    char *delimiterPtr = strstr(mRecvBuffer, MSG_DELIMITER);
    if (!delimiterPtr) {
        // The message buffer has no message delimiters, should be part 
        // of a long ( > BUFFER_SIZE) message. Cache the message piece 
        // into the long message buffer (mLongRecvBuffer).
        if (len + (int)strlen(mLongRecvBuffer) >= mLongRecvBufferSize) {
            char *tmpRecvBuffer = mLongRecvBuffer;
            mLongRecvBufferSize += BUFFER_SIZE * 4;
            mLongRecvBuffer 
                = new char [mLongRecvBufferSize];
            memset(mLongRecvBuffer, '\0', mLongRecvBufferSize);
            strcpy(mLongRecvBuffer, tmpRecvBuffer);

            delete [] tmpRecvBuffer;
        }      
        strcat(mLongRecvBuffer, mRecvBuffer);

        // Clear the normal message buffer for incoming new messages.
        memset(mRecvBuffer, '\0', strlen(mRecvBuffer));
    } else {
        // Find the last message delimiter, the remaining characters are part 
        // of an unfinished message. 
        char *tmpPtr;
        while (tmpPtr = strstr(delimiterPtr + strlen(MSG_DELIMITER), \
            MSG_DELIMITER)) 
          delimiterPtr = tmpPtr;

        for (int i = 0; i < (int)strlen(MSG_DELIMITER); i++)
            *(delimiterPtr + i) = 0;

        // save the unfinished message part, if any.
        strcpy(unfinishedMsgBuf, delimiterPtr + strlen(MSG_DELIMITER));

        char token[BUFFER_SIZE] = "\0";
        char* bufferPtr = mRecvBuffer;

        while (bufferPtr != NULL) {
            memset(token, '\0', strlen(token));

            delimiterPtr = strstr(bufferPtr, MSG_DELIMITER);
            if (delimiterPtr == NULL) {
                // no delimiter, return the whole string.
                strcpy(token, bufferPtr);
                bufferPtr = NULL;
            } else { 
                strncpy(token, bufferPtr, delimiterPtr - bufferPtr);
                bufferPtr = delimiterPtr + strlen(MSG_DELIMITER);
            }

            if (token[0] == '@') {
                // this is a special response message.
                int instance, msg, data;
                int i = sscanf(token, "@%d,%d,%d", &instance, &msg, &data);
                if (i == 3) {
                    for (int i = 0; i < MAX_TRIGGER; i++) {
                        if (mTriggers[i].mInstance == instance 
                            && mTriggers[i].mMsg == msg) {
                            *(mTriggers[i].mTrigger) = data;
                            mTriggers[i].mInstance = EMPTY_TRIGGER;
                            break;
                        }
                    }
                }
            } else if (token[0] == '*') {
                // this is quit message
                if (mHandler) {
                    mHandler(&token[1]);
                }
                return -1;
            } else {
                if (mHandler) {
                    // For each message piece ending with a message delimiter, 
                    // check whether the long message buffer is empty. If not, 
                    // this is the end part of a long message, construct the 
                    // complete, long message.
                    if (!strlen(mLongRecvBuffer)) {
                        mHandler(token);
                    } else {
                        // If the long message buffer is not long enough to hold this end
                        // message piece, alloc more space.
                        if (((int)strlen(mLongRecvBuffer) + (int)strlen(token)) 
                            >= mLongRecvBufferSize) {
                            char *tmpRecvBuffer = mLongRecvBuffer;
                            // one more BUFFER_SIZE space is enough for one msgToken (message).
                            mLongRecvBufferSize += BUFFER_SIZE;
                            mLongRecvBuffer 
                                = new char [mLongRecvBufferSize];
                            memset(mLongRecvBuffer, '\0', mLongRecvBufferSize);
                            strcpy(mLongRecvBuffer, tmpRecvBuffer);
                                
                            delete [] tmpRecvBuffer;
                        }      

                        strcat(mLongRecvBuffer, token);
                        mHandler(mLongRecvBuffer);

                        // Clear the long message buffer.
                        memset(mLongRecvBuffer, '\0', 
                            strlen(mLongRecvBuffer));
                    }
                }
            }
        } // end while{}

        // clear the receiver buffer, all the messages should be handled, 
        // except part of an unfinished message.
        memset(mRecvBuffer, '\0', strlen(mRecvBuffer));

        // restore the unfinished message, if any
        if (strlen(unfinishedMsgBuf)) {
            strcpy(mRecvBuffer, unfinishedMsgBuf);
        }

        // store the unhandled message since the receiver buffer is full.
        if (strlen(recvDataBuf)) {
            strcat(mRecvBuffer, recvDataBuf);
        }
    }

    return len;
}

int MsgServer::SendData()
{   
    int len = strlen(mSendBuffer);
    if (len == 0)
        return 0;

    len = send(mMsgSock, mSendBuffer, len, 0);
    WBTRACE("Client socket send %s\n", mSendBuffer);
    if (len > 0) {
        mSendBuffer[0] = 0;
    }
    else if (len < 0) {
        WBTRACE("send fail!\n");
    }

    return len;
}

///////////////////////////////////////////////////////////

void SendSocketMessage(int instance, int event, const char *pData)
{   
    // Note: As the message sending buffer (mSendBuffer) is BUFFER_SIZE 
    // long, it may contains remaining message contents, so just fill 
    // in half of the BUFFER_SIZE message content (BUFFER_SIZE_HALF).
    char buf[BUFFER_SIZE];
    if (pData && strlen(pData) > 0) {
        if (strlen(pData) <= BUFFER_SIZE_HALF) {
            sprintf(buf, "%d,%d,%s%s", instance, event, pData, MSG_DELIMITER);
            
#ifdef WIN32
            EnterCriticalSection(&CriticalSection);
#else
            pthread_mutex_lock(&gServerMutex);
#endif

            gMessenger.Send(buf);

#ifdef WIN32
            LeaveCriticalSection(&CriticalSection);
#else
            pthread_mutex_unlock(&gServerMutex);
#endif
        } else {
            // in case pData is longer than BUFFER_SIZE_HALF, send it in 
            // multiple pieces.
            char pPartData[BUFFER_SIZE_HALF] = "\0";
            char* dataPtr = (char*)pData;

            // the head message piece.
            strncpy(pPartData, dataPtr, BUFFER_SIZE_HALF);
            // Note!!! this is important, to avoid additional, ending characters 
            // to be part of the message.
            pPartData[BUFFER_SIZE_HALF] = '\0';

            sprintf(buf, "%d,%d,%s%s", instance, event, pPartData, \
                MSG_DELIMITER_HEAD);

            dataPtr += BUFFER_SIZE_HALF;

#ifdef WIN32
            EnterCriticalSection(&CriticalSection);
#else
            pthread_mutex_lock(&gServerMutex);
#endif

            gMessenger.Send(buf);

#ifdef WIN32
            LeaveCriticalSection(&CriticalSection);
#else
            pthread_mutex_unlock(&gServerMutex);
#endif

            memset(buf, '\0', strlen(buf));
            memset(pPartData, '\0', strlen(pPartData));
            
            // multiple middle message pieces.
            while (strlen(dataPtr) > BUFFER_SIZE_HALF) {
                strncpy(pPartData, dataPtr, BUFFER_SIZE_HALF);
                pPartData[BUFFER_SIZE_HALF] = '\0';
                sprintf(buf, "%d,%d,%s%s", instance, event, pPartData, \
                    MSG_DELIMITER_MIDDLE);
                dataPtr += BUFFER_SIZE_HALF;

#ifdef WIN32
                EnterCriticalSection(&CriticalSection);
#else
                pthread_mutex_lock(&gServerMutex);
#endif

                int i = gMessenger.Send(buf);

#ifdef WIN32
                LeaveCriticalSection(&CriticalSection);
#else
                pthread_mutex_unlock(&gServerMutex);
#endif

                while (i == -1) {
                    // sleep in *millisecond*.
#ifdef WIN32
                    Sleep(SLEEP_INTERVAL_TIME);    
#else
                    usleep(SLEEP_INTERVAL_TIME);
#endif

#ifdef WIN32
                    EnterCriticalSection(&CriticalSection);
#else
                    pthread_mutex_lock(&gServerMutex);
#endif

                    i = gMessenger.Send(buf);

#ifdef WIN32
                    LeaveCriticalSection(&CriticalSection);
#else
                    pthread_mutex_unlock(&gServerMutex);
#endif
                }                

                memset(buf, '\0', strlen(buf));
                memset(pPartData, '\0', strlen(pPartData));
            }

            // the end message piece.
            strcpy(pPartData, dataPtr);
            pPartData[strlen(pPartData)] = '\0';
            sprintf(buf, "%d,%d,%s%s", instance, event, pPartData, \
                MSG_DELIMITER_END);

#ifdef WIN32
            EnterCriticalSection(&CriticalSection);
#else        
            pthread_mutex_lock(&gServerMutex);
#endif

            int i = gMessenger.Send(buf);

#ifdef WIN32
            LeaveCriticalSection(&CriticalSection);
#else
            pthread_mutex_unlock(&gServerMutex);
#endif
            while (i == -1) {
#ifdef WIN32
                Sleep(SLEEP_INTERVAL_TIME);    
#else
                usleep(SLEEP_INTERVAL_TIME);
#endif

#ifdef WIN32
                EnterCriticalSection(&CriticalSection);
#else
                pthread_mutex_lock(&gServerMutex);
#endif

                i = gMessenger.Send(buf);

#ifdef WIN32
                LeaveCriticalSection(&CriticalSection);
#else
                pthread_mutex_unlock(&gServerMutex);
#endif
            }
        }
    } else {
        sprintf(buf, "%d,%d%s", instance, event, MSG_DELIMITER);

#ifdef WIN32
        EnterCriticalSection(&CriticalSection);
#else
        pthread_mutex_lock(&gServerMutex);
#endif

        gMessenger.Send(buf);

#ifdef WIN32
        LeaveCriticalSection(&CriticalSection);
#else
        pthread_mutex_unlock(&gServerMutex);
#endif
    }
}

void AddTrigger(int instance, int msg, int *trigger)
{
#ifdef WIN32
    EnterCriticalSection(&CriticalSection);
#else
    pthread_mutex_lock(&gServerMutex);
#endif

    gMessenger.AddTrigger(instance, msg, trigger);

#ifdef WIN32
    LeaveCriticalSection(&CriticalSection);
#else
    pthread_mutex_unlock(&gServerMutex);
#endif
}

// this is a socket server listening thread function.
#ifdef _WIN32_IEEMBED
DWORD WINAPI PortListening(void *pParam)
#else
void PortListening(void *pParam)
#endif
{
    int ret = 0;

    gMessenger.SetHandler((MsgHandler)pParam);
    while (ret >= 0) {
#ifdef WIN32
        Sleep(SLEEP_INTERVAL_TIME);    
#else
        usleep(SLEEP_INTERVAL_TIME);
#endif

#ifdef WIN32
        EnterCriticalSection(&CriticalSection);
#else
        pthread_mutex_lock(&gServerMutex);
#endif

        ret = gMessenger.Listen();

#ifdef WIN32
        LeaveCriticalSection(&CriticalSection);
#else
        pthread_mutex_unlock(&gServerMutex);
#endif
    }

    WBTRACE("Quit listening thread. Quit app.\n");

    char buf[BUFFER_SIZE];
    sprintf(buf, "-1,%d%s", JEVENT_SHUTDOWN, MSG_DELIMITER);
    ((MsgHandler)pParam)(buf);

#ifdef _WIN32_IEEMBED
    return 0;
#endif
}
