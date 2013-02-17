package mylife.home.net.api;

/**
 * Composant de communication
 * @author pumbawoman
 *
 */
public interface NetComponent {

	/**
	 * Identifiant du composant
	 * @return
	 */
	public String getComponentId();
	
	/**
	 * Nom d'affichage du composant
	 * @return
	 */
	public String getComponentDisplay();
	
	/**
	 * Type du composant
	 * @return
	 */
	public String getComponentType();
	
	/**
	 * Indique si le composant est connecté
	 * @return
	 */
	public boolean isOnline();
	
	/**
	 * Définition du statut du composant
	 * @param value
	 */
	public void setStatus(String value);
	
	/**
	 * Obtention du statut du composant
	 * @return
	 */
	public String getStatus();
	
	/**
	 * Enregistrement d'une commande
	 * @param verb
	 * @param listener
	 * @return
	 */
	public boolean registerCommand(String verb, CommandListener listener);
	
	/**
	 * Désenregistrement d'une commande
	 * @param verb
	 * @return
	 */
	public boolean unregisterCommand(String verb);
	
	/**
	 * Fermeture du composant
	 */
	public void close();
}
