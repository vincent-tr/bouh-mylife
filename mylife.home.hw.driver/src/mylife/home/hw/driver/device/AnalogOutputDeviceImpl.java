package mylife.home.hw.driver.device;

import java.io.File;
import java.util.EnumSet;

import mylife.home.hw.api.AnalogOutputDevice;
import mylife.home.hw.api.Options;

public class AnalogOutputDeviceImpl extends SysFSDeviceImpl implements
		AnalogOutputDevice {

	/**
	 * Valeur
	 */
	private int value;

	/**
	 * Valeur max
	 */
	private final int range = 100;

	public AnalogOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options, "/sys/class/soft_pwm", "export", "unexport",
				"pwm");
		try {
			write(getItemDirectoryPath() + File.separator + "period", "10000");
			setValue(0);
		} catch (Exception ex) {
			reset();
			throw ex;
		}
	}

	@Override
	protected void reset() {
		setValue(0);
		super.reset();
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

		write(getItemDirectoryPath() + File.separator + "pulse", ""
				+ (range * value));
		this.value = value;
	}

}
