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

import java.io.IOException;
import java.util.Iterator;

import org.jdesktop.jdic.desktop.Message;
import org.jdesktop.jdic.desktop.internal.LaunchFailedException;
import org.jdesktop.jdic.desktop.internal.MailerService;

/**
 * Implements the MailerService interface for Mac OS X.
 *
 * @author Elliott Hughes <enh@acm.org>
 */
public class MacMailerService implements MailerService {
    /**
     * Launches a compose window with information contained in 'msg' already 
     * filled in.
     * <p>
     * Note that Mac OS X's Mail.app does not support automatic attaching of 
     * attachments.
     *
     * @throws LaunchFailedException if the process fails
     */
    public void open(Message msg) throws LaunchFailedException {
        openComposeWindow(constructMailto(msg));
    }

    /**
     * Launches a blank compose window.
     *
     * @throws LaunchFailedException if the process fails
     */
    public void open() throws LaunchFailedException {
        openComposeWindow("mailto:");
    }

    /**
     * Opens a compose window using the given mailto URI.
     */
    private void openComposeWindow(String mailto) throws LaunchFailedException {
        try {
            Runtime.getRuntime().exec(new String[] { "open", mailto });
            Thread.sleep(2000);
        } catch (IOException ex) {
            throw new LaunchFailedException("Cannot launch mail composer via " +
                "command line: " + ex);
        } catch (InterruptedException interruptedException) {
            interruptedException = interruptedException;
        }
    }

    /**
     * Constructs command-line arguments according to the various fields' values.
     *
     * @return mailto argument string
     */
    private String constructMailto(Message msg) {
        String mailto = "mailto:?";
        mailto += flatten("to=", msg.getToAddrs());
        mailto += flatten("cc=", msg.getCcAddrs());
        mailto += flatten("bcc=", msg.getBccAddrs());
        mailto += encode("subject=", msg.getSubject());
        mailto += encode("body=", msg.getBody());
        mailto += flatten("attach=", msg.getAttachments());
        return mailto; 
    }

    /**
     * URL-encodes 'value' with the given 'prefix'; returns the
     * empty string if 'value' is null.
     */
    private String encode(String prefix, String value) {
        if (value != null) {
            return prefix + URLUTF8Encoder.encode(value) + "&";
        }
        return "";
    }

    /**
     * Flattens the list iterated over by 'it' with the given 'prefix';
     * returns the empty string if the iterator has no elements.
     */
    private String flatten(String prefix, Iterator it) {
        String result = "";
        if (it != null) {
            while (it.hasNext()) {
                String item = (String) it.next();
                result += prefix + item + "&";
            }
        }
        return result;
    }
}
