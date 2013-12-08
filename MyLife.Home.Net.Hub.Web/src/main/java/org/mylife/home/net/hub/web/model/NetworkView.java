package org.mylife.home.net.hub.web.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.mylife.home.net.hub.irc.IrcNetworkAccessHandler;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Channel;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.structure.User;

public class NetworkView {

	private final String name;
	private final ServerView localServer;
	private final Collection<ChannelView> channels;

	public NetworkView(String name, ServerView localServer,
			Collection<ChannelView> channels) {
		this.name = name;
		this.localServer = localServer;
		this.channels = channels;
	}

	public String getName() {
		return name;
	}

	public ServerView getLocalServer() {
		return localServer;
	}

	public Collection<ChannelView> getChannels() {
		return channels;
	}

	public static NetworkView getView(IrcServer server) {
		if (server == null)
			return null;
		AccessHandler handler = new AccessHandler(server);
		try {
			server.requestNetworkAccess(handler);
		} catch (InterruptedException e) {
			return null;
		}
		return handler.getView();
	}

	private static class AccessHandler implements IrcNetworkAccessHandler {

		private final IrcServer server;
		private NetworkView view;

		public AccessHandler(IrcServer server) {
			this.server = server;
		}

		public NetworkView getView() {
			return view;
		}

		@Override
		public void execute(Network net) {
			view = mapNetwork(net);
		}

		private NetworkView mapNetwork(Network source) {
			ServerView localServer = mapServer(source.getLocalServer());

			Collection<ChannelView> channels = new ArrayList<ChannelView>();
			for (Channel sourceChannel : source.getChannels()) {
				ChannelView channel = mapChannel(sourceChannel);
				channels.add(channel);
			}

			return new NetworkView(source.getName(), localServer,
					Collections.unmodifiableCollection(channels));
		}

		private ChannelView mapChannel(Channel source) {
			Collection<String> users = new ArrayList<String>();
			for (User userSource : source.getUsers()) {
				users.add(userSource.getFullName());
			}
			return new ChannelView(source.getName(), users);
		}

		private ServerView mapServer(Server source) {
			Collection<ServerView> children = new ArrayList<ServerView>();
			for (Server childSource : source.getchildren()) {
				ServerView child = mapServer(childSource);
				children.add(child);
			}

			Collection<UserView> users = new ArrayList<UserView>();
			for (User userSource : source.getUsers()) {
				UserView user = mapUser(userSource);
				users.add(user);
			}

			return new ServerView(server.getName(),
					Collections.unmodifiableCollection(users),
					Collections.unmodifiableCollection(children));
		}

		private UserView mapUser(User source) {
			Collection<String> channels = new ArrayList<String>();
			for (Channel channelSource : source.getChannels()) {
				channels.add(channelSource.getName());
			}
			return new UserView(source.getFullName(), channels);
		}
	}
}
