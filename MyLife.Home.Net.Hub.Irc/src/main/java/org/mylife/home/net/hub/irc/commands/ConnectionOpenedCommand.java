package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Unregistered;

/**
 * Commande spéciale pour connexion ouverte
 * 
 * @author pumbawoman
 * 
 */
public class ConnectionOpenedCommand {

	/**
	 * Invocation de la commande
	 * 
	 * @param server
	 * @param src
	 */
	public void invoke(IrcServer server, IrcConnection src) {
		Unregistered structure = new Unregistered();
		src.setStructure(structure);
		structure.setConnection(src);

		if (src.getLocallyinitiated())
			CommandUtils.sendSelf(server, src);
	}

}
