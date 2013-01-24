package mylife.home.hw.api;

public interface OutputDevice extends Device {

	/**
	 * D�finition de la valeur du pin
	 */
	public void setValue(Object value);
	
	/**
	 * Obtention de la valeur du pin
	 * @return
	 */
	public Object getValue();
}
