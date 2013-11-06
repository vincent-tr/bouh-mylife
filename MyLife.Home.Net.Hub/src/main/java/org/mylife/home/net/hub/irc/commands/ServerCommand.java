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

package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.configuration.IrcLinkAccept;
import org.mylife.home.net.hub.configuration.IrcLinkConnect;
import org.mylife.home.net.hub.irc.*;

/**
 * @author markhale
 */
public class ServerCommand implements Command {
	protected final IrcServerMBean jircd;

	public ServerCommand(IrcServerMBean jircd) {
		this.jircd = jircd;
	}
	public final void invoke(Source src, String[] params) {
		if(src instanceof Unknown) {
			handleCommand((Unknown)src, params);
		} else {
			Message message = new Message(Constants.ERR_ALREADYREGISTRED, src);
			message.appendParameter(Util.getResourceString(src, "ERR_ALREADYREGISTRED"));
			src.send(message);
		}
	}
	protected void handleCommand(Unknown src, String[] params) {
		Client client = (Client) src.getClient();
		final Connection connection = client.getConnection();
		if(checkPassword(connection, src.getPassword())) {
			String name = params[0];
			//String hopcount = params[1];
			String token = params[2];
			String desc = params[3];
			Server thisServer = jircd.getServer();
			Server server = new Server(name, Integer.parseInt(token), desc, thisServer, connection, client);
			client.login(server);
			thisServer.getNetwork().addServer(server);

			IrcLinkConnect configLink = jircd.findLinkConnect(connection.getRemoteAddress(), connection.getRemotePort());
			String linkPassword = configLink.getPassword();
			Message message = new Message("PASS");
			message.appendParameter(linkPassword);
			message.appendParameter("0210");
			message.appendParameter("IRC|");
			server.send(message);

			message = new Message("SERVER");
			message.appendParameter(thisServer.getName());
			message.appendParameter("1");
			message.appendParameter(Integer.toString(server.getToken()));
			message.appendParameter(thisServer.getDescription());
			server.send(message);

			Util.sendNetSync(thisServer, server);
		} else {
			jircd.disconnectClient(client, "Invalid password");
		}
	}
	protected boolean checkPassword(Connection connection, String password) {
		IrcLinkAccept configLink = jircd.findLinkAccept(connection.getRemoteAddress(), connection.getLocalPort());
		String expectedPassword = configLink.getPassword();
		return expectedPassword != null && expectedPassword.equals(password);
	}
	public String getName() {
		return "SERVER";
	}
	public int getMinimumParameterCount() {
		return 4;
	}
}
