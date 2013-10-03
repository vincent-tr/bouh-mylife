package org.mylife.home.raspberry.gpio;

/**
 * Exception produite lorsque l'exécution de l'outil d'admin des gpio provoque une exception
 * @author pumbawoman
 *
 */
public class AdminToolException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7835263245189250116L;

	public AdminToolException(String message) {
		super(message);
	}
	
	public AdminToolException(Exception cause) {
		super(cause);
	}
}
