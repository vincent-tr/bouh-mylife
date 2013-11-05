package org.mylife.home.net;


/**
 * Sur changement de connexion
 * 
 * @author pumbawoman
 * 
 */
public interface ConnectedChangeListener {

	void connectedChanged(NetContainer container, boolean isConnected);
}
