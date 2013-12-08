package org.mylife.test;

import java.util.Arrays;
import java.util.Collection;

import org.mylife.home.net.hub.irc.IrcConfiguration;

public class TestConfig implements IrcConfiguration {

	private final String serverName;
	private final int listenPort;
	
	public TestConfig(String serverName, int listenPort) {
		this.serverName = serverName;
		this.listenPort = listenPort;
	}
	
	@Override
	public String getServerName() {
		return serverName;
	}

	@Override
	public String getNetworkName() {
		return "local.me";
	}

	@Override
	public int getServerToken() {
		return 0;
	}

	private Collection<Listener> listeners;
	
	@Override
	public Collection<Listener> getListeners() {
		if(listeners == null) {
			IrcConfiguration.Listener listener = new IrcConfiguration.Listener() {
				@Override
				public String getAddress() {
					return null;
				}
				@Override
				public int getPort() {
					return listenPort;
				}
			};
			listeners = Arrays.asList(listener);
		}
		return listeners;
	}

	public int getListenPort() {
		return listenPort;
	}

}
