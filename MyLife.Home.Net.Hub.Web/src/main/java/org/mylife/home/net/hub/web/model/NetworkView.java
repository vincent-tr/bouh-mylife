package org.mylife.home.net.hub.web.model;

import java.util.Collection;

import org.mylife.home.net.hub.irc.IrcServer;

public class NetworkView {

	private final ServerView localServer;
	private final Collection<ChannelView> channels;
	
	public NetworkView(ServerView localServer, Collection<ChannelView> channels) {
		this.localServer = localServer;
		this.channels = channels;
	}

	public ServerView getLocalServer() {
		return localServer;
	}

	public Collection<ChannelView> getChannels() {
		return channels;
	}

	public static NetworkView getView(IrcServer server) {
		
	}
}
