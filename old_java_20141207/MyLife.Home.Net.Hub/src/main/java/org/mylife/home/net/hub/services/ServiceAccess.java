package org.mylife.home.net.hub.services;

import org.mylife.home.common.services.BaseServiceAccess;

/**
 * Accès aux services
 * 
 * @author pumbawoman
 * 
 */
public class ServiceAccess extends BaseServiceAccess {

	/**
	 * Singleton
	 */
	private final static ServiceAccess instance = new ServiceAccess();

	/**
	 * Singleton
	 * 
	 * @return
	 */
	public final static ServiceAccess getInstance() {
		return instance;
	}

	/**
	 * Singleton
	 */
	private ServiceAccess() {

	}

	/**
	 * Service de gestion des liens
	 */
	private final LinkService linkService = register(new LinkService());

	/**
	 * Service de gestion des liens
	 * 
	 * @return
	 */
	public LinkService getLinkService() {
		return linkService;
	}

	/**
	 * Service de gestion du core
	 */
	private final ManagerService managerService = register(new ManagerService());

	/**
	 * Service de gestion du core
	 * 
	 * @return
	 */
	public ManagerService getManagerService() {
		return managerService;
	}

}
