/*
 * jIRCd - Java Internet Relay Chat Daemon
 * Copyright 2003 Tyrel L. Haveman <tyrel@haveman.net>
 *
 * This file is part of jIRCd.
 *
 * jIRCd is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * jIRCd is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with jIRCd; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.mylife.home.net.hub.irc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.net.hub.IrcServerMBean;

/**
 * An outbound socket connection to another server.
 * @author thaveman
 * @author markhale
 */
public class Connector extends Connection implements Runnable {
	private static final Logger logger = Logger.getLogger(Connector.class.getName());

	protected final IrcServerMBean jircd;
	private final BufferedReader input;
	private final BufferedWriter output;
	private final Link link;
	private volatile boolean dontDie;

	public Connector(IrcServerMBean jircd, String host, int port, org.mylife.home.net.hub.irc.commands.Connect connectFactory) throws IOException {
		super(new Socket(host, port));
		this.jircd = jircd;
		input = new BufferedReader(new InputStreamReader(socket.getInputStream(), Constants.CHARSET), Constants.MAX_MESSAGE_SIZE);
		output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Constants.CHARSET), Constants.MAX_MESSAGE_SIZE);
		link = connectFactory.newLink(jircd, this);
	}
	public final Link getLink() {
		return link;
	}
	public void run() {
		dontDie = true;
		while(dontDie) {
			try {
				String line = input.readLine();
				logger.finest("Message received from " + toString() + "\n\t" + line);
				if (line != null && line.length() > 0) {
					bytesRead += line.length()+2;
					linesRead++;
					link.handleLine(line);
				} else if (line == null) {
					jircd.disconnectLink(link);
					return;
				}
			} catch(SocketException e) {
				jircd.disconnectLink(link);
				return;
			} catch (Exception e) {
				logger.log(Level.WARNING, "Exception occured in thread " + Thread.currentThread().toString(), e);
				return;
			}
		}
	}
	public void writeLine(String text) {
		// Do not use PrintWriter.println() since that depends on the system property line.separator,
		// which may not be "\r\n".
		try {
			output.write(text);
			output.write("\r\n");
			output.flush();
			bytesSent += text.length()+2;
			linesSent++;
			if(logger.isLoggable(Level.FINEST))
				logger.finest("Message sent to " + toString() + "\n\t" + text);
		} catch(IOException e) {
			logger.log(Level.FINEST, "Exception occurred while sending message", e);
		}
	}
	public void close() {
		dontDie = false;
		try {
			socket.close();
		} catch(IOException e) {
			logger.log(Level.FINEST, "Exception on socket close", e);
		}
	}
	public String toString() {
		return "Connection[" + link.toString() + ',' + socket.getInetAddress().getHostAddress() + ':' + socket.getPort() + ',' + socket.getClass().getName() + ']';
	}
}
