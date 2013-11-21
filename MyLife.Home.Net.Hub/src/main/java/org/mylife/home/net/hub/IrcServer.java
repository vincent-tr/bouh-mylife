/*
 * jIRCd - Java Internet Relay Chat Daemon
 * Copyright 2003 Tyrel L. Haveman <tyrel@haveman.net>
 *
 * This file is part of jIRCd.
 *
 * jIRCd is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * jIRCd is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with jIRCd; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.mylife.home.net.hub;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.security.Policy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.apache.commons.lang3.StringUtils;
import org.mylife.home.net.hub.configuration.IrcBinding;
import org.mylife.home.net.hub.configuration.IrcConfiguration;
import org.mylife.home.net.hub.configuration.IrcLinkAccept;
import org.mylife.home.net.hub.configuration.IrcLinkConnect;
import org.mylife.home.net.hub.configuration.IrcOperator;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.CommandContext;
import org.mylife.home.net.hub.irc.ConnectedEntity;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc.ConnectionManager;
import org.mylife.home.net.hub.irc.Listener;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Network;
import org.mylife.home.net.hub.irc.Operator;
import org.mylife.home.net.hub.irc.RegisteredEntity;
import org.mylife.home.net.hub.irc.RegistrationCommand;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.SocketListener;
import org.mylife.home.net.hub.irc.UnregisteredEntity;
import org.mylife.home.net.hub.irc.User;
import org.mylife.home.net.hub.irc.Util;
import org.mylife.home.net.hub.irc.commands.CommandFactory;

/**
 * @author thaveman
 * @author markhale
 */
public class IrcServer implements IrcServerMBean {
	private static final Logger logger = Logger.getLogger(IrcServer.class
			.getName());

	// version information
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 7;
	public static final int VERSION_PATCH = 0;
	public static final String VERSION_URL = "http://j-ircd.sourceforge.net/";

	protected final Network network;
	/** this server. */
	protected final Server thisServer;

	/** set of server socket Listeners. */
	protected final Set<Listener> listeners = Collections
			.synchronizedSet(new HashSet<Listener>());
	protected final ConnectionManager links = new ConnectionManager();
	/** commands */
	private final Map<String, CommandContext> cmdCtxs = Collections
			.synchronizedMap(new HashMap<String, CommandContext>());

	// configuration and informational information
	private long startTime = -1;

	// configuration and informational information
	private final IrcConfiguration config;
	protected final String hostName;
	private final Set<Operator> operators = Collections
			.synchronizedSet(new HashSet<Operator>());

	private final ExecutorService listenerThreadPool = Executors
			.newCachedThreadPool();
	private final ScheduledExecutorService timer = Executors
			.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> pingFuture;

	public IrcServer(IrcConfiguration config) throws IOException {

		this.config = config;
		this.hostName = InetAddress.getLocalHost().getHostName();

		String networkName = config.getNetworkName();
		if (Util.isIRCString(networkName)) {
			logger.info("Network name: " + networkName);
		} else {
			logger.warning("Invalid network name");
			networkName = networkName.replace(' ', '-');
			logger.info("Generated network name: " + networkName);
		}
		network = new Network(networkName);
		String serverName = config.getServerName();
		if (StringUtils.isEmpty(serverName))
			serverName = hostName + "." + network.getName();
		if (serverName.indexOf('.') == -1)
			logger.log(Level.WARNING,
					"The server name should contain at least one dot, e.g. "
							+ serverName + ".net");
		String desc = config.getServerDescription();
		if (StringUtils.isEmpty(desc))
			desc = serverName;
		int token = config.getServerToken();
		if (token <= 0) {
			token = serverName.hashCode();
			logger.info("Generated server token: " + token);
		}
		thisServer = new Server(serverName, token, desc, network);

		for (Class<?> clazz : CommandFactory.getInstance().listClasses()) {
			loadPlugin(clazz);
		}

		initConfiguration();

	}

	protected void loadPlugin(Class<?> cls) {
		try {
			Command command;
			try {
				command = (Command) cls.newInstance();
			} catch (InstantiationException ie) {
				Constructor<?> cnstr = cls
						.getConstructor(new Class[] { IrcServerMBean.class });
				command = (Command) cnstr.newInstance(new Object[] { this });
			}
			cmdCtxs.put(command.getName().toUpperCase(), new CommandContext(
					command));
			logger.info("...installed " + command.getName() + " ("
					+ cls.toString() + ")");
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not load class " + cls.toString(),
					ex);
		}
	}

	private void initConfiguration() {
		for (IrcBinding confBinding : config.getBindings()) {
			ServerSocketFactory factory;
			if (confBinding.isSsl())
				factory = SSLServerSocketFactory.getDefault();
			else
				factory = ServerSocketFactory.getDefault();
			listeners.add(new SocketListener(this, confBinding.getAddress(),
					confBinding.getPort(), factory, listenerThreadPool));
		}

		for (IrcOperator confOper : config.getOperators()) {
			operators.add(new Operator(confOper.getName(), confOper.getHost(),
					confOper.getPass()));
		}
	}

	public void reloadConfiguration() throws IOException {
		stopListeners();
		listeners.clear();
		initConfiguration();
		startListeners();
	}

	public synchronized void reloadPolicy() {
		logger.info("Refreshing security policy");
		Policy.getPolicy().refresh();
	}

	private void startPings() {
		if (pingFuture == null || pingFuture.isDone()) {
			PingTask pingTask = new PingTask();
			final long pingInterval = config.getPingIntervalMs();
			pingFuture = timer.scheduleWithFixedDelay(pingTask, 0,
					pingInterval, TimeUnit.MILLISECONDS);
		}
	}

	private void stopPings() {
		if (pingFuture != null) {
			pingFuture.cancel(true);
			pingFuture = null;
		}
	}

	public synchronized void start() {
		logger.info(getVersion() + " starting...");
		startTime = System.currentTimeMillis();

		startListeners();
		startPings();
	}

	private void startListeners() {
		logger.info("Binding to port(s)...");
		for (Iterator<Listener> iter = listeners.iterator(); iter.hasNext();) {
			Listener listener = iter.next();
			if (listener.bind()) {
				listener.start();
				logger.info("..." + listener.toString() + "...");
			} else {
				iter.remove();
				logger.warning("..." + listener.toString() + " (FAILED)...");
			}
		}
		logger.info("...complete");
	}

	private void stopListeners() {
		logger.info("Stopping all listener connections...");
		for (Iterator<Listener> iter = listeners.iterator(); iter.hasNext();) {
			Listener listener = iter.next();
			listener.stop();
		}
	}

	/**
	 * Stops this server. All clients and server links are disconnected.
	 */
	public synchronized void stop() {
		logger.info("Stopping...");
		stopPings();

		// prevent new incoming connections
		stopListeners();

		// broadcast shutdown notice
		for (Iterator<User> iter = thisServer.getUsers().iterator(); iter
				.hasNext();) {
			User user = iter.next();
			Message message = new Message(thisServer, "NOTICE", user);
			message.appendLastParameter("WARNING: Server shut down by local console.");
			user.send(message);
		}

		// close all connections
		logger.info("Closing all listener connections...");
		for (Iterator<Listener> iter = listeners.iterator(); iter.hasNext();) {
			Listener listener = iter.next();
			disconnect(listener.getConnections());
			listener.close();
		}
		listeners.clear();
		logger.info("Closing all link connections...");
		disconnect(links.getConnections());

		startTime = -1;

		listenerThreadPool.shutdown();
		timer.shutdown();
	}

	private void disconnect(Set<Connection> connections) {
		for (Iterator<Connection> iter = connections.iterator(); iter.hasNext();) {
			Connection conn = iter.next();
			conn.getHandler().getEntity().disconnect("Server shutdown");
		}
	}

	public final Set<Listener> getListeners() {
		return listeners;
	}

	public final ConnectionManager getLinks() {
		return links;
	}

	/**
	 * Security checks are performed on all commands.
	 */
	public final void invokeCommand(final Message message) {
		final ConnectedEntity from = message.getSender();
		final String cmdName = message.getCommand();
		// find command
		final CommandContext ctx = (CommandContext) cmdCtxs.get(cmdName
				.toUpperCase());
		if (ctx == null) {
			// unknown command
			Util.sendUnknownCommandError(from, cmdName);
			logger.fine("Unknown command: " + message.toString());
			return;
		}

		final Command command = ctx.getCommand();

		if (message.getParameterCount() < command.getMinimumParameterCount()) {
			// too few parameters
			Util.sendNeedMoreParamsError(from, cmdName);
			return;
		}

		String[] params = new String[message.getParameterCount()];
		for (int i = 0; i < params.length; i++)
			params[i] = message.getParameter(i);

		// HERE WE GO!!!!!!!!!
		try {
			Util.checkCommandPermission(command);
			if (from instanceof UnregisteredEntity) {
				if (command instanceof RegistrationCommand) {
					((RegistrationCommand) command).invoke(
							(UnregisteredEntity) from, params);
					ctx.commandInvoked();
				} else {
					Util.sendNotRegisteredError((UnregisteredEntity) from);
					logger.fine("Unregistered user " + from
							+ " attempted to use command " + message.toString());
				}
			} else {
				command.invoke((RegisteredEntity) from, params);
				ctx.commandInvoked();
			}
		} catch (RuntimeException e) {
			logger.log(Level.WARNING,
					"Error invoking method in " + command.getClass()
							+ " for command " + cmdName, e);
		}
	}

	public final CommandContext getCommandContext(String name) {
		return (CommandContext) cmdCtxs.get(name.toUpperCase());
	}

	public final Set<CommandContext> getCommandContexts() {
		return new HashSet<CommandContext>(cmdCtxs.values());
	}

	/**
	 * Returns the server uptime in milliseconds.
	 */
	public final long getUptimeMillis() {
		return (startTime == -1) ? 0 : (System.currentTimeMillis() - startTime);
	}

	public final long getStartTimeMillis() {
		return startTime;
	}

	public Server getServer() {
		return thisServer;
	}

	public IrcConfiguration getConfiguration() {
		return config;
	}

	public String getHostName() {
		return hostName;
	}

	public int getVisibleUserCount() {
		return thisServer.getUserCount(User.UMODE_INVISIBLE, false);
	}

	public int getInvisibleUserCount() {
		return thisServer.getUserCount(User.UMODE_INVISIBLE, true);
	}

	/**
	 * Returns the number of visible users on the network.
	 */
	public int getNetworkVisibleUserCount() {
		return network.getUserCount(User.UMODE_INVISIBLE, false);
	}

	/**
	 * Returns the number of invisible users on the network.
	 */
	public int getNetworkInvisibleUserCount() {
		return network.getUserCount(User.UMODE_INVISIBLE, true);
	}

	public int getChannelCount() {
		return network.getChannels().size();
	}

	public int getServerCount() {
		return network.getServers().size();
	}

	public String getVersion() {
		return "jIRCd-" + VERSION_MAJOR + '.' + VERSION_MINOR + '.'
				+ VERSION_PATCH;
	}

	public String toString() {
		return "jIRCd";
	}

	class PingTask implements Runnable {
		public void run() {
			// PING? PONG!
			for (Iterator<Listener> iter = listeners.iterator(); iter.hasNext();) {
				Listener listener = iter.next();
				ping(listener.getConnections());
			}
			ping(links.getConnections());
		}

		private void ping(Set<Connection> connections) {
			for (Iterator<Connection> iter = connections.iterator(); iter
					.hasNext();) {
				Connection connection = (Connection) iter.next();
				Connection.Handler handler = connection.getHandler();
				if (!handler.ping()) {
					// should have had PONG a long time ago, timeout please!
					handler.getEntity().disconnect("Ping timeout");
				}
			}
		}
	}

	protected static class ExtensionFilenameFilter implements FilenameFilter {
		private final String extension;

		public ExtensionFilenameFilter(String ext) {
			extension = "." + ext;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(extension);
		}
	}

	public IrcLinkAccept findLinkAccept(String remoteAddress, int localPort) {
		for (IrcLinkAccept item : this.getConfiguration().getLinksAccept()) {
			if (item.getLocalPort() != localPort)
				continue;
			if (remoteAddress.equalsIgnoreCase(item.getRemoteAddress()))
				return item;
			// match par wilcards pour accept
			if (remoteAddress.matches(item.getRemoteAddress()))
				return item;
		}

		return null;
	}

	public IrcLinkConnect findLinkConnect(String remoteAddress, int remotePort) {
		for (IrcLinkConnect item : this.getConfiguration().getLinksConnect()) {
			if (item.getRemotePort() != remotePort)
				continue;
			if (remoteAddress.equalsIgnoreCase(item.getRemoteAddress()))
				return item;
		}

		return null;
	}

	public Set<Operator> getOperators() {
		return Collections.unmodifiableSet(operators);
	}
}
