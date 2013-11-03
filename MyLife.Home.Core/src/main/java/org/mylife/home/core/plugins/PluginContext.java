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
	public void publishNetObject(NetObject obj);
	
	/**
	 * Le plugin a été instancié pour servir à l'exécution
	 */
	public final static int PURPOSE_RUNTIME = 1;
	
	/**
	 * Le plugin a été instancié pour servir au design
	 */
	public final static int PURPOSE_DESIGN = 2;
	
	/**
	 * Obtention de la raison de l'instanciation du plugin
	 * @return
	 */
	public int getPurpose();
	
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
