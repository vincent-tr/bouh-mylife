package org.mylife.home.components;

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
	public void init() throws Exception;

	/**
	 * Suppression du composant
	 */
	public void destroy();

}
