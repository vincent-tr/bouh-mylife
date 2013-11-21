package org.mylife.home.net.hub.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mylife.home.common.services.Service;
import org.mylife.home.net.hub.data.DataLink;
import org.mylife.home.net.hub.data.DataLinkAccess;

/**
 * Service de gestion des liens
 * 
 * @author pumbawoman
 * 
 */
public class LinkService implements Service {

	private final Map<String, String> types;

	/* internal */LinkService() {

		Map<String, String> map = new HashMap<String, String>();
		map.put("accept", "Accept");
		map.put("connect", "Connect");
		types = Collections.unmodifiableMap(map);
	}

	@Override
	public void terminate() {
	}

	public void create(DataLink link) {
		checkType(link);

		DataLinkAccess access = new DataLinkAccess();
		try {
			access.createLink(link);
		} finally {
			access.close();
		}
	}

	public void update(DataLink link) {
		checkType(link);

		DataLinkAccess access = new DataLinkAccess();
		try {
			DataLink item = access.getLinkByKey(link.getId());
			item.setAddress(link.getAddress());
			item.setPort(link.getPort());
			item.setPassword(link.getPassword());
			item.setRetryInterval(link.getRetryInterval());
			access.updateLink(item);
		} finally {
			access.close();
		}
	}

	public void delete(int id) {
		DataLinkAccess access = new DataLinkAccess();
		try {
			DataLink item = new DataLink();
			item.setId(id);
			access.deleteLink(item);
		} finally {
			access.close();
		}
	}

	public List<DataLink> list() {
		DataLinkAccess access = new DataLinkAccess();
		try {
			return access.getLinksAll();
		} finally {
			access.close();
		}

	}

	private void checkType(DataLink link) {
		if (!types.containsKey(link.getType()))
			throw new UnsupportedOperationException("Unknown type");
	}

	public Map<String, String> listTypes() {
		return types;
	}
}
