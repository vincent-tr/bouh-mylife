package org.mylife.home.ui.services;

import org.mylife.home.common.services.BaseServiceAccess;

/**
 * Accès aux services
 * 
 * @author trumpffv
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
	 * Service de gestion du dispatcher
	 */
	private final DispatcherService dispatcherService = register(new DispatcherService());

	/**
	 * Service de gestion du dispatcher
	 * 
	 * @return
	 */
	public DispatcherService getDispatcherService() {
		return dispatcherService;
	}

	/**
	 * Service de gestion du réseau
	 */
	private final NetService netService = register(new NetService());

	/**
	 * Service de gestion du réseau
	 * 
	 * @return
	 */
	public NetService getNetService() {
		return netService;
	}
}
