/*
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
 
#include <jni.h>
#include <Windows.h>
#include "WinIconWrapper.h"
#ifdef __cplusplus
extern "C" {
#endif

static BOOL CALLBACK iterateIcons(HMODULE, LPCWSTR lpType, LPWSTR lpszName, LONG_PTR lParam) {
    if(*reinterpret_cast<WORD*>(lParam)) {
        --*reinterpret_cast<WORD*>(lParam);
        return true;
    }
    *reinterpret_cast<LPWSTR*>(lParam)= lpszName;
    return false;
}

static jbyteArray loadfile(JNIEnv *env, wchar_t const *iconLocation) {

    HANDLE file= CreateFileW(iconLocation, FILE_READ_DATA, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_FLAG_SEQUENTIAL_SCAN, NULL);
    if(file==INVALID_HANDLE_VALUE)
        return 0;

    DWORD size= GetFileSize(file, 0);
    jbyteArray rc= env->NewByteArray(size);

    jboolean isCopy;
    jbyte *dst= env->GetByteArrayElements(rc, &isCopy);

    DWORD nread;
    bool success= ReadFile(file, dst, size, &nread, NULL) && nread==size;

    env->ReleaseByteArrayElements(rc, dst, success ?0 :JNI_ABORT);

    CloseHandle(file);
    return success ?rc :0;
}

#pragma pack( push )
#pragma pack( 2 )

struct GroupEntry
{
    BYTE   bWidth;               // Width, in pixels, of the image
    BYTE   bHeight;              // Height, in pixels, of the image
    BYTE   bColorCount;          // Number of colors in image (0 if >=8bpp)
    BYTE   bReserved;            // Reserved
    WORD   wPlanes;              // Color Planes
    WORD   wBitCount;            // Bits per pixel
    DWORD  dwBytesInRes;         // how many bytes in this resource?
};

struct DllGroupEntry : GroupEntry
{
    WORD   nID;                  // the ID
};

struct FileGroupEntry : GroupEntry
{
    DWORD   dwImageOffset;
};

struct Group
{
    WORD   zero;    
    WORD   idType;       // Resource type (1 for icons, 2 for cursors)
    WORD   idCount;      // How many images?
};

#pragma pack( pop )
#define RT_GROUP_ICONW MAKEINTRESOURCEW((DWORD)RT_GROUP_ICON)
#define RT_ICONW MAKEINTRESOURCEW((DWORD)RT_ICON)

static jbyteArray getIcon(JNIEnv *env, wchar_t *iconLocation) {
    wchar_t expandedLocation[1024];
    if(ExpandEnvironmentStringsW(iconLocation, expandedLocation, sizeof(expandedLocation)-1)==0)
        return 0;

    wchar_t fileLocation[1024];

    // copy expandedLocation into fileLocation
    wchar_t *dst= fileLocation;
    for(wchar_t const *src= expandedLocation; *src; ) {
        *dst++= *src++;
        if(dst==fileLocation+1024)
            return 0;   // expandedLocation too long
    }
    *dst= 0;
    // backup, looking for ','
    for(--dst; *dst!=','; --dst) {
        if(*dst<'-' || *dst>'9' || dst<=fileLocation) {
            return loadfile(env, expandedLocation); // no trailing comma, so load entire file
        }
    }
    *dst= 0;  // replace ',' with null in fileLocation

    bool minus= *++dst=='-';
    if(minus)
        ++dst;

    // translate trailing digits into number
    WORD num= 0;
    for(; *dst; ++dst) {
        num*= 10;
        num+= *dst-'0';
    }
    LPCWSTR name= MAKEINTRESOURCEW(num);

    HMODULE hmod= LoadLibraryExW(fileLocation, 0, DONT_RESOLVE_DLL_REFERENCES|LOAD_IGNORE_CODE_AUTHZ_LEVEL|LOAD_LIBRARY_AS_DATAFILE);
    if(hmod==0)
        return loadfile(env, fileLocation);

    // iterate through icon groups to the correct index (minus indicates number is the resource number)
    if(!minus)
        EnumResourceNamesW(hmod, RT_GROUP_ICONW, &iterateIcons, reinterpret_cast<LONG_PTR>(&name));

    // open the resource
    jbyteArray rc= 0;
    HRSRC groupHandle= FindResourceW(hmod, name, RT_GROUP_ICONW);
    if(groupHandle!=0) {
        DWORD groupSize= SizeofResource(hmod, groupHandle);
        if(groupSize) {
            LPVOID groupBytes= LockResource(LoadResource(hmod, groupHandle));

            Group *group= static_cast<Group *>(groupBytes);
            if(group->zero==0 && group->idType==1 && group->idCount>1)
            {
                DWORD iconOffset= sizeof(Group)+sizeof(FileGroupEntry)*group->idCount;
                DWORD resourceSize= iconOffset;

                DllGroupEntry *dllEntry= reinterpret_cast<DllGroupEntry *>( static_cast<Group*>(groupBytes)+1 );
                for(WORD i= 0; i<group->idCount; ++i)
                {
                    resourceSize+= dllEntry->dwBytesInRes;
                    ++dllEntry;
                }

                rc= env->NewByteArray(resourceSize);
                jboolean isCopy;
                jbyte *dst= env->GetByteArrayElements(rc, &isCopy);

                // copy the group header
                memcpy(dst, group, sizeof(Group));

                dllEntry= reinterpret_cast<DllGroupEntry *>( static_cast<Group*>(groupBytes)+1 );
                FileGroupEntry *fileEntry= reinterpret_cast<FileGroupEntry*>( dst+sizeof(Group) );

                for(WORD j= 0; j<group->idCount; ++j)
                {
                    // find the icon
                    HRSRC iconHandle= FindResourceW(hmod, MAKEINTRESOURCEW(dllEntry->nID), RT_ICONW);
                    if(!iconHandle)
                        break;

                    DWORD iconSize= SizeofResource(hmod, iconHandle);
                    if(iconSize!=dllEntry->dwBytesInRes)
                        break;

                    LPVOID iconBytes= LockResource(LoadResource(hmod, iconHandle));
                    if(!iconBytes)
                        break;

                    // copy the icon
                    memcpy(dst+iconOffset, iconBytes, iconSize);

                    // copy the group entry
                    fileEntry->dwImageOffset= iconOffset;
                    memcpy(fileEntry++, dllEntry++, sizeof(GroupEntry));

                    iconOffset+= iconSize;
                }
                env->ReleaseByteArrayElements(rc, dst, 0);
                // check that we got all of the icons
                if(iconOffset!=resourceSize)
                    rc= 0;
            }
        }
    }
    FreeLibrary(hmod);
    return rc;
}

/*
 * Class:     org_jdesktop_jdic_filetypes_impl_WinRegistryWrapper
 * Method:    GetIcon
 * Signature: ([C)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_jdesktop_jdic_icons_impl_WinIconWrapper_GetIcon
  (JNIEnv *env, jclass, jcharArray jiconLocation) {

    jboolean isCopy;
    jchar *iconLocation= env->GetCharArrayElements(jiconLocation, &isCopy);
    jbyteArray rc= getIcon(env, iconLocation);
    env->ReleaseCharArrayElements(jiconLocation, iconLocation, JNI_ABORT);

    return rc;
}

// WTF -- PURELY TEST DO NOT LEAVE HERE
/*
 * Class:     org_jdesktop_jdic_icons_implXdgDirectory
 * Method:    getEnvironmentVariable
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_jdesktop_jdic_icons_impl_XdgDirectory_getEnvironmentVariable
  (JNIEnv *env, jclass, jstring key) {

    jboolean isCopy;
    char const *utf= env->GetStringUTFChars(key, &isCopy);

    char const *value= getenv(utf);

    env->ReleaseStringUTFChars(key, utf);

    if(!value || !*value)
        return 0;

    return env->NewStringUTF(value);
}


#ifdef __cplusplus
}
#endif

