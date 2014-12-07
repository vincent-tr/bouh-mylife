package org.mylife.home.core.plugins.design;

import java.util.Collection;

/**
 * Métadonnées de design d'un plugin
 * 
 * @author pumbawoman
 * 
 */
public interface PluginDesignMetadata {

	/**
	 * Indique si le plugin est considéré comme un plugin d'ui
	 * 
	 * @return
	 */
	public boolean isUi();

	/**
	 * Obtention de l'image à afficher pour le plugin, ou null si pas d'image
	 * 
	 * @return
	 */
	public byte[] getImage();

	/**
	 * Métadonnées de configuration
	 * 
	 * @return
	 */
	public Collection<PluginDesignConfiguration> getConfigurationData();

	/**
	 * Métadonnées des attributs
	 * 
	 * @return
	 */
	public Collection<PluginDesignAttribute> getAttributes();

	/**
	 * Métadonnées des actions
	 * 
	 * @return
	 */
	public Collection<PluginDesignAction> getActions();
}
