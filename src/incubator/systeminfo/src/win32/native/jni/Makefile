# Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
# subject to license terms.
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the Lesser GNU General Public License as
# published by the Free Software Foundation; either version 2 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
# USA.

TARGET_ALIAS = i586-mingw32
WIN32_DIR = $(HOME)/.cxoffice/dotwine/fake_windows
JAVA_HOME = $(WIN32_DIR)/j2sdk1.4.2_07
XCOMPILE_DIR = /usr/local/cross-compile/$(TARGET_ALIAS)

CC = $(TARGET_ALIAS)-c++
AS = $(TARGET_ALIAS)-as
DLLWRAP = $(TARGET_ALIAS)-dllwrap
DLLTOOL = $(TARGET_ALIAS)-dlltool

INCLUDE = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32 -I.
LDFLAGS = -L$(XCOMPILE_DIR)/lib -lm -lmingwex -lsupc++ -lstdc++
CFLAGS =  -O2 -Wall -mno-cygwin -mms-bitfields -c $(INCLUDE)
DLLWRAP_FLAGS = --as $(AS) --driver-name $(CC) --dlltool-name $(DLLTOOL) --target $(TARGET_ALIAS) --mno-cygwin --add-stdcall-alias -s

all: systeminfo.dll systemcallback.dll

systeminfo.dll: systeminfo.o
	$(DLLWRAP) $(DLLWRAP_FLAGS) -o $@ $^

systeminfo.o: systeminfo.cpp
	$(CC) $(CFLAGS) $^

systemcallback.dll: systemcallback.o
	$(DLLWRAP) $(DLLWRAP_FLAGS) -o $@ $^

systemcallback.o: systemcallback.cpp
	$(CC) $(CFLAGS) $^

clean:
	@rm -f *.o
	@rm -f *.a
	@rm -f *.def
	@rm -f *.dll
