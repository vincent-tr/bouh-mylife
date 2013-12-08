package org.mylife.home.net.hub.irc;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.net.hub.irc.io.IOListener;
import org.mylife.home.net.hub.irc.io.IOManager;
import org.mylife.home.net.hub.irc.structure.Network;
import org.mylife.home.net.hub.irc.tasks.ConnectTask;
import org.mylife.home.net.hub.irc.tasks.NetworkAccessTask;
import org.mylife.home.net.hub.irc.tasks.ServerDisconnectTask;
import org.mylife.home.net.hub.irc.tasks.UserDisconnectTask;

public class IrcServer extends Thread {

	public static final String NAME;
	public static final String VERSION;
	public static final String BUILD_TIMESTAMP;

	static {
		Properties ircServerProperties = new Properties();
		String name = "/"
				+ IrcServer.class.getPackage().getName().replace('.', '/')
				+ "/ircserver.properties";
		try {
			ircServerProperties.load(IrcServer.class.getResourceAsStream(name));
		} catch (IOException e) {
			throw new RuntimeException("Error loading properties", e);
		}
		NAME = ircServerProperties.getProperty("name");
		VERSION = ircServerProperties.getProperty("version");
		BUILD_TIMESTAMP = ircServerProperties.getProperty("build.timestamp");

	}

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
	private long startTime;
	private String serverName;
	private String networkName;

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
		// Accès que depuis le thread du server
		if (!Thread.currentThread().equals(this))
			throw new UnsupportedOperationException(
					"Not server thread : use requestNetworkAccess instead");
		return net;
	}

	public IrcServer(IrcConfiguration config) {
		this.config = config;
		this.setName("IrcServer thread " + config.getServerName());
		this.setDaemon(true);
	}

	public long getStartTimeMillis() {
		return startTime;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String getNetworkName() {
		return networkName;
	}

	@Override
	public String toString() {
		return "IrcServer:" + serverName;
	}
	

	// ----------------------------------------------------------------
	// -------------------- Gestion de l'exécution --------------------
	// ----------------------------------------------------------------

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
			log.log(Level.SEVERE, "Fatal error while running", ex);
			fatalError = ex;
			status = STATE_ERROR;
		}
	}

	private void execute() throws Exception {
		exit = false;
		while (!exit) {
			select();
			executeExternalTasks();
			scheduleTasks();
		}
	}

	private void init() throws Exception {

		startTime = System.currentTimeMillis();

		iom = new IOManager();
		externalTasks = new ConcurrentLinkedQueue<Runnable>();
		scheduledTasks = new ArrayList<Runnable>();

		networkName = config.getNetworkName();
		String tmpServerName = config.getServerName();
		if (tmpServerName == null)
			tmpServerName = InetAddress.getLocalHost().getHostName();
		serverName = tmpServerName + "." + networkName;
		int serverToken = config.getServerToken();
		if (serverToken == 0)
			serverToken = serverName.hashCode();

		net = new Network(networkName);
		net.serverAdd(this.serverName, serverToken, null);

		connections = new ArrayList<IrcConnection>();
		listeners = new ArrayList<IOListener>();

		for (IrcConfiguration.Listener listenerConfig : config.getListeners()) {
			String address = listenerConfig.getAddress();
			if ("*".equals(address))
				address = null;
			int port = listenerConfig.getPort();
			IOListener listener = new IOListener(listenerHandler, address, port);
			listeners.add(listener);
			iom.addElement(listener);
		}
	}

	private void terminate() throws Exception {

		for (IOListener listener : listeners) {
			iom.removeElement(listener);
			listener.close();
		}

		Collection<IrcConnection> localConnections = new ArrayList<IrcConnection>(
				connections);
		for (IrcConnection con : localConnections) {
			con.close();
		}

		listeners = null;
		connections = null;
		net = null;
		iom.close();
		iom = null;
		externalTasks = null;
		scheduledTasks = null;
	}

	// ----------------------------------------------------------------
	// -------------------- Gestion du réseau -------------------------
	// ----------------------------------------------------------------

	private void select() throws Exception {
		iom.select(SELECT_TIMEOUT);
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
		connection.markConnected();
	}

	private void newConnection(String address, int port,
			IrcConnectHandler connectHandler) {
		IrcConnection connection = null;
		try {
			connection = new IrcConnection(this, address, port, connectHandler);
			iom.addElement(connection.getIOConnection());
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error creating connection", e);
			return;
		}

		connections.add(connection);
		// pas encore connecté
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

	// ----------------------------------------------------------------
	// -------------------- Gestion des tâches périodiques ------------
	// ----------------------------------------------------------------

	private Collection<Runnable> scheduledTasks;

	private void scheduleTasks() throws Exception {
		Collection<Runnable> localScheduledTasks = new ArrayList<Runnable>(
				scheduledTasks);
		for (Runnable task : localScheduledTasks) {
			task.run();
		}
	}

	public void addScheduledTask(Runnable runnable) {
		if (runnable == null)
			throw new IllegalArgumentException();
		scheduledTasks.add(runnable);
	}

	public void removeScheduledTask(Runnable runnable) {
		if (runnable == null)
			throw new IllegalArgumentException();
		scheduledTasks.remove(runnable);
	}

	// ----------------------------------------------------------------
	// -------------------- Gestion des tâches extérieures -------------
	// ----------------------------------------------------------------

	private void executeExternalTasks() throws Exception {
		// Exécution de toutes les tâches en attente
		Runnable task;
		while ((task = externalTasks.poll()) != null) {
			task.run();
		}
	}

	private Queue<Runnable> externalTasks;

	/**
	 * Exécution d'un traitement sur le thread du serveur
	 * 
	 * @param runnable
	 */
	public void execute(Runnable runnable) {
		if (runnable == null)
			throw new IllegalArgumentException();
		externalTasks.add(runnable);
		// Arrêt du select pour permettre l'exécution 'immédiate' de la tâche
		iom.wakeup();
	}

	/**
	 * Déconnexion d'un utilisateur local depuis l'extérieur
	 * 
	 * @param userNick
	 * @return
	 */
	public boolean externalDisconnectUser(String userNick)
			throws InterruptedException {
		UserDisconnectTask task = new UserDisconnectTask(this, userNick);
		execute(task);
		task.waitTask();
		return task.getResult();
	}

	/**
	 * Déconnexion d'un serveur peer depuis l'extérieur
	 * 
	 * @param userNick
	 * @return
	 */
	public boolean externalDisconnectServer(String serverName)
			throws InterruptedException {
		ServerDisconnectTask task = new ServerDisconnectTask(this, serverName);
		execute(task);
		task.waitTask();
		return task.getResult();
	}

	/**
	 * Connexion d'un lien
	 * 
	 * @param address
	 * @param port
	 * @param connectHandler
	 * @throws InterruptedException
	 */
	public void connect(String address, int port,
			IrcConnectHandler connectHandler) throws InterruptedException {
		ConnectTask task = new ConnectTask(address, port, connectHandler,
				new ConnectTask.Handler() {
					@Override
					public void executeNewConnection(String address, int port,
							IrcConnectHandler connectHandler) {
						newConnection(address, port, connectHandler);
					}
				});
		execute(task);
		task.waitTask();
	}

	/**
	 * Accès au réseau
	 * 
	 * @param handler
	 * @throws InterruptedException
	 */
	public void requestNetworkAccess(IrcNetworkAccessHandler handler)
			throws InterruptedException {
		if (handler == null)
			throw new IllegalArgumentException();
		NetworkAccessTask task = new NetworkAccessTask(this, handler);
		execute(task);
		task.waitTask();
	}
}
