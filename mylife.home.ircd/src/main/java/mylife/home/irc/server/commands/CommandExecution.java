package mylife.home.irc.server.commands;

import mylife.home.irc.message.Message;
import mylife.home.irc.message.Numerics;
import mylife.home.irc.message.Prefix;
import mylife.home.irc.message.ServerPrefix;
import mylife.home.irc.server.IrcServer;
import mylife.home.irc.server.structure.Connection;
import mylife.home.irc.server.structure.Server;

/**
 * Paramètre de l'exécution d'une commande
 * @author pumbawoman
 *
 */
public class CommandExecution {

	/**
	 * Serveur
	 */
	private final IrcServer server;
	
	/**
	 * Connexion d'où provient le message
	 */
	private final Connection connection; 
	
	/**
	 * Message à exécuter
	 */
	private final Message message;
	
	/**
	 * Constructeur avec données
	 * @param server
	 * @param connection
	 * @param message
	 */
	public CommandExecution(IrcServer server, Connection connection, Message message) {
		this.server = server;
		this.connection = connection;
		this.message = message;
	}

	/**
	 * Serveur
	 * @return
	 */
	public IrcServer getServer() {
		return server;
	}

	/**
	 * Connexion d'où provient le message
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Message à exécuter
	 * @return
	 */
	public Message getMessage() {
		return message;
	}
	
	/**
	 * Cache de selfPrefix
	 */
	private Prefix cachedSelfPrefix;
	
	/**
	 * Obtention du préfix du serveur
	 * @return
	 */
	public Prefix selfPrefix() {
		if(cachedSelfPrefix == null)
			cachedSelfPrefix = new ServerPrefix(server.getNetwork().findLocalServer().getName());
		return cachedSelfPrefix;
	}
	
	/**
	 * Envoi une réponse numérique
	 * @param numeric
	 * @param strings
	 */
	public void reply(Numerics numeric, String... args) {
		Prefix prefix = selfPrefix();
		numeric.createMessage(prefix, args);
		connection.getStream().send(message);
	}
	
	/**
	 * Envoi d'un message à tous les serveurs sauf ceux exclus
	 * @param message
	 * @param exclusions
	 */
	public void serverBroadcast(Message message, Server... exclusions) {
		for(Server srv : server.getNetwork().getServers()) {
			if(srv.isSelf())
				continue;
			
			boolean excluded = false;
			for(Server exclusion : exclusions) {
				if(exclusion == srv) {
					excluded = true;
					continue;
				}
			}
			if(excluded)
				continue;
			
			srv.getServerConnection().getStream().send(message);
		}
	}
}
