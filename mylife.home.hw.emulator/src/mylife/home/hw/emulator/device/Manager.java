package mylife.home.hw.emulator.device;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.Options;

public class Manager {

	private Manager() {
	}

	private static final Manager instance = new Manager();

	public static Manager getInstance() {
		return instance;
	}

	private final Map<Integer, DeviceImpl> devices = new HashMap<Integer, DeviceImpl>();

	/**
	 * Obtention des devices ouverts
	 * @return
	 */
	public DeviceImpl[] getOpenedDevices() {
		synchronized (devices) {
			return devices.values().toArray(new DeviceImpl[0]);
		}		
	}

	/**
	 * Obtention d'un device ouvert par son pin, ou null si non ouvert
	 * @param pinId
	 * @return
	 */
	public DeviceImpl getOpenedDevice(int pinId) {
		synchronized (devices) {
			return devices.get(Integer.valueOf(pinId));
		}		
	}
	
	/**
	 * Ouverture de l'accès
	 * 
	 * @param pinId
	 * @param options
	 * @return
	 * @throws IllegalAccessException
	 */
	public DeviceImpl open(int pinId, EnumSet<Options> options)
			throws DeviceAccessDeniedException {

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
	public void close(DeviceImpl device) {

		synchronized (devices) {
			devices.remove(Integer.valueOf(device.getPinId()));
		}

	}

	private DeviceImpl openInput(int pinId, EnumSet<Options> options) {
		if(options.contains(Options.OPTION_PULL_DOWN)
				&& options.contains(Options.OPTION_PULL_UP))
				throw new IllegalArgumentException("Cannot contain both OPTION_PULL_DOWN and OPTION_PULL_UP");
		
		return new InputDeviceImpl(pinId, options);
	}

	private DeviceImpl openOutput(int pinId, EnumSet<Options> options) {
		
		if (options.contains(Options.TYPE_ANALOG))
			return new AnalogOutputDeviceImpl(pinId, options);
		if (options.contains(Options.TYPE_DIGITAL))
			return new DigitalOutputDeviceImpl(pinId, options);
		
		throw new IllegalArgumentException("No type defined");
	}
}
