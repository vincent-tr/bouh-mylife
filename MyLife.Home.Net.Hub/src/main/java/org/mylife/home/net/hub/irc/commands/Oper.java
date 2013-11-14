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
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Constants;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Operator;
import org.mylife.home.net.hub.irc.Source;
import org.mylife.home.net.hub.irc.User;

/**
 * @author markhale
 */
public class Oper implements Command {
	protected final IrcServerMBean jircd;

	public Oper(IrcServerMBean jircd) {
		this.jircd = jircd;
	}
	/**
	 * @param params [0] name, [1] password
	 */
	public void invoke(Source src, String[] params) {
		String name = params[0];
		String pass = params[1];
		for(Operator oper : jircd.getOperators()) {
			if (oper.isGood(name, pass, src.toString())) {
				((User)src).processModes("+o",true);
				Message message = new Message(Constants.RPL_YOUREOPER, src);
				message.appendParameter("You are now an IRC operator");
				src.send(message);
				return;
			}
		}
		Message message = new Message(Constants.ERR_NOOPERHOST, src);
		message.appendParameter("No O-lines for your host");
		src.send(message);
	}
	public String getName() {
		return "OPER";
	}
	public int getMinimumParameterCount() {
		return 2;
	}
}