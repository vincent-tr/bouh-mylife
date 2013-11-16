package org.mylife.home.core.services;

import org.mylife.home.common.services.ServiceAccessBase;

/**
 * Accès aux services
 * 
 * @author trumpffv
 * 
 */
public class ServiceAccess extends ServiceAccessBase {

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

	/**
	 * Service de gestion des plugins
	 */
	private final PluginService pluginService = register(new PluginService());

	/**
	 * Service de gestion des plugins
	 * 
	 * @return
	 */
	public PluginService getPluginService() {
		return pluginService;
	}

	/**
	 * Service de gestion de la persistance des plugins
	 */
	private final PluginPersistanceService pluginPersistanceService = register(new PluginPersistanceService());

	/**
	 * Service de gestion de la persistance des plugins
	 * 
	 * @return
	 */
	public PluginPersistanceService getPluginPersistanceService() {
		return pluginPersistanceService;
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
