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

import java.text.DecimalFormat;

import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Constants;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Operator;
import org.mylife.home.net.hub.irc.Source;
import org.mylife.home.net.hub.irc.Util;

/**
 * @author markhale
 */
public class Stats implements Command {
	private static final DecimalFormat TWO_PLACES = new DecimalFormat("00");
	private final IrcServerMBean jircd;

	public Stats(IrcServerMBean jircd) {
		this.jircd = jircd;
	}
	public void invoke(Source src, String[] params) {
		String query = params[0];
		switch(query.charAt(0)) {
			case 'o':
				sendOperators(src);
				break;
			case 'u':
				sendUptime(src);
				break;
		}
		Message message = new Message(Constants.RPL_ENDOFSTATS, src);
		message.appendParameter(query);
		message.appendParameter(Util.getResourceString(src, "RPL_ENDOFSTATS"));
		src.send(message);
	}
	private void sendOperators(Source src) {
		for(Operator oper : jircd.getOperators()) {
			Message message = new Message(Constants.RPL_STATSOLINE, src);
			message.appendParameter("O");
			message.appendParameter(oper.getHost());
			message.appendParameter("*");
			message.appendParameter(oper.getName());
			src.send(message);
		}
	}
	private void sendUptime(Source src) {
		int uptimeSecs = (int) (jircd.getUptimeMillis()/Constants.SECS_TO_MILLIS);
		int days = uptimeSecs/(24*60*60);
		int hours = uptimeSecs/(60*60) - 24*days;
		int mins = uptimeSecs/60 - 60*(hours + 24*days);
		int secs = uptimeSecs - 60*(mins + 60*(hours + 24*days));
		Message message = new Message(Constants.RPL_STATSUPTIME, src);
		message.appendParameter("Server Up "+days+" days "+hours+':'+TWO_PLACES.format(mins)+':'+TWO_PLACES.format(secs));
		src.send(message);
	}
	public String getName() {
		return "STATS";
	}
	public int getMinimumParameterCount() {
		return 1;
	}
}