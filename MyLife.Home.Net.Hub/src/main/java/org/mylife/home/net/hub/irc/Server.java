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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * An IRC server.
 * 
 * @author thaveman
 * @author markhale
 */
public class Server extends Source {
	/** (String nickName, User user) */
	private final Map<String, User> users = Collections
			.synchronizedMap(new HashMap<String, User>());
	private final String name;
	private String description;
	protected final Server route;
	protected final Connection connection; // the connection used if it's linked
											// to me
	protected final Object client;
	protected final Network network;
	protected final int token;

	/**
	 * Constructs an IRC server linked to another IRC server.
	 */
	public Server(String name, int token, String description, Server route,
			Connection connection, Object client) {
		if (route == null)
			throw new NullPointerException("The route cannot be null");
		if (route.getNetwork() == null)
			throw new NullPointerException(
					"The route cannot have a null network");
		this.name = name;
		this.token = token;
		this.description = description;
		this.route = route;
		this.connection = connection;
		this.client = client;
		this.network = route.getNetwork();
	}

	/**
	 * Constructs an IRC server on an IRC network. This should only be used to
	 * construct the local server.
	 */
	public Server(String name, int token, String description, Network network) {
		if (network == null)
			throw new NullPointerException("The network cannot be null");
		this.name = name;
		this.token = token;
		this.description = description;
		this.route = this;
		this.connection = null;
		this.client = null;
		this.network = network;
	}

	/**
	 * Returns the server's name.
	 */
	public String getNick() {
		return getName();
	}

	/**
	 * Returns the server's name.
	 */
	public String getName() {
		return name;
	}

	/** ID */
	public String toString() {
		return getNick();
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	/**
	 * Returns the network this server is connected to. This method should never
	 * return null, to always ensure a reference to the network can be obtained
	 * with <code>Server.getNetwork()</code>.
	 */
	public final Network getNetwork() {
		return network;
	}

	public final Object getClient() {
		return client;
	}

	/**
	 * Returns the server this server is connected to.
	 */
	public final Server getServer() {
		return route;
	}

	public void setDescription(String desc) {
		description = desc;
	}

	public String getDescription() {
		return description;
	}

	public int getToken() {
		return token;
	}

	public void addUser(User user) {
		users.put(user.getNick().toLowerCase(), user);
	}

	public User getUser(String nick) {
		return (User) users.get(nick.toLowerCase());
	}

	public void removeUser(User usr, String reason) {
		String nick = usr.getNick().toLowerCase();
		if (users.containsKey(nick)) {
			// first remove the user from any channels he/she may be in
			for (Iterator<Channel> it = usr.getChannels().iterator(); it.hasNext();) {
				Channel channel = it.next();
				Message message = new Message(usr, "QUIT");
				message.appendLastParameter(reason);
				channel.send(message, usr);
				channel.removeUser(usr);
			}
			usr.getChannels().clear();
			users.remove(nick);
		}
	}

	public void changeUserNick(User user, String oldnick, String newnick) {
		synchronized (this.users) {
			users.put(newnick.toLowerCase(), user);
			users.remove(oldnick.toLowerCase());
			Message message = new Message(user, "NICK");
			message.appendParameter(newnick);
			for (User iusr : users.values()) {
				iusr.send(message);
			}
		}
	}

	public final int getUserCount(char mode, boolean isSet) {
		int count = 0;
		synchronized (users) {
			for (User user : users.values()) {
				if (user.isModeSet(mode) == isSet) {
					count++;
				}
			}
		}
		return count;
	}

	public void send(Message msg) {
		if (connection != null)
			connection.writeLine(msg.toString());
		else
			route.send(msg);
	}
}
