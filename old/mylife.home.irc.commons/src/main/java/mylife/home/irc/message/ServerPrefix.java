package mylife.home.irc.message;

/**
 * Préfix serveur
 * @author pumbawoman
 *
 */
public class ServerPrefix extends Prefix {

	/**
	 * Nom du serveur
	 */
	private final String server;
	
	/**
	 * Constructeur avec données
	 * @param server
	 */
	public ServerPrefix(String server) {
		this.server = server;
	}

	/**
	 * Nom du serveur
	 * @return
	 */
	public String getServer() {
		return server;
	}

	@Override
	public int hashCode() {
		return toString().toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof ServerPrefix))
			return false;
		ServerPrefix other = (ServerPrefix)obj;
		return toString().equalsIgnoreCase(other.toString());
	}

	@Override
	public String toString() {
		return server == null ? "" : server;
	}
}
