package mylife.home.irc.server.structure;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Représentation d'un serveur
 * @author pumbawoman
 *
 */
public class Server extends Component {

	/**
	 * Nom du serveur
	 */
	private final String name;
	
	/**
	 * Token (ou numeric) du serveur, doit être unique sur le réseau
	 */
	private final int token;
	
	/**
	 * Distance du serveur
	 */
	private final int hopCount;
	
	/**
	 * Infos sur le serveur
	 */
	private final String info;
	
	/**
	 * Connexion sur laquelle le serveur est connecté ou null si c'est nous même
	 */
	private final Connection serverConnection;

	/**
	 * Utilisateur connectés au serveur
	 */
	private final Collection<User> users = new ArrayList<User>();
	
	/**
	 * Constructeur avec données
	 * @param name
	 * @param token
	 * @param hopCount
	 * @param info
	 * @param serverConnection
	 */
	public Server(String name, int token, int hopCount, String info, Connection serverConnection) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Server name can not be null");
		this.name = name;
		this.token = token;
		this.hopCount = hopCount;
		this.info = info;
		this.serverConnection = serverConnection;
	}
	
	/**
	 * Nom du serveur
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Token (ou numeric) du serveur, doit être unique sur le réseau
	 * @return
	 */
	public int getToken() {
		return token;
	}

	/**
	 * Distance du serveur
	 * @return
	 */
	public int getHopCount() {
		return hopCount;
	}

	/**
	 * Infos sur le serveur
	 * @return
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * Connexion sur laquelle le serveur est connecté ou null si c'est nous même
	 * @return
	 */
	public Connection getServerConnection() {
		return serverConnection;
	}

	/**
	 * Utilisateur connectés au serveur
	 * @return
	 */
	public Collection<User> getUsers() {
		return users;
	}

	@Override
	public int hashCode() {
		return token;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Server))
			return false;
		Server other = (Server)obj;
		return this.hashCode() == other.hashCode();
	}
	
	/**
	 * Indique si l'objet serveur nous représente 
	 * @return
	 */
	public boolean isSelf() {
		return this.serverConnection == null;
	}
}
