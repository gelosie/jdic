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

PLATFORM = mac_os_x
CXX = g++
CXXFLAGS = -W -Wall -c -I/System/Library/Frameworks/JavaVM.framework/Headers
MMFLAGS = -Wno-protocol -Wundeclared-selector
LDFLAGS = -dynamiclib
LDSUFFIXES = -framework JavaVM -framework Cocoa
SO_NAME = libjdic.jnilib

.SUFFIXES: .o .mm
.mm.o:
	$(CXX) $(MMFLAGS) $(CXXFLAGS) $<

USR_INCLUDE_DIR = /usr/include
USR_LIB_DIR = /usr/lib

INCLUDES = -I$(JAVA_HOME)/include \
           -I$(JAVA_HOME)/include/$(PLATFORM)

MMSRCS += MacBrowserService.mm 
MMSRCS += MacLaunchService.mm 
#MMSRCS += MacMailerService.mm 

CPPSRCS += ../../../share/native/utils/InitUtility.cpp

.PHONY: all clean

all: clean $(SO_NAME)

SO_o = $(MMSRCS:.mm=.o) $(CPPSRCS:.cpp=.o)

.cpp.o:
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(EXTRA_INCLUDES) -o $@ $<

$(SO_NAME): $(SO_o)
	$(CXX) $(LDFLAGS) $(LIBS) $(EXTRA_BROWSER_LIBS) $(SO_o) -o $@ $(LDSUFFIXES)

clean:
	rm -f *.jnilib
	rm -f *.o
