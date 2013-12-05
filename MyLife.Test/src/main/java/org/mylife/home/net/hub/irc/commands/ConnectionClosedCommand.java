package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.structure.Unregistered;
import org.mylife.home.net.hub.irc.structure.User;

/**
 * Commande spéciale pour connexion fermée
 * 
 * @author pumbawoman
 * 
 */
public class ConnectionClosedCommand {

	/**
	 * Invocation de la commande
	 * 
	 * @param server
	 * @param src
	 */
	public void invoke(IrcServer server, IrcConnection src) {
		Connectable structure = src.getStructure();

		// Déjà géré, rien à faire
		if (structure == null)
			return;

		if (structure instanceof Unregistered) {
			// rien à faire
		} else if (structure instanceof Server) {
			serverLeft(server, (Server) structure);
		} else if (structure instanceof User) {
			userLeft(server, (User) structure);
		}
	}

	private void userLeft(IrcServer server, User structure) {
		
		Network net = server.getNetwork();
		net.userRemove(structure);
		
		// TODO
	}

	private void serverLeft(IrcServer server, Server structure) {
		// TODO
	}
}
