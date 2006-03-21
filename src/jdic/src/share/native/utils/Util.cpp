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
#include <stdlib.h>
#include <stdarg.h>
#include <string.h>
#include "Util.h"

#if defined(DEBUG) || defined(_DEBUG)

void WBTrace(const char *format, ...)
{
    va_list args;
    va_start(args, format);

    char buffer[1024];
    sprintf(buffer, format, args);
    printf(buffer);
	LogMsg(buffer);

    va_end(args);
}

#endif //DEBUG

///////////////////////////////////////////////////////////
// Implemetation of Array type
///////////////////////////////////////////////////////////
WBArray::WBArray()
{
    m_pData = NULL;
    m_nSize = m_nMaxSize = 0;
}

WBArray::~WBArray()
{
    delete[] (char*)m_pData;
}

void WBArray::SetSize(int nNewSize)
{
    if (nNewSize == 0)
    {
        // shrink to nothing
        delete[] (char*)m_pData;
        m_pData = NULL;
        m_nSize = m_nMaxSize = 0;
    }
    else if (m_pData == NULL)
    {
        // create one with exact size
        m_pData = (void**) new char[nNewSize * sizeof(void*)];

        memset(m_pData, 0, nNewSize * sizeof(void*));  // zero fill

        m_nSize = m_nMaxSize = nNewSize;
    }
    else if (nNewSize <= m_nMaxSize)
    {
        // it fits
        if (nNewSize > m_nSize)
        {
            // initialize the new elements
            memset(&m_pData[m_nSize], 0, (nNewSize-m_nSize) * sizeof(void*));
        }

        m_nSize = nNewSize;
    }
    else
    {
        // otherwise, grow array
        int nGrowBy = min(1024, max(4, m_nSize / 8));
        
        int nNewMax;
        if (nNewSize < m_nMaxSize + nGrowBy)
            nNewMax = m_nMaxSize + nGrowBy;  // granularity
        else
            nNewMax = nNewSize;  // no slush

        void** pNewData = (void**) new char[nNewMax * sizeof(void*)];

        // copy new data from old
        memcpy(pNewData, m_pData, m_nSize * sizeof(void*));
        memset(&pNewData[m_nSize], 0, (nNewSize-m_nSize) * sizeof(void*));

        // get rid of old stuff (note: no destructors called)
        delete[] (char*)m_pData;
        m_pData = pNewData;
        m_nSize = nNewSize;
        m_nMaxSize = nNewMax;
    }
}

/////////////////////////////////////////////////////////////////////////////

void WBArray::SetAtGrow(int nIndex, void* newElement)
{
    if (nIndex >= m_nSize)
        SetSize(nIndex+1);
    m_pData[nIndex] = newElement;
}

int WBArray::Add(void* newElement)
{ 
    int nIndex = m_nSize;
    SetAtGrow(nIndex, newElement);
    return nIndex; 
}

void WBArray::InsertAt(int nIndex, void* newElement, int nCount)
{
    if (nIndex >= m_nSize)
    {
        // adding after the end of the array
        SetSize(nIndex + nCount);  // grow so nIndex is valid
    }
    else
    {
        // inserting in the middle of the array
        int nOldSize = m_nSize;
        SetSize(m_nSize + nCount);  // grow it to new size
        // shift old data up to fill gap
        memmove(&m_pData[nIndex+nCount], &m_pData[nIndex],
            (nOldSize-nIndex) * sizeof(void*));

        // re-init slots we copied from
        memset(&m_pData[nIndex], 0, nCount * sizeof(void*));
    }

    // copy elements into the empty space
    while (nCount--)
        m_pData[nIndex++] = newElement;
}

void WBArray::RemoveAt(int nIndex, int nCount)
{
    // just remove a range
    int nMoveCount = m_nSize - (nIndex + nCount);

    if (nMoveCount)
        memmove(&m_pData[nIndex], &m_pData[nIndex + nCount],
            nMoveCount * sizeof(void*));
    m_nSize -= nCount;
}

/////////////////////////////////////////////////////////////////////////////

// helper function for tuning the given JavaScript string to assign 
// the ultimate returned value to a predefined property of the currently 
// loaded webpage. And then DOM APIs of Mozilla or IE will be used to
// retrieve the property value. 
// 
// As there is no public/frozen APIs for both IE and Mozilla to execute the 
// given JavaScript and return the execution value, this helper function is 
// used by ExecuteScript(javaScript) function.
char* TuneJavaScript(const char* javaScript)
{
    // Tune the JavaScript into below format:
    //     var retValue = eval("<the user input JavaScript string>"); \
    //     var heads = document.getElementsByTagName('head');"); \
    //     heads[0].setAttribute(<JDIC_BROWSER_INTERMEDIATE_PROP>, retValue);   
    
    // Alloc double space of the given JavaScript string plus enough space
    // for additional script content.
    int resultJScriptLen = strlen(javaScript) * 2 + 1024;
    char* resultJScript 
        = new char[resultJScriptLen];
    memset(resultJScript, '\0', resultJScriptLen);

    strcat(resultJScript, "var retValue = eval(\"");

    // Escape all the '\"', '\\', '\r' and '\n's in the JavaScript string.
    for (int i = 0; i < (int)strlen(javaScript); i++) {
        char c = javaScript[i];
    
        if (c == '\"' || c == '\\' || c == '\r' || c == '\n')
            resultJScript[strlen(resultJScript)] = '\\';

        if (c == '\r') c = 'r';
        if (c == '\n') c = 'n';

        resultJScript[strlen(resultJScript)] = c;
    }
    
    strcat(resultJScript, "\")");
    strcat(resultJScript, ";");

    // Store the returned value of eval command as the "head" element property
    // JDIC_BROWSER_INTERMEDIATE_PROP.
    strcat(resultJScript, "var heads = document.getElementsByTagName('head');");
    strcat(resultJScript, "heads[0].setAttribute('");
    strcat(resultJScript, JDIC_BROWSER_INTERMEDIATE_PROP);
    strcat(resultJScript, "', retValue);");  

    char* retJScript = strdup(resultJScript);
    delete [] resultJScript;    
    return retJScript;
}

/////////////////////////////////////////////////////////////////////////////

// helper function for parsing the post message string fields including 
// url, post data and headers. Which is in the format of:
//   <url><field delimiter><post data><field delimiter><headers>
// The <field delimiter> is a string of 
//   "<instance number>,<event ID>,"
// which must be identical between the Java side and native side.
int ParsePostFields(const char* postMessage, 
                    const int instanceNum, const int eventID, 
                    char* urlBuf, char* postDataBuf, char* headersBuf)
{
    // Construct the message string field delimiter with the instance 
    // number and the event ID.
    char fieldDelimiter[2048];
    memset(fieldDelimiter, '\0', 2048);
    sprintf(fieldDelimiter, "%d,%d,", instanceNum, eventID);

    // Get URL field.
    char *fieldPtr;
    fieldPtr = (char*)postMessage;
    char *delimiterPtr;
    delimiterPtr = strstr(fieldPtr, fieldDelimiter);
    strncpy(urlBuf, fieldPtr, delimiterPtr - fieldPtr); 

    // Get post data field.
    fieldPtr = delimiterPtr + strlen(fieldDelimiter);
    delimiterPtr = strstr(fieldPtr, fieldDelimiter);
    strncpy(postDataBuf, fieldPtr, delimiterPtr - fieldPtr);

    // Get headers field.
    strcpy(headersBuf, delimiterPtr + strlen(fieldDelimiter));

    return 0;
}


/////////////////////////////////////////////////////////////////////////////

// helper function for logging the given message to the predefined,
// log file "JDIC.log" under the *current/working* directory.
int LogMsg(const char* logmsg) 
{
    if (logmsg == NULL) {
        return 0;
    }

    // Predefined JDIC log file.
    const char* logFilePath = "JDIC.log";
    FILE* logFile;

    // Open the log file for appending messages.
    if((logFile = fopen(logFilePath, "at")) == NULL) {
        // Log file doesn't exist, create it. 
        if ((logFile = fopen(logFilePath, "wt+")) == NULL) {
            // Error creating log file.
            return -1;
        }
    }

    // Append the given log message to the log file.    
    fprintf(logFile, "*** JDIC log: %s\n", logmsg);
    fclose(logFile);

    return strlen(logmsg);
}

int LogIntMsg(int i)
{
    // Predefined JDIC log file.
    const char* logFilePath = "JDIC.log";
    FILE* logFile;

    // Open the log file for appending messages.
    if((logFile = fopen(logFilePath, "at")) == NULL) {
        // Log file doesn't exist, create it. 
        if ((logFile = fopen(logFilePath, "wt+")) == NULL) {
            // Error creating log file.
            return -1;
        }
    }

    // Append the given log message to the log file.    
    fprintf(logFile, "*** JDIC log: %d\n", i);
    fclose(logFile);
	return i;
}
