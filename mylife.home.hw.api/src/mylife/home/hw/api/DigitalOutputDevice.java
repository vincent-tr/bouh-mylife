package mylife.home.hw.api;

public interface DigitalOutputDevice extends OutputDevice {

	/**
	 * Obtention de la valeur digiale du pin
	 * @return
	 */
	public boolean getDigitalValue();

	/**
	 * Définition de la valeur digitale du pin
	 */
	public void setDigitalValue(boolean value);
}
