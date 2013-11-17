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
import org.mylife.home.net.hub.irc.Constants;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class Motd implements Command {
	private String[] motd;

	public Motd(IrcServerMBean jircd) {
		try {
			motd = Util.loadTextString(jircd.getConfiguration()
					.getServerMotdContent(), 500);
		} catch (IOException ioe) {
			motd = new String[0];
		}
	}

	public void invoke(RegisteredEntity src, String[] params) {
		Message msg = new Message(Constants.RPL_MOTDSTART, src);
		msg.appendLastParameter("- " + src.getServer().getName()
				+ " Message of the Day -");
		src.send(msg);
		for (int i = 0; i < motd.length; i++) {
			msg = new Message(Constants.RPL_MOTD, src);
			msg.appendLastParameter("- " + motd[i]);
			src.send(msg);
		}
		msg = new Message(Constants.RPL_ENDOFMOTD, src);
		msg.appendLastParameter("End of /MOTD command.");
		src.send(msg);
	}

	public String getName() {
		return "MOTD";
	}

	public int getMinimumParameterCount() {
		return 0;
	}
}
