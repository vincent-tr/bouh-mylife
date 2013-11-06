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
import org.mylife.home.net.hub.irc.*;

/**
 * @author markhale
 */
public class Admin implements Command {
	private final String server;
	private final String location1;
	private final String location2;
	private final String email;

	public Admin(IrcServerMBean jircd) {
		server = jircd.getHostName();
		location1 = jircd.getConfiguration().getLocation1();
		location2 = jircd.getConfiguration().getLocation2();
		email = jircd.getConfiguration().getEmail();
	}
	public void invoke(Source src, String[] params) {
		if(src instanceof Unknown)
			Util.sendNotRegisteredError(src);
		else
			handleCommand(src, params);
	}
	private void handleCommand(Source src, String[] params) {
		Message message = new Message(Constants.RPL_ADMINME, src);
		message.appendParameter(server);
		message.appendParameter("Administrative info");
		src.send(message);

		message = new Message(Constants.RPL_ADMINLOC1, src);
		message.appendParameter(location1);
		src.send(message);

		message = new Message(Constants.RPL_ADMINLOC2, src);
		message.appendParameter(location2);
		src.send(message);

		message = new Message(Constants.RPL_ADMINEMAIL, src);
		message.appendParameter(email);
		src.send(message);
	}
	public String getName() {
		return "ADMIN";
	}
	public int getMinimumParameterCount() {
		return 0;
	}
}
