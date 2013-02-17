package mylife.home.components;

import java.util.EnumSet;
import java.util.Map;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.DeviceManager;
import mylife.home.hw.api.Options;
import mylife.home.net.api.NetComponent;
import mylife.home.net.api.NetComponentFactory;

/**
 * Implémentation de base d'un composant
 * @author pumbawoman
 *
 */
public abstract class Component {

	private final String CONFIG_PIN_ID = "pinId";
	private final String CONFIG_PIN_OPTIONS = "pinOptions";
	
	private DeviceManager deviceManager;
	private NetComponentFactory netManager;
	private String id;
	private String display;
	private Map<String, String> configuration;
	private NetComponent net;
	
	protected DeviceManager getDeviceManager() {
		return deviceManager;
	}

	protected NetComponentFactory getNetManager() {
		return netManager;
	}

	public String getId() {
		return id;
	}

	public String getDisplay() {
		return display;
	}

	protected Map<String, String> getConfiguration() {
		return configuration;
	}

	protected NetComponent getNet() {
		return net;
	}
	
	/**
	 * Obtention du type
	 * @return
	 */
	protected String getType() {
		// Implémentation par défaut : type réel de la classe
		return this.getClass().getSimpleName();
	}
	
	/**
	 * Configuration du composant
	 * @param deviceManager
	 * @param netManager
	 * @param id
	 * @param display
	 * @param configuration
	 */
	public void configure(DeviceManager deviceManager, NetComponentFactory netManager, String id, String display, Map<String, String> configuration) {
		this.deviceManager = deviceManager;
		this.netManager = netManager;
		this.id = id;
		this.display = display;
		this.configuration = configuration;
	}
	
	/**
	 * Initialisation après définition des propriétés
	 */
	public void init() {
		net = netManager.createComponent(id, display, getType());
	}
	
	/**
	 * Fin d'utilisation du composant
	 */
	public void close() throws Exception {
		net.close();
	}
	
	/**
	 * Obtention d'un pin par sa configuration
	 * @param prefix
	 * @return
	 * @throws DeviceAccessDeniedException 
	 */
	protected Device openPinFromConfiguration(String prefix) throws DeviceAccessDeniedException {
		String pinId = configuration.get("" + prefix + CONFIG_PIN_ID); 
		String pinOptions = configuration.get("" + prefix + CONFIG_PIN_OPTIONS);
		if(pinId == null)
			return null;
		if(pinOptions == null)
			return null;
		int id = Integer.parseInt(pinId);
		EnumSet<Options> options = Options.valueOfSet(pinOptions);
		return deviceManager.open(id, options);
	}
}
