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
#include "prlog.h"
#include "prerror.h"
#include "prenv.h"
#include "MsgServer.h"
#include "Message.h"
#include "Util.h"

MsgServer gMessenger;
PRLock *gServerLock;

static PRIntervalTime sleepInterval = PR_MillisecondsToInterval(10);

int MsgServer::mPort = 0;

MsgServer::MsgServer()
{
#if defined(DEBUG) || defined(_DEBUG)
    PR_SetEnv("NSPR_LOG_MODULES=webbrowser:5");
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

    for (i = 0; i < MAX_FD; i++) {
        mPollList[i].fd = NULL;
    }

    gServerLock = PR_NewLock();
}

MsgServer::~MsgServer()
{
    PR_DestroyLock(gServerLock);

    delete [] mSendBuffer;
    delete [] mRecvBuffer;
    delete [] mLongRecvBuffer;
    delete [] mTriggers;

    WBTRACE("Closing socket ...\n");
    for (int i = 0; i < MAX_FD; i++) {
        if (mPollList[i].fd)
            PR_Close(mPollList[i].fd);
    }
}

int MsgServer::CreateServerSocket()
{
    PRFileDesc *sock = PR_NewTCPSocket();
    if (!sock)
        return PR_GetError();

    PRSocketOptionData socket_opt;
    socket_opt.option = PR_SockOpt_Nonblocking;
    socket_opt.value.non_blocking = PR_TRUE;
    if (PR_SetSocketOption(sock, &socket_opt) == PR_FAILURE)
        goto failed;

    socket_opt.option = PR_SockOpt_Reuseaddr;
    socket_opt.value.reuse_addr = PR_TRUE;
    if (PR_SetSocketOption(sock, &socket_opt) == PR_FAILURE)
        goto failed;

    PRNetAddr selfAddr;
    PR_InitializeNetAddr(PR_IpAddrLoopback, mPort, &selfAddr);
    if (PR_Bind(sock, &selfAddr) == PR_FAILURE)
        goto failed;

    if (PR_Listen(sock, MAX_CONN) == PR_FAILURE)
        goto failed;

    WBTRACE("Listening port %d ...\n", mPort);

    mPollList[0].fd = sock;
    mFailed = 0;
    return 0;

failed:
    int error = PR_GetError();
    WBTRACE("CreateServerSocket failed! Error code = %d\n", error);
    PR_Close(sock);
    return error;
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
    int pollcount = 0;
    for (int i = 0; i < MAX_FD; i++) {
        if (mPollList[i].fd) {
            mPollList[i].in_flags = PR_POLL_READ | PR_POLL_WRITE | PR_POLL_EXCEPT;
            mPollList[i].out_flags = 0;
            pollcount++;
        }
        else
            break;
    }

    if (mCounter >= 200 && pollcount == 1) {
        // haven't received any connection request after 200 times. quit
        return -1;
    }

    int n = PR_Poll(mPollList, pollcount, 0);
    if (n > 0) {
        if (mPollList[0].out_flags & PR_POLL_READ) {
            PRNetAddr peerAddr;
            mPollList[1].fd = PR_Accept(mPollList[0].fd, &peerAddr, \
                PR_INTERVAL_NO_TIMEOUT);
            if (mPollList[1].fd == NULL) {
                WBTRACE("accept fail, error code = %d\n", PR_GetError());
                ret = -1;
            }
        }
        else if (mPollList[0].out_flags & PR_POLL_EXCEPT) {
            WBTRACE("Exception occurred!\n");
            ret = -1;
        }
        else if (mPollList[1].out_flags & PR_POLL_READ) {
            ret = RecvData();
        }
        else if (mPollList[1].out_flags & PR_POLL_WRITE) {
            ret = SendData();
        }
        else if (mPollList[1].out_flags & PR_POLL_EXCEPT) {
            WBTRACE("Exception occurred!\n");
            ret = -1;
        }
    }

    return ret;
}

int MsgServer::RecvData()
{    
    char buf[BUFFER_SIZE] = "\0";
    char unfinishedMsgBuf[BUFFER_SIZE] = "\0";

    int len = PR_Recv(mPollList[1].fd, buf, BUFFER_SIZE - 1, 0, 0);
    if (len == 0) {
        // value 0 means the network connection is closed.
        WBTRACE("client socket has been closed!\n");
        return -1;
    }
    else if (len < 0) {
        // value -1 indicates a failure
        WBTRACE("receive fail, error code = %d\n", PR_GetError());
        return len;
    }
    
    buf[len] = 0;

    WBTRACE("Client socket recv %s\n", buf);
    if (len + strlen(mRecvBuffer) < BUFFER_SIZE) {
        strcat(mRecvBuffer, buf);
        memset(buf, '\0', strlen(buf));
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
        // find the last message delimiter, the left characters are part 
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
            }
            else if (token[0] == '*') {
                // this is quit message
                if (mHandler) {
                    mHandler(&token[1]);
                }
                return -1;
            }
            else {
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
                        if (((int)strlen(mLongRecvBuffer) + (int)strlen(token)) >= mLongRecvBufferSize) {
                            char *tmpRecvBuffer = mLongRecvBuffer;
                            // one more BUFFER_SIZE space is enough for one msgToken (message).
                            mLongRecvBufferSize += BUFFER_SIZE;
                            mLongRecvBuffer 
                                = new char [mLongRecvBufferSize];
                            memset(mLongRecvBuffer, '\0', mLongRecvBufferSize);
                                
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
        if (strlen(buf)) {
            strcat(mRecvBuffer, buf);
        }
    }

    return len;
}

int MsgServer::SendData()
{
    int len = strlen(mSendBuffer);
    if (len == 0)
        return 0;

    len = PR_Send(mPollList[1].fd, mSendBuffer, len, 0, 0);
    WBTRACE("Client socket send %s\n", mSendBuffer);
    if (len > 0) {
        mSendBuffer[0] = 0;
    }
    else if (len < 0) {
        WBTRACE("send fail, error code = %d\n", PR_GetError());
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
            
            PR_Lock(gServerLock);
            gMessenger.Send(buf);
            PR_Unlock(gServerLock);           
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

            PR_Lock(gServerLock);
            gMessenger.Send(buf);
            PR_Unlock(gServerLock);

            memset(buf, '\0', strlen(buf));
            memset(pPartData, '\0', strlen(pPartData));
            
            // multiple middle message pieces.
            while (strlen(dataPtr) > BUFFER_SIZE_HALF) {
                strncpy(pPartData, dataPtr, BUFFER_SIZE_HALF);
                pPartData[BUFFER_SIZE_HALF] = '\0';
                sprintf(buf, "%d,%d,%s%s", instance, event, pPartData, \
                    MSG_DELIMITER_MIDDLE);
                dataPtr += BUFFER_SIZE_HALF;

                PR_Lock(gServerLock);
                int i = gMessenger.Send(buf);
                PR_Unlock(gServerLock);
                while (i == -1) {
                    PR_Sleep(sleepInterval);

                    PR_Lock(gServerLock);
                    i = gMessenger.Send(buf);
                    PR_Unlock(gServerLock);
                }                

                memset(buf, '\0', strlen(buf));
                memset(pPartData, '\0', strlen(pPartData));
            }

            // the end message piece.
            strcpy(pPartData, dataPtr);
            pPartData[strlen(pPartData)] = '\0';
            sprintf(buf, "%d,%d,%s%s", instance, event, pPartData, \
                MSG_DELIMITER_END);

            PR_Lock(gServerLock);
            int i = gMessenger.Send(buf);
            PR_Unlock(gServerLock);
            while (i == -1) {
                PR_Sleep(sleepInterval);

                PR_Lock(gServerLock);
                i = gMessenger.Send(buf);
                PR_Unlock(gServerLock);
            }
        }
    } else {
        sprintf(buf, "%d,%d%s", instance, event, MSG_DELIMITER);
        PR_Lock(gServerLock);
        gMessenger.Send(buf);
        PR_Unlock(gServerLock);
    }
}

void AddTrigger(int instance, int msg, int *trigger)
{
    PR_Lock(gServerLock);
    gMessenger.AddTrigger(instance, msg, trigger);
    PR_Unlock(gServerLock);
}

// this is a socket server listening thread
void PortListening(void* pParam)
{
    int ret = 0;

    PR_ASSERT(pParam);

    gMessenger.SetHandler((MsgHandler)pParam);
    while (ret >= 0) {
        PR_Sleep(sleepInterval);
        PR_Lock(gServerLock);
        ret = gMessenger.Listen();
        PR_Unlock(gServerLock);
    }

    WBTRACE("Quit listening thread. Quit app.\n");

    char buf[BUFFER_SIZE];
    sprintf(buf, "-1,%d%s", JEVENT_SHUTDOWN, MSG_DELIMITER);
    ((MsgHandler)pParam)(buf);
}
