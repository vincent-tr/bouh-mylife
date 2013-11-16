package org.mylife.home.net.hub.services;

import java.util.List;

import org.mylife.home.common.services.Service;
import org.mylife.home.net.hub.data.DataLink;
import org.mylife.home.net.hub.data.DataLinkAccess;

/**
 * Service de gestion des liens
 * @author pumbawoman
 *
 */
public class LinkService implements Service {

	/* internal */LinkService() {

	}

	@Override
	public void terminate() {
	}
	
	public void create(DataLink link) {
		DataLinkAccess access = new DataLinkAccess();
		try {
			access.createLink(link);
		} finally {
			access.close();
		}
	}
	
	public void update(DataLink link) {
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
}
