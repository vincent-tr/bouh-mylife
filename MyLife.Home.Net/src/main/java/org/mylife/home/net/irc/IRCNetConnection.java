package org.mylife.home.net.irc;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.schwering.irc.lib.IRCConnection;

/**
 * Amélioration de IRCConnection
 * 
 * @author pumbawoman
 * 
 */
public class IRCNetConnection extends Thread {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(IRCNetConnection.class
			.getName());

	private IRCNetStream stream;
	
	private AutoConnection autoConnect;

	/**
	 * Constructeur avec données
	 * 
	 * @param host
	 * @param port
	 * @param nick
	 * @param id
	 */
	public IRCNetConnection(String host, int port, String nick, String id) {
		super(host, new int[] { port }, null, nick, id, id);
	}

	/**
	 * Démarrage des tentatives de connexion
	 */
	@Override
	public synchronized void connect() throws IOException {
		autoConnect = new AutoConnection();
		autoConnect.start();
	}

	@Override
	public synchronized void close() {
		if (autoConnect != null) {
			// Si tentative de connexion en cours on arrête
			autoConnect.close();
			autoConnect = null;
		}
		super.close();
	}

	private synchronized void connected() {
		autoConnect = null;
	}

	private void superConnect() throws IOException {
		super.connect();
	}

	/**
	 * Gestion de la connexion automatique
	 * 
	 * @author pumbawoman
	 * 
	 */
	private class AutoConnection extends Thread {

		private boolean active;
		private static final int interval = 10000;

		public AutoConnection() {
			setDaemon(true);
		}
		
		private void safeJoin() {
			try {
				join();
			} catch (InterruptedException e) {
			}
		}

		private void safeSleep() {
			try {
				sleep(interval);
			} catch (InterruptedException e) {
			}
		}

		@Override
		public synchronized void start() {
			active = true;
			super.start();
		}

		public synchronized void close() {
			active = false;
			interrupt();
			if (!Thread.currentThread().equals(this)) {
				safeJoin();
			}
		}

		@Override
		public void run() {
			while (active) {
				try {
					superConnect();
					active = false;
					connected();
				} catch (IOException ex) {
					log.log(Level.SEVERE, "IRC connection error", ex);
					safeSleep();
				}
			}
		}
	}
}
