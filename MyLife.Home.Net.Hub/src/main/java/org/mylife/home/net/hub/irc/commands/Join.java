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
public class Join implements Command {
	public void invoke(Source src, String[] params) {
		String[] chanNames = Util.split(params[0], ',');
		for(int i=0; i<chanNames.length; i++) {
			final String channame = chanNames[i];
			if (Util.isChannelIdentifier(channame)) {
				Channel chan = src.getServer().getNetwork().getChannel(channame);
				User user = (User) src;
				if (chan == null) {
					chan = createChannel(channame);
					src.getServer().getNetwork().addChannel(chan);
					chan.joinUser(user, null);
				} else {
					chan.joinUser(user, params);
				}
			} else {
				Util.sendNoSuchChannelError(src, channame);
			}
		}
	}
	protected Channel createChannel(String name) {
		return new Channel(name);
	}
	public String getName() {
		return "JOIN";
	}
	public int getMinimumParameterCount() {
		return 1;
	}
}
