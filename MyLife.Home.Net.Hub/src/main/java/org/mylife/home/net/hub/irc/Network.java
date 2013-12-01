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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An IRC network.
 * 
 * @author markhale
 */
public final class Network implements Entity {
	private String name;

	/**
	 * Servers on this network. (String name, Server server)
	 */
	private final Map<String, Server> servers = new ConcurrentHashMap<String, Server>();

	/** (Integer token, Server server) */
	private final Map<Integer, Server> serverTokens = new ConcurrentHashMap<Integer, Server>();
	
	private Server thisServer;

	/**
	 * All channels on this network. (String name, Channel channel)
	 */
	private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

	public Network(String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}

	public Collection<Server> getServers() {
		return Collections.unmodifiableCollection(servers.values());
	}

	/**
	 * Adds a server to this network.
	 */
	void addServer(Server server) {
		servers.put(server.getName().toLowerCase(), server);
		serverTokens.put(new Integer(server.getToken()), server);
		if(server.isThis())
			thisServer = server;
	}

	public Server getServer(String name) {
		return (Server) servers.get(name.toLowerCase());
	}

	public Server getServer(int token) {
		return (Server) serverTokens.get(new Integer(token));
	}
	
	public Server getThisServer() {
		return thisServer;
	}
	
	/**
	 * Obtention de tous les serveurs connectés derrière un serveur
	 * @param server
	 * @return
	 */
	public Collection<Server> getServersFrom(Server server) {
		Collection<Server> servers = new ArrayList<Server>();
		for(Server item : getServers()) {
			if(server.isFrom(item))
				servers.add(item);
		}
		return servers;
	}
	
	public Collection<Server> getServersPeer() {
		Collection<Server> servers = new ArrayList<Server>();
		for(Server item : getServers()) {
			if(item.isPeer())
				servers.add(item);
		}
		return servers;
	}

	void removeServer(Server server) {
		servers.remove(server.getName().toLowerCase());
		serverTokens.remove(new Integer(server.getToken()));
		if(server.isThis())
			thisServer = null;
	}

	public Collection<Channel> getChannels() {
		return Collections.unmodifiableCollection(channels.values());
	}

	/**
	 * Adds a channel to this network.
	 */
	void addChannel(Channel channel) {
		channels.put(channel.getName().toLowerCase(), channel);
	}

	public Channel getChannel(String name) {
		return (Channel) channels.get(name.toLowerCase());
	}

	void removeChannel(Channel channel) {
		channels.remove(channel.getName().toLowerCase());
	}

	/**
	 * Gets a user on this network.
	 * 
	 * @return null if nick does not exist on the network.
	 */
	public User getUser(String nick) {
		for (Iterator<Server> iter = servers.values().iterator(); iter
				.hasNext();) {
			Server server = iter.next();
			User user = server.getUser(nick);
			if (user != null)
				return user;
		}
		return null;
	}

	public int getUserCount(char mode, boolean isSet) {
		int count = 0;
		for (Iterator<Server> iter = servers.values().iterator(); iter
				.hasNext();) {
			Server server = iter.next();
			count += server.getUserCount(mode, isSet);
		}
		return count;
	}

	public void send(Message message, Server... excluded) {
		Set<ConnectedEntity> excludedPeer = new HashSet<ConnectedEntity>();
		for(Server excludedServer : excluded) {
			if(excludedServer == null)
				continue;
			Connection.Handler handler = excludedServer.getHandler();
			if(handler == null)
				continue;
			excludedPeer.add(handler.getEntity());
		}
		
		for (Iterator<Server> iter = servers.values().iterator(); iter
				.hasNext();) {
			Server server = iter.next();
			if (server.isPeer() && !excludedPeer.contains(server)) {
				// send to local servers for forwarding
				server.send(message);
			}
		}
	}

	/**
	 * Sends a message to all the servers on this network.
	 */
	public void send(Message message) {
		send(message, new Server[0]);
	}

	public String toString() {
		return name;
	}
}
