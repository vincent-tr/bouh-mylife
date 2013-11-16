package mylife.home.hw.driver.device;

import mylife.home.hw.driver.platform.PlatformFile;

/**
 * Interface d'un �l�ment pollable
 * @author pumbawoman
 *
 */
public interface Pollable {
	
	/**
	 * Otention du fichier
	 * @return
	 */
	public PlatformFile getFile();
	
	/**
	 * Otention des evenements � checker
	 * @return
	 */
	public short getCheckedEvents();
	
	/**
	 * Intervient lorsqu'un evenement est lev�
	 * @param events
	 */
	public void setEvents(short events);
}
