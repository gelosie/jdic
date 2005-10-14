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
ARCH = $(shell uname -m)

ifeq ($(UNAME), SunOS)
  PLATFORM = solaris
  CXX = CC -norunpath
  CC = cc  
  CXXFLAGS = -c
  LDFLAGS = -G -ldl -lrt -lpthread
  X11LIB=/usr/openwin/lib
else 
  ifeq ($(UNAME), Linux)
    PLATFORM = linux
    CXX = g++
    CXXFLAGS = -c -fPIC
    LDFLAGS = -shared -fPIC -ldl -lrt -lpthread
  endif
  ifeq ($(UNAME), FreeBSD)
    PLATFORM = freebsd
    CXX = g++
    CXXFLAGS = -c -I/usr/local/include -I/usr/X11R6/include
    LDFLAGS = -shared -fPIC -L/usr/local/lib -L/usr/X11R6/lib
  endif
  CC = gcc
  ifeq ($(ARCH), x86_64)
    X11LIB=/usr/X11R6/lib64
  else
    X11LIB=/usr/X11R6/lib
  endif  
endif

USR_INCLUDE_DIR = /usr/include
ifeq ($(ARCH), x86_64)
  USR_LIB_DIR = /usr/lib64
else
  USR_LIB_DIR = /usr/lib
endif

INCLUDES = -I$(JAVA_HOME)/include \
           -I$(JAVA_HOME)/include/$(PLATFORM)

EXTRA_INCLUDES = `pkg-config --cflags glib-2.0` \
                 `pkg-config --cflags libgnome-2.0` \
                 `pkg-config --cflags gnome-vfs-2.0`\
                 `pkg-config --cflags gnome-vfs-module-2.0` \
                 `pkg-config --cflags bonobo-activation-2.0` \
                 `pkg-config --cflags libbonobo-2.0` \
                 `pkg-config --cflags ORBit-2.0` \
                 `pkg-config --cflags gconf-2.0`

LIBS = -L$(USR_LIB_DIR) \
       -lz \
       -lm \
       -lgnome-2 

EXTRA_BROWSER_LIBS = \
    -L$(JAVA_HOME)/jre/lib/sparc \
    -L$(JAVA_HOME)/jre/lib/sparcv9 \
    -L$(JAVA_HOME)/jre/lib/i386 \
    -L$(JAVA_HOME)/jre/lib/amd64 \
    -L$(JAVA_HOME)/jre/lib/ia64 \
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
TRAY_CSRC = Tray.c
          
#
# Build both libjdic.so and ibtray.so containing all the native code.
#
JDIC_SO_NAME = libjdic.so
TRAY_SO_NAME = libtray.so

all: $(JDIC_SO_NAME) $(TRAY_SO_NAME)

JDIC_SO_OBJS = $(CPPSRCS:.cpp=.o)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(EXTRA_INCLUDES) -o $@ $<

$(JDIC_SO_NAME): $(JDIC_SO_OBJS)
	$(CXX) $(LDFLAGS) $(LIBS) $(EXTRA_BROWSER_LIBS) $(JDIC_SO_OBJS) -o $@

#
# Build libtray.so containing only the Tray Icon API native code.
#
tray: $(TRAY_SO_NAME)

$(TRAY_SO_NAME): $(TRAY_CSRC)
	$(CC) $(INCLUDES) $(LDFLAGS) -ldl -L$(X11LIB) -lXt -lX11 $(TRAY_CSRC) -o $@

clean:
	rm -f *.so
	rm -f *.o
	rm -f ../../../share/native/utils/*.o
