package org.mylife.home.net.hub.configuration;

/**
 * Configuration d'un binding d'adresse port
 * @author TRUMPFFV
 *
 */
public class IrcBinding {

	private final String adress;
	private final int port;
	private final boolean ssl;
	
	public IrcBinding(String adress, int port, boolean ssl) {
		this.adress = adress;
		this.port = port;
		this.ssl = ssl;
	}

	public String getAdress() {
		return adress;
	}

	public int getPort() {
		return port;
	}

	public boolean isSsl() {
		return ssl;
	}
	
}
