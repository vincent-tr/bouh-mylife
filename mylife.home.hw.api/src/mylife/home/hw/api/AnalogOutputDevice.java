package mylife.home.hw.api;

public interface AnalogOutputDevice extends OutputDevice {

	/**
	 * Obtention de la valeur analogique du pin
	 * @return
	 */
	public int getAnalogValue();

	/**
	 * D�finition de la valeur analogique du pin
	 */
	public void setAnalogValue(int value);
}
