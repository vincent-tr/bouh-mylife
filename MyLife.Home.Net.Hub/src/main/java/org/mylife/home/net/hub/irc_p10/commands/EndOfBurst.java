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

package org.mylife.home.net.hub.irc_p10.commands;

import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Source;
import org.mylife.home.net.hub.irc_p10.Message_P10;
import org.mylife.home.net.hub.irc_p10.Server_P10;

/**
 * @author markhale
 */
public class EndOfBurst implements Command {
	public void invoke(Source src, String[] params) {
		if (src instanceof Server_P10) {
			Message_P10 msg = new Message_P10((Server_P10) src.getServer(), "EA");
			src.send(msg);
		} else {
			throw new IllegalArgumentException("Source must be a P10 server, it was "+src);
		}
	}
	public String getName() {
		return "EB";
	}
	public int getMinimumParameterCount() {
		return 0;
	}
}
