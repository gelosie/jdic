# Microsoft Developer Studio Project File - Name="MozEmbed" - Package Owner=<4>
# Microsoft Developer Studio Generated Build File, Format Version 6.00
# ** DO NOT EDIT **

# TARGTYPE "Win32 (x86) Application" 0x0101

CFG=MozEmbed - Win32 Debug
!MESSAGE This is not a valid makefile. To build this project using NMAKE,
!MESSAGE use the Export Makefile command and run
!MESSAGE 
!MESSAGE NMAKE /f "MozEmbed.mak".
!MESSAGE 
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "MozEmbed.mak" CFG="MozEmbed - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "MozEmbed - Win32 Release" (based on "Win32 (x86) Application")
!MESSAGE "MozEmbed - Win32 Debug" (based on "Win32 (x86) Application")
!MESSAGE 

# Begin Project
# PROP AllowPerConfigDependencies 0
# PROP Scc_ProjName ""
# PROP Scc_LocalPath ""
CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# PROP BASE Use_MFC 6
# PROP BASE Use_Debug_Libraries 0
# PROP BASE Output_Dir "Release"
# PROP BASE Intermediate_Dir "Release"
# PROP BASE Target_Dir ""
# PROP Use_MFC 6
# PROP Use_Debug_Libraries 0
# PROP Output_Dir "Release"
# PROP Intermediate_Dir "Release"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MD /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_AFXDLL" /Yu"stdafx.h" /FD /c
# ADD CPP /nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /FD /c
# SUBTRACT CPP /YX /Yc /Yu
# ADD BASE MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "NDEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x804 /d "NDEBUG" /d "_AFXDLL"
# ADD RSC /l 0x804 /d "NDEBUG" /d "_AFXDLL"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 /nologo /subsystem:windows /machine:I386
# ADD LINK32 Ws2_32.lib xpcomglue.lib string_s.lib string_obsolete_s.lib embedstring_s.lib nspr4.lib plc4.lib plds4.lib ole32.lib comdlg32.lib shell32.lib version.lib /nologo /subsystem:windows /machine:I386 /libpath:"$(MOZILLA_SRC_HOME)/dist/lib"
# SUBTRACT LINK32 /pdb:none

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

# PROP BASE Use_MFC 6
# PROP BASE Use_Debug_Libraries 1
# PROP BASE Output_Dir "Debug"
# PROP BASE Intermediate_Dir "Debug"
# PROP BASE Target_Dir ""
# PROP Use_MFC 6
# PROP Use_Debug_Libraries 1
# PROP Output_Dir "Debug"
# PROP Intermediate_Dir "Debug"
# PROP Ignore_Export_Lib 0
# PROP Target_Dir ""
# ADD BASE CPP /nologo /MDd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_AFXDLL" /Yu"stdafx.h" /FD /GZ /c
# ADD CPP /nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR /Yu"stdafx.h" /FD /I /dist/include/docshell" /GZ " /c
# ADD BASE MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD MTL /nologo /D "_DEBUG" /mktyplib203 /win32
# ADD BASE RSC /l 0x804 /d "_DEBUG" /d "_AFXDLL"
# ADD RSC /l 0x804 /d "_DEBUG" /d "_AFXDLL"
BSC32=bscmake.exe
# ADD BASE BSC32 /nologo
# ADD BSC32 /nologo
LINK32=link.exe
# ADD BASE LINK32 /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept
# ADD LINK32 xpcomglue.lib string_s.lib string_obsolete_s.lib embedstring_s.lib nspr4.lib plc4.lib plds4.lib ole32.lib comdlg32.lib shell32.lib version.lib Ws2_32.lib /nologo /subsystem:windows /debug /machine:I386 /pdbtype:sept /libpath:"$(MOZILLA_SRC_HOME)/dist/lib"
# SUBTRACT LINK32 /pdb:none

!ENDIF 

# Begin Target

# Name "MozEmbed - Win32 Release"
# Name "MozEmbed - Win32 Debug"
# Begin Group "Source Files"

# PROP Default_Filter "cpp;c;cxx;rc;def;r;odl;idl;hpj;bat"
# Begin Source File

SOURCE=.\BrowserFrm.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# ADD CPP /Yu"stdafx.h"

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\BrowserImpl.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# ADD CPP /Yu"stdafx.h"

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\BrowserToolTip.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# ADD CPP /Yu"stdafx.h"

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\BrowserView.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# ADD CPP /Yu"stdafx.h"

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\mozilla\Common.cpp
# SUBTRACT CPP /YX /Yc /Yu
# End Source File
# Begin Source File

SOURCE=.\Dialogs.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# ADD CPP /Yu"stdafx.h"

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\MozEmbed.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# ADD CPP /Yu"stdafx.h"

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\MozEmbed.rc
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\utils\MsgServer.cpp
# SUBTRACT CPP /YX /Yc /Yu
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\mozilla\ProfileDirServiceProvider.cpp
# SUBTRACT CPP /YX /Yc /Yu
# End Source File
# Begin Source File

SOURCE=.\PromptService.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

# ADD CPP /Yu"stdafx.h"

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

!ENDIF 

# End Source File
# Begin Source File

SOURCE=.\StdAfx.cpp
# ADD CPP /Yc"stdafx.h"
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\utils\Util.cpp
# SUBTRACT CPP /YX /Yc /Yu
# End Source File
# End Group
# Begin Group "Header Files"

# PROP Default_Filter "h;hpp;hxx;hm;inl"
# Begin Source File

SOURCE=.\BrowserFrm.h
# End Source File
# Begin Source File

SOURCE=.\BrowserImpl.h
# End Source File
# Begin Source File

SOURCE=.\BrowserToolTip.h
# End Source File
# Begin Source File

SOURCE=.\BrowserView.h
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\mozilla\Common.h
# End Source File
# Begin Source File

SOURCE=.\Dialogs.h
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\utils\Message.h
# End Source File
# Begin Source File

SOURCE=.\MozEmbed.h
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\utils\MsgServer.h
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\mozilla\ProfileDirServiceProvider.h
# End Source File
# Begin Source File

SOURCE=.\PromptService.h
# End Source File
# Begin Source File

SOURCE=.\Resource.h
# End Source File
# Begin Source File

SOURCE=.\StdAfx.h
# End Source File
# Begin Source File

SOURCE=..\..\..\share\native\utils\Util.h
# End Source File
# End Group
# Begin Group "Resource Files"

# PROP Default_Filter "ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe"
# Begin Source File

SOURCE=.\res\broken.ico
# End Source File
# Begin Source File

SOURCE=.\res\mozembed.ico
# End Source File
# Begin Source File

SOURCE=.\res\MozEmbed.ico
# End Source File
# Begin Source File

SOURCE=.\res\MozEmbed.rc2
# End Source File
# Begin Source File

SOURCE=.\res\sinsecur.ico
# End Source File
# Begin Source File

SOURCE=.\res\ssecur.ico
# End Source File
# Begin Source File

SOURCE=.\res\Toolbar.bmp
# End Source File
# End Group
# End Target
# End Project
