package org.mylife.home.net.hub.irc.structure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.mylife.home.net.hub.irc.IrcConnection;

/**
 * Représente un utilisateur sur le réseau
 * 
 * @author pumbawoman
 * 
 */
public class User implements Connectable {

	private final Server server;
	private String nick;
	private final String ident;
	private final String host;
	private final String realName;

	private IrcConnection connection;

	/**
	 * Key : channel name lower case
	 */
	private final Map<String, Channel> channels = new HashMap<String, Channel>();

	/* internal */User(Server server, String nick, String ident, String host,
			String realName) {
		this.server = server;
		this.nick = nick;
		this.ident = ident;
		this.host = host;
		this.realName = realName;
	}

	public Server getServer() {
		return server;
	}

	public String getNick() {
		return nick;
	}

	/* internal */void setNick(String nick) {
		this.nick = nick;
	}

	public String getIdent() {
		return ident;
	}

	public String getHost() {
		return host;
	}

	public String getRealName() {
		return realName;
	}

	public String getFullName() {
		return nick + "!" + ident + "@" + host;
	}
	
	/* internal */void addChannel(Channel channel) {
		channels.put(channel.getName().toLowerCase(), channel);
	}

	/* internal */void removeChannel(Channel channel) {
		channels.remove(channel.getName().toLowerCase());
	}

	public Channel getChannel(String name) {
		return channels.get(name.toLowerCase());
	}

	public Collection<Channel> getChannels() {
		return Collections.unmodifiableCollection(channels.values());
	}

	@Override
	public IrcConnection getConnection() {
		return connection;
	}

	@Override
	public void setConnection(IrcConnection connection) {
		this.connection = connection;
	}
}
