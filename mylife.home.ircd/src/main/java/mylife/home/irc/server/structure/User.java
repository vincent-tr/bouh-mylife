package mylife.home.irc.server.structure;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Représentation d'un utilisateur
 * 
 * @author pumbawoman
 * 
 */
public class User extends Component {

	/**
	 * Pseudo
	 */
	private String nick;

	/**
	 * User
	 */
	private final String user;

	/**
	 * Nom d'hôte
	 */
	private final String host;
	
	/**
	 * Nom réel
	 */
	private final String realName;
	
	/**
	 * Serveur sur lequel est connecté l'utilisateur
	 */
	private final Server server;

	/**
	 * Modes de l'utilisateur
	 */
	private final Collection<Mode> modes = new ArrayList<Mode>();

	/**
	 * Salons sur lequel l'utilisateur est présent
	 */
	private final Collection<Channel> channels = new ArrayList<Channel>();
	
	/**
	 * Connexion sur laquelle le client est connecté si c'est un client direct, ou null si c'est un client distant
	 */
	private final Connection clientConnection;

	/**
	 * Constructeur avec données
	 * 
	 * @param nick
	 * @param user
	 * @param host
	 * @param realName
	 * @param server
	 * @param clientConnection
	 */
	public User(String nick, String user, String host, String realName, Server server,  Connection clientConnection) {
		if (nick == null || nick.length() == 0)
			throw new IllegalArgumentException("User nick can not be null");
		if (user == null || user.length() == 0)
			throw new IllegalArgumentException("User user can not be null");
		if(realName == null)
			realName = "";
		if (host == null || host.length() == 0)
			throw new IllegalArgumentException("User host can not be null");
		if(server == null)
			throw new IllegalArgumentException("User server can not be null");
		this.nick = nick;
		this.user = user;
		this.host = host;
		this.realName = realName;
		this.server = server;
		this.clientConnection = clientConnection;
	}

	/**
	 * Pseudo
	 * @return
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * Pseudo
	 * @param nick
	 */
	public void setNick(String nick) {
		if (nick == null || nick.length() == 0)
			throw new IllegalArgumentException("User nick can not be null");
		this.nick = nick;
	}

	/**
	 * User
	 * @return
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Nom d'hôte
	 * @return
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Nom réel
	 * @return
	 */
	public String getRealName() {
		return realName;
	}

	/**
	 * Serveur sur lequel est connecté l'utilisateur
	 * @return
	 */
	public Server getServer() {
		return server;
	}

	/**
	 * Modes de l'utilisateur
	 * 
	 * @return
	 */
	public Collection<Mode> getModes() {
		return modes;
	}

	/**
	 * Salons sur lequel l'utilisateur est présent
	 * 
	 * @return
	 */
	public Collection<Channel> getChannels() {
		return channels;
	}

	/**
	 * Connexion sur laquelle le client est connecté si c'est un client direct, ou null si c'est un client distant
	 * @return
	 */
	public Connection getClientConnection() {
		return clientConnection;
	}

	@Override
	public int hashCode() {
		return nick.toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User))
			return false;
		User other = (User) obj;
		return this.hashCode() == other.hashCode(); 
	}
	
	/**
	 * Indique si l'utilisateur est directement connecté au serveur
	 * @return
	 */
	public boolean isLocal() {
		return this.clientConnection != null;
	}
}
