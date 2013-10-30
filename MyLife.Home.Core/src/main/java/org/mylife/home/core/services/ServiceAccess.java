package org.mylife.home.core.services;


/**
 * Accès aux services
 * 
 * @author trumpffv
 * 
 */
public class ServiceAccess {

	/**
	 * Service de configuration
	 */
	private final static ConfigurationService configurationService = new ConfigurationService();

	/**
	 * Service de configuration
	 * 
	 * @return
	 */
	public static ConfigurationService getConfigurationService() {
		return configurationService;
	}
}
