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

package org.mylife.home.net.hub.irc_p10;

import org.mylife.home.net.hub.jIRCd;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Network;
import org.mylife.home.net.hub.irc.Source;

/**
 * IRC P10 message format.
 * @author markhale
 */
public class Message_P10 extends Message {
	public Message_P10(String from, String command) {
		super(from, command);
	}
	public Message_P10(Server_P10 from, String command) {
		this(from != null ? from.toString() : null, command);
	}
	public Source resolveSender(Network network) {
		if(from != null)
			return Util.findSource(network, from);
		else
			return null;
	}
	public String toString() {
		StringBuffer buf = new StringBuffer();
		// append prefix
		buf.append(from).append(' ');

		// append command
		buf.append(command);

		// append parameters
		if(paramCount > 0) {
			final int lastParamIndex = paramCount - 1;
			for(int i=0; i<lastParamIndex; i++)
				buf.append(' ').append(params[i]);
			final String lastParam = params[lastParamIndex];
			// if the last parameter contains spaces or starts with a ':'
			if(hasLast || lastParam.indexOf(' ') != -1 || lastParam.charAt(0) == ':')
				buf.append(" :").append(lastParam);
			else
				buf.append(' ').append(lastParam);
		}
		return buf.toString();
	}
}
