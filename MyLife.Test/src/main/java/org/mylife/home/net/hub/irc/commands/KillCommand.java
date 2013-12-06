package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Unregistered;
import org.mylife.home.net.hub.irc.structure.User;

public class KillCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		if (!CommandUtils.checkServer(server, src))
			return;
		
		Network net = server.getNetwork();
		
		String nick = msg.getParameter(0);
		String reason = msg.getParameter(1);
		
		User user = net.getUser(nick);
		if(user == null)
			return; // déjà inexistant ?! 

		// Propagation
		CommandUtils.dispatchServerMessage(server, msg, src);
		
		// Si utilisateur local, déco
		disconnectLocal(net, user, reason);
		
		// Suppression de la base
		net.userRemove(user);
	}

	@Override
	public String getName() {
		return "KILL";
	}

	private void disconnectLocal(Network net, User user, String reason) {

		if (user == null)
			return;

		// On check si l'utilisateur est hebergé chez nous
		if(!net.isLocal(user))
			return;

		// Utilisateur local, déco
		Message errorMessage = new Message("ERROR");
		errorMessage.appendLastParameter(reason);
		user.getConnection().send(errorMessage);
		user.getConnection().setStructure(new Unregistered()); // Empeche la gestion de ConnectionClosedCommand
		user.getConnection().close();
	}

}
