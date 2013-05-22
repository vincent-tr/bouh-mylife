package mylife.home.irc.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mylife.home.irc.message.Message;
import mylife.home.irc.net.ConnectionManager;
import mylife.home.irc.net.ListenerConnection;
import mylife.home.irc.server.commands.Command;
import mylife.home.irc.server.structure.Component;
import mylife.home.irc.server.structure.Connection;
import mylife.home.irc.server.structure.Network;
import mylife.home.irc.server.structure.Server;
import mylife.home.irc.server.structure.User;
import mylife.home.irc.stream.CloseListener;
import mylife.home.irc.stream.ConnectionListener;
import mylife.home.irc.stream.ErrorListener;
import mylife.home.irc.stream.MessageListener;
import mylife.home.irc.stream.Stream;
import mylife.home.irc.stream.StreamFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implémentation d'un serveur
 * 
 * @author pumbawoman
 * 
 */
public class IrcServer {
	/**
	 * Logging
	 */
	private final Logger log = LoggerFactory.getLogger(IrcServer.class);

	/**
	 * Représentation logique du réseau
	 */
	private Network network;

	/**
	 * Gestion du traffic réseau
	 */
	private ConnectionManager connectionManager;

	/**
	 * Connexion en écoute
	 */
	private ListenerConnection listener;

	/**
	 * Fabrique de flux
	 */
	private StreamFactory connectionFactory;

	/**
	 * Liste des connexions du serveur
	 */
	private Collection<Connection> connections;

	/**
	 * Liste des commandes d'implémentation du serveur
	 */
	private Map<String, Command> commands;

	/**
	 * Représentation du réseau
	 * 
	 * @return
	 */
	public Network getNetwork() {
		return network;
	}

	/**
	 * Ouverture du serveur
	 * 
	 * @param configuration
	 * @throws IOException
	 */
	public void open(Configuration configuration) throws IOException {

		commands = new HashMap<String, Command>();
		for (Map.Entry<String, Command> entry : configuration.getCommands()
				.entrySet()) {
			commands.put(entry.getKey().toUpperCase(), entry.getValue());
		}
		for (Command cmd : commands.values()) {
			cmd.initialize(this);
		}

		network = new Network();
		// création du serveur local
		network.getServers().add(
				new Server(configuration.getName(), configuration.getToken(),
						0, configuration.getInfo(), null));

		connectionFactory = new StreamFactory(
				configuration.getRecvBufferSize(),
				configuration.getSendBufferSize());
		connectionFactory.addConnectionListener(new ConnectionListener() {
			@Override
			public void newConnection(Stream connection) {
				handleNewConnection(connection);
			}
		});

		connections = new ArrayList<Connection>();

		connectionManager = new ConnectionManager();
		connectionManager.open();
		connectionManager.addConnection(listener = new ListenerConnection(
				connectionManager, new InetSocketAddress(configuration
						.getListenPort()), connectionFactory));
	}

	/**
	 * Fermeture du serveur
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void close() throws IOException, InterruptedException {

		final Object waiter = new Object();
		synchronized (waiter) {

			addCustomOperations(new Runnable() {
				@Override
				public void run() {
					try {
						// fermeture du listener
						listener.close();

						// TODO : fermeture des connexions
						connections = null;
						connectionFactory = null;

						// suppression des commandes
						for (Command cmd : commands.values()) {
							cmd.terminate();
						}
						commands = null;

						// suppression du réseau
						network = null;

					} catch (Exception e) {
						log.error("Error closing IrcServer", e);
					} finally {
						synchronized (waiter) {
							// notification de la fin de l'opération
							waiter.notify();
						}
					}
				}
			});

			// attente de la fin de l'opération
			waiter.wait();
		}

		// arrêt du gestionnaire
		connectionManager.close();
		connectionManager = null;
	}

	/**
	 * Ajout d'opérations extérieurs à exécuter dans le thread de loop, cela
	 * permet d'avoir un fonctionnement single-thread du coeur du serveur
	 * 
	 * @param operations
	 */
	public void addCustomOperations(Runnable... operations) {
		connectionManager.addCustomOperations(operations);
	}

	/**
	 * Appelé lors de la création d'une nouvelle connexion
	 * 
	 * @param stream
	 */
	private void handleNewConnection(Stream stream) {
		final Connection connection = new Connection(stream);
		connections.add(connection);

		stream.addCloseListener(new CloseListener() {
			@Override
			public void closed() {
				handleCloseConnection(connection);
			}
		});
		stream.addErrorListener(new ErrorListener() {
			@Override
			public void errorOccured(Exception e) {
				handleErrorConnection(connection, e);
			}
		});
		stream.addReceiveListener(new MessageListener() {
			@Override
			public void messageReceived(Message message) {
				handleReceiveConnection(connection, message);
			}
		});
	}

	/**
	 * Appelé sur fermeture de connexion
	 * 
	 * @param connection
	 */
	private void handleCloseConnection(Connection connection) {
		// TODO
	}

	/**
	 * Appelé sur erreur d'une connexion
	 * 
	 * @param connection
	 * @param e
	 */
	private void handleErrorConnection(Connection connection, Exception e) {
		// TODO
	}

	/**
	 * Appelé sur réception d'un message d'une connexion
	 * 
	 * @param connection
	 * @param m
	 */
	private void handleReceiveConnection(Connection connection, Message m) {
		Command cmd = commands.get(m.getCommand().toUpperCase());
		if (cmd == null)
			;// TODO : envoyer un message d'erreur ?

		cmd.handle(connection, m);
	}

	/**
	 * Envoi d'un message à une liste de composants Si plusieurs composants sont
	 * sur une même connexion, le message ne sera envoyé qu'une seule fois
	 * 
	 * @param msg
	 * @param targets
	 */
	public void send(Message msg, Component... targets) {
		if (targets == null || targets.length == 0)
			return;

		// set de connexions à qui envoyer le message
		Set<Connection> dest = new HashSet<Connection>();

		// obtention des connexions à qui envoyer
		for (Component component : targets) {
			Connection con = null;

			if (component instanceof Server) {
				Server server = (Server) component;
				if (server.isSelf()) {
					log.debug("Message on myself, ignored");
					continue;
				}
				con = server.getServerConnection();
			} else if (component instanceof User) {
				User user = (User) component;
				if (user.isLocal())
					con = user.getClientConnection();
				else
					con = user.getServer().getServerConnection();
			}

			if (con != null && !dest.contains(con))
				dest.add(con);
		}
		
		// envoi du message
		for(Connection con : dest) {
			con.getStream().send(msg);
		}
	}
}
