package org.mylife.home.net.hub.irc;

import java.util.Collection;

public interface IrcConfiguration {

	/**
	 * Configuration d'un listener
	 * 
	 * @author pumbawoman
	 * 
	 */
	public interface Listener {

		/**
		 * Adresse d'écoute, ou null si toutes les adresses
		 * 
		 * @return
		 */
		String getAddress();

		/**
		 * Port d'écoute
		 * 
		 * @return
		 */
		int getPort();
	}

	/**
	 * Nom du serveur (sans nom de réseau), ou null si nom de la machine
	 * 
	 * @return
	 */
	String getServerName();

	/**
	 * Nom du réseau
	 * 
	 * @return
	 */
	String getNetworkName();

	/**
	 * Token, ou 0 si auto
	 * 
	 * @return
	 */
	int getServerToken();

	/**
	 * Listeners à configurer
	 * 
	 * @return
	 */
	Collection<Listener> getListeners();
}
