package org.mylife.home.components;

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
	 * Création d'un composant
	 * 
	 * @param context
	 * @return
	 */
	public Component create(ComponentContext context);
}
