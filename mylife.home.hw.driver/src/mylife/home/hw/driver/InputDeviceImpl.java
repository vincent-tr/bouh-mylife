package mylife.home.hw.driver;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.InputDeviceListener;
import mylife.home.hw.api.Options;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class InputDeviceImpl extends DeviceImpl implements InputDevice {

	private GpioPinDigitalInput getLocalPin() {
		return (GpioPinDigitalInput) getPin();
	}

	private static GpioPin createPin(int pinId) {
		GpioController controller = GpioFactory.getInstance();
		return controller.provisionDigitalInputPin(getPin(pinId));
	}

	public InputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options, createPin(pinId));
	}

	@Override
	public boolean getValue() {
		return getLocalPin().getState() == PinState.HIGH;
	}

	/**
	 * Map de wrappers
	 */
	private final Map<InputDeviceListener, ListenerWrapper> listenerWrappers = new HashMap<InputDeviceListener, ListenerWrapper>();

	@Override
	public void addListener(InputDeviceListener listener) {

		if (listener == null)
			throw new IllegalArgumentException("listener is null");

		ListenerWrapper wrapper = new ListenerWrapper(listener);
		getLocalPin().addListener(wrapper);

		synchronized (listenerWrappers) {
			listenerWrappers.put(listener, wrapper);
		}
	}

	@Override
	public void removeListener(InputDeviceListener listener) {

		if (listener == null)
			throw new IllegalArgumentException("listener is null");

		ListenerWrapper wrapper = null;

		synchronized (listenerWrappers) {
			wrapper = listenerWrappers.remove(listener);
			if (wrapper == null)
				throw new IllegalArgumentException("listener not found");
		}

		getLocalPin().removeListener(wrapper);
	}

	/**
	 * Wrapper
	 * 
	 * @author pumbawoman
	 * 
	 */
	class ListenerWrapper implements GpioPinListenerDigital {

		private final InputDeviceListener target;

		public ListenerWrapper(InputDeviceListener target) {
			this.target = target;
		}

		@Override
		public void handleGpioPinDigitalStateChangeEvent(
				GpioPinDigitalStateChangeEvent event) {

			target.stateChanged(InputDeviceImpl.this,
					event.getState() == PinState.HIGH);
		}
	}
}
