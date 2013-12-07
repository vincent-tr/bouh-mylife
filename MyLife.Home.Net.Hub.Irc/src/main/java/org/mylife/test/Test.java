package org.mylife.test;

import java.io.IOException;

import org.mylife.home.net.hub.irc.IrcServer;

public class Test {

	public static void main(String[] args) throws IOException {
		IrcServer server = new IrcServer(null);
		server.start();
		sleep();
	}
	
	private static void sleep() {
		try {
			Object o = new Object();
			synchronized(o) {
				o.wait();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
