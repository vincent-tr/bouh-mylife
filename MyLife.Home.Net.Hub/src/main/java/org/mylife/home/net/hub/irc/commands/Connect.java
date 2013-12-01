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

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.ConnectedEntity;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc.Constants;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.StreamConnection;
import org.mylife.home.net.hub.irc.UnregisteredEntity;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class Connect implements Command {
	private final IrcServerMBean jircd;

	public Connect(IrcServerMBean jircd) {
		this.jircd = jircd;
	}

	public final void invoke(RegisteredEntity src, String[] params) {
		final String host = params[0];
		final int port = params.length > 1 ? Integer.parseInt(params[1])
				: Constants.DEFAULT_PORT;
		User user = (User) src;
		try {
			Util.checkOperatorPermission(user);
			try {
				StreamConnection connection = new StreamConnection(new Socket(
						host, port), jircd.getConnectLinks(),
						Executors.newSingleThreadExecutor(), true);
				Connection.Handler handler = newConnectionHandler(jircd,
						connection);
				connection.setHandler(handler);
				connection.start();
				UnregisteredEntity entity = (UnregisteredEntity) handler
						.getEntity();
				/*
				IrcLinkConnect configLink = jircd.findLinkConnect(
						connection.getRemoteAddress(),
						connection.getRemotePort());
				*/
				sendLogin(entity);
			} catch (IOException e) {
				Message message = new Message(Constants.ERR_NOSUCHSERVER, src);
				message.appendParameter(host);
				message.appendLastParameter("No such server");
				src.send(message);
			}
		} catch (SecurityException se) {
			Util.sendNoPrivilegesError(src);
		}
	}

	protected Connection.Handler newConnectionHandler(IrcServerMBean jircd,
			Connection connection) {
		return new Connection.Handler(jircd, connection);
	}

	protected void sendLogin(UnregisteredEntity entity) {
		//sendPass(entity, linkPassword);
		sendServer(entity, jircd);
		entity.setParameters(new String[0]); // so that we know we sent PASS &
												// SERVER
	}

	protected void sendPass(ConnectedEntity to, String password) {
		Util.sendPass(to, password);
	}

	protected void sendServer(ConnectedEntity to, IrcServerMBean jircd) {
		Util.sendServer(to);
	}

	public String getName() {
		return "CONNECT";
	}

	public int getMinimumParameterCount() {
		return 1;
	}
}
