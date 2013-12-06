package org.mylife.home.net.hub.irc.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * Représente le réseau
 * 
 * @author pumbawoman
 * 
 */
public class Network {

	private final String name;
	
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

	public Network(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Channel getChannel(String name) {
		return channels.get(name.toLowerCase());
	}

	public Collection<Channel> getChannels() {
		return Collections.unmodifiableCollection(channels.values());
	}

	public User getUser(String nick) {
		for (User user : users) {
			if (user.getNick().equalsIgnoreCase(nick))
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

	public Server getLocalServer() {
		for (Server server : getServers()) {
			if (server.getParent() == null)
				return server;
		}

		return null;
	}

	public Collection<Server> getPeerServers() {
		return getLocalServer().getchildren();
	}

	/**
	 * Obtention de la liste des utilisateurs impactés par une modification de
	 * l'utilisateur spécifié
	 * 
	 * @param user
	 * @return
	 */
	public Collection<User> getLocalImpactedUsers(User user) {
		Collection<User> list = new ArrayList<User>();
		for(User item : getLocalServer().getUsers()) {
			// on n'ajoute pas l'utilisateur spécifié dans les retours
			if(item == user)
				continue;
			
			// on cherche si les 2 users ont au moins un chan commun
			boolean found = false;
			for(Channel userChan : user.getChannels()) {
				if(item.getChannels().contains(userChan)) {
					found = true;
					break;
				}
			}
			if(found)
				list.add(item);
		}
		return list;
	}
	
	public boolean isOn(User user, Channel channel) {
		return user.getChannel(channel.getName()) != null;
	}
	
	public boolean isLocal(User user) {
		return user.getServer().equals(getLocalServer());
	}

	/* --------------- gestion des modifications --------------- */

	private void checkArgumentNotNull(Object argument) {
		if (argument == null)
			throw new IllegalArgumentException();
	}

	private void checkArgumentNotNull(String argument) {
		if (StringUtils.isEmpty(argument))
			throw new IllegalArgumentException();
	}

	public Server serverAdd(String name, int token, Server parent)
			throws AlreadyExistsException {
		checkArgumentNotNull(name);

		if (getServer(name) != null)
			throw new AlreadyExistsException();
		if (getServer(token) != null)
			throw new AlreadyExistsException();

		Server server = new Server(name, token, parent);
		if (parent != null)
			parent.addChild(server);

		servers.put(name.toLowerCase(), server);
		serversByToken.put(token, server);
		
		return server;
	}

	public boolean serverRemove(Server server) {
		checkArgumentNotNull(server);

		if (!servers.containsKey(server.getName().toLowerCase()))
			return false;

		// suppression des utilisateurs du serveur
		Collection<User> serverUsers = new ArrayList<User>(server.getUsers());
		for (User user : serverUsers) {
			userRemove(user);
		}

		// suppression du serveur dans le parent
		Server parent = server.getParent();
		if (parent != null)
			parent.removeChild(server);

		// suppression de nos listes
		servers.remove(server.getName().toLowerCase());
		serversByToken.remove(server.getToken());

		return true;
	}

	public User userAdd(Server server, String nick, String ident, String host,
			String realName) throws AlreadyExistsException {

		checkArgumentNotNull(server);
		checkArgumentNotNull(nick);
		checkArgumentNotNull(ident);
		checkArgumentNotNull(host);
		checkArgumentNotNull(realName);

		if (getUser(nick) != null)
			throw new AlreadyExistsException();

		User user = new User(server, nick, ident, host, realName);
		server.addUser(user);
		users.add(user);
		
		return user;
	}

	public boolean userRemove(User user) {
		checkArgumentNotNull(user);

		if (!users.contains(user))
			return false;

		// Suppression des salons
		Collection<Channel> userChannels = new ArrayList<Channel>(
				user.getChannels());
		for (Channel channel : userChannels) {
			userPart(user, channel);
		}

		// Suppression de l'utilisateur sur le serveur
		Server server = user.getServer();
		server.removeUser(user);

		// Suppression de notre liste
		users.remove(user);
		return true;
	}

	public boolean userJoin(User user, String channelName) {
		checkArgumentNotNull(user);
		checkArgumentNotNull(channelName);

		// Création ou obtention du salon
		String lowerName = channelName.toLowerCase();
		Channel channel = channels.get(lowerName);
		if (channel == null)
			channel = new Channel(channelName);
		channels.put(lowerName, channel);

		// Check si déjà dessus
		if (user.getChannels().contains(channel))
			return false;

		// Ajout
		channel.addUser(user);
		user.addChannel(channel);
		return true;
	}

	public boolean userPart(User user, Channel channel) {
		checkArgumentNotNull(user);
		checkArgumentNotNull(channel);

		// Check
		if (!user.getChannels().contains(channel))
			return false;

		// Suppression
		user.removeChannel(channel);
		channel.removeUser(user);
		return true;
	}

	public void userChangeNick(User user, String newNick)
			throws AlreadyExistsException {
		checkArgumentNotNull(user);
		checkArgumentNotNull(newNick);

		// Check
		if (getUser(newNick) != null)
			throw new AlreadyExistsException();

		// Changement
		user.setNick(newNick);
	}
}
