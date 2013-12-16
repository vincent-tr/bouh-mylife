package org.mylife.home.components.providers;

import java.util.Map;

import org.mylife.home.net.NetObject;

/**
 * Context d'exécution d'un composant
 * 
 * @author pumbawoman
 * 
 */
public interface ComponentContext {

	/**
	 * Identifiant du composant
	 * 
	 * @return
	 */
	public String componentId();

	/**
	 * Composant
	 * 
	 * @return
	 */
	public Component component();

	/**
	 * Paramètres de création du composant
	 * 
	 * @return
	 */
	public Map<String, String> parameters();

	/**
	 * Enregistrement de l'objet correspondant au composant. L'objet est ensuite publie et depublie automtiquement
	 * @param object
	 * @param channel
	 */
	public void registerObject(NetObject object, String channel);

	/**
	 * Enregistrement de l'objet correspondant au composant. L'objet est ensuite publie et depublie automtiquement
	 * @param object
	 */
	public void registerObject(NetObject object);
	
	/**
	 * Obtention de l'objet publie correspondant au composant
	 * @return
	 */
	public NetObject getObject();
}
