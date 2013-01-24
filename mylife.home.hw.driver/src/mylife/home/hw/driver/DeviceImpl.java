package mylife.home.hw.driver;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.Options;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;

/**
 * Impl�mentation de bsae
 * 
 * @author pumbawoman
 * 
 */
public abstract class DeviceImpl implements Device {

	/**
	 * Objet ferm�
	 */
	private boolean closed;

	/**
	 * Verrou pour op�rations
	 */
	private final Object operationSync = new Object();

	@Override
	public void close() throws Exception {

		synchronized (operationSync) {

			checkClosed();

			// remise � 0 du pin
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
	 * V�rifie si l'objet est ferm�
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
