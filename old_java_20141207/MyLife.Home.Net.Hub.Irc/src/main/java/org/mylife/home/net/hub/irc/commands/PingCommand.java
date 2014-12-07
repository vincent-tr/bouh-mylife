package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;

public class PingCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		
		String arg = null;
		if(msg.getParameterCount() > 0)
			arg = msg.getParameter(0);
		
		Message response = new Message("PONG");
		response.appendLastParameter(arg);
		src.send(response);
	}

	@Override
	public String getName() {
		return "PING";
	}

}
