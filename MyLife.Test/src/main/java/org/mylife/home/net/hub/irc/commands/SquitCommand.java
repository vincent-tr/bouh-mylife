package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.structure.User;

public class SquitCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		if(!CommandUtils.checkRegistered(server, src))
			return;
		
		Network net = server.getNetwork();
		Connectable structure = src.getStructure();
		if(structure instanceof User) {
			CommandUtils.replyError(server, src, Numerics.ERR_NOPRIVILEGES);
			return;
		}
		
		String lostServerName = msg.getParameter(0);
		String reason = msg.getParameter(1);
		Server lostServer = net.getServer(lostServerName);
		
		CommandUtils.sendNetSplit(server, src, lostServer, reason);
		
		net.serverRemove(lostServer);
	}

	@Override
	public String getName() {
		return "SQUIT";
	}

}
