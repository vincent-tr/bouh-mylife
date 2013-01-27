package mylife.home.hw.api;

/**
 * Interface pour s'abonner � un changement d'�tat du pin 
 * @author pumbawoman
 *
 */
public interface InputDeviceListener {

	/**
	 * Changement d'�tat du pin
	 * @param device
	 * @param state
	 */
	public void stateChanged(InputDevice device, boolean state);
}
