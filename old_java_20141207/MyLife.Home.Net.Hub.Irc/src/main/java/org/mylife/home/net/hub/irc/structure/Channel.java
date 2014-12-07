package org.mylife.home.net.hub.irc.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Représente un salon sur le réseau
 * 
 * @author pumbawoman
 * 
 */
public class Channel {

	private final String name;

	private final Collection<User> users = new ArrayList<User>();

	/* internal */Channel(String name) {
		this.name = name;
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
	public String toString() {
		return "Channel:" + name;
	}

}
