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
    mSendBuffer[0] = mRecvBuffer[0] = 0;

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
            mPollList[1].fd = PR_Accept(mPollList[0].fd, &peerAddr, PR_INTERVAL_NO_TIMEOUT);
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
    char buf[BUFFER_SIZE];
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
    if (len + strlen(mRecvBuffer) < BUFFER_SIZE)
        strcat(mRecvBuffer, buf);

    char *p = strrchr(mRecvBuffer, '\n');
    if (p) {
        *p = 0;
        //save the unfinished message
        strcpy(buf, p+1);

        char *token = strtok(mRecvBuffer, "\n");
        while (token != NULL) {
            if (token[0] == '@') {
                //this is a special response message
                int instance, msg, data;
                int i = sscanf(token, "@%d,%d,%d", &instance, &msg, &data);
                if (i == 3) {
                    for (int i = 0; i < MAX_TRIGGER; i++) {
                        if (mTriggers[i].mInstance == instance && mTriggers[i].mMsg == msg) {
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
                    mHandler(token);
                }
            }

            token = strtok(NULL, "\n");
        }

        // restore the unfinished message, if any
        strcpy(mRecvBuffer, buf);
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
    char buf[1024];
    if (pData)
        sprintf(buf, "%d,%d,%s\n", instance, event, pData);
    else
        sprintf(buf, "%d,%d\n", instance, event);
    PR_Lock(gServerLock);
    gMessenger.Send(buf);
    PR_Unlock(gServerLock);
}

void AddTrigger(int instance, int msg, int *trigger)
{
    PR_Lock(gServerLock);
    gMessenger.AddTrigger(instance, msg, trigger);
    PR_Unlock(gServerLock);
}

static PRIntervalTime sleepInterval = PR_MillisecondsToInterval(10);

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

    char buf[10];
    sprintf(buf, "-1,%d\n", JEVENT_SHUTDOWN);
    ((MsgHandler)pParam)(buf);
}
