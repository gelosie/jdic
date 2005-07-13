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
# Project: Misc
# author: Carl Dea
#
# mingw32-gcc-3.4.2
#TARGET_ALIAS = mingw32
CC = gcc
AS = as
DLLWRAP = dllwrap
DLLTOOL = dlltool
DLLWRAP_FLAGS = --driver-name=c++ --add-stdcall-alias

INCLUDE = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/win32 -g
LIB = -L$(JAVA_HOME)/lib
CFLAGS =  -g -O2 -c $(INCLUDE) $(LIB)

DEFFILE=libjdic_misc.def
STATICLIB=libjdic_misc.a


RM = rm -f

OBJ = wallpaper.o
BIN = jdic_misc.dll

.PHONY: all all-before all-after clean clean-custom

all: all-before jdic_misc.dll all-after

check: 
	echo $(JAVA_HOME)
clean: clean-custom
	${RM} $(OBJ) $(BIN)

jdic_misc.dll: wallpaper.o alerter.o
	$(DLLWRAP) $(DLLWRAP_FLAGS) -o jdic_misc.dll -s wallpaper.o alerter.o

wallpaper.o: wallpaper.cpp
	$(CC) $(CFLAGS) wallpaper.cpp

alerter.o: alerter.c
	$(CC) $(CFLAGS) alerter.c

