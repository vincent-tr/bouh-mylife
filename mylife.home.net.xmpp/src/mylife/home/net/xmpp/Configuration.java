package mylife.home.net.xmpp;

/**
 * Classe immuable de configuration
 * @author pumbawoman
 */
public class Configuration {
	
	private final String xmppServer;
	private final String mucRoom;
	
	/**
	 * Constructeur avec données
	 * @param xmppServer
	 * @param mucRoom
	 */
	public Configuration(String xmppServer, String mucRoom) {
		this.xmppServer = xmppServer;
		this.mucRoom = mucRoom;
	}

	/**
	 * Serveur
	 */
	public String getXmppServer() {
		return xmppServer;
	}
	
	/**
	 * muc
	 */
	public String getMucRoom() {
		return mucRoom;
	}
}
