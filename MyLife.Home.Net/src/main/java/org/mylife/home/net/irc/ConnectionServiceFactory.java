package org.mylife.home.net.irc;

/**
 * Interface de création de <code>ConnectionService</code>
 * @author trumpffv
 *
 */
public interface ConnectionServiceFactory {

	/**
	 * Création
	 * @return
	 */
	public ConnectionService create(); 
}
