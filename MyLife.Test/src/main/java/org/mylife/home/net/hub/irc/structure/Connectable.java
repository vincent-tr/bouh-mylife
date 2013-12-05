package org.mylife.home.net.hub.irc.structure;

import org.mylife.home.net.hub.irc.IrcConnection;

/**
 * Interface pour un élément de structure pouvant être connecté
 * @author pumbawoman
 *
 */
public interface Connectable {

	/**
	 * Connexion, peut retourner null si l'élément n'est pas directement connecté
	 * @return
	 */
	IrcConnection getConnection();
	
	/**
	 * Définition de la connexion de l'élément
	 * @param connection
	 */
	void setConnection(IrcConnection connection);
}
