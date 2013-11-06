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
public class Kill implements Command {
	protected final IrcServerMBean jircd;

	public Kill(IrcServerMBean jircd) {
		this.jircd = jircd;
	}
	public void invoke(Source src, String[] params) {
		if(src instanceof User) {
			User user = (User) src;
			if(!hasPermission(user)) {
				Util.sendNoPrivilegesError(src);
				return;
			}
		}
		final String nick = params[0];
		if(Util.isNickName(nick)) {
			User user = src.getServer().getNetwork().getUser(nick);
			if(user == null) {
				Util.sendNoSuchNickError(src, nick);
			} else {
				jircd.disconnectClient((Client)user.getClient(), "Kill by " + src.getNick() + ": " + params[1]);
			}
		}
	}
	private boolean hasPermission(User user) {
		return user.isModeSet(User.UMODE_OPER);
	}
	public String getName() {
		return "KILL";
	}
	public int getMinimumParameterCount() {
		return 2;
	}
}
