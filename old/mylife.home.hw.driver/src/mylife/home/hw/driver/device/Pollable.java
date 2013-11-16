package mylife.home.hw.driver.device;

import mylife.home.hw.driver.platform.PlatformFile;

/**
 * Interface d'un élément pollable
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
	 * Otention des evenements à checker
	 * @return
	 */
	public short getCheckedEvents();
	
	/**
	 * Intervient lorsqu'un evenement est levé
	 * @param events
	 */
	public void setEvents(short events);
}
