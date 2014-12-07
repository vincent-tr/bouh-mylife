package org.mylife.home.core.plugins;

import java.util.Map;

import org.mylife.home.net.NetObject;

/**
 * Gestion du contexte d'utilisation d'un plugin
 * @author pumbawoman
 *
 */
public interface PluginContext {

	/**
	 * Obtention de l'identifiant de l'objet représenté par le plugin
	 * @return
	 */
	public String getId();
	
	/**
	 * Publication d'un objet
	 * @param obj
	 */
	public void publishObject(NetObject obj, boolean ui);
	
	/**
	 * Obtention de la configuration du plugin
	 * @return
	 */
	public Map<String, String> getConfiguration();
	
	/**
	 * Obtention des données persistées
	 * @return
	 */
	public Map<String, String> getPersistance();
	
	/**
	 * Enregistrement des données persistées
	 * @param data
	 */
	public void savePersistance(Map<String, String> data);
}
