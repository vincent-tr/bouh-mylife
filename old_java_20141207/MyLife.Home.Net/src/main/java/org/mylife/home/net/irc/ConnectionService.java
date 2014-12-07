package org.mylife.home.net.irc;

/**
 * Gestion d'une connexion
 * @author trumpffv
 *
 */
public interface ConnectionService {

	/**
	 * Initialisation
	 * @param owner
	 */
	public void initialize(IRCNetConnection owner);
	
	/**
	 * Démarrage
	 */
	public void startService();
	
	/**
	 * Arrêt
	 */
	public void stopService();
}
