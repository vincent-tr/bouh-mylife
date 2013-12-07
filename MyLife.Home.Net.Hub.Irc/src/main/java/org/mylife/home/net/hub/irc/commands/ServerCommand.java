package org.mylife.home.net.hub.irc.commands;

import java.util.logging.Logger;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.protocol.ProtocolUtils;
import org.mylife.home.net.hub.irc.structure.AlreadyExistsException;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.structure.Unregistered;
import org.mylife.home.net.hub.irc.structure.User;

public class ServerCommand implements Command {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(ServerCommand.class
			.getName());

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		Connectable structure = src.getStructure();

		if (structure instanceof Server) {
			serverInvoke(server, src, msg);
		} else if (structure instanceof User) {
			CommandUtils.replyError(server, src, Numerics.ERR_NOPRIVILEGES);
		} else if (structure instanceof Unregistered) {
			unregisteredInvoke(server, src, msg);
		}
	}

	private void serverInvoke(IrcServer server, IrcConnection src, Message msg) {
		// Enregistrement et publication d'un nouveau serveur uniquement
		Network net = server.getNetwork();

		// On trouve à quel serveur le nouveau serveur est connecté
		Server source = null;
		if (msg.getSender() == null)
			source = (Server) src.getStructure();
		else
			source = net.getServer(msg.getSender());

		Server newServer = createServer(server, source, msg);
		if (newServer == null) {
			log.severe("New server from '" + source.getName()
					+ "' already exists !");
			return;
		}

		publishServer(server, newServer, src);
	}

	private void unregisteredInvoke(IrcServer server, IrcConnection src,
			Message msg) {

		// Enregistrement d'un nouveau serveur directement lié à nous : Création
		// du nouveau serveur puis net sync
		Network net = server.getNetwork();
		Server newServer = createServer(server, net.getLocalServer(), msg);
		if (newServer == null) {
			// Server déjà existant
			Message errorMessage = new Message("ERROR");
			errorMessage
					.appendLastParameter(ProtocolUtils
							.getResourceString(Numerics.ERR_ALREADYREGISTRED
									.getName()));
			src.send(errorMessage);
			src.close();
			return;
		}
		src.setStructure(newServer);
		
		publishServer(server, newServer, src);
		
		// Si connexion depuis nous alors nous avons déjà été publié à la connexion 
		boolean publishSelf = !src.getLocallyinitiated();
		CommandUtils.sendNetSync(server, src, publishSelf);
	}

	private void publishServer(IrcServer server, Server newServer,
			IrcConnection... excluded) {
		Message serverMessage = new Message(newServer.getParent().getName(),
				"SERVER");
		serverMessage.appendParameter(newServer.getName());
		serverMessage.appendParameter("0");
		serverMessage.appendParameter("" + newServer.getToken());
		serverMessage.appendLastParameter(newServer.getName()); // description
		CommandUtils.dispatchServerMessage(server, serverMessage, excluded);
	}

	private Server createServer(IrcServer server, Server parent, Message msg) {

		String name = msg.getParameter(0);
		// String hopcount = msg.getParameter(1);
		int token = Integer.parseInt(msg.getParameter(2));
		// String info = msg.getParameter(3);

		try {
			return server.getNetwork().serverAdd(name, token, parent);
		} catch (AlreadyExistsException ex) {
			return null;
		}
	}

	@Override
	public String getName() {
		return "SERVER";
	}

}
