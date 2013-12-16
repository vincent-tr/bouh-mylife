package org.mylife.home.components.providers;

/**
 * Interface d'un composant
 * 
 * @author pumbawoman
 * 
 */
public interface Component {

	/**
	 * Initialisation du composant
	 * 
	 * @throws Exception
	 */
	public void init(ComponentContext context) throws Exception;

	/**
	 * Suppression du composant
	 */
	public void destroy();

}
