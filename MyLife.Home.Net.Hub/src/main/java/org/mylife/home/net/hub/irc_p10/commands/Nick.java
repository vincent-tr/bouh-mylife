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

import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Source;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc_p10.Server_P10;
import org.mylife.home.net.hub.irc_p10.User_P10;
import org.mylife.home.net.hub.irc_p10.Util;

/**
 * @author markhale
 */
public class Nick implements Command {
	public void invoke(Source src, String[] params) {
		if (src instanceof Server_P10) {
			Server_P10 server = (Server_P10) src;
			String nick = params[0];
			String hopcount = params[1];
			String timestamp = params[2];
			String ident = params[3];
			String host = params[4];
			String tokenB64 = params[params.length-2];
			String desc = params[params.length-1];
			int token = Util.parseBase64(tokenB64);
			User user = new User_P10(nick, token, ident, host, desc, server);
			server.addUser(user);
			server.getNetwork().tokens.put(new Integer(token), user);
		} else {
			throw new IllegalArgumentException("Source must be a P10 server, it was "+src);
		}
	}
	public String getName() {
		return "N";
	}
	public int getMinimumParameterCount() {
		return 8;
	}
}
