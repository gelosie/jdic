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

import org.jdesktop.jdic.desktop.Message;

/**
 * The <code>MailerService</code> interface provides methods to launch message
 * compose window of the default mailer with or without prefilled message fields.
 */
public interface MailerService {
  
    /**
     * Launches the message compose window of the system default mailer. 
     *
     * @throws LaunchFailedException if the system mailer fails to be launched.
     */
    public void open() throws LaunchFailedException;
  
    /**
     * Launches the message compose window of system default mailer, and fills 
     * in the specified message fields.
     *
     * @param msg a constructed message object.
     * @throws LaunchFailedException if the system mailer fails to be launched.
     * @see <code>org.jdesktop.jdic.desktop.Message</code>
     */
    public void open(Message msg) throws LaunchFailedException;

}
