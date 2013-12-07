package org.mylife.home.net.hub.irc.structure;

import org.mylife.home.net.hub.irc.IrcConnection;

public class Unregistered implements Connectable {

	private IrcConnection connection;

	@Override
	public IrcConnection getConnection() {
		return connection;
	}

	@Override
	public void setConnection(IrcConnection connection) {
		this.connection = connection;
	}
	
	private String nick;
	private String ident;
	private String realName;

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getIdent() {
		return ident;
	}

	public void setIdent(String ident) {
		this.ident = ident;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}
}
