# Microsoft Developer Studio Generated NMAKE File, Based on packager.dsp
!IF "$(CFG)" == ""
CFG=packager - Win32 Debug
!MESSAGE No configuration specified. Defaulting to packager - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "packager - Win32 Release" && "$(CFG)" != "packager - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "packager.mak" CFG="packager - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "packager - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "packager - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
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

!IF  "$(CFG)" == "packager - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\WinMsiWrapper.dll"


CLEAN :
	-@erase "$(INTDIR)\packager.res"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\WinMsiWrapper.obj"
	-@erase "$(OUTDIR)\WinMsiWrapper.dll"
	-@erase "$(OUTDIR)\WinMsiWrapper.exp"
	-@erase "$(OUTDIR)\WinMsiWrapper.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MT /W3 /GX /O2 /I "$(JAVA_HOME)/include" /I "$(JAVA_HOME)/include/win32" /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "PACKAGER_EXPORTS" /Fp"$(INTDIR)\packager.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\packager.res" /d "NDEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\packager.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib Msi.lib Rpcrt4.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\WinMsiWrapper.pdb" /machine:I386 /out:"$(OUTDIR)\WinMsiWrapper.dll" /implib:"$(OUTDIR)\WinMsiWrapper.lib" 
LINK32_OBJS= \
	"$(INTDIR)\WinMsiWrapper.obj" \
	"$(INTDIR)\packager.res"

"$(OUTDIR)\WinMsiWrapper.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "packager - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\WinMsiWrapper.dll"


CLEAN :
	-@erase "$(INTDIR)\packager.res"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(INTDIR)\WinMsiWrapper.obj"
	-@erase "$(OUTDIR)\WinMsiWrapper.dll"
	-@erase "$(OUTDIR)\WinMsiWrapper.exp"
	-@erase "$(OUTDIR)\WinMsiWrapper.ilk"
	-@erase "$(OUTDIR)\WinMsiWrapper.lib"
	-@erase "$(OUTDIR)\WinMsiWrapper.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /I "$(JAVA_HOME)/include" /I "$(JAVA_HOME)/include/win32" /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "PACKAGER_EXPORTS" /Fp"$(INTDIR)\packager.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\packager.res" /d "_DEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\packager.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib Msi.lib Rpcrt4.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\WinMsiWrapper.pdb" /debug /machine:I386 /out:"$(OUTDIR)\WinMsiWrapper.dll" /implib:"$(OUTDIR)\WinMsiWrapper.lib" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\WinMsiWrapper.obj" \
	"$(INTDIR)\packager.res"

"$(OUTDIR)\WinMsiWrapper.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
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
!IF EXISTS("packager.dep")
!INCLUDE "packager.dep"
!ELSE 
!MESSAGE Warning: cannot find "packager.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "packager - Win32 Release" || "$(CFG)" == "packager - Win32 Debug"
SOURCE=.\packager.rc

"$(INTDIR)\packager.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)


SOURCE=.\WinMsiWrapper.cpp

"$(INTDIR)\WinMsiWrapper.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

