CPP=cl.exe

OUTDIR=.
INTDIR=.

ALL :  "$(OUTDIR)\Tray.dll" 

CLEAN :
	-@erase "$(INTDIR)\vc60.idb"
	-@erase "$(INTDIR)\vc60.pch"
	-@erase "$(INTDIR)\vc60.pdb"
	-@erase "$(INTDIR)\Tray.obj"
	-@erase "$(OUTDIR)\Tray.dll"
	-@erase "$(OUTDIR)\Tray.exp"
	-@erase "$(OUTDIR)\Tray.lib"
	-@erase "$(OUTDIR)\Tray.pdb"
	

CPP_PROJ=/nologo /Zi /MTd /W3 /GX /O2 /YX /Fo"$(INTDIR)\\" /Fd"$(INTDIR)\\" /FD /c  /I $(JAVA_HOME)\include /I $(JAVA_HOME)\include\win32
	
LINK32=link.exe
LINK32_FLAGS=kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /DEBUG /nologo /dll /incremental:no /machine:I386 /WARN:1

LINK32_OBJS_Tray= \
	"$(INTDIR)\Tray.obj"
	
"$(OUTDIR)\Tray.dll" : "$(OUTDIR)" $(DEF_FILE) $(LINK32_OBJS_Tray)
    -@$(LINK32) @<<
  $(LINK32_FLAGS) /out:"$@" $(LINK32_OBJS_Tray)
<<

.c{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

.cpp{$(INTDIR)}.obj::
   $(CPP) @<<
   $(CPP_PROJ) $< 
<<

SOURCE_Tray=Tray.cpp

"$(INTDIR)\Tray.obj" : $(SOURCE_Tray) "$(INTDIR)"
	-@$(CPP) $(CPP_PROJ) $(SOURCE_Tray)
	
