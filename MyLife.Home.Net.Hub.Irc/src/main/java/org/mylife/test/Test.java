package org.mylife.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.mylife.home.net.hub.irc.IrcConfiguration;
import org.mylife.home.net.hub.irc.IrcServer;

public class Test {

	private static class Config implements IrcConfiguration {

		private static class CListener implements IrcConfiguration.Listener {

			private final String address;
			private final int port;

			public CListener(String address, int port) {
				this.address = address;
				this.port = port;
			}

			@Override
			public String getAddress() {
				return address;
			}

			@Override
			public int getPort() {
				return port;
			}

		}

		private final Collection<IrcConfiguration.Listener> listeners;

		public Config() {
			listeners = new ArrayList<IrcConfiguration.Listener>();
			listeners.add(new CListener(null, 6667));
		}

		@Override
		public String getServerName() {
			return null;
		}

		@Override
		public String getNetworkName() {
			return "mti-team2.dyndns.org";
		}

		@Override
		public int getServerToken() {
			return 0;
		}

		@Override
		public Collection<Listener> getListeners() {
			return listeners;
		}

	}

	public static void main(String[] args) throws IOException {
		IrcServer server = new IrcServer(new Config());
		server.start();
		sleep();
	}

	private static void sleep() {
		try {
			Object o = new Object();
			synchronized (o) {
				o.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
