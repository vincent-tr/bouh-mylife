package org.mylife.home.net.hub.irc.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.mylife.home.net.hub.irc.IrcConnection;

/**
 * Représente un serveur sur le réseau
 * 
 * @author pumbawoman
 * 
 */
public class Server implements Connectable {

	private final String name;
	private final int token;
	private final Server parent;

	private final Collection<Server> children = new ArrayList<Server>();
	private final Collection<User> users = new ArrayList<User>();

	private IrcConnection connection;

	/* internal */Server(String name, int token, Server parent) {
		this.name = name;
		this.token = token;
		this.parent = parent;
	}

	/* internal */void addChild(Server child) {
		children.add(child);
	}

	/* internal */void removeChild(Server child) {
		children.remove(child);
	}

	/* internal */void addUser(User user) {
		users.add(user);
	}

	/* internal */void removeUser(User user) {
		users.remove(user);
	}

	public String getName() {
		return name;
	}

	public int getToken() {
		return token;
	}

	/**
	 * Server par lequel passer pour atteindre le serveur Null pour notre
	 * serveur
	 */
	public Server getParent() {
		return parent;
	}

	public Collection<Server> getchildren() {
		return Collections.unmodifiableCollection(children);
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

	@Override
	public IrcConnection getConnection() {
		return connection;
	}

	@Override
	public void setConnection(IrcConnection connection) {
		this.connection = connection;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Server:" + name;
	}
}
