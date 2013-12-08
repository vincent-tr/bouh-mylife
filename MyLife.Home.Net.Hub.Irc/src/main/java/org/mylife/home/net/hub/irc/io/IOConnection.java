package org.mylife.home.net.hub.irc.io;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Connexion
 * 
 * @author pumbawoman
 * 
 */
public class IOConnection extends IOElement {

	public static interface Handler {
		public void read(byte[] data);

		public void remoteClosed();

		public void connected();

		public void connectionFailed(IOException e);
	}

	private final Handler handler;
	private final SocketChannel socket;
	private boolean connecting = false;
	private final ByteBuffer readBuffer;

	public IOConnection(Handler handler, String address, int port)
			throws IOException {
		this(handler, SocketChannel.open());
		beginConnect(address, port);
	}

	public IOConnection(Handler handler, SocketChannel socket)
			throws IOException {
		this.handler = handler;
		this.socket = socket;
		socket.configureBlocking(false);
		readBuffer = ByteBuffer
				.allocate(socket.socket().getReceiveBufferSize());
	}
	
	public String getRemoteHost() {
		if(!socket.isConnected())
			return null;
		InetAddress addr = socket.socket().getInetAddress();
		return addr.getHostName();
	}

	@Override
	protected SelectionKey openImpl(Selector selector) throws IOException {
		int ops = 0;
		if (connecting)
			ops = SelectionKey.OP_CONNECT;
		else
			ops = SelectionKey.OP_READ;
		return socket.register(selector, ops);
	}

	private void beginConnect(String address, int port) throws IOException {
		// Connexion non bloquante par contre après write bloquant
		socket.configureBlocking(false);
		socket.connect(new InetSocketAddress(address, port));
		connecting = true;
	}

	private void finishConnect() throws IOException {
		try {
			socket.finishConnect();
			// On change les opérations à surveiller de CONNECT à READ
			getSelectionKey().interestOps(SelectionKey.OP_READ);
			handler.connected();
		} catch (IOException e) {
			handler.connectionFailed(e);
		}
		connecting = false;
	}

	private void read() throws IOException {
		readBuffer.clear();
		switch (socket.read(readBuffer)) {
		case -1:
			handler.remoteClosed();
			break;
		case 0:
			break;
		default:
			readBuffer.flip();
			byte[] data = new byte[readBuffer.remaining()];
			readBuffer.get(data);
			handler.read(data);
			break;
		}
	}

	@Override
	protected void selectImpl() throws IOException {
		if (connecting)
			finishConnect();
		else
			read();
	}

	@Override
	protected void closeImpl() throws IOException {
		socket.close();
	}

	public void write(byte[] data) throws IOException {
		socket.write(ByteBuffer.wrap(data));
	}
}
