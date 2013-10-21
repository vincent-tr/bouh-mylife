package org.mylife.home.net;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetMember;

/**
 * Connecteur pour un objet distant
 * @author pumbawoman
 *
 */
class RemoteConnector implements Connector {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(RemoteConnector.class.getName());
	
	/**
	 * Liste des connecteurs enregistrés
	 */
	private static final List<RemoteConnector> connectors = new ArrayList<RemoteConnector>();
	
	/**
	 * Watcher réseau utilisé
	 */
	private static NetWatcher watcher;
	
	/**
	 * Conteneur publié
	 */
	private final NetContainer container;
	
	/**
	 * Construction du connecteur avec l'objet
	 * 
	 * @param object
	 * @param channel
	 */
	public RemoteConnector(NetContainer container) {
		this.container = container;

		synchronized(connectors) {
			connectors.add(this);
			if(connectors.size() == 1)
				watcher = new NetWatcher();
			// TODO : add sur watcher
		}
		// TODO
	}

	/**
	 * Fermeture du connecteur
	 */
	@Override
	public void close() {

		synchronized(connectors) {
			// TODO : remove sur watcher
			if(connectors.size() == 0) {
				watcher.close();
				watcher = null;
			}
			connectors.remove(this);
		}
		
		NetObject object = container.getObject();
		for (NetMember member : object.getNetClass().getMembers()) {
			if (!(member instanceof NetAction))
				continue;
			NetAction action = (NetAction) member;
			String name = action.getName();
			object.setActionExecutor(name, null);
		}
	}
}
