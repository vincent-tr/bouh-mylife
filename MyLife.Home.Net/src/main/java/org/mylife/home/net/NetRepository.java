package org.mylife.home.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Dépot d'objets
 * 
 * @author pumbawoman
 * 
 */
public final class NetRepository {

	private NetRepository() {
	}

	public static final String CHANNEL_HARDWARE = "mylife-home";
	public static final String CHANNL_UI = "mylife-home-ui";
	public static final String CHANNEL_DEBUG = "mylife-home-debug";

	private final static Collection<NetContainer> objects = new ArrayList<NetContainer>();

	/**
	 * Enregistrement d'un objet
	 * 
	 * @param object
	 * @param channel
	 * @param local
	 * @returns
	 */
	public static NetContainer register(NetObject object, String channel,
			boolean local) {
		NetContainer container = new NetContainer(object, channel, local);
		objects.add(container);
		return container;
	}

	/**
	 * Désenregistrement
	 * 
	 * @param container
	 */
	public static void unregister(NetContainer container) {
		container.close();
		objects.remove(container);
	}

	/**
	 * Obtention des objets
	 * 
	 * @return
	 */
	public static Collection<NetContainer> getObjects() {
		return Collections.unmodifiableCollection(objects);
	}

	public static NetContainer getObjectById(String id) {
		if (id == null)
			return null;
		for (NetContainer obj : objects) {
			if (id.equals(obj.getObject().getId()))
				return obj;
		}
		return null;
	}
}
