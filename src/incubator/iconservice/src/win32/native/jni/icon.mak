
!IF "$(OS)" == "Windows_NT"
NULL=
!ELSE 
NULL=nul
!ENDIF 

CPP=cl.exe
LINK32=link.exe

!IF  "$(DEBUG)" != "ON"

OUTDIR=.
INTDIR=.\Release

CPP_PROJ=/nologo /MT /W3 /GX /O2 /D "WIN32" /D "NDEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "ICON_EXPORTS" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD
	
LINK32_FLAGS=/nologo /dll /incremental:no /pdb:"$(OUTDIR)\icon.pdb" /machine:I386 /out:"$(OUTDIR)\jdic_icon.dll"

!ELSE

OUTDIR=.
INTDIR=.\Debug

CPP_PROJ=/nologo /MTd /W3 /Gm /GX /ZI /Od /D "WIN32" /D "_DEBUG" /D "_WINDOWS" /D "_MBCS" /D "_USRDLL" /D "ICON_EXPORTS" /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /GZ
	
LINK32_FLAGS=/nologo /dll /incremental:yes /pdb:"$(OUTDIR)\icon.pdb" /debug /machine:I386 /out:"$(OUTDIR)\jdic_icon.dll" /pdbtype:sept

!ENDIF 

ALL : "$(OUTDIR)\jdic_icon.dll"

LINK_LIBS= 

LINK32_OBJS= \
	"$(INTDIR)\WinIconWrapper.obj"

"$(OUTDIR)\jdic_icon.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS)
    $(LINK32) @<<
  $(LINK32_FLAGS) $(LINK32_LIBS) $(LINK32_OBJS)
<<

"$(INTDIR)" :
    if not exist "$(INTDIR)/$(NULL)" mkdir "$(INTDIR)"

CLEAN :
	-@erase "$(OUTDIR)\*.?db"
	-@erase "$(OUTDIR)\*.dll"
	-@erase "$(INTDIR)\WinIconWrapper.obj"

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   /c /I $(JAVA_HOME)\include /I $(JAVA_HOME)\include\win32 $(CPP_PROJ) $< 
<<

"$(INTDIR)\WinIconWrapper.obj" : .\WinIconWrapper.cpp "$(INTDIR)"

