package org.mylife.home.components.providers;

/**
 * Fabrique de composant
 * 
 * @author pumbawoman
 * 
 */
public interface ComponentFactory {

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
	 * Création d'un composant
	 * 
	 * @return
	 */
	public Component create() throws Exception;
}
