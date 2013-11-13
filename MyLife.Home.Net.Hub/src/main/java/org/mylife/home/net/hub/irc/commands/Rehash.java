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

import java.io.IOException;
import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.irc.*;

/**
 * This REHASH command will reload all Command plugins from the plugins directory.
 * Plugins can thus be hot-swapped while the IRC server is running.
 * @author markhale
 */
public class Rehash implements Command {
	private final IrcServerMBean jircd;

	public Rehash(IrcServerMBean jircd) {
		this.jircd = jircd;
	}
	public void invoke(Source src, String[] params) {
		if(src instanceof User) {
			User user = (User) src;
			handleCommand(user, params);
		} else {
			Util.sendNotRegisteredError(src);
		}
	}
	private void handleCommand(User user, String[] params) {
		if(hasPermission(user)) {
			Message msg = new Message(Constants.RPL_REHASHING, user);
			//msg.appendParameter(jircd.getProperty("jircd.configFile"));
			msg.appendParameter("Rehashing");
			user.send(msg);
			jircd.reloadPolicy();
			try {
				jircd.reloadConfiguration();
			} catch(IOException ioe) {
				msg = new Message("ERROR", user);
				msg.appendParameter(ioe.toString());
				user.send(msg);
			}
		} else {
			Message msg = new Message(Constants.ERR_NOPRIVILEGES, user);
			msg.appendParameter("Permission Denied- You're not an IRC operator");
			user.send(msg);
		}
	}
	private boolean hasPermission(User user) {
		return user.isModeSet(User.UMODE_OPER);
	}
	public String getName() {
		return "REHASH";
	}
	public int getMinimumParameterCount() {
		return 0;
	}
}
