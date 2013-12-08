package org.mylife.home.net.hub.irc.commands;

import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.protocol.Message;
import org.mylife.home.net.hub.irc.protocol.Numerics;
import org.mylife.home.net.hub.irc.structure.Connectable;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.structure.User;

public class MapCommand implements Command {

	@Override
	public void invoke(IrcServer server, IrcConnection src, Message msg) {
		if(!CommandUtils.checkRegistered(server, src))
			return;
		Connectable structure = src.getStructure();
		if(!(structure instanceof User))
			return;
		
		User user = (User)structure;
		Network net = server.getNetwork();
		sendServer(server, user, "", net.getLocalServer());
		sendMapEndReply(server, user);
	}
	
	private void sendServer(IrcServer server, User user, String prefix, Server srv) {
		sendMapReply(server, user, prefix, srv);
		prefix += " ";
		for(User usr : srv.getUsers()) {
			sendMapReply(server, user, prefix, usr);
		}
		for(Server child : srv.getchildren()) {
			sendServer(server, user, prefix, child);
		}
	}
	
	private void sendMapReply(IrcServer server, User user, String prefix, Server target) {
		Message msg = Numerics.createMessage(server, Numerics.RPL_MAP, user);
		msg.appendLastParameter(prefix + "S:" + target.getName() + ":" + target.getToken());
		user.getConnection().send(msg);
	}
	
	private void sendMapReply(IrcServer server, User user, String prefix, User target) {
		Message msg = Numerics.createMessage(server, Numerics.RPL_MAP, user);
		msg.appendLastParameter(prefix + "U:" + target.getFullName());
		user.getConnection().send(msg);
	}
	
	private void sendMapEndReply(IrcServer server, User user) {
		Message msg = Numerics.createMessage(server, Numerics.RPL_MAPEND, user);
		user.getConnection().send(msg);
	}

	@Override
	public String getName() {
		return "MAP";
	}

}
