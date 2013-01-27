package mylife.home.hw.api;

public interface DigitalOutputDevice extends Device {

	/**
	 * Obtention de la valeur du pin
	 * @return
	 */
	public boolean getValue();

	/**
	 * D�finition de la valeur du pin
	 */
	public void setValue(boolean value);
}
