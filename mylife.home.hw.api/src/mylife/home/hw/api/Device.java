package mylife.home.hw.api;

import java.util.EnumSet;

/**
 * Représentation de l'accès à un pin.
 * Appeler close en fin d'utilisation
 * @author pumbawoman
 *
 */
public interface Device extends AutoCloseable {

	/**
	 * Obtention de l'identifiant du matériel
	 * @return
	 */
	public int getPinId();
	
	/**
	 * Obtention du nom du matériel
	 * @return
	 */
	public String getName();
	
	/**
	 * Obtention des obtiens
	 * @return
	 */
	public EnumSet<Options> getOptions();
}
