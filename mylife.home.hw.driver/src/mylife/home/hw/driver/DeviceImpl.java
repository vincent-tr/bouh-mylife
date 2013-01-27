package mylife.home.hw.driver;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.Options;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;

/**
 * Implémentation de bsae
 * 
 * @author pumbawoman
 * 
 */
public abstract class DeviceImpl implements Device {

	/**
	 * Objet fermé
	 */
	private boolean closed;

	/**
	 * Verrou pour opérations
	 */
	private final Object operationSync = new Object();

	@Override
	public void close() throws Exception {

		synchronized (operationSync) {

			checkClosed();

			// remise à 0 du pin
			reset();

			// suppression de l'utilisation
			Manager.getInstance().close(this);
		}
	}
	
	/**
	 * RAZ du pin
	 */
	protected void reset() {
		pin.setMode(PinMode.DIGITAL_INPUT);
		pin.setPullResistance(PinPullResistance.OFF);
		pin.unexport();
		GpioFactory.getInstance().unprovisionPin(pin);
	}

	/**
	 * Vérifie si l'objet est fermé
	 */
	protected void checkClosed() {
		if (closed)
			throw new IllegalStateException("Object closed");
	}
	
	private final int pinId;
	private final String name;
	private final EnumSet<Options> options;
	private final GpioPin pin;
	
	protected DeviceImpl(int pindId, EnumSet<Options> options, GpioPin pin) {
		this.pinId = pindId;
		this.options = options;
		this.pin = pin;
		
		// création du nom
		StringBuffer build = new StringBuffer();
		build.append("pin #");
		build.append(pinId);
		build.append(" (");
		boolean firstOption = true;
		for(Options option : options) {
			if(firstOption)
				firstOption = false;
			else
				build.append(",");
			build.append(option.getName());
		}
		build.append(")");
		name = build.toString();
	}

	@Override
	public int getPinId() {
		return pinId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public EnumSet<Options> getOptions() {
		return options;
	}
	
	protected GpioPin getPin() {
		return pin;
	}

	protected static Pin getPin(int pinId) {
		switch(pinId) {
		case 0: return RaspiPin.GPIO_00;
		case 1: return RaspiPin.GPIO_01;
		case 2: return RaspiPin.GPIO_02;
		case 3: return RaspiPin.GPIO_03;
		case 4: return RaspiPin.GPIO_04;
		case 5: return RaspiPin.GPIO_05;
		case 6: return RaspiPin.GPIO_06;
		case 7: return RaspiPin.GPIO_07;
		case 8: return RaspiPin.GPIO_08;
		case 9: return RaspiPin.GPIO_09;
		case 10: return RaspiPin.GPIO_10;
		case 11: return RaspiPin.GPIO_11;
		case 12: return RaspiPin.GPIO_12;
		case 13: return RaspiPin.GPIO_13;
		case 14: return RaspiPin.GPIO_14;
		case 15: return RaspiPin.GPIO_15;
		case 16: return RaspiPin.GPIO_16;
		default: throw new UnsupportedOperationException();
		}
	}
}
