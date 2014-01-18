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
	
	private final Object sync = new Object();

	public AnalogOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options);
		pwm = new SysFS(getGpioId(), "/sys/class/soft_pwm", "export", "unexport", "pwm");
		onOff = new SysFS(getGpioId(), "/sys/class/gpio", "export", "unexport", "gpio");
		
		try {
			setValue(0);
		} catch (RuntimeException ex) {
			reset();
			throw ex;
		}
	}

	@Override
	protected void reset() {
		synchronized(sync) {
			
			setValue(0);
			
			if(pwm.isOpened())
				pwm.close();
			if(onOff.isOpened())
				onOff.close();
		}
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue(int value) {
		synchronized(sync) {
			
			if (value < 0)
				throw new IllegalArgumentException("value must be >= 0");
			if (value > range)
				throw new IllegalArgumentException("value must be <= " + range);
	
			if(value == 0)
				setDigital(false);
			else if(value == 100)
				setDigital(true);
			else
				setAnalog(value);
			
			this.value = value;
		}
	}
	
	private void setDigital(boolean value) {
		
		if(pwm.isOpened())
			pwm.close();
		
		if(!onOff.isOpened()) {
			onOff.open();
			onOff.writeValue("direction", "out");
		}
		
		onOff.writeValue("value", value ? "1" : "0");
	}
	
	private void setAnalog(int value) {
		
		if(onOff.isOpened())
			onOff.close();
		
		if(!pwm.isOpened()) {
			pwm.open();
			pwm.writeValue("period", "10000");
		}
		
		pwm.writeValue("pulse", "" + (range * value));
	}
}
