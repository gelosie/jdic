# Microsoft Developer Studio Generated NMAKE File, Based on jdic.dsp
!IF "$(CFG)" == ""
CFG=jdic - Win32 Debug
!MESSAGE No configuration specified. Defaulting to jdic - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "jdic - Win32 Release" && "$(CFG)" != "jdic - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "jdic.mak" CFG="jdic - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "jdic - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "jdic - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
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

!IF  "$(CFG)" == "jdic - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\jdic.dll"


CLEAN :
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\WebBrowser.obj"
	-@erase "$(INTDIR)\WinAPIWrapper.obj"
	-@erase "$(INTDIR)\WinRegistryWrapper.obj"
	-@erase "$(OUTDIR)\jdic.dll"
	-@erase "$(OUTDIR)\jdic.exp"
	-@erase "$(OUTDIR)\jdic.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /Fp"$(INTDIR)\jdic.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c /I $(JAVA_HOME)\include /I $(JAVA_HOME)\include\win32
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\jdic.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\jdic.pdb" /machine:I386 /out:"$(OUTDIR)\jdic.dll" /implib:"$(OUTDIR)\jdic.lib" 
LINK32_OBJS= \
	"$(INTDIR)\WinAPIWrapper.obj" \
	"$(INTDIR)\WebBrowser.obj" \
	"$(INTDIR)\WinRegistryWrapper.obj"

"$(OUTDIR)\jdic.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "jdic - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug

ALL : ".\jdic.dll"


CLEAN :
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(INTDIR)\WebBrowser.obj"
	-@erase "$(INTDIR)\WinAPIWrapper.obj"
	-@erase "$(INTDIR)\WinRegistryWrapper.obj"
	-@erase "$(OUTDIR)\jdic.exp"
	-@erase "$(OUTDIR)\jdic.lib"
	-@erase "$(OUTDIR)\jdic.pdb"
	-@erase ".\jdic.dll"
	-@erase ".\jdic.ilk"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "JDIC_EXPORTS" /Fp"$(INTDIR)\jdic.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\jdic.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib shlwapi.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\jdic.pdb" /debug /machine:I386 /out:"jdic.dll" /implib:"$(OUTDIR)\jdic.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\WinAPIWrapper.obj" \
	"$(INTDIR)\WebBrowser.obj" \
	"$(INTDIR)\WinRegistryWrapper.obj"

".\jdic.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
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
!IF EXISTS("jdic.dep")
!INCLUDE "jdic.dep"
!ELSE 
!MESSAGE Warning: cannot find "jdic.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "jdic - Win32 Release" || "$(CFG)" == "jdic - Win32 Debug"
SOURCE=.\WebBrowser.cpp

"$(INTDIR)\WebBrowser.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\WinAPIWrapper.cpp

"$(INTDIR)\WinAPIWrapper.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\WinRegistryWrapper.cpp

"$(INTDIR)\WinRegistryWrapper.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

