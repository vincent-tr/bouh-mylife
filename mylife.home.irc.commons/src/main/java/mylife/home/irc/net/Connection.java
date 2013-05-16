package mylife.home.irc.net;

import java.io.IOException;
import java.nio.channels.SelectableChannel;

/**
 * Implémentation de base d'une connexion
 * @author pumbawoman
 *
 */
public abstract class Connection {

	/**
	 * Manager propriétaire
	 */
	protected final ConnectionManager owner;

	/**
	 * Constructeur avec données
	 * @param owner
	 */
	public Connection(ConnectionManager owner) {
		this.owner = owner;
	}
	
	/**
	 * Obtention du channel, ne doit pas changer au court de la vie de la connexion
	 * @return
	 */
	abstract SelectableChannel getChannel();
	
	/**
	 * Obtention des opération à écouter
	 * @return
	 */
	abstract int interestOps();
	
	/**
	 * Appelé lorsque le channel est prêt à lire
	 */
	protected void onReadable() throws IOException {
		
	}
	
	/**
	 * Appelé lorsque le channel est prêt à écrire
	 */
	protected void onWritable() throws IOException {
		
	}
	
	/**
	 * Appelé lorsque le channel est prêt à être connecté
	 */
	protected void onConnectable() throws IOException {
		
	}
	
	/**
	 * Appelé lorsque le channel est prêt à accepter une nouvelle connexion
	 */
	protected void onAcceptable() throws IOException {
		
	}
	
	/**
	 * Appelé lorsqu'une erreur est survenue
	 * @param e
	 */
	protected void onError(Exception e) {
		
	}
}
