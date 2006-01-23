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

package org.jdesktop.jdic.browser.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jdesktop.jdic.browser.BrowserEngineManager;

/**
 * An internal class that implements a socket client.
 * 
 * @author Kyle Yuan
 * @version 0.1, 03/07/30
 */
public class MsgClient {
	private static final int MAX_RETRY = 30;

	private static final int BUFFERSIZE = 2048;

	// socket message delimiter.
	// use these delimiters assuming they won't appear in the message itself.
	private static final String MSG_DELIMITER = "</html><body></html>";

	// a long message may be devided into several pieces:
	// a head piece, multiple middle pieces and an end piece.
	private static final String MSG_DELIMITER_ = MSG_DELIMITER + "_";

	private static final String MSG_DELIMITER_HEAD = MSG_DELIMITER + "_head";

	private static final String MSG_DELIMITER_MIDDLE = MSG_DELIMITER
			+ "_middle";

	private static final String MSG_DELIMITER_END = MSG_DELIMITER + "_end";

	private Selector selector = null;

	private SocketChannel channel = null;

	private int port;

	private InetSocketAddress serverAddr;

	private String charsetName = null;

	private CharsetDecoder decoder;

	private CharsetEncoder encoder;

	private ByteBuffer buffer;

	private CharBuffer charBuffer;

	private String sendBuffer = new String();

	private String recvBuffer = new String();

	// cached long message pieces, once a complete message is received, it will
	// be handled and removed from the set.
	private static Set msgPieces = new HashSet();

	public MsgClient() {
		WebBrowserUtil.trace("Msg Client new once!");
		// For IE on Windows, use the system default charset. With JDK 5.0,
		// there is a method Charset.defaultCharset().
		// Note: for Mozilla on Windows/*nix, use "UTF-8", as there is no
		// public/frozen APIs to use the system default charset, which must
		// be
		// the *same* charset used by the native code.
		charsetName = BrowserEngineManager.instance().getActiveEngine()
				.getCharsetName();

		Charset charset = Charset.forName(charsetName);
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
			WebBrowserUtil.trace("Found a free socket port: " + port);
		} catch (Exception e) {
		}
	}

	int getPort() {
		return port;
	}

	void connect() throws IOException, InterruptedException {
		int retry;
		for (retry = 0; retry < MAX_RETRY; retry++) {
			WebBrowserUtil.trace("Connecting to native browser ... " + retry);

			try {
				channel = SocketChannel.open();
				channel.configureBlocking(false);
				//connect to server
				channel.connect(serverAddr);
				//register events to listen
				channel.register(selector, SelectionKey.OP_CONNECT);

				while (!channel.isConnected()) {
					if (selector.select(1) > 0) {
						Set readyKeys = selector.selectedKeys();
						Iterator i = readyKeys.iterator();
						while (i.hasNext()) {
							SelectionKey key = (SelectionKey) i.next();
							i.remove();
							SocketChannel keyChannel = (SocketChannel) key
									.channel();
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
				WebBrowserUtil.trace(e.toString());
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

		WebBrowserUtil.trace("connected");
		channel.register(selector, SelectionKey.OP_READ);
	}

	// Append a sockate message string to the send buffer.
	// NOTE: the "," character is used as the message field delimiter to
	//       compose/decompose socket message strings. Which should be identical
	//       between the Java side and native side.
	public void sendMessage(String msg) {
		sendBuffer += msg + MSG_DELIMITER;
		channel.keyFor(selector).interestOps(
				SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	String getMessage() {
		int pos = recvBuffer.indexOf(MSG_DELIMITER);
		if (pos < 0)
			return null;

		String msg = recvBuffer.substring(0, pos);
		if (pos != recvBuffer.indexOf(MSG_DELIMITER_)) {
			// a short message.
			recvBuffer = recvBuffer.substring(pos
					+ (new String(MSG_DELIMITER).length()));
			WebBrowserUtil.trace("Got a complete short message: " + msg);
			return msg;
		}

		NativeEventData eventData = NativeEventThread.parseMessageString(msg);

		// receive a long message, consisting of one head message piece,
		// multiple middle message pieces and one end message piece.
		if (pos == recvBuffer.indexOf(MSG_DELIMITER_HEAD)) {
			// The head piece of a long message.
			// Each head piece contains the instance and type information
			// identifying a long message.
			msgPieces.add(new NativeEventData(eventData.instance,
					eventData.type, eventData.stringValue));
			recvBuffer = recvBuffer.substring(pos
					+ (new String(MSG_DELIMITER_HEAD).length()));
			WebBrowserUtil.trace("Got a head message piece: "
					+ eventData.stringValue);
			return null;
		} else {
			Iterator it = msgPieces.iterator();
			while (it.hasNext()) {
				NativeEventData element = (NativeEventData) it.next();
				if ((element.instance == eventData.instance)
						&& (element.type == eventData.type)) {
					if (pos == recvBuffer.indexOf(MSG_DELIMITER_MIDDLE)) {
						NativeEventData newElement = new NativeEventData(
								eventData.instance, eventData.type,
								element.stringValue + eventData.stringValue);
						msgPieces.remove(element);
						msgPieces.add(newElement);
						recvBuffer = recvBuffer.substring(pos
								+ (new String(MSG_DELIMITER_MIDDLE).length()));
						WebBrowserUtil.trace("Got a middle message piece: "
								+ eventData.stringValue);
						return null;
					} else if (pos == recvBuffer.indexOf(MSG_DELIMITER_END)) {
						// The end piece of a long message. Concat the complete
						// message and return it.
						msg = eventData.instance + "," + eventData.type + ","
								+ element.stringValue + eventData.stringValue;
						msgPieces.remove(element);
						recvBuffer = recvBuffer.substring(pos
								+ (new String(MSG_DELIMITER_END).length()));
						WebBrowserUtil.trace("Got an end message piece: "
								+ eventData.stringValue);
						WebBrowserUtil.trace("Got a complete long message: "
								+ element.stringValue + eventData.stringValue);

						return (msg);
					}
				}
			}

			return null;
		}
	}

	void portListening() throws IOException, InterruptedException {
		if (selector != null && selector.select(1) > 0) {
			Set readyKeys = selector.selectedKeys();
			Iterator i = readyKeys.iterator();
			while (i.hasNext()) {
				SelectionKey key = (SelectionKey) i.next();
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
					WebBrowserUtil
							.trace("Read data from socket: " + recvBuffer);
				} else if (key.isWritable()) {
					if (sendBuffer.length() > 0) {
						WebBrowserUtil.trace("Send data to socket: "
								+ sendBuffer);
						ByteBuffer buf = ByteBuffer.wrap(sendBuffer
								.getBytes(charsetName));
						keyChannel.write(buf);
						sendBuffer = "";
						key.interestOps(SelectionKey.OP_READ);
					}
				}
			}
		}
	}
}
