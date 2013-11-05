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
public class Mode implements Command {
	public void invoke(Source src, String[] params) {
		String modeDest = params[0];

		if (Util.isChannelIdentifier(modeDest)) {
			// channel mode
			Channel chanDest = src.getServer().getNetwork().getChannel(modeDest);
			if (chanDest == null) {
				Util.sendNoSuchChannelError(src, modeDest);
			} else {
				User user = (User) src;
				if (chanDest.isOn(user)) {
					if (params.length > 1) {
						if (hasPermission(user, chanDest)) {
							String modeString = params[1];
							String[] modeParams = new String[params.length-2];
							for (int i = 0; i < modeParams.length; i++) {
								modeParams[i] = params[i+2];
							}
							chanDest.processModes(user, modeString, modeParams);
						} else {
							Message message = new Message(Constants.ERR_CHANOPRIVSNEEDED, src);
							message.appendParameter(modeDest);
							message.appendParameter(Util.getResourceString(src, "ERR_CHANOPRIVSNEEDED"));
							src.send(message);
						}
					} else {
						Message message = new Message(Constants.RPL_CHANNELMODEIS, src);
						message.appendParameter(modeDest);
						message.appendParameter(chanDest.getModesList());
						src.send(message);
					}
				} else {
					// not on channel
					Message message = new Message(Constants.ERR_NOTONCHANNEL, src);
					message.appendParameter(modeDest);
					message.appendParameter(Util.getResourceString(src, "ERR_NOTONCHANNEL"));
					src.send(message);
				}
			}
		} else {
			// user mode
			User sender = (User) src;
			User userDest = src.getServer().getNetwork().getUser(modeDest);
			if (hasPermission(sender, userDest)) {
				if (params.length > 1 && params[1].length() > 0) {
					userDest.processModes(params[1]);
				} else {
					Message message = new Message(Constants.RPL_UMODEIS, userDest);
					message.appendParameter(userDest.getModesList());
					userDest.send(message);
				}
			} else {
				Message message = new Message(Constants.ERR_USERSDONTMATCH, src);
				message.appendParameter(modeDest);
				message.appendParameter(Util.getResourceString(src, "ERR_USERSDONTMATCH"));
				src.send(message);
			}
		}
	}
	private boolean hasPermission(User user, Channel context) {
		return context.isOp(user) || user.isModeSet(User.UMODE_OPER);
	}
	private boolean hasPermission(User user, User context) {
		return (user == context) || user.isModeSet(User.UMODE_OPER);
	}
	public String getName() {
		return "MODE";
	}
	public int getMinimumParameterCount() {
		return 1;
	}
}
