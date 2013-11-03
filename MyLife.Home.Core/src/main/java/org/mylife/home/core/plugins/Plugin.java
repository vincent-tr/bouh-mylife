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
	 */
	public void terminate();

	/**
	 * Obtention des metadata de config au design
	 * @return
	 */
	public PluginDesignMetadata getDesignMetadata();
}
