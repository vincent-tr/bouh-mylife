package mylife.home.irc.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Connexion cliente
 * @author pumbawoman
 *
 */
public class ClientConnection extends Connection {

	private final SocketChannel channel;
	private final DataConnectionFactory factory;
	
	/**
	 * Construit et ouvre le socket
	 * @param owner
	 * @param remote
	 * @param factory
	 * @throws IOException 
	 */
	public ClientConnection(ConnectionManager owner, SocketAddress remote, DataConnectionFactory factory) throws IOException {
		
		super(owner);
		
		this.factory = factory;
		factory.setConnectionManager(owner);
		
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(remote);
		owner.addConnection(this);
	}
	
	@Override
	protected SelectableChannel getChannel() {
		return channel;
	}

	@Override
	protected int interestOps() {
		return SelectionKey.OP_CONNECT;
	}

	@Override
	protected void onConnectable() throws IOException {
		
		try {
			channel.finishConnect();
		} catch(IOException e) {
			// la connexion est un echec
			onConnectionFailed(e);
			return;
		}
		
		// connexion réussie
		
		// suppression de l'abonnement courant
		owner.removeConnection(this);
		
		// création de la nouvelle connexion de données à partir du socket channel
		DataConnection connection = factory.createDataConnection(channel);
		owner.addConnection(connection);
		onConnected(connection);
	}
	
	/**
	 * A overrider
	 * @param connection
	 */
	protected void onConnected(DataConnection connection) {
	}
	
	/**
	 * A overrider
	 * @param e
	 */
	protected void onConnectionFailed(IOException e) {
	}

	/**
	 * A overrider
	 * @param e
	 */
	@Override
	protected void onError(Exception e) {
	}
}
