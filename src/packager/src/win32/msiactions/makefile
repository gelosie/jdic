# Microsoft Developer Studio Generated NMAKE File, Based on MsiActions.dsp
!IF "$(CFG)" == ""
CFG=MsiActions - Win32 Release
!MESSAGE No configuration specified. Defaulting to MsiActions - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "MsiActions - Win32 Release" && "$(CFG)" != "MsiActions - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "MsiActions.mak" CFG="MsiActions - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "MsiActions - Win32 Release" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE "MsiActions - Win32 Debug" (based on "Win32 (x86) Dynamic-Link Library")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "MsiActions - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\MsiActions.dll"


CLEAN :
	-@erase "$(INTDIR)\MsiActions.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\MsiActions.dll"
	-@erase "$(OUTDIR)\MsiActions.exp"
	-@erase "$(OUTDIR)\MsiActions.lib"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "MSIACTIONS_EXPORTS" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

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

MTL=midl.exe
MTL_PROJ=/nologo /D "NDEBUG" /mktyplib203 /win32 
RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\MsiActions.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib msi.lib /nologo /dll /incremental:no /pdb:"$(OUTDIR)\MsiActions.pdb" /machine:I386 /def:".\MsiActions.def" /out:"$(OUTDIR)\MsiActions.dll" /implib:"$(OUTDIR)\MsiActions.lib" 
DEF_FILE= \
	".\MsiActions.def"
LINK32_OBJS= \
	"$(INTDIR)\MsiActions.obj"

"$(OUTDIR)\MsiActions.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "MsiActions - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\MsiActions.dll"


CLEAN :
	-@erase "$(INTDIR)\MsiActions.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\MsiActions.dll"
	-@erase "$(OUTDIR)\MsiActions.exp"
	-@erase "$(OUTDIR)\MsiActions.ilk"
	-@erase "$(OUTDIR)\MsiActions.lib"
	-@erase "$(OUTDIR)\MsiActions.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "MSIACTIONS_EXPORTS" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

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

MTL=midl.exe
MTL_PROJ=/nologo /D "_DEBUG" /mktyplib203 /win32 
RSC=rc.exe
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\MsiActions.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib msi.lib /nologo /dll /incremental:yes /pdb:"$(OUTDIR)\MsiActions.pdb" /debug /machine:I386 /def:".\MsiActions.def" /out:"$(OUTDIR)\MsiActions.dll" /implib:"$(OUTDIR)\MsiActions.lib" /pdbtype:sept 
DEF_FILE= \
	".\MsiActions.def"
LINK32_OBJS= \
	"$(INTDIR)\MsiActions.obj"

"$(OUTDIR)\MsiActions.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("MsiActions.dep")
!INCLUDE "MsiActions.dep"
!ELSE 
!MESSAGE Warning: cannot find "MsiActions.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "MsiActions - Win32 Release" || "$(CFG)" == "MsiActions - Win32 Debug"
SOURCE=.\MsiActions.cpp

"$(INTDIR)\MsiActions.obj" : $(SOURCE) "$(INTDIR)"



!ENDIF 

