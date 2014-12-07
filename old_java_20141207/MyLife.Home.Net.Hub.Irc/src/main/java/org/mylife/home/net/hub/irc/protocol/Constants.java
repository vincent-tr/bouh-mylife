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

package org.mylife.home.net.hub.irc.protocol;

/**
 * @author markhale
 */
public final class Constants {
	private Constants() {
	}

	public static final int DEFAULT_PORT = 6667;
	//public static final int DEFAULT_SSL_PORT = 994;
	public static final String CHARSET = "UTF8";
	//public static final int MAX_MESSAGE_PARAMETERS = 15;
	/** Maximum message length including CR-LF. */
	//public static final int MAX_MESSAGE_SIZE = 512;
	/** Maximum message length excluding CR-LF. */
	//public static final int MAX_MESSAGE_LENGTH = MAX_MESSAGE_SIZE - 2;
	/** Use \r\n for maximum compatibility */
	public static final String MESSAGE_TERMINATOR = "\r\n";
	//public static final int MAX_NICK_LENGTH = 50;
	//public static final int MAX_CHANNEL_LENGTH = 50;
	/** Maximum channel topic length */
	//public static final int MAX_TOPIC_LENGTH = 400;

	//public static final long SECS_TO_MILLIS = 1000L;

	//public static final String LINK_VERSION = "021020000";
	//public static final String LINK_FLAGS = "IRC|";

	//public static final char CTCP_DELIMITER = 1;

}
