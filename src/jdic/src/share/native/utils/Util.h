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

#ifndef _util_h
#define _util_h

#if defined(DEBUG) || defined(_DEBUG)
void WBTrace(const char *format, ...);
#define WBTRACE WBTrace
#else
#define WBTRACE
#endif

#ifndef max
#define max(a,b)            (((a) > (b)) ? (a) : (b))
#endif

#ifndef min
#define min(a,b)            (((a) < (b)) ? (a) : (b))
#endif

#define POST_HEADER "Content-Type: application/x-www-form-urlencoded\r\n"

class WBArray
{
public:
// Construction
    WBArray();
    ~WBArray();

// Attributes
    int GetSize() const;
    void SetSize(int nNewSize);

// Operations
    // Clean up
    void RemoveAll();

    // Accessing elements
    void* GetAt(int nIndex) const;
    void SetAt(int nIndex, void* newElement);

    void*& ElementAt(int nIndex);

    // Potentially growing the array
    void SetAtGrow(int nIndex, void* newElement);

    int Add(void* newElement);

    // overloaded operator helpers
    void* operator[](int nIndex) const;
    void*& operator[](int nIndex);

    // Operations that move elements around
    void InsertAt(int nIndex, void* newElement, int nCount = 1);
    void RemoveAt(int nIndex, int nCount = 1);

// Implementation
protected:
    void** m_pData;   // the actual array of data
    int m_nSize;     // # of elements (upperBound - 1)
    int m_nMaxSize;  // max allocated
};

inline int WBArray::GetSize() const
    { return m_nSize; }

inline void WBArray::RemoveAll()
    { SetSize(0); }

inline void* WBArray::GetAt(int nIndex) const
    { return m_pData[nIndex]; }
inline void WBArray::SetAt(int nIndex, void* newElement)
    { m_pData[nIndex] = newElement; }
inline void*& WBArray::ElementAt(int nIndex)
    { return m_pData[nIndex]; }

inline void* WBArray::operator[](int nIndex) const
    { return GetAt(nIndex); }
inline void*& WBArray::operator[](int nIndex)
    { return ElementAt(nIndex); }

// helper function for tuning the given JavaScript string to assign 
// the ultimate returned value to a predefined property of the currently 
// loaded webpage. And then DOM APIs of Mozilla or IE will be used to
// retrieve the property value. 
// 
// As there is no public/frozen APIs for both IE and Mozilla to execute the 
// given JavaScript and return the execution value, this helper function is 
// used by ExecuteScript(javaScript) function.
#define JDIC_BROWSER_INTERMEDIATE_PROP "JDIC_BROWSER_INTERMEDIATE_PROP"
char* TuneJavaScript(const char* javaScript);

// helper function for parsing the post message string fields including 
// url, post data and headers. Which is in the format of:
//   <url><field delimiter><post data><field delimiter><headers>
// The <field delimiter> is a string of 
//   "<instance number>,<event ID>,"
// which must be identical between the Java side and native side.
//
// This function is used to pass multiple string values in one Socket 
// communication.
//
// Return Value:
//   On success, 0 is returned.
//   On error, -1 is returned.
int ParsePostFields(const char* postMessage, 
                    const int instanceNum, const int eventID, 
                    char* urlBuf, char* postDataBuf, char* headersBuf);

// helper function for logging the given message to the predefined,
// log file "JDIC.log" under the *current/working* directory. Usage:
//
//     #include "Util.h"
//
//     LogMsg("Error occured ?");
// or
//     char logBuf[1024];
//     sprintf(logBuf, "Error occured with message %s", errorinfo);
//     LogMsg(logBuf);
//
// Return Value:
//   On success, the total number of characters logged is returned.
//   On error, -1 is returned.
int LogMsg(const char* logmsg);
int LogIntMsg(int i);
#endif // _util_h



