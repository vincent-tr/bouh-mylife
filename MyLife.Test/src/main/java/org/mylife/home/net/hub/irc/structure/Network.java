package org.mylife.home.net.hub.irc.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Représente le réseau
 * 
 * @author pumbawoman
 * 
 */
public class Network {

	/**
	 * Key : channel name lower case
	 */
	private final Map<String, Channel> channels = new HashMap<String, Channel>();
	
	private final Collection<User> users = new ArrayList<User>();
	
	/**
	 * Key : server lower case
	 */
	private final Map<String, Server> servers = new HashMap<String, Server>();
	private final Map<Integer, Server> serversByToken = new HashMap<Integer, Server>();
	
	public Channel getChannel(String name) {
		return channels.get(name.toLowerCase());
	}
	
	public Collection<Channel> getChannels() {
		return Collections.unmodifiableCollection(channels.values());
	}
	
	public User getUser(String nick) {
		for(User user : users) {
			if(user.getNick().equalsIgnoreCase(nick))
				return user;
		}
		return null;
	}
	
	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users);
	}
	
	public Server getServer(String name) {
		return servers.get(name.toLowerCase());
	}
	
	public Server getServer(int token) {
		return serversByToken.get(token);
	}
	
	public Collection<Server> getServers() {
		return Collections.unmodifiableCollection(servers.values());
	}
	
	// TODO : opérations
}
