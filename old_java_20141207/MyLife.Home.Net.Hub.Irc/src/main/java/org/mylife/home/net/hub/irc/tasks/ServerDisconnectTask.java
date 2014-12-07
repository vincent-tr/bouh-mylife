package org.mylife.home.net.hub.irc.tasks;

import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;

public class ServerDisconnectTask extends WaitableTask {

	private final IrcServer server;
	private final String serverName;
	private boolean result;
	
	public ServerDisconnectTask(IrcServer server, String serverName) {
		this.server = server;
		this.serverName = serverName;
		this.result = false;
	}
	
	@Override
	public void runTask() {
		Network net = server.getNetwork();
		Server srv = net.getServer(serverName);
		if(srv == null)
			return;
		if(!net.getPeerServers().contains(srv))
			return;
		srv.getConnection().close();
		result = true;
	}

	public boolean getResult() {
		return result;
	}

}
