package mylife.home.hw.emulator.device;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.InputDeviceListener;
import mylife.home.hw.api.Options;

/**
 * Implémentation de InputDevice
 * @author pumbawoman
 *
 */
public class InputDeviceImpl extends DeviceImpl implements InputDevice {

	/**
	 * Valeur
	 */
	private boolean value;

	/**
	 * Constructeur avec données
	 * @param pinId
	 * @param options
	 */
	public InputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options);
	}

	@Override
	public boolean getValue() {
		return value;
	}
	
	/**
	 * Définition de la valeur
	 */
	public void setValue(boolean value) {
		if(this.value == value)
			return;
		
		this.value = value;

		InputDeviceListener[] listenersCopy;

		synchronized (listeners) {
			listenersCopy = listeners.toArray(new InputDeviceListener[0]);
		}

		for (InputDeviceListener listener : listenersCopy) {
			listener.stateChanged(this, value);
		}
	}

	/**
	 * Liste des listeners abonnés au changement de statut
	 */
	private final Set<InputDeviceListener> listeners = new HashSet<InputDeviceListener>();

	@Override
	public void addListener(InputDeviceListener listener) {

		if (listener == null)
			throw new IllegalArgumentException("listener is null");

		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeListener(InputDeviceListener listener) {

		if (listener == null)
			throw new IllegalArgumentException("listener is null");

		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Type de device
	 */
	private static final String type = "Input";
	
	/**
	 * Obtention du type de device
	 * @return
	 */
	@Override
	public String getType() {
		return type;
	}
	
	/**
	 * Obtention du status du device
	 * @return
	 */
	public String getStatus() {
		return value ? "on" : "off";
	}
}
