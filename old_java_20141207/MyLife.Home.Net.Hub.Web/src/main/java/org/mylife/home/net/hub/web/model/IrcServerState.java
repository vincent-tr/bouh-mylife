package org.mylife.home.net.hub.web.model;

import java.util.Collection;

import org.mylife.home.common.web.model.ServerState;

/**
 * Etat du serveur irc
 * 
 * @author pumbawoman
 * 
 */
public class IrcServerState extends ServerState {

	private boolean ircServer;
	private String ircServerName;
	private String ircNetworkName;
	private Collection<String> ircListeners;

	public boolean isIrcServer() {
		return ircServer;
	}

	public void setIrcServer(boolean ircServer) {
		this.ircServer = ircServer;
	}

	public String getIrcServerName() {
		return ircServerName;
	}

	public void setIrcServerName(String ircServerName) {
		this.ircServerName = ircServerName;
	}

	public String getIrcNetworkName() {
		return ircNetworkName;
	}

	public void setIrcNetworkName(String ircNetworkName) {
		this.ircNetworkName = ircNetworkName;
	}

	public Collection<String> getIrcListeners() {
		return ircListeners;
	}

	public void setIrcListeners(Collection<String> ircListeners) {
		this.ircListeners = ircListeners;
	}
}
