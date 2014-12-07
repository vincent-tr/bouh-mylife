package org.mylife.home.common.data;

public class DataException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5248345751575014777L;

	public DataException(String message) {
		super(message);
	}
	
	public DataException(Throwable cause) {
		super("Data access exception", cause);
	}
}
