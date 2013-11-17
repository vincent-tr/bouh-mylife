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

import java.util.Iterator;

import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Constants;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class Links implements Command {
	public void invoke(RegisteredEntity src, String[] params) {
		// TODO: wildcards and things, Iterator should be synchronized
		String mask = "*";
		for (Iterator<Server> iter = src.getServer().getNetwork().getServers()
				.iterator(); iter.hasNext();) {
			Server server = iter.next();
			Message message = new Message(Constants.RPL_LINKS, src);
			message.appendParameter(mask);
			message.appendParameter(server.getName());
			message.appendLastParameter("0 " + server.getDescription());
			src.send(message);
		}
		Message message = new Message(Constants.RPL_ENDOFLINKS, src);
		message.appendParameter(mask);
		message.appendLastParameter(Util.getResourceString(src,
				"RPL_ENDOFLINKS"));
		src.send(message);
	}

	public String getName() {
		return "LINKS";
	}

	public int getMinimumParameterCount() {
		return 0;
	}
}
