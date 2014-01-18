package mylife.home.irc.server.structure;

import java.util.HashMap;
import java.util.Map;

import mylife.home.irc.stream.Stream;

/**
 * Représentation logique d'une connexion
 * @author pumbawoman
 *
 */
public class Connection {
	
	/**
	 * La connexion est créé  
	 */
	public static final int STATUS_STARTING = 0;
	
	/**
	 * Le mot de passe a été défini
	 */
	public static final int STATUS_PASS_DEFINED = 0x01;
	
	/**
	 * Le handshake est terminé en mode serveur
	 */
	public static final int STATUS_SERVER_OK = 0x1F;
	
	/**
	 * Le handshake a recu le pseudo est est en attente du user en mode client
	 */
	public static final int STATUS_CLIENT_NICK = 0x21;
	
	/**
	 * Le handshake est terminé en mode client
	 */
	public static final int STATUS_CLIENT_OK = 0x2F;

	/**
	 * Flux sous-jacent
	 */
	private final Stream stream;
	
	/**
	 * Statut de la connexion
	 */
	private int status;

	/**
	 * Représentation du composant connecté. Pour les connexion serveurs, le composant pointé est celui dont le serveur a un hopcount à 1 
	 */
	private Component component;
	
	/**
	 * Gestion des données d'initialisation
	 */
	private final Map<String, String> initData = new HashMap<String, String>();

	/**
	 * Gestion des données d'initialisation
	 */
	public Map<String, String> getInitData() {
		return initData;
	}
	
	public static final String INIT_DATA_KEY_PASS = "pass";
	public static final String INIT_DATA_KEY_NICK = "nick";
	
	/**
	 * Constructeur avec données
	 * @param stream
	 */
	public Connection(Stream stream) {
		this.stream = stream;
	}

	/**
	 * Flux sous-jacent
	 * @return
	 */
	public Stream getStream() {
		return stream;
	}
	
	/**
	 * Statut de la connexion
	 * @return
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Statut de la connexion
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Représentation du composant connecté. Pour les connexion serveurs, le composant pointé est celui dont le serveur a un hopcount à 1 
	 * @return
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * Représentation du composant connecté. Pour les connexion serveurs, le composant pointé est celui dont le serveur a un hopcount à 1 
	 * @param component
	 */
	public void setComponent(Component component) {
		this.component = component;
	}
}
