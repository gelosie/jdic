# Microsoft Developer Studio Generated NMAKE File, Based on bootstrapper.dsp
!IF "$(CFG)" == ""
CFG=bootstrapper - Win32 Debug
!MESSAGE No configuration specified. Defaulting to bootstrapper - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "bootstrapper - Win32 Release" && "$(CFG)" != "bootstrapper - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "bootstrapper.mak" CFG="bootstrapper - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "bootstrapper - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "bootstrapper - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

!IF  "$(CFG)" == "bootstrapper - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\bootstrapper.exe"


CLEAN :
	-@erase "$(INTDIR)\bootstrapper.obj"
	-@erase "$(INTDIR)\bootstrapper.res"
	-@erase "$(INTDIR)\bootstrapper_util.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\bootstrapper.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /ML /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 

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

RSC=rc.exe
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\bootstrapper.res" /d "NDEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\bootstrapper.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib strsafe.lib Version.lib Msi.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\bootstrapper.pdb" /machine:I386 /out:"$(OUTDIR)\bootstrapper.exe" 
LINK32_OBJS= \
	"$(INTDIR)\bootstrapper.obj" \
	"$(INTDIR)\bootstrapper_util.obj" \
	"$(INTDIR)\bootstrapper.res"

"$(OUTDIR)\bootstrapper.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "bootstrapper - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\bootstrapper.exe"


CLEAN :
	-@erase "$(INTDIR)\bootstrapper.obj"
	-@erase "$(INTDIR)\bootstrapper.res"
	-@erase "$(INTDIR)\bootstrapper_util.obj"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\bootstrapper.exe"
	-@erase "$(OUTDIR)\bootstrapper.ilk"
	-@erase "$(OUTDIR)\bootstrapper.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP=cl.exe
CPP_PROJ=/nologo /MLd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 

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

RSC=rc.exe
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\bootstrapper.res" /d "_DEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\bootstrapper.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib strsafe.lib Version.lib Msi.lib /nologo /subsystem:console /incremental:yes /pdb:"$(OUTDIR)\bootstrapper.pdb" /debug /machine:I386 /out:"$(OUTDIR)\bootstrapper.exe" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\bootstrapper.obj" \
	"$(INTDIR)\bootstrapper_util.obj" \
	"$(INTDIR)\bootstrapper.res"

"$(OUTDIR)\bootstrapper.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ENDIF 


!IF "$(NO_EXTERNAL_DEPS)" != "1"
!IF EXISTS("bootstrapper.dep")
!INCLUDE "bootstrapper.dep"
!ELSE 
!MESSAGE Warning: cannot find "bootstrapper.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "bootstrapper - Win32 Release" || "$(CFG)" == "bootstrapper - Win32 Debug"
SOURCE=.\bootstrapper.cpp

"$(INTDIR)\bootstrapper.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\bootstrapper_util.cpp

"$(INTDIR)\bootstrapper_util.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\bootstrapper.rc

"$(INTDIR)\bootstrapper.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)



!ENDIF 

