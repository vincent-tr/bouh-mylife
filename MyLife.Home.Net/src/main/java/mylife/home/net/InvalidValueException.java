package mylife.home.net;

public class InvalidValueException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8200544797792189883L;

	public InvalidValueException() {
		super("Invalid value");
	}

	public InvalidValueException(Throwable cause) {
		super("Invalid value", cause);
	}
}
