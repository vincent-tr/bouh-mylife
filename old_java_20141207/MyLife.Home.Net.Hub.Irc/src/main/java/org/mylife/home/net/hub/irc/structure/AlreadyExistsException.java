package org.mylife.home.net.hub.irc.structure;

public class AlreadyExistsException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9115562306592061314L;

	public AlreadyExistsException() {
		super("The specified object already exists");
	}
}
