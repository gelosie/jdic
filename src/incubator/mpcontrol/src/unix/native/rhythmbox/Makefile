 #
 # Copyright (C) 2005 Sun Microsystems, Inc. All rights reserved. Use is
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

CC = gcc
TARGET=jdic-rhythmbox.so
JAVA_PACKAGE_NAME=org_jdesktop_jdic_mpcontrol
#HEADER_PATH=../../headers
HEADER_PATH=.

ORBIT_IDL=orbit-idl-2

#PREFIX = /usr
#CFLAGS=-g -DORBIT2=1 -D_REENTRANT -I$(PREFIX)/include/orbit-2.0 \
#    -I$(PREFIX)/include/linc-1.0 -I$(PREFIX)/include/glib-2.0 \
#    -I$(PREFIX)/lib/glib-2.0/include -I$(PREFIX)/include/libbonobo-2.0 \
#    -I$(PREFIX)/include/bonobo-activation-2.0/ -I$(HEADER_PATH)
CFLAGS=-g `pkg-config --cflags libbonobo-2.0` -I$(HEADER_PATH)


#LDFLAGS= -Wl,--export-dynamic -L$(PREFIX)/lib -lORBit-2 -llinc -lgmodule-2.0 \
#    -ldl -lgobject-2.0 -lgthread-2.0 -lpthread -lglib-2.0 -lbonobo-2 -lm
LDFLAGS= `pkg-config --libs libbonobo-2.0`


IDLOUT=Rhythmbox-stubs.c Rhythmbox-common.c Rhythmbox.h
IDLINC=/usr/share/idl
 
all: $(IDLOUT) $(TARGET) 
 

test-corba : test-corba.o Rhythmbox-common.o Rhythmbox-stubs.o

$(TARGET) : rhy-control.o Rhythmbox-common.o Rhythmbox-stubs.o $(HEADER_PATH)/$(JAVA_PACKAGE_NAME)_rhythmbox_RhytmboxControl.h
	$(CC) --shared $(LDFLAGS) rhy-control.o Rhythmbox-common.o Rhythmbox-stubs.o -o lib$(TARGET)
 
$(IDLOUT): Rhythmbox.idl
	$(ORBIT_IDL) --noskels -include=$(IDLINC) Rhythmbox.idl
 
clean:
	rm -rf *.o *~ $(IDLOUT) *.so
 
distclean: clean
	rm -rf test-corba
