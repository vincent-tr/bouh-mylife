package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.protocol.ProtocolUtils;
import org.mylife.home.net.hub.irc.structure.Channel;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.User;

public class JoinCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		if (!CommandUtils.checkRegistered(server, src))
			return;
		if (!CommandUtils.checkParametersCount(server, src, msg, 1))
			return;

		Connectable structure = src.getStructure();
		Network net = server.getNetwork();

		User source = null;
		if (structure instanceof User)
			source = (User) structure;
		else
			source = net.getUser(msg.getSender());

		String channelNames = msg.getParameter(0);
		String[] chanNameList = channelNames.split(",");

		for (String chanName : chanNameList) {
			if (!ProtocolUtils.isChannelIdentifier(chanName)) {
				CommandUtils
						.replyError(server, src, Numerics.ERR_NOSUCHCHANNEL);
				return;
			}
		}

		for (String chanName : chanNameList) {
			join(server, src, source, chanName);
		}
	}

	private void join(IrcServer server, IrcConnection src, User user,
			String channelName) {

		Network net = server.getNetwork();

		// Maj de la base
		net.userJoin(user, channelName);
		Channel channel = net.getChannel(channelName);

		// Dispatch apres maj de la base
		Message msg = new Message(user.getNick(), "JOIN");
		msg.appendParameter(channelName);
		CommandUtils.dispatchUserMessage(server, user, msg, src);

		// Envoi des messages à l'utilisateur arrivé
		welcome(server, src, user, channel);
	}

	private void welcome(IrcServer server, IrcConnection src, User user,
			Channel channel) {
		
		Network net = server.getNetwork();
		
		// On check si l'utilisateur est hebergé chez nous
		if(!net.isLocal(user))
			return;

/*
		If a JOIN is successful, the user receives a JOIN message as
		   confirmation and is then sent the channel's topic (using RPL_TOPIC) and
		   the list of users who are on the channel (using RPL_NAMREPLY), which
		   MUST include the user joining.
*/
		Message joinMessage = new Message(user.getNick(), "JOIN");
		joinMessage.appendParameter(channel.getName());
		src.send(joinMessage);
		
		Message topicMessage = Numerics.createMessage(server, Numerics.RPL_NOTOPIC, user);
		src.send(topicMessage);
		
		CommandUtils.sendNames(server, user, channel);
	}

	@Override
	public String getName() {
		return "JOIN";
	}

}
