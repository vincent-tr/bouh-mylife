package org.mylife.home.core.plugins.enhanced.annotations;

/**
 * Au design indique une liste de valeurs supportées
 * 
 * @author pumbawoman
 * 
 */
public @interface PluginPossibleValues {

	/**
	 * Liste des valeurs
	 * 
	 * @return
	 */
	String[] values();
}
