#
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
# 

# 
# Makefile for building JDIC native libraries(*.so) on Linux and Solaris.
# *** This makefile must be built using GNU Make ***
# JAVA_HOME must be set before using this script.
#

UNAME = $(shell uname)

ifeq ($(UNAME), SunOS)
  PLATFORM = solaris
  CXX = CC
  CXXFLAGS = -c
  LDFLAGS = -G
  X11LIB=/usr/openwin/lib
else 
  ifeq ($(UNAME), Linux)
    PLATFORM = linux
    CXX = g++
    CXXFLAGS = -c
    LDFLAGS = -shared -fPIC
    X11LIB=/usr/X11R6/lib
  endif
endif

USR_INCLUDE_DIR = /usr/include
USR_LIB_DIR = /usr/lib

INCLUDES = -I$(JAVA_HOME)/include \
           -I$(JAVA_HOME)/include/$(PLATFORM)

LIBS = -L$(USR_LIB_DIR) \
       -lm \
       -ldl \
       -lX11

EXTRA_LIBS = \
    -L$(JAVA_HOME)/jre/lib/sparc \
    -L$(JAVA_HOME)/jre/lib/i386 \
    -ljawt \

SO_NAME = libtray.so

all: clean $(SO_NAME)

SO_o = $(CPPSRCS:.cpp=.o)

libtray.so: native.c
	cc $(INCLUDES) $(LDFLAGS) -ldl $(EXTRA_BROWSER_LIBS) -L$(X11LIB) -lXt -lX11 native.c -o $@

clean:
	rm -f *.so
	rm -f *.o
