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
  
package org.jdesktop.jdic.desktop.internal.impl;

/**
 * The <code>DesktopConstants</code> interface stores constants used and shared
 * by other classes.
 */
public interface DesktopConstants {
    // Launcher service verbs.
    static final String VERB_OPEN = "open";
    static final String VERB_OPENNEW = "opennew";
    static final String VERB_EDIT = "edit";
    static final String VERB_PRINT = "print";
    
    // OS properties.
    static final String OS_PROPERTY = "os.name";
    static final String OS_LINUX = "linux";
    static final String OS_SOLARIS = "sunos";
    static final String OS_WINDOWS = "windows";
    static final String OS_FREEBSD = "freebsd";

    // Mailer properties.
    static final String MOZ_MAILER = "mozilla";
    static final String EVO_MAILER = "evolution";
    static final String THBD_MAILER = "thunderbird";
}
