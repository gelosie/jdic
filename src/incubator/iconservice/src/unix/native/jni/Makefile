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
# Makefile for building incubator iconservice library (libjdic_icon.so) 
# on Unix (Linux/Solaris/FreeBSD) platforms.
#
# *** This makefile for use with GNU Make ***
#

UNAME = $(shell uname)

ifeq ($(UNAME), SunOS)
  PLATFORM = solaris
  CXX = CC
  CC = cc  
  CXXFLAGS = -c
  LDFLAGS = -G -ldl -lrt -lpthread
else 
  ifeq ($(UNAME), Linux)
    PLATFORM = linux
    CXX = g++
    CXXFLAGS = -c
    LDFLAGS = -shared -fPIC -ldl -lrt -lpthread
  endif
  ifeq ($(UNAME), FreeBSD)
    PLATFORM = freebsd
    CXX = g++
    CXXFLAGS = -c -I/usr/local/include
    LDFLAGS = -shared -fPIC
  endif
  CC = gcc
endif

INCLUDES = -I/usr/include \
           -I$(JAVA_HOME)/include \
           -I$(JAVA_HOME)/include/$(PLATFORM) \
            `pkg-config --cflags gconf-2.0`

LIBS = -L$(USR_LIB_DIR) \
       -lgconf-2

#
# The native source file
#
CPPSRCS = XdgDirectory.cpp
          
#
# Build libjdic_icon.so containing the native code.
#
ICON_SO_NAME = libjdic_icon.so

all: clean $(ICON_SO_NAME)

ICON_SO_OBJS = $(CPPSRCS:.cpp=.o)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) -o $@ $<

$(ICON_SO_NAME): $(ICON_SO_OBJS)
	$(CXX) $(LDFLAGS) $(LIBS) $(ICON_SO_OBJS) -o $@

clean:
	rm -f *.so
	rm -f *.o
	rm -f ../../../share/native/utils/*.o
