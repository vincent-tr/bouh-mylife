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
public class Pass implements Command {
	public final void invoke(Source src, String[] params) {
		if(src instanceof Unknown) {
			handleCommand((Unknown)src, params);
		} else {
			Message message = new Message(Constants.ERR_ALREADYREGISTRED, src);
			message.appendParameter("Unauthorized command (already registered)");
			src.send(message);
		}
	}
	protected void handleCommand(Unknown src, String[] params) {
		src.setPassword(params[0]);
	}
	public String getName() {
		return "PASS";
	}
	public int getMinimumParameterCount() {
		return 1;
	}
}
