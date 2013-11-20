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
	public void start();
	
	/**
	 * Arrêt
	 */
	public void stop();
	
	// ne pas oublier setDaemon
}
