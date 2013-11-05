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

package org.mylife.home.net.hub.irc_p10;

import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc.Network;

/**
 * A P10 server on a server.
 * @author markhale
 */
public class Server_P10 extends Server {
	public Server_P10(String name, int token, String description, Server route, Connection connection, Object client) {
		super(name, token, description, route, connection, client);
	}
	public Server_P10(String name, int token, String description, Network network) {
		super(name, token, description, network);
	}
	/** ID */
	public String toString() {
		return Util.toBase64(token, 2);
	}
	public void send(Message msg) {
		msg = Util.transcode(network, msg);
		if(connection != null)
			connection.writeLine(msg.toString());
		else
			route.send(msg);
	}
}
