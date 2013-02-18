package mylife.home.net.xmpp;

import java.util.HashMap;
import java.util.Map;

import mylife.home.net.api.Command;
import mylife.home.net.api.CommandListener;
import mylife.home.net.api.NetComponent;
import mylife.home.net.api.Tokenizer;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Implémentation du composant de communication 
 * @author pumbawoman
 */
public class NetComponentImpl implements NetComponent {

	private final NetComponentService owner;
	private final String componentId;
	private final String componentDisplay;
	private final String componentType;
	private String status;
	private Configuration configuration;
	private boolean closed;
	private final Object managementLock = new Object();
	private Connection connection;
	private MultiUserChat room;
	private final Map<String, CommandListener> commandListeners = new HashMap<String, CommandListener>();
	
	/**
	 * Constructeur avec initialisation des données
	 * @param owner
	 * @param configuration
	 * @param componentId
	 * @param componentDisplay
	 * @param componentType
	 */
	public NetComponentImpl(NetComponentService owner, Configuration configuration, String componentId, String componentDisplay, String componentType) {
		this.owner = owner;
		this.configuration = configuration;
		this.componentId = componentId;
		this.componentDisplay = componentDisplay;
		this.componentType = componentType;
		
		reset();
	}
	
	@Override
	public String getComponentId() {
		return componentId;
	}

	@Override
	public String getComponentDisplay() {
		return componentDisplay;
	}

	@Override
	public String getComponentType() {
		return componentType;
	}

	@Override
	public boolean isOnline() {
		Connection con = this.connection;
		return con != null && con.isAuthenticated();
	}

	@Override
	public void setStatus(String value) {
		checkClosed();
		status = value;
		sendStatus();
	}

	@Override
	public String getStatus() {
		return status;
	}

	@Override
	public boolean registerCommand(String verb, CommandListener listener) {
		
		if(verb == null)
			throw new IllegalArgumentException("verb == null");
		if(listener == null)
			throw new IllegalArgumentException("listener == null");
		
		checkClosed();
		
		synchronized(commandListeners) {
			if(commandListeners.containsKey(verb))
				return false;
			commandListeners.put(verb, listener);
			return true;
		}
	}

	@Override
	public boolean unregisterCommand(String verb) {

		if(verb == null)
			throw new IllegalArgumentException("verb == null");

		synchronized(commandListeners) {
			return commandListeners.remove(verb) != null;
		}
	}

	/**
	 * Vérifie si l'objet est fermé
	 */
	protected void checkClosed() {
		if (closed)
			throw new IllegalStateException("Object closed");
	}

	@Override
	public void close() {
		synchronized(managementLock) {
			checkClosed();
			closeConnection();
			owner.closeComponent(this);
			closed = true;
		}
	}

	/**
	 * Appelé sur configuration modifiée
	 * @param configuration
	 */
	public void changeConfiguration(Configuration configuration) {
		this.configuration = configuration;
		
		reset();
	}

	/**
	 * Fermeture de la connexion et des ressources associées
	 */
	private void closeConnection() {
		if(connection != null) {
			connection.disconnect();
			connection = null;
			room = null;
		}
	}
	
	/**
	 * Envoi du statut
	 */
	private void sendStatus() {
		synchronized(managementLock) {
			if(connection == null)
				return;
			Presence presence = new Presence(Type.available);
			presence.setStatus(componentType + ":" + status);
			connection.sendPacket(presence);
		}
	}
	
	/**
	 * RAZ de la connexion
	 */
	private void reset() {
		synchronized(managementLock) {
			
			closeConnection();
			
			try {
				connection = new XMPPConnection(configuration.xmppServer());
				connection.connect();
				connection.login(componentId, null);
				connection.addConnectionListener(connectionListenerInstance);
				connectionInit();
			}
			catch(XMPPException ex) {
				// Logs
				connection = null;
				room = null;
				throw new RuntimeException("Unexcepted XMPP exception", ex);
			}
		}
	}
	
	/**
	 * Appelé pour initialisation de la connexion
	 * @throws XMPPException 
	 */
	private void connectionInit() throws XMPPException {
		synchronized(managementLock) {
			connection.getChatManager().addChatListener(chatListenerInstance);
			sendStatus();
			room = new MultiUserChat(connection, configuration.mucRoom());
			room.join(componentDisplay);
		}
	}
	
	/**
	 * Gestion d'un message de chat
	 * @param chat
	 * @param message
	 */
	private void processChatMessage(Chat chat, Message message) {
		String data = message.getBody();
		if(data == null)
			return;
		int idx = data.indexOf(' ');
		if(idx == -1)
			return;
		String verb = data.substring(0, idx);
		String args = data.substring(idx);
		Command cmd = new Command(verb, new Tokenizer(args));
		synchronized(commandListeners) {
			CommandListener listener = commandListeners.get(verb);
			if(listener == null)
				return;
			listener.execute(cmd);
		}
		String retData = cmd.getReturnMessage();
		if(retData == null)
			return;
		try {
			chat.sendMessage(retData);
		} catch (XMPPException e) {
			// Logs
			// probablement pas pu envoyer car déconnexion, déjà géré
		}
	}
	
	private final ChatListenerImpl chatListenerInstance = new ChatListenerImpl();
	private class ChatListenerImpl implements ChatManagerListener {
		@Override
		public void chatCreated(Chat chat, boolean createdLocally) {
			chat.addMessageListener(messageListenerInstance);
		}
	}
	
	private final MessageListenerImpl messageListenerInstance = new MessageListenerImpl();
	private class MessageListenerImpl implements MessageListener {
		@Override
		public void processMessage(Chat chat, Message message) {
			processChatMessage(chat, message);
		}
	}
	
	private final ConnectionListenerImpl connectionListenerInstance = new ConnectionListenerImpl();
	private class ConnectionListenerImpl implements ConnectionListener {

		@Override
		public void connectionClosed() {
			// Logs
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			// Logs
		}

		@Override
		public void reconnectingIn(int seconds) {
			// Logs
		}

		@Override
		public void reconnectionSuccessful() {
			// Logs
			try {
				connectionInit();
			}
			catch(XMPPException ex) {
				// Logs
				throw new RuntimeException("Unexcepted XMPP exception", ex);
			}
		}

		@Override
		public void reconnectionFailed(Exception e) {
			// Logs
		}
		
	}
}
