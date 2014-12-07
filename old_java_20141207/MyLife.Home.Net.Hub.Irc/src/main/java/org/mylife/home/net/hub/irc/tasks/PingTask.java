package org.mylife.home.net.hub.irc.tasks;

import org.apache.commons.lang3.RandomStringUtils;
import org.mylife.home.net.hub.irc.IrcConnection;
import org.mylife.home.net.hub.irc.protocol.Message;

public class PingTask implements Runnable {

	private static final int PING_INTERVAL = 30000;
	private static final int PING_TIMEOUT = 30000;

	private final IrcConnection connection;
	private long lastSendPing;
	private String pongWaitingValue;

	public PingTask(IrcConnection connection) {
		this.connection = connection;
		this.lastSendPing = 0;
	}

	@Override
	public void run() {
		long now = System.currentTimeMillis();
		if (pongWaitingValue != null) {
			// Attente d'une réponse de ping
			if (now >= lastSendPing + PING_TIMEOUT)
				pingTimeout();
		} else {
			// On regarde si on doit envoyer un ping
			if (now >= lastSendPing + PING_INTERVAL) {
				sendPing();
				lastSendPing = now;
			}
		}
	}

	private void pingTimeout() {
		connection.close(); // ping timeout
	}

	private void sendPing() {
		pongWaitingValue = RandomStringUtils.randomAlphanumeric(10);
		Message msg = new Message("PING");
		msg.appendLastParameter(pongWaitingValue);
		connection.send(msg);
	}

	public void pong(String value) {
		if (pongWaitingValue == null)
			return;
		if (pongWaitingValue.equals(value)) {
			// Réponse OK, fin de l'attente
			pongWaitingValue = null;
		}
	}

}
