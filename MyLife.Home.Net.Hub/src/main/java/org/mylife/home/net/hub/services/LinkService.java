package org.mylife.home.net.hub.services;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.common.services.Service;
import org.mylife.home.net.hub.data.DataLink;
import org.mylife.home.net.hub.data.DataLinkAccess;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Server;

/**
 * Service de gestion des liens
 * 
 * @author pumbawoman
 * 
 */
public class LinkService implements Service {

	private static final Logger log = Logger.getLogger(LinkService.class
			.getName());

	private final static int CHECK_INTERVAL = 10000;

	/* internal */LinkService() {
	}

	@Override
	public void terminate() {
	}

	public void create(DataLink link) {

		DataLinkAccess access = new DataLinkAccess();
		try {
			access.createLink(link);
			resetCachedConfig();
		} finally {
			access.close();
		}
	}

	public void update(DataLink link) {

		DataLinkAccess access = new DataLinkAccess();
		try {
			DataLink item = access.getLinkByKey(link.getId());
			item.setAddress(link.getAddress());
			item.setPort(link.getPort());
			access.updateLink(item);
			resetCachedConfig();
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
			resetCachedConfig();
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

	// ---------- gestion des liens actifs ----------

	/**
	 * Représentation d'un lien établi entre ce serveur et un autre serveur
	 * 
	 * @author TRUMPFFV
	 * 
	 */
	public static class RunningLink {

		private final String server;
		private final String remoteHost;
		private final int remotePort;
		
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
		IrcServer ircServer = ServiceAccess.getInstance()
				.getManagerService().getServer();
		if (ircServer == null)
			return null;
		Set<Server> servers = ircServer.getServerLinks();
		Set<RunningLink> links = new HashSet<RunningLink>();
		for (Server server : servers) {
			links.add(new RunningLink(server.getHandler().getConnection()));
		}
		return links;
	}

	public synchronized void refreshRunning() {
		if (autoLinksManager != null)
			autoLinksManager.reset();
	}

	private synchronized void resetCachedConfig() {
		cachedConfig = null;
	}

	private Collection<DataLink> cachedConfig;

	public synchronized Collection<DataLink> getConfig() {
		if (cachedConfig == null)
			cachedConfig = list();
		return cachedConfig;
	}

	// ---------- gestion des connexions de liens ----------

	/**
	 * Gestion des liens automatiques
	 * 
	 * @author TRUMPFFV
	 * 
	 */
	private class AutoLinksManager extends Thread {

		private final IrcServer server;
		private boolean closing = false;
		private Date lastRefresh = null;

		public Date getLastRefresh() {
			return lastRefresh;
		}

		public AutoLinksManager(IrcServer server) {
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
			Collection<DataLink> todo = todo();

			// On essaye de se connecter
			for (DataLink ilc : todo) {
				log.info("Connecting link : " + ilc.getName());
				try {
					tryConnect(ilc);
					log.info("Link connected : " + ilc.getName());
				} catch (IOException ioe) {
					log.log(Level.WARNING,
							"Link connection failed : " + ilc.getName(), ioe);
				}
			}
		}

		private void tryConnect(DataLink configLink) throws IOException {

			// Repris de irc.commands.Connect

			StreamConnection connection = new StreamConnection(new Socket(
					configLink.getRemoteAddress(), configLink.getRemotePort()),
					server.getConnectLinks(), Executors.newSingleThreadExecutor(),
					true);
			Connection.Handler handler = new Connection.Handler(server,
					connection);
			connection.setHandler(handler);
			connection.start();
			UnregisteredEntity entity = (UnregisteredEntity) handler
					.getEntity();
			//Util.sendPass(entity, configLink.getPassword());
			Util.sendServer(entity);
			// so that we know we sent PASS & SERVER
			entity.setParameters(new String[0]);
		}

		private Collection<DataLink> todo() throws IOException {

			// Récupération des liens en fonctionnement
			Set<RunningLink> running = running();

			// Récupération de la config des liens
			Collection<DataLink> config = config();

			// Pour chaque config, on cherche si un lien est en cours de
			// fonctionnement
			Collection<DataLink> ret = new ArrayList<DataLink>();
			for (DataLink ilc : config) {
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

		private Collection<DataLink> config() {
			return getConfig();
		}

		private boolean exists(DataLink config, Set<RunningLink> running)
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

	public synchronized void startAutoLinks(IrcServer server) {
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
