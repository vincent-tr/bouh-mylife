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
	 * Indique si le composant est connect�
	 * @return
	 */
	public boolean isOnline();
	
	/**
	 * D�finition du statut du composant
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
	 * D�senregistrement d'une commande
	 * @param verb
	 * @return
	 */
	public boolean unregisterCommand(String verb);
	
	/**
	 * Fermeture du composant
	 */
	public void close();
}
