package mylife.home.net.api;

/**
 * Listener de commandes
 * @author pumbawoman
 *
 */
public interface CommandListener {
	
	/**
	 * Exécution de la commande
	 * @param command
	 */
	public void execute(Command command);
	
}
