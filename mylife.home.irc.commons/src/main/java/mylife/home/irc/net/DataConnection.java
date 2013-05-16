package mylife.home.irc.net;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class DataConnection extends Connection implements Closeable {

	private final SocketChannel channel;
	private final ByteBuffer readBuffer;
	private final ByteBuffer writeBuffer;
	private boolean isWriting;

	public DataConnection(ConnectionManager owner, SocketChannel channel,
			int recvBufferSize, int sendBufferSize) {

		super(owner);

		this.channel = channel;
		this.readBuffer = ByteBuffer.allocate(recvBufferSize);
		this.writeBuffer = ByteBuffer.allocate(sendBufferSize);
		isWriting = false;
	}

	@Override
	protected SelectableChannel getChannel() {
		return channel;
	}

	@Override
	protected int interestOps() {
		return isWriting ? SelectionKey.OP_WRITE : SelectionKey.OP_READ;
	}

	@Override
	protected void onReadable() throws IOException {
		int count = channel.read(readBuffer);
		if (count == -1) {
			owner.removeConnection(this);
			channel.close();
			onClose();
		} else {
			readBuffer.flip();
			byte[] data = new byte[readBuffer.limit()];
			readBuffer.get(data);
			onReceive(data);
			readBuffer.clear();
		}
	}

	@Override
	protected void onWritable() throws IOException {
		synchronized (writeBuffer) {
			channel.write(writeBuffer);
			writeBuffer.compact();

			// si plus de données on repasse en mode lecture
			if (writeBuffer.remaining() == 0)
				setWriting(false);
		}
	}

	/**
	 * A overrider
	 * 
	 * @param data
	 */
	protected void onReceive(byte[] data) {
	}

	/**
	 * A overrider
	 * @param e
	 */
	@Override
	protected void onError(Exception e) {
	}

	/**
	 * A overrider Indique que la connexion a été fermée par la partie distante.
	 * Le socket est déjà fermée lorsque onClose est appelé
	 */
	protected void onClose() {
	}

	/**
	 * Fermeture de la connexion (n'appelle pas onClose)
	 * 
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		owner.removeConnection(this);
		channel.shutdownOutput();
		channel.shutdownInput();
		channel.close();
	}

	private void setWriting(boolean value) {
		if (isWriting == value)
			return;
		isWriting = value;
		owner.changeConnection(this);
	}

	/**
	 * Envoi de données
	 * 
	 * @param data
	 */
	public void send(byte[] data) {
		synchronized (writeBuffer) {
			// ajout de données à la fin du buffer

			// backup des offset
			int position = writeBuffer.position();
			int limit = writeBuffer.limit();

			// passage en mode écriture à la fin des données
			writeBuffer.limit(writeBuffer.capacity());
			writeBuffer.position(limit);

			// ajout des données
			writeBuffer.put(data);

			// remise du buffer pour lecture et envoi
			writeBuffer.position(position);
			writeBuffer.limit(limit);

			// passage en mode ecriture
			setWriting(true);
		}
	}
}
