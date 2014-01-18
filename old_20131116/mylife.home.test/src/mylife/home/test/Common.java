package mylife.home.test;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Common {

	public static GpioPinDigitalOutput getPin() {
		
		System.out.println("Starting");
		
		final GpioController controller = GpioFactory.getInstance();
		final GpioPinDigitalOutput pin = controller.provisionDigitalOutputPin(RaspiPin.GPIO_06, PinState.LOW);
		
		return pin;

	}

}
