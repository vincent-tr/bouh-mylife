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
public class Part implements Command {
	public void invoke(Source src, String[] params) {
		final String channame = params[0];
		if (Util.isChannelIdentifier(channame)) {
			Channel chan = src.getServer().getNetwork().getChannel(channame);
			if (chan == null) {
				Util.sendNoSuchChannelError(src, channame);
			} else {
				User user = (User) src;
				if(chan.isOn(user)) {
					Message message = new Message(user, "PART", chan);
					if(params.length == 2)
						message.appendParameter(params[1]);
					chan.send(message);
					chan.removeUser(user);
				} else {
					Util.sendNotOnChannelError(src, channame);
				}
			}
		} else {
			Util.sendNoSuchChannelError(src, channame);
		}
	}
	public String getName() {
		return "PART";
	}
	public int getMinimumParameterCount() {
		return 1;
	}
}
