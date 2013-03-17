package mylife.home.hw.api;

import java.util.EnumSet;

/**
 * Repr�sentation de l'acc�s � un pin.
 * Appeler close en fin d'utilisation
 * @author pumbawoman
 *
 */
public interface Device {

	/**
	 * Obtention de l'identifiant du mat�riel
	 * @return
	 */
	public int getPinId();
	
	/**
	 * Obtention du nom du mat�riel
	 * @return
	 */
	public String getName();
	
	/**
	 * Obtention des obtiens
	 * @return
	 */
	public EnumSet<Options> getOptions();
	
	/**
	 * Fermeture du mat�riel
	 */
	public void close() throws Exception;
}
