package org.mylife.home.components.providers;

import java.util.Map;

/**
 * Context d'exécution d'un composant
 * 
 * @author pumbawoman
 * 
 */
public interface ComponentContext {

	/**
	 * Identifiant du composant
	 * @return
	 */
	public String componentId();
	
	/**
	 * Paramètres de création du composant
	 * @return
	 */
	public Map<String, String> parameters();
	
	// TODO : création du netobject
}
