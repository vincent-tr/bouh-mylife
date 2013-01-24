package mylife.home.hw.api;

public interface AnalogOutputDevice extends OutputDevice {

	/**
	 * Obtention de la valeur analogique du pin
	 * @return
	 */
	public int getAnalogValue();

	/**
	 * Définition de la valeur analogique du pin
	 */
	public void setAnalogValue(int value);
}
