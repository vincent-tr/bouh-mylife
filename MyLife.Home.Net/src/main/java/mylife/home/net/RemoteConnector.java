package mylife.home.net;

import java.util.logging.Logger;

/**
 * Connecteur pour un objet distant
 * @author pumbawoman
 *
 */
class RemoteConnector implements Connector {

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(RemoteConnector.class.getName());
	
	/**
	 * Conteneur publié
	 */
	private final NetContainer container;

	
	/**
	 * Construction du connecteur avec l'objet
	 * 
	 * @param object
	 * @param channel
	 */
	public RemoteConnector(NetContainer container) {
		this.container = container;
		throw new UnsupportedOperationException();
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
	}
}
