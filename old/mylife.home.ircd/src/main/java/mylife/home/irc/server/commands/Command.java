package mylife.home.irc.server.commands;

import mylife.home.irc.server.IrcServer;

/**
 * Implémentation d'un commande
 * @author pumbawoman
 *
 */
public interface Command {

	/**
	 * Initialisation de la commande
	 * @param owner
	 */
	public void initialize(IrcServer owner);
	
	/**
	 * Fin d'utilisation de la commande
	 */
	public void terminate();
	
	/**
	 * Prise en charge de la commande
	 * @param ce
	 */
	public void handle(CommandExecution ce);
}
