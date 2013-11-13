package org.mylife.home.net.hub;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.mylife.home.net.hub.configuration.IrcBinding;
import org.mylife.home.net.hub.configuration.IrcConfiguration;
import org.mylife.home.net.hub.configuration.IrcLinkAccept;
import org.mylife.home.net.hub.configuration.IrcLinkConnect;
import org.mylife.home.net.hub.configuration.IrcOperator;

public class Test {

	private static class Config implements IrcConfiguration {

		@Override
		public String getNetworkName() {
			return "mti-team2.dyndns.org";
		}

		@Override
		public String getServerName() {
			return null;
		}

		@Override
		public String getServerDescription() {
			return null;
		}

		@Override
		public int getServerToken() {
			return 0;
		}

		@Override
		public int getPingIntervalMs() {
			return 60000;
		}

		@Override
		public int getPingTimeoutMs() {
			return 30000;
		}

		@Override
		public String getLocation1() {
			return "location 1";
		}

		@Override
		public String getLocation2() {
			return "location 2";
		}

		@Override
		public String getEmail() {
			return "email";
		}

		@Override
		public String getServerInfoContent() {
			return "server info";
		}

		@Override
		public String getServerMotdContent() {
			return "motd";
		}

		@Override
		public Collection<IrcLinkAccept> getLinksAccept() {
			return new ArrayList<IrcLinkAccept>();
		}

		@Override
		public Collection<IrcLinkConnect> getLinksConnect() {
			return new ArrayList<IrcLinkConnect>();
		}

		@Override
		public Collection<IrcBinding> getBindings() {
			Collection<IrcBinding> list = new ArrayList<IrcBinding>();
			list.add(new IrcBinding(null, 6667, false));
			try {
				list.add(new IrcBinding(InetAddress.getLocalHost().getHostName(), 6667, false));
			} catch (UnknownHostException e) {
				throw new RuntimeException(e);
			}
			return list;
		}

		@Override
		public Collection<IrcOperator> getOperators() {
			Collection<IrcOperator> list = new ArrayList<IrcOperator>();
			list.add(new IrcOperator("bouh", "*", "toto"));
			return list;
		}

	}

	public static void main(String[] args) {

		System.out
				.println("Welcome to jIRCd: The world's first full-featured multiplatform Java-powered IRC"
						+ " server. Created and maintained by Tyrel L. Haveman and Mark Hale.");
		System.out
				.println("jIRCd uses a TCP protocol based on the Internet Relay Chat Protocol (RFC 1459), "
						+ "by Jarkko Oikarinen (May 1993). Portions may also be based on the IRC version 2 "
						+ "protocol (RFC 2810, RFC 2811, RFC 2812, RFC 2813) by C. Kalt (April 2000).");
		System.out.println("Please visit " + IrcServer.VERSION_URL
				+ " for the latest information and releases.");
		System.out.println();

		IrcServer jircd = null;
		// attempt to read the specified configuration file
		try {
			jircd = new IrcServer(new Config());
		} catch (IOException ioe) {
			System.err.println(ioe
					+ " occured while reading configuration file.");
			System.exit(1);
		}

		jircd.start();

		// now just hang out forever
		System.out.println("Press enter to terminate.");
		try {
			System.in.read();
		} catch (IOException e) {
			System.err.println(e
					+ " occured while waiting for program termination.");
			System.exit(1);
		}

		System.out.println("Shutting down...");
		jircd.stop();
	}
}
