package org.mylife.home.net.hub.irc.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Listener
 * 
 * @author pumbawoman
 * 
 */
public class IOListener extends IOElement {

	public static interface Handler {
		public void newClient(SocketChannel client);
	}

	private final Handler handler;
	private final ServerSocketChannel socket;

	public IOListener(Handler handler, String address, int port)
			throws IOException {
		this.handler = handler;
		socket = ServerSocketChannel.open();
		socket.configureBlocking(false);
		InetSocketAddress addr;
		if (address == null)
			addr = new InetSocketAddress(port);
		else
			addr = new InetSocketAddress(address, port);
		socket.socket().bind(addr);
	}

	@Override
	protected SelectionKey openImpl(Selector selector) throws IOException {
		return socket.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
	protected void closeImpl() throws IOException {
		socket.close();
	}

	@Override
	protected void selectImpl() throws IOException {
		SocketChannel client = socket.accept();
		handler.newClient(client);
	}

}
