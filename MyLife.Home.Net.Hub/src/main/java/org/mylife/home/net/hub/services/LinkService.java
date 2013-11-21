package org.mylife.home.net.hub.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	private final Map<String, String> types;

	/* internal */LinkService() {

		Map<String, String> map = new HashMap<String, String>();
		map.put("accept", "Accept");
		map.put("connect", "Connect");
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
			item.setRetryInterval(link.getRetryInterval());
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

	public void refreshRunning() {

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
						.getAddress(), item.getPort(), item.getPassword(), item
						.getRetryInterval()));
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
	private static class AutoLinksManager extends Thread {

		private boolean closing = false;

		public AutoLinksManager() {
			setName(toString());
			setDaemon(true);
		}

		public void close() {
			closing = true;
			interrupt();
		}
		
		public void reset() {
			
		}

		@Override
		public void run() {
			while (!closing) {
				int nextTime = checkConnect();
				try {
					Thread.sleep(nextTime);
				} catch (InterruptedException ie) {
					// Interruption : fin d'attente
				}
			}
		}

		private int checkConnect() {

		}
	}

	private AutoLinksManager autoLinksManager;

	public void startAutoLinks() {
		autoLinksManager = new AutoLinksManager();
		autoLinksManager.start();
	}

	public void stopAutoLinks() {
		autoLinksManager.close();
		autoLinksManager = null;
	}
}
