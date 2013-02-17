package mylife.home.net.api;

/**
 * Listener de commandes
 * @author pumbawoman
 *
 */
public interface CommandListener {
	
	/**
	 * Ex�cution de la commande
	 * @param command
	 */
	public void execute(Command command);
	
}
