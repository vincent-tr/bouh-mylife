package mylife.home.hw.api;

public interface DigitalOutputDevice extends OutputDevice {

	/**
	 * Obtention de la valeur digiale du pin
	 * @return
	 */
	public boolean getDigitalValue();

	/**
	 * D�finition de la valeur digitale du pin
	 */
	public void setDigitalValue(boolean value);
}
