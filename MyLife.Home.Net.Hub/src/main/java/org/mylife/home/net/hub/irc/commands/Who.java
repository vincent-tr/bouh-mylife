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

import org.mylife.home.net.hub.irc.Channel;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Constants;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class Who implements Command {
	public void invoke(RegisteredEntity src, String[] params) {
		// TODO: everything...
		String name = "*";
		Channel chan = null;
		if (params.length > 0) {
			name = params[0];
			if (Util.isChannelIdentifier(name))
				chan = src.getServer().getNetwork().getChannel(name);
		}
		if (chan != null) {
			// list channel
			for (Iterator<User> iter = chan.getUsers().iterator(); iter
					.hasNext();) {
				User usr = iter.next();
				if (!usr.isModeSet(User.UMODE_INVISIBLE) || usr.equals(src)) {
					Message message = new Message(Constants.RPL_WHOREPLY, src);
					message.appendParameter(chan.getName());
					message.appendParameter(usr.getIdent());
					message.appendParameter(usr.getDisplayHostName());
					message.appendParameter(usr.getServer().getName());
					message.appendParameter(usr.getNick());
					message.appendParameter("H");
					message.appendLastParameter("0 " + usr.getDescription());
					src.send(message);
				}
			}
		} else {
			// list server
			for (Iterator<User> iter = src.getServer().getUsers().iterator(); iter
					.hasNext();) {
				User usr = iter.next();
				// TODO: also check for in same channel
				if (Util.match(name, usr.getNick())
						&& (!usr.isModeSet(User.UMODE_INVISIBLE) || usr
								.equals(src))) {
					String chanName = "*";
					if (usr.getChannels().size() > 0) {
						chanName = ((Channel) usr.getChannels().iterator()
								.next()).getName();
					}
					Message message = new Message(Constants.RPL_WHOREPLY, src);
					message.appendParameter(chanName);
					message.appendParameter(usr.getIdent());
					message.appendParameter(usr.getDisplayHostName());
					message.appendParameter(usr.getServer().getName());
					message.appendParameter(usr.getNick());
					message.appendParameter("H");
					message.appendLastParameter("0 " + usr.getDescription());
					src.send(message);
				}
			}
		}
		Message message = new Message(Constants.RPL_ENDOFWHO, src);
		message.appendParameter(name);
		message.appendLastParameter(Util.getResourceString(src, "RPL_ENDOFWHO"));
		src.send(message);
	}

	public String getName() {
		return "WHO";
	}

	public int getMinimumParameterCount() {
		return 0;
	}
}
