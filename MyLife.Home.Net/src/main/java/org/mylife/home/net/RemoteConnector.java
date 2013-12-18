package org.mylife.home.net;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetMember;

/**
 * Connecteur pour un objet distant
 * 
 * @author pumbawoman
 * 
 */
class RemoteConnector implements Connector, ActionExecutor {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(RemoteConnector.class
			.getName());

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

	private final String[] attributesByIndexes;

	/**
	 * Construction du connecteur avec l'objet
	 * 
	 * @param object
	 * @param channel
	 */
	public RemoteConnector(NetContainer container) {
		this.container = container;

		synchronized (connectors) {
			connectors.add(this);
			if (connectors.size() == 1)
				watcher = new NetWatcher();
			watcher.addChannel(container.getChannel());
		}

		NetObject obj = container.getObject();
		List<String> attributes = new ArrayList<String>();

		for (NetMember member : obj.getNetClass().getMembers()) {
			String name = member.getName();

			if (member instanceof NetAttribute) {
				attributes.add(name);
			}

			if (member instanceof NetAction) {
				obj.setActionExecutor(name, this);
			}
		}

		attributesByIndexes = attributes.toArray(new String[attributes.size()]);
	}

	/**
	 * Fermeture du connecteur
	 */
	@Override
	public void close() {

		synchronized (connectors) {
			watcher.removeChannel(container.getChannel());
			if (connectors.size() == 0) {
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

	private void setAttributes(List<String> attributes) {

		NetObject obj = container.getObject();

		if (attributes.size() != attributesByIndexes.length)
			log.warning("The count of attributes on net is different than the count of attributes on the object class !");
		int len = Math.min(attributes.size(), attributesByIndexes.length);

		for (int i = 0; i < len; i++) {
			obj.setAttributeValueAsString(attributesByIndexes[i],
					attributes.get(i));
		}
	}

	private void setConnected() {
		container.setConnected(true);
	}

	private void setDisconnected() {
		// déconnexion et marquage de tous les attributs à false
		container.setConnected(false);

		for (int i = 0; i < attributesByIndexes.length; i++) {
			container.getObject().setAttributeValue(attributesByIndexes[i],
					null);
		}
	}

	/**
	 * Implémentation de ActionExecutor
	 */
	public void execute(NetObject obj, NetAction action, Object[] arguments) {
		StringBuffer buffer = new StringBuffer();
		for (Object arg : arguments) {
			if (buffer.length() > 0)
				buffer.append(' ');
			buffer.append(String.valueOf(arg));
		}
		String message = action.getName() + " " + buffer.toString();
		watcher.send(container.getChannel(), obj.getId(), message);
	}

	/**
	 * Trouve le connecteur par nick/channel. Si le channel n'est pas spécifié,
	 * seul le nick est matché
	 * 
	 * @param nick
	 * @param channel
	 * @return
	 */
	private static RemoteConnector findByNickChannel(String nick, String channel) {
		int sepIndex;
		String id = nick;
		if ((sepIndex = id.indexOf('|')) > 0) {
			id = id.substring(0, sepIndex);
		}

		for (RemoteConnector connector : connectors) {
			if (!id.equalsIgnoreCase(connector.container.getObject().getId()))
				continue;

			if (channel != null
					&& !channel.equalsIgnoreCase(connector.container
							.getChannel()))
				continue;

			return connector;
		}

		return null;
	}

	private static void setNickAttributes(String nick, RemoteConnector connector) {

		// parsing des éléménts du nouveau nick
		StringTokenizer tokenizer = new StringTokenizer(nick, "|");

		// le 1er token est l'id, le reste les attributs
		tokenizer.nextToken();
		List<String> attributes = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			attributes.add(tokenizer.nextToken());
		}

		if (attributes.size() > 0)
			connector.setAttributes(attributes);
	}

	/**
	 * Connexion
	 * 
	 * @param nick
	 * @param channel
	 */
	public static void nickJoin(String nick, String channel) {
		synchronized (connectors) {
			RemoteConnector connector = findByNickChannel(nick, channel);
			if (connector == null)
				return;

			connector.setConnected();
			setNickAttributes(nick, connector);
		}
	}

	/**
	 * Changement de nick
	 * 
	 * @param oldNick
	 * @param newNick
	 */
	public static void nickChanged(String oldNick, String newNick) {
		synchronized (connectors) {
			RemoteConnector connector = findByNickChannel(oldNick, null);
			if (connector == null)
				return;

			setNickAttributes(newNick, connector);
		}
	}

	/**
	 * Déconnexion (si nick == null alors déco globale)
	 * 
	 * @param nick
	 * @param channel
	 */
	public static void nickPart(String nick, String channel) {
		synchronized (connectors) {
			if (nick == null) {
				// déconnexion globale
				for (RemoteConnector connector : connectors) {
					connector.setDisconnected();
				}
			} else {
				RemoteConnector connector = findByNickChannel(nick, null);
				if (connector == null)
					return;
				connector.setDisconnected();
			}
		}
	}
}
