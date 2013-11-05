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

import org.mylife.home.net.hub.jIRCdMBean;

/**
 * An inbound socket connection to a server.
 * This class is responsible for maintaining a client connection,
 * and dispatching Messages.
 * The Source of a Client is Unknown until it registers/logins.
 * @author thaveman
 * @author markhale
 */
public class Client {
	private final jIRCdMBean jircd; // the server we are connected to
	private final Connection connection;
	private MessageFactory messageFactory = new MessageFactory();
	private Source source;
	private long lastPing = 0; // millis
	private long lastPong = 0; // millis
	private long latency = 0; // millis
	private final long pingTimeout; // millis
	
	public Client(jIRCdMBean jircd, Connection connection) {
		if(connection == null)
			throw new NullPointerException("The connection cannot be null");
		this.jircd = jircd;
		this.connection = connection;
		pingTimeout = 1000 * Integer.parseInt(jircd.getProperty("jircd.ping.timeout", "120"));
		lastPing = System.currentTimeMillis();
		lastPong = lastPing;
		source = new Unknown(connection, this, jircd.getServer());
		source.setLocale(Util.lookupLocale(connection.getRemoteAddress()));
	}
	public final Connection getConnection() {
		return connection;
	}
	public final Source getSource() {
		return source;
	}
	public void setMessageFactory(MessageFactory messageFactory) {
		this.messageFactory = messageFactory;
	}

	/**
	 * Pings this connection.
	 * Returns false on ping timeout.
	 */
	public synchronized boolean pingMe() {
		final long curTime = System.currentTimeMillis();
		if (lastPong >= lastPing) { // got a response previously
			if (curTime - lastPong > pingTimeout || lastPong == 0) { // more than timeout seconds? or newbie?
				lastPing = curTime;
				// send ping
				Server server = source.getServer();
				Message message = new Message("PING");
				message.appendParameter(server.getName());
				source.send(message);
			}
		} else { // I have no response since the last ping
			if (curTime - lastPing > pingTimeout) { // more than timeout seconds since PING
				// should have had PONG a long time ago, timeout please!
				return false;
			}
		}
		return true;
	}

	public long idleTimeMillis() {
		return (System.currentTimeMillis() - lastPong);
	}

	public void handleLine(String line) {
		if (line.length() > Constants.MAX_MESSAGE_LENGTH)
			line = line.substring(0, Constants.MAX_MESSAGE_LENGTH); // max length per RFC

		// if there has been a response since the last PING, then don't try to ping again
		if (lastPing < lastPong)
			lastPong = System.currentTimeMillis();

		jircd.invokeCommand(source, messageFactory.createMessage(line));
	}

	public void processPong() {
		lastPong = System.currentTimeMillis();
		latency = lastPong - lastPing;
	}

	/**
	 * Registers/logs-in using the specified source.
	 * This should be used by Command classes to register Source implementations.
	 */
	public void login(Source newSource) {
		if(source instanceof Unknown) {
			if(newSource.getClient() != this)
				throw new IllegalArgumentException("The client of " + newSource.toString() + " must be " + toString() + " (it was " + newSource.getClient().toString() + ")");
			if(newSource.getServer() != jircd.getServer())
				throw new IllegalArgumentException("The server of " + newSource.toString() + " must be " + jircd.getServer().toString() + " (it was " + newSource.getServer().toString() + ")");
			source = newSource;
		} else {
			Message message = new Message(Constants.ERR_ALREADYREGISTRED, newSource);
			message.appendParameter("Unauthorized command (already registered)");
			newSource.send(message);
		}
	}
	public String toString() {
		return source.toString();
	}
}
