package mylife.home.irc.message;

/**
 * Représentation d'un utilisateur
 * 
 * @author pumbawoman
 * 
 */
public class User {

	/**
	 * Pseudo
	 */
	private final String nick;

	/**
	 * User
	 */
	private final String user;

	/**
	 * Hôte
	 */
	private final String host;

	/**
	 * Constructeur avec données
	 * @param nick
	 * @param user
	 * @param host
	 */
	public User(String nick, String user, String host) {
		this.nick = nick;
		this.user = user;
		this.host = host;
	}

	/**
	 * Pseudo
	 * @return
	 */
	public String getNick() {
		return nick;
	}

	/**
	 * User
	 * @return
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Hôte
	 * @return
	 */
	public String getHost() {
		return host;
	}

	@Override
	public int hashCode() {
		return toString().toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof UserPrefix))
			return false;
		UserPrefix other = (UserPrefix)obj;
		return toString().equalsIgnoreCase(other.toString());
	}

	@Override
	public String toString() {
		if(nick == null)
			return "";
		if(user == null)
			return nick;
		if(host == null)
			return nick + "!" + user;
		return nick + "!" + user + "@" + host;
	}
}
