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

package org.mylife.home.net.hub.irc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.AccessController;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.mylife.home.net.hub.IrcServer;

/**
 * @author thaveman
 * @author markhale
 */
public final class Util {
	/** Common secure random number generator. */
	public static final SecureRandom RANDOM = new SecureRandom();

	private Util() {
	}

	public static void checkCommandPermission(Command cmd) {
		if (System.getSecurityManager() != null)
			AccessController.checkPermission(new CommandPermission(cmd
					.getName()));
	}

	public static void checkCTCPPermission(String dataType, String action) {
		if (System.getSecurityManager() != null)
			AccessController.checkPermission(new CTCPPermission(dataType,
					action));
	}

	public static boolean isIRCString(String str) {
		final int len = str.length();
		for (int i = 0; i < len; i++) {
			if (!isIRCCharacter(str.charAt(i)))
				return false;
		}
		return true;
	}

	private static boolean isIRCCharacter(char c) {
		return ((c >= 'A' && c <= '~') || (c >= '0' && c <= '9') || c == '-');
	}

	public static boolean isNickName(String name) {
		final int len = name.length();
		if (len > Constants.MAX_NICK_LENGTH)
			return false;
		for (int i = 0; i < len; i++) {
			if (!isNickCharacter(name.charAt(i)))
				return false;
		}
		return true;
	}

	private static boolean isNickCharacter(char c) {
		return ((c >= 'A' && c <= '~') || (c >= '0' && c <= '9') || c == '-');
	}

	public static boolean isChannelIdentifier(String name) {
		final int len = name.length();
		if (len > Constants.MAX_CHANNEL_LENGTH)
			return false;
		if (!isChannelIdentifierStart(name.charAt(0)))
			return false;
		for (int i = 1; i < len; i++)
			if (!isChannelIdentifierPart(name.charAt(i)))
				return false;
		return true;
	}

	private static boolean isChannelIdentifierStart(char c) {
		return (c == '#' || c == '&' || c == '+' || c == '!');
	}

	private static boolean isChannelIdentifierPart(char c) {
		return (c != ' ' && c != ',' && c != '\r' && c != '\n');
	}

	public static String[] split(String str, char separator) {
		List<String> splitList = new ArrayList<String>();
		int startPos = 0;
		int endPos = str.indexOf(separator);
		while (endPos != -1) {
			splitList.add(str.substring(startPos, endPos));
			startPos = endPos + 1;
			endPos = str.indexOf(separator, startPos);
		}
		splitList.add(str.substring(startPos));
		return (String[]) splitList.toArray(new String[splitList.size()]);
	}

	public static String join(String[] strs, char separator, int offset) {
		StringBuffer buf = new StringBuffer();
		if (offset < strs.length)
			buf.append(strs[offset]);
		for (int i = offset + 1; i < strs.length; i++) {
			buf.append(separator).append(strs[i]);
		}
		return buf.toString();
	}

	public static void sendNetSync(final Server thisServer, final Server server) {
		for (User user : thisServer.getUsers()) {
			Message message = new Message("NICK");
			message.appendParameter(user.getNick());
			message.appendParameter("1");
			message.appendParameter(user.getIdent());
			message.appendParameter(user.getHostName());
			message.appendParameter(Integer.toString(thisServer.getToken()));
			message.appendParameter(user.getModesList());
			message.appendParameter(user.getDescription());
			server.send(message);
		}
		for (Channel chan : thisServer.getNetwork().channels.values()) {
			Message message = new Message(thisServer, "NJOIN");
			message.appendParameter(chan.getName());
			message.appendParameter(chan.getNamesList());
			server.send(message);
		}
	}

	public static String getResourceString(Source src, String key) {
		return ResourceBundle.getBundle(
				IrcServer.class.getPackage().getName() + ".Bundle",
				src.getLocale()).getString(key);
	}

	public static void sendNoSuchNickError(Source src, String nick) {
		Message message = new Message(Constants.ERR_NOSUCHNICK, src);
		message.appendParameter(nick);
		message.appendLastParameter(getResourceString(src, "ERR_NOSUCHNICK"));
		src.send(message);
	}

	public static void sendNoSuchChannelError(Source src, String channel) {
		Message message = new Message(Constants.ERR_NOSUCHCHANNEL, src);
		message.appendParameter(channel);
		message.appendLastParameter(getResourceString(src, "ERR_NOSUCHCHANNEL"));
		src.send(message);
	}

	public static void sendNotOnChannelError(Source src, String channel) {
		Message message = new Message(Constants.ERR_NOTONCHANNEL, src);
		message.appendParameter(channel);
		message.appendLastParameter(getResourceString(src, "ERR_NOTONCHANNEL"));
		src.send(message);
	}

	public static void sendCannotSendToChannelError(Source src, String channel) {
		Message message = new Message(Constants.ERR_CANNOTSENDTOCHAN, src);
		message.appendParameter(channel);
		message.appendLastParameter(getResourceString(src,
				"ERR_CANNOTSENDTOCHAN"));
		src.send(message);
	}

	public static void sendUserNotInChannelError(Source src, String nick,
			String channel) {
		Message message = new Message(Constants.ERR_USERNOTINCHANNEL, src);
		message.appendParameter(nick);
		message.appendParameter(channel);
		message.appendLastParameter(getResourceString(src,
				"ERR_USERNOTINCHANNEL"));
		src.send(message);
	}

	public static void sendUnknownCommandError(Source src, String cmdName) {
		Message message = new Message(Constants.ERR_UNKNOWNCOMMAND, src);
		message.appendParameter(cmdName);
		message.appendLastParameter(getResourceString(src, "ERR_UNKNOWNCOMMAND"));
		src.send(message);
	}

	public static void sendNeedMoreParamsError(Source src, String cmdName) {
		Message message = new Message(Constants.ERR_NEEDMOREPARAMS, src);
		message.appendParameter(cmdName);
		message.appendLastParameter(getResourceString(src, "ERR_NEEDMOREPARAMS"));
		src.send(message);
	}

	public static void sendNoPrivilegesError(Source src) {
		Message message = new Message(Constants.ERR_NOPRIVILEGES, src);
		message.appendLastParameter(getResourceString(src, "ERR_NOPRIVILEGES"));
		src.send(message);
	}

	public static void sendUserModeUnknownFlagError(Source src) {
		Message message = new Message(Constants.ERR_UMODEUNKNOWNFLAG, src);
		message.appendLastParameter(getResourceString(src,
				"ERR_UMODEUNKNOWNFLAG"));
		src.send(message);
	}

	public static void sendNotRegisteredError(Source src) {
		Message message = new Message(Constants.ERR_NOTREGISTERED, src);
		message.appendLastParameter(getResourceString(src, "ERR_NOTREGISTERED"));
		src.send(message);
	}

	public static boolean match(String pattern, String text) {
		return matchWildcard(pattern, text);
	}

	private static boolean matchWildcard(String pattern, String text) {
		int patSize = pattern.length() - 1;
		int texSize = text.length() - 1;
		int patIndex = 0;
		int texIndex = 0;

		while (true) {
			if (patIndex > patSize)
				return (texIndex > texSize);

			if (pattern.charAt(patIndex) == '*') {
				patIndex++;

				if (patIndex > patSize)
					return true;

				while (pattern.charAt(patIndex) == '*')
					patIndex++;

				while (patIndex <= patSize && pattern.charAt(patIndex) == '?'
						&& texIndex <= texSize) {
					texIndex++;
					patIndex++;
				}

				if (patIndex > patSize)
					return false;

				if (pattern.charAt(patIndex) == '*')
					continue;

				while (texIndex <= texSize) {
					if (matchWildcard(pattern.substring(patIndex),
							text.substring(texIndex)))
						return true;
					else if (texIndex == texSize)
						return false;
					texIndex++;
				}
			}// end if
			if (texIndex > texSize)
				return true;
			if (patIndex <= patSize
					&& pattern.charAt(patIndex) != '?'
					&& Character.toUpperCase(pattern.charAt(patIndex)) != Character
							.toUpperCase(text.charAt(texIndex)))
				return false;
			texIndex++;
			patIndex++;
		}
	}

	public static String[] loadTextReader(Reader reader, int maxLines)
			throws IOException {
		String[] tmpLines = new String[maxLines];
		int n;
		BufferedReader bf = new BufferedReader(reader);
		try {
			String line = bf.readLine();
			for (n = 0; line != null && n < tmpLines.length; n++) {
				tmpLines[n] = line;
				line = bf.readLine();
			}
		} finally {
			bf.close();
		}

		String[] lines = new String[n];
		System.arraycopy(tmpLines, 0, lines, 0, n);
		return lines;
	}

	public static String[] loadTextFile(String filename, int maxLines)
			throws IOException {
		return loadTextReader(new FileReader(filename), maxLines);
	}

	public static String[] loadTextString(String content, int maxLines)
			throws IOException {
		return loadTextReader(new StringReader(content), maxLines);
	}

	public static Locale lookupLocale(String ip) {
		return Locale.getDefault();
	}
}
