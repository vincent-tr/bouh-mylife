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
	
	/**
	 * Suppression d'un serveur
	 * @param server
	 */
	public void deleteServer(Server server) {
		// suppression de tous les users du serveur
		for(User user : server.getUsers()) {
			deleteUser(user);
		}
		// suppression de la liste globale
		this.getServers().remove(server);
	}
	
	/**
	 * Suppression d'un utilisateur
	 * @param user
	 */
	public void deleteUser(User user) {
		// on s'enleve des salons
		// copie de la liste en local pour appel à userLeftChannel
		for(Channel channel : new ArrayList<Channel>(this.getChannels())) {
			userLeftChannel(user, channel);
		}
		// on s'enleve du serveur
		user.getServer().getUsers().remove(user);
		// on s'enleve de la liste globale
		this.getUsers().remove(user);
	}
	
	/**
	 * Un utilisateur quitte un salon
	 * Attention : cette méthode qui supprimer le salon s'il est vide
	 * @param user
	 * @param channel
	 * @return false si l'utilisateur n'était pas sur le salon, true s'il a quitté le salon
	 */
	public boolean userLeftChannel(User user, Channel channel) {
		Collection<User> chanUsers = channel.getUsers();
		if(!chanUsers.remove(user))
			return false;
		// si channel vide, on le supprime
		if(chanUsers.size() == 0)
			this.getChannels().remove(channel);
		return true;
	}
}
