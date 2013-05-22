package mylife.home.irc.server.structure;

/**
 * Représentation d'un mode
 * Cette classe est immuable
 * @author pumbawoman
 *
 */
public class Mode {

	/**
	 * Lettre du mode
	 */
	private final char mode;
	
	/**
	 * Constructeur avec données
	 * @param mode
	 */
	public Mode(char mode) {
		this.mode = mode;
	}

	/**
	 * Lettre du mode
	 * @return
	 */
	public char getMode() {
		return mode;
	}
}
