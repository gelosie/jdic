# Microsoft Developer Studio Generated NMAKE File, Based on MozEmbed.dsp
!IF "$(CFG)" == ""
CFG=MozEmbed - Win32 Debug
!MESSAGE No configuration specified. Defaulting to MozEmbed - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "MozEmbed - Win32 Release" && "$(CFG)" != "MozEmbed - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
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
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
MTL=midl.exe
RSC=rc.exe

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\MozEmbed.exe"


CLEAN :
	-@erase "$(INTDIR)\BrowserFrm.obj"
	-@erase "$(INTDIR)\BrowserImpl.obj"
	-@erase "$(INTDIR)\BrowserToolTip.obj"
	-@erase "$(INTDIR)\BrowserView.obj"
	-@erase "$(INTDIR)\Common.obj"
	-@erase "$(INTDIR)\Dialogs.obj"
	-@erase "$(INTDIR)\MozEmbed.obj"
	-@erase "$(INTDIR)\MozEmbed.pch"
	-@erase "$(INTDIR)\MozEmbed.res"
	-@erase "$(INTDIR)\MsgServer.obj"
	-@erase "$(INTDIR)\ProfileDirServiceProvider.obj"
	-@erase "$(INTDIR)\PromptService.obj"
	-@erase "$(INTDIR)\StdAfx.obj"
	-@erase "$(INTDIR)\Util.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\MozEmbed.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\MozEmbed.res" /d "NDEBUG" /d "_AFXDLL" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\MozEmbed.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=Ws2_32.lib xpcomglue.lib string_s.lib string_obsolete_s.lib embedstring_s.lib nspr4.lib plc4.lib plds4.lib ole32.lib comdlg32.lib shell32.lib version.lib /nologo /subsystem:windows /incremental:no /pdb:"$(OUTDIR)\MozEmbed.pdb" /machine:I386 /out:"$(OUTDIR)\MozEmbed.exe" /libpath:"$(MOZILLA_SRC_HOME)/dist/lib" 
LINK32_OBJS= \
	"$(INTDIR)\BrowserFrm.obj" \
	"$(INTDIR)\BrowserImpl.obj" \
	"$(INTDIR)\BrowserToolTip.obj" \
	"$(INTDIR)\BrowserView.obj" \
	"$(INTDIR)\Common.obj" \
	"$(INTDIR)\Dialogs.obj" \
	"$(INTDIR)\MozEmbed.obj" \
	"$(INTDIR)\MsgServer.obj" \
	"$(INTDIR)\ProfileDirServiceProvider.obj" \
	"$(INTDIR)\PromptService.obj" \
	"$(INTDIR)\StdAfx.obj" \
	"$(INTDIR)\Util.obj" \
	"$(INTDIR)\MozEmbed.res"

"$(OUTDIR)\MozEmbed.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\MozEmbed.exe" "$(OUTDIR)\MozEmbed.bsc"


CLEAN :
	-@erase "$(INTDIR)\BrowserFrm.obj"
	-@erase "$(INTDIR)\BrowserFrm.sbr"
	-@erase "$(INTDIR)\BrowserImpl.obj"
	-@erase "$(INTDIR)\BrowserImpl.sbr"
	-@erase "$(INTDIR)\BrowserToolTip.obj"
	-@erase "$(INTDIR)\BrowserToolTip.sbr"
	-@erase "$(INTDIR)\BrowserView.obj"
	-@erase "$(INTDIR)\BrowserView.sbr"
	-@erase "$(INTDIR)\Common.obj"
	-@erase "$(INTDIR)\Common.sbr"
	-@erase "$(INTDIR)\Dialogs.obj"
	-@erase "$(INTDIR)\Dialogs.sbr"
	-@erase "$(INTDIR)\MozEmbed.obj"
	-@erase "$(INTDIR)\MozEmbed.pch"
	-@erase "$(INTDIR)\MozEmbed.res"
	-@erase "$(INTDIR)\MozEmbed.sbr"
	-@erase "$(INTDIR)\MsgServer.obj"
	-@erase "$(INTDIR)\MsgServer.sbr"
	-@erase "$(INTDIR)\ProfileDirServiceProvider.obj"
	-@erase "$(INTDIR)\ProfileDirServiceProvider.sbr"
	-@erase "$(INTDIR)\PromptService.obj"
	-@erase "$(INTDIR)\PromptService.sbr"
	-@erase "$(INTDIR)\StdAfx.obj"
	-@erase "$(INTDIR)\StdAfx.sbr"
	-@erase "$(INTDIR)\Util.obj"
	-@erase "$(INTDIR)\Util.sbr"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\MozEmbed.bsc"
	-@erase "$(OUTDIR)\MozEmbed.exe"
	-@erase "$(OUTDIR)\MozEmbed.ilk"
	-@erase "$(OUTDIR)\MozEmbed.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\MozEmbed.res" /d "_DEBUG" /d "_AFXDLL" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\MozEmbed.bsc" 
BSC32_SBRS= \
	"$(INTDIR)\BrowserFrm.sbr" \
	"$(INTDIR)\BrowserImpl.sbr" \
	"$(INTDIR)\BrowserToolTip.sbr" \
	"$(INTDIR)\BrowserView.sbr" \
	"$(INTDIR)\Common.sbr" \
	"$(INTDIR)\Dialogs.sbr" \
	"$(INTDIR)\MozEmbed.sbr" \
	"$(INTDIR)\MsgServer.sbr" \
	"$(INTDIR)\ProfileDirServiceProvider.sbr" \
	"$(INTDIR)\PromptService.sbr" \
	"$(INTDIR)\StdAfx.sbr" \
	"$(INTDIR)\Util.sbr"

"$(OUTDIR)\MozEmbed.bsc" : "$(OUTDIR)" $(BSC32_SBRS)
    $(BSC32) @<<
  $(BSC32_FLAGS) $(BSC32_SBRS)
<<

LINK32=link.exe
LINK32_FLAGS=xpcomglue.lib string_s.lib string_obsolete_s.lib embedstring_s.lib nspr4.lib plc4.lib plds4.lib ole32.lib comdlg32.lib shell32.lib version.lib Ws2_32.lib /nologo /subsystem:windows /incremental:yes /pdb:"$(OUTDIR)\MozEmbed.pdb" /debug /machine:I386 /out:"$(OUTDIR)\MozEmbed.exe" /pdbtype:sept /libpath:"$(MOZILLA_SRC_HOME)/dist/lib" 
LINK32_OBJS= \
	"$(INTDIR)\BrowserFrm.obj" \
	"$(INTDIR)\BrowserImpl.obj" \
	"$(INTDIR)\BrowserToolTip.obj" \
	"$(INTDIR)\BrowserView.obj" \
	"$(INTDIR)\Common.obj" \
	"$(INTDIR)\Dialogs.obj" \
	"$(INTDIR)\MozEmbed.obj" \
	"$(INTDIR)\MsgServer.obj" \
	"$(INTDIR)\ProfileDirServiceProvider.obj" \
	"$(INTDIR)\PromptService.obj" \
	"$(INTDIR)\StdAfx.obj" \
	"$(INTDIR)\Util.obj" \
	"$(INTDIR)\MozEmbed.res"

"$(OUTDIR)\MozEmbed.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.c{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cxx{$(INTDIR)}.sbr::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("MozEmbed.dep")
!INCLUDE "MozEmbed.dep"
!ELSE 
!MESSAGE Warning: cannot find "MozEmbed.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "MozEmbed - Win32 Release" || "$(CFG)" == "MozEmbed - Win32 Debug"
SOURCE=.\BrowserFrm.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\BrowserFrm.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\BrowserFrm.obj"	"$(INTDIR)\BrowserFrm.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\BrowserImpl.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\BrowserImpl.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\BrowserImpl.obj"	"$(INTDIR)\BrowserImpl.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\BrowserToolTip.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\BrowserToolTip.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\BrowserToolTip.obj"	"$(INTDIR)\BrowserToolTip.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\BrowserView.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\BrowserView.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\BrowserView.obj"	"$(INTDIR)\BrowserView.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=..\..\..\share\native\mozilla\Common.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\Common.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\Common.obj"	"$(INTDIR)\Common.sbr" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\Dialogs.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\Dialogs.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\Dialogs.obj"	"$(INTDIR)\Dialogs.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\MozEmbed.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\MozEmbed.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\MozEmbed.obj"	"$(INTDIR)\MozEmbed.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\MozEmbed.rc

"$(INTDIR)\MozEmbed.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)


SOURCE=..\..\..\share\native\utils\MsgServer.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\MsgServer.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\MsgServer.obj"	"$(INTDIR)\MsgServer.sbr" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=..\..\..\share\native\mozilla\ProfileDirServiceProvider.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\ProfileDirServiceProvider.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\ProfileDirServiceProvider.obj"	"$(INTDIR)\ProfileDirServiceProvider.sbr" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\PromptService.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\PromptService.obj" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yu"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\PromptService.obj"	"$(INTDIR)\PromptService.sbr" : $(SOURCE) "$(INTDIR)" "$(INTDIR)\MozEmbed.pch"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=.\StdAfx.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fp"$(INTDIR)\MozEmbed.pch" /Yc"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\StdAfx.obj"	"$(INTDIR)\MozEmbed.pch" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fp"$(INTDIR)\MozEmbed.pch" /Yc"stdafx.h" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\StdAfx.obj"	"$(INTDIR)\StdAfx.sbr"	"$(INTDIR)\MozEmbed.pch" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 

SOURCE=..\..\..\share\native\utils\Util.cpp

!IF  "$(CFG)" == "MozEmbed - Win32 Release"

CPP_SWITCHES=/nologo /MD /W3 /GX /O2 /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "NDEBUG" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /D "XPCOM_GLUE" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

"$(INTDIR)\Util.obj" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ELSEIF  "$(CFG)" == "MozEmbed - Win32 Debug"

CPP_SWITCHES=/nologo /MDd /W3 /Gm /GX /ZI /Od /I "../../../share/native/mozilla" /I "../../../share/native/utils" /I "$(MOZILLA_SRC_HOME)/dist/include/xpcom" /I "$(MOZILLA_SRC_HOME)/dist/include/string" /I "$(MOZILLA_SRC_HOME)/dist/include/necko" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrwsr" /I "$(MOZILLA_SRC_HOME)/dist/include/widget" /I "$(MOZILLA_SRC_HOME)/dist/include/dom" /I "$(MOZILLA_SRC_HOME)/dist/include/uriloader" /I "$(MOZILLA_SRC_HOME)/dist/include/embed_base" /I "$(MOZILLA_SRC_HOME)/dist/include/webshell" /I "$(MOZILLA_SRC_HOME)/dist/include/shistory" /I "$(MOZILLA_SRC_HOME)/dist/include/pref" /I "$(MOZILLA_SRC_HOME)/dist/include/profile" /I "$(MOZILLA_SRC_HOME)/dist/include/find" /I "$(MOZILLA_SRC_HOME)/dist/include/gfx" /I "$(MOZILLA_SRC_HOME)/dist/include/windowwatcher" /I "$(MOZILLA_SRC_HOME)/dist/include/layout" /I "$(MOZILLA_SRC_HOME)/dist/include/webbrowserpersist" /I "$(MOZILLA_SRC_HOME)/dist/include/composer" /I "$(MOZILLA_SRC_HOME)/dist/include/commandhandler" /I "$(MOZILLA_SRC_HOME)/dist/include/imglib2" /I "$(MOZILLA_SRC_HOME)/dist/include" /I "$(MOZILLA_SRC_HOME)/dist/include/nspr" /I "$(MOZILLA_SRC_HOME)/dist/include/docshell" /I\
 "$(MOZILLA_SRC_HOME)/dist/include/embedstring" /D "_DEBUG" /D "DEBUG" /D "USE_SINGLE_SIGN_ON" /D HAVE_MMINTRIN_H=1 /D HAVE_SNPRINTF=1 /D _WINDOWS=1 /D _WIN32=1 /D WIN32=1 /D XP_PC=1 /D XP_WIN=1 /D XP_WIN32=1 /D HW_THREADS=1 /D WINVER=0x400 /D MSVC4=1 /D STDC_HEADERS=1 /D NEW_H=<new> /D WIN32_LEAN_AND_MEAN=1 /D NO_X11=1 /D _X86_=1 /D D_INO=d_ino /D MOZ_DEFAULT_TOOLKIT="windows" /D MOZ_ENABLE_COREXFONTS=1 /D IBMBIDI=1 /D ACCESSIBILITY=1 /D MOZ_LOGGING=1 /D DETECT_WEBSHELL_LEAKS=1 /D CPP_THROW_NEW=throw() /D MOZ_XUL=1 /D INCLUDE_XUL=1 /D NS_MT_SUPPORTED=1 /D JS_THREADSAFE=1 /D NS_PRINT_PREVIEW=1 /D NS_PRINTING=1 /D MOZ_REFLOW_PERF=1 /D MOZ_REFLOW_PERF_DSP=1 /D MOZILLA_VERSION=\"1.3\" /D "_MOZILLA_CONFIG_H_" /D "MOZILLA_CLIENT" /D "XPCOM_GLUE" /D "WIN32" /D "_WINDOWS" /D "_AFXDLL" /D "_MBCS" /FR"$(INTDIR)\\" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /I /dist/include/docshell" /GZ " /c 

"$(INTDIR)\Util.obj"	"$(INTDIR)\Util.sbr" : $(SOURCE) "$(INTDIR)"
	$(CPP) @<<
  $(CPP_SWITCHES) $(SOURCE)
<<


!ENDIF 


!ENDIF 

