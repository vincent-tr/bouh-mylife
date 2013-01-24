package mylife.home.hw.driver;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.Options;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;

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
	
	protected DeviceImpl(int pindId, String name, EnumSet<Options> options, GpioPin pin) {
		this.pinId = pindId;
		this.name = name;
		this.options = options;
		this.pin = pin;
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

}
