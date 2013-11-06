package org.mylife.home.net.hub.configuration;

/**
 * Opérateur
 * @author TRUMPFFV
 *
 */
public class IrcOperator {

	private final String name;
	private final String host;
	private final String pass;
	
	public IrcOperator(String name, String host, String pass) {
		this.name = name;
		this.host = host;
		this.pass = pass;
	}

	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public String getPass() {
		return pass;
	}
	
	
}
