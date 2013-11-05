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

package org.mylife.home.net.hub.ircx;

import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Source;
import org.mylife.home.net.hub.irc.Unknown;

/**
 * @author markhale
 */
public final class Util {
	private Util() {}

	public static void sendIRCXReply(Source src) {
		String nick = "*";
		boolean isIRCX = false;
		if(src instanceof UserX) {
			UserX user = (UserX) src;
			nick = user.getNick();
			isIRCX = user.isIRCX();
		}
		Message msg = new Message(src.getServer(), Constants.IRCRPL_IRCX);
		msg.appendParameter(nick);
		msg.appendParameter(isIRCX ? "1" : "0");
		msg.appendParameter(jircd.ircx.Constants.VERSION);
		msg.appendParameter("ANON");
		msg.appendParameter(Integer.toString(jircd.irc.Constants.MAX_MESSAGE_SIZE));
		msg.appendParameter("*");
		src.send(msg);
	}
}
