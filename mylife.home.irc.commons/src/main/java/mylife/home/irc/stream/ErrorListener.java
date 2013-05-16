package mylife.home.irc.stream;

/**
 * Listener d'erreur
 * @author pumbawoman
 *
 */
public interface ErrorListener {

	/**
	 * Une erreur s'est produite
	 * @param e
	 */
	public void errorOccured(Exception e);
}
