# vim:set ts=8 sw=8 sts=8 noet:
# ***** BEGIN LICENSE BLOCK *****
# Version: MPL 1.1/GPL 2.0/LGPL 2.1
#
# The contents of this file are subject to the Mozilla Public License Version
# 1.1 (the "License"); you may not use this file except in compliance with
# the License. You may obtain a copy of the License at
# http://www.mozilla.org/MPL/
#
# Software distributed under the License is distributed on an "AS IS" basis,
# WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
# for the specific language governing rights and limitations under the
# License.
#
# The Original Code is mozilla.org Code.
#
# The Initial Developer of the Original Code is
# Netscape Communications Corporation.
# Portions created by the Initial Developer are Copyright (C) 2001
# the Initial Developer. All Rights Reserved.
#
# Contributor(s):
#
# Alternatively, the contents of this file may be used under the terms of
# either the GNU General Public License Version 2 or later (the "GPL"), or
# the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
# in which case the provisions of the GPL or the LGPL are applicable instead
# of those above. If you wish to allow use of your version of this file only
# under the terms of either the GPL or the LGPL, and not to allow others to
# use your version of this file under the terms of the MPL, indicate your
# decision by deleting the provisions above and replace them with the notice
# and other provisions required by the GPL or the LGPL. If you do not delete
# the provisions above, a recipient may use your version of this file under
# the terms of any one of the MPL, the GPL or the LGPL.
#
# ***** END LICENSE BLOCK *****

#
# Before using this script, MOZILLA_DIST must be set to the Mozilla distribution 
# path for libgtkembedmoz.so, and GECKO_SDK must be set to the Gecko SDK home 
# directory for header and lib files.
#

# using Gecko SDK 1.4 or 1.7.
#
# !!!Note: Below option needs to be changed according to the Gecko SDK version.
# For Gecko SDK 1.6 or ealier, enable USING_GECKO_SDK_1_4. Or else, enable 
# USING_GECKO_SDK_1_7.
#
USING_GECKO_SDK_1_4 = 1
#USING_GECKO_SDK_1_7 = 1

# set gtk version
#
# !!!Note: Below option needs to be changed according to the GTK version 
# (1.2 or 2) Gecko SDK and the Mozilla binary used. The GTK version building
# Gecko SDK and the Mozilla binary must be same.
#
#MOZ_ENABLE_GTK = 1
MOZ_ENABLE_GTK2 = 1

MODULE_NAME_PART= mozembed

# set options for each platform 
OS_ARCH = $(shell uname)
ifeq ($(OS_ARCH), Linux)
CXX = g++
LDFLAGS = -shared -fPIC -Wl,--version-script=gnu-ld-script
MODULE_NAME_PART = mozembed-linux-gtk
endif
ifeq ($(OS_ARCH), SunOS)
CXX = CC
LDFLAGS = -G -M gnu-ld-script
MODULE_NAME_PART = mozembed-solaris-gtk
endif
ifeq ($(OS_ARCH), FreeBSD)
CXX = g++
LDFLAGS = -shared -fPIC -Wl,--version-script=gnu-ld-script
MODULE_NAME_PART = mozembed-freebsd-gtk
endif

# set suffix for MODULE_NAME
ifdef MOZ_ENABLE_GTK
MODULE_NAME = $(MODULE_NAME_PART)1.2
endif
ifdef MOZ_ENABLE_GTK2
MODULE_NAME = $(MODULE_NAME_PART)2
endif

# set other variables
BIN_SUFFIX =

CPPSRCS = MozEmbed.cpp \
          Callback.cpp \
          ../../../share/native/mozilla/Common.cpp \
          ../../../share/native/mozilla/ProfileDirServiceProvider.cpp \
          ../../../share/native/utils/Util.cpp \
          ../../../share/native/utils/MsgServer.cpp \
          $(NULL)
          
PROGRAM = $(MODULE_NAME)$(BIN_SUFFIX)
LIBRARY = lib$(MODULE_NAME).so

#CXXFLAGS = -g -fPIC
CXXFLAGS = -O2 -fPIC

# link against the version of gtkembedmoz included with a
# mozilla distribution
LIBS = -L$(MOZILLA_DIST) -lgtkembedmoz

# libembedstring.a from the 1.4.2 gecko sdk requires __ctype_tolower,
# but for some reason that isn't available from the version of glibc
# that ships with Fedora Core 2.
ifdef USING_GECKO_SDK_1_4
ifeq ($(OS_ARCH), Linux)
NEED___ctype_tolower = 1
endif
endif

ifdef MOZ_ENABLE_GTK
CPPFLAGS += -DMOZ_WIDGET_GTK
LIBS += -lgtksuperwin
LIBS_PROG += -L$(MOZILLA_DIST) -lgtkembedmoz -lxpcom -lgtksuperwin
endif

ifdef MOZ_ENABLE_GTK2
CPPFLAGS += -DMOZ_WIDGET_GTK2
# XXX need to get these from pkg-config perhaps?
LIBS += $(XLDFLAGS) $(XLIBS)
LIBS_PROG += -L$(MOZILLA_DIST) -lgtkembedmoz -lxpcom
endif

CPPFLAGS += \
	-DXPCOM_GLUE -DMOZILLA_STRICT_API \
	-I. \
	-I../../../share/native/utils \
	-I../../../share/native/mozilla \
	$(NULL)

ifdef USING_GECKO_SDK_1_4
CPPFLAGS += \
	-include $(GECKO_SDK)/mozilla-config.h \
	-I$(GECKO_SDK)/ \
	-I$(GECKO_SDK)/xpcom/include \
	-I$(GECKO_SDK)/nspr/include \
	-I$(GECKO_SDK)/webbrwsr/include \
	-I$(GECKO_SDK)/profile/include \
	-I$(GECKO_SDK)/dom/include \
	-I$(GECKO_SDK)/string/include \
	-I$(GECKO_SDK)/embedstring/include \
	-I$(GECKO_SDK)/necko/include \
	-I$(GECKO_SDK)/pref/include \
	$(NULL)
LIBS += \
	-L$(GECKO_SDK)/embedstring/bin -lembedstring \
	-L$(GECKO_SDK)/xpcom/bin -lxpcomglue_s -lxpcom \
	-L$(GECKO_SDK)/nspr/bin -lnspr4 -lplds4 -lplc4 \
	$(NULL)
endif
ifdef USING_GECKO_SDK_1_7
CPPFLAGS += \
        -DUSING_GECKO_SDK_1_7 \
	-include $(GECKO_SDK)/include/mozilla-config.h \
	-I$(GECKO_SDK)/include \
	$(NULL)
LIBS += \
	-L$(GECKO_SDK)/lib -lxpcomglue_s \
	-L$(GECKO_SDK)/bin -lxpcom -lnspr4 -lplds4 -lplc4 \
	$(NULL)
endif

ifeq ($(OS_ARCH), SunOS)
CXXFLAGS += -features=tmplife
endif
ifdef MOZ_ENABLE_GTK
CXXFLAGS += `gtk-config --cflags`
LIBS += `gtk-config --libs`
endif
ifdef MOZ_ENABLE_GTK2
CXXFLAGS += `pkg-config gtk+-2.0 --cflags`
# do not use --libs since it includes -Wl,--export-dynamic
LIBS += `pkg-config gtk+-2.0 --libs-only-L` 
LIBS += `pkg-config gtk+-2.0 --libs-only-l` 
endif

OBJS = $(CPPSRCS:.cpp=.o)

ifdef NEED___ctype_tolower
OBJS += tolower.o
endif

$(PROGRAM): $(LIBRARY) Main.o
	$(CXX) -o $(PROGRAM) Main.o -L. -l$(MODULE_NAME) $(LIBS_PROG)

$(LIBRARY): $(OBJS)
	$(CXX) -o $(LIBRARY) $^ $(LDFLAGS) $(LIBS)

%.o: %.cpp
	$(CXX) -c $< -o $@ $(CPPFLAGS) $(CXXFLAGS)

Main.o: Main.cpp
	$(CXX) -c $< -o $@

clean::
	rm -f $(OBJS) $(PROGRAM) $(LIBRARY) Main.o
	rm -f *.so
	rm -f mozembed-*-gtk*
