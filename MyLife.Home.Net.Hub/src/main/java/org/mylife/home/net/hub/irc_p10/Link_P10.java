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

import java.io.IOException;

import org.mylife.home.net.hub.jIRCdMBean;
import org.mylife.home.net.hub.irc.Connector;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.Unknown;

/**
 * @author markhale
 */
public class Link_P10 extends jircd.irc.Link {
	public Link_P10(jIRCdMBean jircd, Connector connection) {
		super(jircd, connection);
	}
	protected void login(Server thisServer) {
		connection.writeLine("PASS :"+"linkPassword");
		connection.writeLine("SERVER "+thisServer.getName()+" 1 "+Long.toString(jircd.getStartTimeMillis()/1000)+" "+Long.toString(System.currentTimeMillis()/1000)+" J10 "+Util.toBase64(thisServer.getToken(), 2)+"]]] 0"+" :"+thisServer.getDescription());
	}
	protected void loginServer(Message loginMsg) {
		final Server_P10 thisServer = (Server_P10) jircd.getServer();
		if(server instanceof Server) {
			connection.writeLine(':' + thisServer.getName() + " 462 :You may not reregister");
		} else {
			String password = ((Unknown)server).getPassword();
			String name = loginMsg.getParameter(0);
			String tokenAndMask = loginMsg.getParameter(5);
			String desc = loginMsg.getParameter(6);
			int token = Util.parseBase64(tokenAndMask.substring(0, 2));
			server = new Server_P10(name, token, desc, thisServer, connection, this);
			thisServer.getNetwork().addServer((Server)server);
			setMessageFactory(new Message_P10Factory());
			Util.sendNetSync(thisServer, (Server_P10)server);
		}
	}
}
