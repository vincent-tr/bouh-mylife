package mylife.home.hw.api;

import java.io.IOException;

/**
 * Exception levée lorsqu'il n'est pas possible de créer un accès à un pin
 * @author pumbawoman
 *
 */
public class DeviceAccessDeniedException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5333433746043132693L;

	public DeviceAccessDeniedException(String message) {
		super(message);
	}
	
}
