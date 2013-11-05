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

import java.util.*;

/**
 * An IRC network.
 * @author markhale
 */
public class Network {
	private String name;

	/** Servers on this network.
	 * (String host, Server server) */
	public final Map servers = Collections.synchronizedMap(new HashMap());

	/** (Integer token, Server server) */
	public final Map tokens = Collections.synchronizedMap(new HashMap());

	/** All channels on this network.
	 * (String name, Channel channel) */
	public final Map channels = Collections.synchronizedMap(new HashMap());

	public Network(String name) {
		setName(name);
	}
	public final void setName(String name) {
		this.name = name;
	}
	public final String getName() {
		return name;
	}

	/**
	 * Adds a server to this network.
	 */
	public void addServer(Server server) {
		servers.put(server.getNick().toLowerCase(), server);
		tokens.put(new Integer(server.getToken()), server);
	}
	public Server getServer(String nick) {
		return (Server) servers.get(nick.toLowerCase());
	}
	public void removeServer(Server ic) {
		servers.remove(ic.getNick().toLowerCase());
		tokens.remove(new Integer(ic.getToken()));
	}

	/**
	 * Adds a channel to this network.
	 */
	public void addChannel(Channel channel) {
		channels.put(channel.getName().toLowerCase(), channel);
	}
	public Channel getChannel(String name) {
		return (Channel) channels.get(name.toLowerCase());
	}
	public void removeChannel(Channel channel) {
		channels.remove(channel.getName().toLowerCase());
	}

	/**
	 * Gets a user on this network.
	 * @return null if nick does not exist on the network.
	 */
	public User getUser(String nick) {
		synchronized(servers) {
		for(Iterator iter = servers.values().iterator(); iter.hasNext();) {
			Server server = (Server) iter.next();
			User user = server.getUser(nick);
			if(user != null)
				return user;
		}
		}
		return null;
	}

	public final int getUserCount(char mode, boolean isSet) {
		int count = 0;
		synchronized(servers) {
		for(Iterator iter = servers.values().iterator(); iter.hasNext();) {
			Server server = (Server) iter.next();
			count += server.getUserCount(mode, isSet);
		}
		}
		return count;
	}

	public String toString() {
		return name;
	}
}
