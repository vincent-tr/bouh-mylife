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
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc_p10.Message_P10;
import org.mylife.home.net.hub.irc_p10.User_P10;
import org.mylife.home.net.hub.irc_p10.Server_P10;
import org.mylife.home.net.hub.irc_p10.Util;

/**
 * @author markhale
 */
public class UserCommand extends org.mylife.home.net.hub.irc.commands.UserCommand {
	public UserCommand(jIRCdMBean jircd) {
		super(jircd);
	}
	protected User createUser(String nick, String username, String hostname, String desc, Server thisServer, Client client) {
		return new User_P10(nick, Util.randomUserToken(thisServer), username, hostname, desc, (Server_P10)thisServer, client);
	}
	protected void broadcastNewUser(User user, Server thisServer) {
		thisServer.getNetwork().tokens.put(new Integer(((User_P10)user).getToken()), user);
		for(Iterator iter = thisServer.getNetwork().servers.values().iterator(); iter.hasNext(); ) {
			Server_P10 server = (Server_P10) iter.next();
			if(server != thisServer) {
				Message_P10 message = new Message_P10((Server_P10)thisServer, "N");
				message.appendParameter(user.getNick());
				message.appendParameter("1");
				message.appendParameter("1000000000");
				message.appendParameter(user.getIdent());
				message.appendParameter(user.getHostName());
				message.appendParameter(user.getModesList());
				message.appendParameter("AAAAAA");
				message.appendParameter(Util.toBase64(((User_P10)user).getToken(), 5));
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
