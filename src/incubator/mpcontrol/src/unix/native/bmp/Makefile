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
JAVA_PACKAGE_NAME=org_jdesktop_jdic_mpcontrol
#HEADER_PATH=../../headers
HEADER_PATH=.
TARGET=jdic-bmp.so
CFLAGS=-g `pkg-config --cflags bmp` -I$(HEADER_PATH)
LDFLAGS= `pkg-config --libs bmp`
 
all: $(TARGET) 

$(TARGET) : bmp-control.o $(HEADER_PATH)/$(JAVA_PACKAGE_NAME)_bmp_BeepMediaPlayer.h
	$(CC) --shared $(LDFLAGS) bmp-control.o -o lib$(TARGET)
 
clean:
	rm -rf *.o *~ $(IDLOUT) *.so
 
