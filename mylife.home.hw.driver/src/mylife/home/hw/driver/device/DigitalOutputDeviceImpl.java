package mylife.home.hw.driver.device;

import java.io.File;
import java.util.EnumSet;

import mylife.home.hw.api.DigitalOutputDevice;
import mylife.home.hw.api.Options;

public class DigitalOutputDeviceImpl extends SysFSDeviceImpl implements
		DigitalOutputDevice {

	/**
	 * Valeur
	 */
	private boolean value;

	public DigitalOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options, "/sys/class/gpio", "export", "unexport", "gpio");
		try {
			write(getItemDirectoryPath() + File.separator + "direction", "out");
			setValue(false);
		} catch (Exception ex) {
			reset();
			throw ex;
		}
	}

	@Override
	protected void reset() {
		setValue(false);
		super.reset();
	}

	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public void setValue(boolean value) {
		write(getItemDirectoryPath() + File.separator + "value", value ? "1"
				: "0");
		this.value = value;
	}

}
