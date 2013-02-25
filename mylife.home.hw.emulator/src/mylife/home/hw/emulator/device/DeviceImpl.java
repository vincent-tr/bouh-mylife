package mylife.home.hw.emulator.device;

import java.util.EnumSet;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.Options;

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

			// suppression de l'utilisation
			Manager.getInstance().close(this);
		}
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
	
	protected DeviceImpl(int pinId, EnumSet<Options> options) {
		
		this.pinId = pinId;
		this.options = options;
		
		// cr�ation du nom
		StringBuffer build = new StringBuffer();
		build.append("P");
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
	
	/**
	 * Obtention du type de device
	 * @return
	 */
	public abstract String getType();
	
	/**
	 * Obtention du status du device
	 * @return
	 */
	public abstract String getStatus();
}
