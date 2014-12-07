package org.mylife.home.net.hub.irc;

import java.io.IOException;

public interface IrcConnectHandler {

	/**
	 * Appelé sur le thread du serveur
	 * 
	 * @param connection
	 */
	public void connected(IrcConnection connection);

	/**
	 * Appelé sur le thread du serveur
	 * 
	 * @param connection
	 * @param e
	 */
	public void connectionFailed(IrcConnection connection, IOException e);

}
