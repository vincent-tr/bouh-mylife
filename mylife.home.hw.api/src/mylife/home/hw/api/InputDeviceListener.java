package mylife.home.hw.api;

/**
 * Interface pour s'abonner à un changement d'état du pin 
 * @author pumbawoman
 *
 */
public interface InputDeviceListener {

	/**
	 * Changement d'état du pin
	 * @param device
	 * @param state
	 */
	public void stateChanged(InputDevice device, boolean state);
}
