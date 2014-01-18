package mylife.home.hw.api;

public interface InputDevice extends Device {

	/**
	 * Obtention de la valeur du pin
	 * @return
	 */
	public boolean getValue();
	
	/**
	 * Enregistrement d'un abonn�
	 * @param listener
	 */
	public void addListener(InputDeviceListener listener);
	
	/**
	 * D�senregistrement d'un abonn�
	 * @param listener
	 */
	public void removeListener(InputDeviceListener listener);
	
}
