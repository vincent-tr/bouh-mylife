package org.mylife.home.net.hub.irc.commands;

import org.apache.commons.lang3.StringUtils;
import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.protocol.ProtocolUtils;
import org.mylife.home.net.hub.irc.structure.AlreadyExistsException;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Unregistered;
import org.mylife.home.net.hub.irc.structure.User;

public class UserCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {

		Connectable structure = src.getStructure();

		if (!(structure instanceof Unregistered)) {
			CommandUtils.replyError(server, src, Numerics.ERR_ALREADYREGISTRED);
			return;
		}

		if (!CommandUtils.checkParametersCount(server, src, msg, 4))
			return;

		Unregistered unregistered = (Unregistered) structure;
		unregistered.setIdent(msg.getParameter(0));
		// String modeFlags = msg.getParameter(1);
		// String unused = msg.getParameter(2);
		unregistered.setRealName(msg.getParameter(3));

		unregisteredCheck(server, src);
	}

	@Override
	public String getName() {
		return "USER";
	}

	/**
	 * Prise en charge des messages USER/NICK depuis une entitée non enregistrée
	 * (donc client)
	 * 
	 * @param server
	 * @param src
	 * @param msg
	 */
	public static void unregisteredCheck(IrcServer server, IrcConnection src) {

		Unregistered unregistered = (Unregistered) src.getStructure();

		if (StringUtils.isEmpty(unregistered.getNick()))
			return;
		if (StringUtils.isEmpty(unregistered.getIdent()))
			return;

		Network net = server.getNetwork();

		String host = src.getRemoteHost();

		// on a tout, création de l'utilisateur
		User user = null;
		try {
			user = net.userAdd(net.getLocalServer(), unregistered.getNick(),
					unregistered.getIdent(), host, unregistered.getRealName());

		} catch (AlreadyExistsException e) {
			// Si le nick existe maintenant (ce qui veut dire qu'un autre avec
			// le même nick est venu entre le nick et le user de notre
			// utilisateur) alors error
			String reason = ProtocolUtils.getResourceString(Numerics.ERR_NICKNAMEINUSE
					.getName());
			CommandUtils.userError(server, src, reason);
			return;
		}

		user.setConnection(src);
		src.setStructure(user);
		
		// Propagation du nouvel utilisateur
		CommandUtils.dispatchNewUser(server, user, 0);
		
		// Message de bienvenue
		CommandUtils.userWelcome(server, user);
	}
}
