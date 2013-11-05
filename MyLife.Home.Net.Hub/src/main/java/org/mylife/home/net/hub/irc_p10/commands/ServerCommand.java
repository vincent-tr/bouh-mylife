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

package org.mylife.home.net.hub.irc_p10.commands;

import java.util.Iterator;

import org.mylife.home.net.hub.jIRCdMBean;
import org.mylife.home.net.hub.irc.Client;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Channel;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Unknown;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc_p10.*;

/**
 * @author markhale
 */
public class ServerCommand extends org.mylife.home.net.hub.irc.commands.ServerCommand {
	public ServerCommand(jIRCdMBean jircd) {
		super(jircd);
	}
	protected void handleCommand(Unknown src, String[] params) {
		Client client = (Client) src.getClient();
		final Connection connection = client.getConnection();
		if(checkPassword(connection, src.getPassword())) {
			String name = params[0];
			String hopcount = params[1];
			String startTime = params[2];
			String linkTime = params[3];
			String protocol = params[4];
			String tokenB64 = params[5].substring(0, 2);
			String mask = params[5].substring(2);
			String desc = params[6];
			Server_P10 thisServer = (Server_P10) jircd.getServer();
			Server_P10 server = new Server_P10(name, Util.parseBase64(tokenB64), desc, thisServer, connection, client);
			client.setMessageFactory(new Message_P10Factory());
			client.login(server);
			thisServer.getNetwork().addServer(server);

			String linkPassword = jircd.getProperty("jircd.connect."+connection.getRemoteAddress()+'#'+connection.getRemotePort());
			Message message = new Message("PASS");
			message.appendLastParameter(linkPassword);
			server.send(message);

			message = new Message("SERVER");
			message.appendParameter(thisServer.getName());
			message.appendParameter("1");
			message.appendParameter(Long.toString(jircd.getStartTimeMillis()/1000));
			message.appendParameter(Long.toString(System.currentTimeMillis()/1000));
			message.appendParameter("J10");
			message.appendParameter(Util.toBase64(thisServer.getToken(), 2)+"]]]");
			message.appendParameter("0");
			message.appendParameter(thisServer.getDescription());
			server.send(message);

			Util.sendNetSync(thisServer, server);
		} else {
			jircd.disconnectClient(client, "Invalid password");
		}
	}
	public String getName() {
		return "SERVER";
	}
	public int getMinimumParameterCount() {
		return 7;
	}
}
