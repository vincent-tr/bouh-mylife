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

import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.MessageFactory;

/**
 * P10 Message factory.
 * @author markhale
 */
public class Message_P10Factory extends MessageFactory {
	public Message createMessage(String str) {
		// parse prefix
		int endPos = str.indexOf(' ');
		String from = str.substring(0, endPos);
		int startPos = endPos + 1;

		// parse command
		String command;
		endPos = str.indexOf(' ', startPos);
		if(endPos == -1) {
			// no parameters
			command = str.substring(startPos);
		} else {
			command = str.substring(startPos, endPos);
		}

		Message_P10 message = new Message_P10(from, command);

		if(endPos != -1) {
			// parse parameters
			int trailingPos = str.indexOf(" :", endPos);
			if(trailingPos == -1)
				trailingPos = str.length();
			while(endPos != -1 && endPos < trailingPos) {
				startPos = endPos + 1;
				endPos = str.indexOf(' ', startPos);
				if(endPos != -1)
					message.appendParameter(str.substring(startPos, endPos));
			}
			if(endPos == -1 && startPos < str.length()) { // ignore zero length parameters
				message.appendParameter(str.substring(startPos));
			} else if(trailingPos+2 < str.length()) { // ignore zero length parameters
				message.appendParameter(str.substring(trailingPos+2));
			}
		}
		return message;
	}
}
