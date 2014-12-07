package org.mylife.home.core.plugins.enhanced;

/**
 * Indique que le plugin est invalide
 * 
 * @author pumbawoman
 * 
 */
public class InvalidPluginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8710291458201080200L;

	public InvalidPluginException(String message) {
		super(message);
	}

	public InvalidPluginException(String message, Throwable cause) {
		super(message, cause);
	}
}
