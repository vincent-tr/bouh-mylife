package mylife.home.components;

import java.util.EnumSet;

import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.DeviceManager;
import mylife.home.hw.api.InputDevice;
import mylife.home.hw.api.InputDeviceListener;
import mylife.home.hw.api.Options;
import mylife.home.net.api.NetComponent;
import mylife.home.net.api.NetComponentFactory;

import org.osgi.service.component.ComponentContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Implémentation d'un bouton
 * @author pumbawoman
 *
 */
@Component(
		provide=Object.class, 
		configurationPolicy=ConfigurationPolicy.require,
		designateFactory=Button.Configuration.class,
		immediate=true)
public class Button implements InputDeviceListener {

	@Meta.OCD(name="MyLife.Home Component Button")
	public interface Configuration {
		@Meta.AD(name="Component ID")
		String id();
		
		@Meta.AD(name="Component display")
		String display();

		@Meta.AD(name="pin ID")
		int pinId();
	}

	private DeviceManager deviceManager;
	private NetComponentFactory netManager;
	private Configuration configuration;
	private NetComponent net;
	private InputDevice device;
	
	@Reference
	public void setDeviceManager(DeviceManager deviceManager) {
		this.deviceManager = deviceManager;
	}
	
	@Reference
	public void setNetManager(NetComponentFactory netManager) {
		this.netManager = netManager;
	}

	/**
	 * Activation
	 * @param ctx
	 */
	@Activate
	public void activate(ComponentContext ctx) {
		configuration = (Configuration)Configurable.createConfigurable(Configuration.class, ctx.getProperties());
		initialize();
	}

	/**
	 * Désactivation
	 * @param ctx
	 */
	@Deactivate
	public void deactivate(ComponentContext ctx) {
		terminate();
	}
	
	/**
	 * Modification de configuration
	 * @param ctx
	 */
	@Modified
	public void modified(ComponentContext ctx) {
		terminate();
		configuration = (Configuration)Configurable.createConfigurable(Configuration.class, ctx.getProperties());
		initialize();
	}
	
	/**
	 * Libération du composant
	 */
	private void terminate() {
		if(device != null) {
			try {
				device.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		if(net != null)
			net.close();
	}
	
	/**
	 * Initialisation du composant
	 */
	private void initialize() {
		
		net = netManager.createComponent(configuration.id(), configuration.display(), Button.class.getSimpleName());
		
		try {			
			device = (InputDevice)deviceManager.open(configuration.pinId(), EnumSet.of(Options.DIRECTION_INPUT));
		} catch (DeviceAccessDeniedException e) {
			throw new RuntimeException(e);
		}
		device.addListener(this);
		stateChanged(device, device.getValue());
	}

	@Override
	public void stateChanged(InputDevice device, boolean state) {
		net.setStatus(state ? "on" : "off");
	}
}
