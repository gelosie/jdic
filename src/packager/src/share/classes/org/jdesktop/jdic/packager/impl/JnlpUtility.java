/*
 * Copyright (C) 2004 Sun Microsystems, Inc. All rights reserved. Use is
 * subject to license terms.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */ 

package org.jdesktop.jdic.packager.impl;

/**
 * This utility class provides methods used or shared by other classes.
 */
public class JnlpUtility {
    /**
     * Script to check if Java Web Start 1.5 is available on Linux/Solaris.
     * <p>
     * For PKG/Solaris, this script is written into checkinstall information
     * file.
     * <p>
     * For RPM/Linux, this script is written into .spec file.
     * @return String array containg the generated script.
     */
    public static String[] javawsCheckScript() {
        String[] javawscheckscript = {
            "#!/bin/sh",
            "# Check the existence of javaws 1.5 or later under $PATH.",
            "echo_error()",
            "{",
            "  echo \"Error: incorrect javaws version.\"",
            "  echo \"Please install javaws 1.5 or later and set it as the helper application for the Mime type\"",
            "  echo \"\\\"application/x-java-jnlp-file\\\" in /etc/mailcap file. Then try the installation again.\"",
            "  exit 3",
            "}",
            "",
            "JNLP_ASSOCIATION=`grep '^[^#]*application/x-java-jnlp-file' /etc/mailcap`",
            "if [ $? -ne 0 ]",
            "then",
            "  echo \"ERROR: script line 'type javaws 2>&1' returns non-zero value.\"",
            "else",
            "  JAVAWS_PATH=`echo $JNLP_ASSOCIATION | awk '{print $2}'`",
            "fi",
            "",
            "PARENT_DIR=`echo ${JAVAWS_PATH} | awk -F\\/ '{for (i=2; i<NF; i++) printf \"/%s\", $i}'`",
            "if [ -h ${JAVAWS_PATH} ]",
            "then",
            "  LS_RESULT=`ls -l ${JAVAWS_PATH} 2>&1`",
            "  LINK_TARGET=`echo ${LS_RESULT} | awk '{printf \"%s\", $NF}'`",
            "  LINK_TARGET_BEGIN=`echo ${LINK_TARGET} | awk '{printf \"%s\", $1}'`",
            "  if [ ${LINK_TARGET_BEGIN}/ = \"/\" ]",
            "  then",
            "    PARENT_DIR=`echo ${LINK_TARGET} | awk -F\\/ '{for (i=2; i<NF; i++) printf \"/%s\", $i}'`",
            "    JAVA_PATH=${PARENT_DIR}/java",
            "  else",
            "    JAVA_PARENT_DIR=`echo ${PARENT_DIR}/${LINK_TARGET} | awk -F\\/ '{for (i=2; i<NF; i++) printf \"/%s\", $i}'`",
            "    JAVA_PATH=${JAVA_PARENT_DIR}/java",
            "  fi",
            "else",
            "  JAVA_PATH=${PARENT_DIR}/java",
            "fi",
            "",
            "if [ -f ${JAVA_PATH} ]",
            "then",
            "  java_ver=`${JAVA_PATH} -version 2>&1 | awk -F\\\" '{print $2}' `",
            "  minor=`echo $java_ver | awk -F\\. '{print $2}'`",
            "  if [ ${minor} -lt 5 ]",
            "  then",
            "    echo_error",
            "  fi",
            "else",
            "  echo_error",
            "fi"
        };

        return javawscheckscript;
    }

    /**
     * Evaluate if current platform is the matching platform for the running
     * package generator.
     * @param osName The matching platform name.
     */
    public static void checkPlatformCompatibility(String osName) {
        String sysOSName = System.getProperty("os.name").toLowerCase();
        if (!sysOSName.startsWith(osName)) {
            System.out.println(
                "Error: " + "This tool doesn't support this platform.");
            System.exit(0);
            return;
        }
    }
}
