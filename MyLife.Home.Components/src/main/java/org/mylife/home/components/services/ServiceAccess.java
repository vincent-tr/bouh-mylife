package org.mylife.home.components.services;

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
	 * Service de gestion des composants
	 */
	private final ComponentService componentService = register(new ComponentService());

	/**
	 * Service de gestion des composants
	 * 
	 * @return
	 */
	public ComponentService getComponentService() {
		return componentService;
	}

	/**
	 * Service de gestion des configurations
	 */
	private final ConfigurationService configurationService = register(new ConfigurationService());

	/**
	 * Service de gestion des configurations
	 * 
	 * @return
	 */
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
