package org.mylife.home.net.hub.configuration;

/**
 * Configuration d'un lien en connexion (le serveur courant se connecte au serveur distant)
 * @author TRUMPFFV
 *
 */
public class IrcLinkConnect {

	private final String name;
	private final String remoteAddress;
	private final int remotePort;
	
	public IrcLinkConnect(String name, String remoteAddress, int remotePort) {
		this.name = name;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
	}

	public String getName() {
		return name;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public int getRemotePort() {
		return remotePort;
	}
}
