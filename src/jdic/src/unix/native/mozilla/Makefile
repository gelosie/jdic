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

DEPTH       = $(MOZILLA_SRC_HOME)
topsrcdir   = $(MOZILLA_SRC_HOME)
srcdir      = .
VPATH       = .

include $(DEPTH)/config/autoconf.mk

MODULE   = gtkembedmoz

REQUIRES = xpcom \
           string \
           dom \
           webbrwsr \
           docshell \
           necko \
           content \
           widget \
           pref \
           profile \
	   nspr \
           $(NULL)

ifdef NS_TRACE_MALLOC
REQUIRES += tracemalloc
endif

CPPSRCS = MozEmbed.cpp \
          Callback.cpp \
          ../../../share/native/mozilla/Common.cpp \
          ../../../share/native/mozilla/ProfileDirServiceProvider.cpp \
          ../../../share/native/utils/Util.cpp \
          ../../../share/native/utils/MsgServer.cpp \
          $(NULL)
          
PROGRAM = mozembed$(BIN_SUFFIX)
ifeq ($(OS_ARCH), Linux)
ifdef MOZ_ENABLE_GTK
PROGRAM = mozembed-linux-gtk1.2$(BIN_SUFFIX)
endif
ifdef MOZ_ENABLE_GTK2
PROGRAM = mozembed-linux-gtk2$(BIN_SUFFIX)
endif
endif
ifeq ($(OS_ARCH), SunOS)
ifdef MOZ_ENABLE_GTK
PROGRAM = mozembed-solaris-gtk1.2$(BIN_SUFFIX)
endif
ifdef MOZ_ENABLE_GTK2
PROGRAM = mozembed-solaris-gtk2$(BIN_SUFFIX)
endif
endif
ifeq ($(OS_ARCH), FreeBSD)
ifdef MOZ_ENABLE_GTK
PROGRAM = mozembed-freebsd-gtk1.2$(BIN_SUFFIX)
endif
ifdef MOZ_ENABLE_GTK2
PROGRAM = mozembed-freebsd-gtk2$(BIN_SUFFIX)
endif
endif



LIBS = -lgtkembedmoz \
       $(NULL)

ifdef MOZ_ENABLE_GTK
LIBS += -lgtksuperwin \
        $(NULL)
endif

ifdef MOZ_ENABLE_GTK2
LIBS += $(XLDFLAGS) \
        $(XLIBS) \
        $(NULL)
endif

include $(topsrcdir)/config/config.mk

ifdef BUILD_STATIC_LIBS

ifndef MOZ_FAT_EMBED
FINAL_LINK_COMPS=../src/embed-link-comps
endif

include $(topsrcdir)/config/static-config.mk

EXTRA_DEPS  += $(STATIC_EXTRA_DEPS)
EXTRA_DSO_LIBS  += $(STATIC_EXTRA_DSO_LIBS)
EXTRA_LIBS  += -L$(DEPTH)/dist/lib/components
EXTRA_LIBS  += $(EXTRA_DSO_LIBS) $(STATIC_EXTRA_LIBS)

endif

ifdef NS_TRACE_MALLOC
EXTRA_LIBS  += -ltracemalloc
endif

EXTRA_LIBS  += $(MOZ_JS_LIBS)
EXTRA_LIBS  += $(MOZ_COMPONENT_LIBS)

include $(topsrcdir)/config/rules.mk

LOCAL_INCLUDES += -I$(srcdir)/../../../share/native/utils \
                  -I$(srcdir)/../../../share/native/mozilla \
                  $(NULL)

ifeq ($(OS_ARCH), SunOS)
CXXFLAGS += -features=tmplife
endif
CXXFLAGS += $(MOZ_GTK_CFLAGS) $(MOZ_GTK2_CFLAGS)

ifdef ENABLE_GNOME
CXXFLAGS   += `gnome-config --cflags gnomeui`
EXTRA_LIBS += `gnome-config --libs gnomeui`
endif

EXTRA_LIBS += \
        $(TK_LIBS) \
        $(NULL)

ifeq ($(OS_ARCH), OpenVMS)
DEFINES += -DGENERIC_MOTIF_REDEFINES
endif
