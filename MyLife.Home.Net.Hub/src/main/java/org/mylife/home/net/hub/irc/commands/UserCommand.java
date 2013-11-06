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

import java.util.Date;

import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.irc.Client;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc.Constants;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.Source;
import org.mylife.home.net.hub.irc.Unknown;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class UserCommand implements Command {
	protected final IrcServerMBean jircd;

	public UserCommand(IrcServerMBean jircd) {
		this.jircd = jircd;
	}
	public final void invoke(final Source src, String[] params) {
		if(src instanceof Unknown) {
			handleCommand((Unknown)src, params);
		} else {
			Message message = new Message(Constants.ERR_ALREADYREGISTRED, src);
			message.appendParameter(Util.getResourceString(src, "ERR_ALREADYREGISTRED"));
			src.send(message);
		}
	}
	private void handleCommand(Unknown src, String[] params) {
		String nick = src.getNick();
		if(nick == null) {
			// if we have yet to receive a NICK command, remember these USER parameters
			src.setParameters(params);
		} else {
			// already received a NICK command, so can complete login
			Client client = (Client) src.getClient();
			final Connection connection = client.getConnection();
			if(checkPassword(connection, src.getPassword())) {
				String username = params[0];
				//String hostname = params[1];
				//String servername = params[2];
				String desc = params[3];
				Server thisServer = src.getServer();
				User user = createUser(nick, username, connection.getRemoteHost(), desc, thisServer, client);
				user.setLocale(src.getLocale());
				client.login(user);
				thisServer.addUser(user);
				broadcastNewUser(user, thisServer);

				Message message = new Message(Constants.RPL_WELCOME, src);
				message.appendParameter("Welcome to the " + thisServer.getNetwork().toString() + " " + nick + "!" + username + "@" + connection.getRemoteHost());
				src.send(message);

				message = new Message(Constants.RPL_YOURHOST, src);
				message.appendParameter("Your host is " + thisServer.getName() + ", running version " + jircd.getVersion());
				src.send(message);

				message = new Message(Constants.RPL_CREATED, src);
				message.appendParameter("This server was created " + new Date(jircd.getStartTimeMillis()));
				src.send(message);

				message = new Message(Constants.RPL_MYINFO, src);
				message.appendParameter(thisServer.getName() + " " + jircd.getVersion() + " - -");
				src.send(message);

				message = new Message(Constants.RPL_ISUPPORT, src);
				message.appendParameter("NICKLEN="+Constants.MAX_NICK_LENGTH);
				message.appendParameter("CHANNELLEN="+Constants.MAX_CHANNEL_LENGTH);
				message.appendParameter("TOPICLEN="+Constants.MAX_TOPIC_LENGTH);
				message.appendParameter("PREFIX=(ov)@+");
				message.appendParameter("CHANTYPES=#");
				message.appendParameter("CHANMODES=b,k,l,imt");
				message.appendParameter("CASEMAPPING=ascii");
				message.appendParameter("NETWORK=" + thisServer.getNetwork().toString());
				message.appendParameter("are supported by this server");
				src.send(message);

				Command command = jircd.getCommand("LUSERS");
				command.invoke(src, null);
				command = jircd.getCommand("MOTD");
				command.invoke(src, null);
			} else {
				jircd.disconnectClient(client, "Invalid password");
			}
		}
	}
	private boolean checkPassword(Connection connection, String password) {
		//String expectedPassword = jircd.getProperty("jircd.accept."+connection.getRemoteAddress()+'#'+connection.getLocalPort());
		//if(expectedPassword != null)
		//	return expectedPassword.equals(password);
		//else
			return true;
	}
	protected User createUser(String nick, String username, String hostname, String desc, Server thisServer, Client client) {
		return new User(nick, username, hostname, desc, thisServer, client);
	}
	/**
	 * Broadcasts a new user to other servers on the network.
	 * @param user a new user
	 * @param thisServer the server the user is connected to
	 */
	protected void broadcastNewUser(User user, Server thisServer) {
		for(Server server : thisServer.getNetwork().servers.values()) {
			if(server != thisServer) {
				Message message = new Message(thisServer, "NICK");
				message.appendParameter(user.getNick());
				message.appendParameter("1");
				message.appendParameter(user.getIdent());
				message.appendParameter(user.getHostName());
				message.appendParameter(Integer.toString(thisServer.getToken()));
				message.appendParameter(user.getModesList());
				message.appendParameter(user.getDescription());
				server.send(message);
			}
		}
	}
	public String getName() {
		return "USER";
	}
	public int getMinimumParameterCount() {
		return 4;
	}
}
