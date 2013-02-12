package mylife.home.hw.emulator.device;

import java.util.EnumSet;

import mylife.home.hw.api.AnalogOutputDevice;
import mylife.home.hw.api.Options;

/**
 * Implémentation de AnalogOutputDevice
 * @author pumbawoman
 *
 */
public class AnalogOutputDeviceImpl extends DeviceImpl implements
		AnalogOutputDevice {

	/**
	 * Valeur
	 */
	private int value;

	/**
	 * Valeur max
	 */
	public final int range = 100;

	/**
	 * Constructeur avec données
	 * @param pinId
	 * @param options
	 */
	public AnalogOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options);
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue(int value) {
		if (value < 0)
			throw new IllegalArgumentException("value must be >= 0");
		if (value > range)
			throw new IllegalArgumentException("value must be <= " + range);
		
		this.value = value;
		// a voir si besoin de propager la modification
	}

	/**
	 * Type de device
	 */
	private static final String type = "AnalogOuput";
	
	/**
	 * Obtention du type de device
	 * @return
	 */
	@Override
	protected String getType() {
		return type;
	}
}
