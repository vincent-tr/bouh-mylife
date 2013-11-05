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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.mylife.home.net.hub.jIRCd;
import org.mylife.home.net.hub.jIRCdMBean;
import org.mylife.home.net.hub.irc.Channel;
import org.mylife.home.net.hub.irc.Network;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.Source;

/**
 * @author markhale
 */
public final class Util {
	public static final int SERVER_TOKEN_MASK = 4095;
	public static final int USER_TOKEN_MASK = 262143;

	private static char[] encodeLookup = new char[] {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
		'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
		'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '[', ']'
	};
	private static byte[] decodeLookup = new byte[128];
	private static Map commands = new HashMap();
	static {
		decodeLookup['A'] = 0; decodeLookup['B'] = 1; decodeLookup['C'] = 2; decodeLookup['D'] = 3;
		decodeLookup['E'] = 4; decodeLookup['F'] = 5; decodeLookup['G'] = 6; decodeLookup['H'] = 7;
		decodeLookup['I'] = 8; decodeLookup['J'] = 9; decodeLookup['K'] = 10; decodeLookup['L'] = 11;
		decodeLookup['M'] = 12; decodeLookup['N'] = 13; decodeLookup['O'] = 14; decodeLookup['P'] = 15;
		decodeLookup['Q'] = 16; decodeLookup['R'] = 17; decodeLookup['S'] = 18; decodeLookup['T'] = 19;
		decodeLookup['U'] = 20; decodeLookup['V'] = 21; decodeLookup['W'] = 22; decodeLookup['X'] = 23;
		decodeLookup['Y'] = 24; decodeLookup['Z'] = 25; decodeLookup['a'] = 26; decodeLookup['b'] = 27;
		decodeLookup['c'] = 28; decodeLookup['d'] = 29; decodeLookup['e'] = 30; decodeLookup['f'] = 31;
		decodeLookup['g'] = 32; decodeLookup['h'] = 33; decodeLookup['i'] = 34; decodeLookup['j'] = 35;
		decodeLookup['k'] = 36; decodeLookup['l'] = 37; decodeLookup['m'] = 38; decodeLookup['n'] = 39;
		decodeLookup['o'] = 40; decodeLookup['p'] = 41; decodeLookup['q'] = 42; decodeLookup['r'] = 43;
		decodeLookup['s'] = 44; decodeLookup['t'] = 45; decodeLookup['u'] = 46; decodeLookup['v'] = 47;
		decodeLookup['w'] = 48; decodeLookup['x'] = 49; decodeLookup['y'] = 50; decodeLookup['z'] = 51;
		decodeLookup['0'] = 52; decodeLookup['1'] = 53; decodeLookup['2'] = 54; decodeLookup['3'] = 55;
		decodeLookup['4'] = 56; decodeLookup['5'] = 57; decodeLookup['6'] = 58; decodeLookup['7'] = 59;
		decodeLookup['8'] = 60; decodeLookup['9'] = 61; decodeLookup['['] = 62; decodeLookup[']'] = 63;

		commands.put("AWAY", "A");
		commands.put("JOIN", "J");
		commands.put("INVITE", "I");
		commands.put("PART", "L");
		commands.put("MODE", "M");
		commands.put("NICK", "N");
		commands.put("NOTICE", "O");
		commands.put("TOPIC", "T");
		commands.put("QUIT", "Q");
		commands.put("PING", "G");
		commands.put("PONG", "Z");
		commands.put("PRIVMSG", "P");
		commands.put("WALLOPS", "WA");
	}

	private Util() {}

	public static int randomServerToken() {
		return (org.mylife.home.net.hub.irc.Util.RANDOM.nextInt() & SERVER_TOKEN_MASK);
	}
	public static int randomUserToken(Server server) {
		return (server.getToken() << 18) + (org.mylife.home.net.hub.irc.Util.RANDOM.nextInt() & USER_TOKEN_MASK);
	}
	public static int parseBase64(String s) {
		int value = 0;
		int factor = 1;
		final int len = s.length();
		for(int i=0; i<len; i++) {
			value += decodeLookup[s.charAt(len-1-i)]*factor;
			factor *= 64;
		}
		return value;
	}
	/**
	 * Returns a zero-padded ('A'-padded) base 64 string.
	 */
	public static String toBase64(long x, final int strLen) {
		if(strLen > 10)
			throw new IllegalArgumentException(strLen+" > 10");
		char[] b64 = new char[strLen];
		final int mask = 63;
		for(int i=0; i<strLen; i++) {
			b64[strLen-1-i] = encodeLookup[(int)(x) & mask];
			x >>>= 6;
		}
		return new String(b64);
	}

	public static Source findSource(Network network, String tokenB64) {
		return (Source) network.tokens.get(new Integer(parseBase64(tokenB64)));
	}

	public static int toIPAddress(String host) {
		try {
			InetAddress address = InetAddress.getByName(host);
			byte addrBytes[] = address.getAddress();
			return ((addrBytes[3]&0xFF)<<24) | ((addrBytes[2]&0xFF)<<16) | ((addrBytes[1]&0xFF)<<8) | (addrBytes[0]&0xFF);
		} catch(UnknownHostException uhe) {
			return 0;
		}
	}

	public static void sendNetSync(final Server_P10 thisServer, final Server_P10 server) {
		for(Iterator iter = thisServer.getUsers().iterator(); iter.hasNext(); ) {
			User_P10 user = (User_P10) iter.next();
			Message_P10 message = new Message_P10(thisServer, "N");
			message.appendParameter(user.getNick());
			message.appendParameter("1");
			message.appendParameter(Long.toString(user.getNickTimestamp()/1000));
			message.appendParameter(user.getIdent());
			message.appendParameter(user.getDisplayHostName());
			message.appendParameter(user.getModesList());
			message.appendParameter(Util.toBase64(toIPAddress(user.getHostName()), 6));
			message.appendParameter(Util.toBase64(user.getToken(), 5));
			message.appendParameter(user.getDescription());
			server.send(message);
		}
		for(Iterator iter = thisServer.getNetwork().channels.values().iterator(); iter.hasNext(); ) {
			Channel chan = (Channel) iter.next();
			Message_P10 message = new Message_P10(thisServer, "B");
			message.appendParameter(chan.getName());
			message.appendParameter(chan.getNamesList());
			server.send(message);
		}
		Message_P10 message = new Message_P10(thisServer, "EB");
		server.send(message);
	}

	/**
	 * Transcodes a message to a P10 message.
	 */
	public static Message transcode(Network network, Message msg) {
		if(msg instanceof Message_P10 || msg.getSender() == null)
			return msg;
		String fromB64;
		Source src = msg.resolveSender(network);
		if(src instanceof User_P10)
			fromB64 = toBase64(((User_P10)src).getToken(), 5);
		else
			fromB64 = toBase64(((Server_P10)src).getToken(), 2);
		String cmd = (String) commands.get(msg.getCommand());
		if(cmd == null)
			cmd = msg.getCommand();
		Message_P10 msgP10 = new Message_P10(fromB64, cmd);
		if(cmd.equals("P")) {
			String nick = msg.getParameter(0);
			User_P10 user = (User_P10) network.getUser(nick);
			msgP10.appendParameter(toBase64(user.getToken(), 5));
			msgP10.appendParameter(msg.getParameter(1));
		} else {
			for(int i=0; i<msg.getParameterCount(); i++)
				msgP10.appendParameter(msg.getParameter(i));
		}
		return msgP10;
	}
}
