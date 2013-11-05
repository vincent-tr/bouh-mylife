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

import org.mylife.home.net.hub.irc.Client;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc_p10.Util;

/**
 * @author markhale
 */
public class User_P10 extends User {
	private final int token;

	public User_P10(String nickname, int token, String ident, String hostname, String description, Server_P10 server, Client client) {
		super(nickname, ident, hostname, description, server, client);
		this.token = token;
	}
	public User_P10(String nickname, int token, String ident, String hostname, String description, Server_P10 server) {
		this(nickname, token, ident, hostname, description, server, null);
	}
	public int getToken() {
		return token;
	}
}
