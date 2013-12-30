package org.mylife.home.ui.web.old;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.mylife.home.ui.services.ServiceAccess;

/**
 * Gestion de la communication
 * 
 * @author pumbawoman
 * 
 */
@ServerEndpoint("/net")
public class WebEndpoint {
	
	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(WebEndpoint.class
			.getName());
	
	@OnOpen
	public void onOpen(Session session) throws IOException {
		log.info("Session opened");
		ServiceAccess.getInstance().getDispatcherService().sessionOpen(session);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		log.info("Session closed : " + closeReason.toString());
		ServiceAccess.getInstance().getDispatcherService().sessionClose(session);
	}

	@OnError
	public void onError(Session session, Throwable throwable) {
		log.log(Level.SEVERE, "Session error!", throwable);
	}

	@OnMessage
	public void onMessage(String message, Session session) {
		log.log(Level.INFO, "Session message : " + message);
		ServiceAccess.getInstance().getDispatcherService().sessionMessage(message, session);
	}
}
