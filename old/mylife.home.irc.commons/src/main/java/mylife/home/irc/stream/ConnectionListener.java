package mylife.home.irc.stream;

/**
 * Listener de connexions
 * @author pumbawoman
 *
 */
public interface ConnectionListener {

	/**
	 * Nouvelle connexion
	 * @param connection
	 */
	public void newConnection(Stream connection);
}
