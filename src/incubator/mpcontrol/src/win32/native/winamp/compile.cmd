@echo off
rem gcc -Wall -mno-cygwin -shared -Wl,--add-stdcall-alias -I..\..\headers -o jdic-winamp.dll mpcontrol.c

gcc -Wall -mno-cygwin -shared -Wl,--add-stdcall-alias -I..\..\headers -Ic:\j2sdk1.4.2_02\include\win32 -Ic:\j2sdk1.4.2_02\include -o jdic-winamp.dll mpcontrol.c