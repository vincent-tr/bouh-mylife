package org.mylife.home.net.hub.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mylife.home.common.services.Service;
import org.mylife.home.net.hub.data.DataLink;
import org.mylife.home.net.hub.data.DataLinkAccess;
import org.mylife.home.net.hub.irc.IrcConnectHandler;
import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.IrcNetworkAccessHandler;
import org.mylife.home.net.hub.irc.IrcServer;
import org.mylife.home.net.hub.irc.structure.Network;
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

		private final String serverName;
		private final String remoteHost;
		private final boolean locallyInitiated;

		/**
		 * Doit être appelé sur le thread du serveur
		 * 
		 * @param peerServer
		 */
		public RunningLink(Server peerServer) {
			this.serverName = peerServer.getName();
			IrcConnection con = peerServer.getConnection();
			this.remoteHost = con.getRemoteHost();
			this.locallyInitiated = con.getLocallyinitiated();
		}

		public String getServerName() {
			return serverName;
		}

		public String getRemoteHost() {
			return remoteHost;
		}

		public boolean isLocallyInitiated() {
			return locallyInitiated;
		}
	}

	public Set<RunningLink> getRunning() {
		IrcServer ircServer = ServiceAccess.getInstance().getManagerService()
				.getServer();
		if (ircServer == null)
			return null;

		try {
			final Set<RunningLink> links = new HashSet<RunningLink>();
			ircServer.requestNetworkAccess(new IrcNetworkAccessHandler() {
				@Override
				public void execute(Network net) {
					for (Server peerServer : net.getPeerServers()) {
						links.add(new RunningLink(peerServer));
					}
				}
			});
			return links;
		} catch (InterruptedException e) {
			log.warning("Interrupted operation !");
			return null;
		}
	}

	public void closeLink(RunningLink link) {
		IrcServer ircServer = ServiceAccess.getInstance().getManagerService()
				.getServer();
		if (ircServer == null)
			return;

		try {
			ircServer.externalDisconnectServer(link.getServerName());
		} catch (InterruptedException e) {
			log.warning("Interrupted operation !");
		}
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
				}catch (IllegalStateException e) {
					// Le serveur n'est pas encore pret on essayera plus tard
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
				tryConnect(ilc);
			}
		}

		private void tryConnect(final DataLink configLink) {

			log.info("Link connecting : " + configLink.getName());
			try {
				server.connect(configLink.getAddress(), configLink.getPort(),
						new IrcConnectHandler() {
							@Override
							public void connected(IrcConnection connection) {
								log.info("Link success : "
										+ configLink.getName());
							}

							@Override
							public void connectionFailed(
									IrcConnection connection, IOException e) {
								log.warning("Link failed : "
										+ configLink.getName());
							}
						});
			} catch (InterruptedException e) {
				log.warning("Interrupted operation !");
			}
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
				if (rl.isLocallyInitiated())
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
				if (rl.getServerName().equalsIgnoreCase(config.getName()))
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
