# Microsoft Developer Studio Project File - Name="jdic" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Dynamic-Link Library" 0x0102

CFG=jdic - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "jdic.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "jdic.mak" CFG="jdic - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "jdic - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "jdic - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "jdic - Win32 Tray" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "jdic - Win32 Release"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /I "$(JAVA_HOME)/include" /I "$(JAVA_HOME)/include/win32" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x804 /d "NDEBUG"
# ADD RSC /l 0x804 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /machine:I386
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib shlwapi.lib /nologo /dll /machine:I386 /force:multiple
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /YX /FD /GZ /c
# ADD CPP /nologo /MTd /W3 /Gm /GX /ZI /Od /I "$(JAVA_HOME)/include" /I "$(JAVA_HOME)/include/win32" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /YX /FD /GZ /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x804 /d "_DEBUG"
# ADD RSC /l 0x804 /d "_DEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /debug /machine:I386 /pdbtype:sept
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib shlwapi.lib /nologo /dll /debug /machine:I386 /pdbtype:sept /force:multiple
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP BASE Use_MFC 0
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "jdic___Win32_Tray"
# PROP BASE Intermediate_Dir "jdic___Win32_Tray"
# PROP BASE Ignore_Export_Lib 0
# PROP BASE Target_Dir ""
# PROP Use_MFC 0
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MT /W3 /GX /O2 /I "$(JAVA_HOME)/include" /I "$(JAVA_HOME)/include/win32" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /YX /FD /c
# ADD CPP /nologo /MT /W3 /GX /O2 /I "$(JAVA_HOME)/include" /I "$(JAVA_HOME)/include/win32" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /D _WIN32_IE=0x0501 /YX /FD /c
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x804 /d "NDEBUG"
# ADD RSC /l 0x804 /d "NDEBUG"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib shlwapi.lib /nologo /dll /machine:I386 /force:multiple
# SUBTRACT BASE LINK32 /pdb:none
# ADD LINK32 kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib shlwapi.lib /nologo /dll /machine:I386 /out:"Release/tray.dll" /force:multiple
# SUBTRACT LINK32 /pdb:none

!ENDIF 

# Begin Target

# Name "jdic - Win32 Release"
# Name "jdic - Win32 Debug"
# Name "jdic - Win32 Tray"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=..\..\..\share\native\utils\InitUtility.cpp

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\jdic.rc
# End Source File
# Begin Source File

SOURCE=.\JNIloader.cpp
# End Source File
# Begin Source File

SOURCE=.\Tray.cpp

!IF  "$(CFG)" == "jdic - Win32 Release"

# PROP Exclude_From_Build 1

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

# PROP Exclude_From_Build 1

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WebBrowser.cpp

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WebBrowserUtil.cpp

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WinAPIWrapper.cpp

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WinRegistryWrapper.cpp

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\DisplayThread.h

!IF  "$(CFG)" == "jdic - Win32 Release"

# PROP Exclude_From_Build 1

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

# PROP Exclude_From_Build 1

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\utils\InitUtility.h

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\JNIloader.h
# End Source File
# Begin Source File

SOURCE=.\WebBrowser.h

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WebBrowserUtil.h

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WinAPIWrapper.h

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WinRegistryWrapper.h

!IF  "$(CFG)" == "jdic - Win32 Release"

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

!ELSEIF  "$(CFG)" == "jdic - Win32 Tray"

# PROP Exclude_From_Build 1

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\WinSystemTrayService.h
# PROP Exclude_From_Build 1
# End Source File
# Begin Source File

SOURCE=.\WinTrayIconService.h
# PROP Exclude_From_Build 1
# End Source File
# End Group
# Begin Group "Resource Files"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe"
# End Group
# End Target
# End Project
