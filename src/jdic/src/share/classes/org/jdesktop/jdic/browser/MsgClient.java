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

package org.jdesktop.jdic.browser;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;
import java.util.*;

/**
 * An internal class that implements a socket client.
 * 
 * @author Kyle Yuan
 * @version 0.1, 03/07/30
 */ 
class MsgClient {
    private static final int MAX_RETRY = 30;
    private static final int BUFFERSIZE = 1024;

    private Selector selector = null;
    private SocketChannel channel = null;
    private int port;
    private InetSocketAddress serverAddr;

    private CharsetDecoder decoder;
    private CharsetEncoder encoder;
    private ByteBuffer buffer;
    private CharBuffer charBuffer;

    private String sendBuffer = new String();
    private String recvBuffer = new String();

    MsgClient() {
        Charset charset = Charset.forName("ISO-8859-1");
        decoder = charset.newDecoder();
        encoder = charset.newEncoder();
        buffer = ByteBuffer.allocateDirect(BUFFERSIZE);
        charBuffer = CharBuffer.allocate(BUFFERSIZE);

        try {
            //initialize a Selector
            selector = Selector.open();

            ServerSocketChannel sc = ServerSocketChannel.open();
            //find a free port
            sc.socket().bind(new InetSocketAddress("localhost", 0));
            port = sc.socket().getLocalPort();
            sc.close();
            sc = null;
            serverAddr = new InetSocketAddress("localhost", port);
            WebBrowser.trace("found a free port: " + port);
        } catch (Exception e) {
        }
    }

    int getPort() {
        return port;
    }

    void connect() throws IOException, InterruptedException {
        int retry;
        for (retry = 0; retry < MAX_RETRY; retry++) {
            WebBrowser.trace("connecting ... " + retry);

            try {
                channel = SocketChannel.open();
                channel.configureBlocking(false);
                //connect to server
                channel.connect(serverAddr);
                //register events to listen
                channel.register(selector, SelectionKey.OP_CONNECT);

                while (! channel.isConnected()) {
                    if (selector.select(1) > 0) {
                        Set readyKeys = selector.selectedKeys();
                        Iterator i = readyKeys.iterator();
                        while (i.hasNext()) {
                            SelectionKey key = (SelectionKey)i.next();
                            i.remove();
                            SocketChannel keyChannel = (SocketChannel) key.channel();
                            if (key.isConnectable()) {
                                if (keyChannel.isConnectionPending()) {
                                    keyChannel.finishConnect();
                                }
                                break;
                            }
                        }
                    }
                }
                break;
            } catch (Exception e) {
                WebBrowser.trace(e.toString());
                channel.close();
                channel = null;
                try {
                    Thread.sleep(150);
                } catch (Exception ex) {
                }
            }

        }

        if (retry == MAX_RETRY) {
            throw new InterruptedException("Maximum retry number reached!");
        }

        WebBrowser.trace("connected");
        channel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    void sendMessage(String msg) {
        sendBuffer += msg + "\n";
    }

    String getMessage() {
        int pos = recvBuffer.indexOf("\n");
        if (pos >= 0) {
            String msg = recvBuffer.substring(0, pos);
            recvBuffer = recvBuffer.substring(pos + 1);
            return msg;
        }
        else {
            return null;
        }
    }

    void portListening() throws IOException, InterruptedException {
        if (selector != null && selector.select(1) > 0) {
            Set readyKeys = selector.selectedKeys();
            Iterator i = readyKeys.iterator();
            while (i.hasNext()) {
                SelectionKey key = (SelectionKey)i.next();
                i.remove();
                SocketChannel keyChannel = (SocketChannel) key.channel();
                if (key.isReadable()) {
                    buffer.clear();
                    charBuffer.clear();
                    keyChannel.read(buffer);
                    buffer.flip();
                    decoder.decode(buffer, charBuffer, false);
                    charBuffer.flip();
                    recvBuffer += charBuffer;
                }
                else if (key.isWritable()) {
                    if (sendBuffer.length() > 0) {
                        WebBrowser.trace("send data to socket: " + sendBuffer);
                        ByteBuffer buf = ByteBuffer.wrap(sendBuffer.getBytes());
                        keyChannel.write(buf);
                        sendBuffer = "";
                    }
                }
            }
        }
    }
}
