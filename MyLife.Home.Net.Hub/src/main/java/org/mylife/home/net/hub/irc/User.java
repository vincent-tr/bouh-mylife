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

import java.util.HashSet;
import java.util.Set;

/**
 * A user on a server.
 * @author thaveman
 * @author markhale
 */
public class User extends Source {
	public static final char UMODE_INVISIBLE = 'i';
	public static final char UMODE_SNOTICE   = 's';
	public static final char UMODE_WALLOPS   = 'w';
	public static final char UMODE_OPER      = 'o';
	public static final char UMODE_AWAY      = 'a';

	private String nickName;
	private long nickTimestamp;
	private final String ident;
	private final String displayHostName;
	private final String hostName;
	private final String description;
	private String awayMsg;
	protected Server server;
	protected final Client client; // used only for local users
	/** set of Channel */
	private final Set<Channel> channels = new HashSet<Channel>();
	private final Modes modes = new Modes();

	/**
	 * Constructor for remote users (i.e. attached via another server).
	 */
	public User(String nickname, String ident, String hostname, String description, Server server) {
		this(nickname, ident, hostname, description, server, null);
	}
	/**
	 * Constructs a user.
	 * @param client must not be null for local users
	 */
	public User(String nickname, String ident, String hostname, String description, Server server, Client client) {
		if(server == null)
			throw new NullPointerException("The server cannot be null");
		setNick(nickname);
		this.ident = ident;
		this.displayHostName = maskHostName(hostname);
		this.hostName = hostname;
		this.description = description;
		this.server = server;
		this.client = client;
	}
	protected void setNick(String nick) {
		nickName = nick;
		nickTimestamp = System.currentTimeMillis();
	}

	protected String maskHostName(String host) {
		// first see if it's an IP or name
		String[] dotnames = Util.split(host, '.');
		boolean isIP = (dotnames.length == 4);
		if(isIP) {
			for(int i=0; i<dotnames.length; i++) {
				try {
					Integer.parseInt(dotnames[i]);
				} catch (NumberFormatException e) {
					isIP = false;
					break;
				}
			}
		}

		String appendage = Integer.toHexString(Util.RANDOM.nextInt(0xEFFFFF) + 0x100000);
		String maskedHost;
		if (isIP) {
			// IP
			int p = host.lastIndexOf('.');
			maskedHost = host.substring(0, ++p) + appendage;
		} else if (dotnames.length > 1) {
			// dotted name
			int p = host.indexOf('.');
			maskedHost = appendage + host.substring(p);
		} else {
			// simple name
			maskedHost = appendage + ".host";
		}
		return maskedHost;
	}

	public void processModes(String modeString) {
		processModes(modeString,false);
	}
	public void processModes(String modeString, boolean isAllowed) {
		boolean addingMode = true; // are we adding modes (+) or subtracting (-)
		
		StringBuffer goodModes = new StringBuffer();
		
		for (int i = 0; i < modeString.length(); i++) {
			boolean doDo = false;

			char modeChar = modeString.charAt(i);
			switch(modeChar) {
			case '+':
				addingMode = true;
				goodModes.append('+');
				break;
			case '-':
				addingMode = false;
				goodModes.append('-');
				break;
				
			// add other processing here for modes that may not want to be
			// set under certain conditions, etc.
			case UMODE_OPER: // user can't set himself +o, the server must do it
				if (!isAllowed && addingMode) break;
				else doDo = true;
				break;
			case UMODE_AWAY: // user can't set himself +/-a, the server must do it
				if (!isAllowed) break;
				else doDo = true;
				break;
			case ':':
				break;
				
			default:
				doDo = true;
			}
			
			if (doDo) {
				try {
					if (addingMode)
						modes.add(modeChar);
					else
						modes.remove(modeChar);
					goodModes.append(modeChar);
				} catch(IllegalArgumentException e) {
					//Invalid Mode Character Detected!
					Util.sendUserModeUnknownFlagError(this);
				}
			}
		}
		
		if (goodModes.length() > 1) {
			Message message = new Message(server, "MODE", this);
			message.appendParameter(goodModes.toString());
			send(message);
		}
	}

	public boolean isModeSet(char mode) {
		return modes.contains(mode);
	}
	public String getModesList() {
		return modes.toString();
	}

	public void setAwayMessage(String msg) {
		awayMsg = msg;
		if(awayMsg != null)
			modes.add(UMODE_AWAY);
		else
			modes.remove(UMODE_AWAY);
	}
	public String getAwayMessage() {
		return awayMsg;
	}

	public synchronized Set<Channel> getChannels() {
		return channels;
	}

	/** @return Client */
	public final Object getClient() {
		return client;
	}
	/** ID */
	public String toString() {
		return getNick() + '!' + getIdent() + '@' + getDisplayHostName();
	}
	/**
	 * Returns the server this user is connected to.
	 */
	public final Server getServer() {
		return server;
	}

	public synchronized String getNick() {
		return nickName;
	}

	public synchronized long getNickTimestamp() {
		return nickTimestamp;
	}

	public String getDisplayHostName() {
		return displayHostName;
	}
	
	public String getHostName() {
		return hostName;
	}

	public String getIdent() {
		return ident;
	}

	public String getDescription() {
		return description;
	}

	public synchronized void changeNick(String newnick) {
		server.changeUserNick(this, nickName, newnick);
		setNick(newnick);
	}
	
	protected synchronized void addChannel(Channel chan) {
		channels.add(chan);
	}

	protected synchronized void removeChannel(Channel chan) {
		channels.remove(chan);
	}

	public void send(Message msg) {
		if (client != null)
			client.getConnection().writeLine(msg.toString());
		else
			server.send(msg);
	}
}
