package org.mylife.home.net.hub.irc.tasks;

import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.User;

public class UserDisconnectTask extends WaitableTask {

	private final IrcServer server;
	private final String userNick;
	private boolean result;
	
	public UserDisconnectTask(IrcServer server, String userNick) {
		this.server = server;
		this.userNick = userNick;
		this.result = false;
	}
	
	@Override
	public void runTask() {
		Network net = server.getNetwork();
		User user = net.getUser(userNick);
		if(user == null)
			return;
		if(!net.isLocal(user))
			return;
		user.getConnection().close();
		result = true;
	}

	public boolean getResult() {
		return result;
	}
}
