package mylife.home.components;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import mylife.home.hw.api.Device;
import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.DeviceManager;
import mylife.home.hw.api.Options;
import mylife.home.net.api.NetComponent;
import mylife.home.net.api.NetComponentFactory;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;

/**
 * Implémentation de base d'un composant
 * @author pumbawoman
 *
 */
public abstract class ComponentBase {

	public interface ConfigurationBase {
		String id();
		String display();
	}
	
	private DeviceManager deviceManager;
	private NetComponentFactory netManager;
	private ConfigurationBase configuration;
	private NetComponent net;
	private final Set<Device> devices = new HashSet<Device>();

	private final Object sync = new Object();

	protected abstract Class<?> getConfigurationClass();
	
	/**
	 * Activation
	 * @param ctx
	 */
	@Activate
	public void activate(ComponentContext ctx) {
		synchronized(sync) {
			//clear();
			configuration = (ConfigurationBase)Configurable.createConfigurable(getConfigurationClass(), ctx.getProperties());
			initialize();
		}
	}

	/**
	 * Désactivation
	 * @param ctx
	 */
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		synchronized(sync) {
			terminate();
		}
	}
	
	/**
	 * Modification de configuration
	 * @param ctx
	 */
	@Modified
	public void modified(ComponentContext ctx) {
		synchronized(sync) {
			terminate();
			configuration = (ConfigurationBase)Configurable.createConfigurable(getConfigurationClass(), ctx.getProperties());
			initialize();
		}
	}
	
	@Reference
	public void setDeviceManager(DeviceManager deviceManager) {
		synchronized(sync) {
			terminate();
			this.deviceManager = deviceManager;
			initialize();
		}
	}
	
	@Reference
	public void setNetManager(NetComponentFactory netManager) {
		synchronized(sync) {
			terminate();
			this.netManager = netManager;
			initialize();
		}
	}
	
	/**
	 * Libération du composant
	 */
	protected void terminate() {
		synchronized(devices) {
			for(Device device : devices) {
				try {
					device.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			devices.clear();
		}
		
		net.close();
	}
	
	/**
	 * Initialisation du composant
	 */
	protected void initialize() {
		if(deviceManager == null)
			return;
		if(netManager == null)
			return;
		if(configuration == null)
			return;
		
		net = netManager.createComponent(configuration.id(), configuration.display(), getType());
	}
	
	protected DeviceManager getDeviceManager() {
		return deviceManager;
	}

	protected NetComponentFactory getNetManager() {
		return netManager;
	}

	protected ConfigurationBase getConfiguration() {
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
	 * Obtention d'un pin, les pin sont ensuite fermés automatiquement
	 * @param pinId
	 * @param options
	 * @return
	 * @throws DeviceAccessDeniedException 
	 */
	protected Device openPin(int pinId, EnumSet<Options> options) throws DeviceAccessDeniedException {
		synchronized(devices) {
			Device device = deviceManager.open(pinId, options);
			devices.add(device);
			return device;
		}
	}
}
