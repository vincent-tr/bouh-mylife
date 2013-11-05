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

import org.mylife.home.net.hub.jIRCdMBean;
import org.mylife.home.net.hub.irc.*;

/**
 * @author markhale
 */
public class Nick implements Command {
	protected final jIRCdMBean jircd;

	public Nick(jIRCdMBean jircd) {
		this.jircd = jircd;
	}
	public void invoke(final Source src, String[] params) {
		if (src instanceof User) {
			// user is requesting a change of nick
			User user = (User) src;
			String newNick = params[0];
			if (Util.isNickName(newNick)) {
				User newUser = src.getServer().getNetwork().getUser(newNick);
				if (newUser != null) {
					sendNickNameInUseError(src, newNick);
				} else {
					user.changeNick(newNick);
				}
			} else {
				sendErroneousNickNameError(src, newNick);
			}
		} else if (src instanceof Server) {
			if (params.length == 7) {
				String nick = params[0];
				String hopcount = params[1];
				String ident = params[2];
				String host = params[3];
				String token = params[4];
				String modes = params[5];
				String desc = params[6];
				User user = new User(nick, ident, host, desc, (Server)src);
				((Server)src).getServer().addUser(user);
			} else {
				// too few parameters
				Util.sendNeedMoreParamsError(src, getName());
			}
		} else if (src instanceof Unknown) {
			String newNick = params[0];
			if (Util.isNickName(newNick)) {
				if (src.getServer().getNetwork().getUser(newNick) == null) {
					Unknown unknown = (Unknown) src;
					unknown.setNick(newNick);
					String[] userParams = unknown.getParameters();
					if (userParams != null) {
						// re-invoke USER command
						Command command = jircd.getCommand("USER");
						command.invoke(src, userParams);
					}
				} else {
					sendNickNameInUseError(src, newNick);
				}
			} else {
				sendErroneousNickNameError(src, newNick);
			}
		}
	}
	private static void sendNickNameInUseError(Source src, String nick) {
		Message message = new Message(Constants.ERR_NICKNAMEINUSE, src);
		message.appendParameter(nick);
		message.appendParameter(Util.getResourceString(src, "ERR_NICKNAMEINUSE"));
		src.send(message);
	}
	private static void sendErroneousNickNameError(Source src, String nick) {
		Message message = new Message(Constants.ERR_ERRONEUSNICKNAME, src);
		message.appendParameter(nick);
		message.appendParameter(Util.getResourceString(src, "ERR_ERRONEUSNICKNAME"));
		src.send(message);
	}
	public String getName() {
		return "NICK";
	}
	public int getMinimumParameterCount() {
		return 1;
	}
}
