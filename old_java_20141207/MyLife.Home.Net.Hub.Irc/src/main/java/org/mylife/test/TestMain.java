package org.mylife.test;

import java.io.IOException;
import java.util.Collection;

import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Server;
import org.mylife.home.net.hub.irc.tasks.WaitableTask;

public class TestMain {

	public static void main(String[] args) throws IOException {
		IrcServer server1 = TestTools.createServer(1);
		IrcServer server2 = TestTools.createServer(2);
		TestTools.sleep(10000);
		TestTools.connect(server2, server1);
		TestTools.sleep(30000);
		server2.execute(new SplitTask(server2));
		TestTools.sleep();
	}

	private static class SplitTask extends WaitableTask {

		private final IrcServer server;

		public SplitTask(IrcServer server) {
			this.server = server;
		}

		@Override
		public void runTask() {
			Collection<Server> peers = server.getNetwork().getPeerServers();
			for(Server peer : peers) {
				// split
				peer.getConnection().close();
				break;
			}
		}

	}
}
