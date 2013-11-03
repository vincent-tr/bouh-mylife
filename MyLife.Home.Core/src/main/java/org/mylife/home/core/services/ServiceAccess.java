package org.mylife.home.core.services;

import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux services
 * 
 * @author trumpffv
 * 
 */
public class ServiceAccess {

	/**
	 * Liste des services utilisés
	 */
	private static List<Service> services;

	/**
	 * Enregistrement des services
	 * 
	 * @param service
	 * @return
	 */
	private synchronized static <T extends Service> T register(T service) {
		if (services == null)
			services = new ArrayList<Service>();
		services.add(service);
		return service;
	}

	/**
	 * Fin d'utilisation
	 */
	public synchronized static void terminate() {
		if (services != null) {
			for (Service service : services) {
				service.terminate();
			}
		}
	}

	/**
	 * Service de gestion des configurations
	 */
	private final static ConfigurationService configurationService = register(new ConfigurationService());

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
	private final static PluginService pluginService = register(new PluginService());

	/**
	 * Service de gestion des plugins
	 * 
	 * @return
	 */
	public static PluginService getPluginService() {
		return pluginService;
	}

	/**
	 * Service de gestion de la persistance des plugins
	 */
	private final static PluginPersistanceService pluginPersistanceService = register(new PluginPersistanceService());

	/**
	 * Service de gestion de la persistance des plugins
	 * 
	 * @return
	 */
	public static PluginPersistanceService getPluginPersistanceService() {
		return pluginPersistanceService;
	}

	/**
	 * Service de gestion du core
	 */
	private final static ManagerService managerService = register(new ManagerService());

	/**
	 * Service de gestion du core
	 * 
	 * @return
	 */
	public static ManagerService getManagerService() {
		return managerService;
	}
}
