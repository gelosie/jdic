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
else 
  ifeq ($(UNAME), Linux)
    PLATFORM = linux
    CXX = g++
    CXXFLAGS = -c
    LDFLAGS = -shared -fPIC
  endif
endif

USR_INCLUDE_DIR = /usr/include
USR_LIB_DIR = /usr/lib

INCLUDES = -I$(JAVA_HOME)/include \
           -I$(JAVA_HOME)/include/$(PLATFORM)

EXTRA_INCLUDES = -I$(USR_INCLUDE_DIR)/libgnome-2.0 \
                 -I$(USR_INCLUDE_DIR)/gnome-vfs-2.0/libgnomevfs \
                 -I$(USR_INCLUDE_DIR)/gnome-vfs-2.0 \
                 -I$(USR_INCLUDE_DIR)/gnome-vfs-module-2.0 \
                 -I$(USR_INCLUDE_DIR)/gnome-vfs-module-2.0/libgnomevfs \
                 -I$(USR_INCLUDE_DIR)/glib-2.0 \
                 -I$(USR_INCLUDE_DIR)/glib-2.0/glib \
                 -I$(USR_LIB_DIR)/glib-2.0/include \
                 -I$(USR_INCLUDE_DIR)/bonobo-activation-2.0 \
                 -I$(USR_INCLUDE_DIR)/libbonobo-2.0 \
                 -I$(USR_INCLUDE_DIR)/orbit-2.0 \
                 -I$(USR_INCLUDE_DIR)/linc-1.0 \
                 -I$(USR_INCLUDE_DIR)/gconf/2

LIBS = -L$(USR_LIB_DIR) \
       -lgnome-2 \
       -lgnomevfs-2 \
       -lbonobo-activation \
       -lz \
       -lgconf-2 \
       -lORBit-2 \
       -lm \
       -llinc \
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
    -ljawt \

CPPSRCS = GnomeBrowserService.cpp \
          GnomeLaunchService.cpp \
          GnomeUtility.cpp \
          GnomeVfsWrapper.cpp \
          WebBrowser.cpp
          
SO_NAME = libjdic.so

all: clean $(SO_NAME)

SO_o = $(CPPSRCS:.cpp=.o)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(EXTRA_INCLUDES) -o $@ $<

$(SO_NAME): $(SO_o)
	$(CXX) $(LDFLAGS) $(LIBS) $(EXTRA_BROWSER_LIBS) $(SO_o) -o $@

clean:
	rm -f *.so
	rm -f *.o
