package mylife.home.hw.api;

public interface AnalogOutputDevice extends Device {

	/**
	 * Obtention de la valeur du pin
	 * @return
	 */
	public int getValue();

	/**
	 * Définition de la valeur du pin
	 */
	public void setValue(int value);
}
