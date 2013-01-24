package mylife.home.hw.driver;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.Options;
import mylife.home.hw.api.OutputDevice;

class Manager {

	private Manager() {
	}

	private static final Manager instance = new Manager();

	public static Manager getInstance() {
		return instance;
	}

	private final Map<Integer, Device> devices = new HashMap<Integer, Device>();

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

		if (pinId < 0 || pinId > 16)
			throw new IllegalArgumentException("Invalid pin Id");

		if (options == null)
			throw new IllegalArgumentException("No option set");

		synchronized (devices) {
			// vérification que le pin ne soit pas déjà utilisé
			if (devices.containsKey(Integer.valueOf(pinId)))
				throw new DeviceAccessDeniedException("Pin #" + pinId
						+ " already in use");

			if (options.contains(Options.DIRECTION_INPUT))
				return openInput(pinId, options);
			if (options.contains(Options.DIRECTION_OUTPUT))
				return openOutput(pinId, options);
		}

		throw new IllegalArgumentException("No direction defined");
	}

	/**
	 * Fermeture de l'accès
	 * 
	 * @param device
	 */
	public void close(Device device) {

		synchronized (devices) {
			devices.remove(Integer.valueOf(device.getPinId()));
		}

	}

	private InputDevice openInput(int pinId, EnumSet<Options> options) {

	}

	private OutputDevice openOutput(int pinId, EnumSet<Options> options) {

	}
}
