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

import org.mylife.home.net.hub.irc.*;

/**
 * @author markhale
 */
public class Notice implements Command {
	public void invoke(Source src, String[] params) {
		String msgdest = params[0];
		String msg = params[1];
		if (msgdest.charAt(0) == '#') {
			// message to channel
			Channel chan = src.getServer().getNetwork().getChannel(msgdest);
			if (chan == null) {
				Util.sendNoSuchChannelError(src, msgdest);
			} else {
				if(src instanceof User) {
					User user = (User) src;
					Message message = new Message(user, "NOTICE", chan);
					message.appendLastParameter(msg);
					chan.send(message, user);
				} else {
					Server server = (Server) src;
					Message message = new Message(server, "NOTICE", chan);
					message.appendLastParameter(msg);
					chan.send(message);
				}
			}
		} else if (msgdest.charAt(0) == '&') {
			// message to local channel
			Util.sendNoSuchChannelError(src, msgdest);
		} else {
			// message to user
			User target = findUser(src.getServer().getNetwork(), msgdest);
			if (target == null) {
				Util.sendNoSuchNickError(src, msgdest);
			} else {
				if(src instanceof User) {
					Message message = new Message((User)src, "NOTICE", target);
					message.appendLastParameter(msg);
					target.send(message);
				} else {
					Message message = new Message((Server)src, "NOTICE", target);
					message.appendLastParameter(msg);
					target.send(message);
				}
			}
		}
	}
	protected User findUser(Network network, String nick) {
		return network.getUser(nick);
	}
	public String getName() {
		return "NOTICE";
	}
	public int getMinimumParameterCount() {
		return 2;
	}
}
