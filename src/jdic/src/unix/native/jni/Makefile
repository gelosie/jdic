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
# Makefile for building JDIC native libraries(*.so) on Unix platforms
# (Linux/Solaris/FreeBSD).
# *** This makefile must be built using GNU Make ***
# JAVA_HOME must be set before using this script.
#

UNAME = $(shell uname)

ifeq ($(UNAME), SunOS)
  PLATFORM = solaris
  CXX = CC
  CXXFLAGS = -c
  LDFLAGS = -G -ldl -lrt
  X11LIB=/usr/openwin/lib
else 
  ifeq ($(UNAME), Linux)
    PLATFORM = linux
    CXX = gcc
    CXXFLAGS = -c
    LDFLAGS = -shared -fPIC -ldl -lrt
  endif
  ifeq ($(UNAME), FreeBSD)
    PLATFORM = freebsd
    CXX = gcc
    CXXFLAGS = -c -I/usr/local/include -I/usr/X11R6/include
    LDFLAGS = -shared -fPIC -L/usr/local/lib -L/usr/X11R6/lib
  endif
  X11LIB=/usr/X11R6/lib
endif

USR_INCLUDE_DIR = /usr/include
USR_LIB_DIR = /usr/lib

INCLUDES = -I$(JAVA_HOME)/include \
           -I$(JAVA_HOME)/include/$(PLATFORM)

EXTRA_INCLUDES = `pkg-config --cflags glib-2.0` \
                 `pkg-config --cflags libgnome-2.0` \
                 `pkg-config --cflags gnome-vfs-2.0`\
                 `pkg-config --cflags gnome-vfs-module-2.0` \
                 `pkg-config --cflags bonobo-activation-2.0` \
                 `pkg-config --cflags libbonobo-2.0` \
                 `pkg-config --cflags ORBit-2.0` \
                 `pkg-config --cflags linc` \
                 `pkg-config --cflags gconf-2.0`

LIBS = -L$(USR_LIB_DIR) \
       -lz \
       -lm \
       -lgnome-2 \
       -lgnomevfs-2 \
       -lbonobo-activation \
       -lgconf-2 \
       -lORBit-2 \
       -lgmodule-2.0 \
       -ldl \
       -lgobject-2.0 \
       -lgthread-2.0 \
       -lpthread \
       -lglib-2.0 \
       -lpopt \
       -lrt

EXTRA_BROWSER_LIBS = \
    -L$(JAVA_HOME)/jre/lib/sparc \
    -L$(JAVA_HOME)/jre/lib/i386 \
    -ljawt

TRAY_LIBS = -L$(X11LIB) -lXt -lX11

#
# All the native source files.
#
CPPSRCS = GnomeBrowserService.cpp \
           GnomeLaunchService.cpp \
           GnomeUtility.cpp \
           GnomeVfsWrapper.cpp \
           WebBrowser.cpp \
           WebBrowserUtil.cpp \
           ../../../share/native/utils/InitUtility.cpp
CSRCS = Tray.c
          
#
# Build a whole libjdic.so containing all the native APIs.
#
SO_NAME = libjdic.so
all: clean $(SO_NAME)

SO_OBJS = $(CPPSRCS:.cpp=.o) $(CSRCS:.c=.o)

.cpp.o .c.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(EXTRA_INCLUDES) -o $@ $<

$(SO_NAME): $(SO_OBJS)
	$(CXX) $(LDFLAGS) $(LIBS) $(EXTRA_BROWSER_LIBS) $(TRAY_LIBS) $(SO_OBJS) -o $@

#
# Build a libjdic.so containing only the Tray Icon API,
# though it has the same name (libjdic.so).
#
# Source files only needed for the Tray Icon API:
#   ../../../share/native/utils/InitUtility.cpp
#   Tray.c
#
tray: clean TRAY_SO_TARGET

TRAY_SO_OBJS = ../../../share/native/utils/InitUtility.o \
               Tray.o

../../../share/native/utils/InitUtility.o: ../../../share/native/utils/InitUtility.cpp
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $@ $<
Tray.o: Tray.c
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $@ $<

TRAY_SO_TARGET: $(TRAY_SO_OBJS)
	$(CXX) $(LDFLAGS) $(TRAY_LIBS) $(TRAY_SO_OBJS) -o $(SO_NAME) 

clean:
	rm -f *.so
	rm -f *.o
	rm -f ../../../share/native/utils/*.o
