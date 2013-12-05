package org.mylife.home.net.hub.irc.protocol;

import java.util.ResourceBundle;

public final class ProtocolUtils {

	private ProtocolUtils() {
	}

	public static byte[] concat(byte[] buffer1, byte[] buffer2) {
		byte[] ret = new byte[buffer1.length + buffer2.length];
		System.arraycopy(buffer1, 0, ret, 0, buffer1.length);
		System.arraycopy(buffer2, 0, ret, buffer1.length, buffer2.length);
		return ret;
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
		return ((c >= 'A' && c <= '~') || isDigit(c) || c == '-');
	}

	private static boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}

	public static boolean isNickName(String name) {
		final int len = name.length();
		// if (len > Constants.MAX_NICK_LENGTH)
		// return false;
		for (int i = 0; i < len; i++) {
			if (!isNickCharacter(name.charAt(i)))
				return false;
		}
		return true;
	}

	private static boolean isNickCharacter(char c) {
		return ((c >= 'A' && c <= '~') || isDigit(c) || c == '-');
	}

	public static boolean isChannelIdentifier(String name) {
		final int len = name.length();
		// if (len > Constants.MAX_CHANNEL_LENGTH)
		// return false;
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

	public static boolean isUserModeIdentifier(char c) {
		return (c == '@') || (c == '+');
	}

	public static boolean isCommandIdentifier(String s) {
		s = s.toUpperCase();
		char c = s.charAt(0);
		if (isDigit(c)) {
			return (s.length() == 3) && isDigit(s.charAt(1))
					&& isDigit(s.charAt(2));
		} else {
			if (c < 'A' || c > 'Z')
				return false;
			for (int i = 1; i < s.length(); i++) {
				c = s.charAt(i);
				if (c < 'A' || c > 'Z')
					return false;
			}
			return true;
		}
	}

	public static boolean isParameter(String s) {
		return (s.length() > 0) && (s.charAt(0) != ':')
				&& (s.indexOf(' ') == -1);
	}

	public static void copyParameters(Message from, Message to) {
		final int count = from.getParameterCount();
		if (count == 0)
			return;
		final int lastIndex = count - 1;
		for (int i = 0; i < lastIndex; i++) {
			to.appendParameter(from.getParameter(i));
		}
		if (from.hasLastParameter())
			to.appendLastParameter(from.getParameter(lastIndex));
		else
			to.appendParameter(from.getParameter(lastIndex));
	}

	public static String[] split(String str, char separator) {
		int startPos = 0;
		int endPos = str.indexOf(separator);
		if (endPos == -1) {
			return new String[] { str };
		} else {
			String[] splitList = new String[10];
			int count = 0;
			while (endPos != -1) {
				if (count + 1 == splitList.length) { // count+1 to leave room
														// for the last
														// substring
					String[] old = splitList;
					splitList = new String[2 * old.length];
					System.arraycopy(old, 0, splitList, 0, count);
				}
				splitList[count++] = str.substring(startPos, endPos);
				startPos = endPos + 1;
				endPos = str.indexOf(separator, startPos);
			}
			splitList[count++] = str.substring(startPos);
			// trim array
			String[] old = splitList;
			splitList = new String[count];
			System.arraycopy(old, 0, splitList, 0, count);
			return splitList;
		}
	}

	public static String getResourceString(String key) {
		String name = ProtocolUtils.class.getPackage().getName() + ".Bundle";
		ResourceBundle bundle = ResourceBundle.getBundle(name);
		if(!bundle.containsKey(key))
			return null;
		return bundle.getString(key);
	}
}
