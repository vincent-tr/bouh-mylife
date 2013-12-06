package org.mylife.home.net.hub.irc.commands;

import java.util.ArrayList;
import java.util.List;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.structure.Channel;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.User;

public class PartCommand implements Command {

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
		String reason = null;
		if (msg.getParameterCount() > 1)
			reason = msg.getParameter(1);

		List<Channel> channels = new ArrayList<Channel>();
		String[] chanNameList = channelNames.split(",");
		for (String chanName : chanNameList) {
			Channel channel = source.getChannel(chanName);
			if (channel == null) {
				CommandUtils
						.replyError(server, src, Numerics.ERR_NOSUCHCHANNEL);
				return;
			}
			channels.add(channel);
		}

		for (Channel channel : channels) {
			part(server, src, source, channel, reason);
		}
	}

	private void part(IrcServer server, IrcConnection src, User user,
			Channel channel, String reason) {
		Network net = server.getNetwork();

		// Propagation avant maj de la base
		Message msg = new Message(user.getNick(), "PART");
		msg.appendParameter(channel.getName());
		if (reason != null)
			msg.appendLastParameter(reason);

		CommandUtils.dispatchUserMessage(server, user, msg, src);
		
		// Si user local on lui envoie aussi le msg
		if (user.getServer() == net.getLocalServer())
			src.send(new Message(user.getNick(), "PART").appendParameter(channel.getName()));

		// Maj de la base
		net.userPart(user, channel);
	}

	@Override
	public String getName() {
		return "PART";
	}

}
