package mylife.home.hw.api;

import java.io.IOException;

/**
 * Exception lev�e lorsqu'il n'est pas possible de cr�er un acc�s � un pin
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
