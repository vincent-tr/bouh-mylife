package mylife.home.hw.api;

import java.util.EnumSet;

/**
 * Gestionnaire de pins
 * @author pumbawoman
 *
 */
public interface DeviceManager {

	/**
	 * Ouverture d'un pin
	 * @param pinId
	 * @param options
	 * @return
	 */
	public Device open(int pinId, EnumSet<Options> options) throws DeviceAccessDeniedException;
}
