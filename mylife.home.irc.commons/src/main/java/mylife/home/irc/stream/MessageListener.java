package mylife.home.irc.stream;

import mylife.home.irc.message.Message;

/**
 * Listener de message
 * @author pumbawoman
 *
 */
public interface MessageListener {
	
	/**
	 * Traitement du message
	 * @param message
	 */
	public void messageReceived(Message message);
}
