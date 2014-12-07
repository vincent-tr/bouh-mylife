package org.mylife.home.raspberry.gpio;

/**
 * Erreur d'accès sys fs
 * @author pumbawoman
 *
 */
public class SysFsAccessException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2331702096831348484L;


	public SysFsAccessException(String message) {
		super(message);
	}
	
	public SysFsAccessException(Exception cause) {
		super(cause);
	}
}
