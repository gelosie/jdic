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


#include "stdafx.h"
#include "VariantWrapper.h"

#include <comdef.h>
using namespace _com_util;


VariantWrapper::VariantWrapper()
{
	vt = VT_EMPTY;
}

VariantWrapper::VariantWrapper(VARIANT *pV)
{
	ATLASSERT(pV);
	ATLASSERT(AfxIsValidAddress(pV, sizeof(VARIANT), TRUE));

	vt = VT_VARIANT;
	pvarVal = pV;
}

VariantWrapper::~VariantWrapper(void)
{
	unsigned long i;

	switch (vt)
	{
	    case VT_BSTR:
		    SysFreeString(bstrVal);
		    break;
	    case VT_ARRAY | VT_VARIANT:
		    for (i = 0; i < parray->rgsabound[0].cElements; i++)
			    if (((VARIANT *) parray->pvData)[i].vt == VT_BSTR)
				    SysFreeString(((VARIANT *) parray->pvData)[i].bstrVal);

		    delete parray->pvData;
		    delete parray;
		    break;
	    default:
		    break;
	}
}

LPSTR VariantWrapper::ToString()
{
	USES_CONVERSION;
    VARIANT* pV = NULL;
    char* retStr = NULL;
    if (vt == VT_VARIANT && pvarVal != (VARIANT*) NULL)
    {
        pV = pvarVal;
    } 
    else 
    {
        pV = this;
    }
    
    switch (pV->vt)
    {
        case VT_BSTR:
            retStr = _com_util::ConvertBSTRToString(pV->bstrVal);
            break;
        case VT_UI1:
            retStr = new char[256];
            itoa((int) pV->bVal, retStr, 10);
            break;
        case VT_I2:
            retStr = new char[256];
            itoa((int) pV->iVal, retStr, 10);
            break;
        case VT_I4:
            retStr = new char[256];
            itoa((int) pV->lVal, retStr, 10);
            break;
        case VT_BOOL:
            retStr = new char[256];
            if (pV->boolVal)
            {
                sprintf(retStr, "%s\0", "true");
            }
            else
            {
                sprintf(retStr, "%s\0", "false");
            }
        default:
            break;
    }
    return retStr;
}


