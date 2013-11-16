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
	private Collection<String> ircBindings;
	private String ircOperators;

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

	public Collection<String> getIrcBindings() {
		return ircBindings;
	}

	public void setIrcBindings(Collection<String> ircBindings) {
		this.ircBindings = ircBindings;
	}

	public String getIrcOperators() {
		return ircOperators;
	}

	public void setIrcOperators(String ircOperators) {
		this.ircOperators = ircOperators;
	}

}
