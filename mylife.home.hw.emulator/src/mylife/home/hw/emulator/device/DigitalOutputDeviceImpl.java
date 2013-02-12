package mylife.home.hw.emulator.device;

import java.util.EnumSet;

import mylife.home.hw.api.DigitalOutputDevice;
import mylife.home.hw.api.Options;

/**
 * Implémentation de DigitalOutputDevice
 * @author pumbawoman
 *
 */
public class DigitalOutputDeviceImpl extends DeviceImpl implements
		DigitalOutputDevice {

	/**
	 * Valeur
	 */
	private boolean value;

	/**
	 * Constructeur avec données
	 * @param pinId
	 * @param options
	 */
	public DigitalOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options);
	}


	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public void setValue(boolean value) {
		
		this.value = value;
		// a voir si besoin de propager la modification
	}

	/**
	 * Type de device
	 */
	private static final String type = "DigitalOutput";
	
	/**
	 * Obtention du type de device
	 * @return
	 */
	@Override
	protected String getType() {
		return type;
	}
}
