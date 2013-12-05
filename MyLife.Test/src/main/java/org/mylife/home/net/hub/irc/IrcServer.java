package org.mylife.home.net.hub.irc;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.net.hub.irc.commands.CommandFactory;
import org.mylife.home.net.hub.irc.commands.ConnectionOpenedCommand;
import org.mylife.home.net.hub.irc.io.IOListener;
import org.mylife.home.net.hub.irc.io.IOManager;
import org.mylife.home.net.hub.irc.structure.Network;

public class IrcServer extends Thread {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(IrcServer.class
			.getName());

	public static final int STATE_STOPPED = 0;
	public static final int STATE_ERROR = -1;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_STARTING = 2;
	public static final int STATE_STOPPING = 3;

	private static final int SELECT_TIMEOUT = 10000;

	private final IOListenerHandler listenerHandler = new IOListenerHandler();

	private boolean exit;
	private final IrcConfiguration config;
	private int status = STATE_STOPPED;
	private Exception fatalError;

	private IOManager iom;
	private Network net;
	private Collection<IrcConnection> connections;
	private Collection<IOListener> listeners;

	public IrcConfiguration getConfig() {
		return config;
	}

	public int getStatus() {
		return status;
	}

	public Exception getFatalError() {
		return fatalError;
	}

	public Network getNetwork() {
		return net;
	}

	public IrcServer(IrcConfiguration config) {
		this.config = config;
	}

	public void close() {
		exit = true;
		iom.wakeup();
	}

	@Override
	public void run() {

		try {
			status = STATE_STARTING;
			fatalError = null;

			init();
			status = STATE_RUNNING;
			try {
				execute();
			} finally {
				status = STATE_STOPPING;
				terminate();
			}
			status = STATE_STOPPED;

		} catch (Exception ex) {
			fatalError = ex;
			status = STATE_ERROR;
		}
	}

	private void execute() throws Exception {
		exit = false;
		while (!exit) {
			select();
			scheduleTasks();
		}
	}

	private void init() throws Exception {
		iom = new IOManager();
		net = new Network();
		connections = new ArrayList<IrcConnection>();
		listeners = new ArrayList<IOListener>();

		// TODO : lecture de config
		IOListener listener = new IOListener(listenerHandler, null, 6667);
		listeners.add(listener);
		iom.addElement(listener);
	}

	private void terminate() throws Exception {
		// TODO

		for (IOListener listener : listeners) {
			iom.removeElement(listener);
			listener.close();
		}

		// fermeture des connexions

		listeners = null;
		connections = null;
		net = null;
		iom.close();
		iom = null;
	}

	private void select() throws Exception {
		iom.select(SELECT_TIMEOUT);
	}

	private void scheduleTasks() throws Exception {
		// TODO
	}

	private void newConnection(SocketChannel client) {
		IrcConnection connection = null;
		try {
			connection = new IrcConnection(this, client);
			iom.addElement(connection.getIOConnection());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error creating connection", e);
			return;
		}

		connections.add(connection);
		ConnectionOpenedCommand cmd = CommandFactory.getInstance()
				.getConnectionOpenedCommand();
		cmd.invoke(this, connection);
	}

	/* internal */void removeConnection(IrcConnection connection) {
		try {
			iom.removeElement(connection.getIOConnection());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error removing connection", e);
		}
		connections.remove(connection);
	}

	private class IOListenerHandler implements IOListener.Handler {
		@Override
		public void newClient(SocketChannel client) {
			newConnection(client);
		}
	}
}
