package mylife.home.irc.stream;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import mylife.home.irc.message.Message;
import mylife.home.irc.message.Prefix;
import mylife.home.irc.message.ServerPrefix;
import mylife.home.irc.message.User;
import mylife.home.irc.message.UserPrefix;
import mylife.home.irc.net.ConnectionManager;
import mylife.home.irc.net.DataConnection;

/**
 * Construction d'un flux à partir d'une connexion de données
 * 
 * @author pumbawoman
 * 
 */
public class Stream extends DataConnection {

	private static final Charset encoding = Charset.defaultCharset();
	private static final String messageSeparator = "\r\n";

	private final List<CloseListener> closeListeners = new ArrayList<CloseListener>();
	private final List<ErrorListener> errorListeners = new ArrayList<ErrorListener>();
	private final List<MessageListener> sendListeners = new ArrayList<MessageListener>();
	private final List<MessageListener> receiveListeners = new ArrayList<MessageListener>();

	/**
	 * Ajout d'un listener sur fermeture
	 * 
	 * @param listener
	 */
	public void addCloseListener(CloseListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener can not be null");
		synchronized (closeListeners) {
			closeListeners.add(listener);
		}
	}

	/**
	 * Suppression d'un listener sur fermeture
	 * 
	 * @param listener
	 */
	public void removeCloseListener(CloseListener listener) {
		synchronized (closeListeners) {
			closeListeners.remove(listener);
		}
	}

	/**
	 * Exécution des listeners de fermeture
	 */
	protected void executeCloseListeners() {
		synchronized (closeListeners) {
			for (CloseListener listener : closeListeners)
				listener.closed();
		}
	}

	/**
	 * Ajout d'un listener sur erreur
	 * 
	 * @param listener
	 */
	public void addErrorListener(ErrorListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener can not be null");
		synchronized (errorListeners) {
			errorListeners.add(listener);
		}
	}

	/**
	 * Suppression d'un listener sur erreur
	 * 
	 * @param listener
	 */
	public void removeErrorListener(ErrorListener listener) {
		synchronized (errorListeners) {
			errorListeners.remove(listener);
		}
	}

	/**
	 * Exécution des listeners d'erreur
	 * 
	 * @param e
	 */
	protected void executeErrorListeners(Exception e) {
		synchronized (errorListeners) {
			for (ErrorListener listener : errorListeners)
				listener.errorOccured(e);
		}
	}

	/**
	 * Ajout d'un listener sur envoi
	 * 
	 * @param listener
	 */
	public void addSendListener(MessageListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener can not be null");
		synchronized (sendListeners) {
			sendListeners.add(listener);
		}
	}

	/**
	 * Suppression d'un listener sur envoi
	 * 
	 * @param listener
	 */
	public void removeSendListener(MessageListener listener) {
		synchronized (sendListeners) {
			sendListeners.remove(listener);
		}
	}

	/**
	 * Exécution des listeners d'envoi
	 * 
	 * @param message
	 */
	protected void executeSendListener(Message message) {
		synchronized (sendListeners) {
			for (MessageListener listener : sendListeners)
				listener.messageReceived(message);
		}
	}

	/**
	 * Ajout d'un listener sur réception
	 * 
	 * @param listener
	 */
	public void addReceiveListener(MessageListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener can not be null");
		synchronized (receiveListeners) {
			receiveListeners.add(listener);
		}
	}

	/**
	 * Suppression d'un listener sur réception
	 * 
	 * @param listener
	 */
	public void removeReceiveListener(MessageListener listener) {
		synchronized (receiveListeners) {
			receiveListeners.remove(listener);
		}
	}

	/**
	 * Exécution des listeners de réception
	 * 
	 * @param message
	 */
	protected void executeReceiveListener(Message message) {
		synchronized (receiveListeners) {
			for (MessageListener listener : receiveListeners)
				listener.messageReceived(message);
		}
	}

	/**
	 * Construction du flux
	 * 
	 * @param connection
	 */
	public Stream(ConnectionManager owner, SocketChannel channel,
			int recvBufferSize, int sendBufferSize) {
		super(owner, channel, recvBufferSize, sendBufferSize);
	}

	private String messageBuffer = "";

	@Override
	protected void onReceive(byte[] data) {
		// lecture des données en chaine
		String value = new String(data, encoding);
		// ajout du nouveau buffer à la fin de l'ancien
		messageBuffer += value;

		// on obtient les messages du buffer
		int index;
		while ((index = messageBuffer.indexOf(messageSeparator)) > -1) {
			String messageData = messageBuffer.substring(0, index);
			messageBuffer = messageBuffer.substring(index + 2);

			// parsing
			processMessage(messageData);
		}

		// on laisse la fin du buffer si besoin
	}

	private void processMessage(String data) {

		Message message = null;

		try {
			MessageParser parser = new MessageParser(data);

			Prefix prefix = null;
			if (parser.getNick() != null)
				prefix = new UserPrefix(new User(parser.getNick(),
						parser.getUserName(), parser.getHostName()));
			else if (parser.prefix() != null)
				prefix = new ServerPrefix(parser.prefix());

			if (parser.command() == null)
				throw new IllegalArgumentException("command can not be null");

			message = new Message(prefix, parser.command().toUpperCase(), parser.args());

		} catch (Exception e) {
			try {
				throw new MessageParsingException(e);
			} catch (MessageParsingException mpe) {
				executeErrorListeners(mpe);
			}
			return;
		}

		executeReceiveListener(message);
	}

	@Override
	protected void onClose() {
		executeCloseListeners();
	}

	@Override
	public void close() throws IOException {
		super.close();
		executeCloseListeners();
	}

	@Override
	protected void onError(Exception e) {
		executeErrorListeners(e);
	}

	/**
	 * Envoi d'un message
	 * 
	 * @param message
	 */
	public void send(Message message) {
		if (message == null)
			throw new IllegalArgumentException("message can not be null");

		byte[] data = (message.toString() + messageSeparator)
				.getBytes(encoding);
		send(data);
		executeSendListener(message);
	}
}
