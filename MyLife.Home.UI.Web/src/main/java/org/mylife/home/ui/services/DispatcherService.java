package org.mylife.home.ui.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.Session;

import org.mylife.home.common.services.Service;
import org.mylife.home.net.AttributeChangeListener;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetMember;
import org.mylife.home.ui.structure.Component;
import org.mylife.home.ui.structure.Structure;
import org.mylife.home.ui.structure.Window;

public class DispatcherService implements Service, AttributeChangeListener {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(DispatcherService.class
			.getName());

	/* internal */DispatcherService() {

	}

	@Override
	public void terminate() {
	}

	/**
	 * Gestion des dépendances
	 */
	private final Map<String, Collection<Component>> dependencies = new HashMap<String, Collection<Component>>();

	public void structureChanged(XmlUiContainer structure) {

		dependencies.clear();

		if (structure == null)
			log.info("Structure deleted ");
		else
			log.info("New structure : " + structure.documentName
					+ " (Version : " + structure.documentVersion + ")");

		// Mise à jour de la structure
		Structure.update(structure);

		// Mise à jour des dépendances
		for (Window window : Structure.getWindows()) {
			for (Component component : window.getComponents()) {
				for (String objId : component.getDependencies()) {
					Collection<Component> col = dependencies.get(objId);
					if (col == null)
						dependencies.put(objId,
								col = new ArrayList<Component>());
					col.add(component);
				}
			}
		}

		// Propagation de la modification aux clients connectés
		SessionHandler[] localSessions = sessions
				.toArray(new SessionHandler[0]);
		for (SessionHandler session : localSessions) {
			session.sendStructureChanged();
		}
	}

	public void objectNew(NetObject obj) {
		log.info("New object : " + obj.getId());

		for (NetMember member : obj.getNetClass().getMembers()) {
			if (!(member instanceof NetAttribute))
				continue;
			obj.registerAttributeChange(member.getName(), this);
		}
	}

	public void objectDeleted(NetObject obj) {
		log.info("Object deleted : " + obj.getId());

		for (NetMember member : obj.getNetClass().getMembers()) {
			if (!(member instanceof NetAttribute))
				continue;
			obj.unregisterAttributeChange(member.getName(), this);
		}
	}

	public void objectOnlineChanged(NetObject obj, boolean online) {
		log.info("Object " + (online ? "online" : "offline") + " : "
				+ obj.getId());

		Collection<Component> components = dependencies.get(obj.getId());
		if (components != null) {
			for (Component component : components) {
				component.objectOnlineChanged(obj, online);
			}
		}
	}

	@Override
	public void attributeChanged(NetObject obj, NetAttribute attribute,
			Object value) {

		Collection<Component> components = dependencies.get(obj.getId());
		if (components != null) {
			for (Component component : components) {
				component.objectAttributeChanged(obj, attribute, value);
			}
		}
	}

	public void componentIconChanged(Component component, String iconId) {
		Window window = component.getOwner();
		SessionHandler[] localSessions = sessions
				.toArray(new SessionHandler[0]);
		for (SessionHandler session : localSessions) {
			if (session.hasWindow(window))
				session.sendIcon(window.getId(), component.getId(), iconId);
		}
	}

	public void componentOnlineChanged(Component component, boolean online) {
		Window window = component.getOwner();
		SessionHandler[] localSessions = sessions
				.toArray(new SessionHandler[0]);
		for (SessionHandler session : localSessions) {
			if (session.hasWindow(window))
				session.sendOnlineChanged(window.getId(), component.getId(),
						online);
		}
	}

	private static final String SESSION_HANDLER_KEY = "handler-key";

	public void sessionOpen(Session session) throws IOException {
		SessionHandler handler = new SessionHandler(session);
		sessions.add(handler);
		session.getUserProperties().put(SESSION_HANDLER_KEY, handler);
	}

	public void sessionClose(Session session) {
		SessionHandler handler = (SessionHandler) session.getUserProperties()
				.remove(SESSION_HANDLER_KEY);
		if (handler != null) {
			handler.close();
			sessions.remove(handler);
		}
	}

	public void sessionMessage(String message, Session session) {
		log.log(Level.INFO, "Session message : " + message);
		SessionHandler handler = (SessionHandler) session.getUserProperties()
				.remove(SESSION_HANDLER_KEY);
		handler.receive(message);
	}

	private final Collection<SessionHandler> sessions = Collections
			.synchronizedCollection(new ArrayList<SessionHandler>());

	/**
	 * Gestion d'une session
	 * 
	 * @author pumbawoman
	 * 
	 */
	private static class SessionHandler {

		/**
		 * Session websocket
		 */
		private final Session session;

		/**
		 * Liste des fenêtres couramment visibles dans la session
		 */
		private Collection<Window> windows = new ArrayList<Window>();

		public SessionHandler(Session session) {
			this.session = session;
		}

		/**
		 * La session est fermée
		 */
		public void close() {

		}

		/*
		 * Format de message :
		 * 
		 * c -> s : window window1.id window2.id
		 * 
		 * c -> s : action window.id component.id primary/secondary (seulement
		 * si core action)
		 * 
		 * s -> c : icon window.id component.id image.id
		 * 
		 * s -> c : online window.id component.id
		 * 
		 * s -> c : offline window.id component.id
		 * 
		 * s -> c : structureChanged
		 */

		/**
		 * Reception d'un message
		 * 
		 * @param message
		 */
		public void receive(String message) {
			StringTokenizer tokenizer = new StringTokenizer(message, " ");
			List<String> args = new ArrayList<String>();
			while (tokenizer.hasMoreTokens())
				args.add(tokenizer.nextToken());

			if (args.size() < 1) {
				log.warning("No command specified");
				return;
			}

			String command = args.remove(0);
			if ("window".equalsIgnoreCase(command)) {
				receiveWindow(args);
			} else if ("action".equalsIgnoreCase(command)) {
				receiveAction(args);
			} else {
				log.warning("Unknown command : " + command);
			}
		}

		private void receiveWindow(List<String> args) {
			windows.clear();
			for (String windowId : args) {
				Window window = Structure.getWindow(windowId);
				if (window == null) {
					log.warning("Window id not existing : " + windowId);
					continue;
				}

				windows.add(window);
			}

			// Envoi du statut des fenêtres
			for (Window window : windows) {
				for (Component component : window.getComponents()) {
					boolean online = component.isOnline();
					sendOnlineChanged(window.getId(), component.getId(), online);
					if (online)
						sendIcon(window.getId(), component.getId(),
								component.getIconId());
				}
			}
		}

		private void receiveAction(List<String> args) {
			if (args.size() < 3) {
				log.warning("Action not enough arguments");
				return;
			}

			String windowId = args.remove(0);
			String componentId = args.remove(0);
			String actionType = args.remove(0);
			int type = -1;

			if ("primary".equalsIgnoreCase(actionType)) {
				type = Component.ACTION_PRIMARY;
			} else if ("secondary".equalsIgnoreCase(actionType)) {
				type = Component.ACTION_SECONDARY;
			} else {
				log.warning("Invalid action type : " + actionType);
				return;
			}

			Window window = Structure.getWindow(windowId);
			if (window == null) {
				log.warning("Window not found : " + windowId);
				return;
			}

			Component component = window.getComponent(componentId);
			if (component == null) {
				log.warning("Component not found : " + windowId + "/"
						+ componentId);
				return;
			}

			component.actionExecuteCore(type);
		}

		/**
		 * Envoi d'un message
		 * 
		 * @param message
		 * @throws IOException
		 */
		public void send(String message) {
			try {
				session.getBasicRemote().sendText(message);
			} catch (IOException ioe) {
				log.log(Level.SEVERE, "Error sending message", ioe);
			}
		}

		public void sendIcon(String windowId, String componentId, String iconId) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("icon ");
			buffer.append(windowId);
			buffer.append(' ');
			buffer.append(componentId);
			buffer.append(' ');
			buffer.append(iconId);
			send(buffer.toString());
		}

		public void sendOnlineChanged(String windowId, String componentId,
				boolean online) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(online ? "online " : "offline ");
			buffer.append(windowId);
			buffer.append(' ');
			buffer.append(componentId);
			send(buffer.toString());
		}

		public void sendStructureChanged() {
			send("structureChanged");
		}

		public boolean hasWindow(Window window) {
			return windows.contains(window);
		}
	}
}
