package org.mylife.home.net.hub.irc;

import org.mylife.home.net.hub.irc.structure.Network;

/**
 * Accès au réseau depuis l'extérieur
 * 
 * @author pumbawoman
 * 
 */
public interface IrcNetworkAccessHandler {

	/**
	 * Accès, depuis le thread du serveur
	 * 
	 * @param net
	 */
	void execute(Network net);

}
