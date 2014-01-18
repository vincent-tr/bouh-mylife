package mylife.home.irc.message;

/**
 * Préfix utilisateur
 * @author pumbawoman
 *
 */
public class UserPrefix extends Prefix {

	/**
	 * User
	 */
	private final User user;
	
	/**
	 * Constructeur avec données
	 * @param user
	 */
	public UserPrefix(User user) {
		this.user = user;
	}

	/**
	 * User
	 * @return
	 */
	public User getUser() {
		return user;
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
		return user == null ? "" : user.toString();
	}
}
