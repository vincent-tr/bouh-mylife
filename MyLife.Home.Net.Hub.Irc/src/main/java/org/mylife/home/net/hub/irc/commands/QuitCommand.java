package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.structure.Unregistered;
import org.mylife.home.net.hub.irc.structure.User;

public class QuitCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		if (!CommandUtils.checkRegistered(server, src))
			return;
		
		Connectable structure = src.getStructure();

		if (structure instanceof Server) {
			serverInvoke(server, src, msg);
		} else if (structure instanceof User) {
			userInvoke(server, src, msg);
		}
	}

	private void serverInvoke(IrcServer server, IrcConnection src, Message msg) {
		Network net = server.getNetwork();
		
		String nick = msg.getSender();
		User user = net.getUser(nick); 
		
		// Dispatch
		CommandUtils.dispatchUserMessage(server, user, msg, src);
		
		// Maj base
		net.userRemove(user);
	}

	private void userInvoke(IrcServer server, IrcConnection src, Message msg) {
		Network net = server.getNetwork();
		
		User user = (User)src.getStructure(); 
		
		// Dispatch
		String reason = null;
		if(msg.getParameterCount() > 0)
			reason = msg.getParameter(0);
		Message dispatchMessage = new Message(user.getNick(), "QUIT");
		if(reason != null)
			dispatchMessage.appendLastParameter(reason);
		CommandUtils.dispatchUserMessage(server, user, dispatchMessage, src);
		
		// Déco
		src.setStructure(new Unregistered()); // empeche ConnectionClosedCommand de gérer
		src.close();
		
		// Maj base
		net.userRemove(user);
	}

	@Override
	public String getName() {
		return "QUIT";
	}

}
