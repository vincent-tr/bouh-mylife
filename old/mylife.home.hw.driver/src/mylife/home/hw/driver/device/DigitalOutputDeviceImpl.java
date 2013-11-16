package mylife.home.hw.driver.device;

import java.util.EnumSet;

import mylife.home.hw.api.DigitalOutputDevice;
import mylife.home.hw.api.Options;

public class DigitalOutputDeviceImpl extends DeviceImpl implements
		DigitalOutputDevice {

	/**
	 * Valeur
	 */
	private boolean value;
	
	private final SysFS sys; 

	public DigitalOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options);
		sys = new SysFS(getGpioId(), "/sys/class/gpio", "export", "unexport", "gpio");
		try {
			sys.open();
			sys.writeValue("direction", "out");
			setValue(false);
		} catch (RuntimeException ex) {
			reset();
			throw ex;
		}
	}

	@Override
	protected void reset() {
		if(sys.isOpened()) {
			setValue(false);
			sys.close();
		}
	}

	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public void setValue(boolean value) {
		sys.writeValue("value", value ? "1" : "0");
		this.value = value;
	}

}
