package mylife.home.hw.driver;

import java.util.EnumSet;

import mylife.home.hw.api.DigitalOutputDevice;
import mylife.home.hw.api.Options;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class DigitalOutputDeviceImpl extends DeviceImpl implements DigitalOutputDevice {

	/**
	 * Valeur
	 */
	private boolean value;
	
	private static GpioPin createPin(int pinId) {
		GpioController controller = GpioFactory.getInstance();
		return controller.provisionDigitalOutputPin(getPin(pinId));
	}

	public DigitalOutputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options, createPin(pinId));
		setValue(false);
	}

	@Override
	public boolean getValue() {
		return value;
	}

	@Override
	public void setValue(boolean value) {
		((GpioPinDigitalOutput)getPin()).setState(value);
		this.value = value;
	}

}
