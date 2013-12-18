package org.mylife.home.core.plugins;

import org.mylife.home.core.plugins.design.PluginDesignMetadata;

/**
 * Interface de gestion d'un plugin. L'implémentation doit avoir un constructeur
 * sans paramètre, et initialize est appelé pour initialiser le plugin
 * 
 * @author pumbawoman
 * 
 */
public interface Plugin {

	/**
	 * Initialisation
	 * @param context
	 */
	public void init(PluginContext context) throws Exception;

	/**
	 * Fin d'utilisation
	 */
	public void destroy();

	/**
	 * Obtention des metadata de config au design
	 * @return
	 */
	public PluginDesignMetadata getDesignMetadata();
}