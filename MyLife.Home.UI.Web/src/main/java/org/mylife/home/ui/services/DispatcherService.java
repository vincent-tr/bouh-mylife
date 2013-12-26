package org.mylife.home.ui.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.Session;

import org.mylife.home.common.services.Service;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.ui.structure.Window;
import org.mylife.home.ui.structure.Windows;

public class DispatcherService implements Service {

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

	public void structureChanged(XmlUiContainer structure) {
		if (structure == null)
			log.info("Structure deleted ");
		else
			log.info("New structure : " + structure.documentName
					+ " (Version : " + structure.documentVersion + ")");
	}

	public void objectNew(NetObject obj) {
		log.info("New object : " + obj.getId());
	}

	public void objectDeleted(NetObject obj) {
		log.info("Object deleted : " + obj.getId());
	}

	public void objectOnline(NetObject obj) {
		log.info("Object online : " + obj.getId());
	}

	public void objectOffline(NetObject obj) {
		log.info("Object offline : " + obj.getId());
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

		/*
		 * Format de message : 
		 * 
		 * c -> s : window window1.id window2.id
		 * c -> s : action component.id actionName arg1 arg2 arg3
		 * 
		 * s -> c : attribute component.id attributeName newValue
		 * s -> c : online component.id
		 * s -> c : offline component.id
		 * s -> c : structureChanged
		 */

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

		/**
		 * Reception d'un message
		 * @param message
		 */
		public void receive(String message) {
			StringTokenizer tokenizer = new StringTokenizer(message, " ");
			List<String> args = new ArrayList<String>();
			while(tokenizer.hasMoreTokens())
				args.add(tokenizer.nextToken());
			
			if(args.size() < 1) {
				log.warning("No command specified");
				return;
			}
			
			String command = args.remove(0);
			if("window".equalsIgnoreCase(command)) {
				receiveWindow(args);
			} else if("action".equalsIgnoreCase(command)) {
				receiveAction(args);
			} else {
				log.warning("Unknown command : " + command);
			}
		}
		
		private void receiveWindow(List<String> args) {
			windows.clear();
			for(String windowId : args) {
				Window window = Windows.getWindow(windowId);
				if(window == null) {
					log.warning("Window id not existing : " + windowId);
					continue;
				}
					
				windows.add(window);
			}
			
			// TODO : envoyer tous les attributs des fenêtres nouvellement courantes
		}
		
		private void receiveAction(List<String> args) {
			if(args.size() < 2) {
				log.warning("Action not enough arguments");
				return;
			}
			
			String componentId = args.remove(0);
			String actionName = args.remove(0);
			
			// TODO
		}
		
		/**
		 * Envoi d'un message
		 * @param message
		 * @throws IOException
		 */
		public void send(String message) {
			try {
				session.getBasicRemote().sendText(message);
			} catch(IOException ioe) {
				log.log(Level.SEVERE, "Error sending message", ioe);
			}
		}
		
		public void sendAttribute(String componentId, String attributeName, Object newValue) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("attribute ");
			buffer.append(componentId);
			buffer.append(' ');
			buffer.append(attributeName);
			buffer.append(' ');
			buffer.append(String.valueOf(newValue));
			send(buffer.toString());
		}
		
		public void sendOnline(String componentId) {
			send("online " + componentId);
		}
		
		public void sendOffline(String componentId) {
			send("offline " + componentId);
		}
		
		public void sendStructureChanged() {
			send("structureChanged");
		}
	}
}
