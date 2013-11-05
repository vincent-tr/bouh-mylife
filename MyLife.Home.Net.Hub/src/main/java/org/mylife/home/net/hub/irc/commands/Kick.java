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
public class Kick implements Command {
	public void invoke(Source src, String[] params) {
		final String channame = params[0];
		final String nickname = params[1];
		if (Util.isChannelIdentifier(channame)) {
			// check channel exists
			Channel chan = src.getServer().getNetwork().getChannel(channame);
			if (chan == null) {
				Util.sendNoSuchChannelError(src, channame);
			} else {
				if(Util.isNickName(nickname)) {
					// check nick exists
					User user = src.getServer().getNetwork().getUser(nickname);
					if(user == null) {
						Util.sendNoSuchNickError(src, nickname);
					} else {
						if(chan.isOp((User)src)) {
							Message message = new Message((User)src, "KICK", chan);
							message.appendParameter(user.getNick());
							if(params.length == 3)
								message.appendParameter(params[2]);
							chan.send(message);
							chan.removeUser(user);
						} else {
							Message message = new Message(Constants.ERR_CHANOPRIVSNEEDED, src);
							message.appendParameter(channame);
							message.appendParameter("You're not channel operator");
							src.send(message);
						}
					}
				}
			}
		} else {
			Util.sendNoSuchChannelError(src, channame);
		}
	}
	public String getName() {
		return "KICK";
	}
	public int getMinimumParameterCount() {
		return 2;
	}
}
