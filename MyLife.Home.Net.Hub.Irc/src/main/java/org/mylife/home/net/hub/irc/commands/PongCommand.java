package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;

public class PongCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		String value = null;
		if(msg.getParameterCount() > 0)
			value = msg.getParameter(0);
		src.pong(value);
	}

	@Override
	public String getName() {
		return "PONG";
	}

}
