package mylife.home.hw.driver;

import java.util.EnumSet;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.wiringpi.SoftPwm;

import mylife.home.hw.api.AnalogOutputDevice;
import mylife.home.hw.api.Options;

public class AnalogOutputDeviceImpl extends DeviceImpl implements AnalogOutputDevice {

	/**
	 * Valeur
	 */
	private int value;
	
	/**
	 * Valeur max
	 */
	private final int range = 100;
	
	private static GpioPin createPin(int pinId) {
		GpioController controller = GpioFactory.getInstance();
		return controller.provisionDigitalOutputPin(getPin(pinId));
	}

	public AnalogOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options, createPin(pinId));
		SoftPwm.softPwmCreate(getPinId(), 0, range);
		value = 0;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public void setValue(int value) {
		if(value < 0)
			throw new IllegalArgumentException("value must be >= 0");
		if(value > 100)
			throw new IllegalArgumentException("value must be <= 100");
		
		SoftPwm.softPwmWrite(getPinId(), value);
		this.value = value;
	}

}
