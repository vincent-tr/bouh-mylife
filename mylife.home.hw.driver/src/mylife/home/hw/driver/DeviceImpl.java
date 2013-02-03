package mylife.home.hw.driver;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.Options;
import mylife.home.hw.driver.layout.LayoutManager;
import mylife.home.hw.driver.layout.PinLayout;

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
	}

	/**
	 * Vérifie si l'objet est fermé
	 */
	protected void checkClosed() {
		if (closed)
			throw new IllegalStateException("Object closed");
	}
	
	private final int pinId;
	private final int gpioId;
	private final String name;
	private final EnumSet<Options> options;
	
	protected DeviceImpl(int pinId, EnumSet<Options> options) {
		
		this.pinId = pinId;
		this.options = options;
		
		// gestion du layout
		PinLayout pinLayout = LayoutManager.getInstance().getPinLayout();
		this.gpioId = pinLayout.pinToGpio(pinId);
		
		// création du nom
		StringBuffer build = new StringBuffer();
		build.append("P");
		build.append(pinId);
		build.append(" GPIO ");
		build.append(gpioId);
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
	
	public int getGpioId() {
		return gpioId;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public EnumSet<Options> getOptions() {
		return options;
	}
}
