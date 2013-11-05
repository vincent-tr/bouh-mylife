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

/**
 * An unknown source.
 * @author markhale
 */
public final class Unknown extends Source {
	private final Connection connection;
	private final Server server;
	private final Object client;
	private String password;
	private String nickname;
	private String[] params;

	public Unknown(Connection connection, Object client, Server server) {
		if(server == null)
			throw new NullPointerException("The server cannot be null");
		this.connection = connection;
		this.client = client;
		this.server = server;
	}
	public final Object getClient() {
		return client;
	}
	public void setPassword(String pwd) {
		password = pwd;
	}
	public String getPassword() {
		return password;
	}
	public void setNick(String name) {
		nickname = name;
	}
	public String getNick() {
		return nickname;
	}
	/** User parameters */
	public void setParameters(String[] params) {
		this.params = params;
	}
	public String[] getParameters() {
		return params;
	}
	/** ID */
	public String toString() {
		return "Unknown";
	}
	/**
	 * Returns the server this user is connected to.
	 */
	public final Server getServer() {
		return server;
	}
	
	public void send(Message msg) {
		connection.writeLine(msg.toString());
	}
}
