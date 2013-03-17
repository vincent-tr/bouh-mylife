package mylife.home.hw.driver.device;

import java.util.EnumSet;

import mylife.home.hw.api.AnalogOutputDevice;
import mylife.home.hw.api.Options;

public class AnalogOutputDeviceImpl extends DeviceImpl implements
		AnalogOutputDevice {

	/**
	 * Valeur
	 */
	private int value;

	/**
	 * Valeur max
	 */
	private final int range = 100;
	
	private final SysFS pwm;
	private final SysFS onOff;

	public AnalogOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options);
		pwm = new SysFS(getGpioId(), "/sys/class/soft_pwm", "export", "unexport", "pwm");
		onOff = new SysFS(getGpioId(), "/sys/class/gpio", "export", "unexport", "gpio");
		try {
			pwm.open();
			pwm.writeValue("period", "10000");
			onOff.open();
			onOff.writeValue("direction", "out");
			setValue(0);
		} catch (RuntimeException ex) {
			reset();
			throw ex;
		}
	}

	@Override
	protected void reset() {
		if(onOff.isOpened() && pwm.isOpened())
			setValue(0);
		if(pwm.isOpened()) {
			pwm.close();
		}
		if(onOff.isOpened()) {
			onOff.close();
		}
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

		pwm.writeValue("pulse", "" + (range * value));
		if(value == 0)
			onOff.writeValue("value", "0");
		else if(value == 100)
			onOff.writeValue("value", "1");
		this.value = value;
	}

}
