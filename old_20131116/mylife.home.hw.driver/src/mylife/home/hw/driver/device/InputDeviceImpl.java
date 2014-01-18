package mylife.home.hw.driver.device;

import java.io.File;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.InputDeviceListener;
import mylife.home.hw.api.Options;
import mylife.home.hw.driver.platform.PlatformConstants;
import mylife.home.hw.driver.platform.PlatformFile;

public class InputDeviceImpl extends DeviceImpl implements InputDevice, Pollable {

	private final SysFS sys;
	
	public InputDeviceImpl(int pinId, EnumSet<Options> options) {
		super(pinId, options);
		sys = new SysFS(getGpioId(), "/sys/class/gpio", "export", "unexport", "gpio");
		try {
			sys.open();
			sys.writeValue("direction", "in");
			sys.writeValue("edge", "both");
			// TODO : pull resistors
			if (options.contains(Options.OPTION_PULL_DOWN)
					|| options.contains(Options.OPTION_PULL_UP))
				throw new UnsupportedOperationException(
						"Pull resistors unsupported now");

			String valueFilename = sys.getItemDirectoryPath() + File.separator
					+ "value";
			valueFile = new PlatformFile(valueFilename,
					PlatformConstants.O_RDWR);
			
			// abonnement aux changements
			PollingService.getInstance().addPollable(this);
			
			// Initialisation de la valeur
			setEvents(PlatformConstants.POLLPRI);
			
		} catch (RuntimeException ex) {
			reset();
			throw ex;
		}
	}

	@Override
	protected void reset() {
		// désabonnement aux changements
		PollingService.getInstance().removePollable(this);
		
		// fermeture du fichier de valeurs
		if(valueFile != null) {
			valueFile.close();
		}
		
		if(sys.isOpened()) {
			sys.close();
		}
	}

	/**
	 * Fichier de valeur
	 */
	private PlatformFile valueFile;

	@Override
	public boolean getValue() {
		return value;
	}

	/**
	 * Valeur
	 */
	private boolean value;

	/**
	 * Implémentation de Pollable
	 */
	@Override
	public PlatformFile getFile() {
		return valueFile;
	}

	/**
	 * Implémentation de Pollable
	 */
	@Override
	public short getCheckedEvents() {
		return PlatformConstants.POLLPRI | PlatformConstants.POLLERR;
	}

	/**
	 * Implémentation de Pollable
	 */
	@Override
	public void setEvents(short events) {

		if(events == PlatformConstants.POLLERR)
			;// logs ?
		
		if(events != PlatformConstants.POLLPRI)
			return;
		
		valueFile.lseek(0, PlatformConstants.SEEK_SET);
		byte[] data = new byte[1];
		valueFile.read(data);
		char cval = new String(data).charAt(0);
		
		this.value = cval == 1;

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
}
