package org.mylife.home.net.hub.configuration;

/**
 * Configuration d'un binding d'adresse port
 * @author TRUMPFFV
 *
 */
public class IrcBinding {

	private final String address;
	private final int port;
	private final boolean ssl;
	
	public IrcBinding(String address, int port, boolean ssl) {
		this.address = address;
		this.port = port;
		this.ssl = ssl;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public boolean isSsl() {
		return ssl;
	}
	
}
