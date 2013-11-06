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
	private final String password;
	private final int retryIntervalMs;
	
	public IrcLinkConnect(String name, String remoteAddress, int remotePort,
			String password, int retryIntervalMs) {
		this.name = name;
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
		this.password = password;
		this.retryIntervalMs = retryIntervalMs;
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

	public String getPassword() {
		return password;
	}

	public int getRetryIntervalMs() {
		return retryIntervalMs;
	}
	
}
