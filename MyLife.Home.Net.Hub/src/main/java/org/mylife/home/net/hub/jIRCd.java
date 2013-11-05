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
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Policy;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Timer;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.mylife.home.net.hub.irc.Client;
import org.mylife.home.net.hub.irc.Command;
import org.mylife.home.net.hub.irc.CommandPermission;
import org.mylife.home.net.hub.irc.Constants;
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
import org.mylife.home.net.hub.irc_p10.Server_P10;

/**
 * @author thaveman
 * @author markhale
 */
public class jIRCd implements jIRCdMBean {
	private static final Logger logger = Logger.getLogger(jIRCd.class.getName());

	// version information
	public static final int VERSION_MAJOR = 0;
	public static final int VERSION_MINOR = 6;
	public static final int VERSION_PATCH = 2;
	public static final String VERSION_URL = "http://j-ircd.sourceforge.net/";

	private static final String PLUGIN_PATH = "plugins";

	protected final Network network;
	/** this server. */
	protected final Server thisServer;

	/** set of server socket Listeners. */
	private final Set listeners = new HashSet();

	/** set of Clients (including servers) that are connected to this servers. */
	private final Set clients = Collections.synchronizedSet(new HashSet());

	/** set of Links to other servers. */
	private final Set links = Collections.synchronizedSet(new HashSet());

	private final Set operators = Collections.synchronizedSet(new HashSet());

	private final Map commands = Collections.synchronizedMap(new HashMap());

	// configuration and informational information
	private long startTime = -1;
	private final String configFile;
	private final Properties settings = new Properties();

	private final Timer timer = new Timer(true);
	private PingTimerTask pingTimerTask;

	public static void main(String[] args) {
		// program must be executed using: jircd.jIRCd <configuration file>
		if ((args == null) || (args.length < 1)) {
			System.err.println("Usage: jircd.jIRCd <configuration file>");
			System.exit(1);
		}
		final String configFile = args[0];

		System.out.println();
		System.out.println("Welcome to jIRCd: The world's first full-featured multiplatform Java-powered IRC"
					+ " server. Created and maintained by Tyrel L. Haveman and Mark Hale.");
		System.out.println("jIRCd uses a TCP protocol based on the Internet Relay Chat Protocol (RFC 1459), "
					+ "by Jarkko Oikarinen (May 1993). Portions may also be based on the IRC version 2 "
					+ "protocol (RFC 2810, RFC 2811, RFC 2812, RFC 2813) by C. Kalt (April 2000).");
		System.out.println("Please visit "+VERSION_URL+" for the latest information and releases.");
		System.out.println();

		jIRCd jircd = null;
		// attempt to read the specified configuration file
		try {
			jircd = new jIRCd(configFile);
		} catch (IOException ioe) {
			System.err.println(ioe + " occured while reading configuration file.");
			System.exit(1);
		}

		jircd.start();

		// now just hang out forever
		System.out.println("Press enter to terminate.");
		try {
			System.in.read();
		} catch (IOException e) {
			System.err.println(e + " occured while waiting for program termination.");
			System.exit(1);
		}

		System.out.println("Shutting down...");
		jircd.stop();
		//System.exit(0);
	}

	public jIRCd(String configFile) throws IOException {
		this.configFile = configFile;
		settings.setProperty("jircd.configFile", configFile);
		settings.setProperty("jircd.version.name", getVersion());
		settings.setProperty("jircd.version.url", VERSION_URL);

		loadConfiguration();
		network = new Network(settings.getProperty("jircd.networkName"));
		final String serverName = settings.getProperty("jircd.serverName", "dumb.admin");
		if(serverName.indexOf('.') == -1)
			logger.log(Level.WARNING, "The server name should contain at least one dot, e.g. "+serverName+".net");
		final String desc = settings.getProperty("jircd.description", "dumb.admin");
		String tokenProperty = settings.getProperty("jircd.token");
		int token = 0;
		if(tokenProperty != null) {
			try {
				token = Integer.parseInt(tokenProperty);
			} catch(NumberFormatException nfe) {
				logger.log(Level.WARNING, "Invalid server token", nfe);
				tokenProperty = null;
			}
		}
		if(tokenProperty == null) {
			token = org.mylife.home.net.hub.irc_p10.Util.randomServerToken();
			logger.info("Generated server token: "+token);
		}
		thisServer = new Server_P10(serverName, token, desc, network);
		network.addServer(thisServer);

		initConfiguration();
		reloadPlugins();
	}
	private void loadConfiguration() throws IOException {
		logger.info("Reading configuration file...");
		FileInputStream in = new FileInputStream(configFile);
		settings.load(in);
		in.close();
	}
	private void initConfiguration() {
		network.setName(settings.getProperty("jircd.networkName"));
		thisServer.setDescription(settings.getProperty("jircd.description", "dumb.admin"));

		for(Iterator iter = settings.entrySet().iterator(); iter.hasNext(); ) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			if(key.startsWith("jircd.bind.")) {
				String addressPort = key.substring("jircd.bind.".length());
				int pos = addressPort.indexOf('#');
				String address;
				int port = Constants.DEFAULT_PORT;
				if(pos != -1) {
					address = addressPort.substring(0, pos);
					try {
						port = Integer.parseInt(addressPort.substring(pos+1));
					} catch(NumberFormatException nfe) {
						logger.log(Level.WARNING, "Invalid port: "+port, nfe);
						continue;
					}
				} else {
					address = addressPort;
				}
				String[] params = org.mylife.home.net.hub.irc.Util.split((String) entry.getValue(), ',');
				if(params.length > 0) {
					if(params[0].equals("jircd.irc.Listener")) {
						ServerSocketFactory factory;
						if(params.length > 1) {
							if(params[1].equals(SSLServerSocketFactory.class.getName())) {
								factory = SSLServerSocketFactory.getDefault();
							} else {
								try {
									factory = (ServerSocketFactory) Class.forName(params[1]).newInstance();
								} catch(Exception e) {
									logger.log(Level.WARNING, "Could not instantiate factory: "+params[1], e);
									continue;
								}
							}
						} else {
							factory = ServerSocketFactory.getDefault();
						}
						listeners.add(new Listener(this, address, port, factory));
					} else {
						logger.log(Level.WARNING, "Unrecognised listener class: "+params[0]);
					}
				} else {
					logger.log(Level.WARNING, "No listener specified");
				}
			} else if(key.startsWith("jircd.oper.")) {
				String nameHost = key.substring("jircd.oper.".length());
				int pos = nameHost.indexOf('@');
				String name = nameHost.substring(0, pos);
				String host = nameHost.substring(pos+1);
				String pass = (String) entry.getValue();
				operators.add(new Operator(name, host, pass));
			}
		}
	}
	public void reloadConfiguration() throws IOException {
		loadConfiguration();
		stopListeners();
		listeners.clear();
		operators.clear();
		initConfiguration();
		startListeners();
	}
	public void reloadPlugins() {
		commands.clear();
		File pluginDir = new File(PLUGIN_PATH);
		File[] jarFiles = pluginDir.listFiles(new ExtensionFilenameFilter("jar"));
		for(int i=0; i<jarFiles.length; i++) {
			File jarFile = jarFiles[i];
			try {
				// create class loader for plugin
				URLClassLoader loader = URLClassLoader.newInstance(new URL[] {jarFile.toURL()});
				loadPlugin(new JarFile(jarFile), loader);
			} catch(IOException ioe) {
				logger.log(Level.WARNING, "Could not load plugin "+jarFile, ioe);
			}
		}
	}
	protected void loadPlugin(JarFile jar, ClassLoader loader) {
		logger.info("Searching plugin "+jar.getName()+"...");
		Enumeration entries = jar.entries();
		while(entries.hasMoreElements()) {
			JarEntry entry = (JarEntry) entries.nextElement();
			String name = entry.getName();
			if(name.endsWith(".class")) {
				String className = name.substring(0, name.length()-6).replace('/', '.');
				try {
					Class cls = loader.loadClass(className);
					if(Command.class.isAssignableFrom(cls)) {
						Command command;
						try {
							command = (Command) cls.newInstance();
						} catch(InstantiationException ie) {
							Constructor cnstr = cls.getConstructor(new Class[] {jIRCdMBean.class});
							command = (Command) cnstr.newInstance(new Object[] {this});
						}
						commands.put(command.getName().toUpperCase(), command);
						logger.info("...installed "+command.getName()+" ("+className+")");
					}
				} catch(Exception ex) {
					logger.log(Level.WARNING, "Could not load class "+className, ex);
				}
			}
		}
	}
	public void reloadPolicy() {
		logger.info("Refreshing security policy");
		Policy.getPolicy().refresh();
	}
	public void start() {
		logger.info(getVersion()+" starting...");
		startTime = System.currentTimeMillis();

		// start ping timer
		if(pingTimerTask != null) {
			pingTimerTask.cancel();
			pingTimerTask = null;
		}
		pingTimerTask = new PingTimerTask(this);
		final long pingInterval = 1000 * Integer.parseInt(settings.getProperty("jircd.ping.interval", "5"));
		timer.schedule(pingTimerTask, 0, pingInterval);

		startListeners();
	}
	private void startListeners() {
		logger.info("Binding to port(s)...");
		for(Iterator iter = listeners.iterator(); iter.hasNext();) {
			Listener listener = (Listener) iter.next();
			if (listener.bind()) {
				listener.start();
				logger.info("..." + listener.toString() + "...");
			} else {
				iter.remove();
				logger.log(Level.WARNING, "..." + listener.toString() + " (FAILED)...");
			}
		}
		logger.info("...complete");
	}
	/**
	 * Stops this server.
	 * All clients and server links are disconnected.
	 */
	public void stop() {
		logger.info("Stopping...");
		// stop ping timer
		if(pingTimerTask != null) {
			pingTimerTask.cancel();
			pingTimerTask = null;
		}

		// broadcast shutdown notice
		for(Iterator iter = thisServer.getUsers().iterator(); iter.hasNext();) {
			User user = (User) iter.next();
			Message message = new Message(thisServer, "NOTICE", user);
			message.appendParameter("WARNING: Server shut down by local console.");
			user.send(message);
		}

		stopListeners();

		// disconnect clients
		logger.info("Disconnecting clients...");
		synchronized(clients) {
			for(Iterator iter = clients.iterator(); iter.hasNext();) {
				Client client = (Client) iter.next();
				Source src = client.getSource();
				if(src instanceof User)
					thisServer.removeUser((User)src, "Server shutdown");
				client.getConnection().close();
				iter.remove();
			}
		}
		// disconnect links
		logger.info("Disconnecting server links...");
		synchronized(links) {
			for(Iterator iter = links.iterator(); iter.hasNext();) {
				Link link = (Link) iter.next();
				link.getConnection().close();
				iter.remove();
			}
		}

		startTime = -1;
	}
	private void stopListeners() {
		logger.info("Unbinding listeners...");
		for(Iterator iter = listeners.iterator(); iter.hasNext();) {
			Listener listener = (Listener) iter.next();
			listener.close();
		}
	}

	public Set getClients() {
		return Collections.unmodifiableSet(clients);
	}
	public void addClient(Client client) {
		clients.add(client);
	}
	public void disconnectClient(Client client, String reason) {
		Source src = client.getSource();
		if(src instanceof User)
			thisServer.removeUser((User)src, reason);
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
			src = message.resolveSender(((Server)src).getNetwork());
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
		for(int i=0; i<params.length; i++)
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
			logger.log(Level.WARNING, "Error invoking method in " + command.getClass() + " for command " + cmdName, e);
		}
	}

	public Command getCommand(String name) {
		return (Command) commands.get(name.toUpperCase());
	}

	public String getProperty(String key) {
		return settings.getProperty(key);
	}
	public String getProperty(String key, String defaultValue) {
		return settings.getProperty(key, defaultValue);
	}

	public Set getOperators() {
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
		return "jIRCd-" + VERSION_MAJOR + '.' + VERSION_MINOR + '.' + VERSION_PATCH;
	}
	
	public String toString() {
		return "jIRCd";
	}

	protected static class ExtensionFilenameFilter implements FilenameFilter {
		private final String extension;
		public ExtensionFilenameFilter(String ext) {
			extension = "."+ext;
		}
		public boolean accept(File dir, String name) {
			return name.endsWith(extension);
		}
	}
}
