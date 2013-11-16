package mylife.home.irc.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ListenerConnection extends Connection implements Closeable {

	private final ServerSocketChannel channel;
	private final DataConnectionFactory factory;
	
	/**
	 * Construit et ouvre le socket
	 * @param owner
	 * @param local
	 * @param factory
	 * @throws IOException
	 */
	public ListenerConnection(ConnectionManager owner, SocketAddress local, DataConnectionFactory factory) throws IOException {
		
		super(owner);
		
		this.factory = factory;
		factory.setConnectionManager(owner);
		
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.bind(local);
		owner.addConnection(this);
	}
	
	/**
	 * Fermeture du listener
	 */
	@Override
	public void close() throws IOException {
		owner.removeConnection(this);
		channel.close();
	}
	
	@Override
	protected SelectableChannel getChannel() {
		return channel;
	}

	@Override
	protected int interestOps() {
		return SelectionKey.OP_ACCEPT; 
	}
	
	@Override
	protected void onAcceptable() throws IOException {
		SocketChannel newSocket = channel.accept();
		DataConnection connection = factory.createDataConnection(newSocket);
		owner.addConnection(connection);
		onNewClient(connection);
	}

	/**
	 * A overrider
	 * @param connection
	 */
	protected void onNewClient(DataConnection connection) {
	}

	/**
	 * A overrider
	 * @param e
	 */
	@Override
	protected void onError(Exception e) {
	}
}
