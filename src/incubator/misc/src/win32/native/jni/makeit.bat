gcc -g -O2 -c -Ic:/j2sdk1.4.2_07/include -Ic:/j2sdk1.4.2_07/include/win32 -g wallpaper.cpp
dllwrap --driver-name=c++ --add-stdcall-alias -o jdic_misc.dll -s wallpaper.o

cp jdic_misc.dll c:/JDIC_DEV/jdic/src/incubator/misc/demo
cp jdic_misc.dll c:/JDIC_DEV/jdic/src/incubator/misc/dist/windows

