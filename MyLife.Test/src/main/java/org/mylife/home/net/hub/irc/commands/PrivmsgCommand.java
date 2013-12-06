package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;

public class PrivmsgCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		CommandUtils.routeMessage(server, src, msg, false);
	}

	@Override
	public String getName() {
		return "PRIVMSG";
	}

}
