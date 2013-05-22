package mylife.home.irc.net;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestionnaire de connexions
 * 
 * @author pumbawoman
 * 
 */
public class ConnectionManager {

	/**
	 * Logging
	 */
	private final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

	private final Object openSync = new Object();
	private Selector selector;
	private Thread worker;
	private boolean workerExit;
	private Queue<Runnable> pendingOperations;
	private Map<SelectionKey, Connection> connectionsByKey;
	private Map<Connection, SelectionKey> keysByConnection;

	/**
	 * Ouverture du gestionnaire
	 * 
	 * @throws IOException
	 */
	public void open() throws IOException {

		synchronized (openSync) {

			if (isOpen())
				return;

			selector = Selector.open();
			pendingOperations = new LinkedList<Runnable>();
			connectionsByKey = new HashMap<SelectionKey, Connection>();
			keysByConnection = new HashMap<Connection, SelectionKey>();

			worker = new Thread() {
				@Override
				public void run() {
					workerRun();
				}
			};
			worker.setName("ConnectionManager.Worker");
			worker.setDaemon(true);
			workerExit = false;
			worker.start();
		}
	}

	/**
	 * Fermeture du gestionnaire
	 */
	public void close() throws IOException, InterruptedException {

		synchronized (openSync) {

			if (!isOpen())
				return;

			workerAddOperations(new OperationClose());
			worker.join();
			worker = null;
			selector.close();
			selector = null;
			pendingOperations = null;
			connectionsByKey = null;
			keysByConnection = null;
		}
	}

	/**
	 * Indique si le gestionnaire est ouvert
	 * 
	 * @return
	 */
	public boolean isOpen() {
		synchronized (openSync) {
			return worker != null;
		}
	}

	private void ensureOpen() {
		if (!isOpen())
			throw new IllegalStateException();
	}

	/**
	 * Ajout d'une connexion
	 * 
	 * @param connection
	 */
	public void addConnection(Connection connection) {
		synchronized (openSync) {
			ensureOpen();
			workerAddOperations(new OperationAddConnection(connection));
		}
	}

	/**
	 * Suppression d'une connexion
	 * 
	 * @param connection
	 */
	public void removeConnection(Connection connection) {
		synchronized (openSync) {
			ensureOpen();
			workerAddOperations(new OperationRemoveConnection(connection));
		}
	}

	/**
	 * Modification des opérations d'interet d'une connexion
	 * 
	 * @param connection
	 */
	public void changeConnection(Connection connection) {
		synchronized (openSync) {
			ensureOpen();
			workerAddOperations(new OperationChangeConnection(connection));
		}
	}
	
	/**
	 * Ajout d'opérations extérieurs à exécuter dans le thread de loop, cela permet d'avoir un fonctionnement single-thread du coeur du serveur, client, service, ...
	 * @param operations
	 */
	public void addCustomOperations(Runnable ... operations) {
		workerAddOperations(operations);
	}

	private class OperationClose implements Runnable {
		@Override
		public void run() {
			workerExit = true;
		}
	}

	private class OperationAddConnection implements Runnable {
		private final Connection connection;

		public OperationAddConnection(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			try {
				SelectableChannel channel = connection.getChannel();
				int ops = connection.interestOps();

				SelectionKey key = channel.register(selector, ops);

				keysByConnection.put(connection, key);
				connectionsByKey.put(key, connection);

			} catch (ClosedChannelException e) {
				log.error("Error in worker thread", e);
			}
		}
	}

	private class OperationRemoveConnection implements Runnable {
		private final Connection connection;

		public OperationRemoveConnection(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			SelectionKey key = keysByConnection.get(connection);

			key.cancel();

			keysByConnection.remove(connection);
			connectionsByKey.remove(key);
		}
	}

	private class OperationChangeConnection implements Runnable {
		private final Connection connection;

		public OperationChangeConnection(Connection connection) {
			this.connection = connection;
		}

		@Override
		public void run() {
			int ops = connection.interestOps();
			SelectionKey key = keysByConnection.get(connection);

			key.interestOps(ops);
		}
	}

	private void workerAddOperations(Runnable... operations) {

		if (operations == null || operations.length == 0)
			return;

		if (Thread.currentThread().equals(worker)) {

			// si on est déjà dans le worker on exécute les opérations
			// directement
			for (Runnable operation : operations) {
				operation.run();
			}

		} else {

			// sinon on les empile et on reveil le worker
			synchronized (pendingOperations) {
				for (Runnable operation : operations) {
					pendingOperations.add(operation);
				}
			}
			selector.wakeup();

		}
	}

	private void workerRun() {
		try {

			while (!workerExit) {

				selector.select();
				workerRunOperations();
				workerRunEvents();
			}

		} catch (Exception e) {
			log.error("Error in worker thread", e);
		}
	}

	private void workerRunOperations() {
		synchronized (pendingOperations) {
			Runnable operation;
			while ((operation = pendingOperations.poll()) != null) {
				operation.run();
			}
		}
	}

	private void workerRunEvents() {

		Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
		while (iterator.hasNext()) {

			SelectionKey key = iterator.next();
			iterator.remove();

			Connection connection = connectionsByKey.get(key);

			try {
				if (key.isReadable())
					connection.onReadable();
				if (key.isWritable())
					connection.onWritable();
				if (key.isConnectable())
					connection.onConnectable();
				if (key.isAcceptable())
					connection.onAcceptable();
			} catch (Exception e) {
				connection.onError(e);
			}
		}
	}
}
