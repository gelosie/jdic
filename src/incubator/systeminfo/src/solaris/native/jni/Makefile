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
# Makefile for building SystemInfo native libraries(*.so) on Linux platform.
#
# *** This makefile must be built using GNU Make ***
# JAVA_HOME must be set before using this script.
#

X11_DIR = /usr/X11

CC=cc

INCLUDE = -I$(X11_DIR)/include -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/solaris
LDFLAGS = -G -L$(X11_DIR)/lib -R$(X11_DIR)/lib -lX11 -lXext
CFLAGS = $(INCLUDE)

all: clean libsysteminfo.so

libsysteminfo.so: systeminfo.o
	$(CC) $(CFLAGS) -o $@ systeminfo.o $(LDFLAGS)

clean:
	@rm -f *.o
	@rm -f *.so
