# Microsoft Developer Studio Generated NMAKE File, Based on jnlp2msi.dsp
!IF "$(CFG)" == ""
CFG=jnlp2msi - Win32 Debug
!MESSAGE No configuration specified. Defaulting to jnlp2msi - Win32 Debug.
!ENDIF 

!IF "$(CFG)" != "jnlp2msi - Win32 Release" && "$(CFG)" != "jnlp2msi - Win32 Debug"
!MESSAGE Invalid configuration "$(CFG)" specified.
!MESSAGE You can specify a configuration when running NMAKE
!MESSAGE by defining the macro CFG on the command line. For example:
!MESSAGE 
!MESSAGE NMAKE /f "jnlp2msi.mak" CFG="jnlp2msi - Win32 Debug"
!MESSAGE 
!MESSAGE Possible choices for configuration are:
!MESSAGE 
!MESSAGE "jnlp2msi - Win32 Release" (based on "Win32 (x86) Console Application")
!MESSAGE "jnlp2msi - Win32 Debug" (based on "Win32 (x86) Console Application")
!MESSAGE 
!ERROR An invalid configuration is specified.
!ENDIF 

!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
RSC=rc.exe

!IF  "$(CFG)" == "jnlp2msi - Win32 Release"

OUTDIR=.\Release
INTDIR=.\Release
# Begin Custom Macros
OutDir=.\Release
# End Custom Macros

ALL : "$(OUTDIR)\jnlp2msi.exe"


CLEAN :
	-@erase "$(INTDIR)\jnlp2msi.obj"
	-@erase "$(INTDIR)\jnlp2msi.res"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(OUTDIR)\jnlp2msi.exe"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /ML /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\jnlp2msi.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c 
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\jnlp2msi.res" /d "NDEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\jnlp2msi.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /incremental:no /pdb:"$(OUTDIR)\jnlp2msi.pdb" /machine:I386 /out:"$(OUTDIR)\jnlp2msi.exe" 
LINK32_OBJS= \
	"$(INTDIR)\jnlp2msi.obj" \
	"$(INTDIR)\jnlp2msi.res"

"$(OUTDIR)\jnlp2msi.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_OBJS)
<<

!ELSEIF  "$(CFG)" == "jnlp2msi - Win32 Debug"

OUTDIR=.\Debug
INTDIR=.\Debug
# Begin Custom Macros
OutDir=.\Debug
# End Custom Macros

ALL : "$(OUTDIR)\jnlp2msi.exe"


CLEAN :
	-@erase "$(INTDIR)\jnlp2msi.obj"
	-@erase "$(INTDIR)\jnlp2msi.res"
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(OUTDIR)\jnlp2msi.exe"
	-@erase "$(OUTDIR)\jnlp2msi.ilk"
	-@erase "$(OUTDIR)\jnlp2msi.pdb"

"$(OUTDIR)" :
    if not exist "$(OUTDIR)/$(NULL)" mkdir "$(OUTDIR)"

CPP_PROJ=/nologo /MLd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_CONSOLE" /D "_MBCS" /Fp"$(INTDIR)\jnlp2msi.pch" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ /c 
RSC_PROJ=/l 0x804 /fo"$(INTDIR)\jnlp2msi.res" /d "_DEBUG" 
BSC32=bscmake.exe
BSC32_FLAGS=/nologo /o"$(OUTDIR)\jnlp2msi.bsc" 
BSC32_SBRS= \
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /nologo /subsystem:console /incremental:yes /pdb:"$(OUTDIR)\jnlp2msi.pdb" /debug /machine:I386 /out:"$(OUTDIR)\jnlp2msi.exe" /pdbtype:sept 
LINK32_OBJS= \
	"$(INTDIR)\jnlp2msi.obj" \
	"$(INTDIR)\jnlp2msi.res"

"$(OUTDIR)\jnlp2msi.exe" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
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
!IF EXISTS("jnlp2msi.dep")
!INCLUDE "jnlp2msi.dep"
!ELSE 
!MESSAGE Warning: cannot find "jnlp2msi.dep"
!ENDIF 
!ENDIF 


!IF "$(CFG)" == "jnlp2msi - Win32 Release" || "$(CFG)" == "jnlp2msi - Win32 Debug"
SOURCE=.\jnlp2msi.cpp

"$(INTDIR)\jnlp2msi.obj" : $(SOURCE) "$(INTDIR)"


SOURCE=.\jnlp2msi.rc

"$(INTDIR)\jnlp2msi.res" : $(SOURCE) "$(INTDIR)"
	$(RSC) $(RSC_PROJ) $(SOURCE)



!ENDIF 

