package mylife.home.hw.driver.platform;

public class PlatformException extends RuntimeException {
	
	private static final long serialVersionUID = -4590389676176143337L;

	private final PlatformError error;
	private final String call;
	
	public PlatformException(PlatformError error, String call) {
		super(error.getDescription());
		this.error = error;
		this.call = call;
	}

	/**
	 * Obtention de l'erreur qui s'est produite
	 * @return
	 */
	public PlatformError getError() {
		return error;
	}

	/**
	 * Obtention de l'erreur qui s'est produite
	 * @return
	 */
	public String getCall() {
		return call;
	}
}
