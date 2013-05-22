package mylife.home.irc.server.commands;

import mylife.home.irc.message.Message;
import mylife.home.irc.server.IrcServer;
import mylife.home.irc.server.structure.Connection;

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
	 * @param connection
	 * @param message
	 */
	public void handle(Connection connection, Message message);
}
