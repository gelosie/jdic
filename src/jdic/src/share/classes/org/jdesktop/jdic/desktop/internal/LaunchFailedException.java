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
 
package org.jdesktop.jdic.desktop.internal;


/**
 * Thrown by methods when the contenting handling application, or the system 
 * browser, or the system mailer client fails to be launched.
 */
public class LaunchFailedException extends Exception {
  
    /**
     * Constructs a <code>LaunchFailedException</code> without a detail message.
     */
    public LaunchFailedException() {
        super();
    }
  
    /**
     * Constructs a <code>LaunchFailedException</code> with a detail message.
     * 
     * @param msg the detail message pertaining to this exception.
     */
    public LaunchFailedException(String msg) {
        super(msg);
    }
}
