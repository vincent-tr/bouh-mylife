package org.mylife.home.core.services;


/**
 * Accès aux services
 * 
 * @author trumpffv
 * 
 */
public class ServiceAccess {

	/**
	 * Service de gestion des configurations
	 */
	private final static ConfigurationService configurationService = new ConfigurationService();

	/**
	 * Service de gestion des configurations
	 * 
	 * @return
	 */
	public static ConfigurationService getConfigurationService() {
		return configurationService;
	}

	/**
	 * Service de gestion des plugins
	 */
	private final static PluginService pluginService = new PluginService();

	/**
	 * Service de gestion des plugins
	 * 
	 * @return
	 */
	public static PluginService getPluginService() {
		return pluginService;
	}

	/**
	 * Service de gestion du core
	 */
	private final static ManagerService managerService = new ManagerService();

	/**
	 * Service de gestion du core
	 * 
	 * @return
	 */
	public static ManagerService getManagerService() {
		return managerService;
	}
}
