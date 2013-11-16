package mylife.home.irc.server.commands;

import mylife.home.irc.server.IrcServer;

/**
 * Implémentation de base d'une commande
 * @author pumbawoman
 *
 */
public abstract class BaseCommand implements Command {

	/**
	 * Serveur qui possède la commande
	 */
	private IrcServer owner;
	
	/**
	 * Serveur qui possède la commande
	 * @return
	 */
	protected IrcServer getOwner() {
		return owner;
	}
	
	/**
	 * Initialisation de la commande
	 * @param owner
	 */
	@Override
	public void initialize(IrcServer owner) {
		this.owner = owner;
	}

	@Override
	public void terminate() {
	}

}
