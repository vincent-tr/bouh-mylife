package org.mylife.home.net;

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
	
	/*internal*/ NetContainer(NetObject object, String channel, boolean local) {
		this.object = object;
		this.channel = channel;
		this.local = local;
		if(local)
			connector = new LocalConnector(this);
		else
			connector = new RemoteConnector(this);
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
	}
	
	/*internal*/ void close() {
		connector.close();
	}
}

