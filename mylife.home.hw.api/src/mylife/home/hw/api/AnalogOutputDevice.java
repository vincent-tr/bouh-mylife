package mylife.home.hw.api;

public interface AnalogOutputDevice extends Device {

	/**
	 * Obtention de la valeur du pin
	 * @return
	 */
	public int getValue();

	/**
	 * D�finition de la valeur du pin
	 */
	public void setValue(int value);
}
