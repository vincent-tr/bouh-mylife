package org.mylife.home.net.hub.irc.tasks;

import org.mylife.home.net.hub.irc.IrcNetworkAccessHandler;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Network;

public class NetworkAccessTask extends WaitableTask {

	private final IrcServer server;
	private final IrcNetworkAccessHandler handler;

	public NetworkAccessTask(IrcServer server, IrcNetworkAccessHandler handler) {
		this.server = server;
		this.handler = handler;
	}

	@Override
	public void runTask() {
		Network net = server.getNetwork();
		handler.execute(net);
	}

}
