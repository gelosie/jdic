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
 
package org.jdesktop.jdic.desktop;


/**
 * A <code>DesktopException</code> is thrown by certain methods of 
 * <code>Desktop</code> class to indicate that the passed paramaters are invalid 
 * or the operation fails.
 * 
 * @see Desktop
 */
public class DesktopException extends Exception {

    /**
     * Constructs a <code>DesktopException</code> without a detail message.
     */
    public DesktopException() {
        super();
    }
  
    /**
     * Constructs a <code>DesktopException</code> with a detail message.
     * 
     * @param msg the detail message pertaining to this exception.
     */
    public DesktopException(String msg) {
        super(msg);
    }  
}
