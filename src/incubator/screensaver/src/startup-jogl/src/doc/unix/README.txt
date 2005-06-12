-------------------------------------------------------------------------------
SaverBeans Screensaver Pack License:
-------------------------------------------------------------------------------
Copyright (c) 2004-2005 Sun Microsystems, Inc. All rights reserved. Use is
subject to license terms.

This program is free software; you can redistribute it and/or modify
it under the terms of the Lesser GNU General Public License as
published by the Free Software Foundation; either version 2 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
USA
-------------------------------------------------------------------------------

-------------------------------------------------------------------------------
JOGL License:
-------------------------------------------------------------------------------
Distributed with this software is a copy of JOGL, a library that provides
Java bindings for OpenGL.  Usage of JOGL is covered under the following
license terms:

JOGL Copyright (c) 2003 Sun Microsystems, Inc. All Rights Reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

- Redistribution of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.
- Redistribution in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

Neither the name of Sun Microsystems, Inc. or the names of
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

This software is provided "AS IS," without a warranty of any kind
ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, 
INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

You acknowledge that this software is not designed or intended for use
in the design, construction, operation or maintenance of any nuclear
facility.

Sun gratefully acknowledges that this software was originally authored
and developed by Kenneth Bradley Russell and Christopher John Kline.
-------------------------------------------------------------------------------


SaverBeans Screensaver Pack with JOGL Support Unix README
---------------------------------------------------------

Requirements:
  * Administrator access
  * xscreensaver (any version should do, 4-14 and greater is known to work)
  * Java VM 1.5 Beta or higher (Get at http://java.sun.com/).  
  * JOGL (included with this distribution)

To Install:
  * Edit Makefile and set jdkhome, xscreensaverhome to valid directories
  * Run 'make' to build the screensaver binaries for your platform
  * In the future, there will be a 'make install' task (contributions welcome).
    For now this has to be done manually:
  * Copy or symbolically files to the right directories.
      Java Desktop System:
        SCREENSAVER_BIN=/usr/lib/xscreensaver
        SCREENSAVER_CONF=/usr/lib/xscreensaver/config
      Solaris:
        SCREENSAVER_BIN=/usr/openwin/lib/xscreensaver
        SCREENSAVER_CONF=/usr/openwin/share/control-center-2.0/screensavers
      Red Hat 9:
        SCREENSAVER_BIN=/usr/X11R6/bin
        SCREENSAVER_CONF=/usr/share/control-center/screensavers
      Fedora Core 3:
        SCREENSAVER_BIN=/usr/X11R6/lib/xscreensaver
        SCREENSAVER_CONF=/usr/share/control-center/screensavers
      Other platforms:
        SCREENSAVER_BIN=(search for an xscreensaver, like apollonian)
        SCREENSAVER_CONF=(search for a config file, like apollonian.xml)
    Please check where your other screensavers are installed.  You will
    need root access to do this (RPM will be provided later)
    1. Copy or symbolically link *.jar to SCREENSAVER_BIN
       Include jogl.jar and libjogl.so
    2. Copy *.xml to SCREENSAVER_CONF
    3. For each screensaver, you will see two files, e.g.
       picturecube and picturecube-bin.  Copy or symbolically 
       link picturecube and picturecube-bin to SCREENSAVER_BIN
  * Edit ~/.xscreensaver and add an entry for the screensaver.  For
    example, for picturecube, add the following to the programs section:
      "Picture Cube" /full/path/to/picturecube -root \n\
    (the picturecube.bin, saverbeans-examples.jar saverbeans-api.jar files 
    must appear in the same directory)
    NOTE: If you don't have a .xscreensaver file, go to your screensaver
    preferences and adjust the settings of a screensaver.  The file will
    be created for you automatically.
  * Make sure the Java Virtual Machine can be located by each screensaver
    from the shell launched by the xscreensaver process. 
    The following sources will be checked for your Java Virtual Machine
    (in order).  See the screensaver wrapper script for more details.
    At worst, you can always edit these scripts directly, but usually
    editing ~/.xscreensaver and adding -jdkhome will suffice.
      - -jdkhome parameter, if present (this parameter is also set by the
        screensaver "Java Home" option in the control panel)
      - $JAVA_HOME environment variable, if set
      - `rpm -ql j2sdk`, if available
      - `which java`, if found
      - otherwise error
  * Run xscreensaver-demo to test and select.  Look for 
    "Picture Cube".  If it works, you should see a 3D picture cube
    in the preview window.  If not, look for an error message in stderr.

To Run:
  * Go to screensaver settings - the new screensavers will appear there.
    For a basic example, look for picturecube.

Troubleshooting:
  * If it does not work but you get no error, try running picturecube 
    directly from the commandline and observe the output.

  * If you get an error containing:
      libjvm.so: cannot open shared object file: No such file or directory
    Then the screensaver cannot find the JDK.  Pass -jdkhome as 
    a parameter, pointing to a valid installation of J2SDK 1.5.0 or 
    greater.

  * If you get the error:
      Could not find class sun/awt/X11/XEmbeddedFrame
      Exception in thread "main" java.lang.NoClassDefFoundError: 
      sun/awt/X11/XEmbeddedFrame
    Then you're not using JDK 1.5.  JDK 1.5 is currently required on
    Linux/Solaris.  See above for how to fix this (most likely with 
    -jdkhome)

  * If you get the error:
      Exception in thread "main" java.lang.NoClassDefFoundError: 
      net/java/games/jogl/GLEventListener
    Then you do not have jogl.jar in the same directory as your
    screensaver JAR.  Make sure you have jogl.jar downloaded and it is
    either symbolically linked to the same directory as your screensaver JAR
    or copied there.

  * If you get the error:
      java.lang.UnsatisfiedLinkError: no jogl in java.library.path
    Then you do not have libjogl.so in the same directory as your
    screensaver JAR.  Make sure you have libjogl.so downloaded and it is
    either symbolically linked to the same directory as your screensaver JAR
    or copied there.

  * If you get the error:
      java.lang.UnsupportedClassVersionError ... 
    (unsupported major.minor version)
    Then you're not using JDK 1.5.  JDK 1.5 is currently required on
    Linux/Solaris.  See above for how to fix this (most likely with 
    -jdkhome)

  * If "Picture Cube" does not appear, try restarting xscreensaver 
    (or log out and log back in):
      pkill xscreensaver
      xscreensaver -nosplash &

