package org.mylife.home.core.plugins;


/**
 * Fabrique de plugins
 * 
 * @author pumbawoman
 * 
 */
public interface PluginFactory {

	/**
	 * Obtention du type de composant
	 * 
	 * @return
	 */
	public String getType();

	/**
	 * Obtention du type de composant, pour l'affichage
	 * 
	 * @return
	 */
	public String getDisplayType();

	/**
	 * Création d'un plugin
	 * 
	 * @return
	 */
	public Plugin create() throws Exception;

}
