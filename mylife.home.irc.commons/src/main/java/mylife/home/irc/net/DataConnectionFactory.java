package mylife.home.irc.net;

import java.nio.channels.SocketChannel;

/**
 * Fabrique de création de connexion de données pour listener et client
 * @author pumbawoman
 *
 */
public interface DataConnectionFactory {

	/**
	 * Création d'une connexion de données issue du channel
	 * @param channel
	 * @return
	 */
	public DataConnection createDataConnection(SocketChannel channel);
	
	/**
	 * Définition du gestionnaire de connexion associé
	 * @param connectionManager
	 */
	public void setConnectionManager(ConnectionManager connectionManager);
}
