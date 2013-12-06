package org.mylife.home.net.hub.irc.commands;

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

public class NickCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {

		Connectable structure = src.getStructure();

		if (structure instanceof Server) {
			serverInvoke(server, src, msg);
		} else if (structure instanceof User) {
			userInvoke(server, src, msg);
		} else if (structure instanceof Unregistered) {
			unregisteredInvoke(server, src, msg);
		}
	}

	@Override
	public String getName() {
		return "NICK";
	}

	private void userInvoke(IrcServer server, IrcConnection src, Message msg) {

		if (!CommandUtils.checkParametersCount(server, src, msg, 1))
			return;

		String newNick = msg.getParameter(0);

		if (!ProtocolUtils.isNickName(newNick)) {
			CommandUtils.replyError(server, src, Numerics.ERR_ERRONEUSNICKNAME);
			return;
		}

		Network net = server.getNetwork();
		User user = (User) src.getStructure();
		String oldNick = user.getNick();

		try {
			net.userChangeNick(user, newNick);
		} catch (AlreadyExistsException e) {
			CommandUtils.replyError(server, src, Numerics.ERR_NICKNAMEINUSE);
			return;
		}

		// Propagation
		dispatchNickChange(server, user, oldNick, newNick, src);
	}

	private void serverInvoke(IrcServer server, IrcConnection src, Message msg) {

		Network net = server.getNetwork();

		// On doit trier entre nick change et new user
		if (msg.getParameterCount() == 1) {

			// Nick change
			String oldNick = msg.getSender();
			String newNick = msg.getParameter(0);
			User user = net.getUser(oldNick);

			try {
				net.userChangeNick(user, newNick);
			} catch (AlreadyExistsException e) {
				
				handleNickCollision(server, oldNick, newNick);
				return;
			}

			// Propagation
			dispatchNickChange(server, user, oldNick, newNick, src);

		} else {

			// New user
			if (!CommandUtils.checkParametersCount(server, src, msg, 7))
				return;

			String nick = msg.getParameter(0);
			int hopcount =  Integer.parseInt(msg.getParameter(1));
			String ident = msg.getParameter(2);
			String host = msg.getParameter(3);
			int serverToken = Integer.parseInt(msg.getParameter(4));
			// String umode = msg.getParameter(5);
			String realName = msg.getParameter(6);

			Server userServer = net.getServer(serverToken);
			if (userServer == null) {
				CommandUtils.replyError(server, src, Numerics.ERR_NOSUCHSERVER);
				return;
			}

			User user = null;
			try {
				user = net.userAdd(userServer, nick, ident, host, realName);
			} catch (AlreadyExistsException e) {
				
				handleNickCollision(server, nick, null); // pas de 2ieme nick
				return;
			}
			
			// Propagation
			CommandUtils.dispatchNewUser(server, user, hopcount + 1, src);
		}
	}

	private void unregisteredInvoke(IrcServer server, IrcConnection src,
			Message msg) {
		Unregistered unregistered = (Unregistered) src.getStructure();

		if (!CommandUtils.checkParametersCount(server, src, msg, 1))
			return;

		String nick = msg.getParameter(0);

		if (!ProtocolUtils.isNickName(nick)) {
			CommandUtils.replyError(server, src, Numerics.ERR_ERRONEUSNICKNAME);
			return;
		}

		if (!checkNickFree(server, nick)) {
			CommandUtils.replyError(server, src, Numerics.ERR_NICKNAMEINUSE);
			return;
		}

		unregistered.setNick(nick);

		UserCommand.unregisteredCheck(server, src);
	}

	private boolean checkNickFree(IrcServer server, String nick) {
		return server.getNetwork().getUser(nick) == null;
	}

	private void dispatchNickChange(IrcServer server, User user,
			String oldNick, String newNick, IrcConnection... excluded) {

		Message newNickMsg = new Message(oldNick, "NICK");
		newNickMsg.appendParameter(newNick);
		CommandUtils.dispatchUserMessage(server, user, newNickMsg, excluded);
	}

	private final static String NICK_COLLISION_REASON = "Nick collision";

	private void handleNickCollision(IrcServer server, String nick1, String nick2) {
		
		Network net = server.getNetwork();
		User user1 = null;
		User user2 = null;
		
		// On essaye de récupérer les users s'il existent dans la base
		if(nick1 != null)
			user1 = net.getUser(nick1);
		if(nick2 != null)
			user2 = net.getUser(nick2);
		
		// Dispatch des nicks collisions
		if(nick1 != null)
			dispatchNickCollision(server, user1, nick1);
		if(nick2 != null)
			dispatchNickCollision(server, user2, nick2);

		// Si les utilisateurs sont locaux, déco 
		disconnectLocalNickCollision(net, user1);
		disconnectLocalNickCollision(net, user2);

		// maj de la base
		if(user1 != null)
			server.getNetwork().userRemove(user1);
		if(user2 != null)
			server.getNetwork().userRemove(user2);
	}

	private void disconnectLocalNickCollision(Network net, User user) {

		if (user == null)
			return;

		// On check si l'utilisateur est hebergé chez nous
		if (user.getServer() != net.getLocalServer())
			return;

		// Utilisateur local, déco
		Message errorMessage = new Message("ERROR");
		errorMessage.appendLastParameter(NICK_COLLISION_REASON);
		user.getConnection().send(errorMessage);
		user.getConnection().setStructure(new Unregistered()); // Empeche la gestion de ConnectionClosedCommand
		user.getConnection().close();
	}

	private void dispatchNickCollision(IrcServer server, User user, String nick) {

		Message msg = new Message("KILL");
		msg.appendParameter(nick);
		msg.appendLastParameter(NICK_COLLISION_REASON);
		if(user != null)
			CommandUtils.dispatchUserMessage(server, user, msg);
		else
			CommandUtils.dispatchServerMessage(server, msg);
	}
}
