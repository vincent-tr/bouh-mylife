package org.mylife.home.core.plugins;

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
	public void initialize(PluginContext context);

	/**
	 * Fin d'utilisation
	 * @param context
	 */
	public void terminate(PluginContext context);

	// TODO : obtention des metadata de config au design
	
}
