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
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class Restart implements Command {
	private final IrcServerMBean jircd;

	public Restart(IrcServerMBean jircd) {
		this.jircd = jircd;
	}

	public void invoke(RegisteredEntity src, String[] params) {
		if (src instanceof User) {
			User user = (User) src;
			invoke(user, params);
		}
	}

	private void invoke(User user, String[] params) {
		try {
			Util.checkOperatorPermission(user);
			jircd.stop();
			jircd.reloadPolicy();
			try {
				jircd.reloadConfiguration();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			jircd.start();
		} catch (SecurityException se) {
			Util.sendNoPrivilegesError(user);
		}
	}

	public String getName() {
		return "RESTART";
	}

	public int getMinimumParameterCount() {
		return 0;
	}
}
