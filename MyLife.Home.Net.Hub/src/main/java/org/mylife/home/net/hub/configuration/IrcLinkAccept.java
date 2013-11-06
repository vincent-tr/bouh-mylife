package org.mylife.home.net.hub.configuration;

/**
 * Configuration d'un lien en acceptation (le serveur distant vient se connecter)
 * @author TRUMPFFV
 *
 */
public class IrcLinkAccept {

	private final String name;
	private final String remoteAddress;
	private final int localPort;
	private final String password;
	
	public IrcLinkAccept(String name, String remoteAddress, int localPort,
			String password) {
		this.name = name;
		this.remoteAddress = remoteAddress;
		this.localPort = localPort;
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public String getRemoteAddress() {
		return remoteAddress;
	}
	public int getLocalPort() {
		return localPort;
	}
	public String getPassword() {
		return password;
	}
	
	
}
