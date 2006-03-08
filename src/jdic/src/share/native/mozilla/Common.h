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

#ifndef _Common_H_
#define _Common_H_

#include "nsIFile.h"
#include "nsEmbedString.h"
#include "nsID.h"
#include "nsIWebNavigation.h"
#include "Util.h"

nsresult InitializeProfile();
void ReportError(const char* msg);

// helper function for using the xpcom directory service
nsresult GetSpecialDirectory(const char *key, nsIFile **result);

// helper function for converting ASCII chars to a Mozilla nsEmbedString
PRBool ConvertAsciiToUtf16(const char *str, nsEmbedString &result);

// helper function for converting UTF-8 chars to UTF-16 chars.
PRBool ConvertUtf8ToUtf16(const nsEmbedCString &aSource, nsEmbedString &aDest);

// helper function for converting UTF-16 chars to UTF-8 chars.
PRBool ConvertUtf16ToUtf8(const nsEmbedString &aSource, nsEmbedCString &aDest);

// helper function for getting xpcom services
nsresult GetService(const char *aContractID, const nsIID &aIID, void **aResult);

// helper function for instantiating xpcom components
nsresult CreateInstance(const char *aContractID, const nsIID &aIID, void **aResult);

// helper function for getting the HTML page content.
char* GetContent(nsIWebNavigation *aWebNav);

// helper function for seting the HTML page content.
nsresult SetContent(nsIWebNavigation *aWebNav, const char *htmlContent);

// helper function for executing javascript string
char* ExecuteScript(nsIWebNavigation *aWebNav, const char *jscript);

#endif
