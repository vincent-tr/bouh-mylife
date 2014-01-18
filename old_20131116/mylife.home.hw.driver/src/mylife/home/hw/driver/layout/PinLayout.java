package mylife.home.hw.driver.layout;

/**
 * Layout des pins du raspberry
 * @author pumbawoman
 *
 */
public interface PinLayout {

	/**
	 * Indique si le pin est valide
	 * @param pinId
	 * @return
	 */
	public boolean isValid(int pinId);
	
	/**
	 * Donne le numéro de version correspondant au pin
	 * @param pinId
	 * @return
	 */
	public int pinToGpio(int pinId);
}
