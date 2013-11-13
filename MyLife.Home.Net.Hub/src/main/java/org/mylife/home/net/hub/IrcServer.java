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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
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
import org.mylife.home.net.hub.irc.Client;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.Link;
import org.mylife.home.net.hub.irc.Listener;
import org.mylife.home.net.hub.irc.Message;
import org.mylife.home.net.hub.irc.Network;
import org.mylife.home.net.hub.irc.Operator;
import org.mylife.home.net.hub.irc.PingTimerTask;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.Source;
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
	public static final int VERSION_MINOR = 6;
	public static final int VERSION_PATCH = 2;
	public static final String VERSION_URL = "http://j-ircd.sourceforge.net/";

	protected final Network network;
	/** this server. */
	protected final Server thisServer;

	/** set of server socket Listeners. */
	private final Set<Listener> listeners = new HashSet<Listener>();

	/** set of Clients (including servers) that are connected to this servers. */
	private final Set<Client> clients = Collections
			.synchronizedSet(new HashSet<Client>());

	/** set of Links to other servers. */
	private final Set<Link> links = Collections
			.synchronizedSet(new HashSet<Link>());

	private final Set<Operator> operators = Collections
			.synchronizedSet(new HashSet<Operator>());

	private final Map<String, Command> commands = Collections
			.synchronizedMap(new HashMap<String, Command>());

	// configuration and informational information
	private final IrcConfiguration config;
	protected final String hostName;
	protected final Collection<IrcLinkAccept> linksAccept;
	protected final Collection<IrcLinkConnect> linksConnect;

	private long startTime = -1;

	private final Timer timer = new Timer(true);
	private PingTimerTask pingTimerTask;

	public IrcServer(IrcConfiguration config) throws IOException {
		this.config = config;
		this.hostName = InetAddress.getLocalHost().getHostName();
		this.linksAccept = Collections.synchronizedCollection(config
				.getLinksAccept());
		this.linksConnect = Collections.synchronizedCollection(config
				.getLinksConnect());

		network = new Network(config.getNetworkName());
		String serverName = config.getServerName();
		if(StringUtils.isEmpty(serverName))
			serverName = hostName + "." + network.getName();
		if (serverName.indexOf('.') == -1)
			logger.log(Level.WARNING,
					"The server name should contain at least one dot, e.g. "
							+ serverName + ".net");
		String desc = config.getServerDescription();
		if(StringUtils.isEmpty(desc))
			desc = serverName;
		int token = config.getServerToken();
		if (token <= 0) {
			token = serverName.hashCode();
			logger.info("Generated server token: " + token);
		}
		thisServer = new Server(serverName, token, desc, network);
		network.addServer(thisServer);

		for (Class<?> clazz : CommandFactory.getInstance().listClasses()) {
			loadPlugin(clazz);
		}
		
		initConfiguration();
	}

	private void initConfiguration() {
		network.setName(config.getNetworkName());
		thisServer.setDescription(config.getServerDescription());

		for (IrcBinding confBinding : config.getBindings()) {
			ServerSocketFactory factory;
			if (confBinding.isSsl())
				factory = SSLServerSocketFactory.getDefault();
			else
				factory = ServerSocketFactory.getDefault();
			listeners.add(new Listener(this, confBinding.getAdress(),
					confBinding.getPort(), factory));
		}

		for (IrcOperator confOper : config.getOperators()) {
			operators.add(new Operator(confOper.getName(), confOper.getHost(), confOper.getPass()));
		}
	}

	public void reloadConfiguration() throws IOException {
		stopListeners();
		listeners.clear();
		operators.clear();
		initConfiguration();
		startListeners();
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
			commands.put(command.getName().toUpperCase(), command);
			logger.info("...installed " + command.getName() + " ("
					+ cls.toString() + ")");
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not load class " + cls.toString(),
					ex);
		}
	}

	public void reloadPolicy() {
		logger.info("Refreshing security policy");
		Policy.getPolicy().refresh();
	}

	public void start() {
		logger.info(getVersion() + " starting...");
		startTime = System.currentTimeMillis();

		// start ping timer
		if (pingTimerTask != null) {
			pingTimerTask.cancel();
			pingTimerTask = null;
		}
		pingTimerTask = new PingTimerTask(this);
		final long pingInterval = config.getPingIntervalMs();
		timer.schedule(pingTimerTask, 0, pingInterval);

		startListeners();
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
				logger.log(Level.WARNING, "..." + listener.toString()
						+ " (FAILED)...");
			}
		}
		logger.info("...complete");
	}

	/**
	 * Stops this server. All clients and server links are disconnected.
	 */
	public void stop() {
		logger.info("Stopping...");
		// stop ping timer
		if (pingTimerTask != null) {
			pingTimerTask.cancel();
			pingTimerTask = null;
		}

		// broadcast shutdown notice
		for (User user : thisServer.getUsers()) {
			Message message = new Message(thisServer, "NOTICE", user);
			message.appendParameter("WARNING: Server shut down by local console.");
			user.send(message);
		}

		stopListeners();

		// disconnect clients
		logger.info("Disconnecting clients...");
		synchronized (clients) {
			for (Client client : clients) {
				Source src = client.getSource();
				if (src instanceof User)
					thisServer.removeUser((User) src, "Server shutdown");
				client.getConnection().close();
			}
			clients.clear();
		}
		// disconnect links
		logger.info("Disconnecting server links...");
		synchronized (links) {
			for (Link link : links) {
				link.getConnection().close();
			}
			links.clear();
		}

		startTime = -1;
	}

	private void stopListeners() {
		logger.info("Unbinding listeners...");
		for (Listener listener : listeners) {
			listener.close();
		}
	}

	public Set<Client> getClients() {
		return Collections.unmodifiableSet(clients);
	}

	public void addClient(Client client) {
		clients.add(client);
	}

	public void disconnectClient(Client client, String reason) {
		Source src = client.getSource();
		if (src instanceof User)
			thisServer.removeUser((User) src, reason);
		clients.remove(client);
		client.getConnection().close();
	}

	/**
	 * Adds a link to another server.
	 */
	public void addLink(Link link) {
		links.add(link);
	}

	/**
	 * Disconnects a link to another server.
	 */
	public void disconnectLink(Link link) {
		links.remove(link);
		link.getConnection().close();
	}

	/**
	 * Security checks are performed on all commands.
	 */
	public void invokeCommand(Source src, final Message message) {
		// determine sender
		if (src instanceof Server) { // received from a SERVER
			src = message.resolveSender(((Server) src).getNetwork());
		}

		// find command
		final String cmdName = message.getCommand();
		final Command command = (Command) commands.get(cmdName.toUpperCase());
		if (command == null) {
			// Unknown command
			Util.sendUnknownCommandError(src, cmdName);
			logger.finest("Unknown command: " + message.toString());
			return;
		}

		String[] params = new String[message.getParameterCount()];
		for (int i = 0; i < params.length; i++)
			params[i] = message.getParameter(i);

		if (params.length < command.getMinimumParameterCount()) {
			// too few parameters
			Util.sendNeedMoreParamsError(src, cmdName);
			return;
		}
		// HERE WE GO!!!!!!!!!
		try {
			Util.checkCommandPermission(command);
			command.invoke(src, params);
		} catch (RuntimeException e) {
			logger.log(Level.WARNING,
					"Error invoking method in " + command.getClass()
							+ " for command " + cmdName, e);
		}
	}

	public Command getCommand(String name) {
		return (Command) commands.get(name.toUpperCase());
	}

	public IrcConfiguration getConfiguration() {
		return config;
	}

	public IrcLinkAccept findLinkAccept(String remoteAddress, int localPort) {
		for (IrcLinkAccept item : linksAccept) {
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
		for (IrcLinkConnect item : linksConnect) {
			if (item.getRemotePort() != remotePort)
				continue;
			if (remoteAddress.equalsIgnoreCase(item.getRemoteAddress()))
				return item;
		}

		return null;
	}

	public String getHostName() {
		return hostName;
	}

	public Set<Operator> getOperators() {
		return Collections.unmodifiableSet(operators);
	}

	/**
	 * Returns the server uptime in milliseconds.
	 */
	public long getUptimeMillis() {
		return (startTime == -1) ? 0 : (System.currentTimeMillis() - startTime);
	}

	public long getStartTimeMillis() {
		return startTime;
	}

	public Server getServer() {
		return thisServer;
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
		return network.channels.size();
	}

	public int getServerCount() {
		return network.servers.size();
	}

	public String getVersion() {
		return "jIRCd-" + VERSION_MAJOR + '.' + VERSION_MINOR + '.'
				+ VERSION_PATCH;
	}

	public String toString() {
		return "jIRCd";
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
}
