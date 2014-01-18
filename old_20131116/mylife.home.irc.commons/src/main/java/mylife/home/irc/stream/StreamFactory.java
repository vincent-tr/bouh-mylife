package mylife.home.irc.stream;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import mylife.home.irc.net.ConnectionManager;
import mylife.home.irc.net.DataConnection;
import mylife.home.irc.net.DataConnectionFactory;

/**
 * Fabrique de DataConnection de type Stream
 * 
 * @author pumbawoman
 * 
 */
public class StreamFactory implements DataConnectionFactory {

	private final List<ConnectionListener> connectionListeners = new ArrayList<ConnectionListener>();

	/**
	 * Ajout d'un listener sur nouvelle connexion
	 * 
	 * @param listener
	 */
	public void addConnectionListener(ConnectionListener listener) {
		if (listener == null)
			throw new IllegalArgumentException("listener can not be null");
		synchronized (connectionListeners) {
			connectionListeners.add(listener);
		}
	}

	/**
	 * Suppression d'un listener sur nouvelle connexion
	 * 
	 * @param listener
	 */
	public void removeConnectionListener(ConnectionListener listener) {
		synchronized (connectionListeners) {
			connectionListeners.remove(listener);
		}
	}

	/**
	 * Exécution des listeners de nouvelle connexion
	 * 
	 * @param connection
	 */
	protected void executeConnectionListeners(Stream connection) {
		synchronized (connectionListeners) {
			for (ConnectionListener listener : connectionListeners)
				listener.newConnection(connection);
		}
	}

	/**
	 * Gestionnaire de connexion à renseigner après création
	 */
	private ConnectionManager connectionManager;

	/**
	 * Implémentation de DataConnectionFactory, utilisé par ListenerConnection et ClientConnection
	 */
	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	private final int recvBufferSize;
	private final int sendBufferSize;
	
	/**
	 * Constructeur avec taille de buffer
	 * 
	 * @param recvBufferSize
	 * @param sendBufferSize
	 */
	public StreamFactory(int recvBufferSize, int sendBufferSize) {
		this.recvBufferSize = recvBufferSize;
		this.sendBufferSize = sendBufferSize;
	}

	/**
	 * Implémentation de DataConnectionFactory, utilisé par ListenerConnection et ClientConnection
	 */
	@Override
	public DataConnection createDataConnection(SocketChannel channel) {
		return new Stream(connectionManager, channel, recvBufferSize, sendBufferSize);
	}
}
