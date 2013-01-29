package mylife.home.hw.emulator;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.Options;

public class Manager {

	private Manager() {
	}

	private static final Manager instance = new Manager();

	public static Manager getInstance() {
		return instance;
	}

	/**
	 * Ouverture de l'accès
	 * 
	 * @param pinId
	 * @param options
	 * @return
	 * @throws IllegalAccessException
	 */
	public Device open(int pinId, EnumSet<Options> options)
			throws DeviceAccessDeniedException {
		throw new UnsupportedOperationException();
	}
}
