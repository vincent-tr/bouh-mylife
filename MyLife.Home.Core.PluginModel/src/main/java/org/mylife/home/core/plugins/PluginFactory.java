package org.mylife.home.core.plugins;

import org.mylife.home.core.plugins.design.PluginDesignMetadata;


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


	/**
	 * Obtention des metadata de config au design
	 * @return
	 */
	public PluginDesignMetadata getDesignMetadata();
}
