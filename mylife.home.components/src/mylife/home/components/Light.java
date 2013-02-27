package mylife.home.components;

import java.util.EnumSet;

import mylife.home.hw.api.DeviceAccessDeniedException;
import mylife.home.hw.api.DeviceManager;
import mylife.home.hw.api.DigitalOutputDevice;
import mylife.home.hw.api.Options;
import mylife.home.net.api.Command;
import mylife.home.net.api.CommandListener;
import mylife.home.net.api.NetComponent;
import mylife.home.net.api.NetComponentFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Lampe on/off
 * @author pumbawoman
 *
 */
@Component(
		provide=Object.class, 
		configurationPolicy=ConfigurationPolicy.require,
		designateFactory=Light.Configuration.class,
		immediate=true)
public class Light  {

	@Meta.OCD(name="MyLife.Home Component Light")
	public interface Configuration {
		@Meta.AD(name="Component ID")
		String id();
		
		@Meta.AD(name="Component display")
		String display();

		@Meta.AD(name="pin ID")
		int pinId();
	}

	private LogService log;
	private DeviceManager deviceManager;
	private NetComponentFactory netManager;
	private Configuration configuration;
	private NetComponent net;
	private DigitalOutputDevice device;
	private final Object statusLock = new Object();
	
	@Reference
	public void setDeviceManager(DeviceManager deviceManager) {
			this.deviceManager = deviceManager;
	}
	
	@Reference
	public void setNetManager(NetComponentFactory netManager) {
			this.netManager = netManager;
	}
	
	@Reference
	public void setLog(LogService log)  {
		this.log = log;
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
		
		net = netManager.createComponent(configuration.id(), configuration.display(), Light.class.getSimpleName());
		net.registerCommand("on", new OnListener());
		net.registerCommand("off", new OffListener());
		net.registerCommand("toggle", new ToggleListener());
		
		try {
			device = (DigitalOutputDevice)deviceManager.open(configuration.pinId(), EnumSet.of(Options.DIRECTION_OUTPUT, Options.TYPE_DIGITAL));
		} catch (DeviceAccessDeniedException e) {
			throw new RuntimeException(e);
		}

		changeState(false);
	}
	
	private void changeState(boolean state) {
		device.setValue(state);
		net.setStatus(state ? "on" : "off");
		log.log(LogService.LOG_DEBUG, String.format("%s (type=%s) : state changed to %b", net.getComponentId(), net.getComponentType(), state));
	}
	
	private class OnListener implements CommandListener {
		@Override
		public void execute(Command command) {
			synchronized(statusLock) {
				try {
					if(device.getValue()) {
						command.setReturnMessage(CommandReply.ALREADY_ON.getReply());
						return;
					}
					changeState(true);
					command.setReturnMessage(CommandReply.OK.getReply());
				}
				catch(Exception ex) {
					command.setReturnMessage(ex.toString());
				}
			}
		}
	}
	
	private class OffListener implements CommandListener {
		@Override
		public void execute(Command command) {
			synchronized(statusLock) {
				try {
					if(!device.getValue()) {
						command.setReturnMessage(CommandReply.ALREADY_OFF.getReply());
						return;
					}
					changeState(false);
					command.setReturnMessage(CommandReply.OK.getReply());
				}
				catch(Exception ex) {
					command.setReturnMessage(ex.toString());
				}
			}
		}
	}
	
	private class ToggleListener implements CommandListener {
		@Override
		public void execute(Command command) {
			synchronized(statusLock) {
				try {
					changeState(!device.getValue());
					command.setReturnMessage(CommandReply.OK.getReply());
				}
				catch(Exception ex) {
					command.setReturnMessage(ex.toString());
				}
			}
		}
	}
}
