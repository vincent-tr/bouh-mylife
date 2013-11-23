package org.mylife.home.common.services;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestion des services
 * 
 * @author pumbawoman
 * 
 */
public abstract class BaseServiceAccess {

	/**
	 * Liste des services utilisés
	 */
	private final List<Service> services = new ArrayList<Service>();

	/**
	 * Enregistrement des services
	 * 
	 * @param service
	 * @return
	 */
	protected synchronized <T extends Service> T register(T service) {
		services.add(service);
		return service;
	}

	/**
	 * Fin d'utilisation
	 */
	public synchronized void terminate() {
		for (Service service : services) {
			service.terminate();
		}
	}

	/**
	 * Service de gestion des logs
	 */
	private final LoggerService loggerService = register(new LoggerService());

	/**
	 * Service de gestion des logs
	 * 
	 * @return
	 */
	public LoggerService getLoggerService() {
		return loggerService;
	}
	
	/**
	 * Constructeur appelé du singleton : définition de l'instance de base
	 */
	protected BaseServiceAccess() {
		instance = this;
	}
	
	/**
	 * Instance de base
	 */
	private static BaseServiceAccess instance;
	
	/**
	 * Obtention de l'instance de base utilisée pour les composants commmuns
	 * @return
	 */
	public static BaseServiceAccess getBaseInstance() {
		return instance;
	}
}
