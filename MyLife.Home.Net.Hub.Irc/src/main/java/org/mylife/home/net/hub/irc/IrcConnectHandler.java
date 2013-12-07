package org.mylife.home.net.hub.irc;

import java.io.IOException;

public interface IrcConnectHandler {

	/**
	 * Appelé sur le thread du serveur
	 */
	public void connected();

	/**
	 * Appelé sur le thread du serveur
	 * 
	 * @param e
	 */
	public void connectionFailed(IOException e);

}
