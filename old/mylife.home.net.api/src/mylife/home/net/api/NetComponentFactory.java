package mylife.home.net.api;

/**
 * Fabrique de composants
 * @author pumbawoman
 *
 */
public interface NetComponentFactory {
	
	/**
	 * Cr�ation d'un composant
	 * @param componentId
	 * @param componentDisplay
	 * @param componentType
	 * @return
	 */
	public NetComponent createComponent(String componentId, String componentDisplay, String componentType);
	
}
