package org.mylife.test;

import java.io.IOException;
import java.util.logging.Logger;

import org.mylife.home.net.hub.irc.IrcConnectHandler;
import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcServer;

public final class TestTools {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(TestTools.class
			.getName());
	
	private TestTools() {
	}
	
	public static IrcServer createServer(int serverNum) {
		return createServer("server" + serverNum, 6000 + serverNum);
	}
	
	public static IrcServer createServer(String serverName, int listenPort) {
		TestConfig conf = new TestConfig(serverName, listenPort);
		IrcServer server = new IrcServer(conf);
		server.start();
		return server;
	}
	
	public static void connect(final IrcServer from, final IrcServer to) {
		TestConfig confTo = (TestConfig)to.getConfig();
		try {
			from.connect("localhost", confTo.getListenPort(), new IrcConnectHandler() {

				@Override
				public void connected(IrcConnection connection) {
					log.info("Connection from '" + from.getServerName() + "' to '" + to.getServerName() + "' success");
				}

				@Override
				public void connectionFailed(IrcConnection connection, IOException e) {
					log.info("Connection from '" + from.getServerName() + "' to '" + to.getServerName() + "' failed");
				}
			});
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void sleep(int ms) {
		log.info("Sleep " + ms + "ms");
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Sleep end");
	}
	
	public static void sleep() {
		log.info("Sleep infinite");
		try {
			while(true)
				Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Sleep end");
	}
}
