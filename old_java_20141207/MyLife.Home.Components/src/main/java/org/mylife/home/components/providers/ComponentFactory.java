package org.mylife.home.components.providers;

import java.util.Collection;

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
	 * Obtention si supporté de la liste des noms de paramètres supportés
	 * 
	 * @return
	 */
	public Collection<String> getParameterNames();

	/**
	 * Création d'un composant
	 * 
	 * @return
	 */
	public Component create() throws Exception;
}
