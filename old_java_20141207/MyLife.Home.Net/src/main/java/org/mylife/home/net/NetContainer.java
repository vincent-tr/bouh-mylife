package org.mylife.home.net;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Conteneur d'objet du dépot
 * @author pumbawoman
 *
 */
public class NetContainer
{
	private final NetObject object;
	private final String channel;
	private final boolean local;
	private boolean connected;
	private final Connector connector;
	private final Collection<ConnectedChangeListener> listeners = new ArrayList<ConnectedChangeListener>();
	
	/*internal*/ NetContainer(NetObject object, String channel, boolean local) {
		this.object = object;
		this.channel = channel;
		this.local = local;
		if(local)
			connector = new LocalConnector(this);
		else
			connector = new RemoteConnector(this);
	}

	public void registerConnectedChange(ConnectedChangeListener listener) {
		listeners.add(listener);
	}

	public void unregisterConnectedChange(ConnectedChangeListener listener) {
		listeners.remove(listener);
	}
	
	public NetObject getObject() {
		return object;
	}

	public String getChannel() {
		return channel;
	}

	public boolean isLocal() {
		return local;
	}

	public boolean isConnected() {
		return connected;
	}

	/*internal*/ void setConnected(boolean connected) {
		this.connected = connected;
		for (ConnectedChangeListener listener : listeners)
			listener.connectedChanged(this, this.connected);
	}
	
	/*internal*/ void close() {
		connector.close();
	}
}

