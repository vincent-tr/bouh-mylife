package org.mylife.home.net.hub.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.common.services.Service;
import org.mylife.home.net.hub.IrcServerMBean;
import org.mylife.home.net.hub.configuration.IrcLinkAccept;
import org.mylife.home.net.hub.configuration.IrcLinkConnect;
import org.mylife.home.net.hub.data.DataLink;
import org.mylife.home.net.hub.data.DataLinkAccess;
import org.mylife.home.net.hub.irc.ConnectedEntity;
import org.mylife.home.net.hub.irc.Connection;
import org.mylife.home.net.hub.irc.Server;
import org.mylife.home.net.hub.irc.StreamConnection;
import org.mylife.home.net.hub.irc.UnregisteredEntity;
import org.mylife.home.net.hub.irc.Util;

/**
 * Service de gestion des liens
 * 
 * @author pumbawoman
 * 
 */
public class LinkService implements Service {

	private static final Logger log = Logger.getLogger(LinkService.class
			.getName());

	public final static String TYPE_ACCEPT = "accept";
	public final static String TYPE_CONNECT = "connect";

	private final static int CHECK_INTERVAL = 10000;

	private final Map<String, String> types;

	/* internal */LinkService() {

		Map<String, String> map = new HashMap<String, String>();
		map.put(TYPE_ACCEPT, "Accept");
		map.put(TYPE_CONNECT, "Connect");
		types = Collections.unmodifiableMap(map);
	}

	@Override
	public void terminate() {
	}

	public void create(DataLink link) {
		checkType(link);

		DataLinkAccess access = new DataLinkAccess();
		try {
			access.createLink(link);
			resetCachedLinks();
		} finally {
			access.close();
		}
	}

	public void update(DataLink link) {
		checkType(link);

		DataLinkAccess access = new DataLinkAccess();
		try {
			DataLink item = access.getLinkByKey(link.getId());
			item.setAddress(link.getAddress());
			item.setPort(link.getPort());
			item.setPassword(link.getPassword());
			access.updateLink(item);
			resetCachedLinks();
		} finally {
			access.close();
		}
	}

	public void delete(int id) {
		DataLinkAccess access = new DataLinkAccess();
		try {
			DataLink item = new DataLink();
			item.setId(id);
			access.deleteLink(item);
			resetCachedLinks();
		} finally {
			access.close();
		}
	}

	public List<DataLink> list() {
		DataLinkAccess access = new DataLinkAccess();
		try {
			return access.getLinksAll();
		} finally {
			access.close();
		}
	}

	private void checkType(DataLink link) {
		if (!types.containsKey(link.getType()))
			throw new UnsupportedOperationException("Unknown type");
	}

	public Map<String, String> listTypes() {
		return types;
	}

	// ---------- gestion des liens actifs ----------

	/**
	 * Représentation d'un lien établi entre ce serveur et un autre serveur
	 * 
	 * @author TRUMPFFV
	 * 
	 */
	public static class RunningLink {

		private final Connection connection;

		public RunningLink(Connection connection) {
			this.connection = connection;
		}

		public Server getServer() {
			return (Server) connection.getHandler().getEntity();
		}

		public Connection getConnection() {
			return connection;
		}

		public boolean isConnectLink() {
			return connection.isLocallyInitiated();
		}

		public void disconnect() {
			getServer().disconnect("Closing link from admin console");
		}
	}

	public Set<RunningLink> getRunning() {
		IrcServerMBean ircServer = ServiceAccess.getInstance()
				.getManagerService().getServer();
		if (ircServer == null)
			return null;
		Set<Connection> connections = ircServer.getLinks().getConnections();
		Set<RunningLink> links = new HashSet<RunningLink>();
		for (Connection con : connections) {
			ConnectedEntity entity = con.getHandler().getEntity();
			if (!(entity instanceof Server))
				continue;
			links.add(new RunningLink(con));
		}
		return links;
	}

	public synchronized void refreshRunning() {
		if (autoLinksManager != null)
			autoLinksManager.reset();
	}

	private synchronized void resetCachedLinks() {
		cachedLinksAccept = null;
		cachedLinksConnect = null;
	}

	private Collection<IrcLinkAccept> cachedLinksAccept;
	private Collection<IrcLinkConnect> cachedLinksConnect;

	private void loadLinks() {
		Collection<IrcLinkAccept> linksAccept = new ArrayList<IrcLinkAccept>();
		Collection<IrcLinkConnect> linksConnect = new ArrayList<IrcLinkConnect>();

		List<DataLink> list = list();
		for (DataLink item : list) {
			String type = item.getType();
			if (TYPE_ACCEPT.equalsIgnoreCase(type)) {
				linksAccept.add(new IrcLinkAccept(item.getName(), item
						.getAddress(), item.getPort(), item.getPassword()));
			} else if (TYPE_CONNECT.equalsIgnoreCase(type)) {
				linksConnect.add(new IrcLinkConnect(item.getName(), item
						.getAddress(), item.getPort(), item.getPassword()));
			} else
				log.warning("Unknow link type, ignored : " + type);

		}

		cachedLinksAccept = linksAccept;
		cachedLinksConnect = linksConnect;
	}

	public synchronized Collection<IrcLinkAccept> getLinksAccept() {
		if (cachedLinksAccept == null)
			loadLinks();
		return cachedLinksAccept;
	}

	public synchronized Collection<IrcLinkConnect> getLinksConnect() {
		if (cachedLinksConnect == null)
			loadLinks();
		return cachedLinksConnect;
	}

	// ---------- gestion des connexions de liens ----------

	/**
	 * Gestion des liens automatiques
	 * 
	 * @author TRUMPFFV
	 * 
	 */
	private class AutoLinksManager extends Thread {

		private final IrcServerMBean server;
		private boolean closing = false;
		private Date lastRefresh = null;

		public Date getLastRefresh() {
			return lastRefresh;
		}

		public AutoLinksManager(IrcServerMBean server) {
			this.server = server;
			setName(toString());
			setDaemon(true);
		}

		public void close() {
			closing = true;
			interrupt();

			// Attente de la fin du thread
			try {
				join();
			} catch (InterruptedException ie) {
				log.warning("Interrupted");
			}
		}

		public void reset() {
			interrupt();
		}

		@Override
		public void run() {
			while (!closing) {

				try {
					refresh();
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error refreshing", e);
				}
				lastRefresh = new Date();

				try {
					Thread.sleep(CHECK_INTERVAL);
				} catch (InterruptedException ie) {
					// Interruption : fin d'attente
				}
			}
		}

		private void refresh() throws IOException {

			// Obtention des config à faire
			Collection<IrcLinkConnect> todo = todo();

			// On essaye de se connecter
			for (IrcLinkConnect ilc : todo) {
				log.info("Connecting link : " + ilc.getName());
				try {
					tryConnect(ilc);
					log.info("Link connected : " + ilc.getName());
				} catch (IOException ioe) {
					log.log(Level.WARNING, "Link connection failed : " + ilc.getName(), ioe);
				}
			}
		}

		private void tryConnect(IrcLinkConnect configLink) throws IOException {

			// Repris de irc.commands.Connect
			
			StreamConnection connection = new StreamConnection(new Socket(configLink.getRemoteAddress(),
					configLink.getRemotePort()), server.getLinks(),
					Executors.newSingleThreadExecutor(), true);
			Connection.Handler handler = new Connection.Handler(server,
					connection);
			connection.setHandler(handler);
			connection.start();
			UnregisteredEntity entity = (UnregisteredEntity) handler
					.getEntity();
			Util.sendPass(entity, configLink.getPassword());
			Util.sendServer(entity);
			// so that we know we sent PASS & SERVER
			entity.setParameters(new String[0]); 
		}

		private Collection<IrcLinkConnect> todo() throws IOException {

			// Récupération des liens en fonctionnement
			Set<RunningLink> running = running();

			// Récupération de la config des liens
			Collection<IrcLinkConnect> config = config();

			// Pour chaque config, on cherche si un lien est en cours de
			// fonctionnement
			Collection<IrcLinkConnect> ret = new ArrayList<IrcLinkConnect>();
			for (IrcLinkConnect ilc : config) {
				if (!exists(ilc, running))
					ret.add(ilc);
			}

			return ret;
		}

		private Set<RunningLink> running() {

			// Récupération des liens existants
			Set<RunningLink> source = getRunning();

			// On ne garde que ceux en connect
			Set<RunningLink> ret = new HashSet<RunningLink>();
			for (RunningLink rl : source) {
				if (rl.isConnectLink())
					ret.add(rl);
			}

			return ret;
		}

		private Collection<IrcLinkConnect> config() {
			return getLinksConnect();
		}

		private boolean exists(IrcLinkConnect config, Set<RunningLink> running)
				throws IOException {
			for (RunningLink rl : running) {

				Connection con = rl.getConnection();
				if (con.getRemotePort() != config.getRemotePort())
					continue;

				String confAddr = InetAddress.getByName(
						config.getRemoteAddress()).getHostAddress();
				if (!confAddr.equalsIgnoreCase(con.getRemoteAddress()))
					continue;

				return true;
			}

			return false;
		}
	}

	private AutoLinksManager autoLinksManager;

	public synchronized void startAutoLinks(IrcServerMBean server) {
		autoLinksManager = new AutoLinksManager(server);
		autoLinksManager.start();
	}

	public synchronized void stopAutoLinks() {
		autoLinksManager.close();
		autoLinksManager = null;
	}

	public synchronized Date getAutoLinksLastRefresh() {
		if (autoLinksManager == null)
			return null;

		return autoLinksManager.getLastRefresh();
	}
}
