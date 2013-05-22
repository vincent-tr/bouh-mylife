package mylife.home.irc.server.structure;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Représentation d'un réseau
 * @author pumbawoman
 *
 */
public class Network {

	/**
	 * Liste des serveurs présents sur le réseau
	 */
	private final Collection<Server> servers = new ArrayList<Server>();
	
	/**
	 * Liste de tous les utilisateurs du réseau
	 */
	private final Collection<User> users = new ArrayList<User>();
	
	/**
	 * Liste de tous les salons du réseau
	 */
	private final Collection<Channel> channels = new ArrayList<Channel>();

	/**
	 * Liste des serveurs présents sur le réseau
	 * @return
	 */
	public Collection<Server> getServers() {
		return servers;
	}

	/**
	 * Liste de tous les utilisateurs du réseau
	 * @return
	 */
	public Collection<User> getUsers() {
		return users;
	}

	/**
	 * Liste de tous les salons du réseau
	 * @return
	 */
	public Collection<Channel> getChannels() {
		return channels;
	}
	
	/**
	 * Recherche de salon
	 * @param name
	 * @return
	 */
	public Channel findChannel(String name) {
		for(Channel channel : channels) {
			if(channel.getName().equalsIgnoreCase(name))
				return channel;
		}
		return null;
	}
	
	/**
	 * Recherche d'utilisateur
	 * @param nick
	 * @return
	 */
	public User findUser(String nick) {
		for(User user : users) {
			if(user.getNick().equalsIgnoreCase(nick))
				return user;
		}
		return null;
	}
	
	/**
	 * Recherche de serveur
	 * @param name
	 * @return
	 */
	public Server findServer(String name) {
		for(Server server : servers) {
			if(server.getName().equalsIgnoreCase(name))
				return server;
		}
		return null;
	}
	
	/**
	 * Recherche de serveur
	 * @param token
	 * @return
	 */
	public Server findServer(int token) {
		for(Server server : servers) {
			if(server.getToken() == token)
				return server;
		}
		return null;
	}
	
	/**
	 * Recherche du serveur local
	 * @return
	 */
	public Server findLocalServer() {
		for(Server server : servers) {
			if(server.isSelf())
				return server;
		}
		// on doit toujours avoir un serveur local
		throw new UnsupportedOperationException();
	}
}
